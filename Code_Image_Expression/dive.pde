import ddf.minim.*;

float ZOOM = 2.0;
int N = 40*(int)ZOOM;
float RADIUS;
float SPEED = 0.0003;
float FOCAL_LENGTH = 0.5;
float BLUR_AMOUNT = 100;
int MIN_BLUR_LEVELS = 2;
int BLUR_LEVEL_COUNT = 5;
float ZSTEP = 0.010;
color BACKGROUND = color(0, 30, 30);

float xoffs = 0;
float yoffs = 0;
float distance = 0;
float time = 0;

ArrayList objects;

PGraphics pg; 
ArrayList<Stain> stains = new ArrayList<Stain>();
int touchCount = 0;

Minim minim;
AudioPlayer bgm;
AudioPlayer bubble_sound;
AudioPlayer bubble_pop_sound;

final float bubble_sound_amp_init = 0.2;
float bubble_sound_amp;

int sceneNumber;
final int MAIN_SCENE = 1;
final int END_INTER_SCENE = 2;
final int END_SCENE = 3;

PImage bg;

void setup() {
  fullScreen();
  minim = new Minim(this);
  bgm = minim.loadFile("dive.mp3", 2048);
  bubble_sound = minim.loadFile("four_whisper_2.mp3", 2048);
  //size(1000,1000);
  doInitialSetting();
  //bg = loadImage("back.png");
}

void draw() {

  ZSTEP = 0.010 - min(0.009, (stains.size() / 5)*0.001);
  if (sceneNumber == MAIN_SCENE) {    

    xoffs = xoffs*0.8 + 0.2*mouseX/width;
    yoffs = yoffs*0.8 + 0.2*mouseY/height;

    if (objects.size() < 5) {
      //bgm.pause();
      bubble_sound.pause();
      sceneNumber = END_INTER_SCENE;
      time = millis();
      clearAll();
      return;
    } else {
      background(BACKGROUND);
      showExplanationText();
      updateBubbles();
      updateDistance();
      updateWord();
    }
  } else if (sceneNumber == END_INTER_SCENE) {
    if (millis() - time > 2500) {
      endAll();
      sceneNumber = END_SCENE;
      frameRate(60);
      return;
    }

    frameRate(60);
    fill(BACKGROUND, 100);
    rect(width/2, height/2, width, height);
    updateWord();
    
  } else if (sceneNumber == END_SCENE) {
    background(BACKGROUND);
    updateWord();
  }

  updateSound();
  //image(bg, 0, 0);
}

void updateBubbles() {

  for (int i=0; i<objects.size(); i++) {
    ZObject current = (ZObject)objects.get(i);
    current.update(zoomIn, zoomOut);
  }
  sortBubbles();

  for (int i=0; i<objects.size(); i++) {
    ((ZObject)objects.get(i)).draw(xoffs, yoffs);
  }
}

void updateDistance() {
  if (zoomIn) distance += 1;
}

void updateWord() {

  for (Stain s : stains) {
    s.generate();
  }
  for (int x = wordDebrises.size ()-1; x > -1; x--) {
    // Simulate and draw pixels
    WordDebris debris = wordDebrises.get(x);
    debris.move();
    debris.draw();

    // Remove any dead pixels out of bounds
    if (debris.isKilled) {
      if (debris.pos.x < 0 || debris.pos.x > width || debris.pos.y < 0 || debris.pos.y > height) {
        wordDebrises.remove(debris);
      }
    }
  }
}

void updateSound() { 
  bubble_sound.setGain(max(-80, bubble_sound.getGain() - 0.1));
}