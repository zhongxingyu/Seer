 /**
  * 
  */
 package cc.warlock.rcp.userstreams;
 
 import cc.warlock.rcp.ui.client.SWTWarlockClientListener;
 import cc.warlock.rcp.views.StreamView;
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IStyledString;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.IWarlockClientListener;
 import cc.warlock.core.client.WarlockClientRegistry;
 //import cc.warlock.core.client.IStreamListener;
 
 /**
  * @author Will Robertson
  *
  */
 public class UserStream extends StreamView implements IWarlockClientListener {
 	
 	public void clientActivated(IWarlockClient client) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void streamReceivedText (IStream stream, IStyledString text) {
 		super.streamReceivedText(stream, text);
 	}
 
 	public void streamPrompted(IStream stream, String prompt) {
 		super.streamPrompted(stream, prompt);
 	}
 	
 	public void clientConnected(IWarlockClient client) {
 		//mainStream = client.getDefaultStream();
		//client.getDefaultStream().addStreamListener(this);
 		this.addStream(client.getDefaultStream());
 	}
 	
 	public void clientDisconnected(IWarlockClient client) {
 		//mainStream = null;
		// 	client.getDefaultStream().removeStreamListener(this);
 	}
 
 	
 	public void clientRemoved(IWarlockClient client) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public UserStream() {
 		// Constructor
 		super();
 		WarlockClientRegistry.addWarlockClientListener(new SWTWarlockClientListener(this));
 	}
 }
