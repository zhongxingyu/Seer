 package fi.mikuz.boarder.component.soundboard;
 
 import java.io.File;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.util.editor.ImageDrawing;
 import fi.mikuz.boarder.util.editor.SoundNameDrawing;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 @XStreamAlias("graphical-sound")
 public class GraphicalSound implements Cloneable {
 	
 	public static final int SHOW_ALL = 0;
 	public static final int HIDE_IMAGE = 1;
 	public static final int HIDE_TEXT = 2;
 	private int hideImageOrText;
 	
 	public static final int SECOND_CLICK_PLAY_NEW = 0;
 	public static final int SECOND_CLICK_PAUSE = 1;
 	public static final int SECOND_CLICK_STOP = 2;
 	private int secondClickAction;
 	
 	private String name;
 	private File path;
 	private float volumeLeft;
 	private float volumeRight;
 	private float nameFrameX;
 	private float nameFrameY;
 	private File imagePath;
 	private Bitmap image;
 	private float imageX;
 	private float imageY;
 	private float imageWidth;
 	private float imageHeight;
 	private File activeImagePath;
 	private Bitmap activeImage;
 	private int nameTextColor;
 	private int nameFrameInnerColor;
 	private int nameFrameBorderColor;
 	private boolean showNameFrameInnerPaint;
 	private boolean showNameFrameBorderPaint;
 	private boolean linkNameAndImage;
 	private float nameSize;
 	private float namePixelSize;
 	private int autoArrangeColumn;
 	private int autoArrangeRow;
 	
 	public GraphicalSound() {
 		this.setName("blank");
 		this.setNameSize(30);
 		this.setPath(null);
 		this.setVolumeLeft(1.0f);
 		this.setVolumeRight(1.0f);
 		
 		this.setNameFrameX(50);
 		this.setNameFrameY(50);
 		
 		this.setImagePath(null);
 		this.setImageWidth(50);
 		this.setImageHeight(50);
 		this.setHideImageOrText(GraphicalSound.SHOW_ALL);
 		this.generateImageXYFromNameFrameLocation();
 		this.setLinkNameAndImage(true);
 		
 		this.setNameTextColor(255, 255, 255, 255);
 		this.setNameFrameInnerColor(225, 75, 75, 75);
 		this.setNameFrameBorderColor(255, 255, 255, 255);
 		this.setShowNameFrameInnerPaint(true);
 		this.setShowNameFrameBorderPaint(true);
 	}
 	
 	static public void loadImages(Context context, GraphicalSound sound) {
 		if (sound.getImage() == null) {
 			if (sound.getImagePath() == null) {
 				sound.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.sound));
 			} else {
 				sound.setImage(ImageDrawing.decodeFile(context, sound.getImagePath()));
 			}
 		}
 		if (sound.getActiveImage() == null && sound.getActiveImagePath() != null) {
 			sound.setActiveImage(ImageDrawing.decodeFile(context, sound.getActiveImagePath()));
 		}
 	}
 	
 	static public void unloadImages(GraphicalSound sound) {
 		sound.setActiveImage(null);
 		sound.setImage(null);
 	}
 	
 	public int getAutoArrangeColumn() {
 		return autoArrangeColumn;
 	}
 	public void setAutoArrangeColumn(int autoArrangeColumn) {
 		this.autoArrangeColumn = autoArrangeColumn;
 	}
 	public int getAutoArrangeRow() {
 		return autoArrangeRow;
 	}
 	public void setAutoArrangeRow(int autoArrangeRow) {
 		this.autoArrangeRow = autoArrangeRow;
 	}
 	public float getNameSize() {
 		return this.nameSize;
 	}
 	public void setNameSize(float nameSize) {
 		this.nameSize = nameSize;
 		this.namePixelSize = new SoundNameDrawing(this).getNameFrameRect().width();
 	}
 	public float getNamePixelSize() {
 		return this.namePixelSize;
 	}
 	public boolean getLinkNameAndImage() {
 		return linkNameAndImage;
 	}
 	public void setLinkNameAndImage(boolean linkNameAndImage) {
 		this.linkNameAndImage = linkNameAndImage;
 	}
 	public boolean getShowNameFrameInnerPaint() {
 		return showNameFrameInnerPaint;
 	}
 	public void setShowNameFrameInnerPaint(boolean showNameFrameInnerPaint) {
 		this.showNameFrameInnerPaint = showNameFrameInnerPaint;
 	}
 	public boolean getShowNameFrameBorderPaint() {
 		return showNameFrameBorderPaint;
 	}
 	public void setShowNameFrameBorderPaint(boolean showNameFrameBorderPaint) {
 		this.showNameFrameBorderPaint = showNameFrameBorderPaint;
 	}
 	public int getNameTextColor() {
 		return nameTextColor;
 	}
 	public void setNameTextColor(int alpha, int red, int green, int blue) {
 		this.nameTextColor = Color.argb(alpha, red, green, blue);
 	}
 	public void setNameTextColorInt(int nameTextColor) {
 		this.nameTextColor = nameTextColor;
 	}
 	public int getNameFrameInnerColor() {
 		return nameFrameInnerColor;
 	}
 	public void setNameFrameInnerColor(int alpha, int red, int green, int blue) {
 		this.nameFrameInnerColor = Color.argb(alpha, red, green, blue);
 	}
 	public void setNameFrameInnerColorInt(int nameFrameInnerColor) {
 		this.nameFrameInnerColor = nameFrameInnerColor;
 	}
 	public int getNameFrameBorderColor() {
 		return nameFrameBorderColor;
 	}
 	public void setNameFrameBorderColor(int alpha, int red, int green, int blue) {
 		this.nameFrameBorderColor = Color.argb(alpha, red, green, blue);
 	}
 	public void setNameFrameBorderColorInt(int nameFrameBorderColor) {
 		this.nameFrameBorderColor = nameFrameBorderColor;
 	}
 	public int getHideImageOrText() {
 		return hideImageOrText;
 	}
 	public void setHideImageOrText(int hideImageOrText) {
 		this.hideImageOrText = hideImageOrText;
 	}
 	public float getImageX() {
 		return imageX;
 	}
 	public void setImageX(float imageX) {
 		this.imageX = imageX;
 	}
 	public void generateImageXYFromNameFrameLocation() {
 		SoundNameDrawing soundNameDrawing = new SoundNameDrawing(this);
 		setImageX(getNameFrameX() + soundNameDrawing.getNameFrameRect().width()/2 - getImageWidth()/2);
 		setImageY(getNameFrameY() - getImageHeight() - 3);
 	}
 	public void generateNameFrameXYFromImageLocation() {
 		SoundNameDrawing soundNameDrawing = new SoundNameDrawing(this);
 		setNameFrameX(getImageX() + getImageWidth()/2 - soundNameDrawing.getNameFrameRect().width()/2);
 		setNameFrameY(getImageY() + getImageHeight() + 3);
 	}
 	public float getImageY() {
 		return imageY;
 	}
 	public void setImageY(float imageY) {
 		this.imageY = imageY;
 	}
 	public float getImageWidth() {
 		return imageWidth;
 	}
 	public void setImageWidth(float imageWidth) {
 		this.imageWidth = imageWidth;
 	}
 	public float getImageHeight() {
 		return imageHeight;
 	}
 	public void setImageHeight(float imageHeight) {
 		this.imageHeight = imageHeight;
 	}
 	public Bitmap getImage() {
 		return image;
 	}
 	public void setImage(Bitmap image) {
 		this.image = image;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public File getPath() {
 		return path;
 	}
 	public void setPath(File path) {
 		this.path = path;
 	}
 	public float getVolumeLeft() {
 		return volumeLeft;
 	}
 	public void setVolumeLeft(float volumeLeft) {
 		this.volumeLeft = volumeLeft;
 	}
 	public float getVolumeRight() {
 		return volumeRight;
 	}
 	public void setVolumeRight(float volumeRight) {
 		this.volumeRight = volumeRight;
 	}
 	public float getNameFrameX() {
 		return nameFrameX;
 	}
 	public void setNameFrameX(float nameFrameX) {
 		this.nameFrameX = nameFrameX;
 	}
 	public float getNameFrameY() {
 		return nameFrameY;
 	}
 	public void setNameFrameY(float nameFrameY) {
 		this.nameFrameY = nameFrameY;
 	}
 	public void setImagePath(File image) {
 		this.imagePath = image;
 	}
 	public File getImagePath() {
 		return imagePath;
 	}
 	public File getActiveImagePath() {
 		return activeImagePath;
 	}
 	public void setActiveImagePath(File activeImagePath) {
 		this.activeImagePath = activeImagePath;
 	}
 	public Bitmap getActiveImage() {
 		return activeImage;
 	}
 	public void setActiveImage(Bitmap activateImage) {
 		this.activeImage = activateImage;
 	}
 	public void setSecondClickAction(int secondClickAction) {
 		this.secondClickAction = secondClickAction;
 	}
 	public int getSecondClickAction() {
 		return secondClickAction;
 	} 
 	public Object clone() {
         try {
             return super.clone();
         } catch(CloneNotSupportedException e) {
             return null;
         }
     }
 }
