 package br.ufrj.dcc.compgraf.im.ui.options;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 
 import br.ufrj.dcc.compgraf.im.transform.Rotate;
 import br.ufrj.dcc.compgraf.im.transform.Rotate.Degree;
 import br.ufrj.dcc.compgraf.im.transform.Rotate.Direction;
 import br.ufrj.dcc.compgraf.im.ui.SwingUtils;
 import br.ufrj.dcc.compgraf.im.ui.UIContext;
 
 public class RotateOptionsDialog extends JDialog
 {
 
   public RotateOptionsDialog()
   {
     super(UIContext.instance().getMainWindow(), "Rotate", true);
 
     setSize(320, 180);
 
     SwingUtils.configureDialog(this);
 
     initLayout();
   }
 
   private void initLayout()
   {
     JPanel panel = new JPanel(new FlowLayout());
     panel.setOpaque(true);
     setContentPane(panel);
     
     JPanel quickPanel = new JPanel(new FlowLayout());
     JButton quickRotateCounterButton = new JButton("<-");
     quickRotateCounterButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         BufferedImage newImg = new Rotate().rotate(UIContext.instance().getCurrentImage(), Degree.ROTATE_90, Direction.COUNTER_CLOCKWISE);
         UIContext.instance().changeCurrentImage(newImg);
       }
     });
     JButton quickRotateButton = new JButton("->");
     quickRotateButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         BufferedImage newImg = new Rotate().rotate(UIContext.instance().getCurrentImage(), Degree.ROTATE_90, Direction.CLOCKWISE);
         UIContext.instance().changeCurrentImage(newImg);
       }
     });
     quickPanel.setBorder(BorderFactory.createTitledBorder("Quick"));
     quickPanel.add(quickRotateCounterButton);
     quickPanel.add(quickRotateButton);
     
     add(quickPanel);
     
     final JComboBox rotateCombo = new JComboBox();
     for (Degree degree: Degree.values())
     {
       rotateCombo.addItem(degree.descriptiveName());
     }
     final JComboBox directionCombo = new JComboBox();
     for (Direction dir : Direction.values())
     {
       directionCombo.addItem(dir.descriptiveName());
     }
     
     JPanel advPanel = new JPanel(new FlowLayout());
     JButton applyAdvButton = new JButton("Apply");
     applyAdvButton.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent e)
       {
         int selectedRotation = rotateCombo.getSelectedIndex();
         Degree degree = Degree.values()[selectedRotation];
 
        int selectedDirection = rotateCombo.getSelectedIndex();
         Direction dir = Direction.values()[selectedDirection];
         
         BufferedImage newImg = new Rotate().rotate(UIContext.instance().getCurrentImage(), degree, dir);
         UIContext.instance().changeCurrentImage(newImg);
         
         dispose();
       }
     });
     advPanel.setBorder(BorderFactory.createTitledBorder("Advanced"));
     advPanel.add(rotateCombo);
     advPanel.add(directionCombo);
     advPanel.add(applyAdvButton);
     
     add(advPanel);
   }
 }
