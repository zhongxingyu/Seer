 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package yazLabChartLib;
 import java.awt.*;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import java.util.*;
 import java.lang.Math;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 /**
  *
  * @author ugurcan
  * Took some help and code from this link
  * http://cs.fit.edu/~ryan/java/applets/piechart/PieChart-java.html
  */
 public class pieChart extends JPanel{
     String[] name;
     int[] values;
     int topNCount;
     int count;
     int cx=0;
     int cy=0;
     int lx=0;
     int ly=0;
     int total;
 
    Color[] colors = {Color.BLUE,Color.CYAN,Color.GREEN,Color.MAGENTA,Color.ORANGE,Color.RED,Color.YELLOW,Color.PINK,Color.GRAY,Color.DARK_GRAY,Color.LIGHT_GRAY};
 
 
     public pieChart(String[] nm, int[] vls,int lm, int cnt,int tl){
         name = nm;
         values = vls;
         topNCount = lm;
         count = cnt;
         total = tl;
 
     }
 
     public void paintComponent (Graphics g)
     {
         int i,j;
         int sliceCount;
         int strWidth;
     Font labelFont = new Font("Book Antiqua", Font.PLAIN, 10);
     FontMetrics labelFontMetrics = g.getFontMetrics(labelFont);
             float factor = (float) 100 / total;
 
             if (count < topNCount){
                 sliceCount = count;
             }
             else {
                 sliceCount = topNCount;
             }
             float[] percent = new float[sliceCount];
             float[] angle = new float[sliceCount];
             for (i=0; i < sliceCount; i++) {
                 percent[i]= values[i] * factor;
                 angle[i]  = (float) (percent[i] * 3.6) ;
             }
             System.out.println(total);
                   /*  for (i=0;i<sliceCount;i++){
             System.out.println(percent[i]+" ");
         }
         for (i=0;i<sliceCount;i++){
             System.out.println(angle[i]+" ");
         }*/
             int width  = Math.min((getSize().width - 100),(getSize().height - 100));
             int height = width;
             int x=50;
             int y=x;
             if (getSize().width > width){
             x = (getSize().width - width ) /2 ;
             }
 
// Center of pie (cx, cy are global variables)
             cx = x + width/2;
             cy = y + height/2;
             final int radius = width/2;
 
             int initAngle=90;
             int sweepAngle=0;
             int incSweepAngle=0;
             int incLabelAngle= (int) (angle[0]/2);
              for(i=0; i < sliceCount; i++) {
   sweepAngle = (int) Math.round(angle[i]);
  g.setColor((Color)colors[i]);
 
 
       if (i==(sliceCount-1)){
              sweepAngle = 360 - incSweepAngle;
              g.fillArc(x,y,width,height,initAngle,(-sweepAngle));
              g.setColor(Color.black);
              g.drawArc(x,y,width,height,initAngle,(-sweepAngle));
 
                lx = (int) (cx + (radius * Math.cos((incLabelAngle * 3.14f/180) - 3.14f/2)));
                ly = (int) (cy + (radius * Math.sin((incLabelAngle * 3.14f/180) - 3.14f/2)));
              if ( (lx > cx) && (ly < cy) ){
                     lx +=5; ly -=5;
                     }
 
              if ( (lx > cx) && (ly > cy) ){
                     lx +=5; ly +=10;
                  }
 
              if ( (lx < cx) && (ly > cy) ){
                     strWidth=labelFontMetrics.stringWidth(name[i]);
                     lx -= strWidth+5;
                     if (lx < 0) lx=0;
                  }
 
              if ( (lx < cx) && (ly < cy) ){
                     strWidth=labelFontMetrics.stringWidth(name[i]);
                     lx -= strWidth+5;
                     if (lx < 0) lx=0;
                  }
                g.drawString((String)name[i],lx,ly);
            final int px = (int) (cx + ((radius*2/3) * Math.cos((incLabelAngle * 3.14f/180) - 3.14f/2)));
            final int py = (int) (cy + ((radius*2/3) * Math.sin((incLabelAngle * 3.14f/180) - 3.14f/2)));
            g.drawString(String.valueOf(Math.round(percent[i]))+"%",px,py);
     break;
  }
 
    g.fillArc(x,y,width,height,initAngle,(-sweepAngle));
   g.setColor(Color.black);
   g.drawArc(x,y,width,height,initAngle,(-sweepAngle));
   incSweepAngle +=sweepAngle;
 
       //  Black line from center to Peripherial
       final int ax = (int) (cx + (radius * Math.cos((incSweepAngle * 3.14f/180) - 3.14f/2)));
   final int ay = (int) (cy + (radius * Math.sin((incSweepAngle * 3.14f/180) - 3.14f/2)));
   g.drawLine(cx,cy,ax,ay);
 
              lx = (int) (cx + (radius * Math.cos((incLabelAngle * 3.14f/180) - 3.14f/2)));
              ly = (int) (cy + (radius * Math.sin((incLabelAngle * 3.14f/180) - 3.14f/2)));
               if ( (lx > cx) && (ly < cy) ){
                     lx +=5; ly -=5;
                     }
 
              if ( (lx > cx) && (ly > cy) ){
                     lx +=5; ly +=10;
                  }
 
              if ( (lx < cx) && (ly > cy) ){
                     strWidth=labelFontMetrics.stringWidth(name[i]);
                     lx -= strWidth+5;
                     if (lx < 0) lx=0;
                  }
 
              if ( (lx < cx) && (ly < cy) ){
                     strWidth=labelFontMetrics.stringWidth(name[i]);
                     lx -= strWidth+5;
                     if (lx < 0) lx=0;
                  }
              g.drawString((String)name[i],lx,ly);
          final int px = (int) (cx + ((radius*2/3) * Math.cos((incLabelAngle * 3.14f/180) - 3.14f/2)));
          final int py = (int) (cy + ((radius*2/3) * Math.sin((incLabelAngle * 3.14f/180) - 3.14f/2)));
          strWidth = labelFontMetrics.stringWidth(Math.round(percent[i])+"%");
          g.drawString(String.valueOf(Math.round(percent[i]))+"%",(px - strWidth/2),py);
 
       incLabelAngle = incLabelAngle + (int) (angle[i]/2 + angle[i+1]/2);
       initAngle += (-sweepAngle);
        }
        g.setColor(Color.black);
        g.drawLine(cx,cy,cx,cy-radius);
 
     }// Center of pie (cx, cy are global variables)
 
 }
