 package tpneuneu;
 
 import graphisme.JCanvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 
 public class Lapin extends Neuneu {
     
   public Lapin(int a, int b,Color color, Point pos, Dimension dim, JCanvas jc){
       super(color,pos,dim,jc);
       
       this.nameId++;
       this.name="Lapin"+this.nameId;
       this.posX=a;
       this.posY=b;
       this.niveau=100;
       this.sexe=(int)(Math.random()*2);
       jc.addDrawable(this);
   }
   
   @Override
   public int seDeplacer(Loft loft) {
       int anciennePosX=this.posX, anciennePosY=this.posY;
       this.plusProcheVoisin(loft);
       
      for(int i=0; i<Math.abs(this.posX-anciennePosX); i++){ //the lapin moves only in X first, then Y.
         if(this.posX-anciennePosX>=0){
             //we call the method to make neuneu eat
              this.manger(loft.plateau[anciennePosX+i][anciennePosY].listPresence);
              int k = 0;
         }
         else{
             //we call the method to make neuneu eat
              this.manger(loft.plateau[anciennePosX-i][anciennePosY].listPresence);
              int k = 0;
         } 
       }
       
      for(int i=0; i<Math.abs(this.posY-anciennePosY); i++){ //the lapin moves only in Y Y.
         if(this.posY-anciennePosY>=0){
             //we call the method to make neuneu eat
              this.manger(loft.plateau[anciennePosX][anciennePosY+i].listPresence);
              int k = 0;
         }
         else{
             //we call the method to make neuneu eat
              this.manger(loft.plateau[anciennePosX][anciennePosY-i].listPresence);
              int k = 0;
         } 
       }
       return 1;
   }
 
 
       	public void draw(Graphics g) {
                 if(niveau==0){
 		Color c = g.getColor();
 		g.setColor(color);
 		g.drawRect((this.posX)*20+15,(this.posY)*20,5,5);
                   g.setColor(c);
                 }
                 if(niveau>0){
 		Color c = g.getColor();
 		g.setColor(color);
 		g.fillRect((this.posX)*20+15,(this.posY)*20,5,5);
                   g.setColor(c);
                 }
                 
 	}      
 }
