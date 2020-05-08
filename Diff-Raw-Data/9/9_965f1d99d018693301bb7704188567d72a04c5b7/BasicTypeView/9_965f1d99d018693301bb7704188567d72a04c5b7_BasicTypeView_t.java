 package edu.uwlax.toze.editor;
 
 import edu.uwlax.toze.domain.BasicTypeDef;
 
 import java.awt.*;
 
 public class BasicTypeView extends ParagraphView
 {
     private static final String BasicTypePre = "[";
     private static final String BasicTypePost = "]";
     //
     private final BasicTypeDef basicTypeDef;
     private TozeTextArea nameText;
 
     public BasicTypeView(BasicTypeDef basicTypeDef, SpecificationController specController)
     {
         super(specController);
 
         setLayout(new ParaLayout(this));
         this.basicTypeDef = basicTypeDef;
         requestRebuild();
     }
 
     @Override
     public BasicTypeDef getSpecObject()
     {
         return basicTypeDef;
     }
 
     @Override
     protected void rebuild()
     {
         removeAll();
 
         if (basicTypeDef.getName() != null)
             {
             nameText = buildTextArea(basicTypeDef, basicTypeDef.getName(), "name", true);
             add(nameText);
             }
     }
 
     @Override
     protected void updateErrors()
     {
         notNullUpdateError(basicTypeDef, nameText, "name");
     }
 
     @Override
     public void doLayout()
     {
         Insets insets = getInsets();
         Graphics g = getGraphics();
         FontMetrics fm = g.getFontMetrics();
         Dimension d;
 
         int x = insets.left + HMargin + fm.stringWidth(BasicTypePre);
         int y = insets.top + VMargin;
         d = nameText.getPreferredSize();
 
        nameText.setBounds(x, y, d.width, d.height);
     }
 
     @Override
     public Dimension getPreferredSize()
     {
         Graphics g = getGraphics();
         FontMetrics fm = g.getFontMetrics();
         Dimension d;
 
         d = nameText.getPreferredSize();
         int width = fm.stringWidth(BasicTypePre) + d.width + fm.stringWidth(BasicTypePost) + (HMargin * 2);
         int height = d.height + (VMargin * 2);
 
         return new Dimension(width, height);
     }
 
     @Override
     public Dimension getMinimumSize()
     {
         return getPreferredSize();
     }
 
     @Override
     public void paintComponent(Graphics g)
     {
         super.paintComponent(g);
 
         FontMetrics fm = g.getFontMetrics();
         int ystring = (fm.getHeight() - fm.getDescent());
 
         Dimension cd = nameText.getPreferredSize();
         g.drawString(BasicTypePre, HMargin, VMargin + ystring);
         g.drawString(BasicTypePost, HMargin + fm.stringWidth(BasicTypePre) + cd.width, VMargin + ystring);
     }
 }
