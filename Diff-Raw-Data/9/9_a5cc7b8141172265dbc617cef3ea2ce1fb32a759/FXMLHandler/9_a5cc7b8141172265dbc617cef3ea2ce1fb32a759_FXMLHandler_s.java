 /** 
  * Copyright [2013] Antonio J. Iniesta
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * 
  * File created: 01/02/2013 at 22:34:10 by antonio
  */
 package com.iniesta.layerfx;
 
 import java.io.IOException;
 
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.stage.Modality;
 import javafx.stage.Stage;
 import javafx.stage.StageStyle;
 
 /**
  * @author antonio
  * This class is a handler for FXML and create and embed some basic and common tasks from FXML files
  */
 public class FXMLHandler {
 
 	private String fxmlFile;
 	private Parent root;
 	private Stage stage;
 	private boolean modalApplication;
 	private StageStyle stageStyle;
 
 	public FXMLHandler(Class<?> clazz){
 		this(calculateFxmlFile(clazz));
 	}
 	
 	public FXMLHandler(String fxmlFile){
 		this.fxmlFile = fxmlFile;
 		this.stageStyle = StageStyle.DECORATED;
 	}
 
 	/**
 	 * Set the stage as APPLICATION_MODAL
 	 * @param isModal
 	 */
 	public void setModality(boolean isModal){
 		this.modalApplication = isModal;
 	}
 	
 	public void setStageStyle(StageStyle stageStyle){
 		this.stageStyle = stageStyle;
 	}
 	/**
 	 * Get the handling view
 	 * @return 
 	 * @return
 	 * @throws IOException 
 	 */
 	@SuppressWarnings("static-access")
 	public HandlingView getHandlingView() throws IOException{
		HandlingView view = null;
 		FXMLLoader fxmlLoader = new FXMLLoader();
 		this.root = fxmlLoader.load(getClass().getResource(fxmlFile));
		view = (HandlingView)fxmlLoader.getController();
 		return view;
 	}
 	
 	/**
 	 * Show window and keep with the execution
 	 */
 	public void showWindow(){
 		showWindow("",false);
 	}
 	
 	public void showWindow(String title){
 		showWindow(title,false);
 	}
 	
 	/**
 	 * Show window and wait until the stage will be closed
 	 */
 	public void showWindowAndWait(){
 		showWindow("",true);
 	}
 	
 	public void showWindowAndWait(String title){
 		showWindow(title,true);
 	}
 	
 	public void showWindow(String title, boolean wait){
 		this.stage = new Stage(stageStyle);
 		if(modalApplication){
 			this.stage.initModality(Modality.APPLICATION_MODAL);
 		}
 		this.stage.setScene(new Scene(root));
 		this.stage.setTitle(title);
 		if(wait){
 			this.stage.showAndWait();
 		}else{
 			this.stage.show();
 		}
 	}
 
 	/**
 	 * Calculate the default FXML File based on the clazz name
 	 * @param clazz
 	 * @return
 	 */
 	protected static String calculateFxmlFile(Class<?> clazz) {
 		String fileName = null;
 		if(clazz!=null){
 			String canonical = clazz.getCanonicalName();
 			if(canonical!=null){
 				fileName = "/";
 				String[] chunks = canonical.split("\\.");
 				if(chunks!=null){
 					for (int i = 0; i < chunks.length-1; i++) {
 						fileName += chunks[i]+"/";
 					}
 					fileName += chunks[chunks.length-1]+".fxml";
 				}
 			}
 		}
 		return fileName;
 	}
 }
