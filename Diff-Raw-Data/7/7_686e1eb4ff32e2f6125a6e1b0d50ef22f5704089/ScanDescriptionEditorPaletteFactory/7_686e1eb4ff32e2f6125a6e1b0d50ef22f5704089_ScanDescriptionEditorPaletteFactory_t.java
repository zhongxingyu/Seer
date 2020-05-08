 package de.ptb.epics.eve.editor.gef;
 
 import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
 import org.eclipse.gef.palette.ConnectionCreationToolEntry;
 import org.eclipse.gef.palette.MarqueeToolEntry;
 import org.eclipse.gef.palette.PaletteDrawer;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.palette.PaletteToolbar;
 import org.eclipse.gef.palette.PanningSelectionToolEntry;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.tools.AbstractTool;
 
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.editor.Activator;
 
 /**
  * @author Marcus Michalsky
  * @since 1.6
  */
 public class ScanDescriptionEditorPaletteFactory {
 	
 	/**
 	 * Returns the palette root
 	 * 
 	 * @return the palette root
 	 */
 	public static PaletteRoot createPalette() {
 		PaletteRoot palette = new PaletteRoot();
 		palette.add(createToolsGroup(palette));
 		palette.add(createElementsDrawer());
 		return palette;
 	}
 	
 	/*
 	 * 
 	 */
 	private static PaletteEntry createToolsGroup(PaletteRoot palette) {
 		PaletteToolbar toolbar = new PaletteToolbar("Tools");
 		
 		ToolEntry tool = new PanningSelectionToolEntry();
 		toolbar.add(tool);
 		palette.setDefaultEntry(tool);
 		
		tool = new MarqueeToolEntry();
		tool.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED,
				Boolean.TRUE); 
		toolbar.add(tool);
 		
 		ConnectionCreationToolEntry connection = new ConnectionCreationToolEntry(
 				"Connection", "Create a connection", null, 
 				Activator.imageDescriptorFromPlugin(
 						Activator.PLUGIN_ID, "icons/entry_ovr.gif"), 
 				Activator.imageDescriptorFromPlugin(
 						Activator.PLUGIN_ID, "icons/entry_ovr.gif"));
 		toolbar.add(connection);
 		
 		return toolbar;
 	}
 	
 	/*
 	 * 
 	 */
 	private static PaletteEntry createElementsDrawer() {
 		PaletteDrawer componentsDrawer = new PaletteDrawer("Create Tools");
 		CombinedTemplateCreationEntry component = new CombinedTemplateCreationEntry(
 				"Scan Module", "Create a Scan Module", new SimpleFactory(
 						ScanModule.class), Activator.imageDescriptorFromPlugin(
 						Activator.PLUGIN_ID, "icons/devices/scanmodule.gif"),
 				Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/devices/scanmodule24.gif"));
 		componentsDrawer.add(component);
 		return componentsDrawer;
 	}
 }
