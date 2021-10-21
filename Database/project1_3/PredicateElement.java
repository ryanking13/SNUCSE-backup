import java.util.*;

public class PredicateElement {
  public PredicateTypes type;
  public String nullTable = null;
  public String nullColumn = null;
  public boolean isNull;
  
  public OperandTypes operandLType;
  public String operandLValue = null;
  public String operandLTable = null;
  public String operandLColumn = null;
  public String op;
  public OperandTypes operandRType;
  public String operandRValue = null;
  public String operandRTable = null;
  public String operandRColumn = null;
  
  private OperandTypes typeToOperandType(String type) {
    if (type.equals("int")) return OperandTypes.INT;
    if (type.equals("char")) return OperandTypes.CHAR;
    if (type.equals("date")) return OperandTypes.DATE;
    else return OperandTypes.COLUMN;
  }
  
  public void setValue(QueryValue q, String pos) {
    if (pos == "left") {
      operandLType = typeToOperandType(q.type);
      operandLValue = q.value;
    } else if (pos == "right") {
      operandRType = typeToOperandType(q.type);
      operandRValue = q.value;
    }
  }
  
  public void setTable(String tableName, String pos) {
    if (pos == "left") {
      operandLType = typeToOperandType("column");
      operandLTable = tableName;
    } else if (pos == "right") {
      operandRType = typeToOperandType("column");
      operandRTable = tableName;
    }
  }
  
  public void setColumn(String colName, String pos) {
    if (pos == "left") {
      operandLType = typeToOperandType("column");
      operandLColumn = colName;
    } else if (pos == "right") {
      operandRType = typeToOperandType("column");
      operandRColumn = colName;
    }
  }
  
  public void setOp(String op) {
    this.type = PredicateTypes.COMPARABLE;
    this.op = op;
  }
  
  public void setNullTable(String name) {
    this.nullTable = name;
  }
  
  public void setNullColumn(String name) {
    this.nullColumn = name;
  }
  
  public void setNull(boolean b) {
    this.type = PredicateTypes.NULL;
    this.isNull = b;
  }
  
  public Ubool eval(ArrayList<OperandTuple> tupleSet, SimpleDBMSManager mgr) throws SimpleDBMSException {
    
    // null operation
    if (type == PredicateTypes.NULL) {
      if (nullTable == null) {
        // if table is not specified, check all table to find the column
        boolean columnFound = false;
        QueryValue col = null;
        
        for(OperandTuple tuple: tupleSet) {
          int colIdx = resolveColumnIdx(tuple, nullColumn, mgr);
         
          if (colIdx >= 0) {
            // same column name exists, 
            if (columnFound) throw new WhereAmbiguousReference();
            columnFound = true;
            
            if (tuple.row == null) col = null;
            else col = tuple.row.get(colIdx);
          }
        }
        
        // if column is not found, throw error
        if (!columnFound) throw new WhereColumnNotExist();
        
        if (col == null) return Ubool.FALSE;
        else if ((isNull && col.type.equals("null")) || (!isNull && !col.type.equals("null"))) return Ubool.TRUE;
        else return Ubool.FALSE;       
        
      } else {
        // if table is specified, find that table and check column
        for(OperandTuple tuple: tupleSet) {
          if (tuple.refName == nullTable) {
            int colIdx = resolveColumnIdx(tuple, nullColumn, mgr);
            
            if (colIdx < 0) throw new WhereColumnNotExist();
            
            QueryValue col;
            if (tuple.row == null) col = null;
            else col = tuple.row.get(colIdx);
            
            if (col == null) return Ubool.FALSE;
            else if ((isNull && col.type.equals("null")) || (!isNull && !col.type.equals("null"))) return Ubool.TRUE;
            else return Ubool.FALSE;
          }
        }
        
        // if not returned from loop above, table is not found
        throw new WhereTableNotSpecified();
      }
      
    // comparable
    } else {
      
      String ltype = null;
      String rtype = null;
      String lvalue = null;
      String rvalue = null;
      
      if (operandLType == OperandTypes.COLUMN) {
        if (operandLTable == null) {
          // if table is not specified, check all table to find the column
          boolean columnFound = false;
          QueryValue col = null;
          
          for(OperandTuple tuple: tupleSet) {
            int colIdx = resolveColumnIdx(tuple, operandLColumn, mgr);
            
            if (colIdx >= 0) {
              // same column name exists, 
              if (columnFound) throw new WhereAmbiguousReference();
              columnFound = true;
              
              if (tuple.row == null) col = null;
              else col = tuple.row.get(colIdx);
            }
          }
          
          // if column is not found, throw error
          if (!columnFound) throw new WhereColumnNotExist();
          
          if (col == null) return Ubool.FALSE;
          
          ltype = col.type;
          lvalue = col.value;
          
        } else {
          // if table is specified, find that table and check column
          for(OperandTuple tuple: tupleSet) {
            if (tuple.refName.equals(operandLTable)) {
              int colIdx = resolveColumnIdx(tuple, operandLColumn, mgr);
              QueryValue col = null;
              
              if (colIdx < 0) throw new WhereColumnNotExist();
              
              if (tuple.row == null) col = null;
              else col = tuple.row.get(colIdx);
              
              if (col == null) return Ubool.FALSE;
              
              ltype = col.type;
              lvalue = col.value;
            }
          }
          
          // if column not found from loop above, table is not found
          if (lvalue == null) throw new WhereTableNotSpecified();          
        }
      } else {
        ltype = typeToString(operandLType);
        lvalue = operandLValue.replaceAll("^\'|\'$", "");
      }
      
      if (operandRType == OperandTypes.COLUMN) {
        if (operandRTable == null) {
          // if table is not specified, check all table to find the column
          boolean columnFound = false;
          QueryValue col = null;
          
          for(OperandTuple tuple: tupleSet) {
            int colIdx = resolveColumnIdx(tuple, operandRColumn, mgr);
            
            if (colIdx >= 0) {
              // same column name exists, 
              if (columnFound) throw new WhereAmbiguousReference();
              columnFound = true;
              
              if (tuple.row == null) col = null;
              else col = tuple.row.get(colIdx);
            }
          }
          
          // if column is not found, throw error
          if (!columnFound) throw new WhereColumnNotExist();
          
          if (col == null) return Ubool.FALSE;
          
          ltype = col.type;
          lvalue = col.value;
          
        } else {
          // if table is specified, find that table and check column
          for(OperandTuple tuple: tupleSet) {
            if (tuple.refName.equals(operandRTable)) {
              int colIdx = resolveColumnIdx(tuple, operandRColumn, mgr);
              QueryValue col = null;
              
              if (colIdx < 0) throw new WhereColumnNotExist();
              
              if (tuple.row == null) col = null;
              else col = tuple.row.get(colIdx);
              
              if (col == null) return Ubool.FALSE;
              
              rtype = col.type;
              rvalue = col.value;
            }
          }
          
          // if column not found from loop above, table is not found
          if (rvalue == null) throw new WhereTableNotSpecified();          
        }        
      } else {
        rtype = typeToString(operandRType);
        rvalue = operandRValue.replaceAll("^\'|\'$", "");
      }
      
      // if any of two type is null, return unknown
      if (ltype.equals("null") || rtype.equals("null")) return Ubool.UNKNOWN;
      
      // otherwise, if two types are different throw error
      else if (!ltype.equals(rtype)) throw new WhereIncomparableError();
      
      // valid case, compare
      else {
        boolean result = false;
        if (ltype.equals("int")) {
          int lv = Integer.parseInt(lvalue);
          int rv = Integer.parseInt(rvalue);

          if (op.equals(">")) result = lv > rv;
          else if (op.equals("<")) result = lv < rv;
          else if (op.equals("=")) result = lv == rv;
          else if (op.equals(">=")) result = lv >= rv;
          else if (op.equals("<=")) result = lv <= rv;
          else if (op.equals("!=")) result = lv != rv;
        }
        
        else if (ltype.equals("char") || ltype.equals("date")) {
          int cmp = lvalue.compareTo(rvalue);
          if (op.equals(">")) result = cmp > 0;
          else if (op.equals("<")) result = cmp < 0;
          else if (op.equals("=")) result = cmp == 0;
          else if (op.equals(">=")) result = cmp >= 0;
          else if (op.equals("<=")) result = cmp <= 0;
          else if (op.equals("!=")) result = cmp != 0;
          
        }
        
        if (result) return Ubool.TRUE;
        else return Ubool.FALSE;
      }
    }
  }
  
  private int resolveColumnIdx(OperandTuple tuple, String columnName, SimpleDBMSManager mgr) throws SimpleDBMSException {
    SimpleDBMSSchema schema = mgr.loadTable(mgr.getEntry(mgr.tableToKey(tuple.name)));
    return mgr.findColumnIdxByName(schema.columns, columnName);
  }
  
  private String typeToString(OperandTypes type) throws SimpleDBMSException {
    if (type == OperandTypes.INT) return "int";
    else if (type == OperandTypes.CHAR) return "char";
    else if (type == OperandTypes.DATE) return "date";
    else throw new SimpleDBMSException("invalid type case");
  }
}

enum PredicateTypes {
  COMPARABLE, NULL
}

enum OperandTypes {
  COLUMN, INT, CHAR, DATE
}

enum Ubool {
  TRUE, FALSE, UNKNOWN;
}

class OperandTuple {
  String name;
  String refName; // table name or table name alias
  ArrayList<QueryValue> row;
  
  OperandTuple(String name, String ref, ArrayList<QueryValue> row) {
    this.name = name;
    this.refName = ref;
    this.row = row;
  }
}
