 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package game.plugins.editors.graph;
 
 
 import game.configuration.Configurable.Change;
 import game.core.Block;
 import game.editorsystem.Editor;
 import game.editorsystem.EditorWindow;
 import game.editorsystem.Option;
 import game.main.Settings;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.ClipboardContent;
 import javafx.scene.input.DataFormat;
 import javafx.scene.input.DragEvent;
 import javafx.scene.input.Dragboard;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.input.TransferMode;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.VBox;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Text;
 import javafx.scene.text.TextAlignment;
 
 public class BlockNode extends VBox implements Observer {
 
 	private final Image STATUSOK = new Image(getClass().getResourceAsStream("blockOk.png"));
 	private final Image STATUSERRORS = new Image(getClass().getResourceAsStream("blockErrors.png"));
 	
 	public static final DataFormat BLOCKDATA = new DataFormat("game/block");
 	
 	private Block block;
 	
 	private boolean isTemplate;
 	
 	private ImageView status = new ImageView();
 	
 	private Text blockName;
 	
 	private HBox wrapper;
 	
 	private GraphPane pane;
 
 	public BlockNode(Block b, boolean isTpl, GraphPane p) {
 		this.block = b;
 		this.isTemplate = isTpl;
 		this.pane = p;
 		
 		if (isTemplate)
 			block.setOption("name", block.getClass().getSimpleName());
 		
 		setStyle("-fx-border-style: solid; -fx-border-color:gray;");
 		setPadding(new Insets(5));
 		
 		AnchorPane imagePane = new AnchorPane();
 		Rectangle rect = new Rectangle(60, 60);
 		rect.setFill(Color.INDIGO);
 		imagePane.getChildren().addAll(rect, status);
 		
 		blockName = new Text((String)block.getOption("name"));
 		blockName.setTextAlignment(TextAlignment.CENTER);
 		blockName.setWrappingWidth(60.0);
 		
 		getChildren().addAll(imagePane, blockName);
 		
 		setOnDragDetected(new EventHandler<MouseEvent>() {
 			@Override
 			public void handle(MouseEvent event) {
 				Dragboard db = startDragAndDrop(isTemplate ? TransferMode.COPY : TransferMode.MOVE);
 				
 				ClipboardContent content = new ClipboardContent();
 				Settings.getInstance().setDragging( isTemplate ?
 						new BlockNode((Block)block.cloneConfiguration(), false, pane) : BlockNode.this);
 				content.put(BLOCKDATA, new HandlePosition(event.getX(), event.getY()));
 				
 				db.setContent(content);
 				
 				event.consume();
 			}
 		});
 		
 		setOnDragDone(new EventHandler<DragEvent>() {
 			@Override
 			public void handle(DragEvent event) {
 				Settings.getInstance().setDragging(null);
 				event.consume();
 			}
 		});
 	
 		if (!isTemplate) {
 			status.setImage(block.getConfigurationErrors().isEmpty() ? STATUSOK : STATUSERRORS);
 			
 			block.addObserver(this);
 			
 			setOnMouseClicked(new EventHandler<MouseEvent>() {
 				@Override
 				public void handle(MouseEvent event) {
 					if (event.getClickCount() > 1) {
 						Option option = new Option(block);
 						Editor editor = option.getBestEditor();
 						new EditorWindow(editor).show();
 					}
 				}
 			});
 		}
 	}
 	
 	public void disconnect() {
 		block.deleteObserver(this);
 	}
 	
 	public Block getBlock() {
 		return block;
 	}
 	
 	public void setWrapper(HBox wrapper) {
 		this.wrapper = wrapper;
 	}
 	
 	public HBox getWrapper() {
 		return wrapper;
 	}
 	
 	public void setPosition(HandlePosition handle, double left, double top) {
 		if (wrapper == null) {
 			setLayoutX(left-handle.x);
 			setLayoutY(top-handle.y);
 		} else {
 			wrapper.setLayoutX(left-handle.x);
 			wrapper.setLayoutY(top-handle.y);
 		}
 	}
 	
 	public void setDragging(boolean dragging) {
 		if (dragging)
 			(wrapper != null ? wrapper : this).setOpacity(0.3);
 		else
 			(wrapper != null ? wrapper : this).setOpacity(1.0);
 	}
 	
 	@Override
 	public void update(Observable o, Object arg) {
 		if (arg instanceof Change) {
 			Change change = (Change)arg;
 			if (change.getPath().equals("name")) {
 				if (!blockName.getText().equals(block.name)) {
 					blockName.setText((String)block.getOption("name"));
 					pane.fixPosition(BlockNode.this);
 				}
 			}
 			
 			status.setImage(block.getConfigurationErrors().isEmpty() ? STATUSOK : STATUSERRORS);
 		}
 	}
 	
 }
