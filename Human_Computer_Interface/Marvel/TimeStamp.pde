import java.util.*;

class Timestamp {
  String movieName;
  String start;
  String end;
  
  public Timestamp(String name, String start, String end) {
    this.movieName = name;
    this.start = start;
    this.end = end;
  }
}

ArrayList<Timestamp> getTimestamps() {
  ArrayList<Timestamp> ts = new ArrayList<Timestamp>();
  
  ts.add(new Timestamp("Avengers 1", "0000", "2018"));
  ts.add(new Timestamp("Ironman 2", "0000", "2007"));
  return ts;
}
