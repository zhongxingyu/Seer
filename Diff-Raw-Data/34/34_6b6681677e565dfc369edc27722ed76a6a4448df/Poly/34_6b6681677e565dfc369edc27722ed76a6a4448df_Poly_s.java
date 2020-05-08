 import java.io.*;
 import java.util.*;
import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
public class Poly //extends Frame
 {
   private static final long serialVersionUID = 1234L;
 
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
             cont = false;
         
         stdin.close();
       }//end of while
 
     }//end of main

  //-----------------------------------------------------------------
  //  Class that creates the window to display the Polyspiral and
  //  calls the class that creates the Polyspiral.
  //-----------------------------------------------------------------
/*
 Poly (PolyObj poly)
    {

    super ("Polyspiral");
    addWindowListener (new WindowAdapter ()
       {public void windowClosing (WindowEvent e) {System.exit(0);}});
    setSize (725, 725);
    add (new CvSquare ());
    setVisible(true);
    }// end of main

  */
   }//end of poly 
