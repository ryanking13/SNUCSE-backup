import controlP5.*;
import java.util.*;
import java.time.*;

ControlP5 cp5;
ScrollableList movieSelectList;
ButtonBar typeButtons;
Button searchButton;
//ButtonBar movieTimeline;
ArrayList<Button> timeline;
ArrayList<Timestamp> timestamps;
Choice choice;
ExplanationBox eBox;
Textlabel iconDescription;
Textarea iconDescriptionBackground;

ArrayList<String> targetMovieList;
ArrayList<String> typeList;
ArrayList<Explanation> explanations;

PFont UIfont;
PFont Explanationfont;
PFont ExplanationSmallfont;
PFont ExplanationTitlefont;

CColor colorset;

int MAX_ICONS = 5;

boolean showDisabled = false;
long showDisabledTime;

void setup() {
  size(1280, 1000);
  noStroke();
  
  colorset = new CColor();
  colorset.setBackground(color(10, 85, 92));
  colorset.setForeground(color(65, 132, 143));
  colorset.setActive(color(151, 192, 183));
  
  UIfont = loadFont("data/NanumGothicExtraBold-18.vlw");
  Explanationfont = loadFont("data/OTMGothicM-16.vlw");
  ExplanationSmallfont = loadFont("data/OTMGothicM-14.vlw");
  ExplanationTitlefont = loadFont("data/OTMGothicBK-24.vlw");
  cp5 = new ControlP5(this);
  choice = new Choice();
  
  targetMovieList = new ArrayList<String>();
  targetMovieList.add("Avengers 1");
  targetMovieList.add("Ironman 2");

  typeList = new ArrayList<String>();
  typeList.add("Event");
  typeList.add("Character");
  
  explanations = getExplanations();
  timestamps = getTimestamps();
  
  movieSelectList = setMovieList(cp5, targetMovieList, 50);
  //setTypeCheckBox(cp5, typeList);
  typeButtons = setTypeButtons(cp5, typeList, 120);
  searchButton = setSearchButton(cp5, 50);
  //movieTimeline = setTimelineButtons(cp5, previousMovieList, 200);
  //movieTimeline = setTimelineCheckBox(cp5, previousMovieList, 200);
  eBox = setExplanationBox(cp5, 400);
  
  movieSelectList.bringToFront();
  
  //cp5.addTextarea("timelineBackground")
  //   .setSize(width-100, 150)
  //   .setPosition(50, 200)
  //   .setColorBackground(color(238, 233, 209, 100));
}

void draw() {
  background(0x95a5a6);
  fill(color(238, 233, 209, 50));
  rect(50, 200, width-100, 150);
  
  if (showDisabled) {
    if(System.currentTimeMillis() - showDisabledTime > 1500) {
      showDisabled = false;
    }
    
    fill(color(0, 0, 0));
    rect(width/2 - 200, height/2 - 100, 400, 200);
    textSize(32);
    fill(color(255, 255, 255));
    text("Not Implemented", width/2 - 120, height/2);
  }
}


String formatLabelText(String text, int maxLineLength) {
  String label = "";
  
  for(int i = 0; i < text.length(); i += maxLineLength){
    label += text.substring(i, min(i+maxLineLength, text.length())) + "\n";
  }
  
  return label;
}


/* handlers */
void generateTimeline() {
  
  if (cp5.getGroup("timeline") != null) {
    cp5.getGroup("timeline").remove();
  }

  cp5.addGroup("timeline")
     .setPosition(50, 200)
     .hideBar();
  
  timeline = new ArrayList<Button>();
  for(Explanation e: explanations) {
    if (e.targetMovieName == choice.movie && e.type == choice.type) {
      Button b = cp5.addButton(e.name)
                    .setPosition(e.timelinePosX, 0)
                    .setSize(e.timelineLength, 150)
                    .setFont(ExplanationSmallfont)
                    .setColor(e.timelineColor)
                    .setColorBackground(e.timelineColor.getBackground())
                    .setGroup("timeline")
                    .addCallback(new CallbackListener(){
                      public void controlEvent(CallbackEvent ev) {
                        if(ev.getAction() == 6){
                          onTimelineClick(ev.getController().getName());
                        }
                      }
                    });
                    
      int charPerLine = 10;
      b.getCaptionLabel()
       .setText(formatLabelText(e.name, charPerLine))
       .getStyle()
       .setMarginTop(-7*(e.name.length()/charPerLine));
       
      timeline.add(b);
      
      Textlabel lb = cp5.addTextlabel(e.name + str(e.year))
                        .setText(str(e.year))
                        .setPosition(e.timelinePosX + ((e.timelineLength - 50)/2), 160)
                        .setFont(Explanationfont)
                        .setColor(color(0,0,0))
                        .setGroup("timeline");
    }
  }
  
  cp5.addTextlabel("사건년도")
     .setText("사건년도")
     .setPosition(-50, 160)
     .setFont(ExplanationSmallfont)
     .setColor(color(0,0,0))
     .setGroup("timeline");
  
  //for(Timestamp ts: timestamps) {
  //  if (choice.movie == ts.movieName) {
  //    cp5.addTextlabel("start")
  //       .setText(ts.start)
  //       .setPosition(0, 155)
  //       .setSize(50, 50)
  //       .setColor(color(0,0,0,128))
  //       .setFont(UIfont)
  //       .setGroup("timeline");
         
  //    cp5.addTextlabel("end")
  //       .setText(ts.end)
  //       .setPosition(width-150, 155)
  //       .setSize(50, 50)
  //       .setColor(color(0,0,0,128))
  //       .setFont(UIfont)
  //       .setGroup("timeline");
  //  }
  //}
}

void onTimelineClick(String timelineName) {
  
  if (timeline == null) return;
  
  for(int i = 0; i < timeline.size(); i++) {
    Button b = timeline.get(i);
    String name = (String)b.getName();
    
    if (name != timelineName) {
      b.setValue(0);
    }
  }
    
  for(int j = 0; j < explanations.size(); j++) {
    Explanation expl = explanations.get(j);
    if(expl.targetMovieName == choice.movie && expl.name == timelineName && expl.type == choice.type) {
      eBox.textarea.setText(expl.explanation);
      eBox.title.setText(expl.name);
      eBox.canvas.updateImage(expl.image);
      eBox.canvas.updateIcons(expl.icons);
    }
  }
}
