 package nl.tue.fingerpaint.client.gui.celllists;
 
 import java.util.List;
 
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 import nl.tue.fingerpaint.shared.GeometryNames;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 /**
  * CellList that is used to show all saved initial distributions, of which the
  * user can select one to load.
  * 
  * @author Group Fingerpaint
  */
 public class LoadInitDistCellList extends CellList<String> {
 	/**
 	 * Reference to the model. Used to set the loaded concentration
 	 * distribution.
 	 */
 	protected ApplicationState as;
 
 	/** The selection model of this cell list. */
 	final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
 
 	/**
 	 * Construct a new cell list that can be used to select multiple mixing runs
 	 * from all available saved mixing runs.
 	 * 
 	 * @param appState
 	 *            Reference to the model, used to set the selected concentration
 	 *            distribution.
 	 */
 	public LoadInitDistCellList(ApplicationState appState) {
 		super(new TextCell());
 		this.as = appState;
 		setSelectionModel();
		setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
 		setSelectionModel(selectionModel);
 		ensureDebugId("loadInitDistCellList");
 	}
 
 	/**
 	 * Fills this CellList with the items in {@code geometryDistributions}.
 	 * 
 	 * @param geometryDistributions
 	 *            List of all initial concentration distributions currently
 	 *            stored in the local storage.
 	 */
 	public void fillCellList(List<String> geometryDistributions) {
 		setRowCount(geometryDistributions.size(), true);
 		setRowData(0, geometryDistributions);
 		//Alternate between white and light-gray background colors
 		for (int i = 0; i < geometryDistributions.size(); i += 2) {
 			this.getRowElement(i).addClassName("cellListStyleGray");
 		}
 		for (int i = 1; i < geometryDistributions.size(); i += 2) {
 			this.getRowElement(i).addClassName("cellListStyleWhite");
 		}
 	}
 
 	/**
 	 * Sets the selection model for this cell list.
 	 */
 	private void setSelectionModel() {
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					public void onSelectionChange(SelectionChangeEvent event) {
 						String selected = selectionModel.getSelectedObject();
 
 						if (selected != null) {
 
 							// get the selected initial distribution, and
 							// set it in the AS
 							int[] dist = StorageManager.INSTANCE
 									.getDistribution(GeometryNames
 											.getShortName(as
 													.getGeometryChoice()),
 											selected);
 							as.setInitialDistribution(dist);
 							as.drawDistribution();
 
 							selectionModel.setSelected(selected, false);
 							GuiState.loadPanel.removeFromParent();
 						}
 					}
 				});
 	}
 }
