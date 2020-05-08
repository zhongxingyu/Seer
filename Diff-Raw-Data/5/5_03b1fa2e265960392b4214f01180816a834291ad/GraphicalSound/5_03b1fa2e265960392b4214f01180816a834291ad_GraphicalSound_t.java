 /* ========================================================================= *
  * Boarder                                                                   *
  * http://boarder.mikuz.org/                                                 *
  * ========================================================================= *
  * Copyright (C) 2013 Boarder                                                *
  *                                                                           *
  * Licensed under the Apache License, Version 2.0 (the "License");           *
  * you may not use this file except in compliance with the License.          *
  * You may obtain a copy of the License at                                   *
  *                                                                           *
  *     http://www.apache.org/licenses/LICENSE-2.0                            *
  *                                                                           *
  * Unless required by applicable law or agreed to in writing, software       *
  * distributed under the License is distributed on an "AS IS" BASIS,         *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
  * See the License for the specific language governing permissions and       *
  * limitations under the License.                                            *
  * ========================================================================= */
 
 package fi.mikuz.boarder.component.soundboard;
 
 import java.io.File;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.RectF;
 import android.util.Log;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.util.ImageDrawing;
 import fi.mikuz.boarder.util.editor.SoundNameDrawing;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 @XStreamAlias("graphical-sound")
 public class GraphicalSound implements Cloneable {
 	private static final String TAG = GraphicalSound.class.getSimpleName();
 	
 	private static Bitmap defaultSoundImage;
 	
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
 		this.imageWidth = 50;
 		this.imageHeight = 50;
 		this.setHideImageOrText(GraphicalSound.SHOW_ALL);
 		this.generateImageXYFromNameFrameLocation();
 		this.setLinkNameAndImage(true);
 		
 		this.setNameTextColor(255, 255, 255, 255);
 		this.setNameFrameInnerColor(225, 75, 75, 75);
 		this.setNameFrameBorderColor(255, 255, 255, 255);
 		this.setShowNameFrameInnerPaint(true);
 		this.setShowNameFrameBorderPaint(true);
 	}
 	
 	public void loadImages(Context context) {
 		if (this.image == null) {
 			if (getImagePath() == null) {
 				setDefaultImage(context);
 			} else {
 				this.image = ImageDrawing.decodeSoundImage(context, this);
 				if (this.image == null) setDefaultImage(context);
 			}
 		}
 		if (getActiveImage() == null && getActiveImagePath() != null) {
 			this.activeImage = ImageDrawing.decodeSoundActiveImage(context, this);
 		}
 	}
 	
 	private void reloadImages(Context context) {
 		if (this.image != null) {
 			if (getImagePath() == null) {
 				setDefaultImage(context);
 			} else {
 				this.image = ImageDrawing.decodeSoundImage(context, this);
 				if (this.image == null) setDefaultImage(context);
 			}
 		}
 		if (getActiveImage() != null && getActiveImagePath() != null) {
 			this.activeImage = ImageDrawing.decodeSoundActiveImage(context, this);
 		}
 	}
 	
 	public void unloadImages() {
 		this.image = null;
 		this.activeImage = null;
 	}
 	
 	public void setDefaultImage(Context context) {
 		if ((defaultSoundImage == null || defaultSoundImage.isRecycled()) 
 				&& context != null) { // context null for loadGraphicalSoundboardHolder
 			defaultSoundImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.sound);
 		}
 		this.image = defaultSoundImage;
 	}
 	
 	public void setDefaultActiveImage() {
 		this.activeImage = null;
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
 		this.updatePixelSize();
 	}
 	public void updatePixelSize() {
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
 	public void setImageWidthHeight(Context context, float imageWidth, float imageHeight) {
 		this.imageWidth = imageWidth;
 		this.imageHeight = imageHeight;
 		reloadImages(context);
 	}
 	public float getImageHeight() {
 		return imageHeight;
 	}
 	public Bitmap getImage(Context context) {
 		if (image.isRecycled()) {
 			Log.v(TAG, "Sound image " + getImagePath() + " is recycled. Reloading.");
 			reloadImages(context);
 		}
 		return image;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 		this.updatePixelSize();
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
 	public float getActiveImageWidth() {
 		return imageWidth;
 	}
 	public float getActiveImageHeight() {
 		return imageHeight;
 	}
 	public void setSecondClickAction(int secondClickAction) {
 		this.secondClickAction = secondClickAction;
 	}
 	public int getSecondClickAction() {
 		return secondClickAction;
 	} 
 	public float getMiddleX() {
 		float lowerX = Float.MAX_VALUE;
 		float upperX = Float.MIN_VALUE;
 		
 		if (getHideImageOrText() != HIDE_IMAGE) {
 			float x = getImageX();
			float width = getImageWidth();
 			if (x < lowerX) lowerX = x;
 			if (x + width > upperX) upperX = x + width;
 		}
 		
 		if (getHideImageOrText() != HIDE_TEXT) {
 			SoundNameDrawing soundNameDrawing = new SoundNameDrawing(this);
 			RectF size = soundNameDrawing.getNameFrameRect();
 			float x = size.left;
 			float width = size.right - x;
 			if (x < lowerX) lowerX = x;
 			if (x + width > upperX) upperX = x + width;
 		}
 		
 		float middleX = (upperX-lowerX)/2 + lowerX;
 		
 		return middleX;
 	}
 	public float getMiddleY() {
 		float lowerY = Float.MAX_VALUE;
 		float upperY = Float.MIN_VALUE;
 		
 		if (getHideImageOrText() != HIDE_IMAGE) {
 			float y = getImageY();
			float height = getImageHeight();
 			if (y < lowerY) lowerY = y;
 			if (y + height > upperY) upperY = y + height;
 		}
 		
 		if (getHideImageOrText() != HIDE_TEXT) {
 			SoundNameDrawing soundNameDrawing = new SoundNameDrawing(this);
 			RectF size = soundNameDrawing.getNameFrameRect();
 			float y = size.top;
 			float height = size.bottom - y;
 			if (y < lowerY) lowerY = y;
 			if (y + height > upperY) upperY = y + height;
 		}
 		
 		float middleY = (upperY-lowerY)/2 + lowerY;
 		
 		return middleY;
 	}
 	public Object clone() {
         try {
             return super.clone();
         } catch(CloneNotSupportedException e) {
             return null;
         }
     }
 }
