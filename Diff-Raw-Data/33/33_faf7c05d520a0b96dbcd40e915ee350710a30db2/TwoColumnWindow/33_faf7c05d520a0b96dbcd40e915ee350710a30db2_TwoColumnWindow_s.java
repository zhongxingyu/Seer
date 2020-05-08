 package org.myftp.gattserver.grass3.windows.template;
 
import com.vaadin.ui.AbsoluteLayout;
 import com.vaadin.ui.VerticalLayout;
 
 public abstract class TwoColumnWindow extends BaseWindow {
 
 	private static final long serialVersionUID = 5064416476628186307L;
 
 	@Override
 	protected void createWindowContent(VerticalLayout layout) {
 
 		// levý sloupec
		createLeftColumn(layout);
 
 		// pravý sloupec
		createRightColumn(layout);
 	}
 
	private void createLeftColumn(VerticalLayout layout) {
 
 		VerticalLayout backgroundLayout = new ColumnBuilder() {
 
 			@Override
 			protected void createColumnContent(VerticalLayout layout) {
 				createLeftColumnContent(layout);
 			}
 
 		}.buildColumn();
//		layout.addComponent(backgroundLayout, "left:0px; top:135px;");
 
 	}
 
 	/**
 	 * Obsah levé části
 	 * 
 	 * @param layout
 	 */
 	protected abstract void createLeftColumnContent(VerticalLayout layout);
 
	private void createRightColumn(VerticalLayout layout) {
 
 		VerticalLayout backgroundLayout = new ColumnBuilder(725,
 				"long_right_middle_background") {
 
 			@Override
 			protected void createColumnContent(VerticalLayout layout) {
 				createRightColumnContent(layout);
 			}
 
 		}.buildColumn();
//		layout.addComponent(backgroundLayout, "left:265px; top:135px;");
 
 	}
 
 	/**
 	 * Obsah pravé části
 	 * 
 	 * @param layout
 	 */
 	protected abstract void createRightColumnContent(VerticalLayout layout);
 
 }
