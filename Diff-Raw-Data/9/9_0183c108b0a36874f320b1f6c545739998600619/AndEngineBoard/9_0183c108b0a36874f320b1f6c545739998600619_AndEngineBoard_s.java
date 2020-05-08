 /*******************************************************************************
  * Copyright 2011 Federico Paolinelli
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.whiterabbit.checkers.board;
 
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.sprite.Sprite;
 
 import com.whiterabbit.checkers.Constants;
 import com.whiterabbit.checkers.board.BoardCell.CantFillException;
 import com.whiterabbit.checkers.boards.BoardKind;
 import com.whiterabbit.checkers.ui.BallSprite;
 import com.whiterabbit.checkers.ui.CheckersGameActivity;
 import com.whiterabbit.checkers.ui.CheckersSpriteFactory;
 
 /**
  * Checkers board
  * 
  * @author fede
  * 
  */
 public class AndEngineBoard {
 	public class CantMoveException extends Exception {
 
 	}
 
 	static final float BALL_DISTANCE_FACTOR = 10;
 	static final float PADDING_FACTOR = 25;
 
 	private final CheckersGameActivity mGame;
 	private float mVerticalPadding = 0; // Vertical Padding
 	private float mHorizontalPadding = 0; // Horizontal Padding
 	private float mDistanceFromBottom = 20; // Extra offset from bottom
 	private float mBallSize;
 	private float mBallPadding; // distance between the side of two balls
 	private float mYDistance, mXDistance; // Distance between the center of two
 											// balls
 	private CheckersSpriteFactory mSpriteFactory;
 
 	private BoardCell[][] mBoard;
 	private Long mWidth;
 	private Long mHeight;
 	private final Scene mScene;
 
 	private long mScore = 0;
 	private LastMove mLastMove;
 
 	/**
 	 * Returns a board cell built up from the given kind
 	 * 
 	 * @param x
 	 *            board position
 	 * @param y
 	 *            board position
 	 * @param kind
 	 *            char kind
 	 * @return new board cell object
 	 */
 	private BoardCell fillCellFromChar(int x, int y, char kind) {
 		switch (kind) {
 		case '0':
 			return new FilledBoardCell(x, y);
 		case '1':
 			return new FillableBoardCell(x, y, true);
 		case 'X':
 			return new FillableBoardCell(x, y, false);
 		}
 		return new FilledBoardCell(x, y);
 	}
 
 	/**
 	 * Builds board from serialized string
 	 * 
 	 * @param dump
 	 *            the dump
 	 * @param f
 	 *            sprite factory
 	 * @param width
 	 *            board width
 	 * @param height
 	 *            board height
 	 */
 	public void buildFromString(String dump, CheckersSpriteFactory f,
 			int width, int height) {
 		mBoard = new BoardCell[width][];
 		for (int i = 0; i < width; i++) {
 			mBoard[i] = new BoardCell[height];
 			for (int j = 0; j < height; j++) {
 				char c = dump.charAt(i * width + j);
 				mBoard[i][j] = fillCellFromChar(i, j, c);
 				mBoard[i][j].buildSprites(f, mScene, this, this.mBallSize);
 			}
 		}
 	}
 
 	/**
 	 * Getter
 	 * 
 	 * @return
 	 */
 	public Long getWidth() {
 		return mWidth;
 	}
 
 	/**
 	 * Getter
 	 * 
 	 * @return
 	 */
 	public Long getHeight() {
 		return mHeight;
 	}
 
 	/**
 	 * Returns canvas position from board position
 	 */
 	public float getYCoordFromBoardX(int x) {
 		return (mBallSize / 2) + mBallPadding + mYDistance * x
 				+ mVerticalPadding - mDistanceFromBottom;
 	}
 
 	/**
 	 * Returns canvas position from board position
 	 * 
 	 * PADDING | o | o | PADDING
 	 * 
 	 * It must be padding + half ball + ball padding + y times (ball distance)
 	 */
 	public float getXCoordFromBoardY(int y) {
 		return (mBallSize / 2) + mBallPadding + mXDistance * y
 				+ mHorizontalPadding;
 	}
 
 	/**
 	 * Returns board position from canvas position
 	 * 
 	 * The formula is not EXACTLY the reversed one because I need to catch the
 	 * second half of the ball as well
 	 */
 	public int getBoardYfromXCoord(float x) {
 		int res = (int) ((x - mHorizontalPadding - mBallSize / 2 - mBallPadding + mBallSize / 2) / mXDistance);
 		if (res >= mWidth) {
 			res = mWidth.intValue() - 1;
 		}
 		return res;
 	}
 
 	/**
 	 * Returns board position from canvas position
 	 */
 	public int getBoardXfromYCoord(float y) {
 		int res = (int) ((y - mVerticalPadding + mDistanceFromBottom
 				- mBallSize / 2 - mBallPadding + mBallSize / 2) / mYDistance);
 
 		if (res >= mHeight) {
 			res = mHeight.intValue() - 1;
 		}
 		return res;
 	}
 
 	/**
 	 * Moving ball event
 	 * 
 	 * @param startX
 	 *            starting board position
 	 * @param startY
 	 *            starting board position
 	 * @param destX
 	 *            destination board position
 	 * @param destY
 	 *            destination board position
 	 * @throws CantMoveException
 	 * @throws CantFillException
 	 */
 	public void moveBall(int startX, int startY, int destX, int destY)
 			throws CantMoveException, CantFillException {
 		BoardCell startCell = mBoard[startX][startY];
 		BoardCell releaseCell = mBoard[destX][destY];
 
 		int middleX = 0, middleY = 0;
 
 		if (startX == destX) {
 			middleX = startX;
 			int diff = destY - startY;
 			if (Math.abs(diff) != 2) {
 				throw new CantMoveException();
 			}
 			middleY = startY + diff / 2;
 		}
 
 		if (startY == destY) {
 			middleY = startY;
 			int diff = destX - startX;
 			if (Math.abs(diff) != 2) {
 				throw new CantMoveException();
 			}
 			middleX = startX + diff / 2;
 		}
 		BoardCell middleCell = mBoard[middleX][middleY];
 
 		if (!startCell.hasBall() || !middleCell.hasBall()
 				|| releaseCell.hasBall()) {
 			throw new CantMoveException();
 		}
 		startCell.getBall();
 		middleCell.getBall();
 		releaseCell.putBall();
 
 		releaseCell.setBallSprite(startCell.getBallSprite());
 		startCell.eraseBallSprite();
 
 		final Sprite toDel = middleCell.getBallSprite();
 		middleCell.eraseBallSprite();
 
 		mLastMove.update(startCell, releaseCell, middleCell);
 
 		mGame.runOnUpdateThread(new Runnable() {
 			@Override
 			public void run() {
 				mScene.unregisterTouchArea(toDel);
 				mScene.getLayer(Constants.BALL_LAYER).removeEntity(toDel);
 			}
 		});
 
 		postMoveOperations();
 
 	}
 
 	/**
 	 * Sets sprite sizes according to screen size an ball numbers
 	 * 
 	 * @param ballNum
 	 * @param width
 	 * @param height
 	 * 
 	 *            The board is built in this way: | = pall padding o = ball size
 	 * 
 	 *            PADDING | o | o | PADDING
 	 * 
 	 *            ball padding is ballsize / BALL_DISTANCE_FACTOR horizontal
 	 *            padding is a fraction of width
 	 */
 	private void setSizes(int ballNum, float width, float height) {
 		mHorizontalPadding = 0;// ((float)width) / PADDING_FACTOR;
 
 		mBallSize = (width - 2 * mHorizontalPadding)
 				/ (ballNum + (ballNum + 1) / BALL_DISTANCE_FACTOR);
 		mBallPadding = mBallSize / BALL_DISTANCE_FACTOR;
 		mYDistance = (mBallSize + mBallPadding);
 		mXDistance = mYDistance;
 
 		mVerticalPadding = (height - mYDistance * (mWidth - 1)) / 2;
 	}
 
 	public AndEngineBoard(int width, int height, BoardKind b,
 			Boolean buildFromSaved, Scene s, CheckersSpriteFactory f,
 			int offset, CheckersGameActivity game) {
 		mScene = s;
 		mGame = game;
 		mHeight = Long.valueOf(b.getHeight());
 		mWidth = Long.valueOf(b.getWidth());
 		mDistanceFromBottom = offset;
 		mLastMove = new LastMove();
 		mSpriteFactory = f;
 
 		int ballNum = b.getWidth();
 
 		setSizes(ballNum, width, height);
 
 		if (buildFromSaved && b.load(game)) {
 			this.buildFromString(b.getSavedDump(), f, b.getWidth(), b
 					.getHeight());
 			this.mScore = b.getSavedScore();
 		} else {
 			this.buildFromString(b.getDump(), f, b.getWidth(), b.getHeight());
 		}
 
 	}
 
 	/**
 	 * Says if the ball can be moved from near cell to far cell
 	 * 
 	 * @return
 	 */
 	private Boolean canMoveBall(BoardCell near, BoardCell far) {
 		return (near.hasBall() && !far.hasBall() && far.fillableBall());
 	}
 
 	/**
 	 * Says if the ball in i,j position can be moved
 	 */
 	private Boolean canMove(int i, int j) {
 		if (!mBoard[i][j].hasBall()) {
 			return false;
 		}
 
 		BoardCell nearCell;
 		BoardCell farCell;
 		if (i > 1) {
 			nearCell = mBoard[i - 1][j];
 			farCell = mBoard[i - 2][j];
 			if (canMoveBall(nearCell, farCell)) {
 				return true;
 			}
 		}
 
 		if (i < mWidth - 2) {
 			nearCell = mBoard[i + 1][j];
 			farCell = mBoard[i + 2][j];
 			if (canMoveBall(nearCell, farCell)) {
 				return true;
 			}
 		}
 
 		if (j > 1) {
 			nearCell = mBoard[i][j - 1];
 			farCell = mBoard[i][j - 2];
 			if (canMoveBall(nearCell, farCell)) {
 				return true;
 			}
 		}
 
 		if (j < mHeight - 2) {
 			nearCell = mBoard[i][j + 1];
 			farCell = mBoard[i][j + 2];
 			if (canMoveBall(nearCell, farCell)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Says if more moves are available
 	 * 
 	 * @return
 	 */
 	public void postMoveOperations() {
 		mScore++;
 
 		for (int i = 0; i < mBoard.length; i++) {
 			for (int j = 0; j < mBoard[i].length; j++) {
 				if (canMove(i, j)) { // checks if at least one ball can be moved
 					mGame.onMove();
 					mGame.playMarbleSound();
 					return;
 				}
 			}
 		}
 
 		mGame.onGameStall(mScore);
 		return;
 	}
 
 	/**
 	 * Converts current board into string
 	 * 
 	 * @return serialized string
 	 */
 	public String serialize() {
 		StringBuffer res = new StringBuffer();
 		for (int i = 0; i < mBoard.length; i++) {
 			for (int j = 0; j < mBoard[i].length; j++) {
 				res.append(mBoard[i][j].getEncoding());
 			}
 		}
 		return res.toString();
 	}
 
 	public CheckersGameActivity getGame() {
 		return mGame;
 	}
 
 	public long getScore() {
 		return mScore;
 	}
 
 	public void backMove() throws CantFillException {
 		if (!mLastMove.canMoveBack())
 			return;
 
 		BoardCell lastStart = mLastMove.getLastStart();
 		BoardCell lastDest = mLastMove.getLastDest();
 		final BoardCell lastDeleted = mLastMove.getLastEaten();
 		lastStart.putBall();
 		lastDest.getBall();
 		lastDeleted.putBall();
 		BallSprite destBall = (BallSprite) lastDest.getBallSprite();
 		
 		lastStart.setBallSprite(destBall);
 		destBall.setPosition(lastStart.x, lastStart.y);
 		lastDest.eraseBallSprite();
 		mGame.runOnUpdateThread(new Runnable() {
 			@Override
 			public void run() {
 				lastDeleted.buildSprites(mSpriteFactory, mScene,
 						AndEngineBoard.this, AndEngineBoard.this.mBallSize);
 			}
 		});
 		mScore--;
 	}
 
 }
