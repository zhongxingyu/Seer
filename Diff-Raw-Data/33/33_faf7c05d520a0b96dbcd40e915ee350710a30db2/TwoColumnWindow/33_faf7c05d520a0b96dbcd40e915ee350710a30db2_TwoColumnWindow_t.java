 package org.myftp.gattserver.grass3.windows.template;
 
import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.VerticalLayout;
 
 public abstract class TwoColumnWindow extends BaseWindow {
 
 	private static final long serialVersionUID = 5064416476628186307L;
 
 	@Override
 	protected void createWindowContent(VerticalLayout layout) {
 
		VerticalLayout upSpacingLayout = new VerticalLayout();
		upSpacingLayout.setHeight("10px");
		layout.addComponent(upSpacingLayout);
		
		HorizontalLayout columnsLayout = new HorizontalLayout();
		layout.addComponent(columnsLayout);

 		// levý sloupec
		createLeftColumn(columnsLayout);
 
		VerticalLayout spacingLayout = new VerticalLayout();
		spacingLayout.setWidth("15px");
		columnsLayout.addComponent(spacingLayout);
		
 		// pravý sloupec
		createRightColumn(columnsLayout);
 	}
 
	private void createLeftColumn(HorizontalLayout layout) {
 
 		VerticalLayout backgroundLayout = new ColumnBuilder() {
 
 			@Override
 			protected void createColumnContent(VerticalLayout layout) {
 				createLeftColumnContent(layout);
 			}
 
 		}.buildColumn();
		layout.addComponent(backgroundLayout);
 
 	}
 
 	/**
 	 * Obsah levé části
 	 * 
 	 * @param layout
 	 */
 	protected abstract void createLeftColumnContent(VerticalLayout layout);
 
	private void createRightColumn(HorizontalLayout layout) {
 
 		VerticalLayout backgroundLayout = new ColumnBuilder(725,
 				"long_right_middle_background") {
 
 			@Override
 			protected void createColumnContent(VerticalLayout layout) {
 				createRightColumnContent(layout);
 			}
 
 		}.buildColumn();
		layout.addComponent(backgroundLayout);
 
 	}
 
 	/**
 	 * Obsah pravé části
 	 * 
 	 * @param layout
 	 */
 	protected abstract void createRightColumnContent(VerticalLayout layout);
 
 }
