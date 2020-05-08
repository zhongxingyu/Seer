 package tighties.client;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.user.client.Window.Location;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class IFramePresenter {
 	public interface Display {
 		HasClickHandlers getButton();
 		void setTighties(int tighties);
 	}
 
 	public IFramePresenter(final Display display) {
 		final String domain = Location.getParameter("r");
 		
 		if (null != domain) {
 			displayCurrentTighties(display, domain);
 			allowTightification(display, domain);
 		}
 	}
 
 	private void allowTightification(final Display display, final String domain) {
 		display.getButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				AsyncProvider.get(new AsyncCallback<TightnessServiceAsync>() {
 					@Override
 					public void onSuccess(final TightnessServiceAsync service) {
 						service.incrementTighties(domain, new AsyncCallback<Integer>() {
 							@Override
 							public void onSuccess(Integer result) {
 								display.setTighties(result);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 							}
 						});
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 					}
 				});
 			}
 		});
 	}
 
 	private void displayCurrentTighties(final Display display, final String domain) {
 		AsyncProvider.get(new AsyncCallback<TightnessServiceAsync>() {
 			@Override
 			public void onSuccess(final TightnessServiceAsync service) {
 				service.getTightnessByDomain(domain, new AsyncCallback<Tightness>() {
 					@Override
 					public void onSuccess(final Tightness tighties) {
 						display.setTighties(tighties.getTighties());
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 					}
 				});
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 			}
 		});
 	}
 }
