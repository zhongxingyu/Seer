 package screenCore;
 
 import math.Vector2;
 import misc.EventArgs;
 import misc.IEventListener;
 import GUI.controls.Button;
 import content.ContentManager;
 import utils.Rectangle;
 import utils.TimeSpan;
 
 public class TestGUIScreen extends TransitioningGUIScreen {
 
 	public TestGUIScreen(String lookAndFeelPath) {
 		super(false, TimeSpan.fromSeconds(1.0f), TimeSpan.fromSeconds(.5f), lookAndFeelPath);
 	}
 	
 	public void initialize(ContentManager manager) {
 		super.initialize(manager);	
 		
 		Rectangle vp = this.ScreenManager.getViewport();
 		int x = vp.Width - 250;
 		
 		Button button0 = new Button();
 		button0.setBounds(x,40,200,50);
 		button0.setText("Singleplayer");
 		this.addGuiControl(button0, new Vector2(vp.Width,40), new Vector2(x,40),new Vector2(vp.Width,40));
 		
 		Button button1 = new Button();
 		button1.setBounds(x,140,200,50);
		button1.setText("Multiplayer");
 		this.addGuiControl(button1, new Vector2(vp.Width,140), new Vector2(x,140),new Vector2(vp.Width,140));
 		
 		Button button2 = new Button();
 		button2.setBounds(x,340,200,50);
 		button2.setText("Exit");
 		this.addGuiControl(button2, new Vector2(vp.Width,340), new Vector2(x,340),new Vector2(vp.Width,340));
 		
 		
 		button0.addClicked(new IEventListener<EventArgs>() {
 			public void onEvent(Object sender, EventArgs e) {
 				gotoTest2();
 			}
 		});
 		
 		button1.addClicked(new IEventListener<EventArgs>() {
 			public void onEvent(Object sender, EventArgs e) {
 				gotoMultiplayer();
 			}
 		});
 		
 		button2.addClicked(new IEventListener<EventArgs>() {
 			public void onEvent(Object sender, EventArgs e) {
 				exit();				
 			}
 		});
 		
 	}
 
 	protected void exit() {
 		System.exit(0);
 	}
 
 	protected void gotoTest2() {
 		this.ScreenManager.addScreen("Test2_Screen");
 	}
 	
 	protected void gotoMultiplayer() {
 		this.ScreenManager.addScreen("PonySelector");
 	}
 
 }
