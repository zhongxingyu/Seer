 package org.rubypeople.rdt.internal.ui.infoviews;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.RubyPluginImages;
 import org.rubypeople.rdt.internal.ui.rdocexport.RDocUtility;
 import org.rubypeople.rdt.internal.ui.rdocexport.RdocListener;
 import org.rubypeople.rdt.ui.PreferenceConstants;
 
 public class RIView extends ViewPart implements RdocListener {
 
 	private boolean riFound = false;
 	private PageBook pageBook;
     private SashForm form;    
     private Label riNotFoundLabel;
 	private Text searchStr;
     private TableViewer searchListViewer;
     private Browser searchResult;
     private static List<String> fgPossibleMatches = new ArrayList<String>();
     private ListContentProvider contentProvider = new ListContentProvider();
 	private RubyInvokerJob latestJob;
 
 	/**
 	 * The constructor.
 	 */
 	public RIView() {}
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 */
 	public void createPartControl(Composite parent) {
 		contributeToActionBars();
 		
 		pageBook = new PageBook(parent, SWT.NONE);
           
         riNotFoundLabel = new Label( pageBook, SWT.LEFT | SWT.TOP | SWT.WRAP );
         riNotFoundLabel.setText( InfoViewMessages.getString( "RubyInformation.ri_not_found")  );
                        
         
         Label inProgressLabel = new Label( pageBook, SWT.LEFT | SWT.TOP | SWT.WRAP );
         inProgressLabel.setText("Please wait. Updating RI View...");
         
         form = new SashForm(pageBook, SWT.HORIZONTAL);        
                        
         Composite panel = new Composite(form, SWT.NONE);
         panel.setLayout(new GridLayout(1, false));              
        
         // Search String
         searchStr = new Text(panel, SWT.BORDER);
         GridData data = new GridData();        
         data.horizontalAlignment = SWT.FILL;
         searchStr.setLayoutData(data);
         searchStr.addModifyListener(new ModifyListener() {        
             public void modifyText(ModifyEvent e) {
                 filterSearchList();                
             }        
         });
         searchStr.addKeyListener(new KeyAdapter() {
             public void keyPressed(KeyEvent e) {
                 super.keyPressed(e);                                
                 if (e.keyCode == 16777218 || e.keyCode == 13) { // sorry didn't find the SWT constant for down arrow
                     searchListViewer.getTable().setFocus();
                 } else if (e.keyCode == SWT.ESC) {
                     searchStr.setText("");
                 }
             }
         });
         
         searchListViewer = new TableViewer(panel, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
         searchListViewer.setContentProvider(contentProvider);
         data = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
         searchListViewer.getTable().setLayoutData(data);
         searchListViewer.getTable().addSelectionListener(new SelectionListener() {        
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         
             public void widgetSelected(SelectionEvent e) {     
                 showSelectedItem();
             }
         });        
         searchStr.addFocusListener(new FocusAdapter() {        
             public void focusGained(FocusEvent e) {
                 searchStr.selectAll();
             }        
         });
         
         // search result
         searchResult = new Browser(form, SWT.BORDER);
         
         form.setWeights(new int[]{1, 3});
         
         RubyPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
         	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
         		if (event.getProperty().equals(PreferenceConstants.RI_PATH)) {
         			updatePage();
         		}
         	}
         });
         
         pageBook.showPage(inProgressLabel);
         updatePage();
 		RDocUtility.addRdocListener(this);
 	}
 	    
 	private void contributeToActionBars() {
 		IAction refreshAction = new Action() {
 			public void run() {
 				updatePage();
 			}
 		};
 		refreshAction.setText("Refresh");
 		refreshAction.setToolTipText("Refresh list of names");
 		refreshAction.setImageDescriptor(RubyPluginImages.TOOLBAR_REFRESH);
 			
 		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
 		manager.add(refreshAction);		
 	}
 	
     private void updatePage() {
     	initSearchList();
         if( riFound ){
         	pageBook.showPage(form);
         }        
     }
     
     private void showSelectedItem() {
         String searchText = (String)((IStructuredSelection)searchListViewer.getSelection()).getFirstElement();        
         if (latestJob != null && latestJob.getState() != Job.NONE) {
         	latestJob.cancel();
         }
         latestJob = new RubyInvokerJob(new RIDescriptionUpdater(searchText));
         latestJob.setPriority(Job.INTERACTIVE);
         latestJob.schedule();
     }        
     
     public void dispose() {
         RDocUtility.removeRdocListener(this);
         super.dispose();
     }
         
     private synchronized void initSearchList() {        
         RubyInvoker invoker = new RIPopulator(this);
         Job job = new RubyInvokerJob(invoker);
         job.setPriority(Job.LONG);
         job.schedule();
 	}	
 	
 	private static class RubyInvokerJob extends Job {
 		private RubyInvoker invoker;
 
 		public RubyInvokerJob(RubyInvoker invoker) {
 			super("Updating RI View"); // $NON-NLS-1$
 			this.invoker = invoker;
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			invoker.invoke();
 			return Status.OK_STATUS;
 		}
 	}
 	
 
     private void filterSearchList() {               
         List<String> filteredList = new ArrayList<String>();
         String text = searchStr.getText();
         if (text != null && text.trim().length() > 0) {
         	for (String string : fgPossibleMatches) {
         		if (string.toLowerCase().indexOf(text.toLowerCase()) > -1 ) {
         			filteredList.add(string);                
         		}
 			}
         } else {
         	filteredList = fgPossibleMatches;
         }
         searchListViewer.setInput(filteredList);       
         if (filteredList.size() > 0) searchListViewer.getTable().setSelection(0);             
         showSelectedItem();
     }
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		form.setFocus();
 	}       
     
     private final class ListContentProvider implements IStructuredContentProvider {
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {                
         }
 
         public void dispose() {
         }
 
         public Object[] getElements(Object inputElement) {
             return ((Collection)inputElement).toArray();
         }
     }
 
     abstract class RubyInvoker {
         protected abstract List<String> getArgList();
         protected abstract void handleOutput(Process process);
         protected void beforeInvoke(){}
         
         public final void invoke() {
 
         	IPath riPath = new Path( RubyPlugin.getDefault().getPreferenceStore().getString( PreferenceConstants.RI_PATH ) );
         	
         	// check the ri path for existence. It might have been unconfigured
 			// and set to the default value or the file could have been removed
 			File file = new File(riPath.toOSString());
 
 			// If we can't find it ourselves then display an error to the user
 			if (!file.exists() || !file.isFile()) {
 				riFound = false;
 				pageBook.showPage(riNotFoundLabel);
 				return;
 			}
 			   		
    		try {        			
                 List<String> args = getArgList();
                 args.add(0, riPath.toString());
                String[] argArray= (String[]) args.toArray(new String[args.size()]);
                Process p= Runtime.getRuntime().exec(argArray);
                 handleOutput(p); 
     		} catch (IOException e)  {
     			// message of RuntimeException will be displayed in the RI View
     			throw new RuntimeException(e.getMessage(), e);
     		}      
         }
     }
     
     private class RIDescriptionUpdater extends RubyInvoker {
     	private String searchValue;
 		private final String HEADER = "<html><head></head><body>";
 		private final String TAIL = "</body></html>";
 		private StringBuffer buffer;
     	
     	RIDescriptionUpdater(String value) {
     		this.searchValue = value;
     	}
     	
         protected List<String> getArgList() {
             List<String> args = new ArrayList<String>();
             args.add("--no-pager");
             args.add("-f");
             args.add("html");
             args.add(searchValue);
             return args;
         }
 
         protected void beforeInvoke() {
             searchResult.setText(InfoViewMessages.getString("RubyInformation.please_wait"));
         }
         
 		void addToBuffer(int position, final String line) {			
 		    if (position < 0) position = 0;
 		    StringBuffer modifiedLine = new StringBuffer(line);
 		    if (!line.endsWith(">")) modifiedLine.append("<br/>");
 			modifiedLine.append("\r\n");				
 			buffer.insert(position, modifiedLine.toString());
 		}
 
         protected void handleOutput(final Process process) {
         	if (process == null)
 				return;
 			try {
 				buffer = new StringBuffer();
 				InputStreamReader isr = new InputStreamReader(process.getInputStream());
 				BufferedReader br = new BufferedReader(isr);
 				// Insert all the text
 				String line = null; // FIXME What do we do if this process hits an error?
 				while ((line = br.readLine()) != null) {
 					addToBuffer(buffer.length() - 1, line);
 				}
 				buffer.insert(0, HEADER); // Put the header before all the contents
 				buffer.append(TAIL); // Put the body and html close tags at end
 				final String text = buffer.toString();
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						searchResult.setText(text);
 					}
 				});
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
         }
     }
 
     /**
 	 * When teh rdoc has changed, automatically update/regenerate the view
 	 */
 	public void rdocChanged() {
 		updatePage();		
 	}
 	
 	private class RIPopulator extends RubyInvoker {
 		private RIView view;
 		
 		public RIPopulator(RIView view) {
 			this.view = view;
 		}
 		
 		@Override
 		protected List<String> getArgList() {
 			 List<String> args = new ArrayList<String>();
              args.add("--no-pager");
              args.add("-l");
              return args;
 		}
 
 		@Override
 		protected void handleOutput(Process process) {
 			if (process == null) return;
 			view.riFound = false;
             BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             String line = null;
             fgPossibleMatches = new ArrayList<String>();
             try {
                 while ((line = reader.readLine()) != null) {
                     fgPossibleMatches.add(line.trim());
                 }
                 // if not matches were found display an error message
                 if( fgPossibleMatches.size() == 0 ){
                 	view.riNotFound();
                 } else {
                 	view.riFound = true;
                 }
             }
             catch (IOException e) {
                 RubyPlugin.log(e);
             }		
             final Display display = Display.getDefault();
     		display.asyncExec (new Runnable () {
     		      public void run () {
     		    	filterSearchList();
     		    	if (riFound) pageBook.showPage(form);
     		      }
     		   });
 		}
 		
 	}
 	
 	void riNotFound() {
 		riFound = false;
 		pageBook.showPage( riNotFoundLabel );
 	}
 }
