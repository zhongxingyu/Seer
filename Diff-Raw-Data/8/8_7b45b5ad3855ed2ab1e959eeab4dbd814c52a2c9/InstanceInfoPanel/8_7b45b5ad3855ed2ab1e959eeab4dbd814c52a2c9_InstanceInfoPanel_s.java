 package de.uni_leipzig.simba.saim.gui.widget.panel;
 
 
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 
 import de.uni_leipzig.simba.data.Instance;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.core.Configuration;
 /**
  * Panel to display information available for two instances.
  * @author Lyko
  *
  */
 public class InstanceInfoPanel extends Panel {
 	private static final long serialVersionUID = 1L;
 	public static final String TableWidth= "30em";
 	public static final String PanelWidth= "62em";
 	/** Default Constructor 
 	 * @param i1 the source instance
 	 * @param i2 the target instance
 	 */
 	public InstanceInfoPanel(Instance i1, Instance i2) {
 		super();
 		HorizontalLayout mainLayout = new HorizontalLayout();
 		this.setContent(mainLayout);
 		
 
 		KBInfo source = ((SAIMApplication)getApplication()).getConfig().getSource();
 		KBInfo target = ((SAIMApplication)getApplication()).getConfig().getTarget();
 		
 		VerticalLayout vl1 = new VerticalLayout();
 		VerticalLayout vl2 = new VerticalLayout();
 		vl1.setCaption("Source Instance of "+source.id);
 		vl2.setCaption("Target Instance of "+target.id);
 		Table t1 = getTable(i1);
 		Table t2 = getTable(i2);
 		vl1.addComponent(t1);
 		vl2.addComponent(t2);
 		
 		mainLayout.addComponent(vl1);
 		mainLayout.addComponent(vl2);
 		this.setWidth(PanelWidth);
 	}
 	
 	/** Generates a table with two columns: the property name and the value of the property
 	 * @param i Instance to display.
 	 * @return generated table.
 	 */
 	private Table getTable(Instance i) {
 		Table t = new Table();
 		t.setWidth(TableWidth);
 		t.addContainerProperty("Property", String.class, "");
 		t.addContainerProperty("Value", String.class, "");
 		int id=0;
 		for(String prop : i.getAllProperties()) {
 			String value = "";
 			for(String s : i.getProperty(prop)) {
 				value+= s+" ";
 			}
 			t.addItem(new Object[]{prop, value}, new Integer(id));
 			id++;
 		}
 		t.setHeight("10em");
 		return t;
 	}
 }
