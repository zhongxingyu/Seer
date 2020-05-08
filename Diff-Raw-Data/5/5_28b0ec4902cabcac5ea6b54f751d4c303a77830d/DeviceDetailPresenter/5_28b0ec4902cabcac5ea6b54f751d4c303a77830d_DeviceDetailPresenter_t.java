 package com.scurab.gwt.rlw.client.presenter;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.scurab.gwt.rlw.client.DataServiceAsync;
 import com.scurab.gwt.rlw.client.RemoteLogWeb;
 import com.scurab.gwt.rlw.client.view.DeviceDetailView;
 import com.scurab.gwt.rlw.shared.model.Device;
 
 public class DeviceDetailPresenter extends TabBasePresenter {
 
     private static final int MAX_LENGTH = 64;
     private Device mDevice;
     private DeviceDetailView mDisplay;
     private String mApp;
     private HTMLPanel mContainer;
     
     public DeviceDetailPresenter(DataServiceAsync dataService, HandlerManager eventBus, String appName,
             HTMLPanel tabPanel) {
         super(dataService, eventBus, appName, tabPanel); mDataService = dataService;
         
         mEventBus = eventBus;
         mApp = appName;
         mContainer = tabPanel;
         
         init();
         bind();
     }
     
     private void bind() {
         mDisplay.getRawDetailButton().addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 openRawDetail();
             }
         });
     }
 
     private void init(){
         mDisplay = onCreateView();
         mContainer.add(mDisplay);
     }    
     
 
     private DeviceDetailView onCreateView() {
        return new DeviceDetailView();
     }
 
     public Device getDevice() {
         return mDevice;
     }
 
     public void setDevice(Device device) {
         mDevice = device;
         initValues(device);
     }
 
     private void initValues(Device v){
         DeviceDetailView d = mDisplay;
         if(v == null){//simple handle for null values
             v = new Device();
         }
         d.getID().setText(getStringValue(v.getDeviceID()));
         d.getBrand().setText(getStringValue(v.getBrand()));
         d.getDescription().setText(getStringValue(v.getDescription()));
         d.getDetail().setText(getStringValue(v.getDetail()));
         d.getDevUUID().setText(getStringValue(v.getDevUUID()));
         d.getModel().setText(getStringValue(v.getModel()));
         d.getOSDescription().setText(getStringValue(v.getOSDescription()));
         d.getOwner().setText(getStringValue(v.getOwner()));
         d.getPlatform().setText(getStringValue(v.getPlatform()));
         d.getPushID().setText(getStringValue(v.getPushID()));
         d.getResolution().setText(getStringValue(v.getResolution()));
         d.getVersion().setText(getStringValue(v.getVersion()));
         d.getApp().setText(getStringValue(v.getApp()));
         d.getCreated().setText(getStringValue(v.getCreatedText()));
         d.getUpdated().setText(getStringValue(v.getUpdatedText()));
         d.getAppVersion().setText(getStringValue(v.getAppVersion()));
     }
     
     private String getStringValue(Object o){
         if(o == null){
             return "";
         }else{
             String s = String.valueOf(o);
             if(s.length() > MAX_LENGTH){
                 s = s.substring(0,MAX_LENGTH) + "...";
             }
             return s;
         }
     }
     
     protected void openRawDetail(){
         if(mDevice != null){
            String url = "/regs/nice/" + mDevice.getDeviceID();
             Window.open(url , "_blank", null);
         }
     }
 
 }
