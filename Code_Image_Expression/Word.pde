ArrayList<Integer> coordsIndexes = new ArrayList<Integer>();
ArrayList<WordDebris> wordDebrises = new ArrayList<WordDebris>();
int voiceCount = 0;
String fontName = "양재벨라체M";
int pixelSteps = 4;

boolean nextWord() {
  // Draw word in memory
  if(voiceQueue.isEmpty()){
    delay(2000);
    doInitialSetting();
    return false;
  }
  PGraphics pg = createGraphics(width, height);
  pg.beginDraw();
  pg.fill(0);
  pg.textSize(80);
  pg.textAlign(CENTER);
  PFont font = createFont(fontName, 120);
  pg.textFont(font);
  pg.text(voiceQueue.poll(), width/2, height/2);
  pg.endDraw();
  pg.loadPixels();
  voiceCount++;
  
  for(int i = 0; i < wordDebrises.size(); i++){
    wordDebrises.get(i).kill();
  }
  
  coordsIndexes = new ArrayList<Integer>();
  for (int i = 0; i < (width*height)-1; i+= pixelSteps) {
    if(pg.pixels[i] != 0) coordsIndexes.add(i);
  }
  
  return true;
}

float voiceDelayTime = 0;
void addDebris(PVector pos){
  
  if(coordsIndexes.size() == 0) return;
  
  else if(coordsIndexes.size() < 5){ 
    if(millis() - voiceDelayTime > 2500) nextWord();
    return;
  }
  else if(coordsIndexes.size() < 20){
    voiceDelayTime = millis();
  }
  
  color newColor = color(240,240,240, random(50,100));
  
  int randomIndex = (int)random(0, coordsIndexes.size());
  int coordIndex = coordsIndexes.get(randomIndex);
  coordsIndexes.remove(randomIndex);
  
  int x = coordIndex % width;
  int y = coordIndex / width;

  WordDebris newDebris = new WordDebris();
 
  newDebris.pos.x = pos.x;
  newDebris.pos.y = pos.y;
  newDebris.maxSpeed = random(15.0, 20.0);
  newDebris.maxForce = newDebris.maxSpeed*0.07;
  newDebris.debrisSize = 2;
  newDebris.colorBlendRate = random(0.04, 0.05);
        
        
  wordDebrises.add(newDebris);
      
  // Blend it from its current color
  newDebris.startColor = lerpColor(newDebris.startColor, newDebris.targetColor, newDebris.colorWeight);
  newDebris.targetColor = newColor;
  newDebris.colorWeight = 0;
  
  // Assign the particle's new target to seek
  newDebris.target.x = x;
  newDebris.target.y = y;
}

void addDebris(PVector pos, int tp){
  
  color newColor = color(240,240,240, random(50,100));

  WordDebris newDebris = new WordDebris();
 
  newDebris.pos.x = pos.x;
  newDebris.pos.y = pos.y;
  newDebris.maxSpeed = random(15.0, 20.0);
  newDebris.maxForce = newDebris.maxSpeed*0.07;
  newDebris.debrisSize = 2;
  newDebris.colorBlendRate = random(0.04, 0.05);
        
        
  wordDebrises.add(newDebris);
      
  // Blend it from its current color
  newDebris.startColor = lerpColor(newDebris.startColor, newDebris.targetColor, newDebris.colorWeight);
  newDebris.targetColor = newColor;
  newDebris.colorWeight = 0;
  
  //newDebris.kill();
}