 /*
  * Copyright 2012 Anthony Cassidy
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.github.a2g.core.objectmodel;
 
 import java.util.TreeMap;
 
 import com.github.a2g.core.action.TalkAction;
 import com.github.a2g.core.interfaces.IHostingPanel;
 import com.github.a2g.core.interfaces.IMasterPresenterFromScene;
 import com.github.a2g.core.interfaces.IScenePresenter;
 import com.github.a2g.core.interfaces.IScenePanelFromScenePresenter;
 import com.github.a2g.core.primitive.ColorEnum;
 import com.github.a2g.core.primitive.Point;
 import com.github.a2g.core.primitive.Rect;
 import com.github.a2g.core.primitive.RectF;
 
 public class ScenePresenter implements IScenePresenter {
 	private int width;
 	private int height;
 	private Scene scene;
 	private IScenePanelFromScenePresenter view;
 	private double cameraX;
 	private double cameraY;
 	private TreeMap<String, Animation> theAtidMap;
 	private String sceneTalkerAtid;
 	private String sceneWalkerOtid;
 	private ColorEnum talkingColorForScene;
 
 	public ScenePresenter(final IHostingPanel panel,
 			IMasterPresenterFromScene master) {
 		this.cameraX = 0.0;
 		this.cameraY = 0.0;
 		this.width = 320;
 		this.height = 180;
 		this.scene = new Scene();
 		this.view = master.getFactory().createScenePanel(this);
 		panel.setThing(view);
 		view.setVisible(true);
 
 		this.theAtidMap = new TreeMap<String, Animation>();
 		talkingColorForScene = ColorEnum.Fuchsia;
 	}
 
 	private SceneObject getObjectByOCode(short ocode) {
 		return this.scene.objectCollection().getByOCode(ocode);
 	}
 
 	public SceneObject getObjectByOtid(String otid) {
 		return this.scene.objectCollection().getByOtid(otid);
 	}
 
 	public Animation getAnimationByAtid(String atid) {
 		Animation anim = this.theAtidMap.get(atid);
 
 		if (anim == null) {
 			// first param is name, second is parent;
 			anim = new Animation("", null);
 			this.theAtidMap.put(atid, anim);
 		}
 		return anim;
 	}
 
 	@Override
 	public IScenePanelFromScenePresenter getView() {
 		return view;
 	}
 
 	public void setView(IScenePanelFromScenePresenter view) {
 		this.view = view;
 	}
 
 	public void setScenePixelSize(int width, int height) {
 
 		this.view.setScenePixelSize(width, height);
 	}
 
 	public void clear() {
 		view.clear();
 	}
 
 	public Scene getModel() {
 		return scene;
 	}
 
 	public void reset() {
 		this.scene = new Scene();
 	}
 
 	@Override
 	public double getCameraY() {
 		return this.cameraY;
 	}
 
 	@Override
 	public double getCameraX() {
 		return this.cameraX;
 	}
 
 	@Override
 	public void setCameraX(double x) {
 		this.cameraX = x;
 		offsetAllImagesInScene();
 	}
 
 	public void setCameraY(double y) {
 		this.cameraY = y;
 		offsetAllImagesInScene();
 	}
 
 	private void offsetAllImagesInScene() {
 		double camX = cameraX * this.width;
 		double camY = cameraY * this.height;
 		view.setCameraOffset((int) camX, (int) camY);
 		for (int i = 0; i < this.scene.objectCollection().count(); i++) {
 			this.scene.objectCollection().getByIndex(i).updateCurrentImage();
 		}
 	}
 
 	public void addSceneObject(SceneObject destObject) {
 		this.scene.objectCollection().add(destObject);
 	}
 
 	public void addAnimation(String atid, Animation destAnimation) {
 		if (theAtidMap.get(atid) == null) {
 			// System.out.println("ScenePresenter::added " + animTextualId);
 			this.theAtidMap.put(atid, destAnimation);
 		}
 	}
 
 	public void alignBaseMiddleOfOldFrameToFrameOfSpecifiedAnimationByAtid(
 			int frame, String atid) {
 		getAnimationByAtid(atid)
 		.alignBaseMiddleOfOldFrameToFrameOfThisAnimation(frame);
 
 	}
 
 	@Override
 	public int getSceneObjectCount() {
 		return scene.objectCollection().count();
 	}
 
 	@Override
 	public String getOtidByIndex(int i) {
 		return scene.objectCollection().getByIndex(i).getOtid();
 	}
 
 	@Override
 	public String getAtidOfCurrentAnimationByOtid(String otid) {
 		SceneObject o = scene.objectCollection().getByOtid(otid);
 		if(o!=null)
 		{
 			String a = o.getCurrentAnimation();
 			return a;
 		}
 		return "getAtidOfCurrentAnimationByOtid couldn't find current animation";
 	}
 
 	public void setOtidOfDefaultWalkObject(String otid) {
 		setSceneWalkerOtid(otid);
 	}
 
 	@Override
 	public boolean getVisibleByOtid(String otid) {
 		return scene.objectCollection().getByOtid(otid).isVisible();
 	}
 
 	@Override
 	public int getCurrentFrameByOtid(String otid) {
 		return scene.objectCollection().getByOtid(otid).getCurrentFrame();
 	}
 
 	@Override
 	public int getSceneGuiWidth() {
 		return this.width;
 	}
 
 	@Override
 	public int getSceneGuiHeight() {
 		return this.height;
 	}
 
 	@Override
 	public String getDisplayNameByOtid(String otid) {
 		return this.scene.objectCollection().getByOtid(otid).getDisplayName();
 	}
 
 	@Override
 	public Rect getBoundingRectByFrameAndAtid(int frame, String atid) {
 		Rect toReturn = new Rect(0,0,1,1);
 		Animation a = getAnimationByAtid(atid);
 		if(a!=null)
 		{
 			ImageCollection fc = a.getFrames();
 			Image i = fc.getByIndex(frame);
 			if(i!=null)
 				toReturn = i.getBoundingRect();
 			else 
 				assert(false);
 		}
 		return toReturn;
 	}
 
 	@Override
 	public double getXByOtid(String otid) {
 		return this.scene.objectCollection().getByOtid(otid).getX();
 	}
 
 	@Override
 	public double getYByOtid(String otid) {
 		return this.scene.objectCollection().getByOtid(otid).getY();
 	}
 
 	@Override
 	public short getCodeByOtid(String otid) {
 		return getObjectByOtid(otid).getOCode();
 	}
 
 	public String getOtidByCode(short ocode) {
 		if(getObjectByOCode(ocode)==null)
 			return "";
 		return getObjectByOCode(ocode).getOtid();
 	}
 
 	public void setStateOfPopup(String atid, boolean isVisible, String speech, TalkAction sayAction) {
 		Animation a = this.getAnimationByAtid(atid);
 		RectF r = a.getMaxSpeechBalloonExtents();
 		Rect pixels = new Rect(
 				(int)(r.getLeft() * this.getSceneGuiWidth())
 				,	(int)(r.getTop() * this.getSceneGuiHeight())
 				, (int)(r.getWidth()* this.getSceneGuiWidth())
 				, (int)(r.getHeight()* this.getSceneGuiHeight())
 				);
 
 		ColorEnum talkingColor = a.getTalkingColor();
 		if(talkingColor==null)
 		{
 			talkingColor = this.talkingColorForScene;
 		}
 		Point mouth = a.getSceneObject().getMouthLocation();
 		view.setStateOfPopup(isVisible, talkingColor, speech, pixels, mouth, sayAction);
 	}
 
 	public String getSceneTalkerAtid() {
 		return sceneTalkerAtid;
 	}
 
 	public void setSceneTalkerAtid(String sceneTalkerAtid) {
 		this.sceneTalkerAtid = sceneTalkerAtid;
 	}
 
 	public String getSceneWalkerOtid() {
 		return sceneWalkerOtid;
 	}
 
 	public void setSceneWalkerOtid(String sceneWalkerOtid) {
 		this.sceneWalkerOtid = sceneWalkerOtid;
 	}
 };
