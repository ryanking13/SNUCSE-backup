import java.util.*;

public class SimpleDBMSInsertQuery {
  public String tableName;
  public ArrayList<String> columns;
  public ArrayList<QueryValue> values;
  
  SimpleDBMSInsertQuery(String name) {
    this.tableName = name;
  }
  
  public void setColumns(ArrayList<String> columns) {
    this.columns = columns;
  }
  
  public void setValues(ArrayList<QueryValue> values) {
    this.values = values;
  }
}

class QueryValue {
  public String type;
  public String value;
  
  QueryValue(String type, String value) {
    this.type = type;
    this.value = value;
  }
}
