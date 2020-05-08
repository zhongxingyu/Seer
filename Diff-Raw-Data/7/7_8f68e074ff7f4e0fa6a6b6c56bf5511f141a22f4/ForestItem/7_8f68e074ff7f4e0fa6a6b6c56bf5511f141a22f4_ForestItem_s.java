 package com.example.swp_ucd_2013_eule.data;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 import com.example.swp_ucd_2013_eule.R;
 
 public class ForestItem {
 	private int mX;
 	private int mY;
 	private Bitmap mImage;
 	private int mImageWidth;
 	private int mImageHeight;
 	private String mName;
 	private String mDescription;
 	private int mAmount;
 	private int mPrice;
 	private ForestItemType mType;
 
 	private static ForestItem[] examples;
 
 	public enum ForestItemType {
 		STANDARD, SPECIAL
 	};
 
 	public static ForestItem[] getExamples(Context ctx) {
 		if (examples != null) {
 			return examples;
 		}
 		Resources r = ctx.getResources();
 		Bitmap imgTree = BitmapFactory.decodeResource(r, R.drawable.item_tree);
 		Bitmap imgFir = BitmapFactory.decodeResource(r, R.drawable.item_fir);
 		Bitmap imgFrog = BitmapFactory
 				.decodeResource(r, R.drawable.item_gordan);
 		Bitmap imgBush = BitmapFactory.decodeResource(r, R.drawable.item_bush);
 
 		ForestItem fir1 = new ForestItem(
 				ForestItemType.STANDARD,
 				imgFir,
 				"Fichte",
 				"Dies ist eine Fichte.\nZum Erlangen\nmuss man mindestens\ndas Level 5 erreicht haben.",
 				2);
 
 		ForestItem fir2 = new ForestItem(
 				ForestItemType.STANDARD,
 				imgFir,
 				"Fichte",
 				"Dies ist eine Fichte.\nZum Erlangen\nmuss man mindestens\ndas Level 5 erreicht haben.",
 				2);
 
 		ForestItem tree = new ForestItem(ForestItemType.STANDARD, imgTree,
 				"Laubbaum",
				"Dies ist ein Laubbaum.\nEs ist ein Startgegenstand.", 2);
 
 		ForestItem bush1 = new ForestItem(ForestItemType.STANDARD, imgBush,
 				"Busch", "Dies ist ein Busch.\nEr ist ein Startgegenstand.", 2);
 
 		ForestItem bush2 = new ForestItem(ForestItemType.STANDARD, imgBush,
 				"Busch", "Dies ist ein Busch.\nEr ist ein Startgegenstand.", 2);
 
 		ForestItem gordon = new ForestItem(
 				ForestItemType.SPECIAL,
 				imgFrog,
 				"Gordon der Frosch",
				"SPEZIALGEGENSTAND!\nDieser Gegenstand\nist nicht zu kaufen!\nMan erlangt ihn für\nherrausragendes Fahren!",
				2);
 
 		examples = new ForestItem[] { fir1, fir2, tree, bush1, bush2, gordon };
 		return examples;
 	}
 
 	ForestItem(ForestItemType type, Bitmap bitmap, String name,
 			String description, int amount) {
 		mType = type;
 		setImage(bitmap);
 		setName(name);
 		setDescription(description);
 		setAmount(amount);
 	}
 
 	public int getX() {
 		return mX;
 	}
 
 	public int getY() {
 		return mY;
 	}
 
 	public void setCoordinates(int x, int y) {
 		this.mX = x;
 		this.mY = y;
 	}
 
 	public Bitmap getImage() {
 		return mImage;
 	}
 
 	public void setImage(Bitmap image) {
 		this.mImage = image;
 		this.mImageHeight = image.getHeight();
 		this.mImageWidth = image.getWidth();
 	}
 
 	public String getName() {
 		return mName;
 	}
 
 	public void setName(String name) {
 		this.mName = name;
 	}
 
 	public String getDescription() {
 		return mDescription;
 	}
 
 	public void setDescription(String description) {
 		this.mDescription = description;
 	}
 
 	public int getAmount() {
 		return mAmount;
 	}
 
 	public void setAmount(int amount) {
 		this.mAmount = amount;
 	}
 
 	public int getPrice() {
 		return mPrice;
 	}
 
 	public void setPrice(int price) {
 		this.mPrice = price;
 	}
 
 	public boolean isStandardItem() {
 		return mType == ForestItemType.STANDARD;
 	}
 
 	public boolean isSpecialItem() {
 		return mType == ForestItemType.SPECIAL;
 	}
 
 	public int getImageHeight() {
 		return mImageHeight;
 	}
 
 	public int getImageWidth() {
 		return mImageWidth;
 	}
 
 }
