import java.util.*;

public class SimpleDBMSSchema {
  public String name = null; // table name
  public ArrayList<SimpleDBMSColumn> columns = null; // columns
  public ArrayList<String> primaryKeyColumns = null; // primary key names
  public ArrayList<String> foreignKeyColumns = null; // foreign key names
//  public ArrayList<ArrayList<String>> foreignKeyColumns = null; // foreign key names
  public ArrayList<Reference> referenceKeys = null; // table, column which foreign keys reference
//  public ArrayList<ArrayList<Reference>> referenceKeys = null; // table, column which foreign keys reference
  public List<Integer> foreignKeyPairSizes = null; // foreign key pair sizes
  public boolean duplicatePrimaryKeyDefinition = false;
  public boolean wrongLengthForeignKeyDefinition = false;
  
  SimpleDBMSSchema(String tableName) {
    setName(tableName);
    this.columns = new ArrayList<SimpleDBMSColumn>();
    this.primaryKeyColumns = new ArrayList<String>();
    this.foreignKeyColumns = new ArrayList<String>();
    this.referenceKeys = new ArrayList<Reference>();
    this.foreignKeyPairSizes = new ArrayList<Integer>();
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public void addColumn(SimpleDBMSColumn column) {
    this.columns.add(column);
  }
  
  public void addPrimaryKey(ArrayList<String> columns) {
    // if this function is called multiple times, set error trigger
    // this error will be handled by the manager
    if(this.primaryKeyColumns.size() > 0)
      duplicatePrimaryKeyDefinition = true;
    
    this.primaryKeyColumns = columns;
  }
  
  public void addForeignKey(ArrayList<String> columns,
      String referenceTable, ArrayList<String> referenceColumns) {
    // if column sizes are different, set error trigger
    // this error will be handled by the manager    
    if (columns.size() != referenceColumns.size()) {
      wrongLengthForeignKeyDefinition = true;
      return;
    }

    for(int i = 0; i < columns.size(); i++) {
      addForeignKeyEach(columns.get(i), referenceTable, referenceColumns.get(i));
    }
    
    addForeignKeyPairSize(columns.size());
  }
  
  public void addForeignKeyEach(String column,
      String referenceTable, String referenceColumn) {
    this.foreignKeyColumns.add(column);
    this.referenceKeys.add(new Reference(referenceTable, referenceColumn));
  }
  
  public void addForeignKeyPairSize(int size) {
    this.foreignKeyPairSizes.add(size);
  }
}

class Reference {
  public String table;
  public String column;
  
  Reference (String table, String column) {
    this.table = table;
    this.column = column;
  }
}

class ReferenceRelation {
  public String myColumn;
  public Reference ref;
  
  ReferenceRelation(String myColumn, String otherTable, String otherColumn) {
    this.myColumn = myColumn;
    this.ref = new Reference(otherTable, otherColumn);
  }
}