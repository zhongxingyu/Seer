 package com.coasterq.client.data;
 
 import com.coasterq.client.FacebookCoasterqConnection;
 import com.coasterq.client.connection.CQImageBundle;
 import com.coasterq.client.connection.DataBuilder;
 import com.coasterq.client.connection.LoadManager;
 import com.coasterq.client.connection.LogInUser;
 import com.coasterq.client.connection.LogOutUser;
 import com.coasterq.client.connection.ReleaseServiceLocations;
 import com.coasterq.client.connection.ServiceLocations;
import com.coasterq.client.connection.TestServiceLocations;
 import com.coasterq.client.view.MainTabPanel;
 import com.coasterq.client.view.ParkTitlePanel;
 import com.finfrock.client.LocationManager;
 import com.finfrock.client.Returnable;
 import com.finfrock.client.UserPanel;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class GUIBuilder
 { 
    // --------------------------------------------------------------------------
    // Private Class Data
    // --------------------------------------------------------------------------
   
    private static String DIV_TAG = "main";
    
    // --------------------------------------------------------------------------
    // Public Members
    // --------------------------------------------------------------------------
   
    public void build()
    {
       final ServiceLocations serviceLocations = createServiceLocations();
       
       LoadManager loadManager = new LoadManager(serviceLocations);
       
       loadManager.startLoad(new Returnable<DataBuilder>()
       {
          @Override
          public void returned(DataBuilder dataBuilder)
          {
             VerticalPanel mainPanel = createMainPanel(
                dataBuilder, serviceLocations);
             
             RootPanel.get(DIV_TAG).getElement().setInnerHTML("");
             RootPanel.get(DIV_TAG).add(mainPanel);
          }
       });
    }
    
    // --------------------------------------------------------------------------
    // Private Members
    // --------------------------------------------------------------------------
    
    private VerticalPanel createMainPanel(DataBuilder dataBuilder,
                          ServiceLocations serviceLocations)
    {
       RideManagerable rideManager = dataBuilder.getRideManager();
       ItineraryRideManagerable itineraryRideManager = dataBuilder
          .getItineraryRideManager();
       ParkInformation parkInformation = dataBuilder.getParkInformation();
       ParkClock parkClock = dataBuilder.getParkClock();
       CQImageBundle cqImageBundle = (CQImageBundle) 
          GWT.create(CQImageBundle.class);
       LocationManager locationManager = new LocationManager();
       
       LogInUser logInUser = new LogInUser(serviceLocations, rideManager, 
          itineraryRideManager);
       LogOutUser logOutUser = new LogOutUser(serviceLocations);
       
       ParkLocationManager parkLocationManager = 
          new ParkLocationManager(locationManager, parkInformation);
       
       FacebookCoasterqConnection facebookConnectionStatus = 
          new FacebookCoasterqConnection(logInUser, logOutUser);
       
       Widget tabPanel = new MainTabPanel(serviceLocations, rideManager, 
          itineraryRideManager, parkInformation, parkClock, cqImageBundle, 
          facebookConnectionStatus, parkLocationManager);
       
       final VerticalPanel mainPanel = new VerticalPanel();
       
       mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
       
       UserPanel userPanel = new UserPanel(facebookConnectionStatus);
       
       mainPanel.add(userPanel);
       
       mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
       
       mainPanel.add(new ParkTitlePanel(parkInformation));
 
       mainPanel.add(tabPanel);
 
       mainPanel.addStyleName("mainPanel");
 
       return mainPanel;
    }
    
    private ServiceLocations createServiceLocations()
    {
//      return new ReleaseServiceLocations();
      return new TestServiceLocations();
    }
 }
