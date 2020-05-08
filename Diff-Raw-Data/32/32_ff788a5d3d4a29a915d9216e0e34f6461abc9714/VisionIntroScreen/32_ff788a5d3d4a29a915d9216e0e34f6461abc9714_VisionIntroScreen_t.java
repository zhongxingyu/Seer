 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hist.gruppe5.pvu.visionshooter;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.Input;
 import no.hist.gruppe5.pvu.PVU;
 
 /**
  *
  * @author Frode
  */
 public class VisionIntroScreen extends GameScreen {
 
     private String mTitleText = "VisionShooter";
     private String mIntroText;
     private String mYoutubeText;
     private String mFacebookText;
     private String mVisionDocText;
     private Label mLtitle;
     private Label mLintro;
     private Label mLyoutube;
     private Label mLfacebook;
     private Label mLvision;
     private Label mLcontinue;
     private Stage mStage;
     private Label.LabelStyle mLabelstyle;
     private Skin mSkin;
     
     
     private Sprite mSyoutube;
     private Sprite mSfacebook;
     private Sprite mSdoc;
     private Input mInput;
 
     public VisionIntroScreen(PVU game) throws FileNotFoundException, IOException {
         super(game);
 
         mInput = new Input();
 
        mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true);
         mIntroText = Assets.readFile("data/visionScreen_intro/Intro.txt");
         mYoutubeText = Assets.readFile("data/visionScreen_intro/Youtube.txt");
         mFacebookText = Assets.readFile("data/visionScreen_intro/Facebook.txt");
         mVisionDocText = Assets.readFile("data/visionScreen_intro/Vision.txt");
 
         mLtitle = makeHeadLabel(mTitleText);
         mLtitle.setPosition(PVU.SCREEN_WIDTH / 4, PVU.SCREEN_HEIGHT * 0.9f);
         mLtitle.setFontScale(6f);
         mLtitle.setWrap(true);
         mStage.addActor(mLtitle);
         
         mLintro = makeLabel(mIntroText);
         mLintro.setPosition(PVU.SCREEN_WIDTH / 10, PVU.SCREEN_HEIGHT * 0.65f);
         mLintro.setWrap(true);
         mLintro.setWidth(PVU.GAME_WIDTH * 1.5f );
         mLintro.setFontScale(3f);
         mStage.addActor(mLintro);
         
         mLyoutube = makeLabel(mYoutubeText);
         mLyoutube.setPosition(PVU.SCREEN_WIDTH / 6, PVU.SCREEN_HEIGHT / 2.2f);
         mLyoutube.setWrap(true);
         mLyoutube.setWidth(PVU.GAME_WIDTH *2);
         mLyoutube.setFontScale(2f);
         mStage.addActor(mLyoutube);
         
         mLfacebook = makeLabel(mFacebookText);
         mLfacebook.setPosition(PVU.SCREEN_WIDTH / 6, PVU.SCREEN_HEIGHT / 2.8f);
         mLfacebook.setWrap(true);
         mLfacebook.setWidth(PVU.GAME_WIDTH*2);
         mLfacebook.setFontScale(2f);
         mStage.addActor(mLfacebook);
         
         mLvision = makeLabel(mVisionDocText);
         mLvision.setPosition(PVU.SCREEN_WIDTH / 6, PVU.SCREEN_HEIGHT / 4f);
         mLvision.setWrap(true);
         mLvision.setWidth(PVU.GAME_WIDTH*2);
         mLvision.setFontScale(2f);
         mStage.addActor(mLvision);
         
         mLcontinue = makeHeadLabel("Trykk SPACE for aa fortsette");
         mLcontinue.setPosition(PVU.SCREEN_WIDTH/4, PVU.SCREEN_HEIGHT / 7f);
         mLcontinue.setWrap(true);
         mLcontinue.setFontScale(3f);
         mStage.addActor(mLcontinue);
 
        mSyoutube = new Sprite(Assets.visionShooterYoutubeRegion);
        mSyoutube.setPosition(PVU.GAME_WIDTH / 9, PVU.GAME_HEIGHT / 2.3f);

        mSfacebook = new Sprite(Assets.visionShooterFacebookRegion);
        mSfacebook.setPosition(PVU.GAME_WIDTH / 9, PVU.GAME_HEIGHT / 3);
 
        mSdoc = new Sprite(Assets.visionShooterDocumentRegion);
        mSdoc.setPosition(PVU.GAME_WIDTH / 9.5f, PVU.GAME_HEIGHT / 5);
        mSdoc.scale(-0.3f);
 
     }
 
     
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
 
         batch.begin();
         
        batch.draw(Assets.visionShooterRegion, 0, 0);
         mSyoutube.draw(batch);
         mSfacebook.draw(batch);
         mSdoc.draw(batch);

         batch.end();

         mStage.draw();
     }
 
     @Override
     protected void update(float delta) {
         if (mInput.action()) {
             game.setScreen(new VisionScreen(game));
         }
     }
 
     @Override
     protected void cleanUp() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     private Label makeHeadLabel(String text) {
         mSkin = new Skin();
         mLabelstyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
         mLabelstyle.font = Assets.primaryFont10px;
         mLabelstyle.fontColor = Color.RED;
         Label l = new Label(text, mLabelstyle);
         return l;
     }
 
     private Label makeLabel(String text) {
         mSkin = new Skin();
         mLabelstyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
         mLabelstyle.font = Assets.primaryFont10px;
         Label l = new Label(text, mLabelstyle);
         return l;
     }
 }
