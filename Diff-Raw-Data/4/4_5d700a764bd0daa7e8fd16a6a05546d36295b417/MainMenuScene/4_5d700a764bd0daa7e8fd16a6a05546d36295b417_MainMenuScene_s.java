 package com.secondhand.scene;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
 
 import android.content.Context;
 
 import com.secondhand.debug.MyDebug;
 import com.secondhand.resource.LocalizationStrings;
 
 public class MainMenuScene extends GameMenuScene implements IOnMenuItemClickListener {
 
 	private static final int MENU_NEW_GAME = 0;
 	private static final int MENU_HIGH_SCORE = 2;
 	private static final int MENU_SETTINGS = 3;
 	
 	public MainMenuScene(final Engine engine, final Context context) {
 		super(engine, context);
 	}
 
 	@Override
 	public void loadScene() {
 		MyDebug.d("loading main menu scene");
 		super.loadScene();
 		
 		final int menuStartX = layoutHeadline("Twirl");
 		
 		
 		// make a centered menu.
 		final List<GameMenuScene.MenuItem> menuItems = new ArrayList<GameMenuScene.MenuItem>();
 		
 		menuItems.add(new MenuItem(MENU_NEW_GAME, LocalizationStrings.getInstance().getLocalizedString("menu_new_game")));
 		menuItems.add(new MenuItem(MENU_SETTINGS, LocalizationStrings.getInstance().getLocalizedString("menu_settings")));
 		menuItems.add(new MenuItem(MENU_HIGH_SCORE, LocalizationStrings.getInstance().getLocalizedString("menu_high_score")));
 		
 		layoutCenteredMenu(menuStartX, menuItems);
 		this.setOnMenuItemClickListener(this);
 	}
 	
 
 	@Override
 	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem,
 			
 			final float pMenuItemLocalX, final float pMenuItemLocalY) {
 		
 		switch(pMenuItem.getID()) {
 		case MENU_NEW_GAME:
 			MyDebug.i("now the game should start");
			setScene(AllScenes.GAME_PLAY_SCENE_LOADING_SCENE);
 			return true;
 		case MENU_SETTINGS:
 			MyDebug.i("now a settings menu should appear");
 			setScene(AllScenes.SETTINGS_MENU_SCENE);
 			return true;
 		case MENU_HIGH_SCORE:
 			setScene(AllScenes.HIGH_SCORE_SCENE);
 			return true;
 		default:
 			return false;
 		}
 
 	}
 	
 	@Override
 	public AllScenes getParentScene() {
 		// see the getParentScene method of LoadingScene for a motivation of why null
 		// should be returned here.
 		return null;
 	}
 
 }
