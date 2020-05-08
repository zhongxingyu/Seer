 /*
  * 
  */
 package org.opendarts.ui.x01.defi.editor;
 
 import java.text.DecimalFormat;
 import java.text.MessageFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jface.action.ControlContribution;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.events.IExpansionListener;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.menus.IMenuService;
 import org.opendarts.core.model.dart.IDartsThrow;
 import org.opendarts.core.model.game.GameEvent;
 import org.opendarts.core.model.game.IGameEntry;
 import org.opendarts.core.model.game.IGameListener;
 import org.opendarts.core.model.player.IComputerPlayer;
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.service.game.IGameService;
 import org.opendarts.core.x01.defi.model.GameX01Defi;
 import org.opendarts.core.x01.defi.model.GameX01DefiDefinition;
 import org.opendarts.core.x01.model.GameX01Entry;
 import org.opendarts.core.x01.model.WinningX01DartsThrow;
 import org.opendarts.ui.dialog.ThreeDartsComputerDialog;
 import org.opendarts.ui.player.label.PlayerLabelProvider;
 import org.opendarts.ui.pref.IGeneralPrefs;
 import org.opendarts.ui.utils.ColumnDescriptor;
 import org.opendarts.ui.utils.OpenDartsFormsToolkit;
 import org.opendarts.ui.utils.listener.CancelTraverseListener;
 import org.opendarts.ui.utils.listener.FixHeightListener;
 import org.opendarts.ui.utils.listener.GrabColumnsListener;
 import org.opendarts.ui.x01.ISharedImages;
 import org.opendarts.ui.x01.X01UiPlugin;
 import org.opendarts.ui.x01.defi.dialog.DartsComputerX01DefiDialog;
 import org.opendarts.ui.x01.defi.label.DefiTurnLabelProvider;
 import org.opendarts.ui.x01.dialog.ShortcutsTooltip;
 import org.opendarts.ui.x01.editor.GameX01ContentProvider;
 import org.opendarts.ui.x01.editor.ScoreX01EditingSupport;
 import org.opendarts.ui.x01.label.ScoreLabelProvider;
 import org.opendarts.ui.x01.pref.IX01Prefs;
 import org.opendarts.ui.x01.utils.TextInputListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class GameX01Page.
  */
 public class GameX01DefiPage extends FormPage implements IFormPage,
 		IGameListener, IExpansionListener {
 
 	/** The logger. */
 	private static final Logger LOG = LoggerFactory
 			.getLogger(GameX01DefiPage.class);
 
 	/** The Constant SCORE_FORMAT. */
 	private static final NumberFormat SCORE_FORMAT = DecimalFormat
 			.getIntegerInstance();
 
 	/** The toolkit. */
 	private OpenDartsFormsToolkit toolkit;
 
 	/** The game. */
 	private final GameX01Defi game;
 
 	/** The score. */
 	private Text txtScoreLeft;
 
 	/** The player score input. */
 	private final Map<IPlayer, Text> playerScoreInput;
 
 	/** The player score. */
 	private final Map<IPlayer, TableViewerColumn> playerColumn;
 
 	/** The score viewer. */
 	private final Map<IPlayer, TableViewer> scoreViewers;
 
 	/** The game service. */
 	private final IGameService gameService;
 
 	/** The body. */
 	private Composite body;
 
 	/** The managed form. */
 	private IManagedForm mForm;
 
 	/** The dirty. */
 	private boolean dirty;
 
 	/** The game definition. */
 	private final GameX01DefiDefinition gameDefinition;
 
 	/** The player label provider. */
 	private final PlayerLabelProvider playerLabelProvider;
 
 	private final List<IPlayer> players;
 
 	/** The game editor. */
 	private final DefiX01Editor gameEditor;
 
 	/** The txt during. */
 	private Text txtDuration;
 
 	/** The txt forecast. */
 	private Text txtForecast;
 
 	/** The txt forecast. */
 	private Text txtTimeLeft;
 
 	/**
 	 * Instantiates a new game page.
 	 *
 	 * @param gameEditor the game editor
 	 * @param game the game
 	 * @param index the index
 	 */
 	public GameX01DefiPage(DefiX01Editor gameEditor, GameX01Defi game, int index) {
 		super(gameEditor, String.valueOf(index), "Game #" + index);
 		this.game = game;
 		this.gameEditor = gameEditor;
 		this.playerLabelProvider = new PlayerLabelProvider();
 		this.gameDefinition = (GameX01DefiDefinition) this.game.getParentSet()
 				.getGameDefinition();
 		this.playerScoreInput = new HashMap<IPlayer, Text>();
 		this.playerColumn = new HashMap<IPlayer, TableViewerColumn>();
 		this.gameService = gameEditor.getSet().getGameService();
 		this.scoreViewers = new HashMap<IPlayer, TableViewer>();
 
 		// Players
 		if (X01UiPlugin.getX01Preferences().getBoolean(
 				IX01Prefs.SWITCH_USER_POSITION)) {
 			this.players = this.game.getPlayers();
 		} else {
 			this.players = this.gameDefinition.getInitialPlayers();
 		}
 	}
 
 	/**
 	 * Gets the first player.
 	 *
 	 * @return the first player
 	 */
 	protected IPlayer getFirstPlayer() {
 		return this.players.get(0);
 	}
 
 	/**
 	 * Gets the second player.
 	 *
 	 * @return the second player
 	 */
 	protected IPlayer getSecondPlayer() {
 		return this.players.get(1);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
 	 */
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		this.mForm = managedForm;
 		// form
 		ScrolledForm form = managedForm.getForm();
 		this.toolkit = OpenDartsFormsToolkit.getToolkit();
 		form.setText(this.game.getName());
 
 		this.toolkit.decorateFormHeading(form.getForm());
 
 		GridDataFactory playerData;
 		GridDataFactory scoreData;
 		scoreData = GridDataFactory.fillDefaults().grab(true, false).span(2, 1);
 
 		boolean onePlayer = (this.players.size() == 1);
 		boolean twoPlayer = (this.players.size() == 2);
 		// body
 		int nbCol;
 		int tableSpan;
 		if (onePlayer) {
 			nbCol = 2;
 			tableSpan = 1;
 			playerData = GridDataFactory.fillDefaults().grab(false, true);
 			scoreData = GridDataFactory.fillDefaults().grab(true, false);
 		} else if (twoPlayer) {
 			nbCol = 4;
 			tableSpan = 2;
 			playerData = GridDataFactory.fillDefaults().grab(false, true);
 			scoreData = GridDataFactory.fillDefaults().grab(true, false)
 					.span(2, 1);
 		} else {
 			nbCol = this.players.size();
 			tableSpan = nbCol;
 			playerData = GridDataFactory.fillDefaults().grab(false, true);
 			scoreData = GridDataFactory.fillDefaults().grab(true, false);
 		}
 		this.body = form.getBody();
 		GridLayoutFactory.fillDefaults().margins(2, 2).numColumns(nbCol)
 				.equalWidth(true).applyTo(this.body);
 
 		if (onePlayer || twoPlayer) {
 			// First Player Status
 			Composite cmpPlayerOne = this.createPlayerComposite(this.body,
 					this.getFirstPlayer());
 			playerData.copy().applyTo(cmpPlayerOne);
 		} else {
 			// create multi player stats
 		}
 
 		// Score
 		Composite cmpScore = this.createScoreTableComposite(this.body);
 		GridDataFactory.fillDefaults().grab(true, true).span(tableSpan, 1)
 				.applyTo(cmpScore);
 
 		if (twoPlayer) {
 			// Second Player Status
 			Composite cmpPlayerTwo = this.createPlayerComposite(this.body,
 					this.getSecondPlayer());
 			playerData.copy().applyTo(cmpPlayerTwo);
 		}
 
 		// Left score
 		Composite leftScoreMain = this.createLeftScoreComposite(nbCol,
 				scoreData);
 		GridDataFactory.fillDefaults().grab(true, false).span(nbCol, 1)
 				.applyTo(leftScoreMain);
 
 		// Toolbar
 		ToolBarManager manager = (ToolBarManager) form.getToolBarManager();
 		IMenuService menuService = (IMenuService) this.getSite().getService(
 				IMenuService.class);
 		menuService.populateContributionManager(manager,
 				"toolbar:openwis.editor.game.defi.toolbar");
 		manager.add(new ControlContribution("openwis.editor.game.toolbar.help") {
 			@Override
 			protected Control createControl(Composite parent) {
 				Label helpLabel = new Label(parent, SWT.NONE);
 				helpLabel.setImage(X01UiPlugin.getImage(ISharedImages.IMG_HELP));
 				ToolTip toolTip = new ShortcutsTooltip(helpLabel);
 				toolTip.setPopupDelay(0);
 				return helpLabel;
 			}
 		});
 		manager.update(true);
 
 		// Register listener
 		this.game.addListener(this);
 
 		// initialize game
 		for (TableViewer viewer : this.scoreViewers.values()) {
 			viewer.setInput(this.game);
 		}
 		this.handlePlayer(this.game.getCurrentPlayer(),
 				this.game.getCurrentEntry());
 	}
 
 	/**
 	 * Creates the left score composite.
 	 *
 	 * @param nbCol the nb col
 	 * @param scoreData the score data
 	 * @return the composite
 	 */
 	private Composite createLeftScoreComposite(int nbCol,
 			GridDataFactory scoreData) {
 
 		int style = SWT.NONE;
 		if (X01UiPlugin.getX01Preferences().getBoolean(
 				IX01Prefs.SHOW_SCORE_LEFT)) {
 			style = ExpandableComposite.EXPANDED;
 		}
 
 		ExpandableComposite leftScoreMain = this.toolkit
 				.createExpandableComposite(this.body,
 						ExpandableComposite.TWISTIE | style
 								| ExpandableComposite.NO_TITLE);
 		GridLayoutFactory.fillDefaults().applyTo(leftScoreMain);
 		GridDataFactory.fillDefaults().span(nbCol, 1).grab(true, true)
 				.applyTo(leftScoreMain);
 
 		Composite leftScoreBody = this.toolkit.createComposite(leftScoreMain);
 		GridLayoutFactory.fillDefaults().numColumns(2)
 				.extendedMargins(0, 0, 6, 0).applyTo(leftScoreBody);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(leftScoreBody);
 
 		// Score left
 		this.txtScoreLeft = this.toolkit
 				.createText(leftScoreBody, this.getCurrentScore(),
 						SWT.READ_ONLY | SWT.CENTER | SWT.BORDER);
 		this.txtScoreLeft.setFont(OpenDartsFormsToolkit
 				.getFont(IGeneralPrefs.FONT_SCORE_LEFT));
 		GridDataFactory.fillDefaults().grab(true, true)
 				.applyTo(this.txtScoreLeft);
 
 		// Clock
 		Composite cmpClock = this.toolkit.createComposite(leftScoreBody);
 		GridDataFactory.fillDefaults().grab(false, true).applyTo(cmpClock);
 		this.createClockComposite(cmpClock);
 
 		this.toolkit.paintBordersFor(cmpClock);
 		leftScoreMain.setClient(leftScoreBody);
 		leftScoreMain.addExpansionListener(this);
 		return leftScoreMain;
 	}
 
 	/**
 	 * Creates the clock composite.
 	 *
 	 * @param parent the left score body
 	 * @return the composite
 	 */
 	private void createClockComposite(Composite parent) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);
 
 		Label label;
 		// Duration 
 		label = this.toolkit.createLabel(parent, "Time spend: ");
 		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
 		label.setFont(OpenDartsFormsToolkit.getFont(IGeneralPrefs.FONT_STATS));
 
 		this.txtDuration = this.toolkit.createText(parent, "00:00:00",
 				SWT.BORDER | SWT.READ_ONLY);
 		this.txtDuration.setFont(OpenDartsFormsToolkit
 				.getFont(IGeneralPrefs.FONT_STATS_LABEL));
 		GridDataFactory.fillDefaults().grab(true, false)
 				.applyTo(this.txtDuration);
 
 		// Time left
 		label = this.toolkit.createLabel(parent, "Time left: ");
 		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
 		label.setFont(OpenDartsFormsToolkit.getFont(IGeneralPrefs.FONT_STATS));
 
		this.txtTimeLeft = this.toolkit.createText(parent, "00:00:00",
 				SWT.BORDER | SWT.READ_ONLY);
 		this.txtTimeLeft.setFont(OpenDartsFormsToolkit
 				.getFont(IGeneralPrefs.FONT_STATS_LABEL));
 		GridDataFactory.fillDefaults().grab(true, false)
 				.applyTo(this.txtTimeLeft);
 
 		
 		// Forecast 
 		label = this.toolkit.createLabel(parent, "Forecast: ");
 		label.setFont(OpenDartsFormsToolkit.getFont(IGeneralPrefs.FONT_SCORE_INPUT));
 
		this.txtForecast = this.toolkit.createText(parent, "00:00:00",
 				SWT.BORDER | SWT.READ_ONLY);
 		this.txtForecast.setFont(OpenDartsFormsToolkit
 				.getFont(IGeneralPrefs.FONT_SCORE_INPUT));
 		GridDataFactory.fillDefaults().grab(true, false)
 				.applyTo(this.txtForecast);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#setFocus()
 	 */
 	@Override
 	public void setFocus() {
 		Text text = this.playerScoreInput.get(this.game.getCurrentPlayer());
 		if (text != null) {
 			text.setFocus();
 		} else {
 			super.setFocus();
 		}
 	}
 
 	/**
 	 * Creates the score composite.
 	 *
 	 * @param parent the parent
 	 * @return the composite
 	 */
 	private Composite createScoreTableComposite(Composite parent) {
 		Composite main = this.toolkit.createComposite(parent);
 
 		int nbPlayers = this.players.size();
 		boolean onePlayer = (nbPlayers == 1);
 		boolean twoPlayer = (nbPlayers == 2);
 
 		if (twoPlayer) {
 			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(main);
 		} else {
 			GridLayoutFactory.fillDefaults().numColumns(nbPlayers)
 					.applyTo(main);
 		}
 
 		TableViewer viewer;
 		Table table;
 		if (onePlayer) {
 			Composite comp = this.toolkit.createComposite(main);
 			GridLayoutFactory.fillDefaults().applyTo(comp);
 			GridDataFactory.fillDefaults().span(2, 4).grab(true, true)
 					.applyTo(comp);
 
 			// Table
 			table = this.toolkit.createTable(comp, SWT.V_SCROLL | SWT.BORDER
 					| SWT.FULL_SELECTION);
 			GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
 			table.setHeaderVisible(true);
 			table.setLinesVisible(true);
 			table.setFont(OpenDartsFormsToolkit
 					.getFont(IGeneralPrefs.FONT_SCORE_SHEET));
 
 			// resize the row height using a MeasureItem listener
 			table.addListener(SWT.MeasureItem, new FixHeightListener());
 			viewer = new TableViewer(table);
 			viewer.setContentProvider(new GameX01ContentProvider());
 			List<ColumnDescriptor> columns = this.addColumns(
 					this.getFirstPlayer(), viewer);
 			viewer.getControl().addControlListener(
 					new GrabColumnsListener(viewer, columns));
 
 			this.scoreViewers.put(this.getFirstPlayer(), viewer);
 
 		} else if (twoPlayer) {
 			Composite comp = this.toolkit.createComposite(main);
 			GridLayoutFactory.fillDefaults().applyTo(comp);
 			GridDataFactory.fillDefaults().span(2, 4).grab(true, true)
 					.applyTo(comp);
 
 			// Table
 			table = this.toolkit.createTable(comp, SWT.V_SCROLL | SWT.BORDER
 					| SWT.FULL_SELECTION);
 			GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
 			table.setHeaderVisible(true);
 			table.setLinesVisible(true);
 			table.setFont(OpenDartsFormsToolkit
 					.getFont(IGeneralPrefs.FONT_SCORE_SHEET));
 
 			// resize the row height using a MeasureItem listener
 			table.addListener(SWT.MeasureItem, new FixHeightListener());
 			viewer = new TableViewer(table);
 			viewer.setContentProvider(new GameX01ContentProvider());
 			List<ColumnDescriptor> columns = this.addColumns(null, viewer);
 
 			viewer.getControl().addControlListener(
 					new GrabColumnsListener(viewer, columns));
 
 			this.scoreViewers.put(this.getFirstPlayer(), viewer);
 			this.scoreViewers.put(this.getSecondPlayer(), viewer);
 
 		} else {
 			Section section;
 			Composite client;
 			for (IPlayer player : this.players) {
 				// Section
 				section = this.toolkit.createSection(main,
 						ExpandableComposite.TITLE_BAR);
 				GridDataFactory.fillDefaults().grab(true, true)
 						.applyTo(section);
 				if (this.game.getFirstPlayer().equals(player)) {
 					section.setText("* "
 							+ this.playerLabelProvider.getText(player));
 				} else {
 					section.setText(player.getName());
 				}
 				section.setFont(OpenDartsFormsToolkit
 						.getFont(IGeneralPrefs.FONT_SCORE_SHEET_LEFT));
 
 				// Section body
 				client = this.toolkit.createComposite(section, SWT.WRAP);
 				GridLayoutFactory.fillDefaults().applyTo(client);
 
 				this.addPlayerStatSection(client, player);
 
 				Composite comp = this.toolkit.createComposite(client);
 				GridLayoutFactory.fillDefaults().applyTo(comp);
 				GridDataFactory.fillDefaults().grab(true, true).applyTo(comp);
 
 				// Table
 				table = this.toolkit.createTable(comp, SWT.V_SCROLL
 						| SWT.BORDER | SWT.FULL_SELECTION);
 				GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
 				table.setHeaderVisible(true);
 				table.setLinesVisible(true);
 				table.setFont(OpenDartsFormsToolkit
 						.getFont(IGeneralPrefs.FONT_SCORE_SHEET));
 
 				// resize the row height using a MeasureItem listener
 				table.addListener(SWT.MeasureItem, new FixHeightListener());
 				viewer = new TableViewer(table);
 				viewer.setContentProvider(new GameX01ContentProvider());
 				List<ColumnDescriptor> columns = this
 						.addColumns(player, viewer);
 
 				viewer.getControl().addControlListener(
 						new GrabColumnsListener(viewer, columns));
 				this.scoreViewers.put(player, viewer);
 
 				// End section definition
 				this.toolkit.paintBordersFor(client);
 				section.setClient(client);
 				section.layout(true);
 			}
 		}
 		this.toolkit.paintBordersFor(main);
 
 		// Score input
 		for (IPlayer player : this.players) {
 			this.createInputScoreText(main, player);
 		}
 		return main;
 	}
 
 	/**
 	 * Adds the player stat section.
 	 *
 	 * @param parent the parent
 	 * @param player the player
 	 */
 	private void addPlayerStatSection(Composite parent, IPlayer player) {
 		// Section
 		Section section = this.toolkit.createSection(parent,
 				ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
 		section.setText("Statistics");
 
 		// Section body
 		Composite client = this.toolkit.createComposite(section, SWT.WRAP);
 		GridLayoutFactory.fillDefaults().applyTo(client);
 
 		// Player Status
 		PlayerDefiStatusComposite cmpStatus = new PlayerDefiStatusComposite(
 				client, player, this.game);
 		GridDataFactory.fillDefaults().grab(true, false)
 				.applyTo(cmpStatus.getControl());
 
 		// End section definition
 		this.toolkit.paintBordersFor(client);
 		section.setClient(client);
 	}
 
 	/**
 	 * Creates the input score text.
 	 *
 	 * @param main the main
 	 * @param player the player
 	 */
 	private void createInputScoreText(Composite main, IPlayer player) {
 		Text inputScoreText = this.toolkit.createText(main, "", SWT.CENTER
 				| SWT.BORDER);
 		inputScoreText.setFont(OpenDartsFormsToolkit
 				.getFont(IGeneralPrefs.FONT_SCORE_INPUT));
 		inputScoreText.setEnabled(false);
 		this.playerScoreInput.put(player, inputScoreText);
 
 		// layout
 		int indent = FieldDecorationRegistry.getDefault()
 				.getMaximumDecorationWidth() + 2;
 		GridDataFactory.fillDefaults().grab(true, false)
 				.indent(indent, SWT.DEFAULT).applyTo(inputScoreText);
 
 		// decoration
 		ControlDecoration dec = new ControlDecoration(inputScoreText, SWT.TOP
 				| SWT.LEFT);
 
 		// listener
 		TextInputListener listener = new TextInputListener(this.getSite()
 				.getShell(), inputScoreText, this.game, player, dec);
 		inputScoreText.addKeyListener(listener);
 
 		inputScoreText.addTraverseListener(new CancelTraverseListener());
 	}
 
 	/**
 	 * Adds the columns.
 	 * @param viewer 
 	 * @param player2 
 	 * @return 
 	 */
 	private List<ColumnDescriptor> addColumns(IPlayer player, TableViewer viewer) {
 		List<ColumnDescriptor> result = new ArrayList<ColumnDescriptor>();
 
 		ColumnDescriptor colDescr = new ColumnDescriptor("");
 		colDescr.width(60);
 		colDescr.labelProvider(new DefiTurnLabelProvider());
 
 		if (player == null) {
 			// Two player
 			result.addAll(this.createPlayerColumns(viewer,
 					this.getFirstPlayer()));
 
 			this.toolkit.createTableColumn(viewer, colDescr);
 			result.add(colDescr);
 			result.addAll(this.createPlayerColumns(viewer,
 					this.getSecondPlayer()));
 		} else {
 			this.toolkit.createTableColumn(viewer, colDescr);
 			result.add(colDescr);
 			result.addAll(this.createPlayerColumns(viewer, player));
 		}
 		return result;
 	}
 
 	/**
 	 * Creates the player columns.
 	 * @param viewer 
 	 *
 	 * @param player the player
 	 */
 	private List<ColumnDescriptor> createPlayerColumns(TableViewer viewer,
 			IPlayer player) {
 		List<ColumnDescriptor> result = new ArrayList<ColumnDescriptor>();
 		Shell shell = this.getSite().getShell();
 
 		TableViewerColumn column;
 		ColumnDescriptor colDescr;
 		int width = 80;
 
 		// Scored
 		colDescr = new ColumnDescriptor(player.getName());
 		colDescr.width(width);
 		colDescr.labelProvider(new ScoreLabelProvider(player));
 		colDescr.editingSupport(new ScoreX01EditingSupport(shell, this.game,
 				player, viewer));
 		column = this.toolkit.createTableColumn(viewer, colDescr);
 		result.add(colDescr);
 		this.playerColumn.put(player, column);
 
 		return result;
 	}
 
 	/**
 	 * Creates the player composite.
 	 *
 	 * @param parent the parent
 	 * @param iPlayer the i player
 	 * @return the composite
 	 */
 	private Composite createPlayerComposite(Composite parent, IPlayer player) {
 		Composite main = this.toolkit.createComposite(parent);
 		GridLayoutFactory.fillDefaults().applyTo(main);
 
 		Section secPlayer = this.toolkit.createSection(main,
 				ExpandableComposite.TITLE_BAR);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(secPlayer);
 		if (this.game.getFirstPlayer().equals(player)) {
 			secPlayer.setText("* " + this.playerLabelProvider.getText(player));
 		} else {
 			secPlayer.setText(this.playerLabelProvider.getText(player));
 		}
 		secPlayer.setFont(OpenDartsFormsToolkit
 				.getFont(IGeneralPrefs.FONT_SCORE_SHEET_LEFT));
 
 		Composite client = this.toolkit.createComposite(secPlayer, SWT.WRAP);
 		GridLayoutFactory.fillDefaults().applyTo(client);
 
 		// Status
 		PlayerDefiStatusComposite cmpStatus = new PlayerDefiStatusComposite(
 				client, player, this.game);
 		GridDataFactory.fillDefaults().grab(true, false)
 				.applyTo(cmpStatus.getControl());
 
 		secPlayer.setClient(client);
 		this.toolkit.paintBordersFor(client);
 		main.layout(true);
 		return main;
 	}
 
 	/**
 	 * Gets the player current score.
 	 *
 	 * @param player the player
 	 * @return the player current score
 	 */
 	private String getCurrentScore() {
 		String result = "";
 		Integer score = this.game.getScore();
 		if (score == null) {
 			result = "";
 		} else {
 			result = SCORE_FORMAT.format(score);
 		}
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.game.IGameListener#notifyGameEvent(org.opendarts.prototype.model.game.GameEvent)
 	 */
 	@Override
 	public void notifyGameEvent(final GameEvent event) {
 		if (event.getGame().equals(this.game)) {
 			LOG.trace("New Game Event: {}", event);
 			IGameEntry entry = event.getEntry();
 			IPlayer player = event.getPlayer();
 			switch (event.getType()) {
 				case GAME_INITIALIZED:
 					this.handleGameInitialized();
 					break;
 				case GAME_REINITIALIZED:
 					this.game.removeListener(this);
 					this.gameEditor.handleGameReinitialized(this, this.game);
 					break;
 				case GAME_ENTRY_CREATED:
 					this.handleNewEntry(entry);
 					break;
 				case GAME_ENTRY_REMOVED:
 					this.handleRemoveEntry(entry);
 					break;
 				case GAME_ENTRY_UPDATED:
 					this.handleEntryUpdated(player, entry);
 					break;
 				case NEW_CURRENT_PLAYER:
 					this.handlePlayer(player, entry);
 					break;
 				case GAME_FINISHED:
 					this.handleGameFinished(player);
 					break;
 				case GAME_CANCELED:
 					this.dirty = false;
 					this.mForm.dirtyStateChanged();
 			}
 		}
 	}
 
 	/**
 	 * Handle game initialized.
 	 */
 	private void handleGameInitialized() {
 		this.txtScoreLeft.setText(this.getCurrentScore());
 
 		for (TableViewer tw : this.scoreViewers.values()) {
 			tw.setInput(this.game.getGameEntries());
 		}
 		this.isDirty();
 		this.mForm.dirtyStateChanged();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#isDirty()
 	 */
 	@Override
 	public boolean isDirty() {
 		return this.dirty;
 	}
 
 	/**
 	 * Handle new entry.
 	 *
 	 * @param entry the entry
 	 */
 	private void handleNewEntry(IGameEntry entry) {
 		boolean twoPlayer = (this.players.size() == 2);
 		if (twoPlayer) {
 			// only one table
 			TableViewer tw = this.scoreViewers.get(this.getFirstPlayer());
 			tw.add(entry);
 			tw.reveal(entry);
 		} else {
 			for (TableViewer tw : this.scoreViewers.values()) {
 				tw.add(entry);
 				tw.reveal(entry);
 			}
 		}
 	}
 
 	/**
 	 * Handle remove entry.
 	 *
 	 * @param entry the entry
 	 */
 	private void handleRemoveEntry(IGameEntry entry) {
 		boolean twoPlayer = (this.players.size() == 2);
 		if (twoPlayer) {
 			// only one table
 			TableViewer tw = this.scoreViewers.get(this.getFirstPlayer());
 			tw.remove(entry);
 		} else {
 			for (TableViewer tw : this.scoreViewers.values()) {
 				tw.remove(entry);
 			}
 		}
 	}
 
 	/**
 	 * Handle entry updated.
 	 *
 	 * @param entry the entry
 	 */
 	private void handleEntryUpdated(IPlayer player, IGameEntry entry) {
 		Text txt;
 		TableViewer scoreViewer = this.scoreViewers.get(player);
 		scoreViewer.update(entry, null);
 		scoreViewer.setSelection(StructuredSelection.EMPTY);
 		if (player != null) {
 			// Update score left
 			this.txtScoreLeft.setText(this.getCurrentScore());
 			// Clear input
 			txt = this.playerScoreInput.get(player);
 			if (!txt.isDisposed()) {
 				txt.setText("");
 			}
 			this.setInputFocus(this.game.getCurrentPlayer());
 		}
 
 		// Update clock
 		long duration = this.game.getDuration();
 		this.txtDuration.setText(this.getTimeText(duration));
 		
 
 		int score = this.game.getScore();
 		if (score > 0) {
 			int startScore = this.gameDefinition.getStartScore();
 			int done = (startScore - score);
 			long left = (long) (
 					+ (((double) score * duration) / ((double) done)));
 			long forecast = duration + left;
 			this.gameDefinition.getStartScore();
 			this.txtForecast.setText(this.getTimeText(forecast));
 			this.txtTimeLeft.setText(this.getTimeText(left));
 
 			double delta = ((double) this.gameDefinition.getTimeTarget() - forecast)
 					/ this.gameDefinition.getTimeTarget();
 
 			if (forecast > this.gameDefinition.getTimeTarget()) {
 				this.txtForecast.setForeground(Display.getDefault()
 						.getSystemColor(SWT.COLOR_DARK_RED));
 			} else if (delta < 0.10) { // 
 				this.txtForecast.setForeground(Display.getDefault()
 						.getSystemColor(SWT.COLOR_DARK_GRAY));
 			} else {
 				this.txtForecast.setForeground(Display.getDefault()
 						.getSystemColor(SWT.COLOR_DARK_GREEN));
 			}
 
 		} else {
 			this.txtForecast.setText("--:--:--");
 			if (duration > this.gameDefinition.getTimeTarget()) {
 				this.txtDuration.setForeground(this.toolkit.getColors()
 						.getColor(IGeneralPrefs.COLOR_BROKEN));
 			} else {
 				this.txtDuration.setForeground(this.toolkit.getColors()
 						.getColor(IGeneralPrefs.COLOR_WINNING));
 			}
 		}
 	}
 
 	/**
 	 * Gets the time text.
 	 *
 	 * @param duration the duration
 	 * @return the time text
 	 */
 	private String getTimeText(long duration) {
 		Date date = new Date(duration);
 		return this.gameDefinition.getTimeFormatter().format(date);
 	}
 
 	/**
 	 * Handle game finished.
 	 *
 	 * @param player the player
 	 */
 	private void handleGameFinished(IPlayer player) {
 		TableViewerColumn column;
 		column = this.playerColumn.get(player);
 		column.getColumn().setImage(
 				X01UiPlugin.getImage(ISharedImages.IMG_TICK_DECO));
 		// remove edition
 		for (Text inputTxt : this.playerScoreInput.values()) {
 			inputTxt.setEnabled(false);
 
 			inputTxt.setBackground(OpenDartsFormsToolkit.getToolkit()
 					.getColors().getColor(IGeneralPrefs.COLOR_INACTIVE));
 		}
 		for (TableViewerColumn col : this.playerColumn.values()) {
 			col.setEditingSupport(null);
 		}
 
 		// End Game dialog
 		if (!this.game.getParentSet().isFinished()) {
 			String title = MessageFormat.format("{0} finished", this.game);
 			String message = this.game.getWinningMessage();
 			Shell shell = this.getSite().getShell();
 			MessageDialog.open(MessageDialog.INFORMATION, shell, title,
 					message, SWT.SHEET);
 		}
 		this.dirty = false;
 		this.mForm.dirtyStateChanged();
 	}
 
 	/**
 	 * Handle player.
 	 *
 	 * @param player the player
 	 */
 	private void handlePlayer(IPlayer player, IGameEntry entry) {
 		if (player != null) {
 			TableViewerColumn column;
 			// mark column
 			column = this.playerColumn.get(player);
 			TableViewerColumn c;
 			for (IPlayer p : this.players) {
 				c = this.playerColumn.get(p);
 				if (c.equals(column)) {
 					c.getColumn().setImage(
 							X01UiPlugin.getImage(ISharedImages.IMG_ARROW_DECO));
 				} else {
 					c.getColumn().setImage(null);
 				}
 			}
 			// enable/disable inputs & focus
 			this.setInputFocus(player);
 
 			// IA playing
 			if (player.isComputer()) {
 				ThreeDartsComputerDialog computerThrow = new DartsComputerX01DefiDialog(
 						this.getSite().getShell(),
 						this.gameDefinition.getDelay(),
 						(IComputerPlayer) player, this.game,
 						(GameX01Entry) entry);
 				computerThrow.open();
 
 				IDartsThrow dartThrow = computerThrow.getComputerThrow();
 				if (dartThrow instanceof WinningX01DartsThrow) {
 					this.gameService.addWinningPlayerThrow(this.game, player,
 							dartThrow);
 				} else {
 					this.gameService.addPlayerThrow(this.game, player,
 							dartThrow);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets the input focus.
 	 *
 	 * @param player the new input focus
 	 */
 	private void setInputFocus(IPlayer player) {
 		Text playerInputTxt = this.playerScoreInput.get(player);
 		for (Text inputTxt : this.playerScoreInput.values()) {
 			if (playerInputTxt.equals(inputTxt)) {
 				inputTxt.setEnabled(true);
 				inputTxt.setBackground(OpenDartsFormsToolkit.getToolkit()
 						.getColors().getColor(IGeneralPrefs.COLOR_ACTIVE));
 				inputTxt.setFocus();
 			} else {
 				inputTxt.setEnabled(false);
 				inputTxt.setText("");
 				inputTxt.setBackground(OpenDartsFormsToolkit.getToolkit()
 						.getColors().getColor(IGeneralPrefs.COLOR_INACTIVE));
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent)
 	 */
 	@Override
 	public void expansionStateChanged(ExpansionEvent e) {
 		this.body.layout(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent)
 	 */
 	@Override
 	public void expansionStateChanging(ExpansionEvent e) {
 		// Nothing to do
 	}
 }
