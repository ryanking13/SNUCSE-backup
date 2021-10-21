
public class SimpleDBMSColumn {
  
  public String name;   // name of column
  public String type;   // type of column
  public int length;    // length of column (used only if type is char)
  public boolean notNull = false;  // true if not null constraint
  public boolean primaryKey = false; // true if primary key
  public boolean foreignKey = false; // true if references other column
  
  SimpleDBMSColumn(String columnName) {
    setName(columnName);
    length = 0;
  }
  
  public void setName(String columnName) { name = columnName; }
  public void setType(String columnType) { type = columnType; }
  public void setLength(int columnLength) { length = columnLength; }
  public void setNotNull(boolean val) { notNull = val; }
  public void setPrimaryKey(boolean val) { primaryKey = val; }
  public void setForeignKey(boolean val) { foreignKey = val; }
}
