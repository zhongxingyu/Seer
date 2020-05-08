 package pylos.controller.screen;
 
 import pylos.Pylos;
 import pylos.network.Network;
 import pylos.view.View;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.controls.textfield.controller.TextFieldControl;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 
 public class StartScreenController implements ScreenController {
 	Nifty nifty;
 	Screen screen;
 	View view;
 
 	Element multiplayerPanel;
 	Element networkPanel;
 	TextFieldControl localhostTextField;
 	TextFieldControl remotehostTextField;
 
 	public void bind(Nifty nifty, Screen screen) {
 		this.nifty = nifty;
 		this.screen = screen;
 		view = Pylos.view;
 		multiplayerPanel = ScreenControllerUtils.find(screen, "layer/multi/multiButtons");
 		multiplayerPanel.hideWithoutEffect();
 		networkPanel = ScreenControllerUtils.find(screen, "layer/network/networkTextFields");
 		networkPanel.hideWithoutEffect();
 		localhostTextField = screen.findControl("localhost", TextFieldControl.class);
 		remotehostTextField = screen.findControl("remotehost", TextFieldControl.class);
 	}
 
 	public void onEndScreen() {
 	}
 
 	public void onStartScreen() {
 	}
 
 	public void singlePlayer() {
 		System.out.println("Needs AI implementation ;)");
 	}
 
 	public void multiPlayer() {
 		multiplayerPanel.show();
 	}
 
 	public void startLocalGame() {
 		view.initGame();
 	}
 
 	public void networkGame() {
 		networkPanel.show();
 	}
 
 	public void clearLocalhostLabel() {
 		localhostTextField.setText("");
 	}
 
 	public void clearRemotehostLabel() {
 		remotehostTextField.setText("");
 	}
 
 	public void createNetworkGame() {
 		if (Network.validHost(localhostTextField.getText())) {
 			Pylos.network.createConnections(localhostTextField.getText());
 			view.initGame();
 		} else {
			System.err.println("Local host is invalid: " + localhostTextField.getText());
 		}
 	}
 
 	public void joinNetworkGame() {
 		if (!Network.validHost(localhostTextField.getText())) {
			System.err.println("Local host is invalid: " + localhostTextField.getText());
 		} else if (!Network.validHost(remotehostTextField.getText())) {
			System.err.println("Remote host is invalid: " + remotehostTextField.getText());
 		} else {
 			Pylos.network.createConnections(localhostTextField.getText(), remotehostTextField.getText());
 			view.initGame();
 		}
 	}
 }
