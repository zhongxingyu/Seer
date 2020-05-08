 import javax.swing.*;
 import java.awt.*;
 
 public class Test extends JFrame {
   
   public Test()
   {
     super("Testing the Plane");
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setSize(500, 400);
 
     Plane plane = new Plane();
     add(plane, BorderLayout.CENTER);
 
     // Create a function with expression and name
     Function f = new Function("sin(x) * 2", "f(x)");
     f.setColor(Color.RED); // Set color for the graph
 
     // Set the desired scale for the plane
     plane.setScaleInX(1);
     plane.setScaleInY(5);
 
     // Enable grid in plane
     plane.setShowGrid(true);
 
     // Plot function, the plane store a list of functions so that you can
    // graph many functions at the same time
     plane.plot(f);
 
     Function g = new Function("x^3 - x", "g(x)");
     g.setColor(Color.BLUE);
     plane.plot(g);
 
     //plane.removeFunction("g(x)");
     setLocationRelativeTo(null);
     setVisible(true);
   }
 
   public static void main(String[] args)
   {
     Test test = new Test();
   }
 }
 
