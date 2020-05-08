 package net.jorgeherskovic.medrec.client;
 
 import java.util.List;
 import java.util.Map;
 
 import org.adamtacy.client.ui.effects.events.EffectCompletedEvent;
 import org.adamtacy.client.ui.effects.events.EffectCompletedHandler;
 import org.adamtacy.client.ui.effects.impl.Fade;
import org.adamtacy.client.ui.effects.impl.Resize;
 
 import net.jorgeherskovic.medrec.client.event.RedrawEvent;
 import net.jorgeherskovic.medrec.client.event.RowDroppedEvent;
 import net.jorgeherskovic.medrec.shared.Consolidation;
 import net.jorgeherskovic.medrec.shared.Medication;
 
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTML;
 
 public class ReconciledRenderer extends TableRenderer {
 
 	private static String[] columnStyles = { "DragHandle", 
 			"Origin", "Medication", "Dosage", "Frequency", "Date", "Date",
 			"Form", "Alert" };
 
 	public ReconciledRenderer(DraggableFlexTable table, String[] headings,
 			SimpleEventBus bus) {
 		super(table, headings, bus);
 	}
 
 	@Override
 	public void renderTable() {
 		DraggableFlexTable t = this.getAttachedTable();
 		List<Consolidation> meds = t.getMedList();
 		
 		t.removeAllRows();
 		t.clearTargetRows();
 		
 		Map<HTML, Consolidation> rowMapping = t.getRowMapping();
 		rowMapping.clear();
 
 		this.renderTableHeadings("TableHeading");
 		t.setTargetRow(0, 0);
 
 		for (int i = 0; i < meds.size(); i++) {
 			Medication m = meds.get(i).getSelectedMedication();
 			int col = 0;
 
 			HTML handle = new HTML(this.dragToken);
 			t.setWidget(i + 1, col++, handle);
 			t.getRowDragController().makeDraggable(handle);
 
 			rowMapping.put(handle, meds.get(i));
 
 			// t.setText(i + 1, col++, Integer.toString(i + 1)); // No entry number
 			t.setHTML(i + 1, col++, m.getProvenance());
 			t.setHTML(i + 1, col++, m.getMedicationName());
 			t.setHTML(i + 1, col++, m.getDose() + " " + m.getUnits());
 			t.setHTML(i + 1, col++, m.getInstructions());
 			t.setHTML(i + 1, col++, m.getStartDateString());
 			t.setHTML(i + 1, col++, m.getEndDateString());
 			t.setHTML(i + 1, col++, m.getFormulation());
 			t.setText(i + 1, col++, "");
 
 			t.getRowFormatter().addStyleName(i + 1, "SingleRowDesign");
 			t.getRowFormatter().addStyleName(i + 1, "FullReconciliation");
 			/*
 			 * Apply the TableDesign style to each cell individually to get
 			 * borders
 			 */
 			this.applyStyleToAllCellsInRow(i + 1, "SingleRowDesign");
 			this.applyStyleArrayToRow(i + 1, columnStyles);
 			
 			// Every row is its own target in this table
 			t.setTargetRow(i+1, i+1);
 		}
 		
 		int rr=t.getRowToRemove();
 		while (rr > -1) {
 			t.deleteMed(rr);
 			Fade my_fade=new Fade(t.getRowFormatter().getElement(rr+1));
 			my_fade.addEffectCompletedHandler(new EffectCompletedHandler() {
 				public void onEffectCompleted(EffectCompletedEvent evt) {
 					ReconciledRenderer.this.bus.fireEvent(new RedrawEvent());
 				}
 			});
 			my_fade.setDuration(TableRenderer.FADE_DURATION);
 			my_fade.play();
 			 
 			rr=t.getRowToRemove();
 		}
 
 		return;
 
 	}
 
 	@Override
 	public void handleDroppedRow(RowDroppedEvent event) {
 		HTML handle = (HTML) event.getSourceTable().getWidget(
 				event.getSourceRow(), 0);
 		Consolidation cons = event.getSourceTable().getRowMapping().get(handle);
 		Medication selected = cons.getSelectedMedication();
 		if (selected == null) {
 			Window.alert("This should never happen.");
 		}
 		// Window.alert("Dragged medication:" +
 		// selected.getMedicationName()+" "+selected.getDose());
 		int targetPosition = event.getDestRow() - 1;
 
 		this.getAttachedTable().insertMed(targetPosition, cons);
 		// Find all rows in the original table that contain the medications in
 		// this one, and delete them.
 		// We will go in reverse order as not to restart the search.
 		List<? extends Consolidation> otherList = event.getSourceTable()
 				.getMedList();
 		
 		for (int i = otherList.size() - 1; i >= 0; i--) {
 			if (otherList.get(i).same_meds(cons)) {
 				//Fade new_fade=new Fade(event.getSourceTable().getRowFormatter().getElement(event.getSourceRow()));
 				//new_fade.play();
 				
 				//otherList.remove(i);
 				event.getSourceTable().registerRemovalRequest(i);
 			}
 		}
 		bus.fireEvent(new RedrawEvent());
 	}
 
 }
