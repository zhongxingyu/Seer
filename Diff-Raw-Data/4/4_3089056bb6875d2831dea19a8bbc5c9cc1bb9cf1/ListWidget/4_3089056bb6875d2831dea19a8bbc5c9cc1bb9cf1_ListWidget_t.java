 package me.deal.client.view.main;
 
 import java.util.ArrayList;
 
 import me.deal.client.events.DealsEvent;
 import me.deal.client.events.DealsEventHandler;
 import me.deal.client.events.DealsLocationEvent;
 import me.deal.client.events.DealsLocationEventHandler;
 import me.deal.client.model.Deals;
 import me.deal.client.model.DealsLocation;
 import me.deal.client.servlets.DealServiceAsync;
 import me.deal.client.servlets.DirectionsServiceAsync;
 import me.deal.shared.Category;
 import me.deal.shared.Deal;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ListWidget extends Composite {
 
 	private static ListWidgetUiBinder uiBinder = GWT
 			.create(ListWidgetUiBinder.class);
 
 	interface ListWidgetUiBinder extends UiBinder<Widget, ListWidget> {
 	}
 
 	@UiField
 	Image loadingSpinnerImage;
 	@UiField
 	VerticalPanel listItemContainer;
 	
 	private final DealServiceAsync dealService;
 	private final DirectionsServiceAsync directionsService;
 	private final HandlerManager eventBus;
 	private Boolean dealsLoaded = false;
 	private final Integer DEFAULT_NUM_DEALS = 20;
 	
	public @UiConstructor ListWidget(final DealServiceAsync dealService,
 			final DirectionsServiceAsync directionsService,
 			final HandlerManager eventBus) {
 		initWidget(uiBinder.createAndBindUi(this));
 		this.dealService = dealService;
 		this.directionsService = directionsService;
 		this.eventBus = eventBus;
 		initialize();
 	}
 	
 	private void initialize() {
 		/*
 		 * TODO: Add code to observe the DealsData model and automatically
 		 * update the items in the listItemContainer dynamically to reflect
 		 * changes in the model.
 		 * 
 		 */
 		
 		eventBus.addHandler(DealsLocationEvent.TYPE,
 				new DealsLocationEventHandler() {
 			@Override
 			public void onDealsLocation(DealsLocationEvent event) {
 				dealService.getYipitDeals(DealsLocation.getInstance().getDealsLocation().getLatLng(),
 						DealsLocation.getInstance().DEFAULT_RADIUS, DEFAULT_NUM_DEALS, new ArrayList<Category>(),
 						new AsyncCallback<ArrayList<Deal>>() {
 							@Override
 							public void onFailure(Throwable caught) {
 								Window.alert("Failed to load deals.");
 							}
 
 							@Override
 							public void onSuccess(ArrayList<Deal> result) {
 								Deals.getInstance().setDeals(result);
 								eventBus.fireEvent(new DealsEvent());
 							}
 				});
 			}
         });
 		
 		eventBus.addHandler(DealsEvent.TYPE,
 				new DealsEventHandler() {
 				@Override
 				public void onDeals(DealsEvent event) {
 					loadingSpinnerImage.setVisible(true);
 					ArrayList<Deal> deals = Deals.getInstance().getDeals();
 					for(Deal deal : deals) {
 						// change null to business info after YELP api is complete
 						ListItemWidget listItem = new ListItemWidget(deal, null); 
 						listItemContainer.add(listItem);
 					}
 					loadingSpinnerImage.setVisible(false);
 				}
 		});
 	}
 }
