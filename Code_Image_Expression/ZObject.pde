class ZObject {
  float x, y, z, xsize, ysize;
  color bubble_color;
  color shaded_color;
  float vx, vy, vz;
 
  ZObject(float ix, float iy, float iz, color icolor) {
    x = ix;
    y = iy;
    z = iz;
    xsize = RADIUS;
    ysize = RADIUS;
    bubble_color = icolor;
    setColor();
    vx = random(-1.0, 1.0);
    vy = random(-1.0, 1.0);
    vz = random(-1.0, 1.0);
    float magnitude = sqrt(vx*vx + vy*vy + vz*vz);
    vx = SPEED * vx / magnitude;
    vy = SPEED * vy / magnitude;
    vz = SPEED * vz / magnitude;
  }
  
  void setColor() {
    float shade = z;
    float shadeinv = 1.0-shade;
    shaded_color = color( (red(bubble_color)*shade)+(red(BACKGROUND)*shadeinv),
                    (green(bubble_color)*shade)+(green(BACKGROUND)*shadeinv),
                    (blue(bubble_color)*shade)+(blue(BACKGROUND)*shadeinv));
  }
  
  void zoomIn(float step) {
    z += step;
    if (z > 1.0) {
      z = 0.0 + (z-1.0);
    }
  }
  
  void zoomOut(float step) {
    z -= step;
    if (z < 0.0) {
      z = 1.0 - (0.0-z);
    }
  }
 
  void update(boolean doZoomIn, boolean doZoomOut) {
    if (doZoomIn) {
      zoomIn(ZSTEP);
    }
    if (doZoomOut) {
      zoomOut(ZSTEP);
    }
    if (x <= 0) {
        vx = abs(vx);
        x = 0.0f;
    }
    if (x >= 1.0) {
        vx = -1.0 * abs(vx);
        x = 1.0;
    }
    if (y <= 0) {
        vy = abs(vy);
        y = 0.0f;
    }
    if (y >= 1.0) {
        vy = -1.0 * abs(vy);
        y = 1.0;
    }
    if (z < 0 || z > 1.0) {
        z = z % 1.0;
    }
    // float n = (noise(x, y) - 0.5) * 0.00001;
    // vx += n;
    // vy += n;
    x += vx;
    y += vy;
    //z += vz;
    setColor();
  }
 
  void draw(float xoffs, float yoffs) {
    float posX = (ZOOM*x*width*(1+z*z)) - ZOOM*xoffs*width*z*z;
    float posY = (ZOOM*y*height*(1+z*z)) - ZOOM*yoffs*height*z*z;
    float radius = z*xsize;
    if (posX> -xsize*2 && posX < width+xsize*2 && posY > -xsize*2 && posY < height+xsize*2) {
        blurred_circle(posX, posY, radius, abs(z-FOCAL_LENGTH), shaded_color, MIN_BLUR_LEVELS + (z*BLUR_LEVEL_COUNT));
    }
    
    if(posX < width && posY < height && posX > 0 && posY > 0 && z > 1.0-ZSTEP){
      //println(distance);
      //bubble_pop_sound.loop(0);
      pg.beginDraw();
      pg.fill(bubble_color, 100);
      
      pg.ellipse(posX, posY, radius, radius);
      pg.endDraw();
      pg.loadPixels();
      z = (z+ZSTEP)%1.0;
      
      stains.add(new Stain(posX, posY, radius*1.3, color(bubble_color, int(random(100,180)))));
      
    }
  }
}

// This function will draw a blurred circle, according to the "blur" parameter. Need to find a good radial gradient algorithm.
void blurred_circle(float x, float y, float rad, float blur, color col, float levels) {
    float level_distance = BLUR_AMOUNT*(blur)/levels;
    for (float i=0.0; i<levels*2; i++) {
      fill(col, 255*(levels*2-i)/(levels*2));
      ellipse(x, y, rad+(i-levels)*level_distance, rad+(i-levels)*level_distance);
    }
}

void sortBubbles() {
   
    // Sort them (this ensures that they are drawn in the right order)
    float last = 0;
    ArrayList temp = new ArrayList();
    for (int i=0; i<objects.size(); i++) {
        int index = 0;
        float lowest = 100.0;
        for (int j=0; j<objects.size(); j++) {
            ZObject current = (ZObject)objects.get(j);
            if (current.z < lowest && current.z > last) {
                index = j;
                lowest = current.z;
            }
        }
        temp.add(objects.get(index));
        last = ((ZObject)objects.get(index)).z;
    }
    objects = temp;
}