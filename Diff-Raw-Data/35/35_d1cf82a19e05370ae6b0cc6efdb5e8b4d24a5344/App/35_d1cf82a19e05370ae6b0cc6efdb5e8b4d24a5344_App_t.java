 /*
  * Copyright 2013 tarrsalah.org.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.tarrsalah.minimal.javafx.pom;
 
 import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
 import javafx.scene.SceneBuilder;
 import javafx.scene.control.ButtonBuilder;
 import javafx.scene.layout.BorderPaneBuilder;
 import javafx.stage.Stage;
 
 /**
  * App.java (UTF-8)
  *
  * Apr 30, 2013
  *
  * @author tarrsalah.org
  */
 public class App extends Application {
 
 	public static void main(String[] args) {
 		launch(args);
 	}
	private String cssFileString;
	private String cssFile;
 
 	@Override
 	public void start(Stage stage) {

//		cssFile = getClass().getClassLoader().getResource(cssFileString).toExternalForm();
		cssFile = "styles/main.css";
 
 		stage.setScene(SceneBuilder.create()
 				.root(BorderPaneBuilder.create()
				.center(ButtonBuilder.create()
				.text("red button")
				.onAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				System.out.println(cssFile);
			}
		})
				.id("red_btn")
				.build())
				.build())
 				.width(400)
 				.height(380)
 				.stylesheets(cssFile)
				.build());
 		stage.show();
 	}
}
