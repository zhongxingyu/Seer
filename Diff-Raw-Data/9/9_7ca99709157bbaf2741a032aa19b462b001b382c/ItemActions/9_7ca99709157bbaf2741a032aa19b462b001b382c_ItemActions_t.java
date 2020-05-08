 package kea.kme.pullpit.client.UI;
 
 import kea.kme.pullpit.client.UI.widgets.ShowWidget;
 import kea.kme.pullpit.client.services.ObjectService;
 import kea.kme.pullpit.client.services.ObjectServiceAsync;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class ItemActions {
 
 	public ItemActions() {
 
 	}
 
 	public static void addFieldEventsTextBox(final ShowWidget currentShow, final TextBox textBox,final Label label) {
 		final PullPitConstants constants = GWT.create(PullPitConstants.class);
 		final ObjectServiceAsync osa = GWT.create(ObjectService.class);
 		textBox.addBlurHandler(new BlurHandler() {
 
 			@Override
 			public void onBlur(BlurEvent event) {
 
 				if (label.getText().startsWith(constants.fee())) {
 					Double tempDouble = FieldVerifier.verifyDouble(textBox
 							.getText());
 
 					if (!tempDouble.equals(-1.)) {
 						currentShow.getShow().setFee(tempDouble);
 						AsyncCallback<Void> callback = CallbackFactory
 								.setShowCallback();
 						osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 						textBox.setText(tempDouble + "");
 
 					} else {
 						Window.alert("Forkert værdi");
 					}
 				} else if (label.getText().startsWith(constants.provision())) {
 
 					Double tempDouble = FieldVerifier.verifyDouble(textBox
 							.getText());
 					if (!tempDouble.equals(-1.)) {
 						currentShow.getShow().setProvision(tempDouble);
 						AsyncCallback<Void> callback = CallbackFactory
 								.setShowCallback();
 						osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 						textBox.setText(tempDouble + "");
 					} else {
 						// fejl //
 					}
 				} else if (label.getText().startsWith(constants.profitSplit())) {
 
 					Integer tempInt = FieldVerifier.verifyInt(textBox.getText());
 					if (!tempInt.equals(-1)) {
 						currentShow.getShow().setProfitSplit(tempInt);
 						AsyncCallback<Void> callback = CallbackFactory
 								.setShowCallback();
 						osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 						textBox.setText(tempInt + "");
 					} else {
 						// fejl //
 					}
 				} else if (label.getText().startsWith(constants.ticketPrice())) {
 
 					Integer tempInt = FieldVerifier.verifyInt(textBox.getText());
 					if (!tempInt.equals(-1)) {
 						currentShow.getShow().setTicketPrice(tempInt);
 						AsyncCallback<Void> callback = CallbackFactory
 								.setShowCallback();
 						osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 						textBox.setText(tempInt + "");
 					} else {
 						// fejl //
 					}
 				} else if (label.getText().startsWith(constants.kodaPct())) {
 
					Double tempDouble = FieldVerifier.verifyDouble(textBox.getText());
					if (!tempDouble.equals(-1.)) {
						currentShow.getShow().setKodaPct(tempDouble);
 						AsyncCallback<Void> callback = CallbackFactory
 								.setShowCallback();
 						osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
						textBox.setText(tempDouble + "");
 					} else {
 						// fejl //
 					}
 
 				}
 
 				else if (label.getText().startsWith(constants.VAT())) {
 
 					Integer tempInt = FieldVerifier.verifyInt(textBox.getText());
 					if (!tempInt.equals(-1)) {
 						currentShow.getShow().setVAT(tempInt);
 						AsyncCallback<Void> callback = CallbackFactory
 								.setShowCallback();
 						osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 						textBox.setText(tempInt + "");
 					} else {
 						// fejl //
 					}
 				}
 				else if (label.getText().startsWith(constants.ticketsSold())) {
 
 					Integer tempInt = FieldVerifier.verifyInt(textBox
 							.getText());
 					if (!tempInt.equals(-1)) {
 						currentShow.getShow().setTicketsSold(tempInt);
 						AsyncCallback<Void> callback = CallbackFactory
 								.setShowCallback();
 						osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 						textBox.setText(tempInt + "");
 					} else {
 						// fejl //
 					}
 				} 
 				else if (label.getText().startsWith(constants.comments())){
 					currentShow.getShow().setComments(textBox.getText());
 					AsyncCallback<Void> callback = CallbackFactory
 							.setShowCallback();
 					osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 					textBox.setText(textBox.getText());
 				}
 			}
 		});
 	}
 	public static void addFieldEventsListBox(final ShowWidget currentShow, final ListBox listBox,final Label label) {
 		final PullPitConstants constants = GWT.create(PullPitConstants.class);
 		final ObjectServiceAsync osa = GWT.create(ObjectService.class);
 		listBox.addBlurHandler(new BlurHandler() {
 			
 			@Override
 			public void onBlur(BlurEvent event) {
 
 				if (label.getText().startsWith(constants.state())) {
 					Integer tempInt = listBox.getSelectedIndex();
 					currentShow.getShow().setState(tempInt);
 					AsyncCallback<Void> callback = CallbackFactory.setShowCallback();
 					osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 					listBox.setSelectedIndex(tempInt);
 				}
 				else if (label.getText().startsWith(constants.feeCurrency())) {
 					Integer tempInt = listBox.getSelectedIndex();
 					currentShow.getShow().setFeeCurrency(tempInt);
 					AsyncCallback<Void> callback = CallbackFactory.setShowCallback();
 					osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 					listBox.setSelectedIndex(tempInt);
 
 				}
 				else if (label.getText().startsWith(constants.provisionCurrency())) {
 					Integer tempInt = listBox.getSelectedIndex();
 					currentShow.getShow().setProvisionCurrency(tempInt);
 					AsyncCallback<Void> callback = CallbackFactory.setShowCallback();
 					osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 					listBox.setSelectedIndex(tempInt);
 				}
 				
 				else if (label.getText().startsWith(constants.productionType())) {
 					Integer tempInt = listBox.getSelectedIndex();
 					currentShow.getShow().setProductionType(tempInt);
 					AsyncCallback<Void> callback = CallbackFactory.setShowCallback();
 					osa.updateShow(UIMain.getInstance().getUserName(),currentShow.getShow(), callback);
 					listBox.setSelectedIndex(tempInt);
 				}
 			}
 			
 		});
 	}
 }
