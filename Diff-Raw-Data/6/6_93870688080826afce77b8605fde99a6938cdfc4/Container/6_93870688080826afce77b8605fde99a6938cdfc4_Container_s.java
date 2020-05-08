 package translator.com.client;
 
 
 import translator.com.client.favourite.Favourite;
 import translator.com.client.mainview.MainView;
 import translator.com.client.menubar.MenuBar;
 import translator.com.client.menubar.MenuSwitcher;
 import translator.com.client.util.TranslateAction;
 import translator.com.client.util.UserUtil;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.Label;
 
 public class Container extends Composite {
 	private static ContainerUiBinder uiBinder = GWT.create(ContainerUiBinder.class);
	@UiField(provided=true) Image imageLogo;
 	@UiField(provided=true) MenuBar menuBar;
 	@UiField(provided=true) Favourite favourite;
 	@UiField(provided=true) MainView mainView;
 	@UiField Label userName;
 //	@UiField CellList<String> cellList;
 //	@UiField Label header;
 
 	interface ContainerUiBinder extends UiBinder<Widget, Container> {
 	}
 	
 	/*@UiFactory CellList<String> makeCellList() {
 		TextCell textCell = new TextCell();
 		final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
 		
 		CellList<String> cellList = new CellList<String>(textCell){
 			public void render(Context context, String value, SafeHtmlBuilder sb) {
 			}
 		};
 		cellList.setSelectionModel(selectionModel);
 		
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
 				String selected = selectionModel.getSelectedObject();
 				if (selected != null) {
 					header.setText(selected);
 				}
 			}
 	    });
 		
 	    return cellList;
 	}*/
 
 	public Container() {
		imageLogo = new Image("http://code.google.com/appengine/images/appengine-noborder-120x30.gif");
		
 		mainView = new MainView();
 		favourite = new Favourite();
 		
 		MenuSwitcher menuSwitcher = new MenuSwitcher(mainView, favourite);
 		
 		menuBar = new MenuBar(menuSwitcher);
 		
 		favourite.setTranslateAction(new TranslateAction() {
 			public void translateText(String word) {
 				mainView.setText(word);
 				menuBar.activateTranslator();
 				mainView.onClick(null);
 			}
 			public void setWordNotExists(String word) {
 				mainView.setWordNotExists(word);
 			}
 		});
 		
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		String userNameText = UserUtil.getUserName();
 		if (null != userNameText) {
 			userName.setVisible(true);
 			userName.setText(userNameText);
 		}
 //		final List<String> DAYS = Arrays.asList("Translator", "Dictionary");
 //		cellList.setRowCount(DAYS.size(), true);
 //		cellList.setRowData(0, DAYS);
 	}
 }
