import controlP5.*;
import java.util.*;

ScrollableList setMovieList(ControlP5 cp5, ArrayList<String> movieList, int posY) {
  
  int sizeX = 300;
  int sizeY = 50;
  //int posX = width/2 - sizeX/2;
  int posX = width/2 - sizeX;
  
  ScrollableList sl = cp5.addScrollableList("movieListDropDown")
                        .setPosition(posX, posY)
                        .setSize(sizeX, height)
                        .setColor(colorset)
                        .setBarHeight(sizeY)
                        .setItemHeight(sizeY)
                        .addItems(movieList)
                        .close();            
  
  sl.setCaptionLabel("Select Movie");
  sl.getCaptionLabel().setFont(UIfont);
  sl.getValueLabel().setFont(UIfont);
  return sl;
}

/* when select movie on dropdown */
void movieListDropDown(int n) {
  
  Map m = cp5.get(ScrollableList.class, "movieListDropDown").getItem(n);
  choice.movie = (String)m.get("name");
  
  if (cp5.getGroup("timeline") != null) {
    cp5.getGroup("timeline").remove();
  }
  
  eBox.textarea.setText("");
  eBox.title.setText("");
  eBox.canvas.updateImage(null);
  eBox.canvas.updateIcons(null);
}
