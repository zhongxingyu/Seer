 package net.robinjam.mandelbrot;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Panel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JFrame;
 import javax.swing.Timer;
 import net.robinjam.mandelbrot.compute.Job;
 
 public class MandelbrotGUI extends Panel implements ActionListener {
     
     Job job;
     Timer timer;
     
     public MandelbrotGUI(int width, int height, int max_iterations) {
         setPreferredSize(new Dimension(width, height));
         
         Viewport viewport = new Viewport();
         job = new Job(viewport, width, height, max_iterations);
         
         timer = new Timer(1000, this);
         timer.start();
     }
     
     @Override
     public void paint(Graphics g) {
         Graphics2D g2d = (Graphics2D) g;
         g2d.drawImage(job.getImage(), null, this);
     }
     
     @Override
     public void actionPerformed(ActionEvent ae) {
         repaint();
         if (job.isDone()) {
             timer.stop();
         }
     }
     
     public static void main(String[] args) {
         JFrame window = new JFrame("Fractal explorer");
         
         window.add(new MandelbrotGUI(800, 600, 200000));
         window.pack();
         
         window.setResizable(false);
         window.setLocationRelativeTo(null);
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         window.setVisible(true);
     }
     
 }
