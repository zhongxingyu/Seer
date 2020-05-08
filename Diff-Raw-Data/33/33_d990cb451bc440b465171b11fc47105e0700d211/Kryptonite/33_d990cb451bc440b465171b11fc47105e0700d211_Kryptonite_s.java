 /** ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  *  +      Zuercher Hochschule angewandter Wissenschaften      +
  *  +                    Software Projekt 2                    +
  *  +                                                          +
  *  +        Gruppe 10: Miro Ljubicic & Mathias Weigert        +
  *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  */
 package krypto.gui.main;
 
 import javafx.application.Application;
 import javafx.scene.Scene;
 import javafx.scene.control.MenuBar;
 import javafx.scene.control.SelectionMode;
 import javafx.scene.control.TreeView;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.layout.StackPane;
 import javafx.stage.Stage;

 import krypto.gui.action.menu.MainTreeChangeListener;
import krypto.gui.action.panel.EncryptEvent;
 import krypto.gui.javafx.elements.MainTree;
 import krypto.gui.javafx.panel.ChiffrePanelFX;
 import krypto.gui.javafx.panel.InfoPanelFX;
 import krypto.gui.javafx.panel.bottom.CaesarBottomFX;
 import krypto.gui.javafx.panel.right.CaesarRightFX;
 import krypto.gui.menu.MainMenu;
 
 /**
  * @author Mathias Weigert & Miro Ljubicic
  * @version 1.0
  */
 public class Kryptonite extends Application{
 
 	private StackPane centerPane;
 	private StackPane rightPane;
 	private StackPane bottomPane;
 	private Stage ps;
 	private BorderPane border = new BorderPane();
 	private Boolean infoMode;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		launch(args);
 	}
 
 	@Override
 	public void start(Stage primaryStage) {
 		ps = primaryStage;
 		ps.setTitle("Kryptonite - Software Project 2");
 		//		ps.setFullScreen(true);
 
 		TreeView<String> tree = new TreeView<String> (new MainTree().getTree());
 		tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
 		tree.getSelectionModel().selectedItemProperty().addListener(new MainTreeChangeListener(this));
 		MenuBar menuBar = new MainMenu(ps, this).getMenu();
 
 		StackPane treePane =  new StackPane();
 		centerPane = new InfoPanelFX().getPanel();
 		infoMode = true;
 		treePane.getChildren().add(tree);
 
 		border.setLeft(treePane);
 		border.setTop(menuBar);
 		border.setCenter(centerPane);
 
 		ps.setScene(new Scene(border));
 		ps.sizeToScene();
 		ps.show();
 	}
 
 	public Boolean getInfoMode() {
 		return infoMode;
 	}
 	
 	public void setToCaesarCipher() {
 		ChiffrePanelFX caesarPanel = new ChiffrePanelFX('C', new CaesarRightFX(), new CaesarBottomFX());
 //		caesarPanel.setActiveChiffre('C');
 //		System.out.println(caesarPanel.getActiveChiffre());
 //		caesarPanel.setRightPanel(new CaesarRightFX().getPanel());
 //		caesarPanel.setBottomPanel(new CaesarBottomFX().getPanel());
 //		caesarPanel.setEncryptEvent(new EncryptEvent(caesarPanel.getPlainText(), caesarPanel.getCipherText(), caesarPanel.getRightPanel(), 'C'));
 
 		infoMode = false;
 		bottomPane = caesarPanel.getBottomPanel();
 		rightPane = caesarPanel.getRightPanel();
 		centerPane = caesarPanel.getPanel();
 		border.setCenter(centerPane);
 		this.setRightPane(rightPane);
 		this.setBottomPane(bottomPane);
 		ps.sizeToScene();
 		ps.show();
 	}
 	
 	public void setBottomPane(StackPane pane) {
 		bottomPane = pane;
 		border.setBottom(bottomPane);
 		ps.sizeToScene();
 		ps.show();
 	}
 	
 	public void setCenterPane(StackPane pane) {
 
 		centerPane = pane;
 		border.setCenter(centerPane);
 		ps.sizeToScene();
 		ps.show();
 	}
 
 	public void setInfoMode(Boolean infoMode) {
 		this.infoMode = infoMode;
 	}
 	
 	public void setRightPane(StackPane pane) {
 
 		rightPane = pane;
 		border.setRight(rightPane);
 		ps.sizeToScene();
 		ps.show();
 	}
 
 }
