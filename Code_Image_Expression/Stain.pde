int num_of_stains = 8;
String stains_addr = "\\stains\\stain_";

class Stain{
  float x;
  float y;
  float radius;
  color col;
  PShape shape;
  
  public Stain(float x_, float y_, float r_, color col_){
    x = x_;
    y = y_;
    radius = r_;
    col = col_;
    shape = loadShape(stains_addr + (int)random(1,num_of_stains+1) + ".svg");
    shape.disableStyle();
  }
  
  public void generate(){
    fill(col);
    
    shape(shape, x,y, radius*2, radius*2);
    //ellipse(x,y,radius,radius);
  }
  
  public boolean isInside(float mx, float my){
    if(dist(x,y,mx,my) <= radius*1.5/2) return true;
    
    return false;
  } 
}