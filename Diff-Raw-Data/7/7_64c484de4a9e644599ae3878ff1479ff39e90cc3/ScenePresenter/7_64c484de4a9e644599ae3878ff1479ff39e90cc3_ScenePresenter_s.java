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
 
 
 import com.github.a2g.core.interfaces.HostingPanelAPI;
 import com.github.a2g.core.interfaces.InternalAPI;
 import com.github.a2g.core.interfaces.ScenePanelAPI;
 import com.github.a2g.core.primitive.Point;
 
 
 public class ScenePresenter {
 	private int width;
 	private int height;
	//private EventBus eventBus;
 	private Scene scene;
 	private ScenePanelAPI view;
 	private double cameraX;
 	private double cameraY;
 
 	public ScenePresenter(final HostingPanelAPI panel, InternalAPI api)
 	{
 		this.cameraX=0.0;
 		this.cameraY=0.0;
 		this.setWidth(320);
 		this.setHeight(180);
 		this.scene = new Scene();
 		this.view = api.getFactory().createScenePanel();
 		panel.setThing(view);
 		view.setVisible(true);
 
 		// this.theInventoryItemMap = new TreeMap<Integer, InventoryItem>();
 	}
 
 	public ScenePanelAPI getView() {
 		return view;
 	}
 
 	public void setView(ScenePanelAPI view) {
 		this.view = view;
 	}
 
 	// start go in panel
 
 	public void setPixelSize(int width, int height) {
 
 		this.view.setScenePixelSize(width,height);
 	}
 
 	public Point getSizeOfSceneArea() {
 		// int width = this.sceneArea.
 		return new Point(this.getWidth(),
 				this.getHeight());
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 	// end go in panel.
 
 	public void clear() {
 		view.clear();
 	}
 
 
 	public Scene getModel() {
 		return scene;
 	}
 
 	public void reset()
 	{
 		this.scene = new Scene();
 	}
 
 	public double getCameraY()
 	{
 		return this.cameraY;
 	}
 
 	public double getCameraX()
 	{
 		return this.cameraX;
 	}
 
 	public void setCameraX(double x)
 	{
 		this.cameraX = x;
 		offsetAllImagesInScene();
 	}
 
 	public void setCameraY(double y)
 	{
 		this.cameraY = y;
 		offsetAllImagesInScene();
 	}
 	private void offsetAllImagesInScene()
 	{
 		double camX = cameraX*this.width;
 		double camY = cameraY*this.height;
 		view.setCameraOffset((int)camX,(int)camY);
 		for(int i=0;i<this.scene.objectCollection().count();i++)
 		{
 			this.scene.objectCollection().at(i).updateCurrentImage();
 		}
 	}
 
 }
 
 
 ;
