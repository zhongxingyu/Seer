 package com.sri.straylight.client.controller;
 
 import java.util.Vector;
 
 import org.bushe.swing.event.EventBus;
 import org.bushe.swing.event.annotation.EventSubscriber;
 
 import com.sri.straylight.client.event.SimStateClientRequest;
 import com.sri.straylight.client.model.ClientConfig;
 import com.sri.straylight.client.util.FmuConnectionAbstract;
 import com.sri.straylight.client.util.FmuConnectionLocal;
 import com.sri.straylight.client.util.FmuConnectionRemote;
 import com.sri.straylight.fmuWrapper.event.ConfigChangeRequest;
 import com.sri.straylight.fmuWrapper.event.ScalarValueChangeRequest;
 import com.sri.straylight.fmuWrapper.event.SimStateClientNotify;
 import com.sri.straylight.fmuWrapper.event.SimStateNativeNotify;
 import com.sri.straylight.fmuWrapper.framework.AbstractController;
 import com.sri.straylight.fmuWrapper.voNative.ConfigStruct;
 import com.sri.straylight.fmuWrapper.voNative.ScalarValueRealStruct;
 import com.sri.straylight.fmuWrapper.voNative.SimStateNative;
 
 
 /**
  * The Class SimulationController.
  */
 public class SimulationController extends BaseController  {
 
 	private FmuConnectionAbstract fmuConnect_;
     private ClientConfig configModel_;
     private SimStateNative simStateNative_ = SimStateNative.simStateNative_0_uninitialized;
     
     
 	/**
 	 * Instantiates a new simulation controller.
 	 *
 	 * @param parentController the parent controller
 	 * @param configModel the config model
 	 */
 	public SimulationController (AbstractController parentController, ClientConfig configModel) {
         super(parentController);
         configModel_ = configModel;
         
         
         if (configModel_.autoConnectFlag) {
         	fireSimStateRequest_(SimStateNative.simStateNative_1_connect_requested);
         }
 	}
 	
 	
 	public SimulationController(ClientConfig configModel) {
 		super(null);
         configModel_ = configModel;
 	}
 
 	
 	
 
 	/**
 	 * Connect_.
 	 */
 	private void connect_() {
 		
 		if (configModel_.connectTo == null) {
 		    throw new IllegalArgumentException("configModel_.connectTo");
 		}
 
     	switch (configModel_.connectTo) {
 			case connectTo_localhost :
 				fmuConnect_ = new FmuConnectionRemote("localhost");
 				break;
 			case connectTo_wintermute_straylightsim_com :
 				fmuConnect_ = new FmuConnectionRemote("wintermute.straylightsim.com");
 				break;
 			case connectTo_dev_straylightsim_com :
				fmuConnect_ = new FmuConnectionRemote("192.168.0.15");
 				break;
 			case connectTo_file :
 				fmuConnect_ = new FmuConnectionLocal();
 				break;
     	}
     	
 
     }
 	
 	
 	@EventSubscriber(eventClass = ConfigChangeRequest.class)
 	public void onConfigChangeRequest(ConfigChangeRequest event) {
 
 		ConfigStruct configStruct = event.getPayload();
 		fmuConnect_.setConfig(configStruct);
 	}
 	
 	
 
 	@EventSubscriber(eventClass = SimStateNativeNotify.class)
 	public void onSimStateNativeNotify(SimStateNativeNotify event) {
 
 		SimStateClientNotify newEvent = 
 				new SimStateClientNotify(this,event.getPayload());
 		
 		EventBus.publish(newEvent);
 
 	}
 
 	
 	@EventSubscriber(eventClass=ScalarValueChangeRequest.class)
     public void onInputChangeRequest(ScalarValueChangeRequest event) {
 		Vector<ScalarValueRealStruct> list = event.getPayload();
 		fmuConnect_.setScalarValues(list);
 	}
 	
 	
 	//this event comes from the GUI
 	@EventSubscriber(eventClass=SimStateClientRequest.class)
     public void onSimStateClientRequest(SimStateClientRequest event) {
 		
 		SimStateNative requestedState = event.getPayload();
 		
 //		switch (requestedState) {
 //			case simStateNative_1_connect_requested :
 //				connect_();
 //				break;
 //			default:
 //				fmuConnect_.requestStateChange(requestedState);
 //		}
 		
 		if (requestedState == SimStateNative.simStateNative_1_connect_requested ) {
 			connect_();
 		}
 		
 		fmuConnect_.requestStateChange(requestedState);
 		
     }
 	
 	
 	//this event comes from the FMU engine
 	@EventSubscriber(eventClass=SimStateClientNotify.class)
     public void onSimStateNotify(SimStateClientNotify event) {
 		
 		simStateNative_ = event.getPayload();
 		
 		switch (simStateNative_) {
 			case simStateNative_1_connect_completed:
 		        if (configModel_.autoParseXMLFlag) {
 		        	fireSimStateRequest_(SimStateNative.simStateNative_2_xmlParse_requested);
 		        }
 				break;
 			case simStateNative_2_xmlParse_completed :
 		        if (configModel_.autoInitFlag) {
 		        	fireSimStateRequest_(SimStateNative.simStateNative_3_init_requested);
 		        }
 				break;
 			default:
 				break;
 		}
 	}
 	
 	
 
 	private void fireSimStateRequest_(SimStateNative simStateNative)
 	{
 		SimStateClientRequest event = new SimStateClientRequest(this, simStateNative);
 		EventBus.publish(event);
 		
 	}
 	
 
 
 	
 	
 }
     
     
