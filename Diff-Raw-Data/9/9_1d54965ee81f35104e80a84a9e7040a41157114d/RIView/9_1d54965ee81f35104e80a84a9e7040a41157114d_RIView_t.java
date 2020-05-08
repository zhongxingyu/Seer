 package org.rubypeople.rdt.internal.ui.infoviews;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.dialogs.MessageDialog;
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
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 import org.rubypeople.rdt.internal.launching.LaunchingPlugin;
 import org.rubypeople.rdt.internal.launching.StandardVMType;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.RubyPluginImages;
 import org.rubypeople.rdt.internal.ui.rdocexport.RDocUtility;
 import org.rubypeople.rdt.internal.ui.rdocexport.RdocListener;
 import org.rubypeople.rdt.launching.IVMInstall;
 import org.rubypeople.rdt.launching.IVMInstallChangedListener;
 import org.rubypeople.rdt.launching.PropertyChangeEvent;
 import org.rubypeople.rdt.launching.RubyRuntime;
 
 public class RIView extends ViewPart implements RdocListener, IVMInstallChangedListener {
 
 	private boolean riFound = false;
 	private PageBook pageBook;
     private SashForm form;    
 	private Text searchStr;
     private TableViewer searchListViewer;
     private Browser searchResult;
     private static List<String> fgPossibleMatches = new ArrayList<String>();
     private ListContentProvider contentProvider = new ListContentProvider();
 	private RubyInvokerJob latestJob;
 
 	/**
 	 * The constructor.
 	 */
 	public RIView() {
 		RubyRuntime.addVMInstallChangedListener(this);
 	}
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 */
 	public void createPartControl(Composite parent) {
 		contributeToActionBars();
 		
 		pageBook = new PageBook(parent, SWT.NONE);                       
         
         Label inProgressLabel = new Label( pageBook, SWT.LEFT | SWT.TOP | SWT.WRAP );
         inProgressLabel.setText(InfoViewMessages.RubyInformation_please_wait);
         
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
         try {
         	searchResult = new Browser(form, SWT.BORDER);
 		} catch (Exception e) {
 			MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to create embedded browser", "It appears that you do not have an embeddable browser. Please see http://www.eclipse.org/swt/faq.php#browserlinux for more information if you are on Linux.");
 		}
         
         form.setWeights(new int[]{1, 3});        
         
         pageBook.showPage(inProgressLabel);
         updatePage();
 		RDocUtility.addRdocListener(this);
 	}
 	    
 	private void contributeToActionBars() {
 		IAction refreshAction = new Action() {
 			public void run() {
 				clearCache();
 				updatePage();
 			}
 		};
 		refreshAction.setText(InfoViewMessages.RubyInformation_refresh);
 		refreshAction.setToolTipText(InfoViewMessages.RubyInformation_refresh_tooltip);
 		refreshAction.setImageDescriptor(RubyPluginImages.TOOLBAR_REFRESH);
 			
 		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
 		manager.add(refreshAction);		
 	}
 	
     protected void clearCache() {
 		File index = getCachedIndex();
 		if (index != null) {
 			index.delete();
 		}
 	}
 
 	private void updatePage() {
     	initSearchList();
     	Display.getDefault().asyncExec(new Runnable () {
 		      public void run () {
 		    	  if (riFound) {
 		    		  pageBook.showPage(form);
 		    	  }        
 		      }
 		 });
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
         RubyRuntime.removeVMInstallChangedListener(this);
         super.dispose();
     }
     
     protected File getCachedIndex() {
 		return getStateFile("ri.index");
 	}
     
     protected File getFRIIndexFile() {
 		return getStateFile(".fastri-index");
 	}
     
     private File getStateFile(String name) {
     	IPath location = RubyPlugin.getDefault().getStateLocation();
 		location = location.append(name);
 		return location.toFile();
     }
         
     private synchronized void initSearchList() {
     	File file = getCachedIndex();
     	if (file.exists()) {
     		try {
 				List<String> results = read(new FileReader(file));
 				fgPossibleMatches = Collections.unmodifiableList(results);
 				riFound = true;
 				 Display.getDefault().asyncExec(new Runnable () {
 	    		      public void run () {
 	    		    	filterSearchList();
 	    		    	if (riFound) pageBook.showPage(form);
 	    		      }
 	    		   });
 				return;
 			} catch (FileNotFoundException e) {
 				RubyPlugin.log(e);
 			}    		
     	} 
     	RubyInvoker invoker = new RIPopulator(this);
 		Job job = new RubyInvokerJob(invoker);
 		job.setPriority(Job.LONG);
 		job.schedule();
 	}	
 	
 	protected List<String> read(Reader reader) {
 		Set<String> results = new HashSet<String>();
 		BufferedReader reader2 = null;
 		try {
 			reader2 = new BufferedReader(reader);
 			String line = null;                  
 			while ((line = reader2.readLine()) != null) {
 				results.add(line.trim());
 			}			
 		} catch (IOException e) {
 			RubyPlugin.log(e);
 		} finally {
 			try {
 				if (reader2 != null) 
 					reader2.close();
 			} catch (IOException e) {
 				// ignore
 			}
 		}
 		List<String> list = new ArrayList<String>(results);
 		Collections.sort(list);
 		return list;
 	}
 
 	private static class RubyInvokerJob extends Job {
 		private RubyInvoker invoker;
 
 		public RubyInvokerJob(RubyInvoker invoker) {
 			super(InfoViewMessages.RubyInformation_update_job_title);
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
         	filteredList = new ArrayList<String>(fgPossibleMatches);
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
         protected abstract void handleOutput(String content);
         protected void beforeInvoke(){}
         
         public final void invoke() {
         	File file = getFRIIndexFile();
         	if (!file.exists()) {
         		List<String> commands = new ArrayList<String>();            	
             	commands.add("--index-file=\"" + file.getAbsolutePath() + "\"");
             	commands.add("-b");
         		String output = execAndReadOutput(getFastRiServerPath(), commands);
         	}
 		
         	List<String> commands = getArgList();        	
         	commands.add("--index-file=\"" + file.getAbsolutePath() + "\"");
 			String content = execAndReadOutput(getFastRiPath(), commands);			
 
 			// If we can't find it ourselves then display an error to the
 			// user
 			if (content == null) {
 				riFound = false;
 				PlatformUI.getWorkbench().getDisplay().asyncExec(
 						new Runnable() {
 							public void run() {
 								pageBook.showPage(riNotFoundLabel());
 							}
 						});
 				return;
 			}
 			handleOutput(content);    
         }
         
     	private String getFastRiServerPath() {
     		File file = LaunchingPlugin.getFileInPlugin(new Path("ruby/fastri-server"));
     		if (file == null || !file.exists() || !file.isFile()) return null;
     		return file.getAbsolutePath();
 		}
 		private String execAndReadOutput(String file, List<String> commands) {
 			if (file == null) return null;
     		StringBuffer buffer = new StringBuffer();
     		try {
     			List<String> line = new ArrayList<String>();
     			IVMInstall vm = RubyRuntime.getDefaultVMInstall();
     			if (vm == null) return "";
    			File executable = vm.getVMInstallType().findExecutable(vm.getInstallLocation());
     			if (executable.getName().contains("rubyw")) {
     				String name = executable.getName();
     				name = name.replace("rubyw", "ruby");
     				executable = new File(executable.getParent() + File.separator + name);
     			}
     			line.add(executable.getAbsolutePath());
     			line.add(file);
     			for (String command : commands) {
     				line.add(command);
     			}
     			File workingDirectory = new File(file).getParentFile();
     			String[] cmdLine = new String[line.size()];
     			cmdLine = line.toArray(cmdLine);
     			Process p = DebugPlugin.exec(cmdLine, workingDirectory);    			
     			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
     			String liner = null;    			
     			while ((liner = reader.readLine()) != null) {
     				buffer.append(liner);
     				buffer.append("\n");
     				if (!reader.ready()) {
     					Thread.yield();
     				}
     			}
     			p.waitFor();
     		} catch (CoreException e) {
     			RubyPlugin.log(e);
     			return "";
     		} catch (IOException e) {
     			RubyPlugin.log(e);
     			return "";
     		} catch (InterruptedException e) {
     			RubyPlugin.log(e);
 			}
     		buffer.deleteCharAt(buffer.length() - 1); // remove last \n
     		return buffer.toString();
     	}
         
     	private String getFastRiPath() {
     		File file = LaunchingPlugin.getFileInPlugin(new Path("ruby/fri"));
     		if (file == null || !file.exists() || !file.isFile()) return null;
     		return file.getAbsolutePath();
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
             args.add(searchValue);
             return args;
         }
 
         protected void beforeInvoke() {
             searchResult.setText(InfoViewMessages.RubyInformation_please_wait);
         }
         
 
         protected void handleOutput(final String content) {
         	if (content == null)
 				return;
 			buffer = new StringBuffer();
 			buffer.append(content.replace("\n", "<br/>"));
 			buffer.insert(0, HEADER); // Put the header before all the contents
 			buffer.append(TAIL); // Put the body and html close tags at end
 			final String text = buffer.toString();
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					searchResult.setText(text);
 				}
 			});
         }
     }
 
     /**
 	 * When the rdoc has changed, automatically update/regenerate the view
 	 */
 	public void rdocChanged() {
 		clearCache();
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
 		protected void handleOutput(String content) {
 			if (content == null) return;
 			view.riFound = false;
             BufferedReader reader = new BufferedReader(new StringReader(content));
             String line = null;
             fgPossibleMatches = read(new StringReader(content));
             // if no matches were found display an error message
             if (fgPossibleMatches.isEmpty()){
                 view.riNotFound();
             } else {
                 view.riFound = true;
                 cacheListings(content);
             }	
             Display.getDefault().asyncExec(new Runnable () {
     		      public void run () {
     		    	filterSearchList();
     		    	if (riFound) pageBook.showPage(form);
     		      }
     		   });
 		}
 
 		private void cacheListings(String content) {
 			File file = getCachedIndex();
 			FileWriter writer = null;
 			try {
 				file.createNewFile();
 				writer = new FileWriter(file);
 				writer.write(content);
 			} catch (IOException e) {
 				RubyPlugin.log(e);
 			} finally {
 				try {
 					if (writer != null)
 						writer.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 		}
 		
 	}
 	
 	void riNotFound() {
 		riFound = false;
 		Display.getDefault().asyncExec(new Runnable() {
 		
 			public void run() {
 				pageBook.showPage( riNotFoundLabel() );
 			}
 		
 		});
 		
 	}
 	
 	protected Label riNotFoundLabel() {
 		Label riNotFoundLabel = new Label( pageBook, SWT.LEFT | SWT.TOP | SWT.WRAP );
         riNotFoundLabel.setText(InfoViewMessages.bind(InfoViewMessages.RubyInformation_ri_not_found, RubyRuntime.getRI()));
         return riNotFoundLabel;
 	}
 
 	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
 		clearCache();
 		updatePage();		
 	}
 
 	public void vmAdded(IVMInstall newVm) {
 		// ignore		
 	}
 
 	public void vmChanged(PropertyChangeEvent event) {
 		// ignore		
 	}
 
 	public void vmRemoved(IVMInstall removedVm) {
 		// ignore		
 	}
 }
