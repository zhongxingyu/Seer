 package be.betty.gwtp.client.views;
 
 import be.betty.gwtp.client.CellDropControler;
 import be.betty.gwtp.client.presenters.MainPresenter;
 
 import com.allen_sauer.gwt.dnd.client.PickupDragController;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.ViewImpl;
 import com.google.gwt.user.client.ui.ListBox;
 
 public class MainView extends ViewImpl implements MainPresenter.MyView {
 
 	private final Widget widget;
 
 	@UiField Label mainLabel;
 	@UiField AbsolutePanel dndpanel;
 	@UiField Label content;
 	@UiField SimplePanel board_panel;
 	@UiField ListBox comboInstance;
 	@UiField Label currentInstance;
 	@UiField ListBox combo_viewChoice1;
 	@UiField ListBox combo_viewChoice2;
 	@UiField SimplePanel CardSelectionOption;
 	@UiField VerticalPanel cards_panel;
	@UiField VerticalPanel notifBarVerticalPanel;
  
 
 	public interface Binder extends UiBinder<Widget, MainView> {
 	}
 
 	@Inject
 	public MainView(final Binder binder) {
 		widget = binder.createAndBindUi(this);
 	}
 
 	@Override
 	public void setInSlot(Object slot, Widget content) {
 		if (slot == MainPresenter.SLOT_Card) {
 			cards_panel.clear();
 			if (content != null)
 				cards_panel.add(content);
 		} 
 		else if (slot == MainPresenter.SLOT_OPTION_SELECION) {
 			 CardSelectionOption.clear();
 			 if (content != null)
 				 CardSelectionOption.add(content);
 		}
 		else if (slot == MainPresenter.SLOT_BOARD) {
 			 board_panel.clear();
 			 if (content != null)
 				 board_panel.add(content);
 		}
 		else {
 		}
 		super.setInSlot(slot, content);
 	}
 
 
 	@Override
 	public void addToSlot(Object slot, Widget content) {
 		if (slot == MainPresenter.SLOT_Card)
 			if (content != null)
 				cards_panel.add(content);
 			else
 				super.addToSlot(slot, content);
 
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	public Label getMainLabel() {
 		return mainLabel;
 	}
 
 
 	public AbsolutePanel getDndPanel() {
 		return dndpanel;
 	}
 
 	@Override
 	public Label getContent() {
 		return content;
 	}
 
 	@Override
 	public void setContent(Label content) {
 		this.content = content;
 	}
 
 	@Override
 	public VerticalPanel getCards_panel() {
 		return cards_panel;
 	}
 
 	@Override
 	public void constructFlex(PickupDragController cardDragController) {
 //		int COLUMNS = 5;
 //		int ROWS = 6;
 //		for (int i = 0; i < COLUMNS; i++) {
 //
 //			for (int j = 0; j < ROWS; j++) {
 //				// create a simple panel drop target for the current cell
 //				SimplePanel simplePanel = new SimplePanel();
 //				simplePanel.setPixelSize(100, 60);
 //				flexTable.setWidget(j, i, simplePanel);
 //				// flexTable.getCellFormatter().setStyleName(i, j,
 //				// CSS_DEMO_PUZZLE_CELL);
 //
 //				// instantiate a drop controller of the panel in the current
 //				// cell
 //				CellDropControler dropController = new CellDropControler(
 //						simplePanel);
 //				cardDragController.registerDropController(dropController);
 //			}
 //		}
 		
 		
 	}
 
 	@Override
 	public ListBox getComboInstance() {
 		return comboInstance;
 	}
 
 	@Override
 	public Label getCurrentInstance() {
 		return currentInstance;
 	}
 
 	@Override
 	public ListBox getCombo_viewChoice1() {
 		return combo_viewChoice1;
 	}
 
 	@Override
 	public ListBox getCombo_viewChoice2() {
 		return combo_viewChoice2;
 	}
	
	public VerticalPanel GetNotifBarVerticalPanel(){
		return notifBarVerticalPanel;
	}
 
 
 }
