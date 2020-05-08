 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * Footage.java
  *
  * Created on 09.07.2012, 23:47:33
  */
 package videoMixerLite.resources;
 
 
 import codeanticode.glgraphics.*;
 import codeanticode.gsvideo.*;
 
 //import java.awt.Image;
 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import javax.swing.BorderFactory;
 import javax.swing.JFileChooser;
 //import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import processing.core.*;
 
 
 
 /**
  *
  * @author User
  */
 public class Footage extends javax.swing.JPanel {
 
     /** Creates new form Footage */
     
     
     
     public Effect[] effect;
     
     private static JFileChooser fc = new JFileChooser();
     
     public Core core;
     
     public GLTexture tex;
     
     private String footageName;
     
     //private GLTextureCanvas texCanvas;
     private GSMovie movie;
     
     private boolean movieReady = false;
     
     private boolean previewSetup = false;
     
     //public boolean editable;
     
     //private boolean isPaused = true;
     
     public PImage image = new PImage();
     
     private Channel channel;
     
     //public static Footage footageEditable;
     
     private Preview preview;
     
     //private JPanel propertiesPanel;
     private FootageProperties footageProperties;
     private MovieProperties movieProperties;
     private BitmapProperties bitmapProperties;
     
     //private Timer timer;
     
     
     public Footage() {
         initComponents();
     }
     
     public void init(Channel parent)
     {
         channel = parent;
         core = channel.core;
         
         //editable = false;
         
         footageProperties = new FootageProperties();
         footageProperties.init(this);
         
         movieProperties = new MovieProperties();
         movieProperties.init(this);
         
         bitmapProperties = new BitmapProperties();
         bitmapProperties.init(this);
         
         //propertiesPanel = footageProperties;
         
         
         displayPanel.addMouseListener(new MouseAdapter() 
         {
             @Override
             public void mousePressed(MouseEvent evt) 
             {
                 channel.clear();
             }
         });
         
         
         jButton1.setEnabled(true);
         
         effect = new Effect[0];
     }
     
     
     
     private void select()
     {
         channel.selectFootage(this);
         if(movie != null)
         {
             movie.play();
         }
         else if(tex != null)
         {
             core.redraw();
         }
         
         //core.redraw();
         
         setBorder(BorderFactory.createEtchedBorder(new Color(255, 255, 0), new Color(255, 255, 0)));
     }
     
     
     public void deselect()
     {
         if(movie != null)
         {
             //movie.jump(0);
             movie.goToBeginning();
             movie.pause();
             //redraw();
         }
         setBorder(BorderFactory.createEtchedBorder());
     }
     
     
     
     
     public void redraw()
     {      
         //System.out.println("redraw() --> movieReady = " + movieReady);
         /*
         if(movie == null  && tex != null && !tex.available() && image != null)
         {
             System.out.println("redraw() --> tex.available() = " + tex.available());
             
             System.out.println("redraw() --> image = " + image);
             
             //System.out.println("redraw() --> tex = " + tex);
             //tex.putImage(image.get());
             tex.putImage(image);
             //tex.updatePixels();
             //tex.updateTexture();   
         }
         */
         
         
         if(movieReady)
         {
             try
             {
                 tex.putPixelsIntoTexture();
             }
             catch (Exception evt)
             {
                  System.out.println("Exception: Footage redraw() movieReady ----> " + evt);
             }
         }
         else if(previewSetup)
         {
             try
             {
                 if(tex.putPixelsIntoTexture())
                 {
                     movieReady = true;
                     previewSetup = false;
                     
                     movie.goToBeginning();
                     movie.pause();
                     
                     /*
                     image = new PImage();
                     
                     tex.getImage(image);
                     
                     
                     Preview preview = new Preview(92, 50);
                     preview.setImage(image);
                     displayPanel.add(preview);
                     preview.init(); 
                     
                     preview.addMouseListener(new MouseAdapter() 
                     {
                         @Override
                         public void mousePressed(MouseEvent evt) 
                         {
                             select();
                         }
                     });
                     */
                     
                     image = new PImage();
                     tex.getImage(image);
                     setPrevew();
                     
                     //setPrevew(tex);
                     
                     
                     jButton1.setText(footageName);
                     
                     //System.out.println("footageName --> " + footageName);
                     
                     setActive(true);
                     
                     //propertiesPanel = movieProperties;
                     footageProperties.setPanel(movieProperties);
                     
                     
                     /*
                     if(footageEditable == this)
                     {
                         core.parent.setProperties(propertiesPanel);
                     } 
                     */
                     //setEditable(true);
                 }
             }
             catch (Exception evt)
             {
                  System.out.println("Exception: Footage redraw() previewSetup ----> " + evt);
             }
         }
         else if(/*movie == null  && */tex != null && !tex.available() && image != null)
         {
             //System.out.println("redraw() --> tex.available() = " + tex.available());
             //System.out.println("redraw() --> image = " + image);
             
             tex.putImage(image); 
             
             jButton1.setText(footageName);
             setActive(true);
                             
             footageProperties.setPanel(bitmapProperties);
         }
     }
     
     
     private void setPrevew(/*GLTexture t*/ /*PImage img*/)
     {
         /*
         image = new PImage();
         t.getImage(image);
         */
         
        
 
         /*Preview */preview = new Preview(92, 50);
         preview.setImage(image);
         //preview.setImage(img);
         displayPanel.add(preview);
         preview.init(); 
         
         
         //System.out.println(preview.frameCount);
 
         preview.addMouseListener(new MouseAdapter() 
         {
             @Override
             public void mousePressed(MouseEvent evt) 
             {
                 select();
             }
         });
         
         
         /*
         timer = new Timer();
         timer.schedule(new previewReadyWait(), 1);
         */
     }
     
     /*
     class previewReadyWait extends TimerTask
     {
 
         @Override
         public void run() 
         {
             //System.out.println(preview.frameCount);
             
             timer.cancel();
             if(preview.frameCount != 0)
             {
                 
                 if(movie == null)
                 {
                     tex.putImage(image);
                 }
                 
                 preview.addMouseListener(new MouseAdapter() 
                 {
                     @Override
                     public void mousePressed(MouseEvent evt) 
                     {
                         select();
                     }
                 });
             }
             else
             {
                 timer = new Timer();
                 timer.schedule(new previewReadyWait(), 1);
             }
         }
         
     }
     */
     
     
     private void setMovie(String path)
     {
         
         clearMovie();
         
         //movieReady = false;
         
         previewSetup = true;
         
         movie = new GSMovie(core, path);
         movie.setPixelDest(tex);
         movie.loop();
         movie.volume(0);
         
     }
     
     
     public void clearMovie()
     {
         movieReady = false;
         previewSetup = false;
         
         footageName = null;
         
         jButton1.setText("");
         
         image = null;
         
        displayPanel.removeAll();
         
         tex = new GLTexture(core);
         
         if(movie != null)
         {
             movie.stop();
             movie.dispose();
             movie.delete();
             movie = null;
         }
         
         
         if(preview != null)
         {
             preview.dispose();
             preview = null;
         }
         
         
         //propertiesPanel = footageProperties;
         
         footageProperties.clearPanel();
         //setEditable(true);
         
         /*
         if(footageEditable == this)
         {
             core.parent.setProperties(propertiesPanel);
         }
         */
         
         /*
         if(editable)
         {
             channel.showPropertiesPanel(propertiesPanel);
         }
         */
         
         
         
         if(channel.footageSelected == this)
         {
             channel.clear();
         }
         
     }
     
     
     
     public void open()
     {
         SwingUtilities.invokeLater(new Runnable() 
         {
             @Override
             public void run() 
             {
                 try 
                 {
                     setActive(false);
                     
                     int returnVal = fc.showOpenDialog(null);
                     if (returnVal == JFileChooser.APPROVE_OPTION) 
                     {
                         File file = fc.getSelectedFile();
                         String name = file.getName().toLowerCase();
                         if (name.endsWith(".avi") || name.endsWith(".mp4") || name.endsWith(".mov")) 
                         {
                             setMovie(file.getAbsolutePath());
                             
                             footageName = file.getName();
                             footageName = footageName.substring(0, footageName.lastIndexOf("."));
                             
                         }
                         else if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))
                         {
                             
                             clearMovie();
                             
                             footageName = file.getName();
                             footageName = footageName.substring(0, footageName.lastIndexOf("."));
                             
                             
                             
                             tex.loadTexture(file.getAbsolutePath());
                             image = tex.get();
                             setPrevew();
                             
                             core.redraw();
                             
                             /*
                             jButton1.setText(footageName);
                             setActive(true);
                             
                             footageProperties.setPanel(bitmapProperties);
                             */
                             
                         }
                         else
                         {
                             setActive(true);
                         }
                     }
                     else
                     {
                         setActive(true);
                     }
                 } 
                 catch (Exception e) 
                 {
                     //e.printStackTrace();
                     System.out.println("Exception: File Chooser ----> " + e);
                 }
             }
         });
     }
     
     
     
     /*
     class textureReadyWait extends TimerTask
     {
 
         @Override
         public void run() 
         {
             //System.out.println(tex.available());
             //System.out.println(image.getImage());
             System.out.println(image);
             
             timer.cancel();
             
             if(tex.available())
             {
                 
             }
             else
             {
                 //tex.putImage(image);
                 
                 timer = new Timer();
                 timer.schedule(new textureReadyWait(), 1000);
                 
             }
             
         }
     }
     */
     
     
     public void setEditable(boolean editable)
     {
         if(editable)
         {
             channel.showPropertiesPanel(footageProperties);
             jButton1.setBackground(new Color(255, 255, 0));
         }
         else
         {
             jButton1.setBackground(new Color(212, 208, 200));
         }
     }
     
     
     
     
     private void setActive(boolean active)
     {
         //System.out.println("setActive --> " + active);
         jButton1.setEnabled(active);
         footageProperties.setActive(active);
     }
 
     
     public void addEffect(Effect eff)
     {
         Effect[] temp = effect;
         effect = new Effect[effect.length + 1];
         /*
         for(int i = 0; i < temp.length; i++)
         {
             effect[i] = temp[i];
         }
         */
         System.arraycopy(temp, 0, effect, 0, temp.length);
         effect[temp.length] = eff;
         
         //System.out.println("Footage addEffect: " + effect.length);
     }
     
     
     public void removeEffect(Effect eff)
     {
         Effect[] temp = effect;
         effect = new Effect[effect.length - 1];
         int j = 0;
         for(int i = 0; i < temp.length; i++)
         {
           if(temp[i] != eff)
           {
             effect[j++] = temp[i];
           }
         }
         
         //System.out.println("Footage removeEffect: " + effect.length);
     }
     
     
     
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jButton1 = new javax.swing.JButton();
         displayPanel = new javax.swing.JPanel();
 
         setBorder(javax.swing.BorderFactory.createEtchedBorder());
         setAlignmentX(0.0F);
         setAlignmentY(0.0F);
         setFocusable(false);
         setMaximumSize(new java.awt.Dimension(96, 70));
         setMinimumSize(new java.awt.Dimension(96, 70));
         setPreferredSize(new java.awt.Dimension(96, 70));
 
        jButton1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jButton1.setAlignmentY(0.0F);
         jButton1.setEnabled(false);
         jButton1.setFocusable(false);
         jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
         jButton1.setMaximumSize(new java.awt.Dimension(96, 16));
         jButton1.setMinimumSize(new java.awt.Dimension(96, 16));
         jButton1.setPreferredSize(new java.awt.Dimension(96, 16));
         jButton1.setRequestFocusEnabled(false);
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         displayPanel.setBackground(new java.awt.Color(64, 64, 64));
         displayPanel.setMaximumSize(new java.awt.Dimension(92, 50));
         displayPanel.setMinimumSize(new java.awt.Dimension(92, 50));
         displayPanel.setPreferredSize(new java.awt.Dimension(92, 50));
 
         javax.swing.GroupLayout displayPanelLayout = new javax.swing.GroupLayout(displayPanel);
         displayPanel.setLayout(displayPanelLayout);
         displayPanelLayout.setHorizontalGroup(
             displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 92, Short.MAX_VALUE)
         );
         displayPanelLayout.setVerticalGroup(
             displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, Short.MAX_VALUE)
             .addComponent(displayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(displayPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         // TODO add your handling code here:
         if(channel.displayedPopertiesPanel == footageProperties)
         {
             channel.hidePropertiesPanel();
         }
         else
         {
             setEditable(true);
         }
         
         //channel.showPropertiesPanel(this);
         
     }//GEN-LAST:event_jButton1ActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel displayPanel;
     private javax.swing.JButton jButton1;
     // End of variables declaration//GEN-END:variables
 }
