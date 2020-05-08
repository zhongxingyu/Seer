 package net.jorgeherskovic.medrec.client;
 
 import net.jorgeherskovic.medrec.client.event.RedrawEvent;
 import net.jorgeherskovic.medrec.client.event.RedrawEventHandler;
 import net.jorgeherskovic.medrec.client.event.RowDroppedEvent;
 import net.jorgeherskovic.medrec.client.event.RowDroppedEventHandler;
 
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.client.ui.HTMLTable;
 import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
 
 public abstract class TableRenderer {
 	private final DraggableFlexTable attachedTable;
 	private String[] headings;
 	protected final SimpleEventBus bus;
	protected final String dragToken="&nbsp;[DRAG]&nbsp;";
	
 	public TableRenderer(DraggableFlexTable table, String[] headings,
 			SimpleEventBus bus) {
 		this.attachedTable = table;
 		this.headings = headings;
 
 		bus.addHandler(RowDroppedEvent.TYPE, new RowDroppedEventHandler() {
 
 			public void onRowDropped(RowDroppedEvent event) {
 				// Only react to messages targeted at THIS table
 				if (event.getDestTable() == TableRenderer.this.attachedTable
 						&& event.getDestTable() != event.getSourceTable()) {
 					TableRenderer.this.handleDroppedRow(event);
 				}
 			}
 		});
 
 		bus.addHandler(RedrawEvent.TYPE, new RedrawEventHandler() {
 
 			public void onRedraw(RedrawEvent event) {
 				// TODO Auto-generated method stub
 				TableRenderer.this.renderTable();
 			}
 
 		});
 
 		this.bus = bus;
 	}
 
 	public abstract void handleDroppedRow(RowDroppedEvent event);
 
 	public DraggableFlexTable getAttachedTable() {
 		return attachedTable;
 	}
 
 	public void applyStyleToAllCellsInRow(int rownum, String styleName) {
 		CellFormatter cf = attachedTable.getCellFormatter();
 		for (int j = 0; j < attachedTable.getCellCount(rownum); j++) {
 			cf.setStyleName(rownum, j, styleName);
 		}
 	}
 
 	public void applyStyleArrayToRow(int rownum, String[] styles) {
 		CellFormatter cf = attachedTable.getCellFormatter();
 		for (int j = 0; j < attachedTable.getCellCount(rownum); j++) {
 			cf.setStyleName(rownum, j, styles[j]);
 		}
 	}
 
 	public void renderTableHeadings(String style) {
 		HTMLTable.CellFormatter h = attachedTable.getCellFormatter();
 
 		for (int i = 0; i < headings.length; i++) {
			attachedTable.setHTML(0, i, headings[i]);
 			h.setStyleName(0, i, style);
 		}
 	}
 
 	/*
 	 * renderTable has to fill out the Widget->? extends Consolidation mapping
 	 * as well.
 	 */
 	public abstract void renderTable();
 }
