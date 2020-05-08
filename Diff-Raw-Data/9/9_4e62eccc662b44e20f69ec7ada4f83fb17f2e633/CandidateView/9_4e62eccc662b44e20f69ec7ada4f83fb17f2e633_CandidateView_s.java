 /*
  * Copyright (C) 2008-2009 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.easy.ChIME;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CandidateView extends View {
 
     private static final int OUT_OF_BOUNDS = -1;
 
     private ChIME mService;
     private List<String> mSuggestions;
     private int mSelectedIndex;
     private int mTouchX = OUT_OF_BOUNDS;
     private int mTouchY = OUT_OF_BOUNDS;
     private Drawable mSelectionHighlight;
     private boolean mTypedWordValid;
     private ArrayList<Page> mPages = new ArrayList<Page>();
     private int mHeightOfRow = 0;
     private int mCurrPage = 0;
     
     private Rect mBgPadding;
 
     private static final int X_GAP = 10;
     
     private int mColorNormal;
     private int mColorRecommended;
     private int mColorOther;
     private int mVerticalPadding;
     private Paint mPaint;
 
 	private int mLinesPerPage = 1;
 
 	private int mRowOffset;
 
     /**
      * Construct a CandidateView for showing suggested words for completion.
      * @param context
      * @param attrs
      */
     public CandidateView(Context context) {
         super(context);
         mSelectionHighlight = context.getResources().getDrawable(
                 android.R.drawable.list_selector_background);
         mSelectionHighlight.setState(new int[] {
                 android.R.attr.state_enabled,
                 android.R.attr.state_focused,
                 android.R.attr.state_window_focused,
                 android.R.attr.state_pressed
         });
 
         Resources r = context.getResources();
 
         setBackgroundColor(r.getColor(R.color.candidate_background));
         
         mColorNormal = r.getColor(R.color.candidate_normal);
         mColorRecommended = r.getColor(R.color.candidate_recommended);
         mColorOther = r.getColor(R.color.candidate_other);
         mVerticalPadding = r.getDimensionPixelSize(R.dimen.candidate_vertical_padding);
         
         mPaint = new Paint();
         mPaint.setColor(mColorNormal);
         mPaint.setAntiAlias(true);
         mPaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_font_height));
         mPaint.setStrokeWidth(0);
 
         setHorizontalFadingEdgeEnabled(true);
         setWillNotDraw(false);
     }
     
     /**
      * A connection back to the service to communicate with the text field
      * @param listener
      */
     public void setService(ChIME listener) {
         mService = listener;
     }
     
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         int measuredWidth = resolveSize(20, widthMeasureSpec);
         
         // Get the desired height of the icon menu view (last row of items does
         // not have a divider below)
         Rect padding = new Rect();
         mSelectionHighlight.getPadding(padding);
         int paddingSize = mVerticalPadding + padding.top + padding.bottom;
         mHeightOfRow = ((int)mPaint.getTextSize()) + paddingSize;
         this.mRowOffset = (int) ((paddingSize / 2) - mPaint.ascent());
         // Maximum possible width and desired height
         setMeasuredDimension(measuredWidth,
                 resolveSize(mHeightOfRow * mLinesPerPage, heightMeasureSpec));
     }
 
     /**
      * If the canvas is null, then only touch calculations are performed to pick the target
      * candidate.
      */
     @Override
     protected void onDraw(Canvas canvas) {
         if (canvas == null) {
         	return;
         }
         super.onDraw(canvas);
         if (mSuggestions == null || mSuggestions.isEmpty()) return;
         if (mBgPadding == null) {
             mBgPadding = new Rect(0, 0, 0, 0);
             if (getBackground() != null) {
                 getBackground().getPadding(mBgPadding);
             }
         }
         Page pg = mPages.get(this.mCurrPage);
         ArrayList<RowInfo> rows = pg.getRows();
         int rowCount = rows.size();
         for (int i = 0; i < rowCount; ++i) {
         	RowInfo ri = rows.get(i);
         	boolean hitRow = ri.hits(this.mTouchY);
         	int y = mRowOffset + ri.getTop();
         	ArrayList<WordInfo> words = ri.getWords();
         	int wordCount = words.size();
         	int height = ri.getHeight();
         	for (int j = 0; j < wordCount; ++j) {
         		WordInfo wi = words.get(j);
         		int x = wi.getLeft();
         		int wordWidth = wi.getWidth();
         		mPaint.setColor(mColorNormal);
         		if (hitRow && wi.containPos(mTouchX)) {
         			canvas.translate(x, 0);
                     mSelectionHighlight.setBounds(0, ri.getTop() + mBgPadding.top, wordWidth, height);
                     mSelectionHighlight.draw(canvas);
                     canvas.translate(-x, 0);
                     mSelectedIndex = wi.getIndex();
         		}
         		if (i == 0 && j == 0 && mTypedWordValid) {
         			mPaint.setFakeBoldText(true);
         			mPaint.setColor(mColorRecommended);
                 } else {
                 	mPaint.setColor(mColorOther);
                 }
                 canvas.drawText(mSuggestions.get(wi.getIndex()), x + X_GAP, y, mPaint);
                 mPaint.setColor(mColorOther); 
                 mPaint.setFakeBoldText(false);
         	}
         }
     
     }
     
     public void setSuggestions(List<String> suggestions, boolean completions,
             boolean typedWordValid) {
         restoreStatus();
         mSuggestions = suggestions;
         if (suggestions != null) {
             this.preparePages();
         } else {
         	this.mPages.clear();
         }
         mTypedWordValid = typedWordValid;
         requestLayout();
         invalidate();
     }
 
     public void restoreStatus() {
         mTouchX = OUT_OF_BOUNDS;
         mTouchY = OUT_OF_BOUNDS;
         mSelectedIndex = -1;
         this.mCurrPage = 0;
         this.mSuggestions = null;
         invalidate();
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent me) {
         int action = me.getAction();
         int x = (int) me.getX();
         int y = (int) me.getY();
         switch (action) {
         case MotionEvent.ACTION_DOWN:
             mTouchX = x;
             mTouchY = y;
             invalidate();
             break;
         case MotionEvent.ACTION_UP:
             if (!doMoveAction (me)) {
                 if (mSelectedIndex >= 0) {
                     mService.pickSuggestionManually(mSelectedIndex);
                 }
             }
             mSelectedIndex = -1;
             removeHighlight();
             requestLayout();
             break;
         }
         return true;
     }
     
 	private boolean doMoveAction(MotionEvent me) {
 		int x = (int)me.getX();
 		int y = (int)me.getY();
 		int deltX = x - mTouchX;
 		int deltY = y - mTouchY;
		if (Math.abs(deltY) > Math.abs(deltX)) {
 			expandCandidationView (deltY);
		}  else if (deltX != 0) {
 			turnPages (deltX);
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	private void turnPages(int deltX) {
		if (deltX < 0 && this.mCurrPage < mPages.size()) {
 			this.mCurrPage += 1;
 			this.invalidate();
 		} else if (deltX > 0 && this.mCurrPage > 0) {
 			this.mCurrPage -= 1;
 			this.invalidate();
 		}
 	}
 
 	private void expandCandidationView(int deltY) {
 		if (deltY > 0 && this.mLinesPerPage > 1) {
 			this.mLinesPerPage = 1;
 			this.preparePages();
 			this.requestLayout();
 			this.invalidate();
 		} else if (deltY < 0 && mLinesPerPage < 3) {
 			this.mLinesPerPage = 3;
 			this.preparePages();
 			this.requestLayout();
 			this.invalidate();
 		}
 	}
 
 	/**
      * For flick through from keyboard, call this method with the x coordinate of the flick 
      * gesture.
      * @param x
      */
     public void takeSuggestionAt(float x) {
     	Log.v("ChIME", "Candidate.takeSuggestionAt " + x);
         mTouchX = (int) x;
         // To detect candidate
         onDraw(null);
         if (mSelectedIndex >= 0) {
             mService.pickSuggestionManually(mSelectedIndex);
         }
         invalidate();
     }
 
     private void removeHighlight() {
         mTouchX = OUT_OF_BOUNDS;
         mTouchY = OUT_OF_BOUNDS;
         invalidate();
     }
     
     private int linesPerPage () {
     	return this.mLinesPerPage;
     }
     
     private void preparePages () {
     	if (mSuggestions == null || mSuggestions.isEmpty()) {
     		return;
     	}
     	this.mPages.clear(); 
     	this.mCurrPage = 0;
     	int count = this.mSuggestions.size();
     	Page pg = new Page();
     	mPages.add(pg);
     	RowInfo ri = new RowInfo (0, mHeightOfRow);
     	pg.addRow(ri);
     	
     	int width = (int) mPaint.measureText(mSuggestions.get(0))  + 2 * X_GAP;
     	WordInfo firstWord = new WordInfo (0, 0, width);
     	ri.addWordInfo(firstWord);
     	
     	final int firstWordWidth = width;
     	int left = width;
     	for (int i = 1, row = 0; i < count; ++i) {
     		width = (int) mPaint.measureText(mSuggestions.get(i))  + 2 * X_GAP;
     		// exceed the width, so a new row should be created
     		if (left + width > this.getWidth()) {
     			row += 1;
     			// exceed the page, so a new page will be created
     			if (this.linesPerPage() == row) {
     				pg = new Page();
     				mPages.add(pg);
     				row = 0;
     			}
     			// create a row and add it to this page
     			ri = new RowInfo (row * mHeightOfRow, mHeightOfRow);
     			pg.addRow(ri);
     			
     			if (0 == row) {
     				ri.addWordInfo(firstWord);
     				left = firstWordWidth;
     			} else {
     				left = 0;
     			}
     		}
     		WordInfo wi = new WordInfo (i, left, width);
     		left += width;
 			ri.addWordInfo(wi);
     	}
     }
 
 	class WordInfo {
     	private int index = -1;
     	private int leftx = -1;
     	private int rightx = -1;
     	public WordInfo (int idx, int left, int width) {
     		index = idx;
     		leftx = left;
     		this.rightx = leftx + width;
     	}
     	public boolean containPos (int x) {
     		return x >= leftx && x <= rightx;
     	}
     	public int getIndex () {
     		return index;
     	}
     	
     	public int getLeft () {
     		return leftx;
     	}
     	
     	public int getWidth () {
     		return rightx - leftx;
     	}
     }
     
     class RowInfo {
     	private ArrayList<WordInfo> words;
     	private int top = -1;
     	private int bottom = -1;
     	
     	public RowInfo (int top, int height) {
     		this.top = top;
     		this.bottom = top + height;
     		words = new ArrayList<WordInfo>();
     	}
     	
     	public int getHeight() {
 			return bottom - top;
 		}
 
     	public int getTop () {
     		return top;
     	}
     	
 		public boolean hits (int y) {
     		return y <= bottom && y >= top;
     	}
     	
     	public WordInfo hitsWord (int x) {
     		int cnt = words.size();
     		for (int i = 0; i < cnt; ++i) {
     			if (words.get(i).containPos(x)) {
     				return words.get(i);
     			}
     		}
     		return null;
     	}
     	
     	public ArrayList<WordInfo> getWords () {
     		return words;
     	}
     	
     	public void addWordInfo (WordInfo wi) {
     		words.add(wi);
     	}
     }
     
     class Page {
     	private ArrayList<RowInfo> rows;
     	
     	public Page () {
     		rows = new ArrayList<RowInfo>();
     	}
     	
     	public ArrayList<RowInfo> getRows () {
     		return rows;
     	}
     	
     	public void addRow (RowInfo ri) {
     		rows.add(ri);
     	}
     }
 }
