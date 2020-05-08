 package com.objectgraph.gui;
 
 
 
 import javafx.application.Application;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.layout.VBox;
 import javafx.stage.Stage;
 
 import com.google.common.base.Strings;
 import com.objectgraph.core.Constraint;
 import com.objectgraph.core.MapNode;
 import com.objectgraph.core.Node;
 import com.objectgraph.core.ObjectNode;
 import com.objectgraph.core.triggers.Assignment;
 import com.objectgraph.gui.editors.ImplementationChooserEditor;
 import com.objectgraph.gui.editors.StringEditor;
 
 public class Prova {
 	private static ProvaNode node = new ProvaNode();
 	
 	public static class ProvaNode extends ObjectNode {
 		@Property
 		protected String s;
 		@Property
 		protected Node node;
 		
 		public ProvaNode() {
 			addConstraint("node", new Constraint<ProvaNode, Node>() {
 				@Override
 				protected boolean check(Node element) {
 					if (Strings.nullToEmpty(getNode().s).equals("Automatic message"))
 						return element instanceof MapNode;
 					else
 						return true;
 				}
 			});
 		}
 	}
 	
 	public static class ProvaParent extends ObjectNode {
 		@Property
 		protected String t = "Ciao";
 		@Property
 		protected ProvaNode child;
 		
 		public ProvaParent() {
 			addTrigger(new Assignment("t", "child.s"));
 		}
 	}
 
 	public static class MyApp extends Application {
 		@Override
 		public void start(Stage stage) throws Exception {
 			EditorPane editor1 = node.attachEditor("s", new StringEditor());
 			EditorPane editor2 = node.attachEditor("s", new StringEditor());
 			EditorPane editor3 = node.attachEditor("node", new ImplementationChooserEditor());
 			
 			VBox box = new VBox();
 			Button test = new Button("Automatic");
 			test.setOnAction(new EventHandler<ActionEvent>() {
 				@Override
 				public void handle(ActionEvent event) {
 					node.set("s", "Automatic message");
 				}
 			});
 			box.getChildren().addAll(editor1, editor2, editor3, test);
 			
 			stage.setScene(new Scene(box));
 			stage.show();
 		}
 	}
 	
 	public static void provaParent(ProvaNode node) {
 		new ProvaParent().set("child", node);
 	}
 
 	public static void main(String[] args) {
		Application.launch(MyApp.class);
 		node.detachAllEditors();
 		int i = 1;
 		ProvaNode node = new ProvaNode();
 		provaParent(node);
 		while (node.getFreeProperties().size() != 1) {
 			System.out.println(i++);
 		}
 
 		System.out.println("Finished!");
 	}
 
 }
