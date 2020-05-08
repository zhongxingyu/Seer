 package ui.swing.mainScreen.periods;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JToolBar;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 import javax.swing.table.TableColumnModel;
 
 import net.java.balloontip.BalloonTip;
 import net.java.balloontip.TablecellBalloonTip;
 import net.java.balloontip.styles.RoundedBalloonStyle;
 import net.java.balloontip.utils.TimingUtils;
 
 import org.apache.log4j.Logger;
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.decorator.SortOrder;
 import org.jdesktop.swingx.renderer.DefaultTableRenderer;
 import org.jdesktop.swingx.renderer.StringValue;
 import org.reactive.Receiver;
 
 import periods.Period;
 import periods.PeriodsListener;
 import periodsInTasks.PeriodsInTasksSystem;
 import sun.swing.SwingUtilities2;
 import swing.JXTableImproved;
 import tasks.TaskView;
 import tasks.tasks.Tasks;
 import ui.swing.mainScreen.TaskList;
 import ui.swing.mainScreen.dragAndDrop.PeriodTransferable;
 import ui.swing.mainScreen.tasks.TaskSelectionListener;
 import ui.swing.tasks.SelectedTaskSource;
 import ui.swing.utils.SimpleInternalFrame;
 import ui.swing.utils.SwingUtils;
 import basic.HardwareClock;
 
 @SuppressWarnings( { "serial", "restriction" })
 public class PeriodsList extends SimpleInternalFrame implements
 		TaskSelectionListener {
 
 	private JXTable periodsTable;
 	private JButton addPeriodButton;
 	private JButton removePeriodButton;
 	private final SelectedTaskSource selectedTaskSource;
 	private final PeriodsTableModel periodsTableModel;
 	private final PeriodsListener listScrollListener;
 	private TaskView _selectedTask;
 	private final PeriodsInTasksSystem periodsSystem;
 	protected final PeriodsListModel model;
 	private final HardwareClock machineClock;
 	private final RemovePeriodsDialogController removePeriodsDialogController;
 	private final PeriodsTableWhiteboard periodsWhiteboard;
 	private final SelectedTaskSource selectedTask;
 	private final Tasks tasks;
 
 	public PeriodsList(final TaskList tasksList,
 			final SelectedTaskSource selectedTaskSource,
 			final PeriodsInTasksSystem periodsInTasks,
 			final PeriodsTableModel periodsTableModel,
 			final HardwareClock machineClock,
 			final RemovePeriodsDialogController removePeriodsDialogController,
 			final PeriodsTableWhiteboard periodsWhiteboard, SelectedTaskSource selectedTask, Tasks tasks) {
 
 		super("Selected task's periods");
 		this.selectedTaskSource = selectedTaskSource;
 		this.periodsSystem = periodsInTasks;
 		this.periodsTableModel = periodsTableModel;
 		this.machineClock = machineClock;
 		this.removePeriodsDialogController = removePeriodsDialogController;
 		this.periodsWhiteboard = periodsWhiteboard;
 		this.selectedTask = selectedTask;
 		this.tasks = tasks;
 		this.model = new PeriodsListModel(periodsSystem, selectedTaskSource);
 
 		initialize();
 		listScrollListener = listScrollListener();
 
 		tasksList.addTaskSelectionListener(this);
 
 	}
 
 	private void initialize() {
 		final JScrollPane scrollPane = getPeriodsTable();
 		scrollPane.setPreferredSize(new Dimension(100, 0));
 		add(scrollPane, BorderLayout.CENTER);
 
 		final JToolBar toolBar = getPeriodsToolbar();
 
 		setToolBar(toolBar);
 	}
 
 	private JToolBar getPeriodsToolbar() {
 		final JToolBar toolBar = new JToolBar();
 
 		addPeriodButton = new JButton("add");
 		removePeriodButton = new JButton("remove");
 
 		addPeriodButton.setFocusable(false);
 		removePeriodButton.setFocusable(false);
 
 		addPeriodButton.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(final ActionEvent e) {
 				addPeriod();
 			}
 		});
 
 		removePeriodButton
 				.addActionListener(new java.awt.event.ActionListener() {
 
 					public void actionPerformed(final ActionEvent e) {
 						removePeriods();
 					}
 				});
 
 		addPeriodButton.setToolTipText("Adds a new custom period");
 		toolBar.add(addPeriodButton);
 		toolBar.add(removePeriodButton);
 		return toolBar;
 	}
 
 	private JScrollPane getPeriodsTable() {
 		this.periodsTable = new JXTableImproved(this.periodsTableModel);
 		final JScrollPane scrollPane = new JScrollPane(this.periodsTable);
 
 		bindPeriodsTableToWhiteboard(periodsTable, scrollPane);
 
 		periodsTable.setFont(Font.decode(Font.MONOSPACED));
 		this.periodsTable.getSelectionModel().setSelectionMode(
 				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
 		periodsTable.setSortable(true);
 		periodsTable.setSortOrder(0, SortOrder.DESCENDING);
 
 		removePeriodsWithDelKey();
 
 		configureDragAndDropStuff();
 		adjustColumnsAppearance(this.periodsTable);
 
 		return scrollPane;
 	}
 
 	private void removePeriodsWithDelKey() {
 		periodsTable.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(final KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
 					removePeriods();
 				}
 			}
 		});
 	}
 
 	private void bindPeriodsTableToWhiteboard(final JTable table,
 			final JScrollPane scrollPane) {
 
 		periodsWhiteboard.messageBoard().addReceiver(new Receiver<String>() {
 			TablecellBalloonTip tablecellBalloonTip;
 
 			@Override
 			public void receive(final String value) {
 				if (tablecellBalloonTip != null) {
 					hideTableCellBalloonTipInSwingThread();
 				}
 
 				if (value == null || value.equals("")) {
 					return;
 				}
 
 				tablecellBalloonTip = showBallonTipInSwingThread(value);
 
 			}
 
 			private TablecellBalloonTip showBallonTipInSwingThread(
 					final String value) {
 
 				final Future<TablecellBalloonTip> future = SwingUtilities2
 						.submit(new Callable<TablecellBalloonTip>() {
 
 							@Override
 							public TablecellBalloonTip call() throws Exception {
 								final TablecellBalloonTip tableBalloon = new TablecellBalloonTip(
 										table, value, table.getSelectedRow(),
 										table.getSelectedColumn(),
 										new RoundedBalloonStyle(5, 5,
 												Color.WHITE, Color.BLACK),
 										BalloonTip.Orientation.LEFT_ABOVE,
 										BalloonTip.AttachLocation.ALIGNED, 40,
 										20, true);
 								tableBalloon.setViewport(scrollPane
 										.getViewport());
 
 								final int fiveSeconds = 5000;
 								TimingUtils.showTimedBalloon(tableBalloon,
 										fiveSeconds);
 
 								return tableBalloon;
 							}
 
 						});
 
 				try {
 					return future.get();
 				} catch (final InterruptedException e) {
 					throw new RuntimeException(e);
 				} catch (final ExecutionException e) {
 					throw new RuntimeException(e);
 				}
 
 			}
 
 			private void hideTableCellBalloonTipInSwingThread() {
 
 				final Runnable hide = new Runnable() {
 					public void run() {
 						tablecellBalloonTip.setVisible(false);
 					}
 				};
 
 				SwingUtils.invokeAndWaitOrCry(hide);
 			}
 		});
 	}
 
 	private void adjustColumnsAppearance(final JXTable table) {
 		final TableColumnModel columnModel = table.getColumnModel();
 		final int smallSize = 100;
 		final int mediumSize = 200;
 		columnModel.getColumn(0).setPreferredWidth(mediumSize);
 		columnModel.getColumn(0).setMaxWidth(mediumSize);
 		columnModel.getColumn(1).setPreferredWidth(smallSize);
 		columnModel.getColumn(1).setMaxWidth(smallSize);
 		columnModel.getColumn(2).setPreferredWidth(smallSize);
 		columnModel.getColumn(2).setMaxWidth(smallSize);
 		columnModel.getColumn(3).setPreferredWidth(smallSize);
 		columnModel.getColumn(3).setMaxWidth(smallSize);
 		columnModel.getColumn(3).setCellRenderer(
 				new DefaultTableRenderer(StringValue.TO_STRING,
 						SwingConstants.RIGHT));
 	}
 
 	private void configureDragAndDropStuff() {
 		this.periodsTable.setDragEnabled(true);
 
 		this.periodsTable.setTransferHandler(new TransferHandler() {
 
 			@Override
 			public int getSourceActions(final JComponent arg0) {
 				return MOVE;
 			}
 
 			@Override
 			protected Transferable createTransferable(final JComponent arg0) {
 				String idOfSelectedTask = tasks.idOf(selectedTask.currentValue()).getId();
 				return new PeriodTransferable(idOfSelectedTask,  selectedPeriodIndex());
 			}
 
 		});
 	}
 
 	public void selectionChangedTo(final TaskView selectedTask) {
 		if (_selectedTask != null) {
 			_selectedTask.removePeriodListener(listScrollListener);
 		}
 
 		this._selectedTask = selectedTask;
 
 		if (_selectedTask == null) {
 			return;
 		}
 
 		// selectAndScrollToRow(0);
 		selectedTask.addPeriodsListener(listScrollListener);
 	}
 
 	private PeriodsListener listScrollListener() {
 		return new PeriodsListener() {
 
 			@Override
 			public void periodAdded(final Period period) {
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						selectAndScrollToRow(0);
 					}
 				});
 			}
 
 			@Override
 			public void periodRemoved(final Period period) {
 			}
 
 			@Override
 			public void clean() {
 			}
 
 		};
 	}
 
 	void selectAndScrollToRow(final int row) {
 
 		if (row > periodsTable.getRowCount()) {
 			return;
 		}
 
 		try {
 			this.periodsTable.getSelectionModel().setSelectionInterval(row, row);			
 			this.periodsTable.scrollRectToVisible(this.periodsTable.getCellRect(
 					row, 0, true));
 		} catch (ArrayIndexOutOfBoundsException knownIndexOutOfbounds){
 			Logger.getLogger(PeriodsList.class).error("Known IndexOutOfbounds in setSelectionTo", knownIndexOutOfbounds);
 		}
 	}
 
 	public List<Period> selectedPeriods() {
 
 		final int[] selectedRows = this.periodsTable.getSelectedRows();
 
 		final boolean noneSelected = selectedRows == null
 				|| selectedRows.length == 0 || selectedRows[0] == -1;
 		if (noneSelected) {
 			return new ArrayList<Period>();
 		}
 
 		final List<Period> selectedPeriods = new ArrayList<Period>();
 		for (final int index : selectedRows) {
 			selectedPeriods.add(periodsTableModel.getPeriod(periodsTable
 					.convertRowIndexToModel(index)));
 		}
 
 		return selectedPeriods;
 	}
 
 	public void setSelectedPeriodTesting(final int index) {
 		this.periodsTable.getSelectionModel()
 				.setSelectionInterval(index, index);
 
 	}
 
 	public void clickOnAddPediodButton() {
 		addPeriodButton.doClick();
 	}
 
 	private void addPeriod() {
 		try {
 			addPeriodButton.setCursor(new Cursor(Cursor.WAIT_CURSOR));
 			enqueueAddPeriod();
 		} finally {
 			addPeriodButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 		}
 
 	}
 
 	private void enqueueAddPeriod() {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				final TaskView selectedTask = selectedTaskSource.currentValue();
 
 				final Period newPeriod = new Period(machineClock.getTime());
 				periodsSystem.addPeriod(selectedTask, newPeriod);
 
 			}
 		});
 	}
 
 	public int selectedPeriodIndex() {
 		final TaskView selectedTask = selectedTaskSource.currentValue();
		return selectedTask.periods().indexOf(selectedPeriods().get(0));
 	}
 
	

 	private void removePeriods() {
 		final List<Period> selectedPeriods = selectedPeriods();
 
 		if (selectedPeriods.isEmpty()) {
 			PeriodsLogger.logger().error(
 					"Trying to remove periods with none selected");
 			return;
 		}
 
 		removePeriodsDialogController.confirmPeriodsRemoval(selectedPeriods);
 	}
 
 }
