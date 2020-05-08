 package me.deal.client.view.main;
 
 import java.util.ArrayList;
 
 import me.deal.client.events.DealsEvent;
 import me.deal.client.events.DealsEventHandler;
 import me.deal.client.model.Deals;
 import me.deal.client.servlets.DealServiceAsync;
 import me.deal.client.servlets.DirectionsServiceAsync;
 import me.deal.shared.Deal;
 import me.deal.shared.LatLngCoor;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiConstructor;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ScrollPanel;
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
     
     private final ScrollPanel mainScrollPanel;
     private Boolean dealsLoaded = false;
     private final Integer DEFAULT_NUM_DEALS = 10;
     private Boolean initialLoad=true;
     private boolean mapView = false;
     
     public void setMapSize(boolean mapView)
     {
         this.mapView = mapView;
     }
     
     public @UiConstructor ListWidget(final ScrollPanel mainScrollPanel,
             final DealServiceAsync dealService,
             final DirectionsServiceAsync directionsService,
             final HandlerManager eventBus) {
         initWidget(uiBinder.createAndBindUi(this));
         this.mainScrollPanel = mainScrollPanel;
         this.dealService = dealService;
         this.directionsService = directionsService;
         this.eventBus = eventBus;
         initialize();
     }
     
     private String getDuplicateURL(ArrayList<Deal> deals, Deal current, int size)
     {
         LatLngCoor test = current.getBusinessAddress().getLatLng();
         for(int i = 0; i < size; i++)
         {
             double lat1 = test.getLatitude();
             double lat2 = deals.get(i).getBusinessAddress().getLatLng().getLatitude();
             double lon1 = test.getLongitude();
             double lon2 = deals.get(i).getBusinessAddress().getLatLng().getLongitude();
             
             if(lat1 == lat2 && lon1 == lon2)
             {    
                 return deals.get(i).getIDUrl();
             }
         }
         return "";
 
     }
     
     private void initialize() {
         /*
          * TODO: Add code to observe the DealsData model and automatically
          * update the items in the listItemContainer dynamically to reflect
          * changes in the model.
          * 
          */
         
         
         mainScrollPanel.addScrollHandler(new ScrollHandler() {
             @Override
             public void onScroll(ScrollEvent event) {
                 if(dealsLoaded) {
                     int maxPos = mainScrollPanel.getMaximumVerticalScrollPosition();
                     int currPos = mainScrollPanel.getVerticalScrollPosition();
                     
                     if(currPos > maxPos - 1000) {
                         dealsLoaded = false;
                     }
                 }
             }
         });
         
         eventBus.addHandler(DealsEvent.TYPE,
                 new DealsEventHandler() {
                 @Override
                 public void onDeals(DealsEvent event) {
                     loadingSpinnerImage.setVisible(true);
                     
                     listItemContainer.setVisible(false);
                    
                     final ArrayList<Deal> deals = Deals.getInstance().getDeals();
                 
                     
                     if(initialLoad)
                     {
                         
                         for(int i=0;i<deals.size();i++){
                             System.out.println("before item widget");
                             ListItemWidget item=new ListItemWidget();
                             System.out.println("after item widget");
                             item.setMapButton(i, deals.get(i).getIDUrl(), eventBus);
                             System.out.println("before add");
                             listItemContainer.add(item);
                             System.out.println("after add");
                         }
                         initialLoad=false;
                     }
                     
                     // if there are not enough widget as the number of deals, create them
                     if(deals.size()> listItemContainer.getWidgetCount())
                     { int lessCount=deals.size()-listItemContainer.getWidgetCount();
                         while(lessCount>0)
                         {
                             listItemContainer.add(new ListItemWidget());
                             lessCount--;
                         }
                     }
                     
                     System.out.println("after intial load");
                     int i=0;
                     System.out.println(deals.size());
                     Deals.getInstance().setDuplicates(0);
                     for(Deal deal:deals)
                     {    System.out.println("inside for loop ");
                         
                         ListItemWidget temp=(ListItemWidget)listItemContainer.getWidget(i);
                         System.out.println("Added " + temp.getTitle());
                         if (deal.getDealBusinessInfo() != null)
                         {
                             temp.setAvgRatingImageUrl(deal.getDealBusinessInfo().getAvgRatingImageUrl());
                             temp.setBusinessName(deal.getDealBusinessInfo().getName());
                             temp.setNumReviews(deal.getDealBusinessInfo().getNumReviews());
                             temp.setReviewsUrl(deal.getDealBusinessInfo().getWebUrl());
                         }
 
                         temp.setTitle(deal.getTitle());
                         temp.setSubtitle(deal.getSubtitle());
                         temp.setPrice(deal.getPrice());
                         temp.setDiscountPercentage(deal.getDiscountPercentage());
                         temp.setBusinessAddress(deal.getBusinessAddress());
                         temp.setBigImageUrl(deal.getBigImageUrl());
                         temp.setYipitUrl(deal.getYipitWebUrl());
                         temp.setEndDate(deal.getEndDate());
                         temp.setDealSource(deal.getDealSource());
                         String url = getDuplicateURL(deals, deals.get(i), i);
                         if(url != "")
                         {
                             Deals.getInstance().setDuplicates(Deals.getInstance().getDuplicates() + 1);
                            deals.get(i).setIDUrl(url);
                         }
                         else if(i < 26)
                         {
                             if(mapView)
                             {
                                 try
                                 {
                                     deals.get(i).setIDUrl("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + deals.get(i).getColor());
                                 }
                                 catch(NullPointerException n)
                                 {
                                     deals.get(i).setIDUrl("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|FFFFFF");
 
                                 }
                             }
                             else
                             {
                                 deals.get(i).setIDUrl("http://www.google.com/mapfiles/marker" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(i - Deals.getInstance().getDuplicates(), i - Deals.getInstance().getDuplicates() + 1) + ".png" );
                             }
                         }
                         temp.setIcon(deals.get(i).getIDUrl());
                         //listItemContainer.getWidget(i)
                         i++;
                     }
                     /*
                     i=0;
                     
                     for(Deal deal:deals)
                     {
                         ListItemWidget temp=(ListItemWidget)listItemContainer.getWidget(i);
                         temp.setBigImageUrl(deal.getBigImageUrl());
                     }
                     */
                     /*
                     final Timer dealTimer = new Timer() {
                         Integer dealIndex = 0;
                         public void run() {
                             if(dealIndex != deals.size()) {
                                 
                                 listItemContainer.add(new ListItemWidget(deals.get(dealIndex)));
                                 this.schedule(1);
                                 dealIndex++;
                             }
                         }
                     };
                     dealTimer.schedule(1);
                     */
                     listItemContainer.setVisible(true);
                     loadingSpinnerImage.setVisible(false);
                     dealsLoaded = true;
                 }
         });
     }
 }
