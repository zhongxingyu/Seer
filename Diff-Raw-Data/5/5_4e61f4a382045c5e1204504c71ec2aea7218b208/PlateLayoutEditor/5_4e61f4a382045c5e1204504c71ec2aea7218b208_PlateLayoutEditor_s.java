 package net.bioclipse.brunn.ui.editors.plateLayoutEditor;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.plateLayout.IPlateLayoutManager;
 import net.bioclipse.brunn.genericDAO.IPlateLayoutDAO;
 import net.bioclipse.brunn.pojos.LayoutMarker;
 import net.bioclipse.brunn.pojos.LayoutWell;
 import net.bioclipse.brunn.pojos.PlateFunction;
 import net.bioclipse.brunn.pojos.WellFunction;
 import net.bioclipse.brunn.results.PlateFunctionBody;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.brunn.results.WellFunctionBody;
 import net.bioclipse.brunn.ui.Activator;
 import net.bioclipse.expression.Calculator;
 import net.bioclipse.expression.FunctionBody.ParamType;
 import net.bioclipse.brunn.ui.editors.plateLayoutEditor.CreatePlateFunction;
 import net.bioclipse.brunn.ui.editors.plateLayoutEditor.CreateWellFunction;
 import net.bioclipse.brunn.ui.editors.plateLayoutEditor.model.MarkersModel;
 import net.bioclipse.brunn.ui.editors.plateLayoutEditor.model.PlateFunctionsModel;
 import net.bioclipse.brunn.ui.editors.plateLayoutEditor.model.WellFunctionsModel;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.PlateLayout;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 
 import de.kupzog.ktable.KTable;
 import de.kupzog.ktable.KTableCellSelectionListener;
 import de.kupzog.ktable.SWTX;
 
 public class PlateLayoutEditor extends EditorPart {
 
 	public final static String ID = "net.bioclipse.brunn.ui.editors.plateLayoutEditor.PlateLayoutEditor";
 
 	private KTable markersTable;
 	private KTable wellFunctionsTable;
 	private KTable plateFunctionsTable;
 
 	private Calculator calculator; 
 	
 	private volatile net.bioclipse.brunn.pojos.PlateLayout referencePlateLayout;
 	private volatile net.bioclipse.brunn.pojos.PlateLayout toBeSaved;
 	private PlateLayout plateLayout;
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		IPlateLayoutManager plm 
 			= (IPlateLayoutManager) Springcontact.getBean("plateLayoutManager");
 		plm.edit( Activator.getDefault().getCurrentUser(), toBeSaved );
 		plm.evictFromLazyLoading(toBeSaved);
 		referencePlateLayout = toBeSaved.deepCopy();
 		firePropertyChange(PROP_DIRTY);
 		plateLayout.getParent().fireUpdate();
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 
 		setSite(site);
 		setInput(input);
 
 		plateLayout = (PlateLayout) input;
 		setPartName(plateLayout.getName());
 		
 		toBeSaved = ((net.bioclipse.brunn.pojos.PlateLayout) ((PlateLayout) input)
 				.getPOJO()).deepCopy();
 		toBeSaved.setId(((net.bioclipse.brunn.pojos.PlateLayout) ((PlateLayout) input)
 				.getPOJO()).getId());
 		
 		referencePlateLayout = toBeSaved.deepCopy();
 		calculator = new Calculator();
 		PlateResults.createFunctionBodies(calculator);
 		addVariablesAndFunctions(calculator);
 		plateLayout.getParent().fireUpdate();
 	}
 
 	private void addVariablesAndFunctions(Calculator calculator) {
 		calculator.addVariable("well", Double.NaN);
 		for( LayoutWell lw : toBeSaved.getLayoutWells() ) {
 			calculator.addVariable(lw.getName(), Double.NaN);
 			for( WellFunction wf : lw.getWellFunctions() ) {
 				calculator.addFunction( wf.getName(), 
 						                new WellFunctionBody (wf.getExpression(), 
 						                     lw.getName() + "_" + wf.getName() ) 
 				                      );
 			}
 		}
 		for ( PlateFunction pf : toBeSaved.getPlateFunctions() ) {
 			calculator.addFunction( pf.getName(), 
 					                new PlateFunctionBody(pf.getExpression()) );
 		}
 		calculator.removeVariable("well");
 	}
 
 	@Override
 	public boolean isDirty() {
 		return !referencePlateLayout.equals(toBeSaved);
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FillLayout());
 		final SashForm sashForm = new SashForm(parent, SWT.NONE);
 		sashForm.setOrientation(SWT.VERTICAL);
 		
 		/*
 		 * Create markerstable
 		 */
 		markersTable = new KTable(sashForm, SWT.V_SCROLL           |
 				                            SWT.H_SCROLL           |
 				                            SWTX.FILL_WITH_LASTCOL | 
 				                            SWTX.MARK_FOCUS_HEADERS|
 				                            SWT.MULTI
 				                            );
 		markersTable.setModel(new MarkersModel(toBeSaved, markersTable, this));
 		markersTable.setSelection(2, 2, false);
 		final PlateLayoutEditor editor = this;
 
 		/*
 		 *  When wells are selected, Update the Functions Table
 		 */
 		markersTable
 				.addCellSelectionListener(new KTableCellSelectionListener() {
 
 					public void cellSelected(int col, int row, int statemask) {
 						List<LayoutWell> selectedWells = new ArrayList<LayoutWell>(); 
 						for( Point p : markersTable.getCellSelection() ) {
 							selectedWells.add( toBeSaved.getWell(p.x, (char) (p.y + 'a' - 1)) );
 						}
 						wellFunctionsTable.setModel(
 								new WellFunctionsModel( selectedWells,
 										                editor, calculator ) );
 					}
 
 					public void fixedCellSelected(int col, int row,
 							int statemask) {
 						// TODO Auto-generated method stub
 					}
 				});
 
 		final Composite composite = new Composite(sashForm, SWT.NONE);
 		composite.setLayout(new FillLayout());
 
 		final TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
 
 		final TabItem plateFunctionsTabItem = new TabItem(tabFolder, SWT.NONE);
 		plateFunctionsTabItem.setText("Plate Functions");
 		final Composite plateFunctionsComposite = new Composite(tabFolder, SWT.NONE);
 		plateFunctionsComposite.setLayout(new FillLayout());
 		plateFunctionsTabItem.setControl(plateFunctionsComposite);
 
 		final TabItem wellfunctionsTabItem = new TabItem(tabFolder, SWT.NONE);
 		wellfunctionsTabItem.setText("Well Functions");
 		final Composite wellFunctionsComposite = new Composite(tabFolder, SWT.NONE);
 		wellFunctionsComposite.setLayout(new FillLayout());
 		wellfunctionsTabItem.setControl(wellFunctionsComposite);
 
 		/*
 		 * Create wellfunctions table
 		 */
 		wellFunctionsTable = new KTable(wellFunctionsComposite, SWTX.AUTO_SCROLL       | 
 				                                                SWTX.FILL_WITH_LASTCOL | 
 				                                                SWT.FULL_SELECTION);
 		List<LayoutWell> lws = new ArrayList<LayoutWell>();
 		lws.add(toBeSaved.getWell(2, 'b'));
 		wellFunctionsTable.setModel(new WellFunctionsModel(lws, this, calculator));
 		wellFunctionsTable.setSelection(1, 1, false);
 
 		/*
 		 * Create platefunctions table
 		 */
 		plateFunctionsTable = new KTable(plateFunctionsComposite, SWTX.AUTO_SCROLL       | 
                                                                   SWTX.FILL_WITH_LASTCOL |
                                                                   SWT.FULL_SELECTION);
 		plateFunctionsTable.setModel(new PlateFunctionsModel(toBeSaved, this, calculator));
 		plateFunctionsTable.setSelection(1, 1, false);
 		
 		createWellFunctionsTablesContextMenu();
 		createPlateFunctionsTablesContextMenu();
 		createMarkerTableContextMenu();
 	}
 
 	private void createMarkerTableContextMenu() {
 		// Create menu manager.
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager mgr) {
 				fillMarkerTableContextMenu(mgr);
 			}
 		});
 
 		// Create menu.
 		Menu menu = menuMgr.createContextMenu(markersTable);
 		markersTable.setMenu(menu);
 	}
 
 	/**
 	 * Creating the contextmenu for the wellfunctionstable
 	 */
 	private void createWellFunctionsTablesContextMenu() {
 		// Create menu manager.
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager mgr) {
 				fillWellFunctionsContextMenu(mgr);
 			}
 		});
 
 		// Create menu.
 		Menu menu = menuMgr.createContextMenu(wellFunctionsTable);
 		wellFunctionsTable.setMenu(menu);
 	}
 
 	/**
 	 * Creating the contextmenu for the platefunctionstable
 	 */
 	private void createPlateFunctionsTablesContextMenu() {
 		// Create menu manager.
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager mgr) {
 				fillPlateFunctionsContextMenu(mgr);
 			}
 		});
 
 		// Create menu.
 		Menu menu = menuMgr.createContextMenu(plateFunctionsTable);
 		plateFunctionsTable.setMenu(menu);
 	}
 
 	private void fillMarkerTableContextMenu(IMenuManager mgr) {
 		HashMap<String, List<Action>> markerNames = new HashMap<String, List<Action>>();
 		for ( Point p : markersTable.getCellSelection() ) {
 			for( Action a 
 				 : ( (MarkersModel)markersTable.getModel() )
 				   .createMarkerEditactions( p.x, 
 					  	                     p.y, 
 						                     null, 
 						                     markersTable) ) {
 				if( markerNames.get(a.getText()) == null ) {
 					List<Action> l = new ArrayList<Action>();
 					l.add(a);
 					markerNames.put(a.getText(), l);
 				}
 				else {
 					markerNames.get(a.getText()).add(a);
 				}
 			}
 		}
 		List<List<Action>> markerNamesList = new ArrayList<List<Action>>(markerNames.values());
 		Comparator<List<Action>> c = new Comparator<List<Action>>() {
 			public int compare(List<Action> o1, List<Action> o2) {
 				String s1 = o1.get(0).getText(),
 				       s2 = o2.get(0).getText();
 				Pattern actionNameSplicer = Pattern.compile("(.*?) M(\\d+)");
 				Matcher m1 = actionNameSplicer.matcher(s1),
 				        m2 = actionNameSplicer.matcher(s2);
 				if (m1.matches() && m2.matches()) {
 					String actionType1 = m1.group(1),
 					       actionType2 = m2.group(1);
 					
 					if(actionType1.equals(actionType2)) {
 						int mNumber1 = Integer.parseInt( m1.group(2) ), 
 						    mNumber2 = Integer.parseInt( m2.group(2) );
 						return mNumber1-mNumber2;
 					}
 				}
 				return s1.compareTo(s2);
 			}
 		};
 		Collections.sort(markerNamesList, c);
 		for( final List<Action> actions : markerNamesList ) {
 			if(actions.size() == markersTable.getCellSelection().length) {
 				mgr.add( new Action(actions.get(0).getText() ) { 
 					public void run() {
 						for(Action a : actions) {
 							a.run();
 						}
 						dirtyCheck();
 					}
 				});
 			}
 		}
 		
 	}
 	
 	private void fillWellFunctionsContextMenu(IMenuManager mgr) {
 		final PlateLayoutEditor editor = this;
 		
 		/*
 		 * CREATE WELL FUNCTION
 		 */
 		mgr.add( new Action("Add function") {
 			public void run() {
 			    
 			    if ( markersTable.getCellSelection().length == 0 ) {
 			        MessageDialog.openInformation( 
 			            PlatformUI.getWorkbench()
 			                      .getActiveWorkbenchWindow()
 			                      .getShell(), 
 			            "No well selected", 
 			            "Select one or many wells to add the well "
 			            + "function to first" );
 			        return; 
 			    }
 	
 				CreateWellFunction dialog = new CreateWellFunction( PlatformUI.
                                                                     getWorkbench().
                                                                     getActiveWorkbenchWindow().
                                                                     getShell(), 
                                                                     calculator );
 				if( dialog.open() == CreateWellFunction.OK ) {
 					List<LayoutWell> selectedLayoutWells = new ArrayList<LayoutWell>();
 					for( Point p : markersTable.getCellSelection() ) {
 						selectedLayoutWells.add( toBeSaved.getLayoutWell(p) );
 					}
 					
 					for( LayoutWell lw : selectedLayoutWells ) {
 						lw.getWellFunctions().add(
 							new WellFunction( Activator.getDefault().getCurrentUser(), 
 						                      dialog.getName(), 
 						                      dialog.getExpression().replaceAll("well", lw.getName()), 
 						                      lw ));
 					}
 					
 					wellFunctionsTable.setModel( new WellFunctionsModel(selectedLayoutWells, editor, calculator) );
 					wellFunctionsTable.redraw();
 					editor.firePropertyChange(PROP_DIRTY);
 				}
 			}
 		});
 		
 		/*
 		 * remove well function
 		 */
 		mgr.add( new Action("Remove function") {
 			public void run() {
 				int selectedRow = wellFunctionsTable.getRowSelection()[0];
 				WellFunction toBeRemoved =
 					( (WellFunctionsModel)wellFunctionsTable.getModel() )
 						.getWellFunctions().get( selectedRow -1 + "" );
 				
 				List<WellFunction> listOfToBeRemoved = new ArrayList<WellFunction>();
 				
 				for ( Point p : markersTable.getCellSelection() ) {
 					for ( WellFunction wf : toBeSaved.getLayoutWell(p)
 							                         .getWellFunctions() ) {
 						if ( wf.getName().equals( toBeRemoved.getName() ) ) {
 							listOfToBeRemoved.add(wf);
 						}
 					}
 				}
 				
 				for ( WellFunction wf : listOfToBeRemoved ) {
 					wf.delete();
 					wf.getWell().getWellFunctions().remove(wf);					
 				}
 				
 				List<LayoutWell> lws = new ArrayList<LayoutWell>();
 				for( Point p : markersTable.getCellSelection() ) {
 					lws.add( toBeSaved.getLayoutWell(p) );	
 				}				
 				wellFunctionsTable.setModel( new WellFunctionsModel(lws, editor, calculator) );
 
 				editor.firePropertyChange(PROP_DIRTY);
 			}
 		});
 	}
 	
 	private void fillPlateFunctionsContextMenu(IMenuManager mgr) {
 		final PlateLayoutEditor editor = this;
 		
 		/*
 		 * CREATE PLATE FUNCTION
 		 */
 		mgr.add( new Action("Add function") {
 			public void run() {
 	
 				CreatePlateFunction dialog = new CreatePlateFunction( PlatformUI.
                                                                       getWorkbench().
                                                                       getActiveWorkbenchWindow().
                                                                       getShell(), 
                                                                       calculator );
 				if(dialog.open() == CreatePlateFunction.OK) {
 				
 					toBeSaved.getPlateFunctions().add(
 							new PlateFunction( Activator.getDefault().getCurrentUser(), 
 									           dialog.getName(), 
 									           dialog.getExpression(),
 									           dialog.getGoodFrom(),
 									           dialog.getGoodTo(),
 									           dialog.hasSpecifiedValues(), 
 									           toBeSaved ) );
 	
 					calculator.addFunction( dialog.getName(), 
 							new PlateFunctionBody( dialog.getExpression() ) );
 					
 					plateFunctionsTable.setModel(
 							new PlateFunctionsModel( toBeSaved, 
 									                 editor, 
 									                 calculator ) );
 					plateFunctionsTable.redraw();
 					editor.firePropertyChange(PROP_DIRTY);
 				}
 			}
 		});
 		
 		/*
 		 * remove plate function
 		 */
 		mgr.add( new Action("Remove function") {
 			public void run() {
 				int selectedRow = plateFunctionsTable.getRowSelection()[0];
 				PlateFunction toBeRemoved =
 					( (PlateFunctionsModel)plateFunctionsTable.getModel() )
 						.getPlateFunctions().get( selectedRow -1 + "" );
 				toBeRemoved.delete();
 				toBeRemoved.getPlate().getPlateFunctions().remove(toBeRemoved);
 				
 				plateFunctionsTable.setModel(
 						new PlateFunctionsModel( toBeSaved, 
 								                 editor, 
 								                 calculator) );
 				editor.firePropertyChange(PROP_DIRTY);
 			}
 		});
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 	}
 	
 	public static class MarkerComparator implements Comparator<String> {
 
 		public static final MarkerComparator INSTANCE = new MarkerComparator();
 		
 		private MarkerComparator() {
 			super();
 		}
 		
 		public int compare(String o1, String o2) {
 			try {
 				int i1 = Integer.parseInt(o1.substring(1));
 				int i2 = Integer.parseInt(o2.substring(1));
 				return i1 - i2;
 			} catch (Exception e) {
 				return o1.compareTo(o2);
 			}
 		}
 	}
 	
 	public void dirtyCheck() {
 		firePropertyChange(PROP_DIRTY);
 	}
 	
     public void updatePlateFunctions( String markerName ) {
 
        assert ( markerName.contains( "C" ) || markerName.contains( "B" ) ); 
         
         /*
          * Collect all platefunction names
          */
         List<String> plateFunctionNames = new ArrayList<String>();
         
         if ( markerName.contains( "C" ) ) {
             plateFunctionNames.add( "CV_"  + markerName );
             plateFunctionNames.add( "AVG_AllControls"   );
             plateFunctionNames.add( "CV_AllControls"    );
             plateFunctionNames.add( "AVG_" + markerName );
         }
         else {
             plateFunctionNames.add( "AVG_AllBlanks" );
         }
         plateFunctionNames.add( "ControlBlankRatio" );
         
         /*
          * remove any old versions of the plate functions before adding new 
          */
         for ( String s : plateFunctionNames ) {
             removePlateFunction( s );
         }
         
         /*
          * Build the plateFunctions
          */
         String controlWellNames = buildWellNamesList( "C" );
         String blankWellNames   = buildWellNamesList( "B" ); 
         List<PlateFunction> plateFunctions = new ArrayList<PlateFunction>();
         if ( markerName.contains( "C" ) ) {
             String wellNames = buildWellNamesList(markerName);
             plateFunctions.add( 
                 new PlateFunction( 
                     Activator.getDefault().getCurrentUser(), 
                     plateFunctionNames.get( 0 ), 
                     "100 * ( stddev(" + wellNames + " ) / avg(" + wellNames + ") )",
                     0,
                     0,
                     false, 
                     toBeSaved ) );
             plateFunctions.add( 
                 new PlateFunction( 
                     Activator.getDefault().getCurrentUser(), 
                     plateFunctionNames.get( 1 ), 
                     "avg(" + controlWellNames + ")",
                     0,
                     0,
                     false, 
                     toBeSaved ) );
             plateFunctions.add( 
                 new PlateFunction( 
                     Activator.getDefault().getCurrentUser(), 
                     plateFunctionNames.get( 2 ), 
                     "100 * ( stddev(" + controlWellNames + ") / avg(" + controlWellNames + ") )",
                     0,
                     0,
                     false, 
                     toBeSaved ) );
             plateFunctions.add( 
                 new PlateFunction( 
                     Activator.getDefault().getCurrentUser(), 
                     plateFunctionNames.get( 3 ), 
                     "avg(" + wellNames + ")",
                     0,
                     0,
                     false, 
                     toBeSaved ) );
         }
         else {
             plateFunctions.add( 
                 new PlateFunction( 
                     Activator.getDefault().getCurrentUser(), 
                     plateFunctionNames.get( 0 ), 
                     "avg(" + blankWellNames + ")",
                     0,
                     0,
                     false, 
                     toBeSaved ) );
         }
 
         /*
          * Only build RatioControlBlank if both controls and blanks are on the 
          * platelayout
          */
         if ( blankWellNames.length()   > 0 && 
              controlWellNames.length() > 0 ) {
             
             plateFunctions.add( 
                 new PlateFunction( 
                     Activator.getDefault().getCurrentUser(), 
                                    plateFunctionNames.get( 
                                        plateFunctionNames.size()-1 ), 
                                    "AVG_AllControls() / AVG_AllBlanks()",
                                    0,
                                    0,
                                    false, 
                                    toBeSaved ) );
         }
 
         /*
          * Add the platefunctions
          */
         Iterator<PlateFunction> pfi = plateFunctions.iterator();
         Iterator<String>        pfni = plateFunctionNames.iterator();
         while( pfi.hasNext() && pfni.hasNext() ) {
             PlateFunction pf = pfi.next();
             String pfn       = pfni.next();
             
             toBeSaved.getPlateFunctions().add( pf );
             calculator.addFunction( pfn, 
                                     new PlateFunctionBody( 
                                             pf.getExpression() ) );
         }
 
         /*
          * Update gui
          */
         plateFunctionsTable.setModel( new PlateFunctionsModel( toBeSaved, 
                                                                this, 
                                                                calculator ) );
         plateFunctionsTable.redraw();
         firePropertyChange(PROP_DIRTY);
     }
 
     
     /**
      * Builds string for platefunctions that contains all wells with a marker 
      * with a name containing a part of the given string
      * 
      * @param markerNamePart
      * @return
      */
     private String buildWellNamesList( String markerNamePart ) {
         StringBuilder wellNames = new StringBuilder();
         for ( LayoutWell w : toBeSaved.getLayoutWells() ) {
             for ( LayoutMarker m : w.getLayoutMarkers() ) {
                 if ( m.getName().contains( markerNamePart ) ) {
                     wellNames.append( w.getName() );
                     wellNames.append( "," );
                     break;
                 }
             }
         }
         return wellNames
                .substring( 0, wellNames.length() > 0 
                               ? wellNames.length() - 1 //get rid of last "," 
                               : 0);  
     }
 
     private void removePlateFunction( String plateFunctionName ) {
         for ( Iterator<PlateFunction> i 
                 = toBeSaved.getPlateFunctions().iterator() ; 
               i.hasNext() ; ) {
             if ( plateFunctionName.equals( i.next().getName() ) ) {
                 i.remove();
                 break;
             }
         }
     }
 }
