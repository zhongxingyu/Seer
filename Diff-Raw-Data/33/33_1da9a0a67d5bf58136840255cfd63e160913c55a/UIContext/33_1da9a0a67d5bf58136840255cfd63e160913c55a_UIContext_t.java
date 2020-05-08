 package br.ufrj.dcc.compgraf.im.ui;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
import java.util.Stack;
 
 import javax.swing.ImageIcon;
 import javax.swing.JScrollPane;
 
 import br.ufrj.dcc.compgraf.im.crop.CropContext;
 import br.ufrj.dcc.compgraf.im.ui.swing.ext.ScrollablePicture;
 
 public class UIContext
 {
   private static UIContext instance;
 
   private BufferedImage currentImage;
   private JScrollPane imageScrollPane;
   private MainWindow mainWindow;
  private Stack<BufferedImage> images = new Stack<BufferedImage>();
   
   private UIContext() {}
   
   public static UIContext instance()
   {
     if (instance == null) 
     {
       instance = new UIContext();
     }
     
     return instance;
   }
   
   public BufferedImage getCurrentImage()
   {
     return currentImage;
   }
 
   protected void setImageScrollPane(JScrollPane imageScrollPane)
   {
     this.imageScrollPane = imageScrollPane;
   }
   
   protected JScrollPane getImageScrollPane()
   {
     return imageScrollPane;
   }
 
   public void changeCurrentImage(BufferedImage image)
   {
     currentImage = image;
     
     ImageIcon icon = new ImageIcon(image);
     ScrollablePicture scrlImg = new ScrollablePicture(icon) {
       protected void paintComponent(java.awt.Graphics g) {
         super.paintComponent(g);
         
         CropContext cc = CropContext.instance();
         
         if (cc.isCropRunning() && cc.getClickedPixel() != null) {
           g.setColor(Color.BLACK);
           
           int x = (int) cc.getClickedPixel().getX();
           int y = (int) cc.getClickedPixel().getY();
           int width = (int) cc.getCurrentPixel().getX() - x;
           int heigth = (int) (int) cc.getCurrentPixel().getY() - y;
               
           g.drawRect(x, y, width, heigth);
         }
       };
     };
     
     imageScrollPane.setViewportView(scrlImg);
   }
 
   public MainWindow getMainWindow()
   {
     return mainWindow;
   }
 
   protected void setMainWindow(MainWindow mainWindow)
   {
     this.mainWindow = mainWindow;
   }
 }
