 package org.manojlovic.skockoj2me;
 
 import javax.microedition.lcdui.*;
 import java.io.IOException;
 import java.util.Random;
 
 /**
  *
  * @author Manojlović Miloš (manojlovic88@gmail.com)
  */
 public class Graphic extends Canvas implements Runnable {
     private int w = getWidth();
     private int h = getHeight();
     private int x1; // coordinate 0,0
     private int y1;
     
     private int time = 0; // time in seconds
     
     private boolean game_over = true;
     private boolean end_game = false;
     // array of images
     private int[] selectedCircls = {0,0,0,0, // row 1
                                     0,0,0,0, 
                                     0,0,0,0, 
                                     0,0,0,0, 
                                     0,0,0,0, 
                                     0,0,0,0};// row 6
     private int[] selectedCircls_res = {0,0,0,0, // row 1
                                         0,0,0,0, 
                                         0,0,0,0, 
                                         0,0,0,0, 
                                         0,0,0,0, 
                                         0,0,0,0};// row 6
     
     private Random r = new Random();
    private int[] result = new int[]{this.r.nextInt(5), this.r.nextInt(5), this.r.nextInt(5), this.r.nextInt(5)};
     
     private Image[] img = new Image[11];
     private int step = 10; // in pixels
     
     private Thread t; // time thread
     
     private int cursor = 0; // position of image selektor 0-5
     private int[] cursorSteps = new int[]{3*step, 4*step, 5*step, 6*step, 7*step, 8*step};
     private boolean clickedFire = false;
     private int clickedFire_count = 0;
     private int line = 0;
     
     private SkockoLogic skocko = new SkockoLogic(result);
     
     public Graphic(){
         try {
             if(this.w >= 240 && this.h >= 240){
                 this.img[0] = Image.createImage("/20/MalaPozadina20.jpg");
                 this.img[1] = Image.createImage("/20/Herc20.jpg");
                 this.img[2] = Image.createImage("/20/Pic20.jpg");
                 this.img[3] = Image.createImage("/20/Karo20.jpg");
                 this.img[4] = Image.createImage("/20/Tref20.jpg");
                 this.img[5] = Image.createImage("/20/Skocko20.jpg");
                 this.img[6] = Image.createImage("/20/Zvezda20.jpg");
                 this.img[7] = Image.createImage("/20/BeliKruzic20.jpg");
                 this.img[8] = Image.createImage("/20/CrveniKruzic20.jpg");
                 this.img[9] = Image.createImage("/20/ZutiKruzic20.jpg");
                 this.img[10] = Image.createImage("/20/Background240full.jpg");
                 
                 this.step = 20;
             }else{
                 this.img[0] = Image.createImage("/MalaPozadina10.jpg");
                 this.img[1] = Image.createImage("/Herc10.jpg");
                 this.img[2] = Image.createImage("/Pic10.jpg");
                 this.img[3] = Image.createImage("/Karo10.jpg");
                 this.img[4] = Image.createImage("/Tref10.jpg");
                 this.img[5] = Image.createImage("/Skocko10.jpg");
                 this.img[6] = Image.createImage("/Zvezda10.jpg");
                 this.img[7] = Image.createImage("/BeliKruzic10.jpg");
                 this.img[8] = Image.createImage("/CrveniKruzic10.jpg");
                 this.img[9] = Image.createImage("/ZutiKruzic10.jpg");
                 this.img[10] = Image.createImage("/background120full.jpg");
             }
         } catch (IOException ex) {
             //g.drawString("Nema slike:"+ex, x1-70, y1-40, 0);
             ex.printStackTrace();
         }
         
         // set coordinate
         x1 = (w - 12 * step) / 2;
         y1 = (h - 12 * step) / 2;
         
         this.t= new Thread(this, "Time");
         this.t.start();
     }
     
     public void paint(Graphics g){
         g.setColor(0, 23, 90);
         g.fillRect(0, 0, w, h);
         
         g.drawImage(this.img[10], x1, y1, 0); // Background
         // header images
         g.drawImage(this.img[1], x1+3*step, y1+step, 0);
         g.drawImage(this.img[2], x1+4*step, y1+step, 0);
         g.drawImage(this.img[3], x1+5*step, y1+step, 0);
         g.drawImage(this.img[4], x1+6*step, y1+step, 0);
         g.drawImage(this.img[5], x1+7*step, y1+step, 0);
         g.drawImage(this.img[6], x1+8*step, y1+step, 0);
         
         // time
         int change_color = 50; // sec when to change color
         
         g.setColor(255, 255, 0);
         g.fillRect(x1+6*step-step/5, y1+3*step, 2*step/5, time<change_color ? 1*time * step/10 : 1*change_color * step/10); // 58, 30, 4
         
         if(time >= change_color){
             g.setColor(255,0, 0);
             g.fillRect(x1+6*step-step/5, y1+3*step + step/10*change_color, 2*step/5, (1*time-1*change_color) * step/10);
         }
         
         moveCursor(g);
         
         if(clickedFire){
             this.clickedFire = false;
             if(clickedFire_count < 24){ // 24 fields
                 //TODO sredi niz da selektovana slika odgovara
                 selectedCircls[clickedFire_count] = cursor + 1; // index of array -> image
                 clickedFire_count++;
             }
             
             if(clickedFire_count % 4 == 0 && clickedFire_count != 0 && line < 6){
                 //TODO sredi niz da selektovana slika odgovara
                 int[] res = skocko.chechResult(new int[]{selectedCircls[4*line+0] - 1, selectedCircls[4*line+1] - 1, selectedCircls[4*line+2] - 1, selectedCircls[4*line+3] - 1});
                 selectedCircls_res[4*line+0] = res[0];
                 selectedCircls_res[4*line+1] = res[1];
                 selectedCircls_res[4*line+2] = res[2];
                 selectedCircls_res[4*line+3] = res[3];
                 
                if(res[0] == 8 && res[1] == 8 && res[2] == 8 && res[3] == 8){ // four red circles
                     game_over = false;
                     the_end(g);
                 }
                     
                 this.line++;
             }
         }
         
         this.drawSelectedCircls(g);
         
         if(end_game || line == 6)
             the_end(g);
        
        //TODO delete it's TEST
        g.setColor(255, 0, 0);
        //g.drawString("rez - "+result[0]+", "+result[1]+", "+result[2]+", "+result[3], 10, 10, 0);
        
     }
     
     public void run(){
         while (time < 60 && !end_game){ // 60sec
             try {
                 Thread.sleep(1000); // one sec
             } catch (InterruptedException ex) {
                 ex.printStackTrace();
             }
             time++;
             repaint();
         }
         
         this.end_game = true;
     }
     
     protected void keyPressed(int keyCode){
         int gameAction = getGameAction(keyCode);
 
         if(!end_game){
             switch(gameAction){
                 case UP:
                     cursor +=1;
                     if(cursor==6)
                         cursor=0;
                     break;
                 case DOWN:
                     cursor -=1;
                     if(cursor==-1)
                         cursor=5;
                     break;
                 case RIGHT:
                     cursor +=1;
                     if(cursor==6)
                         cursor=0;
                     break;
                 case LEFT:
                     cursor -=1;
                     if(cursor==-1)
                         cursor=5;
                     break;
                 case FIRE:
                     clickedFire=true;
                     // "[FIRE]";
                 break;
 
                 default:
                     // "";
                 break;
             }
         }
 
         repaint();
     }
     private void moveCursor(Graphics g){
         g.setColor(255, 0, 0);
         g.drawRect(x1+cursorSteps[cursor] * step/10, y1+step, step, step);
     }
     
     private void drawSelectedCircls(Graphics g){
         int[] x={step,2*step,3*step,4*step};
         int[] y={3*step,4*step,5*step,6*step,7*step,8*step,9*step};
         int k = 0; // inner couter
 
         for(int i=0;i<6;i++){
             for(int j=0;j<4;j++){
                 g.drawImage(this.img[selectedCircls[k]], x1 + x[j], y1 + y[i], 0);
                 g.drawImage(this.img[selectedCircls_res[k]], x1 + x[j] + 6*step, y1 + y[i], 0);
                 k++;
             }
         }
     }
     
     private void the_end(Graphics g){
         g.drawImage(this.img[result[0] + 1], x1 + 4*step, y1 + 10*step, 0);
         g.drawImage(this.img[result[1] + 1], x1 + 5*step, y1 + 10*step, 0);
         g.drawImage(this.img[result[2] + 1], x1 + 6*step, y1 + 10*step, 0);
         g.drawImage(this.img[result[3] + 1], x1 + 7*step, y1 + 10*step, 0);
         
         if(game_over){
             g.setColor(255, 0, 0);
             g.drawString("GAME OVER", x1+3*step + step, y1+5*step, 0); // 30, 50
         }else{
             g.setColor(255, 0, 0);
             g.drawString("WIN!!!", x1+5*step + step, y1+5*step, 0);
         }
         
         end_game = true;
     }
 
 }
