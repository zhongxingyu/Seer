 
 import java.util.ArrayList;
 
 import javafx.application.Application;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Menu;
 import javafx.scene.control.MenuBar;
 import javafx.scene.control.MenuItem;
 import javafx.scene.control.MenuItemBuilder;
 import javafx.scene.input.KeyCombination;
 import javafx.scene.layout.StackPane;
 import javafx.scene.paint.Color;
 import javafx.stage.Stage;
 import javafx.stage.StageStyle;
  
 public  class main extends Application {
     
 	
 	
 	public static void main(String[] args) {
         launch(args);
     }
     
     
     @Override
     public void start(final Stage primaryStage) {
         primaryStage.setTitle("ECS");    
         new KeyCombination() {};
         final Menu menuFichier = new Menu("Fichier");
         final Menu menuRestaurant = new Menu("Restaurant");
         final Menu menuCuisine = new Menu("Cuisine");
         final Menu menuAppareilElectrique = new Menu("Appareils Electriques");
         final Menu menuPlanAllumage = new Menu("Plans d'allumage");
         final Menu menuForfait = new Menu("Forfaits");
         final Menu menuAide = new Menu("Aide");
         menuFichier.getItems().add(MenuItemBuilder.create()
         		.text("Option")
         		.onAction(
         		new EventHandler<ActionEvent>()
         		{
         		@Override public void handle(ActionEvent e)
         		{
<<<<<<< HEAD
        			
=======
>>>>>>> 0de9bd6196cfb2a361a552e3e347c50887cf9320
         		}
         		}).accelerator( KeyCombination.keyCombination("ctrl+o")).build());
        
 		menuFichier.getItems().add(MenuItemBuilder.create()
         		.text("Quitter")
         		.onAction(
         		new EventHandler<ActionEvent>()
         		{
         		@Override public void handle(ActionEvent e)
         		{
         			AppCore.SaveRestaurant();
         			primaryStage.close();
         		}
         		}).accelerator( KeyCombination.keyCombination("ctrl+q")).build());
 
         		
         menuRestaurant.getItems().add(MenuItemBuilder.create()
         		.text("Nouveau")
         		.onAction(
         		new EventHandler<ActionEvent>()
         		{
         		@Override public void handle(ActionEvent e)
         		{
         			new NewRestaurantWindow();
         		}
         		}).accelerator( KeyCombination.keyCombination("ctrl+r")).build());
         
         menuCuisine.getItems().add(MenuItemBuilder.create()
         		.text("Nouveau")
         		.onAction(
         		new EventHandler<ActionEvent>()
         		{
         		@Override public void handle(ActionEvent e)
         		{
         			new NewCuisineWindow();
         		}
         		}).accelerator( KeyCombination.keyCombination("ctrl+c")).build());
         
         menuRestaurant.getItems().add(new MenuItem("Modifier/Supprimer"));
         menuCuisine.getItems().add(new MenuItem("Modifier/Supprimer"));
         menuForfait.getItems().add(new MenuItem("Nouveau"));
         menuForfait.getItems().add(new MenuItem("Modifier/Supprimer"));
         menuAppareilElectrique.getItems().add(new MenuItem("Nouveau"));
         menuAppareilElectrique.getItems().add(new MenuItem("Modifier/Supprimer"));
         menuPlanAllumage.getItems().add(new MenuItem("Nouveau"));
         menuPlanAllumage.getItems().add(new MenuItem("Modifier/Supprimer"));
         menuAide.getItems().add(new MenuItem("Guide Utilisateur"));
                
         MenuBar menuBar = new MenuBar();
         
         menuBar.getMenus().addAll(menuFichier, menuRestaurant, menuCuisine, menuAppareilElectrique,menuPlanAllumage,menuForfait,menuAide);
        
         final Group root = new Group();        
         root.getChildren().add(menuBar);
         
       
         Scene MyScene = new Scene(root, 600, 400);
         
         
         menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
         MyScene.getStylesheets().add(main.class.getResource("style.css").toExternalForm());
         
         primaryStage.setScene(MyScene);
 
         primaryStage.show();
         
     }
 }
