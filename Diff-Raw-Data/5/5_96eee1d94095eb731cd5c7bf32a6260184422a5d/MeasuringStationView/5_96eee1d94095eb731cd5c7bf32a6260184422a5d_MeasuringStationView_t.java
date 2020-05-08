 package de.ptb.epics.eve.viewer;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 import de.ptb.epics.eve.data.measuringstation.AbstractDevice;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 
 import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.actions.LoadMeasuringStationAction;
 
 /**
  * A simple view implementation, which only displays a label.
  * 
  * @author PTB
  *
  */
 public final class MeasuringStationView extends ViewPart {
 
 	public static final String ID = "MeasuringStationView"; // TODO Needs to be whatever is mentioned in plugin.xml
 
 	private IMeasuringStation measuringStation;
 	
 	private TreeViewer treeViewer;
 	
 	private DragSource source;
 	
 	private LoadMeasuringStationAction loadMeasuringStationAction;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 	
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl( final Composite parent ) {
 		
 		measuringStation = Activator.getDefault().getMeasuringStation();
 		if( measuringStation == null ) {
 			final Label errorLabel = new Label( parent, SWT.NONE );
 			errorLabel.setText( "No device description has been loaded. Please check Preferences!" );
 			return;
 		}
 				
 		final FillLayout fillLayout = new FillLayout();
 		parent.setLayout( fillLayout );
 		this.treeViewer = new TreeViewer( parent );
 		this.treeViewer.setContentProvider( new MeasuringStationTreeViewContentProvider() );
 		this.treeViewer.setLabelProvider( new MeasuringStationTreeViewLabelProvider() );
 		this.treeViewer.getTree().setEnabled( false );
		/*this.treeViewer.getTree().addMouseListener( new MouseListener() {
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				IViewReference[] ref = getSite().getPage().getViewReferences();
 				for( final IViewReference r : ref ) {
 					final IViewPart view = r.getView( false );
 					if( r.getId().equals( "DeviceInspectorView" ) && getSite().getPage().isPartVisible( view ) ) {
 						if( treeViewer.getTree().getSelection()[0].getData() instanceof AbstractDevice ) {
 							((DeviceInspectorViewer)view).addAbstractDevice( (AbstractDevice)treeViewer.getTree().getSelection()[0].getData() );
 						}
 					}
 				}
 				
 			}
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
		});*/
 		
 		this.treeViewer.addDoubleClickListener( new IDoubleClickListener() {
 
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				IViewReference[] ref = getSite().getPage().getViewReferences();
 				for( final IViewReference r : ref ) {
 					final IViewPart view = r.getView( false );
 					if( r.getId().equals( "DeviceInspectorView" ) && getSite().getPage().isPartVisible( view ) ) {
 						if( treeViewer.getTree().getSelection()[0].getData() instanceof AbstractDevice ) {
 							((DeviceInspectorViewer)view).addAbstractDevice( (AbstractDevice)treeViewer.getTree().getSelection()[0].getData() );
 						}
 					}
 				}
 				
 			}
 			
 		});
 		
 		this.source = new DragSource( this.treeViewer.getTree(), DND.DROP_COPY | DND.DROP_MOVE );
 		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
 		source.setTransfer( types );
 		source.addDragListener( new DragSourceListener() {
 
 			public void dragStart( final DragSourceEvent event ) {
 				if( treeViewer.getTree().getSelection().length == 0 ) {
 					event.doit = false;
 				} else {
 					event.doit = true;
 				}
 				event.data = null;
 				
 			}
 			
 			public void dragSetData( final DragSourceEvent event ) {
 				TreeItem[] items = treeViewer.getTree().getSelection();
 				if( TextTransfer.getInstance().isSupportedType( event.dataType ) ) {
 					if (items[0].getData() instanceof AbstractDevice)
 						event.data = getViewSite().getId() + "," + ((AbstractDevice)items[0].getData()).getFullIdentifyer();
 					else if (items[0].getData() instanceof String) {
 						System.err.println("MeasuringStationView String item");
 						event.data = getViewSite().getId() + "," + (String)items[0].getData();		
 					}
 				}
 			}
 			
 			public void dragFinished( final DragSourceEvent event ) {
 				
 			}
 
 			
 
 			
 		});
 		
 		
 		this.treeViewer.getTree().addSelectionListener( new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				TreeItem[] items = treeViewer.getTree().getSelection();
 				if( items.length > 0 ) {
 					if( items[0].getData() instanceof AbstractDevice ) {
 						IViewReference[] ref = getSite().getPage().getViewReferences();
 						DeviceOptionsViewer view = null;
 						for( int i = 0; i < ref.length; ++i ) {
 							if( ref[i].getId().equals( "DeviceOptionsView" ) &&  !((DeviceOptionsViewer)ref[i].getPart(true)).isFixed() ) {
 								view = (DeviceOptionsViewer)ref[i].getPart( true );
 							}
 						}
 						if (view != null) view.setDevice( (AbstractDevice)items[0].getData() );
 					}
 				}
 				
 			}
 			
 		});
 		setMeasuringStation(measuringStation);
 		final IWorkbenchPage page = getSite().getPage();
 		final MenuManager menuManager = new MenuManager( "#PopupMenu" );
 		menuManager.setRemoveAllWhenShown( true );
 		menuManager.addMenuListener( new IMenuListener() {
 
 			@Override
 			public void menuAboutToShow( final IMenuManager manager ) {
 				final TreeItem[] selectedItems = treeViewer.getTree().getSelection();
 				if( selectedItems != null && selectedItems.length > 0 ) {
 					final Action openAction = new Action() {
 						public void run() {
 							super.run();
 							for( final TreeItem item : selectedItems ) {
 								if( item.getData() instanceof AbstractDevice ) {
 									try {
 										
 										final IViewPart view = page.showView( "DeviceOptionsView", ((AbstractDevice)item.getData()).getName(), IWorkbenchPage.VIEW_CREATE );
 										((DeviceOptionsViewer)view).setDevice( (AbstractDevice)item.getData() );
 										((DeviceOptionsViewer)view).setFixed( true );
 									} catch (PartInitException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 								}
 								
 							}
 						}
 					};
 					openAction.setText( "Open in seperate options window" );
 					manager.add( openAction );
 					
 					final MenuManager deviceInspectors = new MenuManager( "Open in Device Inspector" );
 					
 					IViewReference[] ref = getSite().getPage().getViewReferences();
 					for( final IViewReference r : ref ) {
 						if( r.getId().equals( "DeviceInspectorView" ) ) {
 							final Action action = new Action() {
 								
 								public void run() {
 									super.run();
 									for( final TreeItem item : selectedItems ) {
 										if( item.getData() instanceof AbstractDevice ) {
 											((DeviceInspectorViewer)r.getPart( true )).addAbstractDevice( (AbstractDevice)item.getData() );
 										}
 										
 									}
 								}
 							};
 							action.setText( r.getPartName() );
 							deviceInspectors.add( action );
 						}
 					}
 					
 					manager.add( deviceInspectors );
 				}
 				
 			}
 			
 		});
 		final Menu contextMenu = menuManager.createContextMenu( this.treeViewer.getTree() );
 		this.treeViewer.getTree().setMenu( contextMenu );
 		
 	}
 
 	public void setMeasuringStation( final IMeasuringStation measuringStation ) {
 		Activator.getDefault().getMessagesContainer().addMessage( new ViewerMessage( MessageSource.VIEWER, MessageTypes.INFO, "Got new measuring station description." ) );
 		Activator.getDefault().getChainStatusAnalyzer().reset();
 		this.measuringStation = measuringStation;
 		this.treeViewer.setInput( this.measuringStation );
 		this.treeViewer.getTree().setEnabled( this.measuringStation != null );
 		this.treeViewer.expandAll();
 	}
 	
 	public IMeasuringStation getMeasuringStation() {
 		return this.measuringStation;
 	}
 
 }
