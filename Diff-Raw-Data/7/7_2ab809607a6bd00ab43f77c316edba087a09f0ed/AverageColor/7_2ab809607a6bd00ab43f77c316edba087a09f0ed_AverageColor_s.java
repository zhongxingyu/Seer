 package leocarbon.cu.modules;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FileDialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.colorchooser.AbstractColorChooserPanel;
 import static leocarbon.cu.ColorUtility.RB;
 import static leocarbon.cu.ColorUtility.cc;
 import org.apache.log4j.Logger;
 
 public class AverageColor extends AbstractColorChooserPanel implements ActionListener {
     BufferedImage img;
     long redBucket = 0;
     long greenBucket = 0;
     long blueBucket = 0;
     long pixelCount = 0;
     public static AverageColor ac;
             
     public final JButton averageStart = new JButton(RB.getString("AC.start"));
     
     public AverageColor() {
         averageStart.addActionListener(this);
         averageStart.setActionCommand("A");
         add(averageStart,BorderLayout.CENTER);
     }
     
     public Color getMedian() {
         for (int y = 0; y < img.getHeight(); y++){
             for (int x = 0; x < img.getWidth(); x++){
                 int color = img.getRGB(x, y);
                 int b = (color)&0xFF;
                 int g = (color>>8)&0xFF;
                 int r = (color>>16)&0xFF;
                 Color concolor = new Color(r,g,b);
                 pixelCount++;
                 redBucket += concolor.getRed();
                 greenBucket += concolor.getGreen();
                 blueBucket += concolor.getBlue();
             }
         }
         int r = (int)(redBucket/pixelCount);
         int g = (int)(greenBucket/pixelCount);
         int b = (int)(blueBucket/pixelCount);
         pixelCount = 0;
         redBucket = 0;
         greenBucket = 0;
         blueBucket = 0;
         return new Color(r ,g ,b );
     }
     public Color getMostFrequent() {
         return null;
     }
     
     protected BufferedImage getImage() {
         FileDialog chooser = new FileDialog(new JFrame(), RB.getString("AC.FileDialog.title"), FileDialog.LOAD);
         chooser.setFile("*.jpg; *.bmp; *.jpeg; *.wbmp; *.png; *.gif");
         chooser.setVisible(true);
         String path = chooser.getFile();
         if (path == null) Logger.getLogger(AverageColor.class.getName()).trace("No image was selected.");
         
         File f = new File(chooser.getDirectory()+"/"+chooser.getFile());
         try {
             img = ImageIO.read(f);
         } catch (IOException IOE) {
             Logger.getLogger(AverageColor.class.getName()).trace(IOE);
             Logger.getLogger(AverageColor.class.getName()).trace("ImageIO couldn't load the image.");
         }
         return img;
     }
     
     @Override
     public void updateChooser() {}
 
     @Override
     protected void buildChooser() {
         ac = new AverageColor();
     }
 
     @Override
     public String getDisplayName() {
         return RB.getString("AC");
     }
 
     @Override
     public Icon getSmallDisplayIcon() {
         return null;
     }
 
     @Override
     public Icon getLargeDisplayIcon() {
         return null;
     }
 
     @Override
     public void actionPerformed(ActionEvent AE) {
        if(AE.getActionCommand().equals("A"))
        img = getImage();
        cc.getSelectionModel().setSelectedColor(getMedian());
     }
 }
