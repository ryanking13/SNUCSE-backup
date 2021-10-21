import java.io.File;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

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
        if (schema.name.equals(reference.table))
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
      if (col.type.equals("char") && col.length < 1)
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
      if (col.type.equals("char")) b.append(String.format("(%d)", col.length));
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
  
  public void insert(SimpleDBMSInsertQuery query) throws SimpleDBMSException {
    String tableName = query.tableName;
    ArrayList<String> columns = query.columns;
    ArrayList<QueryValue> values = query.values;
    String tableKey = tableToKey(tableName);
    
    // Error case: [NoSuchTable]
    if (!checkEntry(tableKey)) {
      throw new NoSuchTable();
    }
    
    String tableValue = getEntry(tableKey);
    SimpleDBMSSchema schema = loadTable(tableValue);
    
    // if target columns are not specified, compare all columns
    if (columns == null) {
      
      // Error case: size mismatch [InsertTypeMismatchError]
      if (values.size() != schema.columns.size()) throw new InsertTypeMismatchError();
      
      for(int i = 0; i < values.size(); i++) {
        QueryValue q = values.get(i);
        SimpleDBMSColumn c = schema.columns.get(i);
        
        // Error case: type mismatch [InsertTypeMismatchError]
        if ((!q.type.equals(c.type)) && !q.type.equals("null")) throw new InsertTypeMismatchError();
      }
    } else {
      // Error case: size mismatch [InsertTypeMismatchError]
      if (columns.size() < values.size()) throw new InsertTypeMismatchError();
      
      for(int i = 0; i < values.size(); i++) {
        QueryValue q = values.get(i);
        String colName = columns.get(i);
        SimpleDBMSColumn c = findColumnByName(schema.columns, colName);
        
        // Error case : target column not exists [InsertColumnExistenceError]
        if (c == null) throw new InsertColumnExistenceError(colName);
        
        // Error case: type mismatch [InsertTypeMismatchError]
        if (!q.type.equals(c.type) && !q.type.equals("null")) throw new InsertTypeMismatchError();
      }
    }
    
    // values with all columns filled (with null values)
    ArrayList<QueryValue> fullValues = new ArrayList<QueryValue>();
    if (columns == null) fullValues = values;
    else {
      for(SimpleDBMSColumn c: schema.columns) {
        int colIdx = columns.indexOf(c.name);
        
        if (colIdx == -1) fullValues.add(new QueryValue("null", "_"));
        else fullValues.add(values.get(colIdx));
      }
    }
    
    // if some values are not specified, set it to null
    while (fullValues.size() < schema.columns.size()) {
      fullValues.add(new QueryValue("null", "_"));
    }
    
    // check if any foreign key have null value
    boolean haveNullForeignKey = false;
    for (int i = 0; i < fullValues.size(); i++) {
      QueryValue q = fullValues.get(i);
      SimpleDBMSColumn c = schema.columns.get(i);
      
      if (c.foreignKey && q.type.equals("null")) haveNullForeignKey = true;
    }
    
    // truncate char type values
    for (int i = 0; i < fullValues.size(); i++) {
      QueryValue q = fullValues.get(i);
      SimpleDBMSColumn c = schema.columns.get(i);
      
      // remove quotes
      if (c.type.equals("char")) {
        q.value = q.value.replaceAll("^\'|\'$", "");
        
        // if length is longer than limit, truncate it
        if (c.length < q.value.length()) {
          q.value = q.value.substring(0, c.length);
        }
        
        fullValues.set(i, q);
      }
    }

    // checking constraints
    
    String refKey = reftoToKey(tableName);
    ArrayList<String> referenceList = getEntryAll(refKey);
    ArrayList<ReferenceRelation> references = new ArrayList<ReferenceRelation>();
    for (String ref: referenceList) references.add(loadReference(ref));

    for(int i = 0; i < fullValues.size(); i++) {
      QueryValue q = fullValues.get(i);
      SimpleDBMSColumn c = schema.columns.get(i);
      
      // Error case: [InsertColumnNonNullableError]
      if (c.notNull && q.type.equals("null")) throw new InsertColumnNonNullableError(c.name);
      
      // if c is foreign key, check it's value is in referencing table
      if (c.foreignKey && !haveNullForeignKey) {
        boolean found = false;
        
        for (ReferenceRelation rel: references) {
          if (c.name.equals(rel.myColumn)) {
            String otherTableKey = tableToKey(rel.ref.table);
            String otherRowKey = rowToKey(rel.ref.table);
            String otherTableValue = getEntry(otherTableKey);

            SimpleDBMSSchema otherTable = loadTable(otherTableValue);
            int colIdx = findColumnIdxByName(otherTable.columns, rel.ref.column);
            ArrayList<String> otherRowValues = getEntryAll(otherRowKey);

            for(String rowStr: otherRowValues) {
              ArrayList<QueryValue> row = loadRow(rowStr);
              if (row.get(colIdx).value.equals(q.value)) {
                found = true;
                break;
              }
            }
          }
          if (found) break;
        }
        
        // Error case: matching value not found: [InsertReferentialIntegrityError]
        if (!found) throw new InsertReferentialIntegrityError();
      }
    }
    
    // check there is duplicate primary key
    String rowKey = rowToKey(schema.name);
    ArrayList<String> rowValues = getEntryAll(rowKey);
    for (String rowStr: rowValues) {
      boolean dup = true;
      ArrayList<QueryValue> row = loadRow(rowStr);
      if (schema.primaryKeyColumns.size() == 0) dup = false;
      
      for(String pkey: schema.primaryKeyColumns) {
        int pkeyIdx = findColumnIdxByName(schema.columns, pkey);
        if (!row.get(pkeyIdx).value.equals(values.get(pkeyIdx).value)) {
          dup = false;
          break;
        }
      }
      
      // duplication found
      if (dup) throw new InsertDuplicatePrimaryKeyError();
    }
    
    pushEntry(rowKey, dumpRow(fullValues));
    return;
  }
  
  public Pair<Integer, Integer> delete(SimpleDBMSDeleteQuery query) throws SimpleDBMSException {
    int deletedCnt = 0;
    int notDeletedCnt = 0;
    String tableName = query.tableName;
    PredicateTree condition = query.condition;
    
    String tableKey = tableToKey(tableName);
    String rowKey = rowToKey(tableName);

    // Error case: table not exists
    if (!checkEntry(tableKey)) throw new NoSuchTable();
    
    ArrayList<String> rows = getEntryAll(rowKey);
    ArrayList<ArrayList<OperandTuple>> allEntries = new ArrayList<ArrayList<OperandTuple>>();
    for (String r: rows) {
      ArrayList<QueryValue> row = loadRow(r);
      OperandTuple tuple = new OperandTuple(tableName, tableName, row);
      ArrayList<OperandTuple> _tuple = new ArrayList<OperandTuple>();
      _tuple.add(tuple);
      allEntries.add(_tuple);
    }

    // generate list of references
    Set<String> rels = new HashSet<String>();
    ArrayList<String> refDump = getEntryAll(reffromToKey(tableName));
    for(String ref: refDump) {
      rels.add(loadReference(ref).ref.table);
    }
    

    SimpleDBMSSchema schema = loadTable(getEntry(tableKey));
    for(ArrayList<OperandTuple> tupleSet: allEntries) {
      if(condition == null || condition.eval(tupleSet, this)) {

        ArrayList<QueryValue> myRow = tupleSet.get(0).row;
        boolean notNullExists = false;

        // for each table which references this table
        ArrayList<String> affectedRows = new ArrayList<String>(); // rows that are affected by deletion
        ArrayList<SimpleDBMSSchema> affectedTables = new ArrayList<SimpleDBMSSchema>(); // rows that are affected by deletion
        for (String thereTable: rels) {
          SimpleDBMSSchema thereSchema = loadTable(getEntry(tableToKey(thereTable)));
          ArrayList<String> thereRowsDump = getEntryAll(rowToKey(thereTable));

          // generate foreign key pairs
          ArrayList<Pair<Integer, Integer>> refList = new ArrayList<Pair<Integer, Integer>>();
          for(int i = 0; i < thereSchema.foreignKeyColumns.size(); i++) {

            // if foreign key is not referencing this table, ignore it
            if (!thereSchema.referenceKeys.get(i).table.equals(tableName)) continue;
            
            String hereColumn = thereSchema.referenceKeys.get(i).column;
            String thereColumn = thereSchema.foreignKeyColumns.get(i);
            int hereColumnIdx = findColumnIdxByName(schema.columns, hereColumn);
            int thereColumnIdx = findColumnIdxByName(thereSchema.columns, thereColumn);
            
            refList.add(new Pair<Integer, Integer>(hereColumnIdx, thereColumnIdx));
          }

          // now iterate all rows in there table, check relational integrity
          for (String dump: thereRowsDump) {
            ArrayList<QueryValue> thereRow = loadRow(dump);
            
            int matchCnt = 0;
            int notNullCnt = 0;
            for(Pair<Integer, Integer> r: refList) {
              // there is a referencing column with same value and it is not null
              if (myRow.get(r.first).value.equals(thereRow.get(r.second).value) && !thereRow.get(r.second).type.equals("null")) {
                matchCnt += 1;
                // if referencing column cannot be null
                if (thereSchema.columns.get(r.second).notNull) notNullCnt += 1;
              } 
            }
            
            // there is a row with all columns match
            if (matchCnt == refList.size()) {
              affectedRows.add(dump);
              affectedTables.add(thereSchema);
              // and there is not nullable column
              if (notNullCnt > 0) notNullExists = true;
            }
          }
        }
        
        // now, cascade
        if (notNullExists) { // if not null exists, delete fail, nothing happens
          notDeletedCnt += 1;
        } else { // it not null not exists, do cascade deletion
          for (int i = 0; i < affectedRows.size(); i++) {
            String row = affectedRows.get(i);
            SimpleDBMSSchema table = affectedTables.get(i);
            ArrayList<QueryValue> qv = loadRow(row);
            popEntry(rowToKey(table.name), row); // remove original entry
            
            for (int j = 0; j < table.foreignKeyColumns.size(); j++) {
              if (!table.referenceKeys.get(j).table.equals(tableName)) continue;
              
              String col = table.foreignKeyColumns.get(j);
              int colIdx = findColumnIdxByName(table.columns, col);
              
              // set value to null
              qv.get(colIdx).type = "null";
              qv.get(colIdx).value = "_";
            }
            
            pushEntry(rowToKey(table.name), dumpRow(qv)); // push new entry with null masked
          }

          popEntry(rowKey, dumpRow(myRow)); // delete target row
          deletedCnt += 1;
        }
      }
    }
    
//    // generate list of references
//    ArrayList<ReferenceRelation> rels = new ArrayList<ReferenceRelation>();
//    ArrayList<String> refDump = getEntryAll(reffromToKey(tableName));
//    for(String ref: refDump) {
//      rels.add(loadReference(ref));
//    }
//    
//    SimpleDBMSSchema schema = loadTable(getEntry(tableKey));
//    for(ArrayList<OperandTuple> tupleSet: allEntries) {
//      if(condition == null || condition.eval(tupleSet, this)) {
//        ArrayList<QueryValue> myRow = tupleSet.get(0).row;
//        boolean notNullExists = false;
//        
//        for (ReferenceRelation rel: rels) {
//          SimpleDBMSSchema thereSchema = loadTable(tableToKey(rel.ref.table));
//          ArrayList<String> thereRowsDump = getEntryAll(rowToKey(rel.ref.table));
//          int colIdx = findColumnIdxByName(schema.columns, rel.myColumn);
//          int thereColIdx = findColumnIdxByName(thereSchema.columns, rel.ref.column);
//          
//          for(String dump: thereRowsDump) {
//            ArrayList<QueryValue> thereRow = loadRow(dump);
//            
//            // there is a referencing column
//            if (myRow.get(colIdx).value.equals(thereRow.get(thereColIdx).value)) {
//              // if referencing column cannot be null
//              if (thereSchema.columns.get(thereColIdx).notNull) notNullExists = true;
//            }
//          }
//        }
//        
//        if (notNullExists) {
//          notDeletedCnt += 1;
//        } else {
//            for (ReferenceRelation rel: rels) {
//              SimpleDBMSSchema thereSchema = loadTable(tableToKey(rel.ref.table));
//              ArrayList<String> thereRowsDump = getEntryAll(rowToKey(rel.ref.table));
//              int colIdx = findColumnIdxByName(schema.columns, rel.myColumn);
//              int thereColIdx = findColumnIdxByName(thereSchema.columns, rel.ref.column);
//              
//              for(String dump: thereRowsDump) {
//                ArrayList<QueryValue> thereRow = loadRow(dump);
//                
//                // there is a referencing column
//                if (myRow.get(colIdx).value.equals(thereRow.get(thereColIdx).value)) {
//                  // delete old row
//                  popEntry(rowToKey(rel.ref.table), dump);
//                  
//                  // change to null values
//                  thereRow.get(thereColIdx).type = "null";
//                  thereRow.get(thereColIdx).value = "";
//                  
//                  // push new row
//                  pushEntry(rowToKey(rel.ref.table), dumpRow(thereRow));
//                }
//              }
//            }          
//          // remove target row
//          popEntry(rowKey, dumpRow(myRow));
//          deletedCnt += 1;
//        }
//      }
//    }
    
    return new Pair<Integer, Integer>(deletedCnt, notDeletedCnt);
  }
  
  public String select(SimpleDBMSSelectQuery query) throws SimpleDBMSException {
    boolean selectAll = query.asterisk;
    ArrayList<Selection> selection = query.selection;
    ArrayList<From> from = query.from;
    PredicateTree condition = query.condition;
    
    // check table names in `from`
    for(From f: from) {
      if (f.table != null) {
        String tableName = f.table;
        String tableKey = tableToKey(tableName);
        
        // Error case: table name in `from` not exists
        if (!checkEntry(tableKey)) throw new SelectTableExistenceError(tableName);
      }
    }

    
    // check column names in `selection`
    for (Selection s: selection) {
      String tableName = s.table;
      String columnName = s.column;
      
      // Case 1: table is not specified
      if (tableName == null) {
        boolean columnFound = false;
        for (From f: from) {
          String fromTableName = f.table;
          String fromTableKey = tableToKey(fromTableName);
          SimpleDBMSSchema fromTableSchema = loadTable(getEntry(fromTableKey));
          if (findColumnByName(fromTableSchema.columns, columnName) != null) {
            
            // if matching column is already found, then column name is ambiguous
            // Error case: SelectColumnResolveError
            if (columnFound) throw new SelectColumnResolveError(columnName);
            
            // else, matching column is found
            columnFound = true;
            s.table = (f.alias == null) ? f.table : f.alias;
          }
        }
        
        // if column is not found, then column name cannot be resolved
        // Error case: SelectColumnResolveError
        if (!columnFound) throw new SelectColumnResolveError(columnName);
      }
      
      // Case 2: table is specified
      else {
        boolean columnFound = false;
        for (From f: from) {
          String fromTableName = f.table;
          String fromTableNameAlias = f.alias;
          
          // if table has it's alias, compare name with alias,
          // else compare name with original table name
          if ((fromTableNameAlias != null && tableName.equals(fromTableNameAlias)) ||
              (fromTableNameAlias == null && tableName.equals(fromTableName))) {
            String fromTableKey = tableToKey(fromTableName);
            SimpleDBMSSchema fromTableSchema = loadTable(getEntry(fromTableKey));
            
            if (findColumnByName(fromTableSchema.columns, columnName) != null) {
              columnFound = true;
            }
          }
        }
        
        if (!columnFound) throw new SelectColumnResolveError(String.format("%s.%s", tableName, columnName));
      }
    }

    // load all entries specified in `from`
    ArrayList<ArrayList<OperandTuple>> allEntries = new ArrayList<ArrayList<OperandTuple>>();
    
    for(From f: from) {
      ArrayList<OperandTuple> tuples = new ArrayList<OperandTuple>();
      
      String rowKey = rowToKey(f.table);
      String refName = (f.alias == null) ?  f.table : f.alias;
      ArrayList<String> rows = getEntryAll(rowKey);
      
//      if (rows.size() == 0) {
//        tuples.add(new OperandTuple(f.table, refName, null)); // add empty tuple to prevent cartesian product becomes empty set
//      }
      
      for(String r: rows) {
        ArrayList<QueryValue> row = loadRow(r);
        tuples.add(new OperandTuple(f.table, refName, row));
      }
      
      allEntries.add(tuples);
    }

    // get all cartesian product
    List<ArrayList<OperandTuple>> product = CartesianProduct.calculate(allEntries);
    
    // if asterisk(*), add all columns to selection
    if (selectAll) {
      selection = new ArrayList<Selection>();
      for (From f: from) {
        String fromTable = f.table;
        String fromRefName = (f.alias == null) ? f.table : f.alias;
        String fromTableKey = tableToKey(fromTable);
        SimpleDBMSSchema fromTableSchema = loadTable(getEntry(fromTableKey));
        for (SimpleDBMSColumn col: fromTableSchema.columns) {
          selection.add(new Selection(fromRefName, col.name, null)); 
        }
      }
    }

    StringBuilder b = new StringBuilder();
    for(int i = 0; i < selection.size(); i++) {
      b.append("+--------------------");
    }
    b.append("+\n");
    
    b.append("|");
    for(Selection s: selection) {
      if (s.alias != null) {
        b.append(String.format("%-20s", s.alias));
      } else if (checkDuplicateSelection(s, selection)) {
        b.append(String.format("%-20s", String.format("%s.%s", s.table, s.column)));
      } else {
        b.append(String.format("%-20s", s.column));
      }
//      if (s.alias != null) b.append(String.format("%-20s", s.alias));
//      else {
//        if (s.table != null) b.append(String.format("%-20s", String.format("%s.%s", s.table, s.column)));
//        else b.append(String.format("%-20s", s.column));
//      }
      b.append("|");
    }
    b.append("\n");
    for(int i = 0; i < selection.size(); i++) {
      b.append("+--------------------");
    }
    b.append("+\n");
    
    /////
    
    
    for(ArrayList<OperandTuple> tupleSet: product) {
      if(condition == null || condition.eval(tupleSet, this)) {
        // add tuple to select result
        b.append("|");
        for(Selection s: selection) {
          QueryValue selected = null;
          String selectedTable = s.table;
          String selectedColumn = s.column;

          for(OperandTuple t: tupleSet) {
            SimpleDBMSSchema tupleSchema = loadTable(getEntry(tableToKey(t.name)));
            if (selectedTable != null && selectedTable.equals(t.refName)) {
              int colIdx = findColumnIdxByName(tupleSchema.columns, selectedColumn);
              selected = t.row.get(colIdx);
              
            } else if (selectedTable == null) {
              int colIdx = findColumnIdxByName(tupleSchema.columns, selectedColumn);
              if (colIdx >= 0) selected = t.row.get(colIdx);
            }
          }
          // must be true
          if (selected != null) {
            b.append(String.format("%-20s|", (selected.type.equals("null")) ? "null" : selected.value));
          }
        }
        b.append("\n");
      };
    }

    /////

    for(int i = 0; i < selection.size(); i++) {
      b.append("+--------------------");
    }
    b.append("+\n");
    
    return b.toString();
  }
  
  /******************************/
  
  /***** DB access method *****/
  
  // push entry to database
  public void pushEntry(String akey, String adata) {
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
  public void popEntryAll(String akey) {
    DatabaseEntry key;
    try {
      key = new DatabaseEntry(akey.getBytes("UTF-8"));
      database.delete(null, key);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
  
  // delete entries with akey/adata from database
  public void popEntry(String akey, String adata) {
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
  public String getEntry(String akey) throws SimpleDBMSException{
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
  public ArrayList<String> getEntryAll(String akey) throws SimpleDBMSException {
    Cursor cursor = null;
    ArrayList<String> entries = new ArrayList<String>();
    try {
      cursor = database.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(akey.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      
      if (cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.NOTFOUND) {
        return entries;
      }
      
      do {
        entries.add(new String(data.getData(), "UTF-8"));
      } while(cursor.getNextDup(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);
      
      return entries;
      
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) cursor.close();
    }
    
    throw new SimpleDBMSException("Unhandled Error");
  }
  
  // get all entries with akey PREFIX from database
  public ArrayList<String> getEntryAllRange(String akey) throws SimpleDBMSException {
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
        String v = new String(data.getData(), "UTF-8");
        String k = new String(key.getData(), "UTF-8");
        if (!k.startsWith(akey)) break;
        entries.add(v);
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
  public boolean checkEntry(String akey) {
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
  public String tableToKey(String tableName) {
    return String.format("table-%s", tableName);
  }
  
  // convert column to database key
  public String columnToKey(String tableName, String columnName) {
    return String.format("column-%s-%s", tableName, columnName);
  }
  
  // convert foreign key (To) to database key
  public String reftoToKey(String tableName) {
    return String.format("reference-to-%s",  tableName);
  }
  
  // convert foreign key (From) to database key
  public String reffromToKey(String tableName) {
    return String.format("reference-from-%s", tableName);
  }
  
  // convert row to database key
  public String rowToKey(String tableName) {
    return String.format("row-%s", tableName);
  }
  
  
  //dump SimpleDBMSSchema object to string
  public String dumpTable(SimpleDBMSSchema schema) {
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
  public String dumpColumn(SimpleDBMSColumn col) {
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
  public String dumpReferenceRelation(String column, String otherTable, String otherColumn) {
    // format: <column> | <reference[d] table > | <reference[d] column>
    return String.format("%s|%s|%s", column, otherTable, otherColumn);
  }
  
  // dump one row to string
  public String dumpRow(ArrayList<QueryValue> values) {
    // format:
    // <column size> | <type1> | <value1> | ... | <typeN> | <valueN>
    StringBuilder b = new StringBuilder();
    
    b.append(values.size());
    for(QueryValue v: values) {
      b.append("|"); b.append(v.type);
      b.append("|"); b.append(v.value);
    }
    
    return b.toString();
  }
  
  // load string to SimpleDBMSSchema
  public SimpleDBMSSchema loadTable(String str) throws SimpleDBMSException {
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
  public SimpleDBMSColumn loadColumn(String str) {
    String[] s = str.split("\\|");
    SimpleDBMSColumn col = new SimpleDBMSColumn(s[0]);
    col.setType(s[1]);
    col.setLength(Integer.parseInt(s[2]));
    col.setNotNull(Boolean.parseBoolean(s[3]));
    col.setPrimaryKey(Boolean.parseBoolean(s[4]));
    col.setForeignKey(Boolean.parseBoolean(s[5]));
    return col;
  }
  
  public ReferenceRelation loadReference(String str) {
    String[] s = str.split("\\|");
    return new ReferenceRelation(s[0], s[1], s[2]);
  }
  
  public ArrayList<QueryValue> loadRow(String str) {
    String[] s = str.split("\\|");
    ArrayList<QueryValue> row = new ArrayList<QueryValue>();
    
    int columnSize = Integer.parseInt(s[0]);
    for(int i = 1; i < s.length; i += 2) {
      row.add(new QueryValue(s[i], s[i+1]));
    }

    return row;
  }
  
  // find SimpleDBMSColumn object by column name
  public SimpleDBMSColumn findColumnByName(ArrayList<SimpleDBMSColumn> columns, String name) {
    for(int i = 0; i < columns.size(); i++) {
      if (columns.get(i).name.equals(name)) return columns.get(i);
    }
    
    return null;
  }

  // find SimpleDBMSColumn object by column name
  public int findColumnIdxByName(ArrayList<SimpleDBMSColumn> columns, String name) {
    for(int i = 0; i < columns.size(); i++) {
      if (columns.get(i).name.equals(name)) return i;
    }
    
    return -1;
  }
  
  // check duplicate in string list
  public String checkDuplicateStr(ArrayList<String> arr) {
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
  public String checkDuplicateCol(ArrayList<SimpleDBMSColumn> arr) {
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
  
  // check duplicate column name in selection list
  public boolean checkDuplicateSelection(Selection s, ArrayList<Selection> l) {
    for (Selection _s: l) {
      if (_s.column.equals(s.column) && _s.table != null && !_s.table.equals(s.table)) return true;
    }
    return false;
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

class Pair<T, U> {
  public T first;
  public U second;
  
  Pair(T f, U s) {
    this.first = f;
    this.second = s;
  }
}