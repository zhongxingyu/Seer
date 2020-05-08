 import java.awt.Color;
 import java.awt.GraphicsEnvironment;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsConfiguration;
 import java.awt.Transparency;
 
 /**
  *
  * @author gmcwhirt
  */
 public class DtRGraphGenerator implements GraphGenerator {
     private CanvasImage ci;
     /*
      * The grid is:
      *      A, B    |   C, D
      *      E, F    |   G, H
      */
     private int A, B, C, D, E, F, G, H;
 
     private float tolerance = 1e-6f;
 
     public DtRGraphGenerator(int Ap, int Bp, int Cp, int Dp, int Ep, int Fp, int Gp, int Hp, int width, int height){
         A = Ap;
         B = Bp;
         C = Cp;
         D = Dp;
         E = Ep;
         F = Fp;
         G = Gp;
         H = Hp;
 
         int min;
 
         //adjust all payoffs to be non-negative
         min = Math.min(Math.min(A, C), Math.min(E, G));
         if (min < 0){
             A -= min;
             C -= min;
             E -= min;
             G -= min;
         }
 
         min = Math.min(Math.min(B, D), Math.min(F, H));
         if (min < 0){
             B -= min;
             D -= min;
             F -= min;
             H -= min;
         }
 
 
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gs = ge.getDefaultScreenDevice();
         GraphicsConfiguration gc = gs.getDefaultConfiguration();
 
         ci = new CanvasImage(gc.createCompatibleImage(width, height, Transparency.BITMASK));
     }
 
     @Override
     public CanvasImage getCImage(){
         return ci;
     }
 
     @Override
     public CanvasImage generate(){
 
        float xf, yf, xxf, yyf;
         float[] oldxy;
         float[] newxy;
 
         int dots = 5;
         for (int x = 0; x <= dots; x++){
             for (int y = 0; y <= dots; y++){
                xf = (float)x / (float)dots;
                yf = (float)y / (float)dots;
 
                 oldxy = new float[2];
                 newxy = new float[2];
                newxy[0] = xf;
                newxy[1] = yf;
 
                 do {
                     oldxy = newxy.clone();
                     newxy = genstep(oldxy);
 
                    xf = oldxy[0];
                    yf = oldxy[1];
                    xxf = newxy[0];
                    yyf = newxy[1];

                    ci.drawArrow(xf, yf, xxf, yyf, Color.green, Color.black);
                    ci.drawLine(xf, yf, xf, yf, Color.black);
                 } while (Math.abs(oldxy[0] - newxy[0]) > tolerance || Math.abs(oldxy[1] - newxy[1]) > tolerance);
             }
         }
 
         return ci;
     }
 
     private float[] genstep(float[] oldxy){
         float[] newxy = new float[2];
 
         try {
             float avg0 = avg_payoff(0, oldxy);
             float avg1 = avg_payoff(1, oldxy);
 
             if (avg0 != 0f){
                 newxy[0] = (.1f + payoff(0, oldxy)) / (.1f + avg0);
             } else {
                 newxy[0] = oldxy[0];
             }
 
             if (avg1 != 0f){
                 newxy[1] = (.1f + payoff(1, oldxy)) / (.1f + avg1);
             } else {
                 newxy[1] = oldxy[1];
             }
         } catch (Exception e){
             newxy = oldxy;
         }
 
         return newxy;
     }
 
     private float payoff(int typ, float[] pops) throws Exception{
        return payoff(typ, typ, pops);
     }
 
     private float payoff(int str, int typ, float[] pops) throws Exception{
        //generate payoff for the top row or the right column
         /*
          * The grid is:
          *      A, B    |   C, D
          *      E, F    |   G, H
          */
 
         int opp = 1 - typ;
         float opp0 = pops[opp]; // this is the right col or top row
         float opp1 = 1f - opp0; // this is the left col or bottom row
         float score = 0f;
 
         switch (typ){
            case 0:
                 //row player
                 if (str == 0){
                     score = (float)A * opp1 + (float)C * opp0;
                 } else if (str == 1){
                     score = (float)E * opp1 + (float)G * opp0;
                 } else {
                     throw new Exception("Invalid str parameter");
                 }
                 break;
            case 1:
                 //column player
                 if (str == 0){
                     score = (float)B * opp0 + (float)F * opp1;
                 } else if (str == 1){
                     score = (float)D * opp0 + (float)H * opp1;
                 } else {
                     throw new Exception("Invalid str parameter");
                 }
                 break;
             default:
                 throw new Exception("Invalid type parameter");
         }
 
         return score;
     }
 
     private float avg_payoff(int typ, float[] pops) throws Exception{
         float score = 0f;
 
         /*
          * The grid is:
          *      A, B    |   C, D
          *      E, F    |   G, H
          */
 
        score = pops[typ] * payoff(typ, typ, pops) + (1f - pops[typ]) * payoff(1 - typ, typ, pops);
 
         return score;
     }
 }
