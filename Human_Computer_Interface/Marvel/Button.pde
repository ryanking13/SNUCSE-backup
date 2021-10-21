import controlP5.*;
import java.util.*;

Button setSearchButton(ControlP5 cp5, int posY) {

  int sizeX = 200;
  int sizeY = 120;
  //int posX = width / 2 - sizeX / 2;
  int posX = width/2 + 100;
  
  Button b = cp5.addButton("search")
                .setPosition(posX, posY)
                .setSize(sizeX, sizeY)
                .setColor(colorset)
                .setFont(UIfont);
  
  return b;
}

public void search(int value) {
  generateTimeline();
}


ButtonBar setTypeButtons(ControlP5 cp5, ArrayList<String> typeList, int posY) {
  
  int typesCount = typeList.size();
  int sizeX = 120 * typesCount;
  int sizeY = 50;
  int posX = width/2 - sizeX / 2 - 120;
                 
  ButtonBar bb = cp5.addButtonBar("typeButtons")
                    .setPosition(posX, posY)
                    .setSize(sizeX, sizeY)
                    .setColor(colorset);
                    
  for(int i = 0; i < typesCount; i++) {
    bb.addItem(typeList.get(i), i);
  }
  
  bb.changeItem("Event","selected", true);
  choice.type = "Event";
  
  bb.getValueLabel().setFont(UIfont);
  bb.setCaptionLabel("asdf");
  
  return bb;
}

void typeButtons(int n) {
  Map m = (Map)cp5.get(ButtonBar.class, "typeButtons").getItems().get(n);
  choice.type = (String)m.get("name");
  
  // Character is not implemented
  if (choice.type == "Character") {
    showDisabled = true;
    showDisabledTime = System.currentTimeMillis();
    choice.type = "Event";
    typeButtons.changeItem("Event", "selected", true);
    typeButtons.changeItem("Character", "selected", false);
  }
}

// Not Using
ButtonBar setTimelineButtons(ControlP5 cp5, ArrayList<String> movieList, int posY) {

  int moviesCount = movieList.size();
  int posX = 50;
  int sizeY = 150;
  int sizeX = width - posX * 2;
  
  ButtonBar bb = cp5.addButtonBar("timelineButtons")
                   .setPosition(posX, posY)
                   .setSize(sizeX, sizeY);
  
  for(int i = 0; i < moviesCount; i++) {
    bb.addItem(movieList.get(i), i);
  }
                   
  return bb;
}
