 /*
  * Copyright 2013 Healthometry Software Inc.
  */
 
 package com.healthometry.client.openid.selector;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.event.shared.HandlerRegistration;
 
 /**
  * 
  * @author Boris Brudnoy
  * 
  */
 public class OpenIdSelector extends Composite implements HasLoginRequestedHandlers {
 
 
    @UiField
    Frame loginFrame;
 
    private static OpenIdSelectorUiBinder uiBinder = GWT.create(OpenIdSelectorUiBinder.class);
 
    interface OpenIdSelectorUiBinder extends UiBinder<Widget, OpenIdSelector> {}
 
 
    public OpenIdSelector() {
       initWidget(uiBinder.createAndBindUi(this));
       loginFrame.setUrl(GWT.getModuleBaseForStaticFiles() + "lib/osel/signin.html");
       setupRequestLoginFunc();
    }
 
 
   /*
    * TODO: Once GWT 2.6 is out and issue https://code.google.com/p/google-web-toolkit/issues/detail?id=7079 is fixed,
    * the return type could become the new com.google.web.bindery.event.shared.HandlerRegistration instead of the
    * currently used legacy one.
    */
    @Override
    public HandlerRegistration addLoginRequestedHandler(LoginRequestedEvent.Handler handler) {
       return addHandler(handler, LoginRequestedEvent.getType());
    }
 
 
    private void requestLogin(String provider) {
       fireEvent(new LoginRequestedEvent(provider));
    }
 
 
    private native void setupRequestLoginFunc() /*-{
 		var widget = this;
 		$wnd.requestLogin = $entry(function(provider) {
 			widget.@com.healthometry.client.openid.selector.OpenIdSelector::requestLogin(Ljava/lang/String;)(provider);
 		});
    }-*/;
 
 }
