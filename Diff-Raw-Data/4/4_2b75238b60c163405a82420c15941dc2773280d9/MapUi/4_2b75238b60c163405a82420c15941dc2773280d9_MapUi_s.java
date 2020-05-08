 package de.futjikato.mrwhiz.map;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.lwjgl.LWJGLException;
 
 import de.futjikato.mrwhiz.App;
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.Label;
 import de.matthiasmann.twl.Widget;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.theme.ThemeManager;
 
 public class MapUi extends Widget {
 
 	private MapPlayer player;
 
 	private GUI gui;
 	private ThemeManager theme;
 
 	// ui elements
 	private Label fps;
 	private long fpsValue;
 
 	private Label playerCoords;
 
 	public MapUi(MapPlayer player) throws LWJGLException {
 		this.player = player;
 
 		// init ui
 		this.initUi();
 
 		// init gui
 		LWJGLRenderer renderer = new LWJGLRenderer();
 		this.gui = new GUI(this, renderer);
 
 		try {
 			// load theme
 			URL res = App.class.getClassLoader().getResource("themes/whiz.xml");
 			this.theme = ThemeManager.createThemeManager(res, renderer);
 			this.gui.applyTheme(this.theme);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void initUi() {
 
 		if (App.getInstance().isDebug()) {
 			this.initDebugUi();
 		}
 
 		// init fps label
 		this.fps = new Label();
 		this.fps.setText(String.format("FPS: %d", this.fpsValue));
 		this.add(this.fps);
 	}
 
 	private void initDebugUi() {
 		// init player coordinate label
 		this.playerCoords = new Label();
 		this.playerCoords.setText(String.format("You´re at : ( %d/%d )", this.player.getXBlock(), this.player.getYBlock()));
 		this.add(this.playerCoords);
 	}
 
 	@Override
 	protected void layout() {
 
 		if (App.getInstance().isDebug()) {
 			this.layoutDebugElements();
 		}
 
 		// fps label
 		this.fps.setPosition(20, 20);
 		this.fps.adjustSize();
 	}
 
 	private void layoutDebugElements() {
 		// player coordinate label
 		this.playerCoords.setPosition(20, 40);
 		this.playerCoords.adjustSize();
 	}
 
 	public void update() {
 		// update fps label
 		this.fps.setText(String.format("FPS: %d", this.fpsValue));
 
 		// update player position
		this.playerCoords.setText(String.format("You´re at : ( %d/%d )", this.player.getXBlock(), this.player.getYBlock()));
 
 		this.gui.update();
 	}
 
 	public void setFps(long fps) {
 		this.fpsValue = fps;
 	}
 }
