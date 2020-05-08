 /*
  * 
  */
 package org.opendarts.ui.x01.utils.comp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.opendarts.core.model.game.IGame;
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.x01.model.GameX01;
 import org.opendarts.ui.utils.ColumnDescriptor;
 import org.opendarts.ui.utils.OpenDartsFormsToolkit;
 import org.opendarts.ui.utils.listener.GrabColumnsListener;
 import org.opendarts.ui.x01.label.ScoreLabelProvider;
 import org.opendarts.ui.x01.label.ToGoLabelProvider;
 import org.opendarts.ui.x01.label.TurnLabelProvider;
 
 /**
  * The Class SessionDetailComposite.
  */
 public class GameDetailComposite extends Composite {
 	private final GameX01 game;
 	private TableViewer viewer;
 	private final OpenDartsFormsToolkit toolkit;
 
 	/**
 	 * Instantiates a new session detail composite.
 	 *
 	 * @param parent the parent
 	 * @param game2 
 	 */
 	public GameDetailComposite(Composite parent, IGame game) {
 		super(parent, SWT.NONE);
 		this.toolkit = OpenDartsFormsToolkit.getToolkit();
 		this.game = (GameX01) game;
 		GridLayoutFactory.fillDefaults().applyTo(this);
 		this.createContents();
 	}
 
 	/**
 	 * Creates the contents.
 	 */
 	private void createContents() {
 		Table table = new Table(this, SWT.V_SCROLL | SWT.BORDER
 				| SWT.FULL_SELECTION);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		this.viewer = new TableViewer(table);
 		this.viewer.setContentProvider(new ArrayContentProvider());
 
 		List<ColumnDescriptor> columns = this.getColumns();
 		this.viewer.getControl().addControlListener(
 				new GrabColumnsListener(this.viewer, columns));
 
 		this.viewer.setInput(this.game.getGameEntries());
 	}
 
 	/**
 	 * Gets the columns.
 	 *
 	 * @return the columns
 	 */
 	private List<ColumnDescriptor> getColumns() {
 		List<ColumnDescriptor> result = new ArrayList<ColumnDescriptor>();
 
 		ColumnDescriptor colDescr = new ColumnDescriptor("");
 		colDescr.width(60);
 		colDescr.labelProvider(new TurnLabelProvider(false));
 
		List<IPlayer> players = this.game.getParentSet().getGameDefinition().getInitialPlayers();
 
 		if (players.size() == 2) {
 			// Two player
 			result.addAll(this.createPlayerColumns(this.viewer, players.get(0)));
 
 			this.toolkit.createTableColumn(this.viewer, colDescr);
 			result.add(colDescr);
 			result.addAll(this.createPlayerColumns(this.viewer, players.get(1)));
 		} else {
 			for (IPlayer player : players) {
 				this.toolkit.createTableColumn(this.viewer, colDescr);
 				result.add(colDescr);
 				result.addAll(this.createPlayerColumns(this.viewer, player));
 			}
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
 
 		ColumnDescriptor colDescr;
 		int width = 80;
 
 		// Scored
 		colDescr = new ColumnDescriptor(player.getName());
 		colDescr.width(width);
 		colDescr.labelProvider(new ScoreLabelProvider(player, false));
 		this.toolkit.createTableColumn(viewer, colDescr);
 		result.add(colDescr);
 
 		// Scored
 		colDescr = new ColumnDescriptor(
 				String.valueOf(this.game.getScoreToDo()));
 		colDescr.width(width);
 		colDescr.labelProvider(new ToGoLabelProvider(player, false));
 		this.toolkit.createTableColumn(viewer, colDescr);
 		result.add(colDescr);
 
 		return result;
 	}
 
 }
