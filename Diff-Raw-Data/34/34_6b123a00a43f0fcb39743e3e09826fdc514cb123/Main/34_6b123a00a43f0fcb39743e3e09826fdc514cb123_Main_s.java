 package com.hatenablog.masquerade0324;
 
<<<<<<< HEAD
 public class Main {
 	
 	public static void main(String[] args) {
 		MMRSSViewer viewer = new MMRSSViewer();
 		viewer.run(args);
=======
import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage stage) {
		 HBox hbox = new HBox();
		 Scene scene = new Scene(hbox);
		 hbox.getChildren().add(new Label("Hello, World"));
		 
		 stage.setScene(scene);
		 stage.setTitle("Hello");
		 stage.show();
	}

	public static void main(String[] args) {
		String endadget = "http://feed.rssad.jp/rss/engadget/rss";
		String gigazine = "http://feed.rssad.jp/rss/gigazine/rss_2.0";
		ArticlesBuilder articlesBuilder = new ArticlesBuilder();
		ArrayList<Article> articleList = articlesBuilder.parseRSS(gigazine);
		for (Article article: articleList) {
			System.out.println(article);
		}
		launch(args);
>>>>>>> 1214aff88621922c4a75d9c704a37bb2b97892c2
 	}
 }
