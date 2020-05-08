 package org.cotrix.web.client;
 
 import org.cotrix.web.client.view.CodeListManagerView;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class CodeListManager implements EntryPoint {
 	/**
 	 * The message displayed to the user when the server cannot be reached or
 	 * returns an error.
 	 */
 	private static final String SERVER_ERROR = "An error occurred while "
 			+ "attempting to contact the server. Please check your network "
 			+ "connection and try again.";
 
 	/**
 	 * Create a remote service proxy to talk to the server-side Greeting
 	 * service.
 	 */
 	private final CodeListServiceAsync codeListService = GWT
 			.create(CodeListService.class);
 
 	private final Messages messages = GWT.create(Messages.class);
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 		// Get rid of scrollbars, and clear out the window's built-in margin,
 		// because we want to take advantage of the entire client area.
 		Window.enableScrolling(false);
 		Window.setMargin("0px");
 
 		CodeListManagerView codeListManagerView = new CodeListManagerView();
 		codeListManagerView.setSize("100%", "100%");
 		
		RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
 		rootLayoutPanel.add(codeListManagerView);
 	}
 }
