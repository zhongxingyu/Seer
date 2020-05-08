 package zamours;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Circle;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.utils.Timer;
 import com.badlogic.gdx.utils.Timer.Task;
 
 public class Quiz1 implements Screen {
 
 	boolean maintenu;
 	private SpriteBatch batch;
 	private Texture background, backgroundBoy, backgroundGirl, textureRectangle1,
 			textureRectangle1bis, textureRectangle2, textureRectangle2bis,
 			textureRectangle3, textureRectangle3bis, retourMenu, chocolat,
 			resteChocolat;
 
 	private Sprite spritebackgroundBoy, spritebackgroundGirl, spriteChocolat1,
 			spriteChocolat2, spriteChocolat3, spriteChocolat4, spriteChocolat5,
 			spriteChocolat6, spriteChocolat7, spriteChocolat8, spriteChocolat9,
 			spriteChocolat10, spriteResteChocolat1, spriteResteChocolat2,
 			spriteResteChocolat3, spriteResteChocolat4, spriteResteChocolat5,
 			spriteResteChocolat6, spriteResteChocolat7, spriteResteChocolat8,
 			spriteResteChocolat9, spriteResteChocolat10;
 
 	private BitmapFont fontmessage1;
 	private Rectangle rectangleQuest1, rectangleQuest2, rectangleQuest3;
 	private Circle circleBackButton;
 	private Question quest1;
 	private Reponse rep1;
 	int screenWidth, screenHeight, spaceBetweenAnswers,
 			spaceBetweenQuestAnswers, positionQuestion1;
 	private float xDoigt, yDoigt,delay = 0.5f; // secondes a attendre avant apparition resultatquiz;
 	int[] tabReponseBoy, tabReponseGirl;
 	boolean[] reponse;
 	private String pageSex; // need pour savoir quel tableau remplir dans
 							// touchUp
 	int compteurNombreReponsesBoy = 0;
 	int compteurNombreReponsesGirl = 0;
 	int appuiRep; // va me donner le numero de reponse sur lequel l'utilisateur
 					// a clicked (look touchDown), va me servir pour touchUp
 
 	private Sound soundTouchDown;
 
 	private Jeu game;
 
 	Quiz1(Jeu game) {
 		this.game = game;
 	}
 
 	@Override
 	public void show() {
 		soundTouchDown = Gdx.audio.newSound(Gdx.files
 				.internal("sound/sound_click_down.wav"));
 		Texture.setEnforcePotImages(false);
 
 		screenWidth = Gdx.graphics.getWidth();
 		screenHeight = Gdx.graphics.getHeight();
 
 		positionQuestion1 = screenHeight / 2 + screenHeight / 7;
 
 		batch = new SpriteBatch();
 		/**
 		 * xhdpi: 640x960 px hdpi: 480x800 px mdpi: 320x480 px ldpi: 240x320 px
 		 */
 		background = new Texture(Gdx.files.internal("background_options_s2.png"));
 		backgroundBoy = new Texture(
 				Gdx.files.internal("background_blue_s2.png"));
 		backgroundGirl = new Texture(
 				Gdx.files.internal("background_pink_s2.png"));
 		spritebackgroundBoy = new Sprite(backgroundBoy);
 		spritebackgroundGirl = new Sprite(backgroundGirl);
 		spritebackgroundBoy.setSize(screenWidth, screenHeight);
 		spritebackgroundGirl.setSize(screenWidth, screenHeight);
 
 		fontmessage1 = new BitmapFont(Gdx.files.internal("font/white.fnt"),
 				false);
 
 		spaceBetweenAnswers = screenHeight / 6;
 		spaceBetweenQuestAnswers = screenHeight / 4;
 
 		quest1 = new Question(1);
 		rep1 = new Reponse(1);
 
 		/********************************** Placement des 3 rectangles ****************************************************************/
 
 		textureRectangle1 = new Texture(Gdx.files.internal("reponse.png"));
 		textureRectangle1bis = new Texture(
 				Gdx.files.internal("reponseClick.png"));
 		textureRectangle2 = new Texture(Gdx.files.internal("reponse.png"));
 		textureRectangle2bis = new Texture(
 				Gdx.files.internal("reponseClick.png"));
 		textureRectangle3 = new Texture(Gdx.files.internal("reponse.png"));
 		textureRectangle3bis = new Texture(
 				Gdx.files.internal("reponseClick.png"));
 		/*********************************************************************************************************************************/
 
 		tabReponseBoy = new int[10];
 		tabReponseGirl = new int[10];
 
 		Jeu.numeroQuestionQuiz = 1;
 		retourMenu = new Texture(Gdx.files.internal("backbutton1.png"));
 
 		/***************************************** Affichage coeur ou reste en fonction de la rep **************************************/
 		reponse = new boolean[10];
 		for(int i=0; i < reponse.length; i++){
 			reponse[i] = false;
 		}
 		
 		chocolat = new Texture(Gdx.files.internal("bonne_rep.png"));
 		resteChocolat = new Texture(Gdx.files.internal("miete.png"));
 		spriteChocolat1 = new Sprite(chocolat);
 		spriteChocolat2 = new Sprite(chocolat);
 		spriteChocolat3 = new Sprite(chocolat);
 		spriteChocolat4 = new Sprite(chocolat);
 		spriteChocolat5 = new Sprite(chocolat);
 		spriteChocolat6 = new Sprite(chocolat);
 		spriteChocolat7 = new Sprite(chocolat);
 		spriteChocolat8 = new Sprite(chocolat);
 		spriteChocolat9 = new Sprite(chocolat);
 		spriteChocolat10 = new Sprite(chocolat);
 		spriteResteChocolat1 = new Sprite(resteChocolat);
 		spriteResteChocolat2 = new Sprite(resteChocolat);
 		spriteResteChocolat3 = new Sprite(resteChocolat);
 		spriteResteChocolat4 = new Sprite(resteChocolat);
 		spriteResteChocolat5 = new Sprite(resteChocolat);
 		spriteResteChocolat6 = new Sprite(resteChocolat);
 		spriteResteChocolat7 = new Sprite(resteChocolat);
 		spriteResteChocolat8 = new Sprite(resteChocolat);
 		spriteResteChocolat9 = new Sprite(resteChocolat);
 		spriteResteChocolat10 = new Sprite(resteChocolat);
 		spriteChocolat1.setSize(32, 32);
 		spriteChocolat1.setPosition(30, 100);
 		spriteChocolat2.setSize(32, 32);
 		spriteChocolat2.setPosition(70, 100);
 		spriteChocolat3.setSize(32, 32);
 		spriteChocolat3.setPosition(110, 100);
 		spriteChocolat4.setSize(32, 32);
 		spriteChocolat4.setPosition(150, 100);
 		spriteChocolat5.setSize(32, 32);
 		spriteChocolat5.setPosition(190, 100);
 		spriteChocolat6.setSize(32, 32);
 		spriteChocolat6.setPosition(230, 100);
 		spriteChocolat7.setSize(32, 32);
 		spriteChocolat7.setPosition(270, 100);
 		spriteChocolat8.setSize(32, 32);
 		spriteChocolat8.setPosition(310, 100);
 		spriteChocolat9.setSize(32, 32);
 		spriteChocolat9.setPosition(350, 100);
 		spriteChocolat10.setSize(32, 32);
 		spriteChocolat10.setPosition(390, 100);
 		spriteResteChocolat1.setSize(16, 16);
 		spriteResteChocolat1.setPosition(40, 110);
 		spriteResteChocolat2.setSize(16, 16);
 		spriteResteChocolat2.setPosition(80, 110);
 		spriteResteChocolat3.setSize(16, 16);
 		spriteResteChocolat3.setPosition(120, 110);
 		spriteResteChocolat4.setSize(16, 16);
 		spriteResteChocolat4.setPosition(160, 110);
 		spriteResteChocolat5.setSize(16, 16);
 		spriteResteChocolat5.setPosition(200, 110);
 		spriteResteChocolat6.setSize(16, 16);
 		spriteResteChocolat6.setPosition(240, 110);
 		spriteResteChocolat7.setSize(16, 16);
 		spriteResteChocolat7.setPosition(280, 110);
 		spriteResteChocolat8.setSize(16, 16);
 		spriteResteChocolat8.setPosition(320, 110);
 		spriteResteChocolat9.setSize(16, 16);
 		spriteResteChocolat9.setPosition(360, 110);
 		spriteResteChocolat10.setSize(16, 16);
 		spriteResteChocolat10.setPosition(400, 110);
 
 	}
 
 	@Override
 	public void dispose() {
 
 	}
 
 	@Override
 	public void hide() {
 		dispose();
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void render(float arg0) {
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		gestionDesEntrees();
 		batch.begin();
 		questionTouched();
 		afficheCoeur();
 		batch.end();
 
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	public void afficheQuestionReponses() {
 		switch (Jeu.numeroQuestionQuiz) {
 		case 1:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(0),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(0, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(0, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(0, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 
 		case 2:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(1),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(1, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(1, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(1, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 3:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(2),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(2, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(2, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(2, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 4:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(3),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(3, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(3, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(3, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 5:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(4),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(4, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(4, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(4, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 6:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(5),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(5, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(5, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(5, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 7:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(6),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(6, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(6, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(6, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 8:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(7),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(7, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(7, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(7, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 9:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(8),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(8, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(8, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(8, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 10:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(9),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(9, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(9, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(9, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 11:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(10),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(10, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(10, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(10, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 12:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(11),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(11, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(11, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(11, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 13:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(12),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(12, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(12, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(12, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 14:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(13),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(13, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(13, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(13, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 15:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(14),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(14, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(14, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(14, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 16:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(15),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(15, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(15, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(15, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 17:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(16),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(16, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(16, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(16, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 18:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(17),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(17, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(17, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(17, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 19:
 			pageSex = "girl";
 			spritebackgroundGirl.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(18),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(18, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(18, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(18, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 20:
 			pageSex = "boy";
 			spritebackgroundBoy.draw(batch);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(19),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(19, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(19, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(19, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 
 		}
 
 	}
 
 	/***************************************/
 	/** renvoi le nombre de reponse juste **/
 	/***************************************/
 	public int compareReponses() { // attention on renvoie un int
 		int nbRepJuste = 0;
 		for (int i = 0; i < tabReponseBoy.length; i++) {
 			if (tabReponseBoy[i] == tabReponseGirl[i])
 				nbRepJuste++;
 		}
 		Jeu.nbRepJuste = nbRepJuste;
 		return nbRepJuste;
 	}
 
 	/*******************************************/
 	/** renvoi true si 2 meme rep sinon false **/
 	/*******************************************/
 	public boolean memeRep() {
 		if (tabReponseBoy[compteurNombreReponsesBoy - 1] == tabReponseGirl[compteurNombreReponsesGirl - 1]) {
 			reponse[compteurNombreReponsesBoy - 1] = true;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public void afficheCoeur(){
 		if(reponse[0]){
 			spriteChocolat1.draw(batch);
 		}
 		else  spriteResteChocolat1.draw(batch);
 		if(reponse[1]){
 			spriteChocolat2.draw(batch);
 		}
 		else  spriteResteChocolat2.draw(batch);
 		if(reponse[2]){
 			spriteChocolat3.draw(batch);
 		}
 		else  spriteResteChocolat3.draw(batch);
 		if(reponse[3]){
 			spriteChocolat4.draw(batch);
 		}
 		else  spriteResteChocolat4.draw(batch);
 		if(reponse[4]){
 			spriteChocolat5.draw(batch);
 		}
 		else  spriteResteChocolat5.draw(batch);
 		if(reponse[5]){
 			spriteChocolat6.draw(batch);
 		}
 		else  spriteResteChocolat6.draw(batch);
 		if(reponse[6]){
 			spriteChocolat7.draw(batch);
 		}
 		else  spriteResteChocolat7.draw(batch);
 		if(reponse[7]){
 			spriteChocolat8.draw(batch);
 		}
 		else  spriteResteChocolat8.draw(batch);
 		if(reponse[8]){
 			spriteChocolat9.draw(batch);
 		}
 		else  spriteResteChocolat9.draw(batch);
 		if(reponse[9]){
 			spriteChocolat10.draw(batch);
 		}
 		else  spriteResteChocolat10.draw(batch);
 	}
 
 	public void clickDown(int x, int y) {
 		if (rectangleQuest1.contains(x, y) || rectangleQuest2.contains(x, y)
				|| rectangleQuest3.contains(x, y)) {
 			if (Jeu.getDesactiveSoundTouchDown() == false)
 				soundTouchDown.play();
 		}
 		if (rectangleQuest1.contains(x, y)) {
 			appuiRep = 1;
 		} else if (rectangleQuest2.contains(x, y)) {
 			appuiRep = 2;
 		} else if (rectangleQuest3.contains(x, y)) {
 			appuiRep = 3;
 		}
 		if (circleBackButton.contains(x, y)) {
 			game.setScreen(new MainMenu(game));
 		}
 	}
 
 	public void testRep(int x, int y) {
 		/***************/
 		/***** boy *****/
 		/***************/
 		tabReponseBoy[compteurNombreReponsesBoy] = 1;
 		if (rectangleQuest1.contains(x, y) && maintenu && pageSex.equals("boy")
 				&& appuiRep == 1) {
 			compteurNombreReponsesBoy++;
 			if (compteurNombreReponsesBoy == compteurNombreReponsesGirl
 					&& Jeu.numeroQuestionQuiz != 1) {
 				memeRep();
 			}
 			Jeu.numeroQuestionQuiz++;
 		} else if (rectangleQuest2.contains(x, y) && maintenu
 				&& pageSex.equals("boy") && appuiRep == 2) {
 			tabReponseBoy[compteurNombreReponsesBoy] = 2;
 			compteurNombreReponsesBoy++;
 			if (compteurNombreReponsesBoy == compteurNombreReponsesGirl
 					&& Jeu.numeroQuestionQuiz != 1) {
 				memeRep();
 			}
 			Jeu.numeroQuestionQuiz++;
 		} else if (rectangleQuest3.contains(x, y) && maintenu
 				&& pageSex.equals("boy") && appuiRep == 3) {
 			tabReponseBoy[compteurNombreReponsesBoy] = 3;
 			compteurNombreReponsesBoy++;
 			if (compteurNombreReponsesBoy == compteurNombreReponsesGirl
 					&& Jeu.numeroQuestionQuiz != 1) {
 				memeRep();
 			}
 			Jeu.numeroQuestionQuiz++;
 		}
 		/***************/
 		/**** girl *****/
 		/***************/
 		if (rectangleQuest1.contains(x, y) && maintenu
 				&& pageSex.equals("girl") && appuiRep == 1) {
 			tabReponseGirl[compteurNombreReponsesGirl] = 1;
 			compteurNombreReponsesGirl++;
 			if (compteurNombreReponsesBoy == compteurNombreReponsesGirl
 					&& Jeu.numeroQuestionQuiz != 1) {
 				memeRep();
 			}
 			Jeu.numeroQuestionQuiz++;
 		} else if (rectangleQuest2.contains(x, y) && maintenu
 				&& pageSex.equals("girl") && appuiRep == 2) {
 			tabReponseGirl[compteurNombreReponsesGirl] = 2;
 			compteurNombreReponsesGirl++;
 			if (compteurNombreReponsesBoy == compteurNombreReponsesGirl
 					&& Jeu.numeroQuestionQuiz != 1) {
 				memeRep();
 			}
 			Jeu.numeroQuestionQuiz++;
 		} else if (rectangleQuest3.contains(x, y) && maintenu
 				&& pageSex.equals("girl") && appuiRep == 3) {
 			tabReponseGirl[compteurNombreReponsesGirl] = 3;
 			compteurNombreReponsesGirl++;
 			if (compteurNombreReponsesBoy == compteurNombreReponsesGirl
 					&& Jeu.numeroQuestionQuiz != 1) {
 				memeRep();
 			}
 			Jeu.numeroQuestionQuiz++;
 		}
 		if (Jeu.quizTermine()) {
 			compareReponses();
 
 			
 			Timer.schedule(new Task(){
 			    @Override
 			    public void run() {
 			    	game.setScreen(new ResultatDuQuiz(game)); // changement de
 			    }
 			}, delay);
 			
 			// page,
 			// redirection
 			// vers la
 			// screen
 			// ResultatDuQuiz
 		}
 	}
 
 	public void questionTouched() {
 
 		/** bouton retour **/
 		circleBackButton = new Circle(screenWidth / 15 + 34, 47, 26);
 
 		rectangleQuest1 = new Rectangle((screenWidth / 10) - 10, screenHeight
 				- positionQuestion1 - 17, screenWidth - 2
 				* ((screenWidth / 10) - 10), 85);
 		rectangleQuest2 = new Rectangle((screenWidth / 10) - 10, screenHeight
 				- positionQuestion1 + spaceBetweenAnswers - 17, screenWidth - 2
 				* ((screenWidth / 10) - 10), 85);
 		rectangleQuest3 = new Rectangle((screenWidth / 10) - 10, screenHeight
 				- positionQuestion1 + 2 * spaceBetweenAnswers - 17, screenWidth
 				- 2 * ((screenWidth / 10) - 10), 85);
 
 		afficheQuestionReponses();
 
 		batch.draw(retourMenu, screenWidth / 15, screenHeight - screenHeight
 				/ 10);
 
 		if (rectangleQuest1.contains(xDoigt, yDoigt) && maintenu) {
 
 			batch.draw(textureRectangle1bis, -8, 432);
 
 		} else {
 			batch.draw(textureRectangle1, -5, 432);
 		}
 		if (rectangleQuest2.contains(xDoigt, yDoigt) && maintenu) {
 			batch.draw(textureRectangle2bis, -8, 297);
 		} else {
 			batch.draw(textureRectangle2, -5, 297);
 
 		}
 		if (rectangleQuest3.contains(xDoigt, yDoigt) && maintenu) {
 			batch.draw(textureRectangle3bis, -8, 165);
 		} else {
 			batch.draw(textureRectangle3, -5, 165);
 		}
 	}
 
 	// ********************************** Gestion des
 	// entrees************************************************/
 
 	public void gestionDesEntrees() {
 		Gdx.input.setInputProcessor(new InputProcessor() {
 
 			public boolean mouseMoved(int x, int y) {
 
 				return false;
 			}
 
 			@Override
 			public boolean touchUp(int x, int y, int arg2, int arg3) {
 				if (Jeu.numeroQuestionQuiz <= 20){
 					testRep(x, y);
 				}
 				xDoigt = 0;
 				yDoigt = 0;
 				maintenu = false;
 				return false;
 			}
 
 			@Override
 			public boolean touchDragged(int x, int y, int arg2) {
 				return false;
 			}
 
 			@Override
 			public boolean touchDown(int x, int y, int arg2, int arg3) {
 				if (Jeu.numeroQuestionQuiz <= 20){
 					clickDown(x, y);				
 				}
 				xDoigt = x;
 				yDoigt = y;
 				maintenu = true;
 				return false;
 			}
 
 			@Override
 			public boolean scrolled(int arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean keyUp(int arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean keyTyped(char arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean keyDown(int arg0) {
 				return false;
 			}
 		});
 	}
 }
