 package core;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.swing.JComponent;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 
 import manager.PluginManager;
 import plug.majorplugin.patatoe.PatatoePlugin;
 import plug.minorplugin.MinorPlugin;
 import plug.minorplugin.MinorPlugin.ActionType;
 
 public class Plateform {
 
 	private List<String> defaultList = new ArrayList<String>();
 	private Map<Object, Set<Object>> pluginStore;
 	private Object gui;
 	private String mode;
 	
 	/**
 	 * Constructor
 	 * Initialize the plateform with the list of plugin to load
 	 * @param init The setting file with the list of default plugin to load
 	 * @param modeParam The mode of the plateform
 	 */
 	public Plateform(String init, String modeParam){
 		mode = modeParam;
 		System.out.println(modeParam);
 		Properties prop = new Properties();
 		try {
 			prop.load(new FileReader(init));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		for(String plug : prop.getProperty("default").split(";")){
 			defaultList.add(plug);
 		}
 		PluginManager.getInstance().loadMajorPlugins(defaultList, this/*, modeParam*/);
 		
 		pluginStore = PluginManager.getInstance().getPluginStore();
 		
 		System.out.println("Loading all plugin from the plugin store");
 		for(Object major : pluginStore.keySet()){
 			PluginManager.getInstance().loadMinorsFromMajor(major, this);
 		}
 
 		//Allow to display the plugin Store and his elements
 		System.out.println(PluginManager.getInstance().displayPluginStore());
 		
 		if (modeParam.equals("gui")){
 			for(Object c : pluginStore.keySet()){
 				if (c.getClass().getName().equals("plug.majorplugin.generalgui.GeneralGuiPlugin")){
 					gui = c;
 				}
 			}
 			try {
 				// TODO A refractorer pour mettre l'instance de gui a part
 				Method addMajorPlugin =	gui.getClass().getMethod("displayButtonNewMajorPlugin", Object.class);
 				for(Object c : pluginStore.keySet()){
 					if (!c.getClass().getName().equals("plug.majorplugin.generalgui.GeneralGuiPlugin")){
 						try {
 							addMajorPlugin.invoke(gui, c);
 						} catch (IllegalArgumentException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (IllegalAccessException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (InvocationTargetException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Loading the major plugin and his minor
 	 * If the core is loading in gui: display the gui of major plugin (in parameter) with minor plugins in JToolBar
 	 * If the core is loading in console: display the minors ????????????????????? // TODO
 	 * @param majorPlugin The name of major plugin
 	 */
 	public void displayMajorPluginComplete(Object majorPlugin){
 		// TODO demander le chargement au Pluginmanager et utiliser l'instance retourn�e
 //		Object majorPlugin = pluginManager.getInstance()majorPlugin.loadMaj(nameMajor);
 		if (mode.equals("gui")){
 			try {
 				Method addAllMinorPlugin =	gui.getClass().getMethod("displayMinorsPlugins", Set.class, Object.class, String.class, JPanel.class, JMenuBar.class);
 				// The name of majorPlugin
 				Method getNameMajorPlugin = majorPlugin.getClass().getMethod("getName");
 				String nameMajorPlugin = (String) getNameMajorPlugin.invoke(majorPlugin);
 				for(Object c : pluginStore.keySet()){
 					if (c.getClass().getName().equals(majorPlugin.getClass().getName()) && !c.getClass().getName().equals("plug.majorplugin..generalgui.GeneralGuiPlugin")){
 						Method loadingGuiPlugin = c.getClass().getMethod("getPanel");
 						Method loadingMenuBar = c.getClass().getMethod("getMenuBar");
 						JPanel guiPlugin = (JPanel) loadingGuiPlugin.invoke(c);
 						JMenuBar guiPluginMenuBar = (JMenuBar) loadingMenuBar.invoke(c);
 						addAllMinorPlugin.invoke(gui, pluginStore.get(c), majorPlugin, nameMajorPlugin, guiPlugin, guiPluginMenuBar);
 					}
 				}
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void displayMinorPlugin(Object major, String namePlugin, Object minor){
 		// TODO Add method who create instance of minor
 		Method displayMinor;
 		Method actionTypeMethod;
 		try {	
 			actionTypeMethod = minor.getClass().getMethod("getActionType");
 			ActionType minorActionType = (ActionType) actionTypeMethod.invoke(minor);
 			
 			switch(minorActionType){
 				case DISPLAY:
 					Method displayMethod = minor.getClass().getMethod("display");
 					JComponent toaddComponent = (JComponent) displayMethod.invoke(minor);
 					displayMinor = major.getClass().getMethod("addComponent", JComponent.class);
 					displayMinor.invoke(major, toaddComponent);
 					break;
 				case EXECUTE:
 					Method executeMethod = minor.getClass().getMethod("exec");
 					executeMethod.invoke(minor);
 					break;
 			}
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	void getPlugins(){
 		PluginManager.getInstance().getPluginStore();
 	}
 
 	/**
 	 * Display the list of default plugin in the setting file
 	 */
 	void displayDefaultList(){
 		Iterator<String> i = defaultList.iterator();
 		while(i.hasNext()){
 		  System.out.println(i.next());
 		}
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String mode;
 		if (args.length == 0){
 			mode = "gui";
 		}
 		else{
 			mode = args[0];
 		}
 		Plateform p = new Plateform("src/core/init.txt", mode);
 	}
 }
