 import java.io.*;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 
public class Poly
 {
 
    //-----------------------------------------------------------------
    //  Start of main body of the program.  Includes inputing the data
    //  and making the call to the Poly class.
    //-----------------------------------------------------------------
 
    public static void main (String [] args) throws IOException
    {
       BufferedReader stdin = new BufferedReader (new InputStreamReader (System.in));
       boolean cont = true;
       PolyObj poly = null;
 
       while (cont)
       {
          poly = PolyInput.getPolyInput(stdin);
 
          System.out.println ();
          System.out.println ("To terminate the program close the Polyspiral" +
                           " window. Otherwise press 1 to draw");
          System.out.print ("another polyspiral: ");
 
          Frame foo = new Frame("Polyspiral");
          foo.addWindowListener (new WindowAdapter ()
             {public void windowClosing (WindowEvent e) {System.exit(0);}});
          foo.setSize (725, 725);
          foo.add (new PolyCanvas (poly));
          foo.setVisible(true);
 
          if (Integer.parseInt (stdin.readLine()) == 0)
         {
            foo.dispose();
             cont = false;
         }
       }//end of while
    stdin.close();
 
     }//end of main
   }//end of poly 
