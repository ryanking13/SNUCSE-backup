import java.util.ArrayList;

public class PredicateTree {
  public PredicateTree left = null;
  public PredicateTree right = null;
  public PredicateJoin join = PredicateJoin.AND;
  public PredicateElement e = null;
  public boolean not = false;
  
  public void setLeft(PredicateTree t) {
    this.left = t;
  }
  
  public void setRight(PredicateTree t) {
    this.right = t;
  }
  
  public void setJoin(PredicateJoin j) {
    this.join = j;
  }
  
  public void setNot(boolean not) {
    this.not = not;
  }
  
  public void setValue(PredicateElement e) {
    this.e = e;
  }
  
  public boolean eval(ArrayList<OperandTuple> tupleSet, SimpleDBMSManager mgr) throws SimpleDBMSException {
    Ubool result = _eval(tupleSet, mgr);
    if (result == Ubool.TRUE) return true;
    else return false;
  }
  
  public Ubool _eval(ArrayList<OperandTuple> tupleSet, SimpleDBMSManager mgr) throws SimpleDBMSException{
    Ubool result = Ubool.TRUE;
    
    if (e != null) result = e.eval(tupleSet, mgr);
    else if (join == PredicateJoin.AND) {
      Ubool lres = (left == null) ? Ubool.TRUE : left._eval(tupleSet, mgr);
      Ubool rres = (right == null) ? Ubool.TRUE : right._eval(tupleSet, mgr);
      if (lres == Ubool.FALSE || rres == Ubool.FALSE) result = Ubool.FALSE;
      else if (lres == Ubool.TRUE && rres == Ubool.TRUE) result = Ubool.TRUE;
      else result = Ubool.UNKNOWN;
    } else if (join == PredicateJoin.OR) {
      Ubool lres = (left == null) ? Ubool.TRUE : left._eval(tupleSet, mgr);
      Ubool rres = (right == null) ? Ubool.TRUE : right._eval(tupleSet, mgr);
      
      if (lres == Ubool.TRUE || rres == Ubool.TRUE) result = Ubool.TRUE;
      else if (lres == Ubool.FALSE && rres == Ubool.FALSE) result = Ubool.FALSE;
      else result = Ubool.UNKNOWN;
    } 
    
    if (not) {
      if (result == Ubool.TRUE) result = Ubool.FALSE;
      else if (result == Ubool.FALSE) result = Ubool.TRUE;
      else result = Ubool.UNKNOWN;
    }
    
    return result;
  }
}

enum PredicateJoin {
  AND, OR,
}
