 package engenhoka.balloons;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import javafx.animation.KeyFrame;
 import javafx.animation.KeyValue;
 import javafx.animation.Timeline;
 import javafx.application.Application;
 import javafx.beans.property.BooleanProperty;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.ToggleGroup;
 import javafx.scene.effect.InnerShadow;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 import javafx.scene.transform.Transform;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 
 public class BalloonsGame extends Application {
 	private static final int LOGO_SHOLD_BE_REMOVED = 0x8000;
 	private static final int BONUS_INDEX = Resources.LOGO_COUNT;
 	private static final int INITIAL_VELOCITY = 350;
 	private static final int LOGO_HEIGHT = 114;
 	private static final int LOGO_WIDTH = 261;
 	private static final int MAX_TIME = 30;
 	private static final double BALLOON_SCALE_Y = 0.5;
 	private static final double BALLOON_SCALE_X = 0.5;
 
 	private static final double DELTA_TIME = 0.03333;
 	
 	public static BalloonsGame game;
 	
 	private List<Balloon> balloons = new ArrayList<Balloon>();
 	
 	private int[] score = new int[Resources.LOGO_COUNT];
 	private GameState state;
 	private Group currentScene;
 
 	private Group root;
 	private double timeSpan;
 
 	private Random random = new Random(System.currentTimeMillis());
 	
 	private Timeline balloonsTimeline;
 	private Timeline clockTimeline;
 
 	private double velocity = INITIAL_VELOCITY;
 
 	private int countDown;
 	
 	private Scene scene;
 
 	private int bonusCount;
 	private long currentTimestamp;
 	
 	public static void main(String[] args) {
 		launch(args);
 	}
 	
 	@Override
 	public void start(Stage stage) throws Exception {
 		game = this;
 		
 		stage.setTitle("Balloons Game");
 		
 		stage.setWidth(1024);
 		stage.setHeight(768);
 		stage.setResizable(false);
 		stage.setFullScreen(true);
 		stage.getIcons().add(Resources.icon);
 
 		root = new Group();
 		scene = new Scene(root);
 		scene.setFill(Color.BLUE);
 		stage.setScene(scene);
 		
 		Background background = new Background();
 		background.widthProperty().bind(scene.widthProperty());
 		background.heightProperty().bind(scene.heightProperty());
 		root.getChildren().add(background);
 		
 		final ImageView logo = new ImageView(Resources.logo);
 		logo.setScaleX(0.4);
 		logo.setScaleY(0.4);
 		logo.setTranslateX(340);
 		logo.setTranslateY(590);
 		
 		root.getChildren().add(logo);
 		
 		balloonsTimeline = new Timeline();
 		balloonsTimeline.setCycleCount(Timeline.INDEFINITE);
 		
 		KeyFrame kf = new KeyFrame(Duration.millis(33.33), new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent event) {
 			update();
 			logo.toFront();
 		}});
 		
 		balloonsTimeline.getKeyFrames().add(kf);
 		
 		changeState(GameState.INTRO);
 		
 		stage.show();
 	}
 	
 	private void changeState(GameState newState) {
 		if(currentScene != null)
 			currentScene.getChildren().clear();
 		
 		root.getChildren().remove(currentScene);
 		
 		switch(newState) {
 		case INTRO:
 			currentScene = createIntro();
 			break;
 			
 		case MENU:
 			currentScene = createMenu();
 			break;
 			
 		case PLAYING:
 			currentScene = startGame();
 			break;
 			
 		case WINNING:
 			currentScene = winningGame();
 			break;
 			
 		case LOSE:
 			currentScene = loseGame();
 			break;
 			
 		default:
 			break;
 		}
 		
 		state = newState;
 		root.getChildren().add(currentScene);
 	}
 
 	private Group createIntro() {
 		Group group = new Group();
 		
 		ImageView engenhoka = new ImageView(Resources.engenhoka);
 		engenhoka.translateXProperty().bind(scene.widthProperty().divide(2).subtract(Resources.engenhoka.getWidth() / 2));
 		engenhoka.translateYProperty().bind(scene.heightProperty().divide(2).subtract(Resources.engenhoka.getHeight() / 2));
 		engenhoka.setOpacity(0);
 		group.getChildren().add(engenhoka);
 		
 		Timeline timeline = new Timeline();
 		timeline.setCycleCount(1);
 		
 		KeyFrame kf0 = new KeyFrame(Duration.millis(3000), new KeyValue(engenhoka.opacityProperty(), 1));
 		KeyFrame kf1 = new KeyFrame(Duration.millis(6000), new KeyValue(engenhoka.opacityProperty(), 1));
 		KeyFrame kf2 = new KeyFrame(Duration.millis(7000), new KeyValue(engenhoka.opacityProperty(), 0));
 		
 		timeline.getKeyFrames().addAll(kf0, kf1, kf2);
 		
 		timeline.setOnFinished(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent event) {
 			changeState(GameState.MENU);
 		}});
 		
 		timeline.play();
 		
 		return group;
 	}
 
 	private Group loseGame() {
 		Resources.trumpet.play();
 		clockTimeline.stop();
 		balloonsTimeline.stop();
 		
 		Group group = new Group();
 		
		Text text = new Text("Não foi desta vez!!");
 		text.setFont(Font.font("Verdana", FontWeight.BOLD, 90));
 		text.setFill(Color.WHITE);
 		text.setStroke(Color.BLACK);
 		text.setTranslateX(70);
 		text.setTranslateY(240);
 		group.getChildren().add(text);
 		
 		final Button playButton = new Button("Jogar novamente");
 		playButton.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
 		playButton.setTranslateX(260);
 		playButton.setTranslateY(300);
 		group.getChildren().add(playButton);
 		
 		final BooleanProperty hardModeSelected = showModes(group, playButton, 30);
 		
 		playButton.setOnMousePressed(new EventHandler<MouseEvent>() { @Override public void handle(MouseEvent e) {
 			changeState(GameState.PLAYING);
 			hardMode = hardModeSelected.get();
 		}});
 		
 		ImageView iwatinha = new ImageView(Resources.iwatinha_sad);
 		iwatinha.setTranslateX(10);
 		iwatinha.setTranslateY(250);
 		group.getChildren().add(iwatinha);
 		
 		Credits credits = new Credits();
 		credits.setTranslateX(280);
 		credits.setTranslateY(500);
 		group.getChildren().add(credits);
 
 		return group;
 	}
 
 	private BooleanProperty showModes(Group group, final Button playButton, double xAxisDeviation) {
 		final ToggleGroup tgroup = new ToggleGroup();
 
 		final RadioButton easyRadioButton = new RadioButton("Easy");
 		easyRadioButton.setToggleGroup(tgroup);
 		easyRadioButton.setSelected(!hardMode);
 		easyRadioButton.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
 		easyRadioButton.translateXProperty().bind(playButton.translateXProperty().add(xAxisDeviation));
 		easyRadioButton.translateYProperty().bind(playButton.translateYProperty().subtract(-100));
 
 		final RadioButton hardRadioButton = new RadioButton("Hard");
 		hardRadioButton.setToggleGroup(tgroup);
 		hardRadioButton.setSelected(hardMode);
 		hardRadioButton.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
 		hardRadioButton.translateXProperty().bind(easyRadioButton.translateXProperty());
 		hardRadioButton.translateYProperty().bind(easyRadioButton.translateYProperty().subtract(-40));
 		
 		group.getChildren().add(easyRadioButton);
 		group.getChildren().add(hardRadioButton);
 		
 		return hardRadioButton.selectedProperty();
 	}
 
 	private Group winningGame() {
 		Resources.applause.play();
 		clockTimeline.stop();
 		balloonsTimeline.stop();
 		
 		Group group = new Group();
 		
		Text text = new Text("Você ganhou!!");
 		text.setFont(Font.font("Verdana", FontWeight.BOLD, 110));
 		text.setFill(Color.WHITE);
 		text.setStroke(Color.BLACK);
 		text.setTranslateX(70);
 		text.setTranslateY(250);
 		group.getChildren().add(text);
 		
 		final Button playButton = new Button("Jogar novamente");
 		playButton.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
 		playButton.setTranslateX(260);
 		playButton.setTranslateY(300);
 		playButton.setOnMousePressed(new EventHandler<MouseEvent>() { @Override public void handle(MouseEvent e) {
 			changeState(GameState.PLAYING);
 		}});
 		group.getChildren().add(playButton);
 		
 		final BooleanProperty hardModeSelected = showModes(group, playButton, 30);
 		
 		playButton.setOnMousePressed(new EventHandler<MouseEvent>() { @Override public void handle(MouseEvent e) {
 			changeState(GameState.PLAYING);
 			hardMode = hardModeSelected.get();
 		}});
 		
 		ImageView iwatinha = new ImageView(Resources.iwatinha_happy);
 		iwatinha.setTranslateX(10);
 		iwatinha.setTranslateY(250);
 		group.getChildren().add(iwatinha);
 		
 		Credits credits = new Credits();
 		credits.setTranslateX(280);
 		credits.setTranslateY(500);
 		group.getChildren().add(credits);
 		
 		return group;
 	}
 
 	private Group startGame() {
 		Group group = new Group();
 		
 		currentTimestamp = System.currentTimeMillis();
 		countDown = MAX_TIME;
 		velocity = INITIAL_VELOCITY;
 		Arrays.fill(score, 0);
 		bonusCount = 0;
 		
 		final Text clockText = new Text("30");
 		clockText.translateXProperty().bind(scene.widthProperty().subtract(150));
 		clockText.setTranslateY(100);
 
 		InnerShadow is = new InnerShadow();
 		is.setOffsetX(3.0f);
 		is.setOffsetY(3.0f);
 		
 		clockText.setFont(Font.font("Verdana", FontWeight.BOLD, 100));
 		clockText.setFill(Color.WHITE);
 		clockText.setStroke(Color.BLACK);
 //		clockText.setEffect(is);
 		
 		group.getChildren().add(clockText);
 		
 		KeyFrame kf = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent event) {
 			clockText.toFront();
 			clockText.setText(String.valueOf(countDown));
 			countDown--;
 			
 			if (countDown < 0) {
 				clockTimeline.stop();
 				balloonsTimeline.stop();
 				
 				changeState(GameState.LOSE);
 			}
 		}});
 		
 		clockTimeline = new Timeline();
 		clockTimeline.setCycleCount(Timeline.INDEFINITE);
 		clockTimeline.getKeyFrames().add(kf);
 		
 		clockTimeline.play();
 		balloonsTimeline.play();
 		
 		return group;
 	}
 
 	private Group createMenu() {
 		Group menu = new Group();
 		
 		final Button playButton = new Button("Jogar");
 		playButton.translateXProperty().bind(scene.widthProperty().divide(2).subtract(playButton.widthProperty().divide(2)));
 		playButton.translateYProperty().bind(scene.heightProperty().divide(2).subtract(playButton.heightProperty().divide(2)));
 		playButton.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
 		
 		menu.getChildren().add(playButton);
 		
 		final BooleanProperty hardModeSelected = showModes(menu, playButton, 10);
 
 		playButton.setOnMousePressed(new EventHandler<MouseEvent>() { @Override public void handle(MouseEvent e) {
 			changeState(GameState.PLAYING);
 			hardMode = hardModeSelected.get();
 		}});
 		
 		return menu;
 	}
 	
 	private boolean hardMode = false;
 
 	private void createBalloon() {
 		bonusCount++;
 		
 		int colorIndex = random.nextInt(Resources.balloons.length);
 		int logoIndex = random.nextInt(Resources.LOGO_COUNT);
 		
 		if(bonusCount == 10) {
 			logoIndex = BONUS_INDEX;
 			bonusCount = 0;
 		}
 		
 		//Balloon balloon = new Balloon(colorIndex, logoIndex, velocity, state != GameState.WINNING, currentTimestamp);
 		double speed = hardMode ? velocity * 2 : velocity;
 		Balloon balloon = new Balloon(colorIndex, logoIndex, speed, state != GameState.WINNING, currentTimestamp);
 		
 		balloon.setScaleX(BALLOON_SCALE_X);
 		balloon.setScaleY(BALLOON_SCALE_Y);
 		balloon.setTranslateY(root.getScene().getHeight());
 		balloon.setTranslateX(random.nextDouble() * (root.getScene().getWidth() - Resources.balloons[colorIndex].getWidth()));
 		currentScene.getChildren().add(balloon);
 		balloons.add(balloon);
 	}
 
 	private void update() {
 		timeSpan += DELTA_TIME;
 		
 		if(timeSpan > 1.0) {
 			timeSpan = 0;
 			velocity += 2;
 			
 			createBalloon();
 		}
 		
 		Iterator<Balloon> iterator = balloons.iterator();
 		while(iterator.hasNext()) {
 			Balloon balloon = iterator.next();
 			
 			if (!balloon.isAlive())
 				continue;
 			
 			balloon.setTranslateY(balloon.getTranslateY() - balloon.getVelocity()*DELTA_TIME);
 
 			if(balloon.getTranslateY()+balloon.getBoundsInLocal().getHeight() < 0) {
 				currentScene.getChildren().remove(balloon);
 				iterator.remove();
 			}
 		}
 	}
 
 	public void animateBalloon(final int logoIndex, final ImageView logoView, final Transform transform, long gameTimestamp) {
 		if(state != GameState.PLAYING) return;
 		if(currentTimestamp != gameTimestamp) return; //workaround
 		
 		double sx = transform.getMxx();
 		double sy = transform.getMyy();
 		double tx = transform.getTx();
 		double ty = transform.getTy();
 		
 		currentScene.getChildren().add(logoView);
 		logoView.setScaleX(sx);
 		logoView.setScaleY(sy);
 		logoView.setTranslateX(tx);
 		logoView.setTranslateY(ty);
 		
 		Timeline timeline = new Timeline();
 		timeline.setCycleCount(1);
 		
 		double offsetX;
 		double offsetY;
 		
 		if(logoIndex == BONUS_INDEX) {
 			offsetX = scene.getWidth() - 150;
 			offsetY = 0;
 		} else {
 			int x = logoIndex % 6;
 			int y = logoIndex / 6;
 			
 			offsetX = x*LOGO_WIDTH*BALLOON_SCALE_X + x*15 - 50;
 			offsetY = y*LOGO_HEIGHT*BALLOON_SCALE_Y + y*15 - 10;
 		}
 		
 		timeline.setOnFinished(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent event) {
 			if(logoIndex == BONUS_INDEX)
 				currentScene.getChildren().remove(logoView);
 			else if(score[logoIndex] > 0) {
 				if((score[logoIndex] & LOGO_SHOLD_BE_REMOVED) != 0)
 					currentScene.getChildren().remove(logoView);
 				score[logoIndex] |= LOGO_SHOLD_BE_REMOVED;
 			}
 		}});
 		
 		KeyFrame kf0 = new KeyFrame(Duration.millis(1000)
 				, new KeyValue(logoView.translateXProperty(), offsetX)
 				, new KeyValue(logoView.translateYProperty(), offsetY)
 		);
 		
 		timeline.getKeyFrames().addAll(kf0);
 
 		timeline.play();
 	}
 
 	public void incrementScore(int logoIndex, Transform transform, long gameTimestamp) {
 		if(state != GameState.PLAYING) return;
 		
 		double tx = transform.getTx();
 		double ty = transform.getTy();
 		
 		if(logoIndex == BONUS_INDEX) {
 			countDown += 5;
 			
 			Text text = new Text("+5");
 			text.setFont(Font.font("Verdana", FontWeight.BOLD, 100));
 			text.setTranslateX(tx);
 			text.setTranslateY(ty);
 			text.setFill(Color.WHITE);
 			text.setStroke(Color.BLACK);
 			currentScene.getChildren().add(text);
 			
 			Timeline timeline = new Timeline();
 			KeyFrame kf0 = new KeyFrame(Duration.millis(1500), new KeyValue(text.opacityProperty(), 1));
 			KeyFrame kf1 = new KeyFrame(Duration.millis(2000), new KeyValue(text.opacityProperty(), 0));
 			timeline.getKeyFrames().addAll(kf0, kf1);
 			timeline.play();
 		} else {
 			score[logoIndex]++;
 
 			int count = 0;
 			for(int v : score)
 				if(v != 0) count++;
 			
 			if(count == Resources.LOGO_COUNT)
 				changeState(GameState.WINNING);
 		}
 	}
 }
