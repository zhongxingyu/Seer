 package signature.display;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 import signature.ColoredTree;
 
 public class ColoredTreePanel extends JPanel {
     
	private static final long serialVersionUID = 2144237936253828531L;

	public int maxDepth;
     
     private DisplayableColoredTree displayTree;
     
     private boolean drawKey;
     
     public ColoredTreePanel(int width, int height) {
         this.setPreferredSize(new Dimension(width, height));
         this.drawKey = false;
         this.displayTree = new DisplayableColoredTree(width, height);
     }
     
     public ColoredTreePanel(ColoredTree tree, int width, int height) {
         displayTree = new DisplayableColoredTree(tree, width, height);
         this.drawKey = false;
         this.setPreferredSize(new Dimension(width, height));
     }
     
     public void setDrawKey(boolean drawKey) {
         this.drawKey = drawKey; 
     }
     
     public void setTree(ColoredTree tree) {
         System.out.println("setting tree " + tree);
         if (displayTree == null) {
             displayTree = new DisplayableColoredTree(getWidth(), getHeight());
         } else {
             displayTree.makeFromColoredTree(tree);
         }
         this.displayTree.setDrawKey(drawKey);
         this.maxDepth = tree.getHeight();
     }
     
     public void paint(Graphics g) {
         if (displayTree != null) {
             this.displayTree.paint(g);
         }
     }
 
     public void clear() {
         this.displayTree = null;
     }
 
 }
