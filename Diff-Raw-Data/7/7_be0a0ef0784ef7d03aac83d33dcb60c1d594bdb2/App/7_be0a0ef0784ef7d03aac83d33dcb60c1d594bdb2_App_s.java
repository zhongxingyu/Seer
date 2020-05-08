 package infrastructure;
 
 import guiobject.BackGround;
 import guiobject.Camera;
 import guiobject.EdwardPopup;
 import guiobject.PopupText;
 import guiobject.ShapeMaker;
 import guiobject.TimePopup;
 import javafx.scene.control.*;
 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Cursor;
 import javafx.scene.Group;
 import javafx.scene.ImageCursor;
 import javafx.scene.Scene;
 import javafx.scene.image.Image;
 import javafx.scene.media.Media;
 import javafx.scene.media.MediaPlayer;
 import javafx.scene.media.MediaView;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Circle;
 import javafx.scene.shape.Polygon;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 import javafx.stage.Popup;
 import javafx.scene.text.Text;
 import entities.*;
 import mousemanagers.DevMouse;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import keymanagers.CreationKeys;
 import keymanagers.DefaultKeys;
 import keymanagers.DevModeKeys;
 import keymanagers.FlyingKeys;
 import keymanagers.InertialKeys;
 import keymanagers.KeyManager;
 import keymanagers.MouseManager;
 import keymanagers.PixelEscapeKeys;
 import keymanagers.SlowKeys;
 
 import org.jbox2d.common.Vec2;
 
 import utils.Parse;
 import utils.Util;
 
 import entities.BouncyBall;
 import entities.Creature;
 import entities.Entity;
 import entities.Floor;
 import entities.Wall;
 
 public class App extends Application {
 	public static GameWorld game;
 	public static Camera camera;
 	public static ShapeMaker shaker;
 	public static Group root;
 	public static Scene scene;
 	public static Stage pS;
 	public static MenuBar menuBar;
 	public static Thread key;
 	public static Thread cam;
 	public static float tC;
 	private MediaView mediaView;
 	public static EdwardPopup reverse;
 	public static EdwardPopup speed;
 	public static EdwardPopup slow;
 
 	public static final List<String> musicList = Arrays.asList(new String[] {
 			"audio/Melancholy.mp3", "audio/ZombieTheme.mp3",
 			"audio/Mysterious.mp3", "audio/Mountain.m4a" });
 	public static final List<String> levelList = Arrays
 			.asList(new String[] { "levels/menu.txt", "levels/1-1.txt",
 					"levels/1-2.txt", "levels/1-3.txt",
 					"levels/1-4.txt", "levels/1-6.txt",
 					"levels/2-1.txt", "levels/2-2.txt",
 					"levels/1-7.txt", "levels/1-5.txt",
 					"levels/1-5-1.txt", "levels/2-3.txt" });
 
 	public static void main(String[] args) throws IOException {
 		launch(args);
 	}
 
 	public static void reverseTime() {
 		App.game.getCurrentMap().killTime();
 		App.game.getCurrentMap().newReverseTime();
 		App.game.getCurrentMap().startTime();
 	}
 
 	public static void PIreverseTime() {
 		App.game.getCurrentMap().killTime();
 		App.game.getCurrentMap().newPIReverseTime();
 		App.game.getCurrentMap().startTime();
 	}
 
 	public static synchronized void setTC(float tC) {
 		App.game.getCurrentMap().killTime();
 		App.game.getCurrentMap().newTime();
 		App.tC = tC;
 		App.game.getCurrentMap().startTime();
 	}
 
 	public static synchronized void speedUp() {
 		if (App.tC != 1.0f / 20.0f) {
 			App.game.getCurrentMap().killTime();
 			App.game.getCurrentMap().newTime();
 			App.tC = 1.0f / 20.0f;
 			App.game.getCurrentMap().startTime();
 		} else {
 			App.game.getCurrentMap().killTime();
 			App.game.getCurrentMap().newTime();
 			App.tC = 1.0f / 60.0f;
 			App.game.getCurrentMap().startTime();
 		}
 		speed.toggle();
 	}
 
 	public static synchronized void slowDown() {
 		if (App.tC != 1.0f / 100.0f) {
 			App.game.getCurrentMap().killTime();
 			App.game.getCurrentMap().newTime();
 			App.tC = 1.0f / 100.0f;
 			App.game.getCurrentMap().startTime();
 		} else {
 			App.game.getCurrentMap().killTime();
 			App.game.getCurrentMap().newTime();
 			App.tC = 1.0f / 60.0f;
 			App.game.getCurrentMap().startTime();
 		}
 		slow.toggle();
 	}
 
 	public static synchronized float getTC() {
 		return tC;
 	}
 
 	public static void toggleRTime() {
 		if (App.game.getCurrentMap().getTime() instanceof ReverseTime) {
 			App.game.getCurrentMap().killTime();
 			App.game.getCurrentMap().newTime();
 			App.game.getCurrentMap().startTime();
 		} else {
 			App.game.getCurrentMap().killTime();
 			App.game.getCurrentMap().newReverseTime();
 			App.game.getCurrentMap().startTime();
 		}
 		reverse.toggle();
 	}
 
 	public static void togglePIRTime() {
 		if (App.game.getCurrentMap().getTime() instanceof ReverseTime2) {
 			App.game.getCurrentMap().killTime();
 			App.game.getCurrentMap().newTime();
 			App.game.getCurrentMap().startTime();
 		} else {
 			App.game.getCurrentMap().killTime();
 			App.game.getCurrentMap().newPIReverseTime();
 			App.game.getCurrentMap().startTime();
 		}
 	}
 
 	@Override
 	public void start(final Stage primaryStage) throws IOException {
 		pS = primaryStage;
 		primaryStage.setTitle("Dreamscape");
 		primaryStage.setFullScreen(false);
 		primaryStage.setResizable(false);
 		tC = 1.0f / 60.0f;
 
 		root = new Group();
 		scene = new Scene(root, Util.WIDTH, Util.HEIGHT);
 
 		game = new GameWorld();
 
 		scene.setCursor(Cursor.CROSSHAIR);
 		camera = new Camera();
 		shaker = new ShapeMaker();
 		MouseManager mouse = new DevMouse();
 		DefaultKeys keyManager = new DefaultKeys();
 		key = new Thread(keyManager.keyThread);
 		cam = new Thread(camera);
 		key.start();
 		cam.start();
 
 		// ball.setVisible(true);
 
 		// game.addMap(new GameMap(new BackGround("maps/1-1.jpg"), 1350, 280,
 		// 36,
 		// 49, 30.0f));
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(1)),
 				getLevelForIndex(1)));
 		// game.addMap(new GameMap(new BackGround("maps/1-2.jpg"), 20, 280, 36,
 		// 49, 30.0f));
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(2)),
 				getLevelForIndex(2)));
 		// game.addMap(new GameMap(new BackGround("maps/1-3.jpg"), 1200, 270,
 		// 36,
 		// 49, 30.0f));
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(3)),
 				getLevelForIndex(3)));
 		// game.addMap(new GameMap(new BackGround("maps/1-4.png"), 900, 100, 36,
 		// 49, 0));
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(4)),
 				getLevelForIndex(4)));
 		// game.addMap(new GameMap(new BackGround("maps/1-4.png"), 900, 100, 36,
 		// 49, 0));
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(5)),
 				getLevelForIndex(5)));
 		// game.addMap(new GameMap(new BackGround("maps/1-7.jpg"), 1200, 237,
 		// 10,
 		// 49, 30.0f));
 		// game.getMaps().get(5).addCoreElements();
 		// for (int a = 0; a < 210; a += 10) {
 		// BouncyBall bouncy = new BouncyBall(a, 90, 8, Color.BLUE);
 		// bouncy.addToMap(game.getMaps().get(5));
 		// }
 		// for (int a = 0; a < 311; a += 5) {
 		// BouncyBall bouncy = new BouncyBall(30, a, 8, Color.WHITE);
 		// bouncy.addToMap(game.getMaps().get(5));
 		// }
 		// game.addMap(new GameMap(new BackGround("maps/2-2.jpg"), 1150, 100,
 		// 36,
 		// 49, 30));
 		// game.getMaps().get(6).addCoreElements();
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(6)),
 				getLevelForIndex(6)));
 		// game.addMap(new GameMap(new BackGround("maps/1-5.jpg"), 1340, 285,
 		// 36,
 		// 49, 40.0f));
 		// App.game.getMaps().get(7).addLeftRightWalls();
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(7)),
 				getLevelForIndex(7)));
 
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(8)),
 				getLevelForIndex(8)));
 
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(9)),
 				getLevelForIndex(9)));
 
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(10)),
 				getLevelForIndex(10)));
 
 		game.addMap(GameMap.parse(this.readFromFile(getLevelForIndex(11)),
 				getLevelForIndex(11)));
 		
 		game.addMap(new GameMap(new BackGround("maps/3-1.jpg"),1000,1000,10,49,30.0f));
 
 		game.getMaps().get(12).setTime(new Ending(game.getMaps().get(12)));
 		//game.addMap(new GameMap(new BackGround("maps/backgrounds.gif"), 900,
 			//	800, 10, 49, 30.0f));
 
 		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
 			@Override
 			public void handle(WindowEvent arg0) {
 				Platform.exit();
 				System.exit(0);
 			}
 		});
 
 		scene.setOnKeyPressed(keyManager.keyPress);
 		scene.setOnKeyReleased(keyManager.keyRelease);
 		scene.setOnMouseClicked(mouse);
 		scene.setOnMouseMoved(mouse);
 		this.mediaView = createMediaView();
 
 		menuBar = new MenuBar();
 
 		// Menu File
 		Menu menuFile = new Menu("File");
 		MenuItem save = new MenuItem("Save Map");
 		MenuItem reset = new MenuItem("Reset Level");
 		MenuItem pause = new MenuItem("Pause Level");
 		pause.setOnAction(new EventHandler<ActionEvent>() {
 			EdwardPopup pop = new EdwardPopup("Paused. ");
 			int i = 0;
 
 			public synchronized void handle(ActionEvent arg0) {
 				App.game.getCurrentMap().getTime().toggleTime();
 				pop.toggle();
 				if (i % 2 == 0)
 					mediaView.getMediaPlayer().pause();
 				else
 					mediaView.getMediaPlayer().play();
 				i++;
 			}
 		});
 		reset.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public synchronized void handle(ActionEvent arg0) {
 				try {
 					App.game.getCurrentMap().reset();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		menuFile.getItems().addAll(save, reset, pause);
 
 		Menu menuEdit = new Menu("Edit");
 		MenuItem devMode = new MenuItem("DevMode");
 		menuEdit.getItems().add(devMode);
 		Menu menuView = new Menu("View");
 		MenuItem zoom = new MenuItem("Zoom");
 		menuView.getItems().add(zoom);
 		menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
 		devMode.setOnAction(new EventHandler<ActionEvent>() {
 			EdwardPopup pop = new EdwardPopup(
 					"This is the feature of our game that allows us to dynamically draw and execute player-defined shapes. To use, draw points with"
 							+ "your mouse, and press 1 or 2 to create the shape, depending on what kind of shape you want. 3 and 4 make shapes that kill the player. ");
 
 			@Override
 			public synchronized void handle(ActionEvent event) {
 				pop.toggle();
 
 			}
 
 		});
 		save.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public synchronized void handle(ActionEvent event) {
 				try {
 					Parse.writeToFile(App.game.getCurrentMap().toString(),
 							"savefile.txt");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 		});
 
 		((Group) scene.getRoot()).getChildren().addAll(menuBar);
 		// audio stuff
 		((Group) scene.getRoot()).getChildren().add(mediaView);
 		primaryStage.setScene(scene);
 		primaryStage.show();
 		/*for(int a = 0; a < 11; a++){
 			game.getMaps().get(a).newNonReversableTime();
 		}*/
 		game.changeMap(game.getMaps().get(1));
 		// game.getCurrentMap().getPhysics()
 		// .setContactListener(new ContactManager());
 		for (GameMap a : game.getMaps()) {
 			a.getPhysics().setContactListener(new ContactManager());
 		}
 		// App.game.changeMap(App.game.getMaps().get(12));
 		// game.getCurrentMap().addCoreElements();
 		// App.game.getMaps().get(7).toggleTime();
 
 		reverse = new TimePopup("Time Reversing...");
 		slow = new TimePopup("Time Slow...");
 		speed = new TimePopup("Time Sped...");
 
 	}
 
 	public MediaView createMediaView() {
 		MediaView mediaView = new MediaView();
 		initMediaPlayer(mediaView, musicList.listIterator());
 		return mediaView;
 	}
 
 	private void initMediaPlayer(final MediaView mediaView,
 			final ListIterator<String> urls) {
 		if (urls.hasNext()) {
 			MediaPlayer mediaPlayer = new MediaPlayer(new Media((new File(
 					urls.next()).toURI().toString())));
 			mediaPlayer.setAutoPlay(true);
 			mediaPlayer.setOnEndOfMedia(new Runnable() {
 				@Override
 				public void run() {
 					initMediaPlayer(mediaView, urls);
 				}
 			});
 			mediaView.setMediaPlayer(mediaPlayer);
 		}
 		// if(!urls.hasNext()&&
 		// !mediaView.getMediaPlayer().getMedia().getTracks().get(0).getName().equals(musicList.get(musicList.size()-1)))
 	}
 
 	public static String getLevelForIndex(int index) {
 		return (levelList.get(index));
 	}
 	public static String readFromFile(String fileName) throws IOException {
 		BufferedReader in = new BufferedReader(
 			    new InputStreamReader(
 			        App.class.getClassLoader().getResourceAsStream(
 			            fileName)));
 //		Path path = Paths.get(fileName);
 //		List<String> listed = Files.readAllLines(path, ENCODING);
 		String result = "";
		while((result=in.readLine())!=null){
			result+="\n";
 		}
 		return result;
 //		for (String str : listed)
 //			result += str + "\n";
 //		return result;
 	}
 //	public static String readFromFile(File file) throws IOException {
 //		List<String> listed = Files.readAllLines(file.toPath(), ENCODING);
 //		String result = "";
 //		for (String str : listed)
 //			result += str + "\n";
 //		return result;
 //	}
 }
