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
       
      for(int i=0 ;i<Math.abs(this.posY-anciennePosY); i++){ //the lapin moves only in Y Y.
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
   
       public void seReproduire(Loft loft){
         if (this.niveau>=50){
             Boolean findPart = false;
             Mangeable tampon ;
             int i = 0;
             while(!findPart && i<loft.plateau[this.posX][this.posY].listPresence.size()){
                 tampon = loft.plateau[this.posX][this.posY].listPresence.get(i);
                 // we verify that tampon is a Neuneu 
                 // and tampon is not this
                 // and tampon as enought energy to make a baby
                 // and tampon and this can have a baby together
                 if((tampon instanceof Neuneu)&& !(this.equals(tampon)) && tampon.niveau>=50 && (((Neuneu)tampon).sexe+this.sexe)==1){
                     this.niveau-=50;                    
                     tampon.niveau-=50;
                     //then the baby is created
                     Lapin baby = new Lapin(this.posX,this.posY,Color.YELLOW,new Point(this.posX,this.posY),new Dimension(5,5),this.jcanvas);
                     baby.setNiveau(50);
                     findPart=true;
                     loft.populationToAdd.add((Neuneu)baby);
                     baby.majPresence(loft.plateau[this.posX][this.posY]);
                     
                 }
                 else i++;
                         }          
         }
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
