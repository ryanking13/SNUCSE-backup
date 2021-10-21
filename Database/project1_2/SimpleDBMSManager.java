import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class SimpleDBMSManager {
  public Environment environment = null;
  public Database database = null;
  public EnvironmentConfig envCfg = null;
  public DatabaseConfig dbCfg = null;
  
  SimpleDBMSManager() {
    envCfg = new EnvironmentConfig();
    envCfg.setAllowCreate(true);
    
    File d = new File("db/");
    if (!d.exists()) { d.mkdir(); }
    environment = new Environment(new File("db/"), envCfg);
    dbCfg = new DatabaseConfig();
    dbCfg.setAllowCreate(true);
    dbCfg.setSortedDuplicates(true);
    database = environment.openDatabase(null, "DatabaseProject1_2", dbCfg);
  }
  
  /***** API method *****/
  
  public String createTable(SimpleDBMSSchema schema) throws SimpleDBMSException {
      
    // Error case: [TableExistenceError] if table already exists
    if (checkEntry(tableToKey(schema.name)))
      throw new TableExistenceError();
    
    // checking duplicates
    
    // Error case: [DuplicateColumnDefError] if column name duplicates
    if (checkDuplicateCol(schema.columns) != null)
      throw new DuplicateColumnDefError();

    // Error case: [DuplicateColumnDefError] if primary key name duplicates
    if (checkDuplicateStr(schema.primaryKeyColumns) != null)
      throw new DuplicateColumnDefError();
    
    // Error case: [DuplicateColumnDefError] if foreign key name duplicates
    if (checkDuplicateStr(schema.foreignKeyColumns) != null)
      throw new DuplicateColumnDefError();
    
    // checking primary key
    
    if (schema.duplicatePrimaryKeyDefinition)
      throw new SimpleDBMSException("Create table has failed: primary key definition is duplicated");
    
    for(int i = 0; i < schema.primaryKeyColumns.size(); i++) {
      String pkey = schema.primaryKeyColumns.get(i);
      SimpleDBMSColumn pcol = findColumnByName(schema.columns, pkey);
      
      // Error case: [NonExistingColumnDefError] column name defined for foreign key not exists
      if (pcol == null)
        throw new NonExistingColumnDefError(pkey);
      
      // No error
      pcol.setPrimaryKey(true);
      pcol.setNotNull(true);
    }
    
    // checking foreign key
    
    // Error case: [ReferenceTypeError]
    if (schema.wrongLengthForeignKeyDefinition) 
      throw new ReferenceTypeError();
    
    if (schema.referenceKeys.size() != 0) {
      
      int pkeyCount = 0;
      int curFkeyPairIdx = 0;
      int curFkeyPairEnd = schema.foreignKeyPairSizes.get(curFkeyPairIdx);
      for(int i = 0; i < schema.foreignKeyColumns.size(); i++) {
        Reference reference = schema.referenceKeys.get(i);
        
        // Error case: [ReferenceTableExistenceError] referenceTable not exists in DB
        if (!checkEntry(tableToKey(reference.table)))
          throw new ReferenceTableExistenceError();
        
        // ADDITIONAL Error case: referenceTable is itself
        if (schema.name == reference.table)
          throw new SimpleDBMSException("Create table has failed: foreign key references table itself");
        
        String referenceColumnKey = columnToKey(reference.table, reference.column);
        String fkey = schema.foreignKeyColumns.get(i);
        SimpleDBMSColumn here = findColumnByName(schema.columns, fkey);
        // Error case: [NonExistingColumnDefError] column name defined for foreign key not exists
        if (here == null)
          throw new NonExistingColumnDefError(fkey);
          
        // Error case: [ReferenceColumnExistenceError] if no referenced column in DB
        if(!checkEntry(referenceColumnKey))
          throw new ReferenceColumnExistenceError();
        
        SimpleDBMSColumn there = loadColumn(getEntry(referenceColumnKey));
        
        // Error case: [ReferenceTypeError] if type mismatch for two columns
        if (!here.type.equals(there.type) || here.length != there.length)
          throw new ReferenceTypeError();
        
        // Error case : [ReferenceNonPrimaryKeyError] if reference column is not primary key
        if (!there.primaryKey)
          throw new ReferenceNonPrimaryKeyError();
        
        pkeyCount += 1;
        
        if (i == curFkeyPairEnd - 1) {
          SimpleDBMSSchema thereTable = loadTable(getEntry(tableToKey(reference.table)));
          // Error case : [ReferenceNonPrimaryKeyError] if reference column is not primary key
          if (thereTable.primaryKeyColumns.size() != pkeyCount) {
            throw new ReferenceNonPrimaryKeyError();
          }
          pkeyCount = 0;
          curFkeyPairIdx += 1;
          if (curFkeyPairIdx < schema.foreignKeyPairSizes.size()) {
            curFkeyPairEnd += schema.foreignKeyPairSizes.get(curFkeyPairIdx);
          }
        }
        
        // No Error
        here.setForeignKey(true);
      }
    }
      
    // checking each columns
    for(int i = 0; i < schema.columns.size(); i++) {
      SimpleDBMSColumn col = schema.columns.get(i);
      
      // Error case: [CharLengthError]
      if (col.type == "char" && col.length < 1)
        throw new CharLengthError();
    }
    
    // No error, add schema and columns to database
    
    String tableKey = tableToKey(schema.name);
    String tableValue = dumpTable(schema);
    pushEntry(tableKey, tableValue);
    
    for(int i = 0; i < schema.columns.size(); i++) {
      SimpleDBMSColumn col = schema.columns.get(i);
      String columnKey = columnToKey(schema.name, col.name);
      String columnValue = dumpColumn(col);
      pushEntry(columnKey, columnValue);
    }
    
    if (schema.referenceKeys != null) {
      String reftoKey = reftoToKey(schema.name);
      for(int i = 0; i < schema.foreignKeyColumns.size(); i++) {
        Reference reference = schema.referenceKeys.get(i);
        String here = schema.foreignKeyColumns.get(i);
        String reffromKey = reffromToKey(reference.table);
        
        String reftoValue = dumpReferenceRelation(here, reference.table, reference.column);
        String reffromValue = dumpReferenceRelation(reference.column, schema.name, here);
        // Add both side relation (reference To / reference From) to database
        pushEntry(reftoKey, reftoValue);
        pushEntry(reffromKey, reffromValue);
      }
    }
    
    return schema.name;
  }
  
  public String dropTable(String tableName) throws SimpleDBMSException{
    String tableKey = tableToKey(tableName);
    
    // Error case: [NoSuchTable]
    if (!checkEntry(tableKey))
      throw new NoSuchTable();
    
    SimpleDBMSSchema schema = loadTable(getEntry(tableKey));
    String reffromKey = reffromToKey(schema.name);
    
    // Error case: [DropReferencedTableError]
    if (checkEntry(reffromKey))
      throw new DropReferencedTableError(schema.name);
    
    // No error, delete all entries related to this table
    popEntryAll(tableKey); // table entry
    for(SimpleDBMSColumn col: schema.columns) { // column entries
      popEntryAll(columnToKey(schema.name, col.name));
    }
    
    popEntryAll(reftoToKey(schema.name)); // reference (To) entries
    for(int i = 0; i < schema.foreignKeyColumns.size(); i++) { // reference (From) entries
      String here = schema.foreignKeyColumns.get(i);
      Reference ref = schema.referenceKeys.get(i);
      popEntry(reffromToKey(ref.table), dumpReferenceRelation(ref.column, schema.name, here));
    }
    
    return tableName;
  }
  
  public String desc(String tableName) throws SimpleDBMSException{
    String tableKey = tableToKey(tableName);
    
    // Error case: [NoSuchTable]
    if (!checkEntry(tableKey))
      throw new NoSuchTable();
    
    String tableValue = getEntry(tableKey);
    SimpleDBMSSchema schema = loadTable(tableValue);
    
    StringBuilder b = new StringBuilder();
    b.append("-------------------------------------------------\n");
    b.append(String.format("table_name [%s]\n", schema.name));
    b.append("column_name\ttype\tnull\tkey\n");
    for(SimpleDBMSColumn col: schema.columns) {
      b.append(String.format("%-11s", col.name));
      b.append("\t"); b.append(col.type);
      if (col.type == "char") b.append(String.format("(%d)", col.length));
      b.append("\t"); b.append(col.notNull ? "N" : "Y");
      b.append("\t");
      if (col.primaryKey && col.foreignKey) b.append("PRI/FOR");
      else if (col.primaryKey) b.append("PRI");
      else if (col.foreignKey) b.append("FOR");
      b.append("\n");
    }
    b.append("-------------------------------------------------");
    
    return b.toString();
  }
  
  public String showTables() throws SimpleDBMSException {
    String tableKeyPrefix = tableToKey("");
    ArrayList<String> tables = getEntryAllRange(tableKeyPrefix);
    
    // Error case: [ShowTablesNoTable]
    if (tables.size() == 0)
      throw new ShowTablesNoTable();
    
    StringBuilder b = new StringBuilder();
    b.append("----------------");
    for(String table: tables) {
      SimpleDBMSSchema schema = loadTable(table);
      b.append("\n"); b.append(schema.name);
    }
    b.append("\n----------------");
    return b.toString();
  }
  
  /******************************/
  
  /***** DB access method *****/
  
  // push entry to database
  private void pushEntry(String akey, String adata) {
    try {
      DatabaseEntry key = new DatabaseEntry(akey.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry(adata.getBytes("UTF-8"));
      database.put(null, key, data);
//      System.out.println(String.format("Insert: %s", akey));
    } catch (DatabaseException de) { // should not happen
      de.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
  
  // delete all entries with akey from database
  private void popEntryAll(String akey) {
    DatabaseEntry key;
    try {
      key = new DatabaseEntry(akey.getBytes("UTF-8"));
      database.delete(null, key);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
  
  // delete entries with akey/adata from database
  private void popEntry(String akey, String adata) {
    Cursor cursor = null;
    try {
      cursor = database.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(akey.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry(adata.getBytes("UTF-8"));

      cursor.getSearchBoth(key, data, LockMode.DEFAULT);
      cursor.delete();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) cursor.close();
    }
  }
  
  // get first entry from database
  private String getEntry(String akey) throws SimpleDBMSException{
    try {
      DatabaseEntry key = new DatabaseEntry(akey.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      
      if (database.get(null, key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
        return new String(data.getData(), "UTF-8");
      } else {
        throw new SimpleDBMSException("No Such Entry");
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    throw new SimpleDBMSException("Unhandled Error");
  }
  
  // get all entries with akey from database
  private ArrayList<String> getEntryAll(String akey) throws SimpleDBMSException {
    Cursor cursor = null;
    ArrayList<String> entries = new ArrayList<String>();
    try {
      cursor = database.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(akey.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      
      if (cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.NOTFOUND) {
        throw new SimpleDBMSException("No Such Entry");
      }
      
      do {
        entries.add(new String(data.getData(), "UTF-8"));
      } while(cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);
      
      return entries;
      
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) cursor.close();
    }
    
    throw new SimpleDBMSException("Unhandled Error");
  }
  
  // get all entries with akey PREFIX from database
  private ArrayList<String> getEntryAllRange(String akey) throws SimpleDBMSException {
    Cursor cursor = null;
    ArrayList<String> entries = new ArrayList<String>();
    try {
      cursor = database.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(akey.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      
      if (cursor.getSearchKeyRange(key, data, LockMode.DEFAULT) == OperationStatus.NOTFOUND) {
        return entries;
      }
      
      do {
        entries.add(new String(data.getData(), "UTF-8"));
      } while(cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);
      
      return entries;
      
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) cursor.close();
    }
    
    throw new SimpleDBMSException("Unhandled Error");
  }
  
  // check if database have the entry
  private boolean checkEntry(String akey) {
    try {
      DatabaseEntry key = new DatabaseEntry(akey.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      
      if (database.get(null, key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
        return true;
      } else {
        return false;
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    return false;
  }
  
  /******************************/
  
  /***** Helper functions *****/
  
  // convert table name to database key
  private String tableToKey(String tableName) {
    return String.format("table-%s", tableName);
  }
  
  // convert column to database key
  private String columnToKey(String tableName, String columnName) {
    return String.format("column-%s-%s", tableName, columnName);
  }
  
  // convert foreign key (To) to database key
  private String reftoToKey(String tableName) {
    return String.format("reference-to-%s",  tableName);
  }
  
  // convert foreign key (From) to database key
  private String reffromToKey(String tableName) {
    return String.format("reference-from-%s", tableName);
  }
  
  
  //dump SimpleDBMSSchema object to string
  private String dumpTable(SimpleDBMSSchema schema) {
    // format: 
    // <table> | <column size> | <column1> | ... | <columnN> |
    // <primary key size> | <pcolumn1> | ... | <pcolumnN> |
    // <foreign key size> | <fcolumn1> | <rtable1> | <rcolumn1> | ... | <fcolumnN> | <rtableN> | <rcolumnN> |
    // <foreign key pair size> | <fpairsize1> | <fpairsize2> | ... | <fpairsizeN>
    
    StringBuilder b = new StringBuilder();
    b.append(schema.name);
    
    int colSize = schema.columns == null ? 0 : schema.columns.size();
    b.append("|"); b.append(Integer.toString(colSize));
    for(SimpleDBMSColumn col: schema.columns) {
      b.append("|"); b.append(col.name);
    }
    
    int pkeySize = schema.primaryKeyColumns == null ? 0 : schema.primaryKeyColumns.size();
    b.append("|"); b.append(Integer.toString(pkeySize));
    for(String pkey: schema.primaryKeyColumns) {
      b.append("|"); b.append(pkey);
    }
    
    int fkeySize = schema.foreignKeyColumns == null ? 0 : schema.foreignKeyColumns.size();
    b.append("|"); b.append(Integer.toString(fkeySize));
    for(int i = 0; i < fkeySize; i++) {
      b.append("|"); b.append(schema.foreignKeyColumns.get(i));
      b.append("|"); b.append(schema.referenceKeys.get(i).table);
      b.append("|"); b.append(schema.referenceKeys.get(i).column);
    }

    int fkeyPairSize = schema.foreignKeyPairSizes == null ? 0 : schema.foreignKeyPairSizes.size();
    b.append("|"); b.append(Integer.toString(fkeyPairSize));
    for(int i = 0; i < fkeyPairSize; i++) {
      b.append("|"); b.append(Integer.toString(schema.foreignKeyPairSizes.get(i)));
    }
    
    return b.toString();
  }

  // dump SimpleDBMSColumn object to string
  private String dumpColumn(SimpleDBMSColumn col) {
    // format:
    // <name> | <type> | <length> | <not null> | <primary key> | <foreign Key>
    
    StringBuilder b = new StringBuilder();
    b.append(col.name); 
    b.append("|"); b.append(col.type); 
    b.append("|"); b.append(Integer.toString(col.length)); 
    b.append("|"); b.append(String.valueOf(col.notNull));
    b.append("|"); b.append(String.valueOf(col.primaryKey)); 
    b.append("|"); b.append(String.valueOf(col.foreignKey)); 
    
    return b.toString();
  }

  // dump foreign key reference data to string
  private String dumpReferenceRelation(String column, String otherTable, String otherColumn) {
    // format: <column> | <reference[d] table > | <reference[d] column>
    return String.format("%s|%s|%s", column, otherTable, otherColumn);
  }
  
  // load string to SimpleDBMSSchema
  private SimpleDBMSSchema loadTable(String str) throws SimpleDBMSException {
    String[] s = str.split("\\|");
    SimpleDBMSSchema schema = new SimpleDBMSSchema(s[0]);
    int colSizeIdx = 1;
    int pkeySizeIdx = colSizeIdx + 1 + Integer.parseInt(s[colSizeIdx]);
    int fkeySizeIdx = pkeySizeIdx + 1 + Integer.parseInt(s[pkeySizeIdx]);
    int fkeyPairSizeIdx = fkeySizeIdx + 1 + Integer.parseInt(s[fkeySizeIdx]) * 3;

    for (int i = colSizeIdx + 1; i < pkeySizeIdx; i++) {
      String columnKey = columnToKey(s[0], s[i]);
      String columnValue = getEntry(columnKey);
      SimpleDBMSColumn col = loadColumn(columnValue);
      schema.addColumn(col);
    }
    
    ArrayList<String> pkey = new ArrayList<String>();
    for (int i = pkeySizeIdx + 1; i < fkeySizeIdx; i++) pkey.add(s[i]);
    schema.addPrimaryKey(pkey);
    
    for (int i = fkeySizeIdx + 1; i < fkeyPairSizeIdx; i+= 3) {
      schema.addForeignKeyEach(s[i], s[i + 1], s[i + 2]);
    }

    for (int i = fkeyPairSizeIdx + 1; i < s.length; i++) {
      schema.addForeignKeyPairSize(Integer.parseInt(s[i]));
    }
    
    return schema;
  }
  
  // load string to SimpleDBMSColumn
  private SimpleDBMSColumn loadColumn(String str) {
    String[] s = str.split("\\|");
    SimpleDBMSColumn col = new SimpleDBMSColumn(s[0]);
    col.setType(s[1]);
    col.setLength(Integer.parseInt(s[2]));
    col.setNotNull(Boolean.parseBoolean(s[3]));
    col.setPrimaryKey(Boolean.parseBoolean(s[4]));
    col.setForeignKey(Boolean.parseBoolean(s[5]));
    return col;
  }
  
  // find SimpleDBMSColumn object by column name
  private SimpleDBMSColumn findColumnByName(ArrayList<SimpleDBMSColumn> columns, String name) {
    for(int i = 0; i < columns.size(); i++) {
      if (columns.get(i).name.equals(name)) return columns.get(i);
    }
    
    return null;
  }
  
  // check duplicate in string list
  private String checkDuplicateStr(ArrayList<String> arr) {
    if (arr == null) return null;
    
    int length = arr.size();
    for (int i = 0; i < length; i++) {
      String si = arr.get(i);
      for (int j = i + 1; j < length; j++) {
        String sj = arr.get(j);
        if (si.equals(sj)) return si;
      }
    }
    return null;
  }
  
  // check duplicate in SimpleDBMSColumn list
  private String checkDuplicateCol(ArrayList<SimpleDBMSColumn> arr) {
    if (arr == null) return null;
    
    int length = arr.size();
    for (int i = 0; i < length; i++) {
      String si = arr.get(i).name;
      for (int j = i + 1; j < length; j++) {
        String sj = arr.get(j).name;
        if (si.equals(sj)) return si;
      }
    }
    return null;
  }
  
  /******************************/
  
  // close database
  public void close() {
    if (database != null) database.close();
    if (environment != null) environment.close();
  }
}

// key-value pair storage
class KeyValue<K, V> {
  public K key;
  public V value;
  
  KeyValue(K key, V value) {
    this.key = key;
    this.value = value;
  }
}