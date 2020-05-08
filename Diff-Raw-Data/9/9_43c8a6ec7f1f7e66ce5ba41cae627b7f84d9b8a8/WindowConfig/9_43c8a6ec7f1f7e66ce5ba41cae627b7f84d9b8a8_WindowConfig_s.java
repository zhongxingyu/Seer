 package ca.uwaterloo.lkc;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Map;
 import java.util.Vector;
 
 import org.gnome.gdk.Event;
 import org.gnome.glade.Glade;
 import org.gnome.glade.XML;
 import org.gnome.gtk.Gtk;
 import org.gnome.gtk.ResponseType;
 import org.gnome.gtk.ToolButton;
 import org.gnome.gtk.Widget;
 import org.gnome.gtk.Window;
 
 import ca.uwaterloo.lkc.FeatureScreenHandler.Features;
 
 public class WindowConfig {
 	public final Window w;
 	public final ToolButton tbtnOpen;
 	public final ToolButton tbtnSave;
 	public final ToolButton tbtnUndo;
 	public final ToolButton tbtnRedo;
 	public final ToolButton tbtnAdvanced;
 
 	private FeatureScreenHandler fsh;
 	
 	public WindowConfig(final String gladeFile) throws FileNotFoundException {
 		this(gladeFile, null);
 	}
 
 	public WindowConfig(final String gladeFile, final URI configFile) throws FileNotFoundException {
 		final XML xmlWndConfig = Glade.parse(gladeFile, "wndConfig");
 
 		w = (Window) xmlWndConfig.getWidget("wndConfig");
 
 		w.connect(new Window.DeleteEvent() {
 
 			@Override
 			public boolean onDeleteEvent(Widget arg0, Event arg1) {
 				Gtk.mainQuit();
 				return false;
 			}
 		});
 		
 		fsh = new FeatureScreenHandler(xmlWndConfig);
 				
 		// Adding events for tool bar buttons
 		/* Open */
 		tbtnOpen = (ToolButton) xmlWndConfig.getWidget("tbtnOpen");
 		tbtnOpen.connect(new ToolButton.Clicked() {
 
 			@Override
 			public void onClicked(ToolButton source) {
 				try {
 					FcdChooseFile fcdChooseFile;
 					fcdChooseFile = new FcdChooseFile(gladeFile);
 					ResponseType responseType = fcdChooseFile.run();
 
 					/*
 					 * If a file is chosen, hide the current WindowConfig,
 					 * instantiate a new one with the new configuration.
 					 */
 					if (responseType == ResponseType.OK) {
 						w.hide();
 						fsh.load(fcdChooseFile.getURI());
 					} else if (responseType == ResponseType.CANCEL) {
 						fcdChooseFile.hide();
 					}
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 
 		/* Save */
 		tbtnSave = (ToolButton) xmlWndConfig.getWidget("tbtnSave");
 		tbtnSave.connect(new ToolButton.Clicked() {
 
 			@Override
 			public void onClicked(ToolButton source) {
 				try {
 					FcdChooseFile fcdChooseFile;
 					fcdChooseFile = new FcdChooseFile(gladeFile);
 					fcdChooseFile.setFileChooserActionSave();
 					ResponseType responseType = fcdChooseFile.run();
 
 					/*
 					 * If a file is chosen, save the current configuration.
 					 */
 					if (responseType == ResponseType.OK) {
 						fsh.save(fcdChooseFile.getURI());
 					} else if (responseType == ResponseType.CANCEL) {
 						fcdChooseFile.hide();
 					}
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		/* Advanced (i.e. calling xconfig) */
 		tbtnAdvanced = (ToolButton) xmlWndConfig.getWidget("tbtnAdvanced");
 		tbtnAdvanced.connect(new ToolButton.Clicked() {
 
 			@Override
 			public void onClicked(ToolButton source) {
 				try {
 					// Run xconfig
 				    ProcessBuilder pb = new ProcessBuilder("bash", "-c", "make xconfig");
 				    pb.start();
 				    w.hide();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		/* Undo */
 		tbtnUndo = (ToolButton) xmlWndConfig.getWidget("tbtnUndo");
 		tbtnUndo.connect(new ToolButton.Clicked() {
 
 			@Override
 			public void onClicked(ToolButton source) {
				currentFeaturesIndex--;
				if (currentFeaturesIndex <= 0) {
                     tbtnUndo.setSensitive(false);
                     tbtnRedo.setSensitive(true);
                 }
 			}
 		});
 		
 		/* Redo */
 		tbtnRedo = (ToolButton) xmlWndConfig.getWidget("tbtnRedo");
 		tbtnRedo.connect(new ToolButton.Clicked() {
 
 			@Override
 			public void onClicked(ToolButton source) {
				currentFeaturesIndex++;
				if (currentFeaturesIndex >= MAX_UNDO_REDO) {
                     tbtnUndo.setSensitive(true);
                     tbtnRedo.setSensitive(false);
                 }
 			}
 		});
 		
 	}
 	
     public void run()
     {
         w.show();
         fsh.showScreen();
     }
 
 }
