 package com.uta.byos;
 
 /*
  * This class provides the "crafting tableaux" so-to-speak for the user to create his own custom card games.
  * All javadoc was written by Matthew Waller
  * @author Matthew Waller
  */
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class GameBuilder extends View {
 
 	private Paint mCanvasPaint;
 	
     private Bitmap mCacheBitmap;
     private Bitmap posBit;
     private Bitmap negBit;
     private Rect mScreenSize = new Rect();
     private Rect cCell = new Rect();
 	private Rect typeBack;
 	private Rect faceUpBack;
 	private Rect sizeBack;
 	private Rect posRect;
 	private Rect negRect;
     private boolean mUseCache = false;
     private boolean itos = false;
 
     private Rect mCardSize = new Rect();
     private int cardXCap;
     private int cardYCap;
     private int alloc = 1;
     private int initX;
     private int initY;
     private int mCardCap;
     private Paint textP = new Paint();
     private String sType;
     private String rP = "D";
     private Paint typeP = new Paint();
     private Paint whitewash = new Paint();
     private int deckSize;
 
     protected ArrayList<Placeholder> places = new ArrayList<Placeholder>();
     private Placeholder mActiveStack;
 	protected Placeholder menuDummyS;
 	protected Placeholder menuDummyT;
 
     
     public boolean buildInProgress = false;
     
     /*
      * Standard constructors for view objects in Android
      * @see android.view.View
      */
 	
     public GameBuilder(Context context) {
     	super(context);
 
     	mCanvasPaint = new Paint();
     	mCanvasPaint.setColor(0xFF228B22); // Green background
 		whitewash.setColor(0xFFFFFFFF);
     	mCanvasPaint.setAntiAlias(false);
     	mCanvasPaint.setFilterBitmap(false);
         textP.setColor(0xFF000000);
         typeP.setColor(0xFF000000);
     	setClickable(true);
     	
     	
     }
     
     public GameBuilder(Context context, AttributeSet attrs){
         super(context, attrs);
 
         mCanvasPaint = new Paint();
         mCanvasPaint.setColor(0xFF228B22); // Green background
 		whitewash.setColor(0xFFFFFFFF);
         mCanvasPaint.setAntiAlias(false);
         mCanvasPaint.setFilterBitmap(false);
         textP.setColor(0xFF000000);
         typeP.setColor(0xFF000000);
     	setClickable(true);
     }
     
     public GameBuilder(Context context, AttributeSet attrs, int defStyle){
         super(context, attrs, defStyle);
 
         mCanvasPaint = new Paint();
         mCanvasPaint.setColor(0xFF228B22); // Green background
 		whitewash.setColor(0xFFFFFFFF);
         mCanvasPaint.setAntiAlias(false);
         mCanvasPaint.setFilterBitmap(false);
         textP.setColor(0xFF000000);
         typeP.setColor(0xFF000000);
     	setClickable(true);
     	
     }
     
     /*
      * (non-Javadoc)
      * Creates a new table if it is newly initialized otherwise
      * preserves and redraws the current elements for user
      * friendliness
      * @see android.view.View#onSizeChanged(int, int, int, int)
      */
     
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh){
 		mScreenSize.set(0, 0, w, h);
 
 		// Calculate card and decks sizes and positions
 		int cw = w / 11;
 		mCardSize.set(0, 0, cw, (int) (cw * 1.5));
 		cCell.set(0, 0, cw, h/((int) (cw*1.5)));
 		Log.v("card size", mCardSize.toString());
 
 		int freeSize = w - cw * 7;
 		mCardCap = freeSize / (6 + 4 * 2);
 
 		int cy = (int) (mScreenSize.height() * 0.35);
 		String allocStr = String.valueOf(alloc);
 		sType = "R";
 		places.add(new Placeholder((w-mCardSize.width())/2, (h-mCardSize.height())/2, deckSize*52, mCardSize.height(), mCardSize.width(), Deck.DeckType.EWaste1, getResources()));
 		buildInProgress = true;
 		typeBack = new Rect(mScreenSize.width()/4 - 16, mScreenSize.height()*7/8 - 24, 
 				mScreenSize.width()/4 + 32, mScreenSize.height()*7/8 + 8);
 		faceUpBack = new Rect(mScreenSize.width()/2 - 16, mScreenSize.height()*7/8 - 24, 
 				mScreenSize.width()/2 + 32, mScreenSize.height()*7/8 + 8);
 		sizeBack = new Rect(mScreenSize.width()*3/4 - 16, mScreenSize.height()*7/8 - 24, 
 				mScreenSize.width()*3/4 + 32, mScreenSize.height()*7/8 + 8);
 		posRect = new Rect(mScreenSize.width()*3/4 - 16, mScreenSize.height()*7/8 - 56, 
 				mScreenSize.width()*3/4 + 32, mScreenSize.height()*7/8 - 24);
 		negRect = new Rect(mScreenSize.width()*3/4 - 16, mScreenSize.height()*7/8 + 8, 
 				mScreenSize.width()*3/4 + 32, mScreenSize.height()*7/8 + 40);
 		Bitmap tmp = BitmapFactory.decodeResource(getResources(), R.raw.positive);
 		posBit = Bitmap.createScaledBitmap(tmp, posRect.width(), posRect.height(), true);
 		tmp  = BitmapFactory.decodeResource(getResources(), R.raw.negative);
 		negBit = Bitmap.createScaledBitmap(tmp, negRect.width(), negRect.height(), true);
     }
     
     /*
      * (non-Javadoc)
      * Based on Tero's original algorithm
      * @see android.view.View#onDraw(android.graphics.Canvas)
      * @see TableauView#onDraw
      */
     
     @Override
     public void onDraw(Canvas canvas) {
 
             // Cache?
             if (mUseCache) {
                     // Yes
                     canvas.drawBitmap(mCacheBitmap, 0, 0, null);
             } else {
                     // No
                     mCanvasPaint.setStyle(Style.FILL);
             		whitewash.setStyle(Style.FILL);
                     canvas.drawRect(mScreenSize, mCanvasPaint);
                     // Draw decks
                     for(Placeholder card : places){
                     	if(card instanceof CustomReserve)
                     		((CustomReserve) card).doDraw(canvas);
                     	else
                     		card.doDraw(canvas);}
                     canvas.drawRect(typeBack, whitewash);
                     canvas.drawRect(faceUpBack, whitewash);
                     canvas.drawRect(sizeBack, whitewash);
                     canvas.drawText(String.valueOf(alloc), mScreenSize.width()*3/4, mScreenSize.height()*7/8, textP);
                     canvas.drawText(sType, mScreenSize.width()/4, mScreenSize.height()*7/8, textP);
                     canvas.drawText(rP, mScreenSize.width()/2, mScreenSize.height()*7/8, textP);
                     canvas.drawBitmap(posBit, posRect.left, posRect.top, null);
                     canvas.drawBitmap(negBit, negRect.left, negRect.top, null);
                     
             }
             if (mActiveStack != null) {
                 mActiveStack.doDraw(canvas);}
            
 
     }
     
     public void setDeckSize(int in){
     	if(in == deckSize)
     		return;
     	invalidate();
     	Placeholder main = places.get(0);
     	int post = main.getSize() + (in-deckSize)*52;
     	deckSize = in;
     	if(post > 0){
     		main.setSize(post);
     		return;}
     	int current = main.getSize();
     	for(int i = 1; i < places.size(); i++)
     		if(current < deckSize*52)
     			current += places.get(i).getSize();
     		else{
     			places.remove(i); i--;}
     	main.incSize(deckSize*52 - current);
     }
     
     /*
      * Based on Tero's original algorithm
      * @see TableauView enableCache
      */
     private void enableCache(boolean enabled) {
     	if (enabled && !mUseCache) { //<Team 4 comment> Panaanen had this written as enabled && mUseCache != enabled
     		setDrawingCacheEnabled(true);
     		//buildDrawingCache();
     		mCacheBitmap = Bitmap.createBitmap(getDrawingCache());
     	} else if (!enabled && mUseCache) {
     		setDrawingCacheEnabled(false);
     		mCacheBitmap = null;
     	}
     	mUseCache = enabled;
     }
     
     /*
      * (non-Javadoc)
      * Handles interactivity within the view
      * @see android.view.View#onTouchEvent(android.view.MotionEvent)
      * @see TableauView#onTouchEvent
      */
 	
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	int action = event.getAction();
     	if (action == MotionEvent.ACTION_DOWN) {
     		initX = (int) event.getX();
     		initY = (int) event.getY();
     		mActiveStack = getDeckUnderTouch(initX, initY);
     		enableCache(true);
     		invalidate();
     		return true;
     	} else if (action == MotionEvent.ACTION_MOVE) {
     		int x = (int) event.getX();
     		int y = (int) event.getY();
     		if (mActiveStack != null && Math.abs(y - mScreenSize.height()*7/8) > mCardSize.height()){
     			if(mActiveStack instanceof CustomReserve)
     				((CustomReserve) mActiveStack).setPos(getXCell(x)-mCardSize.width()/2, getYCell(y)-mCardSize.height()/2);
     			else
     				mActiveStack.setPos(getXCell(x)-mCardSize.width()/2, getYCell(y)-mCardSize.height()/2);}
     		invalidate();
     		return true;
 
     	} else if (action == MotionEvent.ACTION_UP) {
     		int x = (int) event.getX();
     		int y = (int) event.getY();  
     		enableCache(false);
     		if(mActiveStack == null){
     			if(posRect.contains(x, y) || negRect.contains(x, y)){
    				if(posRect.contains(x, y) && alloc < deckSize*52)
     					alloc++;
    				else if(negRect.contains(x, y) && alloc > 0)
     					alloc--;
     			}else if(typeBack.contains(x, y)){
     				switch(sType.charAt(0)){
     				case 'R':
     					sType = "W";
     					break;
     				case 'W':
     					sType = "F";
     					break;
     				case 'F':
     					sType = "R";
     					break;
     				}
     			}else if(faceUpBack.contains(x, y)){
     				switch(rP.charAt(0)){
     				case 'D':
     					rP = "U"; break;
     				case 'U':
     					rP = "D"; break;
     				}
     			}else if(Math.abs(y - mScreenSize.height()*7/8) > mCardSize.height())
     				try{
     					addStack(alloc, sType, getXCell(x), getYCell(y));
     				}catch (ArithmeticException caught){}				
     		}else if(Math.abs(x - initX) < 32  && Math.abs(y - initY) < 32 && places.indexOf(mActiveStack) != 0){
     			if(itos)
     				addCardsOrRemove(initX, initY);
     			else{
     				Placeholder index = places.get(0);
         			index.setSize(index.getSize() + mActiveStack.getSize());
         			places.remove(mActiveStack);}}
     		//handleCardMove(x, y);
     		enableCache(false);
     		mActiveStack = null;
     		invalidate();
     		return true;
     	}
     	mActiveStack = null;
     	return false;
 
     }
     
 
     
     /*
      * Adds a stack to the crafting table
      * @param s		Size of the deck to be added
      * @param type	Any string denoting the type of deck to be added (Reserve/Waste/Foundation/Stock)
      * @param x		Target x-coordinate
      * @param y		Target y-coordinate
      * @throws ArithmeticException	If there are not enough cards in the main waste or if
      * 								there was in error with the argument to param type
      * @see Deck#DeckType 
      */
     
     
     
 
 	public void addStack(int s, String type, int x, int y) throws ArithmeticException{
     	Placeholder main;
     	main = places.get(0);
     	Deck.DeckType set;
     	if(s > main.getSize())
     		throw new ArithmeticException("Not enough cards!");
     	switch(type.charAt(0)){
     	case 'S':
     		set = Deck.DeckType.EWaste1;
     		break;
     	case 'W':
     		s = 0;
     		set = Deck.DeckType.EWaste2;
     		break;
     	case 'R':
         	main.setSize(main.getSize() - s);
     		places.add(new CustomReserve(x-mCardSize.width()/2, y-mCardSize.height()/2, s, mCardSize.height(), mCardSize.width(),
     				getResources(), rP.equals("U")));
     		return;
     	case 'F':
     		s = 0;
     		set = Deck.DeckType.ETarget;
     		break;
     	default:
     		throw new ArithmeticException("Not a valid deck type");
     	}
     	main.setSize(main.getSize() - s);
     	places.add(new Placeholder(x-mCardSize.width()/2, y-mCardSize.height()/2, s, mCardSize.height(), mCardSize.width(), set,
     			getResources()));
     }
 	
 	private void addCardsOrRemove(int x, int y) {
     	Rect bounds;
     	int dec = 0;
     	boolean iUT = mActiveStack.isUnderTouch(x, y);
     	Placeholder index = places.get(0);
     	if(itos && iUT){
     		bounds = mActiveStack.mRect;
     		if(y >= bounds.top + bounds.height()/2)
     			dec = 1;
     		else
     			dec = 2;
     	}else if(itos)
     		dec = 1;
     	else if(iUT)
     		dec = 2;
     	switch(dec){
     	case 1:
     		if(alloc < index.getSize()){
     			index.setSize(index.getSize() - alloc);
     			switch(rP.charAt(0)){
     			case 'U':
     				((CustomReserve) mActiveStack).addCards(alloc, true); break;
     			case 'D':
     				((CustomReserve) mActiveStack).addCards(alloc, false); break;
     			} break;}
     	case 2:
     		index.setSize(index.getSize() + mActiveStack.getSize());
     		places.remove(mActiveStack);
     	}
 	}
     
     /*
      * Finds the placeholder selected the user is "touching"
      * @param  x			X-coordinate of the click
      * @param  y			Y-coordinate of the click
      * @return Placeholder
      */
 
 
 	private Placeholder getDeckUnderTouch(int x, int y) {
 		for(Placeholder stack : places){
 			if(stack instanceof CustomReserve){
 				if(((CustomReserve) stack).isTopOfStack(x, y)){
 					itos = true;
 					return stack;
 				}else if(stack.isUnderTouch(x, y)){
 					itos = false;
 					return stack;}
 			}else if(stack.isUnderTouch(x, y)){
 				itos = false;
 				return stack;}
 		}
 		return null;
     }
     
     /*
      * Finds and removes the placeholder selected by the user
      * @param x	X-coordinate of the tap
      * @param y	Y-coordinate of the tap
      */
     
     private int getXCell(int x){
     	int adj = x/mCardSize.width();
     	return adj * mCardSize.width() + mCardSize.width()/2;
     }
     
     private int getYCell(int y){
     	int adj = y/mCardSize.height();
     	return adj*mCardSize.height() + mCardSize.height()/2;
     }
     
     private void removeUnderTouch(int x, int y){
     	Placeholder index;
     	int dec;
     	for(int i = 0; i < places.size(); i++){
     		index = places.get(i);
     		dec = index.getSize();
     		if(index.isUnderTouch(x, y) && i != 0){
     			index = places.get(0);
     			index.setSize(index.getSize() + dec);
     			places.set(0, index);
     			places.remove(i);
     			break;
     		}}
     }
     
     @Override
     public String toString(){
     	String data = String.valueOf(deckSize) + ":";
     	for(Placeholder item : places){
     		Deck.DeckType type = item.type;
     		if(type == Deck.DeckType.ESource)
     			data += "R";
     		else if(type == Deck.DeckType.ETarget)
     			data += "F";
     		else if(type == Deck.DeckType.EWaste1)
     			data += "S";
     		else
     			data += "W";
     		data += ("," + item.getX() + "," + item.getY() + ",");
     		if(type != Deck.DeckType.ESource)
     			data += (item.getSize());
     		else
     			data += ((CustomReserve) item);
     		data += ";";
     	}
     	return data;
     }
 
 	public void initDeckSize(int in) {
     	deckSize = in;}
 
     
 }
