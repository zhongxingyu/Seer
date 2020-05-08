 /*
  *  The OpenDiamond Platform for Interactive Search
  *  Version 3
  *
  *  Copyright (c) 2007 Carnegie Mellon University
  *  All rights reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.opendiamond;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 public class Test {
 
     /**
      * @param args
      * @throws IOException
      */
     public static void main(String[] args) throws IOException {
         // set up the rgb filter
         Filter rgb = null;
         Filter thumb = null;
         try {
             FilterCode c = new FilterCode(
                     new FileInputStream("/tmp/fil_rgb.so"));
             rgb = new Filter("RGB", c, "f_eval_img2rgb", "f_init_img2rgb",
                     "f_fini_img2rgb", 1, new String[0], new String[0], 400);
             System.out.println(rgb);
 
             c = new FilterCode(new FileInputStream("/tmp/fil_thumb.so"));
             thumb = new Filter("thumb", c, "f_eval_thumbnailer",
                     "f_init_thumbnailer", "f_fini_thumbnailer", 1,
                     new String[] { "RGB" }, new String[] { "200", "150" }, 0);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // init diamond
         Search2 search = new Search2();
         System.out.println("defining scope");
         search.defineScope();
 
         // make a new searchlet
         Searchlet searchlet = new Searchlet();
         searchlet.addFilter(rgb);
         searchlet.addFilter(thumb);
         searchlet.setApplicationDependencies(new String[] { "RGB" });
         search.setSearchlet(searchlet);
         search.setPushAttributes(new HashSet<String>(Arrays
                 .asList(new String[] { "thumbnail.jpeg" })));
 
         Result r;
 
         DoubleComposer sum = new DoubleComposer() {
             public double compose(String key, double a, double b) {
                 // ignore key
                 return a + b;
             }
         };
 
         for (int ii = 0; ii < 2; ii++) {
             // begin search
             System.out.println("starting search");
             search.start();
 
             Map<String, Double> map = new HashMap<String, Double>();
             map.put("Hi", 42.0);
             map.put("Oops", 23842938.0);
 
             search.mergeSessionVariables(map, sum);
 
             // read some results
             int count = 0;
             try {
                 while ((r = search.getNextResult()) != null && count < 10) {
                     processResult(search, r);
 
                     System.out.println(search.mergeSessionVariables(map, sum));
 
                     count++;
                 }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             search.stop();
         }
     }
 
     private static void processResult(final Search2 s, final Result r)
             throws IOException {
         System.out.println(r);
 
         byte[] data = r.getValue("thumbnail.jpeg");
         BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
 
         int w = img.getWidth();
         int h = img.getHeight();
 
         System.out.println(w + "x" + h);
 
         JFrame j = new JFrame();
         j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         JButton b = new JButton(new ImageIcon(img));
         b.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                Result r2;
                 try {
                    r2 = s.reevaluateResult(r, new HashSet<String>(Arrays
                            .asList(new String[] { "" })));
                    System.out.println(r2);

                     byte data[] = r2.getData();
                     BufferedImage bigimg = ImageIO
                             .read(new ByteArrayInputStream(data));
                     JLabel l = new JLabel(new ImageIcon(bigimg));
                     JFrame j = new JFrame();
                     j.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                     j.add(l);
                     j.pack();
                     j.setVisible(true);
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 }
             }
         });
         j.getContentPane().add(b);
         j.pack();
         j.setVisible(true);
     }
 }
