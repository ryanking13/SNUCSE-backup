class WordDebris {
  PVector pos = new PVector(0, 0);
  PVector vel = new PVector(0, 0);
  PVector acc = new PVector(0, 0);
  PVector target = new PVector(0, 0);

  float closeEnoughTarget = 50;
  float maxSpeed = 4.0;
  float maxForce = 0.1;
  float debrisSize = 3.5;
  boolean isKilled = false;

  color startColor = color(0);
  color targetColor = color(0);
  float colorWeight = 0;
  float colorBlendRate = 0;

  void move() {
    // Check if particle is close enough to its target to slow down
    float proximityMult = 1.0;
    float distance = dist(this.pos.x, this.pos.y, this.target.x, this.target.y);
    if (distance < this.closeEnoughTarget) {
      proximityMult = distance/this.closeEnoughTarget;
      //proximityMult = 0;
    }

    // Add force towards target
    PVector towardsTarget = new PVector(this.target.x, this.target.y);
    towardsTarget.sub(this.pos);
    towardsTarget.normalize();
    towardsTarget.mult(this.maxSpeed*proximityMult);

    PVector steer = new PVector(towardsTarget.x, towardsTarget.y);
    steer.sub(this.vel);
    steer.normalize();
    steer.mult(this.maxForce);
    this.acc.add(steer);

    // Move particle
    this.vel.add(this.acc);
    this.pos.add(this.vel);
    this.acc.mult(0);
  }

  void draw() {
    // Draw particle
    color currentColor = lerpColor(this.startColor, this.targetColor, this.colorWeight);
    //color currentColor = targetColor;
    if (false) { // not used
      stroke(currentColor);
      point(this.pos.x, this.pos.y);
    } else {
      noStroke();
      fill(currentColor);
      ellipse(this.pos.x, this.pos.y, this.debrisSize, this.debrisSize);
    }

    // Blend towards its target color
    if (this.colorWeight < 1.0) {
      this.colorWeight = min(this.colorWeight+this.colorBlendRate, 1.0);
    }
  }

  void kill() {
    if (! this.isKilled) {
      // Set its target outside the scene
      PVector randomPos = generateRandomPos(width/2, height/2, (width+height)/2);
      this.target.x = randomPos.x;
      this.target.y = randomPos.y;

      // Begin blending its color to black
      this.startColor = lerpColor(this.startColor, this.targetColor, this.colorWeight);
      this.targetColor = color(0);
      this.colorWeight = 0;

      this.isKilled = true;
    }
  }
  
    void kill(int tp) {
    if (! this.isKilled) {
      // Set its target outside the scene
      PVector randomPos = generateRandomPos(width/2, height/2, (width+height)/2);
      this.target.x = randomPos.x;
      this.target.y = randomPos.y;

      // Begin blending its color to black
      this.startColor = lerpColor(this.startColor, this.targetColor, this.colorWeight);
      this.targetColor = color(255,50);
      this.colorWeight = 0;

      this.isKilled = true;
    }
  }
  
  boolean isOutOfBound(){
    if (!isKilled) return false;
    
    if(pos.x < 0 || pos.x > width || pos.y < 0 || pos.y > height) {
        return true;
    }
    
    else{
      return false;
    }
  }
}


PVector generateRandomPos(int x, int y, float mag) {
  PVector sourcePos = new PVector(x, y);
  PVector randomPos = new PVector(random(0, width), random(0, height));

  PVector direction = PVector.sub(randomPos, sourcePos);
  direction.normalize();
  direction.mult(mag);
  sourcePos.add(direction);

  return sourcePos;
}