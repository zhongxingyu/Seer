 /**
  * @author Michael Zhou (Dominator008)
  */
 package leveleditor;
 
 import io.SpriteWrapper;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import leveleditor.eventhandlers.MoveSpriteListener;
 import leveleditor.eventhandlers.SpriteCreateTransferHandler;
 import leveleditor.eventhandlers.SpriteDropTargetListener;
 import core.characters.GameElement;
 
 @SuppressWarnings("serial")
 public class Canvas extends JScrollPane {
     
     private Map<JLabel, SpriteWrapper> myLabelWrapperMap;
     private String myBackGroundImgSrc;
     private JLabel myBackGroundLabel;
     private JPanel myCanvasPane;
     private LevelEditor myView;
 
     public Canvas(LevelEditor view, GridBagConstraints constraint) {
 	setPreferredSize(new Dimension(800, 600));
 	myView = view;
 	myView.add(this, constraint, 0);
 	myCanvasPane = new JPanel();
 	myCanvasPane.setLayout(null);
 	myCanvasPane.setBackground(Color.BLACK);
 	Border border = BorderFactory.createTitledBorder(
 		BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
 		"Canvas");
 	setBorder(border);
 	getViewport().add(myCanvasPane);
 	myLabelWrapperMap = new HashMap<JLabel, SpriteWrapper>();
     }
 
     protected void setUpBackground(String imagesrc) {
 	if (myBackGroundLabel != null) {
 	    myBackGroundLabel.setVisible(false);
 	    myCanvasPane.remove(myBackGroundLabel);
 	    myCanvasPane.revalidate();
 	} else
 	    myBackGroundLabel = new JLabel();
 	ImageIcon icon = new ImageIcon(imagesrc);
 	myBackGroundLabel = new JLabel(icon);
 	myCanvasPane.setPreferredSize(new Dimension(icon.getIconWidth(), icon
 		.getIconHeight()));
 	new DropTarget(myCanvasPane, DnDConstants.ACTION_COPY_OR_MOVE,
 		new SpriteDropTargetListener(myView));
 	myCanvasPane.setLocation(0, 0);
 	myCanvasPane.add(myBackGroundLabel);
 	myBackGroundLabel.setBounds(0, 0, icon.getIconWidth(),
 		icon.getIconHeight());
 	myBackGroundImgSrc = imagesrc;
 	myCanvasPane.revalidate();
     }
     
     public void deleteSpriteLabel(JLabel l) {
 	l.setVisible(false);
 	myCanvasPane.remove(l);
 	myCanvasPane.revalidate();
 	myCanvasPane.repaint();
 	myLabelWrapperMap.remove(l);
     }
     
     public Map<JLabel, SpriteWrapper> getLabelWrapperMap() {
 	return myLabelWrapperMap;
     }
     
     protected String getBackgroundImageSrc() {
 	return myBackGroundImgSrc;
     }
     
     protected void loadSprites(List<SpriteWrapper> sprites) {
 	if (myLabelWrapperMap != null) {
 	    for (JLabel l: myLabelWrapperMap.keySet()) {
 		l.setVisible(false);
 		myCanvasPane.remove(l);
 	    }
 	    myLabelWrapperMap.clear();
 	    myCanvasPane.revalidate();
 	}
 	else myLabelWrapperMap = new HashMap<JLabel, SpriteWrapper>();
 	for (SpriteWrapper sw: sprites) {
 	    GameElement sp = sw.getGameElement();
	    System.out.println(sp + " " + sp.getX());
 	    BufferedImage currentImage = sp.getImage();
 	    JLabel label = new JLabel();
 	    ImageIcon icon = new ImageIcon(currentImage);
 	    label.setIcon(icon);
 	    label.setTransferHandler(new SpriteCreateTransferHandler(myView));
 	    label.addMouseListener(new MoveSpriteListener(myView));
 	    label.addMouseMotionListener(new MoveSpriteListener(myView));
 	    myCanvasPane.add(label, 0);
 	    label.setBounds((int) sp.getX(), (int) sp.getY(),
 		   currentImage.getWidth(), currentImage.getHeight());
 	    myLabelWrapperMap.put(label, sw);
 	}
     }
     
     public JPanel getCanvasPane() {
 	return myCanvasPane;
     }
 }
