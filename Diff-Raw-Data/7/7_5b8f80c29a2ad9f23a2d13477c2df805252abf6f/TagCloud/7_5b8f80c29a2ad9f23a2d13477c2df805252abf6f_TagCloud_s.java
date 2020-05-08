 package ca.idrc.tagin.cloud;
 
 /**
  * Komodo Lab: Tagin! Project: 3D Tag Cloud
  * Google Summer of Code 2011
  * @authors Reza Shiftehfar, Sara Khosravinasr and Jorge Silva
  */
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.graphics.Color;
 
 public class TagCloud {
 
 	private static final int DEFAULT_COLOR1 = Color.argb(1, 226, 185, 48);
 	private static final int DEFAULT_COLOR2 = Color.argb(1, 76, 76, 76);
 	
 	private Map<String,Tag> mTags;
 	private int mRadius;
 	private int mTagColor1;
 	private int mTagColor2;
 	private int mTextSizeMin;
 	private int mTextSizeMax;
 	private float sin_mAngleX, cos_mAngleX, sin_mAngleY, cos_mAngleY, sin_mAngleZ, cos_mAngleZ;
 	private float mAngleZ = 0;
     private float mAngleX = 0;
     private float mAngleY = 0;
     private int smallest, largest; // used to find spectrum for tag colors
 	
 	public TagCloud(Map<String,Tag> tags, int radius, int textSizeMin, int textSizeMax) {
 		this(tags, radius, DEFAULT_COLOR1, DEFAULT_COLOR2, textSizeMin, textSizeMax);
 	}
 
 	public TagCloud(Map<String,Tag> tags, int radius, int tagColor1, int tagColor2, 
 						int textSizeMin, int textSizeMax) {
 		mTags = tags;    // Java does the initialization and deep copying
 		mRadius = radius;
 		mTagColor1 = tagColor1;
 		mTagColor2 = tagColor2;
 		mTextSizeMax = textSizeMax;
 		mTextSizeMin = textSizeMin;	
 	}       
 	
 	// create method calculates the correct initial location of each tag
 	public void create() {
 		// calculate and set the location of each Tag
 		positionAll();
 		sineCosine();
 		updateAll();
 		// Now, let's calculate and set the color for each tag:
 		// first loop through all tags to find the smallest and largest populariteies
 		// largest popularity gets tcolor2, smallest gets tcolor1, the rest in between
 		smallest = 9999;
 		largest = 0;
 		for (Tag tag : mTags.values()) {
 			int popularity = tag.getPopularity();
 			largest = Math.max(largest, popularity);
 			smallest = Math.min(smallest, popularity);
 		}
 		//figuring out and assigning the colors/ textsize
 		for (Tag tag : mTags.values()) {
 			float percentage = 1.0f;
 			if (smallest != largest) {
 				percentage = ((float) tag.getPopularity() - smallest) / ((float) largest - smallest);
 			}
 			int tempTextSize = getTextSizeGradient(percentage);
 			tag.setColor(getColorFromGradient(percentage));
 			tag.setTextSize(tempTextSize);
 		}
 	}
 	
 	// updates the transparency/scale of all elements
 	public void update() {
		// if mAngleX and mAngleY under threshold, skip motion calculations for performance
		if (Math.abs(mAngleX) > .1 || Math.abs(mAngleY) > .1 ) {
			sineCosine();
			updateAll();
		}
 	}
 	
 	// if a single tag needed to be added
 	public void add(Tag tag) {
 		int j = tag.getPopularity();
 		float percentage = (smallest == largest) ? 1.0f : ((float) j - smallest) / ((float) largest - smallest);
 		int tempTextSize = getTextSizeGradient(percentage);
 		tag.setColor(getColorFromGradient(percentage));
 		tag.setTextSize(tempTextSize);
 		position(tag);
 		// now add the new tag to the tagCloud
 		mTags.put(tag.getID(), tag);				
 		updateAll();
 	}
 
 	// for a given tag, sets the value of RGB and text size based on other existing tags
 	public void setTagRGBT(Tag tag) {
 		int popularity = tag.getPopularity();
 		float percentage = (smallest == largest) ? 
 								1.0f :
 								((float)popularity-smallest) / ((float)largest-smallest);
 		int tempTextSize = getTextSizeGradient(percentage);
 		tag.setColor(getColorFromGradient(percentage));
 		tag.setTextSize(tempTextSize);
 	}
 
 	private void position(Tag tag) {
 		// when adding a new tag, just place it at some random location
 		// this is in fact why adding too many elements make TagCloud ugly
 		// after many add, do one reset to rearrange all tags
 		double phi = Math.random() * Math.PI;
 		double theta = Math.random() * (2 * Math.PI);
 
 		// coordinate conversion:
 		tag.setX((int) (mRadius * Math.cos(theta) * Math.sin(phi)));
 		tag.setY((int) (mRadius * Math.sin(theta) * Math.sin(phi)));
 		tag.setZ((int) (mRadius * Math.cos(phi)));
 	}
 	
 	private void positionAll() {
 		double phi = 0;
 		double theta = 0;
 		int i = 1;
 		int max = mTags.size();
 		for (Tag tag : mTags.values()) {
 			phi = Math.acos(-1.0 + (2.0 * i - 1.0) / max);
 			theta = Math.sqrt(max * Math.PI) * phi;
 			
 			//coordinate conversion:			
 			tag.setX((int) (mRadius * Math.cos(theta) * Math.sin(phi)));
 			tag.setY((int) (mRadius * Math.sin(theta) * Math.sin(phi)));
 			tag.setZ((int) (mRadius * Math.cos(phi)));
 			i++;
 		}		
 	}	
 	
 	private void updateAll() {
 		// update transparency/scale for all tags:
 		for (Tag tag : mTags.values()) {
 			// There exists two options for this part:
 			// multiply positions by a x-rotation matrix
 			float rx1 = (tag.getX());
 			float ry1 = (tag.getY()) * cos_mAngleX + tag.getZ() * -sin_mAngleX;
 			float rz1 = (tag.getY()) * sin_mAngleX + tag.getZ() * cos_mAngleX;						
 			// multiply new positions by a y-rotation matrix
 			float rx2 = rx1 * cos_mAngleY + rz1 * sin_mAngleY;
 			float ry2 = ry1;
 			float rz2 = rx1 * -sin_mAngleY + rz1 * cos_mAngleY;
 			// multiply new positions by a z-rotation matrix
 			float rx3 = rx2 * cos_mAngleZ + ry2 * -sin_mAngleZ;
 			float ry3 = rx2 * sin_mAngleZ + ry2 * cos_mAngleZ;
 			float rz3 = rz2;
 			// set arrays to new positions
 			tag.setX(rx3);
 			tag.setY(ry3);
 			tag.setZ(rz3);
 
 			// add perspective
 			int diameter = 2 * mRadius;
 			float per = diameter / (diameter + rz3);
 			// let's set position, scale, alpha for the tag;
 			tag.setX2D((int) (rx3 * per));
 			tag.setY2D((int) (ry3 * per));
 			tag.setScale(per);
 			
 			int color = tag.getColor();
 			int a = (int) ((per/2) * 255);
 			int r = Color.red(color);
 			int g = Color.green(color);
 			int b = Color.blue(color);
 			tag.setColor(Color.argb(a, r, g, b));
 		}	
 		depthSort();
 	}	
 	
 	// now let's sort all tags in the tagCloud based on their z coordinate
 	// this way, when they are finally drawn, upper tags will be drawn on top of lower tags
 	private void depthSort() {
 		List<Tag> entries = new ArrayList<Tag>(mTags.values());
 		Collections.sort(entries);
 		
 		mTags = new LinkedHashMap<String, Tag>();
 		for (Tag tag : entries) {
 			mTags.put(tag.getID(), tag);
 		}
 	}
 	
 	private int getColorFromGradient(float perc) {
 		int r = (int) ((perc * Color.red(mTagColor1)) + ((1-perc) * Color.red(mTagColor2)));
 		int g = (int) ((perc * Color.green(mTagColor1)) + ((1-perc) * Color.green(mTagColor2)));
 		int b = (int) ((perc * Color.blue(mTagColor1)) + ((1-perc) * Color.blue(mTagColor2)));
  		return Color.rgb(r, g, b);
 	}
 	
 	private int getTextSizeGradient(float perc) {
 		return (int) (perc * mTextSizeMax + (1-perc) * mTextSizeMin);
 	}
 	
 	private void sineCosine() {
 		double degToRad = Math.PI / 180;
 		sin_mAngleX = (float) Math.sin(mAngleX * degToRad);
 		cos_mAngleX = (float) Math.cos(mAngleX * degToRad);
 		sin_mAngleY = (float) Math.sin(mAngleY * degToRad);
 		cos_mAngleY = (float) Math.cos(mAngleY * degToRad);
 		sin_mAngleZ = (float) Math.sin(mAngleZ * degToRad);
 		cos_mAngleZ = (float) Math.cos(mAngleZ * degToRad);
 	}
 	
 	public int getRadius() {
 		return mRadius;
 	}
 	
 	public void setRadius(int radius) {
 		this.mRadius = radius;
 	}
 	
 	public int getTagColor1() {
 		return mTagColor1;
 	}
 	
 	public void setTagColor1(int tagColor) {
 		this.mTagColor1 = tagColor;
 	}
 	
 	public int getTagColor2() {
 		return mTagColor2;
 	}
 	
 	public void setTagColor2(int tagColor2) {
 		this.mTagColor2 = tagColor2;
 	}
 	
 	public float getAngleX() {
 		return mAngleX;
 	}
 	
 	public void setAngleX(float angleX) {
 		this.mAngleX = angleX;
 	}
 	
 	public float getAngleY() {
 		return mAngleY;
 	}
 	
 	public void setAngleY(float angleY) {
 		this.mAngleY = angleY;
 	}
 	
 	public Map<String,Tag> getTags() {
 		return mTags;
 	}
 }
