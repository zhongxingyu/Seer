 package ch.bli.mez.view;
 
 import java.awt.BorderLayout;
 import java.util.HashMap;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 
 public class PanelWithMap extends JPanel {
 	private static final long serialVersionUID = -8776121615582171474L;
 	private HashMap<String, JComponent> componentMap;
 	
 	
 	public PanelWithMap(){
 		this.componentMap = new HashMap<String, JComponent>();
 		setLayout(new BorderLayout(0, 0));
 	}
 	
 	public void putComponent(String name, JComponent component){
 		componentMap.put(name, component);
 	}
 	
 	public JComponent getComponentByName(String name){
 		return componentMap.get(name);
 	}
 }
