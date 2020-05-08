 package se.danielduner.minesweeper.client;
 
 import se.danielduner.minesweeper.client.resources.Images;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 
 public class Square extends Composite {
 	private static Images images = GWT.create(Images.class);
 	
	private int value = Integer.MIN_VALUE;
 	private AbsolutePanel absolutePanel = new AbsolutePanel();
 	private Label button;
 	private Image image = new Image();
 	
 	public Square(int value) {
 		absolutePanel.setSize("32px", "32px");
 		initWidget(absolutePanel);
 		button = new Label();
 		image = new Image();
 		absolutePanel.add(button, 0, 0);
 		absolutePanel.add(image, 1, 2);
 		button.setStyleName("MineBox");
 		setType(value);
 	}
 	
 	public void setType(int value) {
 		if (this.value == value) {
 			return;
 		} else if(value == MineField.HIDDEN) {
 			button.setText("");
 			absolutePanel.remove(image);
 			setImage(new Image());
 			button.setStyleDependentName("hidden", true);
 		} else if (value == MineField.FLAGGED) {
 			button.setText("");
 			setImage(new Image(images.flagImage()));
 			button.setStyleDependentName("flagged", true);
 		} else if (value == MineField.EXPLODEDMINE) {
 			button.setText("");
 			setImage(new Image(images.bangImage()));
 			button.setStyleDependentName("exploded", true);
 		} else if (value == MineField.HIDDENMINE) {
 			button.setText("");
 			setImage(new Image(images.mineImage()));
			button.setStyleDependentName("hidden", true);
		} else if (value == MineField.FOUNDMINE) {
			button.setText("");
			setImage(new Image(images.mineImage()));
			button.setStyleDependentName("found", true);
 		} else if (value==0){
 			button.setText("");
 			setImage(new Image());
 			button.setStyleDependentName("exposed", true);
 			button.setStyleDependentName(Integer.toString(value), true);
 		} else {
 			button.setText(Integer.toString(value));
 			setImage(new Image());
 			button.setStyleDependentName("exposed", true);
 			button.setStyleDependentName(Integer.toString(value), true);
 		}
 	}
 	
 	private void setImage(Image image) {
 		absolutePanel.remove(this.image);
 		this.image = image;
 		absolutePanel.add(image, 4, 4);
 	}
 }
