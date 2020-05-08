 /*
 Copyright 2013 Cadeli
 Contact the authors at cadeli.drummachine@yahoo.com
 See updates at http://github.com/cadeli/CdlUI
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  */
 
 package com.cadeli.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class CdlView extends View implements OnGestureListener {
 	private static final String TAG = "CdlView";
 
 	public static final int CDL_LAYOUT_GRID = 0;
 	public static final int CDL_LAYOUT_FLOW = 1;
 	public static final int CDL_LAYOUT_ABSOLUTE = 2;
 	protected static final long FLASH_DURATION = 200;
 
 	protected List<CdlBaseButton> cdlBaseButtons = new ArrayList<CdlBaseButton>();
 	private int cdlLayout = CDL_LAYOUT_GRID; // default val
 	private int grid_nbCols = 3; // defaultval
 	private boolean sized;
 	private int padding = 2; // defaultval
 	private int scrollBarHeight = 8;
 	private int startXScroll;
 	private int w_btn;
 	private static Rect urect = new Rect();
 
 	private GestureDetector gestureDetector;
 	private boolean flashing = false;
 	private int timerCountFlash;
 	private Handler handlerFlash = new Handler();
 	private CdlBaseButton flashingBtn;
 	private Runnable runnableFlash = new Runnable() {
 
 		public void run() {
 			timerCountFlash--;
 			if (timerCountFlash <= 0) {
 				flashing = false;
 				flashingBtn.setFlashing(false);
 				invalidate(flashingBtn.getRect());
 			} else {
 				handlerFlash.postDelayed(this, FLASH_DURATION);
 			}
 		}
 	};
 
 	private int w;
 	private int h;
 
 	private int sizeInWCase;
 
 	public CdlView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context);
 	}
 
 	public CdlView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 	}
 
 	public CdlView(Context context) {
 		super(context);
 		init(context);
 	}
 
 	private void init(Context context) {
 		gestureDetector = new GestureDetector(context, this);
 		if (CdlPalette.getLastColorIndex() <= 0) {
 			CdlPalette.createDefaultColors();
 		}
 	}
 
 	public void draw(Canvas canvas) {
 		super.draw(canvas);
 		if (sized == false && cdlLayout != CDL_LAYOUT_ABSOLUTE) {
 			size();
 		}
 		if (cdlLayout == CDL_LAYOUT_GRID) {
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				cdlBaseButton.draw(canvas);
 			}
 		}
 		if (cdlLayout == CDL_LAYOUT_FLOW) {
 			sizeInWCase = computeSizeInWCase();
 			int jPosBtn = 0;
 			for (int idBtn = 0; idBtn < cdlBaseButtons.size(); idBtn++) {
 				if ((jPosBtn * w_btn - startXScroll) < w) {
 					drawBtn(idBtn, jPosBtn, canvas);
 				}
 				jPosBtn += cdlBaseButtons.get(idBtn).getGrid_width();
 			}
 			drawScrollBar(canvas, sizeInWCase);
 		}
 		urect.set(0, getTop(), padding, getBottom());
 		canvas.drawRect(urect, CdlPalette.getBlackPaint()); // TODO this.getBackgroundPaint()
 		urect.set(getWidth() - padding, getTop(), getWidth(), getBottom());
 		canvas.drawRect(urect, CdlPalette.getBlackPaint());
 	}
 
 	private int computeSizeInWCase() {
 		int jPosBtn = 0;
 		for (int idBtn = 0; idBtn < cdlBaseButtons.size(); idBtn++) {
 			jPosBtn += cdlBaseButtons.get(idBtn).getGrid_width();
 		}
 		return jPosBtn * w_btn;
 	}
 
 	private void drawBtn(int idBtn, int jPosBtn, Canvas canvas) {
 		if (idBtn < 0 || idBtn >= cdlBaseButtons.size())
 			return;
 		CdlUtils.cdlLog(TAG, "drawBtn id=" + idBtn + " jPos=" + jPosBtn);
 		CdlBaseButton cdlBaseButton = cdlBaseButtons.get(idBtn);
 		int sLeft = (int) (jPosBtn * w_btn - startXScroll);
 		cdlBaseButton.setSize(sLeft + padding, padding, w_btn * cdlBaseButton.getGrid_width() - padding, getHeight() - scrollBarHeight - padding);
 		cdlBaseButton.draw(canvas);
 	}
 
 	private void drawScrollBar(Canvas canvas, int sizeInWCase) {
 		if (cdlBaseButtons.size() == 0)
 			return;
 		if (sizeInWCase < w)
 			return;
 		int padding = 4;
 		int sLeft = getLeft() + padding;
 		int sRight = getRight() - padding;
 		urect.set(sLeft, getHeight() - scrollBarHeight, sRight, getHeight());
 		canvas.drawRect(urect, CdlPalette.getBlackPaint());
 		int lgr = (int) ((w * w) / sizeInWCase);
 		sLeft = (int) (startXScroll * w / sizeInWCase) + getLeft() + padding;
 		sRight = sLeft + lgr - padding;
 		if (sRight > getRight() - padding) {
 			sRight = getRight() - padding;
 		}
 		urect.set(sLeft - getLeft(), getHeight() - scrollBarHeight, sRight - getLeft(), getHeight());
 		canvas.drawRect(urect, CdlPalette.getHilightPaint());
 	}
 
 	private void size() {
 		w = getWidth();
 		h = getHeight();
 		w_btn = (w - 2 * padding) / grid_nbCols;
 		if (cdlLayout == CDL_LAYOUT_GRID) {
 			size_gridMode(w, h);
 		}
 		if (cdlLayout == CDL_LAYOUT_FLOW) {
 			size_flawMode(w, h);
 		}
 		sized = true;
 	}
 
 	private void size_flawMode(int w, int h) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void size_gridMode(int w, int h) {
 		int nbBtn = cdlBaseButtons.size();
 		if (nbBtn == 0)
 			return;
 		if (grid_nbCols <= 0)
 			grid_nbCols = 1;
 		int nbRows = (nbBtn + 1) / grid_nbCols;
 		int realNbRow = nbRows;
 		int maxGHforRow = 1;
 		int h_btn = h;
 		if (nbRows != 0) {
 			h_btn = h / nbRows;
 
 			if (h % nbRows != 0) {
 				h_btn++;
 			}
 		}
 		int col = 0;
 		int row = 0;
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			cdlBaseButton.setPadding(padding);
 			cdlBaseButton.setSize(w_btn * col, h_btn * row, w_btn * cdlBaseButton.grid_width, h_btn * cdlBaseButton.grid_height);
 			col += cdlBaseButton.grid_width;
 			if (maxGHforRow < cdlBaseButton.grid_height) {
 				maxGHforRow = cdlBaseButton.grid_height;
 			}
 			if (cdlLayout == CDL_LAYOUT_GRID) {
 				if (col >= grid_nbCols) {
 					col = 0;
 					row += maxGHforRow;
 					if (row > realNbRow) {
 						realNbRow = row;
 					}
 					maxGHforRow = 1;
 				}
 			}
 		}
 		if (realNbRow != nbRows) {
 			maxGHforRow = 1;
 			h_btn = h / realNbRow;
 			if (h % nbRows != 0) {
 				h_btn++;
 			}
 			row = 0;
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				cdlBaseButton.setSize(w_btn * col, h_btn * row, w_btn * cdlBaseButton.grid_width, h_btn * cdlBaseButton.grid_height);
 				col += cdlBaseButton.grid_width;
 				if (maxGHforRow < cdlBaseButton.grid_height) {
 					maxGHforRow = cdlBaseButton.grid_height;
 				}
 				if (cdlLayout == CDL_LAYOUT_GRID) {
 					if (col >= grid_nbCols) {
 						col = 0;
 						row += maxGHforRow;
 						maxGHforRow = 1;
 					}
 				}
 			}
 		}
 	}
 
 	// just for flaw mode
 	protected int getIdBtnFromX(float x) {
 		int id = 0;
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			if (cdlBaseButton.getLeft() < x && cdlBaseButton.getRight() > x) {
 				return id;
 			}
 			id++;
 		}
 		return id;
 	}
 
 	public void addCdlBaseButton(CdlBaseButton cdlBaseButton) {
 		int color = cdlBaseButtons.size() % (CdlPalette.getLastColorIndex());// avoid first color index
 		cdlBaseButton.setBackgroundColor(color + 1);
 		cdlBaseButtons.add(cdlBaseButton);
 	}
 
 	public void setButtonsEnabled(boolean b) {
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			cdlBaseButton.setEnabled(b);
 		}
 	}
 
 	public boolean onTouchEvent(MotionEvent event) {
 		return gestureDetector.onTouchEvent(event);
 	}
 
 	@Override
 	public boolean onDown(MotionEvent arg0) {
 		return true;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent e) {
 		if (cdlLayout == CDL_LAYOUT_ABSOLUTE || cdlLayout == CDL_LAYOUT_GRID) {
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				if (cdlBaseButton.isXYInControl(e.getX(), e.getY())) {
 					longPressOnCdlBaseButton(cdlBaseButton, e);
 				}
 			}
 			return;
 		}
 		if (cdlLayout == CDL_LAYOUT_FLOW) {
 			int idBtn = getIdBtnFromX(e.getX());
 			if (idBtn < 0 || idBtn >= cdlBaseButtons.size())
 				return;
 			CdlBaseButton cdlBaseButton = cdlBaseButtons.get(idBtn);
 			longPressOnCdlBaseButton(cdlBaseButton, e);
 			return;
 		}
 		return;
 	}
 
 	private void longPressOnCdlBaseButton(CdlBaseButton cdlBaseButton, MotionEvent e) {
 		if (cdlBaseButton.isVisible() && cdlBaseButton.isEnabled()) {
 			if (cdlBaseButton.isFlashCapable()) {
 				timerCountFlash = 2;
 				flashingBtn = cdlBaseButton;
 				runnableFlash.run();
 				cdlBaseButton.setFlashing(true);
 				invalidate(cdlBaseButton.getRect());
 			}
 			cdlBaseButton.longPress(e);
 		}
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
 		CdlUtils.cdlLog(TAG, "onScroll dx:" + distanceX);
 		if (cdlLayout == CDL_LAYOUT_FLOW) {
 			int d = (int) distanceY;
 			if (Math.abs((int) distanceX) > Math.abs((int) distanceY)) {
 				d = (int) distanceX;
 			}
 			startXScroll += d;
 			if (startXScroll < 0 - padding) {
 				startXScroll = -padding;
 			}
 			if (startXScroll > (sizeInWCase - w) + 2 * padding) {
 				startXScroll = (sizeInWCase - w) + 2 * padding;
 			}
 			invalidate();
 		}
 		if (cdlLayout == CDL_LAYOUT_ABSOLUTE || cdlLayout == CDL_LAYOUT_GRID) {
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				if (cdlBaseButton.isXYInControl(e1.getX(), e1.getY())) {
 					cdlBaseButton.scroll(e1, e2, distanceX, distanceY);
 					invalidate(cdlBaseButton.getRect());
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		if (cdlLayout == CDL_LAYOUT_ABSOLUTE || cdlLayout == CDL_LAYOUT_GRID) {
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				if (cdlBaseButton.isXYInControl(e.getX(), e.getY())) {
 					tapUpOnCdlBaseButton(cdlBaseButton, e);
 				}
 			}
 			return false;
 		}
 		if (cdlLayout == CDL_LAYOUT_FLOW) {
 			int idBtn = getIdBtnFromX(e.getX());
 			if (idBtn < 0 || idBtn >= cdlBaseButtons.size())
 				return false;
 			CdlBaseButton cdlBaseButton = cdlBaseButtons.get(idBtn);
 			tapUpOnCdlBaseButton(cdlBaseButton, e);
 			return false;
 		}
 		return false;
 	}
 
 	private void tapUpOnCdlBaseButton(CdlBaseButton cdlBaseButton, MotionEvent e) {
 		if (cdlBaseButton.isVisible() && cdlBaseButton.isEnabled()) {
 			if (cdlBaseButton.isFlashCapable()) {
 				timerCountFlash = 2;
 				flashingBtn = cdlBaseButton;
 				runnableFlash.run();
 				cdlBaseButton.setFlashing(true);
 			}
 			cdlBaseButton.singleTapUp(e);
 			invalidate(cdlBaseButton.getRect());
 			CdlUtils.cdlLog(TAG, "invalidate : " + cdlBaseButton.getLabel());
 		}
 	}
 
 	public CdlBaseButton getButtonFromLabel(String s) {
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			if (cdlBaseButton.getLabel().equals(s)) {
 				return cdlBaseButton;
 			}
 		}
 		return null;
 	}
 
 	public CdlBaseButton getButtonFromId(int id) {
 		return cdlBaseButtons.get(id);
 	}
 
 	public void setCdlLayout(int cdlLayout) {
 		this.cdlLayout = cdlLayout;
 	}
 
 	public void setGrid_nbCols(int grid_nbCols) {
 		this.grid_nbCols = grid_nbCols;
 	}
 
 	public void setPadding(int padding) {
 		this.padding = padding;
 	}
 
 }
