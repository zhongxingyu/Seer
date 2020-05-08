 package chalmers.dax021308.ecosystem.controller.mapeditor;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import chalmers.dax021308.ecosystem.controller.IController;
 import chalmers.dax021308.ecosystem.controller.mapeditor.ChangeNameDialogController.OnChangeNameSelectedListener;
 import chalmers.dax021308.ecosystem.controller.mapeditor.ChooseMapDialogController.OnLoadedMapSelectedListener;
 import chalmers.dax021308.ecosystem.controller.mapeditor.NewMapDialogController.OnNameSelectedListener;
 import chalmers.dax021308.ecosystem.model.environment.IModel;
 import chalmers.dax021308.ecosystem.model.environment.mapeditor.MapEditorModel;
 import chalmers.dax021308.ecosystem.model.environment.mapeditor.MapFileHandler;
 import chalmers.dax021308.ecosystem.model.environment.mapeditor.SimulationMap;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
 import chalmers.dax021308.ecosystem.view.mapeditor.MapEditorView;
 
 
 /**
  * Controller class for the Map Editor system.
  * 
  * @author Erik
  *
  */
 public class MapEditorController implements IController {
 	
 	private final MapEditorView view;
 	private final MapEditorModel model;
 	
 	private final AddObstacleController addObstacle;
 	private final EditObstaclesController editObstacle;
 	private final SelectedObstacleController selectedObstacle;
 	private final MapEditorGLController glController;
 	
 	private final OnNameSelectedListener nameSelectedListener = new OnNameSelectedListener() {
 		@Override
 		public void onSelectedName(String name) {
 			model.createNewMap(name);
 		}
 	};
 	
 	private final ObstacleInjectListener obstacleListener = new ObstacleInjectListener() {
 		@Override
 		public void onObstacleAdd(IObstacle o) {
 			if(o != null) {
 				model.addObstacle(o);
 			}
 		}
 	};
 	
 	private final OnLoadedMapSelectedListener loadedMapListener = new OnLoadedMapSelectedListener() {
 		
 		@Override
 		public void onSelect(SimulationMap map) {
 			if(map == null) {
 				JOptionPane.showMessageDialog(view, "Error loading map.", "Error", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			if(!map.isValidMap()) {
 				JOptionPane.showMessageDialog(view, "Error loading map.", "Error", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			model.loadMap(map);
 		}
 	};
 
 	public MapEditorController() {
 		model = new MapEditorModel();
 		view = new MapEditorView(model);
 		glController = new MapEditorGLController(model);
 		view.left.add(glController.view, BorderLayout.CENTER);
 		addObstacle = new AddObstacleController(model, obstacleListener);
 		editObstacle = new EditObstaclesController(model);
 		selectedObstacle = new SelectedObstacleController(model);
 		view.right.add(addObstacle.view);
 		view.right.add(editObstacle.view);
 		view.right.add(selectedObstacle.view);
		view.setVisible(true);
 		init();
 	}
 	
 	
 	@Override
 	public void init() {
 		view.mntmNew.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showSelectNameDialog();
 			}
 		});
 		view.mntmExport.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(!model.hasValidMap()) {
 					JOptionPane.showMessageDialog(view,
 						    "No map to save!",
 						    "Error",
 						    JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				JFileChooser  fc = new JFileChooser();
 				fc.setFileFilter(new MapFileFilter());
 				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				int ret = fc.showSaveDialog(view);
 				if(ret == JFileChooser.APPROVE_OPTION) {
 					File savedFileAs = fc.getSelectedFile();
 					String filePath = savedFileAs.getPath();
 					if(!filePath.toLowerCase().endsWith(".map")) {
 						savedFileAs = new File(filePath + ".map");
 					}
 					if(!MapFileHandler.saveSimulationMap(savedFileAs, model.getCurrentMap())) {
 						JOptionPane.showMessageDialog(view,
 							    "Error saving file to disk!",
 							    "Error",
 							    JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 
 		view.mntmImport.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser  fc = new JFileChooser();
 				fc.setFileFilter(new MapFileFilter());
 				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				int ret = fc.showOpenDialog(view);
 				if(ret == JFileChooser.APPROVE_OPTION) {
 					File selectedFile = fc.getSelectedFile();
 					if(selectedFile != null) {
 						SimulationMap loaded = MapFileHandler.readMapFromFile(selectedFile);
 						if(loaded == null) {
 							JOptionPane.showMessageDialog(view,
 								    "Error loading map from disk.",
 								    "Error",
 								    JOptionPane.ERROR_MESSAGE);
 						} else {
 							if(!MapFileHandler.saveSimulationMap(loaded)) {
 								JOptionPane.showMessageDialog(view,
 									    "Error loading map from disk.",
 									    "Error",
 									    JOptionPane.ERROR_MESSAGE);
 							} else {
 								model.loadMap(loaded);
 							}
 						}
 					} else {
 						JOptionPane.showMessageDialog(view,
 							    "Error loading map from disk.",
 							    "Error",
 							    JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 		
 		view.mntmExit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				MapEditorController.this.release();
 			}
 		});
 
 		view.mntmLoad.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				new ChooseMapDialogController(view, loadedMapListener);
 			}
 		});
 
 		view.mntmSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(!MapFileHandler.saveSimulationMap(model.getCurrentMap())) {
 					JOptionPane.showMessageDialog(view,
 						    "Error saving file to disk!",
 						    "Error",
 						    JOptionPane.ERROR_MESSAGE);
 				} else {
 					JOptionPane.showMessageDialog(view,
 						    "Map saved to Maps folder successfully.",
 						    "Saved",
 						    JOptionPane.INFORMATION_MESSAGE);
 				}
 			}
 		});
 
 		view.mntmSaveAs.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				new ChangeNameDialogController(view, new OnChangeNameSelectedListener() {
 					@Override
 					public void onSelectedName(String name) {
 						if(name == null) {
 							JOptionPane.showMessageDialog(view,
 								    "Invalid file name.",
 								    "Error",
 								    JOptionPane.ERROR_MESSAGE);
 							return;
 						} 
 						if(name.equals("")) {
 							JOptionPane.showMessageDialog(view,
 								    "Invalid file name.",
 								    "Error",
 								    JOptionPane.ERROR_MESSAGE);
 							return;
 						}
 						model.getCurrentMap().setName(name);
 						if(!MapFileHandler.saveSimulationMap(model.getCurrentMap())) {
 							JOptionPane.showMessageDialog(view,
 								    "Error saving file to disk!",
 								    "Error",
 								    JOptionPane.ERROR_MESSAGE);
 						} else {
 							JOptionPane.showMessageDialog(view,
 								    "Map saved to Maps folder successfully.",
 								    "Saved",
 								    JOptionPane.INFORMATION_MESSAGE);
 						}
 					}
 				});
 			}
 		});
 	}
 
 	private void showSelectNameDialog() {
 		new NewMapDialogController(view, nameSelectedListener);
 	}
 	
 	@Override
 	public void release() {
 		view.dispose();
 	}
 
 	@Override
 	public void setModel(IModel m) {
 		
 	}
 	
 	public interface ObstacleInjectListener {
 		public void onObstacleAdd(IObstacle o);
 	}
 	
 
 	private class MapFileFilter extends FileFilter {
 		@Override
 		public boolean accept(File f) {
 			  return f.isDirectory() || f.getName().toLowerCase().endsWith(".map");  
 		}
 		@Override
 		public String getDescription() {
 			  return ".map files"; 
 		}
 	};
 	
 }
