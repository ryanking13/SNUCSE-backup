
public class SimpleDBMSDeleteQuery {
  public String tableName;
  public PredicateTree condition = null;
  SimpleDBMSDeleteQuery(String name) {
    this.tableName = name;
  }
  
  public void addCondition(PredicateTree t) {
    condition = t;
  }
}
