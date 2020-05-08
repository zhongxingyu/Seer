 package no.hist.gruppe5.pvu;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.utils.Array;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * Created with IntelliJ IDEA. User: karl Date: 8/28/13 Time: 10:06 AM
  */
 public class Assets {
 
     public static boolean IS_LOADED = false;
     public static TextureRegion testRegion;
     public static TextureRegion visionShooterRegion;
     public static TextureRegion visionShooterDocumentRegion;
     public static TextureRegion visionShooterShipRegion;
     public static TextureRegion visionShooterBullet;
     public static TextureRegion visionShooterFacebookRegion;
     public static TextureRegion visionShooterYoutubeRegion;
     public static TextureRegion[] mainAvatar;
     public static TextureRegion[] umlBlocks;
     public static final int UML_BLOCK_1 = 0;
     public static final int UML_BLOCK_2 = 1;
     public static final int UML_BLOCK_3 = 2;
     public static final int UML_BLOCK_4 = 3;
     public static final int SECONDARY_AVATAR_LEFT_1 = 0;
     public static final int SECONDARY_AVATAR_LEFT_1_FRAME_2 = 1;
     public static final int SECONDARY_AVATAR_LEFT_2 = 2;
     public static final int SECONDARY_AVATAR_LEFT_2_FRAME_2 = 3;
     public static final int SECONDARY_AVATAR_RIGHT_1 = 4;
     public static final int SECONDARY_AVATAR_RIGHT_1_FRAME_2 = 5;
     public static final int SECONDARY_AVATAR_RIGHT_2 = 6;
     public static final int SECONDARY_AVATAR_RIGHT_2_FRAME_2 = 7;
     public static final int SECONDARY_AVATAR_RIGHT_3 = 8;
     public static final int SECONDARY_AVATAR_RIGHT_3_FRAME_2 = 9;
     public static final int MAIN_AVATAR_SITTING = 10;
     public static final int MAIN_AVATAR_FRONT = 11;
     public static final int MAIN_AVATAR_FRONT_FRAME_2 = 12;
     public static final int MAIN_AVATAR_FRONT_FRAME_3 = 13;
     public static final int MAIN_AVATAR_SIDE_RIGHT = 14;
     public static final int MAIN_AVATAR_STEP_RIGHT = 15;
     public static final int MAIN_AVATAR_STEP_RIGHT_FRAME_2 = 16;
     public static final int MAIN_AVATAR_SIDE_LEFT = 17;
     public static final int MAIN_AVATAR_STEP_LEFT = 18;
     public static final int MAIN_AVATAR_STEP_LEFT_FRAME_2 = 19;
     public static final int MAIN_AVATAR_BACK = 20;
     public static final int MAIN_AVATAR_BACK_FRAME_2 = 21;
     public static final int MAIN_AVATAR_BACK_FRAME_3 = 22;
     public static BitmapFont minecraftFont10px;
     public static BitmapFont primaryFont10px;
     // MAIN SCREEN
     public static TextureRegion msBackground;
     public static TextureRegion msTable;
     public static TextureRegion msPcBackground;
     public static TextureRegion[] msBurndownCarts;
     public static TextureRegion exclamationMark;
     // INTRO SCREEN
     public static TextureRegion introMainLogo;
     public static TextureRegion introTeamLogo;
     public static TextureRegion introPressSpace;
     // UML BLOCKS
     public static TextureRegion ubBackground;
     public static TextureRegion ubEasy;
     public static TextureRegion ubMedium;
     public static TextureRegion ubHard;
     // BOOK SCREEN
     public static TextureRegion bookBook;
     // SeqJumper
     public static TextureRegion borderBorder;
     public static TextureRegion seqHead;
     public static TextureRegion seqBackground;
     public static TextureRegion seqBall;
     public static TextureRegion seqLine;
     public static TextureRegion seqBox;
     public static TextureRegion redBar;
     // QUIZ
     public static TextureRegion quizBg;
     // End screen
     public static TextureRegion endScreenBackground;
 
 
     public static void load() {
         minecraftFont10px = new BitmapFont(
                 Gdx.files.internal("data/MinecraftiaBitmap10px.fnt"),
                 Gdx.files.internal("data/MinecraftiaBitmap10px_0.png"), false);
         primaryFont10px = new BitmapFont(
                 Gdx.files.internal("data/LucidaBitmap10px.fnt"),
                 Gdx.files.internal("data/LucidaBitmap10px_0.png"), false);
         primaryFont10px.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
         minecraftFont10px.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
 
         TextureAtlas visionShooterAtlas = new TextureAtlas(Gdx.files.internal("data/VisionshooterPack/VisionShooter.pack"));
         TextureAtlas mainAvatarAtlas = new TextureAtlas(Gdx.files.internal("data/Avatar.pack"));
         TextureAtlas borderAtlas = new TextureAtlas(Gdx.files.internal("data/border/BorderPack.pack"));
         TextureAtlas umlBlocksAtlas = new TextureAtlas(Gdx.files.internal("data/UMLBlocks/UMLBlocks.pack"));
         TextureAtlas endScreenAtlas = new TextureAtlas(Gdx.files.internal("data/EndScreen/EndScreen.pack"));
 
         visionShooterRegion = visionShooterAtlas.findRegion("VisionShooterTexture");
         visionShooterDocumentRegion = visionShooterAtlas.findRegion("Document");
         visionShooterShipRegion = visionShooterAtlas.findRegion("VisionShooterShip");
         visionShooterBullet = visionShooterAtlas.findRegion("Bullet");
         visionShooterFacebookRegion = visionShooterAtlas.findRegion("Facebook");
         visionShooterYoutubeRegion = visionShooterAtlas.findRegion("YouTube");
         
         Array mainAvatarArray = mainAvatarAtlas.findRegions("Avatar");
         mainAvatar = new TextureRegion[mainAvatarArray.size];
         for (int i = 0; i < mainAvatarArray.size; i++) {
             mainAvatar[i] = (TextureRegion) mainAvatarArray.get(i);
         }
         
         Array umlBlocksArray = umlBlocksAtlas.findRegions("uml");
         umlBlocks = new TextureRegion[umlBlocksArray.size];
         for (int i = 0; i < umlBlocks.length; i++) {
             umlBlocks[i] = (TextureRegion) umlBlocksArray.get(i);
         }
 
         // MAIN SCREEN
         TextureAtlas msAtlas = new TextureAtlas(Gdx.files.internal("data/main_room.pack"));
 
         msBackground = msAtlas.findRegion("main_room");
         msPcBackground = msAtlas.findRegion("pcscreen");
         msTable = msAtlas.findRegion("table");
         Array carts = msAtlas.findRegions("chart");
         msBurndownCarts = new TextureRegion[carts.size];
         for (int i = 0; i < carts.size; i++) {
             msBurndownCarts[i] = (TextureRegion) carts.get(i);
         }
 
         exclamationMark = mainAvatar[24];
 
         // INTRO
         introMainLogo = msAtlas.findRegion("logo");
         introTeamLogo = msAtlas.findRegion("logo_2_group");
         introPressSpace = msAtlas.findRegion("trykk");
 
         // UML BLOCKS
         ubBackground = msAtlas.findRegion("umlbackground");
         ubEasy = msAtlas.findRegion("ub_easy");
         ubMedium = msAtlas.findRegion("ub_medium");
         ubHard = msAtlas.findRegion("ub_hard");
 
         // BOOK
         bookBook = msAtlas.findRegion("book");
 
         // SeqJumper
         seqBackground = msAtlas.findRegion("sequencebg");
         TextureAtlas seqAtlas = new TextureAtlas(Gdx.files.internal("data/seqjumper.atlas"));
         seqHead = seqAtlas.findRegion("head");
         seqLine = seqAtlas.findRegion("line");
         seqBall = seqAtlas.findRegion("ball");
         seqBox = seqAtlas.findRegion("box");
         redBar = seqAtlas.findRegion("redbar");
 
         borderBorder = borderAtlas.findRegion("BorderChooser");
 
         // QUIZ
         quizBg = msAtlas.findRegion("quizbg");
 
         // END SCREEN
         endScreenBackground = endScreenAtlas.findRegion("End_screen");
 
         // Simple "loading" hack, always put last
         IS_LOADED = true;
     }
 
     public static TextureRegion getAvatarRegion(int region) {
         if (region > mainAvatar.length || region < 0) {
             return null;
         }
         return mainAvatar[region];
     }
 
     public static void dispose() {
     }
 
    public static String readFile(String fileName) throws IOException {
         String text = "";
         DataInputStream in = new DataInputStream(new FileInputStream(fileName));
         BufferedReader inBR = new BufferedReader(new InputStreamReader(in));
         String strLine;
         while ((strLine = inBR.readLine()) != null) {
             if (!"".equals(strLine)) {
                 text += strLine;
             }
         }
        return new String(text.getBytes("UTF-8"));
     }
 }
