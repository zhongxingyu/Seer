 package es.icarto.gvsig.navtableforms.gui.tables;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JTable;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.icarto.gvsig.navtableforms.AbstractForm;
 import es.icarto.gvsig.navtableforms.gui.tables.menu.JTableContextualMenu;
 import es.icarto.gvsig.navtableforms.utils.FormFactory;
 import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
 
 public class JTableVectorialContextualMenu extends JTableContextualMenu {
 
     private FLyrVect layer;
     private JTable table;
     private FormFactory factory;
 
    public JTableVectorialContextualMenu(String layerName,
 FormFactory factory) {
 	this.layer = new TOCLayerManager().getLayerByName(layerName);
 	this.factory = factory;
 	initContextualMenu();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
 	table = (JTable) e.getComponent();
 	if ((e.getClickCount() == 2) && (table.getSelectedRow() > -1)) {
 	    openDialog();
 	} else if (e.getButton() == BUTTON_RIGHT) {
 	    if (!JTableUtils.hasRows(table)
 		    || (table.getSelectedRow() == NO_ROW_SELECTED)) {
 		updateMenuItem.setEnabled(false);
 	    } else {
 		updateMenuItem.setEnabled(true);
 	    }
 	    popupMenu.show(e.getComponent(), e.getX(), e.getY());
 	}
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     protected void initContextualMenu() {
 	updateMenuItem.addActionListener(new ActionListener() {
 	    @Override
 	    public void actionPerformed(ActionEvent arg0) {
 		openDialog();
 	    }
 	});
 	popupMenu.add(updateMenuItem);
     }
 
     protected void openDialog() {
 	AbstractForm form = factory.createForm(layer);
 	form.init();
	form.setPosition(table.convertRowIndexToModel(table.getSelectedRow()));
 	PluginServices.getMDIManager().addWindow(form);
     }
 
 }
