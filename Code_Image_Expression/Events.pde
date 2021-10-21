void showExplanationText(){
  if (distance == 0) {
    fill(255, 100);
    text("Hold left button to go forward", width/2, height/2);
  }

  if (distance > 200 && touchCount == 0) {
    fill(255, 100);
    text("Click splatters to remove it", width/2, height/2);
  }
  
  if(!zoomIn){
    
    if(millis() - time > 30000){
      clearAll();
      setup();
      return;
    }
    if(millis() - time > 8000 && stains.size() < 10 && touchCount > 0){
      fill(255,100);
      text("Hold left button to go forward", width/2, height/2);
    }
    else if(millis() - time > 8000 && stains.size() >= 10){
      fill(255,100);
      text("Click stains to remove it", width/2, height/2);
    }
  }
}

void endAll(){
  clearAll();
  //clearScreen();
  
  voiceQueue.add("이제 아무것도 남지 않았어");
  voiceQueue.add("아무것도");
  nextWord();
  for (int j = 0; j < 4000; j++) {
        addDebris(new PVector(random(0, width), random(0, height)));
  }
}

void clearAll(){
  objects.clear();
  wordDebrises.clear();
  voiceQueue.clear();
  
  for(int i = 0; i < stains.size(); i++){
    Stain s = stains.get(i);
    for(int j = 0; j < 600; j++){
      addDebris(new PVector(s.x + random(-s.radius, s.radius), s.y + random(-s.radius, s.radius)), 1);
    }
  }
  for(int i = 0; i < wordDebrises.size(); i++){
    wordDebrises.get(i).kill(1);
  }
  stains.clear();
}

void clearScreen(){
  int delay_count = 20;
  for(int i = 0; i < delay_count; i++){
    delay(100);
    background(BACKGROUND,10);
  }
}