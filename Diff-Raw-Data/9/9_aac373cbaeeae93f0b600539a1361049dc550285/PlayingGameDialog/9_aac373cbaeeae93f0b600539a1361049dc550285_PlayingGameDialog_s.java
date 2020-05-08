 package org.promasi.client.gui;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.nebula.widgets.cdatetime.CDT;
 import org.eclipse.nebula.widgets.cdatetime.CDateTime;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Decorations;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Shell;
 import org.joda.time.DateTime;
 import org.promasi.client.gui.company.Project;
 import org.promasi.client.gui.marketplace.MarketPlaceComposite;
 import org.promasi.client.gui.scheduler.SchedulerComposite;
 import org.promasi.game.IGame;
 import org.promasi.game.SerializableGameModel;
 import org.promasi.game.company.Company;
 import org.promasi.game.company.MarketPlace;
 import org.promasi.game.company.SerializableCompany;
 import org.promasi.game.company.SerializableEmployee;
 import org.promasi.game.company.SerializableEmployeeTask;
 import org.promasi.game.company.SerializableMarketPlace;
 import org.promasi.game.project.SerializableProject;
 import org.promasi.utilities.exceptions.NullArgumentException;
 import org.promasi.utilities.file.RootDirectory;
 import org.promasi.utilities.serialization.SerializationException;
 
 import org.eclipse.swt.layout.grouplayout.GroupLayout;
 import org.eclipse.swt.layout.grouplayout.LayoutStyle;
 
 /**
  * 
  * @author m1cRo
  *
  */
 public class PlayingGameDialog extends Dialog 
 {
 	/**
 	 * 
 	 */
 	public static final String CONST_NO_PROJECT_HTML_STRING="";
 	
 	/**
 	 * 
 	 */
 	protected Object _result;
 	
 	/**
 	 * 
 	 */
 	protected Shell _shell;
 	
 	/**
 	 * 
 	 */
 	private CDateTime _clock;
 	
 	/**
 	 * 
 	 */
 	private org.eclipse.swt.widgets.DateTime _calendar;
 	
 	/**
 	 * 
 	 */
 	private Label _budgetLabel;
 	
 	/**
 	 * 
 	 */
 	private IGame _game;
 	
 	/**
 	 * 
 	 */
 	private Composite _menu;
 	
 	/**
 	 * 
 	 */
 	private DateTime _currentDateTime;
 	
 	/**
 	 * 
 	 */
 	private Company _company;
 	
 	/**
 	 * 
 	 */
 	private MarketPlace _marketPlace;
 	
 	/**
 	 * 
 	 */
 	private MarketPlaceComposite _marketPlaceDialog;
 	
 	/**
 	 * 
 	 */
 	private Decorations _marketPlaceDecorations;
 	
 	/**
 	 * 
 	 */
 	private Decorations _scheduler;
 	
 	/**
 	 * 
 	 */
 	private double _progress;
 	
 	/**
 	 * 
 	 */
 	private double _budget;
 	
 	/**
 	 * 
 	 */
 	private Button _marketPlaceButton;
 	
 	/**
 	 * 
 	 */
 	private Browser _projectInfoBrowser;
 	
 	/**
 	 * 
 	 */
 	private Decorations _schedulerDecorations;
 	
 	/**
 	 * 
 	 */
 	private Button _plannerButton;
 	
 	/**
 	 * 
 	 */
 	private ProgressBar _progressBar;
 	
 	/**
 	 * 
 	 */
 	private Project _currentProject;
 	
 	/**
 	 * 
 	 */
 	private Composite _desktop;
 	
 	/**
 	 * 
 	 */
 	private Button _startButton ;
 	
 	/**
 	 * 
 	 */
 	private java.util.List<IDialogListener> _listeners;
 	
 	/**
 	 * Create the dialog.
 	 * @param parent
 	 * @param style
 	 */
 	public PlayingGameDialog(Shell parent, int style, IGame game)throws NullArgumentException {
 		super(parent, style);
 		if(game==null){
 			throw new NullArgumentException("Wrong argument game==null");
 		}
 		
 		setText("ProMaSi");
 		_game=game;
 		_listeners=new LinkedList<IDialogListener>();
 		createContents();
 	}
 
 	/**
 	 * Open the dialog.
 	 * @return the result
 	 */
 	public void open() {
 		if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 			_shell.getDisplay().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					_shell.open();
 					_shell.layout();
 					Display display = getParent().getDisplay();
 					while (!_shell.isDisposed()) {
 						if (!display.readAndDispatch()) {
 							display.sleep();
 						}
 					}
 					
 					synchronized (PlayingGameDialog.this) {
 						for(IDialogListener listener : _listeners){
 							listener.dialogClosed(PlayingGameDialog.this);
 						}
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	public void closeDialog(){
 		if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 			_shell.getDisplay().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					_shell.dispose();	
 				}
 			});
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void setupImages(){
 		try {
 			Image img=new Image(_shell.getDisplay(),RootDirectory.getInstance().getImagesDirectory()+"exit.png");
 			img=new Image(_shell.getDisplay(),RootDirectory.getInstance().getImagesDirectory()+"planner.png");
 			_plannerButton.setImage(img);
 			_plannerButton.setText("Scheduler");
 			
 			img=new Image(_shell.getDisplay(),RootDirectory.getInstance().getImagesDirectory()+"marketplace.png");
 			_marketPlaceButton.setImage(img);
 			_marketPlaceButton.setText("MarketPlace");
 			
 			img=new Image(_shell.getDisplay(),RootDirectory.getInstance().getImagesDirectory()+"exit.png");
 		} catch (IOException e) {
 			//Logger
 		}
 	}
 	
 	/**
 	 * Create contents of the dialog.
 	 */
 	private void createContents() {
 		_shell = new Shell(getParent(), SWT.SHELL_TRIM | SWT.BORDER | SWT.PRIMARY_MODAL);
 		_shell.setSize(1091, 679);
 		_shell.setText(getText());
 		_shell.setLocation(0, 0);
 		
 		_desktop = new Composite(_shell, SWT.NONE);
 		_desktop.setBackground(new Color(_shell.getDisplay(), new RGB(100, 100, 100)));
 		_desktop.addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseUp(MouseEvent arg0) {
 				_menu.setVisible(false);
 			}
 			
 			@Override
 			public void mouseDown(MouseEvent arg0) {
 				_menu.setVisible(false);
 			}
 			
 			@Override
 			public void mouseDoubleClick(MouseEvent arg0) {
 				_menu.setVisible(false);
 			}
 		});
 		
 		_menu = new Composite(_desktop, SWT.BORDER);
 		_menu.setVisible(false);
 		
 				_marketPlaceButton = new Button(_menu, SWT.TOGGLE);
 				_marketPlaceButton.setAlignment(SWT.LEFT);
 				_marketPlaceButton.setBounds(10, 218, 209, 38);
 				_marketPlaceButton.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						openMarketPlaceDialog();
 						_menu.setVisible(false);
 					
 					}
 				});
 				
 				
 				_plannerButton = new Button(_menu, SWT.TOGGLE);
 				_plannerButton.setAlignment(SWT.LEFT);
 				_plannerButton.setBounds(10, 262, 209, 38);
 				_plannerButton.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						openSchedulerDialog();
 						_menu.setVisible(false);
 					}
 				});
 				
 				_projectInfoBrowser = new Browser(_menu, SWT.NONE);
 				_projectInfoBrowser.setBounds(10, 30, 209, 182);
 				
 				_progressBar = new ProgressBar(_menu, SWT.NONE);
 				_progressBar.setBounds(10, 338, 209, 15);
 				
 				Label lblOverallProjgress = new Label(_menu, SWT.NONE);
 				lblOverallProjgress.setBounds(10, 319, 125, 13);
 				lblOverallProjgress.setText("Overall Progress");
 				Label lblCurrentProject = new Label(_menu, SWT.NONE);
 				lblCurrentProject.setBounds(10, 10, 154, 13);
 				
 						lblCurrentProject.setText("Current Project");
 				GroupLayout gl__desktop = new GroupLayout(_desktop);
 				gl__desktop.setHorizontalGroup(
 					gl__desktop.createParallelGroup(GroupLayout.LEADING)
 						.add(gl__desktop.createSequentialGroup()
 							.add(_menu, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
 							.addContainerGap(681, Short.MAX_VALUE))
 				);
 				gl__desktop.setVerticalGroup(
 					gl__desktop.createParallelGroup(GroupLayout.TRAILING)
 						.add(gl__desktop.createSequentialGroup()
 							.addContainerGap(534, Short.MAX_VALUE)
 							.add(_menu, GroupLayout.PREFERRED_SIZE, 362, GroupLayout.PREFERRED_SIZE))
 				);
 				_desktop.setLayout(gl__desktop);
 				
 				Composite taskBar = new Composite(_shell, SWT.NONE);
 				
 				_startButton = new Button(taskBar, SWT.FLAT);
 				_startButton.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						if(_menu.isVisible()){
 							_menu.setVisible(false);
 						}else{
 							_menu.setVisible(true);
 						}
 					}
 				});
 				
 				_startButton.setText("Start");
 				GroupLayout gl_taskBar = new GroupLayout(taskBar);
 				gl_taskBar.setHorizontalGroup(
 					gl_taskBar.createParallelGroup(GroupLayout.LEADING)
 						.add(gl_taskBar.createSequentialGroup()
 							.add(_startButton, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)
 							.addContainerGap(1221, Short.MAX_VALUE))
 				);
 				gl_taskBar.setVerticalGroup(
 					gl_taskBar.createParallelGroup(GroupLayout.LEADING)
 						.add(_startButton, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
 				);
 				taskBar.setLayout(gl_taskBar);
 				
 				Composite composite = new Composite(_shell, SWT.NONE);
 				
 				_clock = new CDateTime(composite, CDT.CLOCK_24_HOUR | CDT.SIMPLE | CDT.BUTTON_LEFT | CDT.TIME_SHORT);
 				_clock.setEditable(false);
 				
 				_calendar = new org.eclipse.swt.widgets.DateTime(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.CALENDAR);
 				
 				Label label = new Label(composite, SWT.NONE);
 				label.setText("Budget : ");
 				
 				_budgetLabel = new Label(composite, SWT.NONE);
 				_budgetLabel.setText("0");
 				GroupLayout gl__shell = new GroupLayout(_shell);
 				gl__shell.setHorizontalGroup(
 					gl__shell.createParallelGroup(GroupLayout.TRAILING)
 						.add(gl__shell.createSequentialGroup()
 							.addContainerGap()
 							.add(gl__shell.createParallelGroup(GroupLayout.LEADING)
 								.add(GroupLayout.TRAILING, gl__shell.createSequentialGroup()
 									.add(_desktop, GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
 									.addPreferredGap(LayoutStyle.RELATED)
 									.add(composite, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE)
 									.add(22))
 								.add(gl__shell.createSequentialGroup()
 									.add(taskBar, GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
 									.add(4))))
 				);
 				gl__shell.setVerticalGroup(
 					gl__shell.createParallelGroup(GroupLayout.LEADING)
 						.add(gl__shell.createSequentialGroup()
 							.addContainerGap()
 							.add(gl__shell.createParallelGroup(GroupLayout.TRAILING)
 								.add(composite, GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
 								.add(_desktop, GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE))
 							.addPreferredGap(LayoutStyle.RELATED)
 							.add(taskBar, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
 							.add(4))
 				);
 				GroupLayout gl_composite = new GroupLayout(composite);
 				gl_composite.setHorizontalGroup(
 					gl_composite.createParallelGroup(GroupLayout.TRAILING)
 						.add(gl_composite.createSequentialGroup()
 							.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 							.add(gl_composite.createParallelGroup(GroupLayout.LEADING)
 								.add(_clock, GroupLayout.PREFERRED_SIZE, 188, GroupLayout.PREFERRED_SIZE)
 								.add(_calendar, GroupLayout.PREFERRED_SIZE, 188, GroupLayout.PREFERRED_SIZE)
 								.add(gl_composite.createSequentialGroup()
 									.add(label, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(LayoutStyle.RELATED)
 									.add(_budgetLabel, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)))
 							.add(9))
 				);
 				gl_composite.setVerticalGroup(
 					gl_composite.createParallelGroup(GroupLayout.LEADING)
 						.add(gl_composite.createSequentialGroup()
 							.add(10)
 							.add(_clock, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE)
 							.add(22)
 							.add(_calendar, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE)
 							.add(18)
 							.add(gl_composite.createParallelGroup(GroupLayout.BASELINE)
 								.add(label, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
 								.add(_budgetLabel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
 							.add(148))
 				);
 				composite.setLayout(gl_composite);
 				_shell.setLayout(gl__shell);
 
 		setupImages();
 	}
 	
 	/**
 	 * 
 	 */
 	private void openSchedulerDialog(){
 		synchronized(this){
 			if(_company!=null && _currentProject!=null && _scheduler==null){
 				try {
 					if(_schedulerDecorations!=null){
 						_schedulerDecorations.dispose();
 						_schedulerDecorations=null;
 					}
 					
 					if(_marketPlaceDecorations!=null){
 						_marketPlaceDecorations.dispose();
 						_marketPlaceDecorations=null;
 					}
 					
 					_schedulerDecorations = new Decorations(_desktop, SWT.SHELL_TRIM);
 					_schedulerDecorations.setLayoutData(new GridData(GridData.FILL_BOTH));
 					_schedulerDecorations.setLayout(new FillLayout());
 					_schedulerDecorations.setBounds(new Rectangle(0,0,400,400));
 				    new SchedulerComposite(_schedulerDecorations,SWT.FILL,_game, _company, _currentProject);
 				    _schedulerDecorations.setVisible(true);
 				    _schedulerDecorations.setBounds(new Rectangle(0,0,600,400));
 				    _schedulerDecorations.setText("Scheduler");
 				    _schedulerDecorations.setFocus();
 				    _schedulerDecorations.layout();
 				} catch (IllegalArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (NullArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private void openMarketPlaceDialog(){
 		/*if(_marketPlaceDialog!=null){
 			_marketPlaceDialog.open();
 		}*/
 		
 		try {
 			if(_marketPlaceDecorations!=null){
 				_marketPlaceDecorations.dispose();
 				_marketPlaceDecorations=null;
 			}
 			
 			if(_schedulerDecorations!=null){
 				_schedulerDecorations.dispose();
 				_schedulerDecorations=null;
 			}
 			
 			_marketPlaceDecorations = new Decorations(_desktop, SWT.CLOSE);
 			_marketPlaceDecorations.setLayoutData(new GridData(GridData.FILL_BOTH));
 			_marketPlaceDecorations.setLayout(new FillLayout());
 			_marketPlaceDecorations.setBounds(new Rectangle(0,0,400,400));
 		    _marketPlaceDialog=new MarketPlaceComposite(_marketPlaceDecorations,SWT.FILL,_company,_marketPlace, _game);
 		    _marketPlaceDecorations.setVisible(true);
 		    _marketPlaceDecorations.setBounds(new Rectangle(0,0,650,500));
 		    _marketPlaceDecorations.setText("MarketPlace");
 		    _marketPlaceDecorations.setFocus();
 		    _marketPlaceDecorations.layout();
 
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NullArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public synchronized void projectAssigned(IGame game, SerializableCompany company, SerializableProject project, DateTime dateTime) {
 		try {
 			_progress=project.getOverallProgress();
 			_currentProject=new Project(project,dateTime);
 			if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 				_shell.getDisplay().syncExec(new Runnable() {
 					
 					@Override
 					public void run() {
 						if(_currentProject!=null){
 							_projectInfoBrowser.setText(_currentProject.getProjectDescription());
 						}
 					}
 				});
 			}
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NullArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	public synchronized void projectFinished(IGame game, SerializableCompany company, SerializableProject project, DateTime dateTime) {
 		_progress=0;
 		if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 			_shell.getDisplay().syncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					if(_scheduler!=null){
 						_scheduler.setEnabled(false);
 					}
 					
 					_projectInfoBrowser.setText(CONST_NO_PROJECT_HTML_STRING);
 				}
 			});
 		}
 		
 		_currentProject=null;
 		_scheduler=null;
 	}
 
 	public synchronized void employeeHired(IGame game, SerializableMarketPlace marketPlace,SerializableCompany company, SerializableEmployee employee,DateTime dateTime) {
 		try {
 			_company=company.getCompany();
 			_marketPlace=marketPlace.getMarketPlace();
 		}catch (SerializationException e) {
 			//Logger
 			return;
 		}
 		
 		if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 			_shell.getDisplay().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					if(_company!=null && _marketPlace!=null){
 						try {
							_marketPlaceDialog.updateMarketPlaceDialog(_company, _marketPlace);
 						} catch (SerializationException e) {
 							//Logger
 						} catch (NullArgumentException e) {
 							//Logger
 						}
 					}
 				}
 			});
 		}
 	}
 
 	public void employeeDischarged(IGame game, SerializableMarketPlace marketPlace,SerializableCompany company, SerializableEmployee employee,DateTime dateTime) {
 		try {
 			_company=company.getCompany();
 			_marketPlace=marketPlace.getMarketPlace();
 		}catch (SerializationException e) {
 			//Logger
 			return;
 		}
 		
 		if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 			_shell.getDisplay().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					if(_company!=null && _marketPlace!=null){
 						try {
							_marketPlaceDialog.updateMarketPlaceDialog(_company, _marketPlace);
 						} catch (SerializationException e) {
 							//Logger
 						} catch (NullArgumentException e) {
 							//Logger
 						}
 					}
 				}
 			});
 		}
 	}
 	
 	public void employeeTaskDetached(IGame game, SerializableMarketPlace marketPlace,
 			SerializableCompany company, SerializableEmployee employee,
 			SerializableEmployeeTask employeeTask,
 			DateTime dateTime) {
 		if(_company!=null && _marketPlace!=null){
 			try {
 				if(_marketPlace.hireEmployee(_company, employee.getEmployeeId())){
 					_marketPlaceDialog.updateMarketPlaceDialog(_company, _marketPlace);
 				}
 			} catch (NullArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}catch (SerializationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	public void companyIsInsolvent(IGame game, SerializableCompany company,DateTime dateTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onExecuteStep(IGame game, SerializableCompany company, SerializableProject assignedProject, DateTime dateTime) {
 		_budget=company.getBudget();
 		_progress=assignedProject.getOverallProgress();
 	}
 
 	public void onTick(IGame game, DateTime dateTime) {
 		_currentDateTime=dateTime;
 		if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 			_shell.getDisplay().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 						if(_currentDateTime!=null){
 							_clock.setSelection(_currentDateTime.toDate());
 							//_calendarCombo.setDate(_currentDateTime.toDate());
 							_calendar.setDate(_currentDateTime.getYear(), _currentDateTime.getMonthOfYear(), _currentDateTime.getDayOfMonth());
 							_budgetLabel.setText(String.format("%.2f$",_budget) );
 							_progressBar.setSelection((int) _progress);
 						}	
 					}
 				}
 			});
 		}
 	}
 
 	public void gameStarted(IGame game, SerializableGameModel gameModel, DateTime dateTime) {
 		try {
 			_marketPlace=gameModel.getMarketPlace().getMarketPlace();
 			_company=gameModel.getCompany().getCompany();
 			_budget=_company.getBudget();
 		} catch (IllegalArgumentException e) {
 			//Logger
 		}catch (SerializationException e) {
 			//Logger
 		}
 	}
 
 	public void employeeTasksAttached(IGame game, SerializableCompany company,
 			SerializableEmployee employee,
 			List<SerializableEmployeeTask> employeeTasks, DateTime dateTime) {
 		// TODO Auto-generated method stub	
 	}
 	
 	/**
 	 * 
 	 * @param gamesDialogListener
 	 * @return
 	 * @throws NullArgumentException
 	 */
 	public synchronized boolean registerGamesDialogListener(IDialogListener listener)throws NullArgumentException{
 		if(listener==null){
 			throw new NullArgumentException("Wrong argument listener==null");
 		}
 		
 		if(_listeners.contains(listener)){
 			return false;
 		}
 		
 		_listeners.add(listener);
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param gamesDialogListener
 	 * @return
 	 */
 	public synchronized boolean removeGamesDialogListener(IDialogListener listener)throws NullArgumentException{
 		if(listener==null){
 			throw new NullArgumentException("Wrong argument listener==null");
 		}
 		
 		if(!_listeners.contains(listener)){
 			return false;
 		}
 		
 		_listeners.remove(listener);
 		return true;
 	}
 }
