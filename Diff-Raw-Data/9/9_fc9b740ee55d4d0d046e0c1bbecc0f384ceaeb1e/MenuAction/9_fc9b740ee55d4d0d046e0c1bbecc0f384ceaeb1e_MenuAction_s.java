 package kea.kme.pullpit.client.UI;
 
 import kea.kme.pullpit.client.objects.Band;
 import kea.kme.pullpit.client.services.ObjectService;
 import kea.kme.pullpit.client.services.ObjectServiceAsync;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author Clement
  */
 public class MenuAction implements ClickHandler {
 
 	private Button activeButton;
 
 	public MenuAction() {
 
 	}
 
 	public void mainMenuHandler(Button... b) {
 
 		for (Button menuButton : b) {
 
 			if (menuButton.getText().equals("Home")) {
 				menuButton.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						highlightButton((Button) event.getSource());
 						UIMain.getInstance().getContentPanel().clear();
 						UIMain.getInstance().getContentPanel()
 								.add(new Label("Home content"));
 					}
 				});
 			} else if (menuButton.getText().equals("Shows")) {
 				menuButton.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						highlightButton((Button) event.getSource());
 						UIMain.getInstance().getContentPanel().clear();
 						UIMain.getInstance().getContentPanel()
 								.add(new Label("Shows content"));
 					}
 				});
 			} else if (menuButton.getText().equals("Bands")) {
 				menuButton.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						highlightButton((Button) event.getSource());
 						UIMain.getInstance().getContentPanel().clear();
 						ObjectServiceAsync osa = GWT
 								.create(ObjectService.class);
 						AsyncCallback<Band[]> callback = new AsyncCallback<Band[]>() {
 
 							@Override
 							public void onFailure(Throwable caught) {
 								// TODO Auto-generated method stub
 								if (caught instanceof Exception) {
 									Window.alert(caught.getMessage());
 
 								}
 								// Window.alert(caught.getMessage());
 
 							}
 
 							@Override
 							public void onSuccess(Band[] result) {
 								// Window.alert("!"+result);
 
 								UIMain.getInstance().displayBandTable(result);
 
 							}
 						};
						osa.getBands(0, 252, "bands.lastEdit DESC", callback);
 
 					}
 				});
 			} else if (menuButton.getText().equals("Venues")) {
 				menuButton.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						highlightButton((Button) event.getSource());
 						UIMain.getInstance().getContentPanel().clear();
 
 						// UIMain.getInstance().displayShowTable(testBand,testBand1,testBand2,testBand3,testBand4);
 						// AdminServiceAsync asa =
 						// GWT.create(AdminService.class);
 						// AsyncCallback<Integer> callback = new
 						// AsyncCallback<Integer>() {
 						//
 						// @Override
 						// public void onFailure(Throwable caught) {
 						// // TODO Auto-generated method stub
 						// Window.alert(caught.getMessage());
 						//
 						// }
 						//
 						// @Override
 						// public void onSuccess(Integer result) {
 						// Window.alert("!"+result);
 						//
 						// }
 						// };
 						// asa.pullBands(callback);
 					}
 				});
 			} else if (menuButton.getText().equals("Dokumenter")) {
 				menuButton.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						highlightButton((Button) event.getSource());
 						UIMain.getInstance().getContentPanel().clear();
 						UIMain.getInstance().getContentPanel()
 								.add(new Label("Dokumenter content"));
 					}
 				});
 			} else if (menuButton.getText().equals("Kontakter")) {
 				menuButton.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						highlightButton((Button) event.getSource());
 						UIMain.getInstance().getContentPanel().clear();
 						UIMain.getInstance().getContentPanel()
 								.add(new Label("Kontakter content"));
 					}
 				});
 			} else if (menuButton.getText().equals("")) {
 				menuButton.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						highlightButton((Button) event.getSource());
 						UIMain.getInstance().getContentPanel().clear();
 						new UIAdmin();
 					}
 				});
 			}
 
 		}
 	}
 
 	/**
 	 * Gives a Button element a highlighted style
 	 * 
 	 * @param b
 	 */
 	public void highlightButton(Button b) {
 		DOM.setElementAttribute(b.getElement(), "id", "highlightButton");
 		if (activeButton != null) {
 			DOM.setElementAttribute(activeButton.getElement(), "id", "menuItem");
 		}
 		activeButton = b;
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 	}
 
 }
