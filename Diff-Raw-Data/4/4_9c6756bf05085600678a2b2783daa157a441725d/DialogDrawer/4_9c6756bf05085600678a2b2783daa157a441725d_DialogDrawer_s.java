 package no.hist.gruppe5.pvu.dialogdrawer;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.PVU;
 
 public class DialogDrawer {
 
     private String code = "Hei, og velkommen til PVU! ";
     private String code2 = "Her kan du lære om programutvikling ved hjelp av SCRUM";
     private String code3 = "Hvis du er nybegynner foreslår vi at du leser introduksjonen i Instruksjonsboka!";
     private String code4 = "Lykke til på ferden til å bli en programutvikler!";
     private TextField textfield;
     private SpriteBatch batch;
     private Label codeOutput;
     private Skin textboxskin;
     private TextFieldStyle textfieldstyle;
     private LabelStyle labelStyle;
     private Texture tex;
     private boolean buttonPressedENTER;
     private int counter = 0;
 
     public DialogDrawer(SpriteBatch batch) {
         this.batch = batch;
         setVariables();
     }
 
     public void draw(float delta) {
         textfield.draw(batch, 1f);
         codeOutput.draw(batch, 1f);
     }
 
    public void setVariables() {
         labelStyle = new LabelStyle(Assets.primaryFont10px, Color.BLACK);
         tex = new Texture(Gdx.files.internal("data/DialogTexture.png"));
         textfieldstyle = new TextFieldStyle();
         textboxskin = new Skin();
         textboxskin.add("textfieldback", new TextureRegion(tex, 1, 1, 190, 56));
        labelStyle.font.setScale(0.3f);
         textfieldstyle.font = Assets.primaryFont10px;
         textfieldstyle.background = textboxskin.getDrawable("textfieldback");
         textfield = new TextField("", textfieldstyle);
         textfield.setHeight(PVU.GAME_HEIGHT / 3);
         textfield.setWidth(PVU.GAME_WIDTH);
         codeOutput = new Label(code, labelStyle);
         codeOutput.setPosition(6, -37);
         codeOutput.setWrap(true);
         codeOutput.setHeight(PVU.GAME_HEIGHT);
         codeOutput.setWidth(PVU.GAME_WIDTH - 10);
     }
 
     public String getCode() {
         return code;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public void intro() {
         codeOutput.setPosition(6, -37);
         codeOutput.setWrap(true);
         codeOutput.setHeight(PVU.GAME_HEIGHT);
         codeOutput.setWidth((PVU.GAME_WIDTH/0.6f) - 10);
         codeOutput.setFontScale(0.6f);
         if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && !buttonPressedENTER) {
             buttonPressedENTER = true;
             counter++;
             if (counter == 1) {
                 codeOutput.setText(code2);
             }
             if (counter == 2) {
                 codeOutput.setText(code3);
             }
             if (counter == 3) {
                 codeOutput.setText(code4);
             }
             if (counter == 4) {
                 codeOutput.setText("");
                 textfield.setSize(0, 0);
             }
         }
     }
 
     public void introNext() {
         if (!Gdx.input.isKeyPressed(Input.Keys.ENTER) && buttonPressedENTER) {
             buttonPressedENTER = false;
         }
     }
 }
