 package br.ufrj.dcc.compgraf.im.ui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import br.ufrj.dcc.compgraf.im.crop.CropContext;
 import br.ufrj.dcc.compgraf.im.crop.CropMouseAdapter;
 import br.ufrj.dcc.compgraf.im.ui.options.FlipOptionsDialog;
 import br.ufrj.dcc.compgraf.im.ui.options.GreyScaleOptionsDialog;
 import br.ufrj.dcc.compgraf.im.ui.options.ResizeOptionsDialog;
 import br.ufrj.dcc.compgraf.im.ui.options.RotateOptionsDialog;
 import br.ufrj.dcc.compgraf.im.ui.swing.ext.ScrollablePicture;
 
 public class ToolboxPanel extends JPanel
 {
 
   public ToolboxPanel()
   {
     super();
     
     setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
     
     setSize(20, 550);
     
     JButton resizeButton = new JButton("Resize");
     resizeButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         new ResizeOptionsDialog().setVisible(true);
       }
     });
     
     JButton greyScaleButton = new JButton("Greyscale");
     greyScaleButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         new GreyScaleOptionsDialog().setVisible(true);
       }
     });
     
     JButton flipButton = new JButton("Flip");
     flipButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         new FlipOptionsDialog().setVisible(true);
       }
     });
     
     JButton cropButton = new JButton("Crop");
     cropButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         ScrollablePicture pic = (ScrollablePicture) UIContext.instance().getImageScrollPane().getViewport().getView();
         
         CropMouseAdapter cma = CropMouseAdapter.instance();
         CropContext cc = CropContext.instance();
         
         if (!cc.isCropRunning())
         {
           pic.addMouseListener(cma);
           pic.addMouseMotionListener(cma);
           cc.setCropRunning(true);
         }
         else
         {
           pic.removeMouseListener(cma);
           pic.removeMouseMotionListener(cma);
           
           cc.setCropRunning(false);
           
           UIContext.instance().getImageScrollPane().repaint();
         }
       }
     });
     
     JButton rotateButton = new JButton("Rotate");
     rotateButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         new RotateOptionsDialog().setVisible(true);
       }
     });
     
    JButton undoButton = new JButton("Undo");
    undoButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
      }
    });
    
     add(resizeButton);
     add(Box.createRigidArea(new Dimension(0, 10)));
     add(cropButton);
     add(Box.createRigidArea(new Dimension(0, 10)));
     add(greyScaleButton);
     add(Box.createRigidArea(new Dimension(0, 10)));
     add(flipButton);
     add(Box.createRigidArea(new Dimension(0, 10)));
     add(rotateButton);
   }
   
 }
