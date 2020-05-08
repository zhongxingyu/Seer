 package ch.bli.mez.view;
 
 import java.awt.BorderLayout;
 import java.util.HashMap;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
<<<<<<< HEAD
 import java.awt.BorderLayout;
=======
>>>>>>> 90179577e72537cbe0ed654aa5b1f4150768984f
 
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
