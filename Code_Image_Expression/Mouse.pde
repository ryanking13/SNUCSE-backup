boolean zoomIn = false;
boolean zoomOut = false;
boolean rightClicked = false;

void mousePressed() {
  time = millis();
  
  if(sceneNumber == END_SCENE){
    if(nextWord()){
      for (int j = 0; j < 2000; j++) {
          addDebris(new PVector(random(0, width), random(0, height)));
      }
    }
  }
  
  if (mouseButton == LEFT) {
    if(!checkStainTouch()) zoomIn = true;
  }
}

void mouseReleased() {
  time = millis();
  if (mouseButton == LEFT) {
    zoomIn = false;
  } else if (mouseButton == RIGHT) {
    zoomOut = false;
    rightClicked = false;
  }
}

boolean checkStainTouch() {
  int debris_size = (int)random(1000,1200);
  float mx = mouseX;
  float my = mouseY;

  for (int i = stains.size() -1; i >= 0; i--) {
    Stain s = stains.get(i);
    if (s.isInside(mx, my)) {
      bubble_sound.setGain(-10);
      touchCount++;
      if (touchCount % 1 == 0){
        objects.remove(0);
        if(objects.size() > 0) objects.remove(0);
      }
      for (int j = 0; j < debris_size; j++) {
        addDebris(new PVector(s.x + random(-s.radius, s.radius), s.y + random(-s.radius, s.radius)));
      }
      stains.remove(s);
      return true;
    }
  }
  
  return false;
}