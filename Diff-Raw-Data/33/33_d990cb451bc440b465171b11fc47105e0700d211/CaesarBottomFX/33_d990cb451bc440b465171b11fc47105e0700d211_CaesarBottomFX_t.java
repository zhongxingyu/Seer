 /** ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  *  +      Zuercher Hochschule angewandter Wissenschaften      +
  *  +                    Software Projekt 2                    +
  *  +                                                          +
  *  +        Gruppe 10: Miro Ljubicic & Mathias Weigert        +
  *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  */
 package krypto.gui.javafx.panel.bottom;
 
 import javafx.geometry.Insets;
 import javafx.scene.control.TextArea;
 import javafx.scene.layout.StackPane;
 import javafx.scene.layout.VBox;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Text;
 import krypto.gui.javafx.panel.right.BaseRight;
 
 /**
  * @author Mathias Weigert & Miro Ljubicic
  * @version 1.0
  */
 public class CaesarBottomFX extends BaseRight{
 	
 	private TextArea outputArea;
 	private Text timeLabel;
 	
 	public CaesarBottomFX() {
 		super();
		outputArea = new TextArea("er");
		outputArea.setText("klj");
 		timeLabel = new Text();
 		this.addChildren();
 //		panel = new StackPane();
 //		
 	}
 
 	/**
 	 * @param outputArea the outputArea to set
 	 */
 	public void setOutputArea(TextArea outputArea) {
 		this.outputArea = outputArea;
 	}
 
 	/**
 	 * @param timeLabel the timeLabel to set
 	 */
 	public void setTimeLabel(Text timeLabel) {
 		this.timeLabel = timeLabel;
 	}
	

	/**
	 * @return the outputArea
	 */
	public TextArea getOutputArea() {
		return outputArea;
	}
 
 	@Override
 	public void addChildren() {
 		// TODO Auto-generated method stub
 		VBox vBox = new VBox();
 		vBox.setPadding(new Insets(10));
 		vBox.setSpacing(8);
 		
 		Text titel = new Text("Brute Force");
		outputArea = new TextArea("eerre");
 		timeLabel = new Text("Elapsed time: ");
 		timeLabel.setFill(Color.RED);
 		
 		vBox.getChildren().add(titel);
 		vBox.getChildren().add(outputArea);
 		vBox.getChildren().add(timeLabel);
 		
 		panel.getChildren().add(vBox);
 	}
 
 }
