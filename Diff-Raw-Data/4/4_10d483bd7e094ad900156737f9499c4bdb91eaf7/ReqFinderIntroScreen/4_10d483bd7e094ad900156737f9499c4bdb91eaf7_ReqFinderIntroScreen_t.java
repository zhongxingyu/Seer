 package no.hist.gruppe5.pvu.reqfinder;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.Input;
 import no.hist.gruppe5.pvu.PVU;
 
 /**
  *
  * @author Martin
  */
 public class ReqFinderIntroScreen extends GameScreen {
     private Label mLabel;
     private String mText;
     private LabelStyle mLabelStyle;
     private Stage mStage;
     private BitmapFont mFont;
    private Input mInput = new Input(200, 1500);
     public ReqFinderIntroScreen(PVU pvu) {
         super(pvu);
         try {
             mText =  Assets.readFile("data/reqFinder/intro.txt");
             mText += "\n\nTrykk space for å begynne, lykke til!";
         } catch (FileNotFoundException ex) {
             Logger.getLogger(ReqFinderIntroScreen.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(ReqFinderIntroScreen.class.getName()).log(Level.SEVERE, null, ex);
         }
         mFont = new BitmapFont(
                 Gdx.files.internal("data/LucidaBitmap10px.fnt"),
                 Gdx.files.internal("data/LucidaBitmap10px_0.png"), false);
         mStage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true);
         mLabelStyle = new LabelStyle(mFont, Color.BLACK);
         mLabelStyle.font.scale(2f);
         mLabel = new Label(mText, mLabelStyle);
         
         mLabel.setAlignment(Align.center);
         mLabel.setWrap(true);
         mLabel.setFillParent(true);
         
         mStage.addActor(mLabel);
     }
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
         mStage.draw();
     }
 
     @Override
     protected void update(float delta) {
        if(mInput.action()) {
             game.setScreen(new ReqFinderScreen(game));
         }
     }
 
     @Override
     protected void cleanUp() {
     }
     
 }
