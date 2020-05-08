 /*
  * Copyright 2011 Alexander Baumgartner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madthrax.ridiculousRPG.ui;
 
 import javax.script.ScriptException;
 
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.actions.Delay;
 import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
 import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
 import com.badlogic.gdx.scenes.scene2d.actions.Remove;
 import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.badlogic.gdx.utils.IntMap;
 import com.madthrax.ridiculousRPG.GameBase;
 
 /**
  * This class provides a customizable standard menu for the game.<br>
  * 
  * @author Alexander Baumgartner
  */
 public class StandardMenuService extends ActorsOnStageService implements
 		MenuService {
 
 	private IntMap<MenuStateHandler> stateHandlerMap = new IntMap<MenuStateHandler>(
 			16);
 	private MenuStateHandler activeState;
 	private String startNewGameScript;
 
 	@Override
 	public boolean keyUp(int keycode) {
		return (activeState != null && activeState.processInput(keycode, this))
				|| super.keyUp(keycode);
 	}
 
 	/**
 	 * Refresh the menu. Call this method after you performed an action which
 	 * changes the behavior of the menu creation itself. The menu will be
 	 * recreated.
 	 */
 	public void rebuildMenu() {
 		changeState(activeState);
 	}
 
 	/**
 	 * Clear the menu. If you have to rebuild the menu (see
 	 * {@link #rebuildMenu()}), maybe you want to clean it first. This method
 	 * removes all GUI elements from the screen.
 	 */
 	public void clearAllMenus() {
 		super.clear();
 	}
 
 	public boolean changeState(MenuStateHandler newState) {
 		if (newState != null
 				&& newState.isFreezeTheWorld()
 				&& !GameBase.$serviceProvider().requestAttention(this, true,
 						newState.isClearTheScreen())) {
 			return false;
 		}
 		if (activeState != null && activeState.isFreezeTheWorld()
 				&& !GameBase.$serviceProvider().releaseAttention(this)) {
 			throw new RuntimeException(
 					"Oooops, couldn't release the attention. Something got terribly wrong!");
 		}
 		if (newState == null) {
 			super.clear();
 		} else {
 			if (newState.isClearTheMenu()) {
 				super.clear();
 			}
 			newState.createGui(this);
 		}
 		activeState = newState;
 		return true;
 	}
 
 	@Override
 	public void addGUIcomponent(Object component) {
 		if (component instanceof Actor)
 			addActor((Actor) component);
 	}
 
 	public void center(Object obj) {
 		if (obj instanceof Actor) {
 			Actor actor = (Actor) obj;
 			actor.x = (int) (centerX() - actor.width * .5f);
 			actor.y = (int) (centerY() - actor.height * .5f);
 		}
 	}
 
 	@Override
 	public void focus(Object guiElement) {
 		if (guiElement instanceof Actor) {
 			super.focus((Actor) guiElement);
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		clearAllMenus();
 		super.resize(width, height);
 		if (activeState != null)
 			rebuildMenu();
 	}
 
 	@Override
 	public float getHeight() {
 		return height;
 	}
 
 	@Override
 	public float getWidth() {
 		return width;
 	}
 
 	public void showInfoNormal(String info) {
 		showInfo(getSkinNormal(), info);
 	}
 
 	public void showInfoFocused(String info) {
 		showInfo(getSkinFocused(), info);
 	}
 
 	private void showInfo(final Skin skin, String info) {
 		final Window w = new Window(skin);
 		addActor(w);
 
 		w.touchable = false;
 		w.color.a = .1f;
		w.action(Sequence.$(FadeIn.$(.3f), Delay.$(FadeOut.$(.3f), 2f),
				Remove.$()));
 		w.add(new Label(info, skin));
 
 		w.pack();
 		center(w);
 	}
 
 	/**
 	 * Replaces or adds a state with an associated {@link MenuStateHandler}.
 	 * 
 	 * @param state
 	 *            An int value representing one state
 	 * @param stateHandler
 	 *            The state handler associated with the state
 	 * @return The old {@link MenuStateHandler}
 	 */
 	public MenuStateHandler putStateHandler(int state,
 			MenuStateHandler stateHandler) {
 		return stateHandlerMap.put(state, stateHandler);
 	}
 
 	/**
 	 * Returns the state handler associated with the given state.
 	 */
 	public MenuStateHandler getStateHandler(int state) {
 		return stateHandlerMap.get(state);
 	}
 
 	/**
 	 * Removes and returns the state handler associated with the state.
 	 */
 	public MenuStateHandler removeStateHandler(int state) {
 		return stateHandlerMap.remove(state);
 	}
 
 	@Override
 	public boolean changeState(int newState) {
 		return changeState(stateHandlerMap.get(newState));
 	}
 
 	@Override
 	public MenuStateHandler getActiveStateHandler() {
 		return activeState;
 	}
 
 	public void startNewGame() throws ScriptException {
 		GameBase.$().eval(startNewGameScript);
 	}
 
 	public void setStartNewGameScript(String startNewGameScript) {
 		this.startNewGameScript = startNewGameScript;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		for (MenuStateHandler handler : stateHandlerMap.values()) {
 			handler.dispose();
 		}
 	}
 }
