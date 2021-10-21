import controlP5.*;

class ExplanationBox {
  public Textarea textarea;
  public ExplanationCanvas canvas;
  public Textlabel title;
}

class ExplanationCanvas extends Canvas {
  
  public int posY;
  public int posX;
  public int sizeX;
  public int sizeY;
  public int imageSizeX;
  public int imageSizeY;
  public int iconSize;
  public PImage img;
  public ArrayList<ExplanationIcon> icons;
  
  public int padX;
  public int padY;
  
  public ExplanationCanvas(int posX, int posY, int padX, int padY, int sizeY, int imageSizeX, int imageSizeY, int iconSize) {
    this.posY = posY;
    this.posX = posX;
    this.sizeX = width - 2*posX;
    this.sizeY = sizeY;
    this.imageSizeX = imageSizeX;
    this.imageSizeY = imageSizeY;
    this.iconSize = iconSize;
    this.padX = padX;
    this.padY = padY;
    
    cp5.addButton("textareaBackground")
       .setLabelVisible(false)
       .setSize(sizeX, sizeY)
       .setPosition(posX, posY)
       .setColorBackground(color(238, 233, 209, 100))
       .lock();
       
    cp5.addTextlabel("characterLabel")
       .setText("관련 등장인물")
       .setPosition(posX + padX * 2, posY + padY*2)
       .setFont(ExplanationSmallfont)
       .setColor(color(0,0,0))
       .hide();
  }
  
  public void updateIcons(ArrayList<ExplanationIcon> icons) {
    this.icons = icons;
    
    if (icons != null){
      for(int i = 0; i < icons.size(); i++) {
        ExplanationIcon icon = icons.get(i);
        icon.icon.resize(iconSize, iconSize);
        PGraphics mask = createGraphics(icon.icon.width, icon.icon.height);
        mask.beginDraw();
        mask.smooth();
        mask.background(0);
        mask.fill(255);
        mask.ellipse(icon.icon.width/2, icon.icon.height/2, icon.icon.width, icon.icon.height);
        mask.endDraw();
        icon.icon.mask(mask);
      }
      
      for(int i = 0; i < this.icons.size(); i++) {
        cp5.get(Button.class, "explanationIcon" + str(i))
           .setImage(this.icons.get(i).icon)
           .setLabel(this.icons.get(i).description)
           .show();
      }
    
      for(int i = this.icons.size(); i < MAX_ICONS; i++) {
        cp5.get(Button.class, "explanationIcon" + str(i))
           .hide();
      }
      
      cp5.get(Textlabel.class, "characterLabel").show();
      
    } else {
      for(int i = 0; i < MAX_ICONS; i++) {
        cp5.get(Button.class, "explanationIcon" + str(i))
           .hide();        
      }
      
      cp5.get(Textlabel.class, "characterLabel").hide();
    }
  }
  
  public void updateImage(PImage img){
    this.img = img;
  }
  
  public void draw(PGraphics pg){
    //pg.fill(color(238, 233, 209, 100));
    //pg.rect(posX, posY, sizeX, sizeY);
    pg.fill(color(0, 0, 0, 10));
    pg.rect(posX + padX, posY + padY, sizeX - padX * 2 - imageSizeX - padX, iconSize + padY * 3); 
    
    int imgX = posX + sizeX - imageSizeX - padX;
    int imgY = posY + padY;
    if (img != null)
      pg.image(this.img, imgX, imgY, imageSizeX, imageSizeY);
      
    //if (icons != null){
    //  for(int i = 0; i < icons.size(); i++) {
    //    ExplanationIcon icon = icons.get(i);
    //    PGraphics mask = createGraphics(icon.icon.width, icon.icon.height);
    //    mask.beginDraw();
    //    mask.smooth();
    //    mask.background(0);
    //    mask.fill(255);
    //    mask.ellipse(icon.icon.width/2, icon.icon.height/2, icon.icon.width, icon.icon.height);
    //    mask.endDraw();
        
    //    icon.icon.mask(mask);
    //    image(icon.icon, posX + padX * 2 + i*(iconSize + padX), posY + padY * 1.5, iconSize, iconSize);
    //  }
    //}
     
  }
}

ExplanationBox setExplanationBox(ControlP5 cp5, int posY) {
  
  int posX = 50;
  int sizeY = 550;
  
  int padX = 15;
  int padY = 15;
  
  int imageSizeX = 300;
  int imageSizeY = sizeY - padY * 2;
  int iconSize = 100;
  
  ExplanationBox eBox = new ExplanationBox();
  
  ExplanationCanvas cc = new ExplanationCanvas(posX, posY, padX, padY, sizeY, imageSizeX, imageSizeY, iconSize);
  cc.post();
  cp5.addCanvas(cc);  
  
  Textlabel title = cp5.addTextlabel("explanation title")
                           .setPosition(posX + padX, posY +iconSize + padY * 5)
                           .setSize(300, 50)
                           .setFont(ExplanationTitlefont)
                           .setColor(color(0));
  
  Textarea textarea = cp5.addTextarea("explanation")
                .setPosition(posX + padX, posY + iconSize + padY * 7)
                .setSize(width - 2*posX - imageSizeX - 3 * padX, sizeY - iconSize - padY * 5)
                .setFont(Explanationfont)
                .setColor(color(0))
                .setLineHeight(24)
                .bringToFront()
                .disableColorBackground();
                
  for(int i = 0; i < MAX_ICONS; i++) {
    float iconPosX = posX + padX * 2 + i*(iconSize + padX) + 100;
    float iconPosY = posY + padY * 3.5;
    cp5.addButton("explanationIcon" + str(i))
       .setSize(iconSize, iconSize)
       .setPosition(iconPosX, iconPosY)
       .hide()
       .addCallback(new CallbackListener(){
         
         public float posX;
         public float posY;
         
         public CallbackListener setPos(float posX, float posY){
           this.posX = posX;
           this.posY = posY;
           return this;
         }
         
         public void controlEvent(CallbackEvent ev) {
           if(ev.getAction() == 6){ // hover
             onIconHover(ev.getController().getLabel(), posX, posY);
           }
           else if(ev.getAction() == 7){ // hover out
             onIconHoverOut();
           }
         }
       }.setPos(iconPosX, iconPosY));
  }
  
  iconDescription = cp5.addTextlabel("iconDescription")
                       .setSize(200, 100)
                       .setColor(color(0))
                       .setColorBackground(color(0))
                       .setFont(ExplanationTitlefont)
                       .hide();
  
  iconDescriptionBackground = cp5.addTextarea("iconDescriptionBackground")
                                 .setSize(200, 100)
                                 .setColor(color(0))
                                 .setColorBackground(color(0))
                                 .hide();

  
  eBox.textarea = textarea;
  eBox.title = title;
  eBox.canvas = cc;
  
  return eBox;
}

void onIconHover(String description, float posX, float posY) {
  iconDescription.setText(description)
                 .setPosition(posX, posY - 30)
                 .bringToFront()
                 .show();
}

void onIconHoverOut() {
  iconDescription.hide();
  iconDescriptionBackground.hide();
}
