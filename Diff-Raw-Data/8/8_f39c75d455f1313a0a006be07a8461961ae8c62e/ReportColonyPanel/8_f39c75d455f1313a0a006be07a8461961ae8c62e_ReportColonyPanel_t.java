 package net.sf.freecol.client.gui.panel;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 import net.sf.freecol.client.gui.Canvas;
 import net.sf.freecol.client.gui.i18n.Messages;
 import net.sf.freecol.common.model.Building;
 import net.sf.freecol.common.model.Colony;
 import net.sf.freecol.common.model.Goods;
 import net.sf.freecol.common.model.Player;
 import net.sf.freecol.common.model.Unit;
 import cz.autel.dmi.HIGLayout;
 
 /**
  * This panel displays the Colony Report.
  */
 public final class ReportColonyPanel extends ReportPanel implements ActionListener {
     public static final String COPYRIGHT = "Copyright (C) 2003-2006 The FreeCol Team";
 
     public static final String LICENSE = "http://www.gnu.org/licenses/gpl.html";
 
     public static final String REVISION = "$Revision$";
 
     private List<Colony> colonies;
 
     private final int ROWS_PER_COLONY = 4;
 
     /**
      * The constructor that will add the items to this panel.
      * 
      * @param parent The parent of this panel.
      */
     public ReportColonyPanel(Canvas parent) {
         super(parent, Messages.message("menuBar.report.colony"));
     }
 
     /**
      * Prepares this panel to be displayed.
      */
     @Override
     public void initialize() {
         Player player = getCanvas().getClient().getMyPlayer();
         colonies = player.getColonies();
 
         // Display Panel
         
         int widths[] = new int[] {0};
        // If no colonies are defined, show an empty panel.
        int heights[] = null;
        if (colonies.size() == 0) {
        	heights = new int[0];
        } else {
        	heights = new int[colonies.size() * 2 - 1];
        }
         for (int i = 1; i < heights.length; i += 2) {
             heights[i] = 12;
         }
         reportPanel.setLayout(new HIGLayout(widths, heights));
 
         widths = new int[] {0};
         heights = new int[2 * ROWS_PER_COLONY - 1];
         for (int i = 1; i < heights.length; i += 2) {
             heights[i] = 5;
         }
 
         int panelColumn = 1;
         int colonyRow = 1;
 
         Border colonyBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINK_COLOR),
                                                                  BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
         Collections.sort(colonies, getCanvas().getClient().getClientOptions().getColonyComparator());
         for (int colonyIndex = 0; colonyIndex < colonies.size(); colonyIndex++) {
             int row = 1;
 
             Colony colony = colonies.get(colonyIndex);
             JPanel colonyPanel = new JPanel(new HIGLayout(widths, heights));
             colonyPanel.setBorder(colonyBorder);
 
             colonyPanel.add(createColonyButton(colonyIndex), higConst.rc(row, panelColumn, "l"));
             row += 2;
 
             colonyPanel.add(createUnitPanel(colony), higConst.rc(row, panelColumn));
             row += 2;
 
             colonyPanel.add(createProductionPanel(colony), higConst.rc(row, panelColumn));
             row += 2;
 
             colonyPanel.add(createBuildingPanel(colony), higConst.rc(row, panelColumn));
 
             reportPanel.add(colonyPanel, higConst.rc(colonyRow, panelColumn));
             colonyRow += 2;
         }
 
     }
 
 
     private JPanel createUnitPanel(Colony colony) { 
         JPanel unitPanel = new JPanel(new GridLayout(0, 12));
         unitPanel.setOpaque(false);
         List<Unit> unitList = colony.getUnitList();
         Collections.sort(unitList, getUnitTypeComparator());
         for(Unit unit : unitList) {
             UnitLabel unitLabel = new UnitLabel(unit, getCanvas(), true, true);
             unitPanel.add(unitLabel);
         }
         return unitPanel;
     }
 
     private JPanel createProductionPanel(Colony colony) {
         JPanel goodsPanel = new JPanel(new GridLayout(0, 10));
         goodsPanel.setOpaque(false);
         for (int goodsType = 0; goodsType < Goods.NUMBER_OF_ALL_TYPES; goodsType++) {
             int newValue = colony.getProductionOf(goodsType);
             if (newValue > 0) {
                 Goods goods = new Goods(colony.getGame(), colony, goodsType, newValue);
                 // goods.setAmount(newValue);
                 GoodsLabel goodsLabel = new GoodsLabel(goods, getCanvas());
                 goodsLabel.setHorizontalAlignment(JLabel.LEADING);
                 goodsPanel.add(goodsLabel);
             }
         }
         return goodsPanel;
     }
 
     private JPanel createBuildingPanel(Colony colony) {
         JPanel buildingPanel = new JPanel(new GridLayout(0, 5, 12, 0));
         buildingPanel.setOpaque(false);
         int currentType = colony.getCurrentlyBuilding();
         for (int buildingType = 0; buildingType < Building.NUMBER_OF_TYPES; buildingType++) {
             Building building = colony.getBuilding(buildingType);
             if (building.getLevel() != Building.NOT_BUILT) {
                 buildingPanel.add(new JLabel(building.getName()));
             }
             if (buildingType == currentType) {
                 JLabel buildingLabel = new JLabel(building.getNextName());
                 buildingLabel.setForeground(Color.GRAY);
                 buildingPanel.add(buildingLabel);
             }
         }
         if (currentType >= Colony.BUILDING_UNIT_ADDITION) {
             JLabel unitLabel = new JLabel(Unit.getName(currentType - Colony.BUILDING_UNIT_ADDITION));
             unitLabel.setForeground(Color.GRAY);
             buildingPanel.add(unitLabel);
         }
         return buildingPanel;
     }
 
     private JButton createColonyButton(int index) {
 
         JButton button = new JButton(colonies.get(index).getName());
         button.setFont(smallHeaderFont);
         button.setMargin(new Insets(0,0,0,0));
         button.setOpaque(false);
         button.setForeground(LINK_COLOR);
         button.setAlignmentY(0.8f);
         button.setBorder(BorderFactory.createEmptyBorder());
         button.setActionCommand(String.valueOf(index));
         button.addActionListener(this);
         return button;
     }
 
     /**
      * This function analyses an event and calls the right methods to take care
      * of the user's requests.
      * 
      * @param event The incoming ActionEvent.
      */
     @Override
     public void actionPerformed(ActionEvent event) {
         String command = event.getActionCommand();
         int action = Integer.valueOf(command).intValue();
         if (action == OK) {
             super.actionPerformed(event);
         } else {
             getCanvas().showColonyPanel(colonies.get(action));
         }
     }
 
 }
