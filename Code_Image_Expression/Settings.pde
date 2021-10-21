void doInitialSetting() {
  sceneNumber = MAIN_SCENE;
  RADIUS = height/18;
  smooth();
  noStroke();
  setPG();
  xoffs = 0;
  yoffs = 0;
  textSize(40);
  ZSTEP = 0.010;
  textAlign(CENTER);
  rectMode(CENTER);
  shapeMode(CENTER);
  objects = new ArrayList();
  stains = new ArrayList();

  bgm.loop();
  bubble_sound.loop();
  bubble_sound.setGain(-80);
  //bubble_pop_sound = minim.loadFile("splatter.mp3", 2048);
  //bubble_pop_sound.setGain(-20);
  //bubble_pop_sound = minim.loadFile("pop.wav", 2048);
  //bubble_pop_sound.sampleRate();

  touchCount = 0;
  distance = 0;
  time = millis();

  // Randomly generate the bubbles
  for (int i=0; i<N; i++) {
    //objects.add(new ZObject(random(1.0f), random(1.0f), random(1.0f), color(random(150.0, 190.0), random(150.0, 190.0), random(20.0, 20.0))));
    objects.add(new ZObject(random(1.0f), random(1.0f), random(1.0f), color(random(240.0, 256.0), random(240.0, 256.0), random(240.0, 256.0))));
  }

  addVoices();
  sortBubbles();
  nextWord();
}

void setPG() {
  pg = createGraphics(width, height);
  pg.beginDraw();
  pg.noStroke();
  pg.smooth();
  pg.fill(255);
  pg.endDraw();
  pg.loadPixels();
}