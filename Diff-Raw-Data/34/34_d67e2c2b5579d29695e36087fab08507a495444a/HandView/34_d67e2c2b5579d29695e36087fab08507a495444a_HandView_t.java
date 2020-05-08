 /**
  * 
  */
 package com.game.pileon;
 
 
 import junit.framework.Assert;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.widget.ImageView;
 
 /**
  * @author breeze4
  * @since 2013-01-03
  *
  */
 public class HandView extends ImageView
 {
 	public Hand mHand; //stores the underlying Hand that this HandView represents
 	private Drawable mCardGraphic;
 	private Context mContext;
 
 
 	/**
 	 * @param context
 	 */
 	public HandView(Context context, Hand hand)
 	{
 		this(context, null, 0, hand);
 		// default constructor, not used
 	}
 
 	/**
 	 * @param context
 	 * @param attrs
 	 */
 	public HandView(Context context, AttributeSet attrs, Hand hand)
 	{
 		this(context, attrs, 0, hand);
 		// default constructor, not used
 	}
 
 	/**
 	 * @param context
 	 * @param attrs
 	 * @param defStyle
 	 */
 	public HandView(Context context, AttributeSet attrs, int defStyle, Hand hand)
 	{
 		super(context, attrs, defStyle);
 		mContext = context;
 
 		setHand(hand);
 		updateGraphic();
 
 		Log.i("PO CreateDeck", "hand LayoutParams width: " + getDrawable().getIntrinsicWidth() +
 				" height: "+ getDrawable().getIntrinsicHeight());
 	}
 
 	public void updateGraphic(){
 		updateGraphic(mHand.mCard.getCardID());
 	}
 
 	public void updateGraphic(String cardID){
 		int handImageResource = getDrawable(mContext, cardID);
 		setImageResource(handImageResource);
 		mCardGraphic = mContext.getResources().getDrawable(handImageResource);
 		invalidate();
 	}
 
 
 	public int getDrawable(Context context, String name)
 	{
 		Assert.assertNotNull(context);
 		Assert.assertNotNull(name);
 
 		return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
 	}
 
 	public void setHand(Hand hand)
 	{
 		mHand = hand;
 	}
 
 	public boolean isPlaceholder()
 	{
		return mHand.mCard.isPlaceholder();
 	}
 
 	public String toString(){
 		return mHand.toString();
 	}
 }
