 package GUI;
 
 import java.awt.*;
 import java.io.IOException;
 
 import javax.swing.*;
 
 public class ModuleGUI extends JPanel
 {
 	/**
 	 * God knows what this is, but eclipse wants it
 	 */
 	private static final long serialVersionUID = 4170785623135839381L;
 
 	// FIXME: There has to be an inbuilt-constant for this
 	public Engine.Module module;
 	protected ModuleAL AL;
 	protected String name;
 	public PortGUI[] input_ports;
 	public PortGUI[] output_ports;
 	
 	public ModuleGUI(ContainerWindow main_window, Engine.Module module) throws IOException
 	{
 		name = module.MODULE_NAME + " " + module.get_index();
 		
 		this.module = module;
 		
 		AL = new ModuleAL(this);
 		addMouseMotionListener(AL);
 	}
 	
 	public ModuleAL getAL()
 	{
 		return AL;
 	}
 }
