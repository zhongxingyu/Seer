 package genlab.gui.views;
 
 import genlab.core.usermachineinteraction.IListOfMessagesListener;
 import genlab.core.usermachineinteraction.ITextMessage;
 import genlab.core.usermachineinteraction.ListOfMessages;
 import genlab.core.usermachineinteraction.ListsOfMessages;
 import genlab.gui.VisualResources;
 import genlab.gui.actions.ClearMessagesAction;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.util.Locale;
 
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.part.ViewPart;
 
 /**
  * Displays messages as a table. 
  * Listens for general messages.
  * 
  * TODO add exportation to a text file ?!
  * 
  * @author Samuel Thiriot
  *
  */
 public abstract class MessagesViewAbstract extends ViewPart  {
 
 
 	/**
 	 * Datetime format used to display the "when" column
 	 */
 	public static final DateFormat DATE_FORMAT = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault()); 
 
 	/**
 	 * Comparator for the table viewer (sorts info)
 	 */
 	private MyViewerComparator comparator = null;
 	
 	/**
 	 * The table viewer; we use a virtual one (meaning, it does only display what is visible), 
 	 * but not a lazy loader, because loading data is not coslty for us.
 	 */
 	protected TableViewer viewer = null;
 
 	/**
 	 * The listener which monitors the changes in the list of messages
 	 */
 	protected IListOfMessagesListener listener = null;
 	
 	/**
 	 * The list of messages we are listening to
 	 */
 	protected ListOfMessages messages = null;
 	
 	/**
 	 * The part composite of the view (if of use ?)
 	 */
 	private Composite parent;
 	private Display display;
 	
 	
 	public MessagesViewAbstract() {
 	}
 
 	
 	/**
 	 * In charge of refreshing the view as soon as possible, 
 	 * but not too often
 	 * @author Samuel Thiriot
 	 *
 	 */
 	class ThreadMessageUpdate extends Thread {
 		
 		private boolean cancel = false;
 		private boolean contentToDisplay = false;
 		private boolean updateInProgress = false;
 
 		
 		/**
 		 * Minimum delay between two refresh (avoids to update in continuous)
 		 */
 		private long minPeriodMs = 100;
 		
 		private boolean longSleep = false;
 		
 		private Runnable runnable = null;
 
 		public ThreadMessageUpdate() {
 			setDaemon(true);
 			setPriority(MIN_PRIORITY);
 			setName("glUpdateMessages");
 			
 			runnable = new Runnable() {
 				
 				@Override
 				public void run() {
 											
 					if (parent.isDisposed() && listener != null) {
 						ListsOfMessages.getGenlabMessages().removeListener(listener);
 						return;
 					}
 
 					try {
 
 						viewer.getControl().setRedraw(false);
 						//viewer.setInput("toto");
 						viewer.refresh(false);
 						viewer.getControl().setRedraw(true);
 												
 					} catch (RuntimeException e) {
 							// TODO manage disposed exception
 						e.printStackTrace();
 					}
 					
 					updateInProgress = false;
 					
 				}
 			};
 		}
 		
 		public void cancel() {
 			cancel = true;
 			
 			if (longSleep)
 				interrupt();
 		}
 		
 		public void notifySomethingToDisplay() {
 						
 			if (updateInProgress)
 				return; // ignore if we are aleady updating
 			
 			contentToDisplay = true;
 			
 			if (longSleep)
 				interrupt();
 			
 		}
 		
 		protected void refreshDisplay() {
 			
 			
 			if (updateInProgress)
 				return;
 			
 			if (display == null || display.isDisposed()) {
 				this.cancel();
 				if (messages != null)
 					messages.removeListener(listener);
 			}
 
 			updateInProgress = true;
 			//long timestampStart = System.currentTimeMillis();  
 			//System.err.println("update display: begin");
 			display.syncExec(runnable);
 			//System.err.println("time to update display : "+(System.currentTimeMillis()-timestampStart));
 
 		}
 		
 		@Override
 		public void run() {
 						
 			while (!cancel) {
 				
 				// update display
 				if (contentToDisplay) {
 					
 					refreshDisplay();
 					contentToDisplay = false;
 
 					// now sleep (short)
 					
 					longSleep = false;
 					try {
 						Thread.sleep(minPeriodMs);
 					} catch (InterruptedException e) {
 					}
 					
 				} else {
 					
 					longSleep = true;
 					try {
 						Thread.sleep(1000*60*60);
 					} catch (InterruptedException e) {
 					}
 					longSleep = false;
 				}
 				
 				
 								
 			}
 		}
 	}
 	
 	private ThreadMessageUpdate updateThread = null;
 	
 
 	class MessagesContentProvider implements IStructuredContentProvider {
 
 		private final ListOfMessages list;
 		
 		public MessagesContentProvider(ListOfMessages list) {
 			this.list = list;
 		}
 		
 		@Override
 		public void dispose() {
 			
 		}
 
 		@Override
 		public void inputChanged(Viewer pviewer, Object oldInput, Object newInput) {
 			// nothing to do (read only !)
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			//System.err.println("get elements called; "+list.getSize()+" vs. "+viewer.getTable().getItemCount());
 
 			Object[] res = null;
 			res = list.asArray();
 			return res;
 		}
 
 		
 	}
 	
 	/**
 	 * Basic label provider that manages colors
 	 * 
 	 * @author Samuel Thiriot
 	 *
 	 */
 	protected class ColorColumnProvider extends ColumnLabelProvider {
 		
 		  @Override
 		  public Color getForeground(Object element) {
 			    ITextMessage message = (ITextMessage)element;
 			    
 			    switch (message.getLevel()) {
 			    case DEBUG:
 			    case TRACE:
 			    	return VisualResources.COLOR_GRAY;
 			    case ERROR:
 			    	return VisualResources.COLOR_RED;
 			    default:
 			    	return null;
 			    }
 			    
 		  }
 
 	}
 	
 	/**
 	 * Provides comparison for sorting.
 	 *
 	 */
 	public class MyViewerComparator extends ViewerComparator {
 		
 		  private int propertyIndex;
 		  private static final int DESCENDING = 1;
 		  private int direction = DESCENDING;
 
 		  public MyViewerComparator() {
 		    this.propertyIndex = 2; // sort by date by default
 		    direction = DESCENDING;
 		  }
 
 		  public int getDirection() {
 		    return direction == 1 ? SWT.DOWN : SWT.UP;
 		  }
 
 		  public void setColumn(int column) {
 		    if (column == this.propertyIndex) {
 		      // Same column as last sort; toggle the direction
 		      direction = 1 - direction;
 		    } else {
 		      // New column; do an ascending sort
 		      this.propertyIndex = column;
 		      direction = DESCENDING;
 		    }
 		  }
 
 		  @Override
 		  public int compare(Viewer viewer, Object e1, Object e2) {
 		    ITextMessage p1 = (ITextMessage) e1;
 		    ITextMessage p2 = (ITextMessage) e2;
 		    int rc = 0;
 		    switch (propertyIndex) {
 		    case 0:
 		      rc = p1.getLevel().compareTo(p2.getLevel());
 		      break;
 		    case 1:
 		    	rc = p1.getAudience().compareTo(p2.getAudience());
 		      break;
 		    case 2:
 		      rc = p1.getTimestamp().compareTo(p2.getTimestamp());
 		      break;
 		    case 3:
 		    	rc = p1.getEmitter().getCanonicalName().compareTo(p2.getEmitter().getCanonicalName());
 		      break;
 		    case 4:
 		    	rc = p1.getMessage().compareTo(p2.getMessage());
 		      break;
 		    default:
 		    	System.err.println("default ! :-(");
 		      rc = 0;
 		    }
 		    // If descending order, flip the direction
 		    if (direction == DESCENDING) {
 		      rc = -rc;
 		    }
 		    return rc;
 		  }
 
 		} 
 	
 	/**
 	 * Listens for click events on the table, and changes sorting
 	 * @param column
 	 * @param index
 	 * @return
 	 */
 	 private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
 		 
 		    SelectionAdapter selectionAdapter = new SelectionAdapter() {
 		      @Override
 		      public void widgetSelected(SelectionEvent e) {
 		    	  if (comparator == null || viewer == null)
 		    		  return;
 		    	  
 		        comparator.setColumn(index);
 		        int dir = comparator.getDirection();
 		        viewer.getTable().setSortDirection(dir);
 		        viewer.refresh();
 		      }
 		    };
 		    return selectionAdapter;
 	}
 	 
 	/**
 	 * Returns the list of messages tracked by this view
 	 * @return
 	 */
 	public ListOfMessages getListOfMessages() {
 		return messages;
 	}
 	
 	@Override
 	public void createPartControl(final Composite parent) {
 		
 		this.parent = parent;
 		this.display = parent.getDisplay();
 		
 		viewer = new TableViewer(
 				parent, 
 				//SWT.MULTI | 
 				SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.READ_ONLY | SWT.VIRTUAL
 				);
 		viewer.setUseHashlookup(true);
 
 		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
 
 
 		// configure viewer
 		
 		comparator = new MyViewerComparator();
 		viewer.setComparator(comparator);
 		
 		// ... columns
 		{
 			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
 			col.getColumn().setWidth(100);
 			col.getColumn().setText("Level");
 			col.setLabelProvider(new ColorColumnProvider() {
 				
 			  @Override
 			  public String getText(Object element) {
 			    ITextMessage message = (ITextMessage)element;
 			    return message.getLevel().toString();
 			  }
 				
 			  @Override
 				public Image getImage(Object element) {
 				    ITextMessage message = (ITextMessage)element;
 
 				  switch (message.getLevel()) {
 				  case TRACE:
 				  case DEBUG:
 					  return VisualResources.ICON_DEBUG;
 				  case TIP:
 					  return VisualResources.ICON_TIP;
 				  case ERROR:
 					  return VisualResources.ICON_ERROR;
 				  case INFO:
 					  return VisualResources.ICON_INFO;
 				  case WARNING:
 					  return VisualResources.ICON_WARNING;
 				  default:
 					  return null;
 				  }
 
 				}
 			});
 			col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 0));
 
 		}
 		{
 			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
 			col.getColumn().setWidth(100);
 			col.getColumn().setText("Audience");
 			col.setLabelProvider(new ColorColumnProvider() {
 
 			@Override
 			  public String getText(Object element) {
 			    ITextMessage message = (ITextMessage)element;
 			    return message.getAudience().toString();
 			  }
 			});
 			col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 1));
 
 		}
 		{
 			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
 			col.getColumn().setWidth(100);
 			col.getColumn().setText("When");
 			col.setLabelProvider(new ColorColumnProvider() {
 			  @Override
 			  public String getText(Object element) {
 			    ITextMessage message = (ITextMessage)element;
 			    return DATE_FORMAT.format(message.getDate());
 			  }
 			});
 			col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 2));
 
 		}
 		{
 			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
 			col.getColumn().setWidth(300);
 			col.getColumn().setText("From");
 			
 			col.setLabelProvider(new ColorColumnProvider() {
 				
 				@Override
 				public String getToolTipText(Object element) {
 					ITextMessage message = (ITextMessage)element;
 					return message.getEmitter().getCanonicalName();
 				}
 				
 
 				public int getToolTipDisplayDelayTime(Object object) {
 					return 50;
 				}
 
 				public int getToolTipTimeDisplayed(Object object) {
 					return 10000;
 				}
 
 				  @Override
 				  public String getText(Object element) {
 				    ITextMessage message = (ITextMessage)element;
 				    return message.getEmitter().getCanonicalName();
 				  }
 			  
 			  
 			  
 			});
 			
 			col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 3));
 			
 		}
 		{
 			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
 			col.getColumn().setWidth(400);
 			col.getColumn().setText("Message");
 			
 			col.setLabelProvider(new ColorColumnProvider() {
 				
 				@Override
 				public String getToolTipText(Object element) {
 					ITextMessage message = (ITextMessage)element;
 					StringWriter sw = new StringWriter();
 					PrintWriter pw = new PrintWriter(sw);
					pw.println(message.getMessage());
 					if (message.getException() != null)
 						message.getException().printStackTrace(pw);
 					return sw.toString();
 				}
 				
 
 				public int getToolTipDisplayDelayTime(Object object) {
 					return 100;
 				}
 
 				public int getToolTipTimeDisplayed(Object object) {
 					return 40000;
 				}
 
 				  @Override
 				  public String getText(Object element) {
 				    ITextMessage message = (ITextMessage)element;
 				    if (message.getCount() > 1) {
 				    	StringBuffer sb = new StringBuffer();
 				    	return sb
 					    		.append("(")					//$NON-NLS-1$
 					    		.append(message.getCount())
 					    		.append(" times") 
 					    		.append(") ")					//$NON-NLS-1$
 					    		.append(message.getMessageFirstLine())
 					    		.toString();
 					  } else
 				    	return message.getMessageFirstLine();
 				  	}
 			  
 			  
 			  
 			});
 			
 			//col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 4));
 			
 		}
 		
 		// configure table
 		final Table table = viewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true); 
 		
 		
 		updateThread = new ThreadMessageUpdate();
 		
 		listener = new IListOfMessagesListener() {
 			
 			@Override
 			public void contentChanged(ListOfMessages list) {
 
 				if (viewer == null || viewer.getControl().isDisposed()) {
 					messages.removeListener(this);
 					return;
 				}
 				
 				updateThread.notifySomethingToDisplay();
 				
 			}
 
 			@Override
 			public void messageAdded(ListOfMessages list, ITextMessage message) {
 				
 			}
 		};
 		
 		updateThread.start();
 		
 		listenMessages();
 	
 		// add actions
 		getViewSite().getActionBars().getToolBarManager().add(new ClearMessagesAction());  
 
 		
 	}
 	
 
 
 	@Override
 	public void setFocus() {
 		// nothing to do (?)
 	}
 
 	protected abstract void listenMessages();
 
 	@Override
 	public void dispose() {
 		
 		if (updateThread != null)
 			updateThread.cancel();
 
 		if (listener != null && messages != null)
 			messages.removeListener(listener);
 
 		
 		
 		super.dispose();
 	}
 
 	
 	
 }
