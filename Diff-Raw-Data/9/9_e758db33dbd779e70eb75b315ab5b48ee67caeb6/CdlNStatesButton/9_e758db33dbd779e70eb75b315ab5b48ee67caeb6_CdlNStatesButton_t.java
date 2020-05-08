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
 
 package com.cadeli.ui.buttons;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.view.MotionEvent;
 
 import com.cadeli.ui.CdlBaseButton;
 import com.cadeli.ui.CdlPalette;
 import com.cadeli.uiDemo.TestElement;
 
 public class CdlNStatesButton extends CdlBaseButton {
 	private static final String TAG = "CdlNStatesButton";
 	protected List<Object> stateValues = new ArrayList<Object>();
 	private String stateTxt;
 	private int state;
 	private int defaultState = 0;
 	private int nbLig = 4;
 	private float startVisuLig;
 
 	public CdlNStatesButton(String title) {
 		super();
 		init(title);
 	}
 
 	public CdlNStatesButton() {
 		super();
 		init("default_title");
 	}
 
 	private void init(String label) {
 		setSize(10, 10, 20, 20);
 		defaultState = 0;
 		state = defaultState;
 		this.label = label;
 		this.stateTxt = "no_value";
 	}
 
 	public void draw(Canvas canvas) {
 		if (isVisible()) {
 			super.draw(canvas);
 			if (stateValues.size() == 0)
 				return;
 
 			if (displayMode == CdlBaseButton.DISPLAYMODE_COMPACT) {
 				drawCompact(canvas);
 			}
 			if (displayMode == CdlBaseButton.DISPLAYMODE_LIST) {
 				drawList(canvas);
 			}
 			if (displayMode == CdlBaseButton.DISPLAYMODE_EXPANDED) {
 				drawExpanded(canvas);
 			}
 			if (displayMode == CdlBaseButton.DISPLAYMODE_WITH_ARROW_BTN) {
 				drawWithArrowBtn(canvas);
 			}
 			if (isBorder()) {
 				rectf.set(rect.left + padding, rect.top + padding, rect.right - padding, rect.bottom - padding);
 				canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getBorderPaint());
 			}
 		}
 	}
 
 	private void drawWithArrowBtn(Canvas canvas) {
 		rectf.set(getLeft(), getTop(), getLeft() + w, getTop() + h);
 		drawCenterTextInrectCase(canvas, "" + (state + 1) + "/" + (stateValues.size()) + " - " + stateValues.get(state).toString(), CdlPalette.getTxtPaint(w - 2 * padding, h - 2 * padding));
 		rectf.set(getLeft(), getTop(), getLeft() + w / 5, getTop() + h);
 		drawCenterTextInrectCase(canvas, "<", CdlPalette.getTxtPaint(w - 2 * padding, h - 2 * padding));
 		rectf.set(getRight() - w / 5, getTop(), getRight(), getTop() + h);
 		drawCenterTextInrectCase(canvas, ">", CdlPalette.getTxtPaint(w - 2 * padding, h - 2 * padding));
 	}
 
 	private void drawList(Canvas canvas) {
 		int h_case = (h - (2 * padding + (int) round_w / 2)) / getNbLig();
 		for (int i = (int) startVisuLig; i < stateValues.size(); i++) {
 			float hstart = (i - startVisuLig) * h_case + rect.top + (int) round_w / 2;
 			if (hstart >= rect.top) {
 				if (hstart + h_case <= rect.bottom) {
					rectf.set(padding + rect.left + (int) round_w / 2, hstart, rect.right - padding - (int) round_w , hstart + h_case);
 					// CdlUtils.cdlLog(TAG, "DrawList:" + stateValues.get(0).toString() + "recf=" + rectf);
 					if (i == getState()) {
 						canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getHilightPaint());
 					} else {
 						if (backgroundPaint == null) {
 							canvas.drawRect(rectf, CdlPalette.getPaint(backgroundColor, 0, 0, w, h_case));
 						} else {
 							canvas.drawRect(rectf, backgroundPaint);
 						}
 					}
 					String txt = schrinkText(CdlPalette.getTxtPaint(w - 2 * padding, h_case - 2 * padding), bounds, getWidth(), stateValues.get(i).toString());
 					drawCenterTextInrectCase(canvas, txt, CdlPalette.getTxtPaint(w - 2 * padding, h_case - 2 * padding));
 				}
 			}
 		}
 		if (stateValues.size() > nbLig) {
 			drawVScrollbar(canvas);
 		} else {
 			startVisuLig = 0;
 		}
 	}
 
 	private void drawVScrollbar(Canvas canvas) {
 		int sLeft = rect.right - 8 - padding - (int) round_w / 2;
 		int sRight = rect.right - padding - (int) round_w / 2;
 		int sTop = rect.top + padding + (int) round_w / 2;
 		int sBottom = rect.bottom - padding - (int) round_w / 2;
 		int sHeigth = sBottom - sTop;
 		rectf.set(sLeft, sTop, sRight, sBottom);
 		canvas.drawRect(rectf, CdlPalette.getFlashPaint());
 		sTop = (int) ((startVisuLig * sHeigth) / stateValues.size()) + sTop;
 		sBottom = (int) (((startVisuLig + getNbLig()) * sHeigth) / stateValues.size());
 		rectf.set(sLeft, sTop, sRight, sBottom);
 		canvas.drawRect(rectf, CdlPalette.getHilightPaint());
 	}
 
 	private void drawExpanded(Canvas canvas) {
 		int w_case = (rect.width() - 2 * (int) (round_w / 2 + padding)) / stateValues.size();
 		int top = rect.top + padding + (int) round_h / 2;
 		int bottom = rect.bottom - padding - (int) round_w / 2;
 		int left = rect.left + padding + (int) round_w / 2;
 		int right = rect.right - padding - (int) round_w / 2;
 		canvas.drawRect(left, top, right, bottom, CdlPalette.getPaint(backgroundColor, getLeft(), getTop(), w, h));
 		for (int i = 0; i < stateValues.size(); i++) {
 			rectf.set(i * w_case + left, top, (i + 1) * w_case + left, bottom);
 			if (i == getState()) {
 				canvas.drawRect(rectf, CdlPalette.getHilightPaint());
 			}
 			String txt = schrinkText(CdlPalette.getTxtPaint(w_case, bottom - top), bounds, (int) rectf.width(), stateValues.get(i).toString());
 			drawCenterTextInrectCase(canvas, txt, CdlPalette.getTxtPaint(w_case, bottom - top));
 		}
 	}
 
 	private void drawCompact(Canvas canvas) {
 		rectf.set(rect.left + padding, rect.top + padding, rect.right - padding, rect.centerY());
 		canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getHilightPaint());
 		drawCenterTextUp(canvas, label, CdlPalette.getTxtPaint(w - 2 * padding, h - 2 * padding));
 		drawCenterTextDn(canvas, stateTxt, CdlPalette.getTxtPaint(w - 2 * padding, h - 2 * padding));
 	}
 
 	public void addState(String stateTxt) {
 		stateValues.add(stateTxt.toString());
 		if (stateValues.size() > defaultState) {
 			if (state >= 0 && state < stateValues.size()) {
 				this.stateTxt = stateValues.get(state).toString();
 			}
 		}
 	}
 
 	public void addState(TestElement testElement) {
 		addState(testElement.toString());
 	}
 
 	public void setStateTxt(String s) {
 		this.stateTxt = s;
 	}
 
 	public void scroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
 		if (displayMode == DISPLAYMODE_LIST) {
 			int h_case = (h - (2 * padding + (int) round_w / 2)) / getNbLig();
 			startVisuLig += ((float) distanceY / h_case);
 			if (startVisuLig + getNbLig() >= stateValues.size()) {
 				startVisuLig = stateValues.size() - getNbLig();
 			}
 			if (startVisuLig < 0)
 				startVisuLig = 0;
 		}
 		super.scroll(e1, e2, distanceX, distanceY);
 	}
 
 	public void singleTapUp(MotionEvent e) {
 		if (stateValues.isEmpty())
 			return;
 		if (displayMode == DISPLAYMODE_COMPACT) {
 			gotToNextState();
 		}
 		if (displayMode == DISPLAYMODE_LIST) {
 			int h_case = (h - (2 * padding + (int) round_w / 2)) / getNbLig();
 			float statef = ((e.getY() / h_case) + startVisuLig);
 			setState((int) statef);
 		}
 		if (displayMode == DISPLAYMODE_EXPANDED) {
 			setState(getNewStateFromX(e.getX()));
 		}
 		if (displayMode == DISPLAYMODE_WITH_ARROW_BTN) {
 			if (e.getX() > getLeft() + getWidth() / 2) {
 				gotToNextState();
 			} else {
 				gotToPrevState();
 			}
 		}
 		super.singleTapUp(e);
 	}
 
 	private int getNewStateFromX(float x) {
 		int st = (int) (((x - getLeft()) * stateValues.size()) / getWidth());
 		if (st >= 0 && st < stateValues.size())
 			return st;
 		else
 			return 0;
 	}
 
 	public void longPress(MotionEvent e) {
 		gotToDefaultState();
 		super.longPress(e);
 	}
 
 	public void gotToDefaultState() {
 		int newState = defaultState;
 		if (newState >= stateValues.size() || newState < 0) {
 			newState = 0;
 		}
 		setState(newState);
 	}
 
 	public void gotToNextState() {
 		int newState = state + 1;
 		if (newState >= stateValues.size()) {
 			newState = 0;
 		}
 		setState(newState);
 	}
 
 	public void gotToPrevState() {
 		int newState = state - 1;
 		if (newState < 0) {
 			newState = stateValues.size() - 1;
 		}
 		setState(newState);
 	}
 
 	public void setState(int newState) {
 		if (newState < stateValues.size() && newState >= 0) {
 			this.state = newState;
 			this.stateTxt = stateValues.get(newState).toString();
 		}
 	}
 
 	public int getState() {
 		return state;
 	}
 
 	public String getStateTxt() {
 		return stateTxt;
 	}
 
 	public void setDefaultState(int defaultState) {
 		this.defaultState = defaultState;
 	}
 
 	public void setList(List<Object> stateValues) {
 		this.stateValues = stateValues;
 		if (stateValues.size() > 0) {
 			setState(0);
 		}
 	}
 
 	public void setList(Object[] stateValues) {
 		this.stateValues.clear();
 		for (Object object : stateValues) {
 			addState(object.toString());
 		}
 	}
 
 	public void setNbLig(int nbLig) {
 		this.nbLig = nbLig;
 	}
 
 	private int getNbLig() {
 		return nbLig;
 	}
 
 }
