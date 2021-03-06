 package net.sf.freecol.client.gui.action;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.sf.freecol.client.FreeColClient;
 import net.sf.freecol.client.gui.Canvas;
 import net.sf.freecol.client.gui.i18n.Messages;
 import net.sf.freecol.client.gui.panel.FreeColDialog;
 import net.sf.freecol.common.model.Game;
 import net.sf.freecol.common.model.Map;
 import net.sf.freecol.common.model.Tile;
 import net.sf.freecol.common.model.Map.Position;
 
 /**
  * An action for scaling a map. This action is a part of the map editor.
  */
 public class ScaleMapAction extends FreeColAction {
     @SuppressWarnings("unused")
     private static final Logger logger = Logger.getLogger(ScaleMapAction.class.getName());
 
     public static final String COPYRIGHT = "Copyright (C) 2003-2005 The FreeCol Team";
 
     public static final String LICENSE = "http://www.gnu.org/licenses/gpl.html";
 
     public static final String REVISION = "$Revision$";
 
     public static final String ID = "scaleMapAction";
 
 
     /**
      * Creates a new <code>ScaleMapAction</code>.
      * 
      * @param freeColClient The main controller object for the client.
      */
     ScaleMapAction(FreeColClient freeColClient) {
         super(freeColClient, "menuBar.tools.scaleMap", null, 0, null);
     }
 
     /**
      * Returns the id of this <code>Option</code>.
      * 
      * @return "scaleMapAction"
      */
     public String getId() {
         return ID;
     }
     
     /**
      * Checks if this action should be enabled.
      * 
      * @return <code>false</code> if there is no active map.
      */
     @Override
     protected boolean shouldBeEnabled() {
         return super.shouldBeEnabled()
                 && freeColClient.isMapEditor()
                 && freeColClient.getGame() != null
                 && freeColClient.getGame().getMap() != null; 
     }
     
     /**
      * Applies this action.
      * 
      * @param e The <code>ActionEvent</code>.
      */
     public void actionPerformed(ActionEvent e) {
         final Game game = freeColClient.getGame();
         final Map oldMap = game.getMap();
 
         final int oldWidth = oldMap.getWidth();
         final int oldHeight = oldMap.getHeight();
 
         MapSize ms = showMapSizeDialog();
         if (ms != null) {
             scaleMapTo(ms.width, ms.height);
         }
     }
     
     /**
      * Displays a dialog for choosing the new map size.
      * @return The size of the new map.
      */
     private MapSize showMapSizeDialog() {
         /*
          * TODO: Extend this dialog. It should be possible
          *       to specify the sizes using percentages.
          *       
          *       Add a panel containing information about
          *       the scaling (old size, new size etc).
          */
         final int COLUMNS = 5;
 
         final Game game = freeColClient.getGame();
         final Map oldMap = game.getMap();
         
         final Canvas canvas = getFreeColClient().getCanvas();
         final String okText = Messages.message("ok");
         final String cancelText = Messages.message("cancel");
         final String widthText = Messages.message("width");
         final String heightText = Messages.message("height");
         
         final JTextField inputWidth = new JTextField(Integer.toString(oldMap.getWidth()), COLUMNS);
         final JTextField inputHeight = new JTextField(Integer.toString(oldMap.getHeight()), COLUMNS);
 
         final FreeColDialog inputDialog = new FreeColDialog()  {
             public void requestFocus() {
                 inputWidth.requestFocus();
             }
         };
 
         inputDialog.setLayout(new BoxLayout(inputDialog, BoxLayout.Y_AXIS));
 
         JPanel buttons = new JPanel();
         buttons.setOpaque(false);
 
         final ActionListener al = new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 try {
                     int width = Integer.parseInt(inputWidth.getText());
                     int height = Integer.parseInt(inputHeight.getText());
                     if (width <= 0 || height <= 0) {
                         throw new NumberFormatException();
                     }
                     inputDialog.setResponse(new MapSize(width, height));
                 } catch (NumberFormatException nfe) {
                     canvas.errorMessage("integerAboveZero");
                 }
             }
         };
         JButton okButton = new JButton(okText);
         buttons.add(okButton);
         
         JButton cancelButton = new JButton(cancelText);
         cancelButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 inputDialog.setResponse(null);
             }
         });
         buttons.add(cancelButton);
         inputDialog.setCancelComponent(cancelButton);
         
         okButton.addActionListener(al);
         inputWidth.addActionListener(al);
         inputHeight.addActionListener(al);
         
         JLabel widthLabel = new JLabel(widthText);
         widthLabel.setLabelFor(inputWidth);
         JLabel heightLabel = new JLabel(heightText);
         heightLabel.setLabelFor(inputHeight);
         
         JPanel widthPanel = new JPanel(new FlowLayout());
         widthPanel.setOpaque(false);
         widthPanel.add(widthLabel);
         widthPanel.add(inputWidth);
         JPanel heightPanel = new JPanel(new FlowLayout());
         heightPanel.setOpaque(false);
         heightPanel.add(heightLabel);
         heightPanel.add(inputHeight);       
         
         inputDialog.add(widthPanel);
         inputDialog.add(heightPanel);
         inputDialog.add(buttons);
 
         inputDialog.setSize(inputDialog.getPreferredSize());
 
         return (MapSize) canvas.showFreeColDialog(inputDialog);
     }
     
     private class MapSize {
         int width;
         int height;
         
         MapSize(int width, int height) {
             this.width = width;
             this.height = height;
         }
     }
     
     /**
      * Scales the current map into the specified size. The current
      * map is given by {@link FreeColClient#getGame()#getMap}.   
      * 
      * @param width The width of the resulting map.
      * @param height The height of the resulting map.
      */
     private void scaleMapTo(final int width, final int height) {
         /*
          * This implementation uses a simple linear scaling, and
          * the isometric shape is not taken into account.
          * 
          * TODO: Find a better method for choosing a group of
          *       adjacent tiles. This group can then be merged into
          *       a common tile by using the average value (for
          *       example: are there a majority of ocean tiles?).
          */
         
         final Game game = freeColClient.getGame();
         final Map oldMap = game.getMap();
 
         final int oldWidth = oldMap.getWidth();
         final int oldHeight = oldMap.getHeight();
         
         Vector<Vector<Tile>> columns = new Vector<Vector<Tile>>(width);
         for (int i = 0; i < width; i++) {
             Vector<Tile> v = new Vector<Tile>(height);
             for (int j = 0; j < height; j++) {
                 final int oldX = (i * oldWidth) / width;
                 final int oldY = (j * oldHeight) / height;
                 /*
                  * TODO: This tile should be based on the average as
                  *       mentioned at the top of this method.
                  */
                 Tile oldTile = oldMap.getTile(oldX, oldY);
                 
                 // Copy values to the new tile:
                 Tile t = new Tile(game, oldTile.getType(), i, j);
                t.getTileItemContainer().copyFrom(oldTile.getTileItemContainer());
                 v.add(t);
             }
             columns.add(v);
         }
 
         Map map = new Map(game, columns);
         game.setMap(map);
         
         // Update river directions
         for (Tile t : map.getAllTiles()) {
            t.getTileItemContainer().updateRiver();
         }
         
         freeColClient.getGUI().setSelectedTile(new Position(0, 0));
         freeColClient.getCanvas().refresh();
     }
 }
