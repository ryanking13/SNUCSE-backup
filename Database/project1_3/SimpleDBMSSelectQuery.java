import java.util.*;

public class SimpleDBMSSelectQuery {
  
  public boolean asterisk = false;
  public PredicateTree condition = null;
  
  public ArrayList<Selection> selection = new ArrayList<Selection>();
  public ArrayList<From> from = new ArrayList<From>();
  
  SimpleDBMSSelectQuery() {}
  
  public void setAsterisk(boolean v) {
    asterisk = v;
  }
  
  public void setSelection(String table, String column, String columnAlias) {
    selection.add(new Selection(table, column, columnAlias));
  }
  
  public void setFrom(String table, String tableAlias) {
    from.add(new From(table, tableAlias));
  }
  
  public void setWhere(PredicateTree condition) {
    this.condition = condition;
  }
}

class Selection {
  public String table;
  public String column;
  public String alias;
  
  Selection(String table, String column, String alias) {
    this.table = table;
    this.column = column;
    this.alias = alias;
  }
}

class From {
  public String table;
  public String alias;
  
  From(String table, String alias) {
    this.table = table;
    this.alias = alias;
  }
}