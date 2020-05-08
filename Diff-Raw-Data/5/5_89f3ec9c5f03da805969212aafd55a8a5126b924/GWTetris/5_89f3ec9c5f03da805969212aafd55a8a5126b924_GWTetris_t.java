 package com.github.fhd.gwtetris.client;
 
 import com.github.fhd.gwtetris.client.gamelogic.*;
 import com.github.fhd.gwtetris.client.gamelogic.Grid;
 import com.google.gwt.core.client.*;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.*;
 import com.google.gwt.uibinder.client.*;
 import com.google.gwt.user.client.*;
 import com.google.gwt.user.client.Event.NativePreviewEvent;
 import com.google.gwt.user.client.Event.NativePreviewHandler;
 import com.google.gwt.user.client.ui.*;
 
 /**
  * The entry point and user interface of GWTetris.
  */
 class GWTetris extends UIObject implements EntryPoint, Renderer {
     interface Binder extends UiBinder<Widget, GWTetris> {}
     private static Binder uiBinder = GWT.create(Binder.class);
 
     private Style style;
     private Game game;
     private Grid grid;
     private Piece currentPiece;
     private boolean started;
     private boolean paused;
     private LayoutPanel piecePanel;
     private DecoratedPopupPanel popup;
     @UiField LayoutPanel gridPanel;
     @UiField LayoutPanel previewPanel;
     @UiField Button resumeButton;
 
     /**
      * The entry point method.
      */
     public void onModuleLoad() {
         Resources resources = GWT.create(Resources.class);
         style = resources.style();
         style.ensureInjected();
         game = new Game(this, new JavaRNG(),
                         Constants.GRID_COLS, Constants.GRID_ROWS);
         piecePanel = new LayoutPanel();
         popup = new DecoratedPopupPanel();
         RootPanel.get().add(uiBinder.createAndBindUi(this));
         resumeButton.setFocus(true);
 
         new Timer() {
             @Override
             public void run() {
                 if (started && !paused)
                     game.step();
             }
         }.scheduleRepeating(Constants.STEP_TIME);

         Event.addNativePreviewHandler(new NativePreviewHandler() {
                 @Override
                 public void onPreviewNativeEvent(NativePreviewEvent event) {
                    if (event.getTypeInt() == Event.ONKEYUP)
                         onKey(event.getNativeEvent().getKeyCode());
                 }
         });
     }
 
     private void onKey(int keyCode) {
         if (!started || paused)
             return;
                 
         switch (keyCode) {
         case KeyCodes.KEY_LEFT:
             currentPiece.moveLeft();
             break;
         case KeyCodes.KEY_RIGHT:
             currentPiece.moveRight();
             break;
         case KeyCodes.KEY_DOWN:
             currentPiece.moveDown();
             break;
         case KeyCodes.KEY_UP:
             currentPiece.rotate();
             break;
         case KeyCodes.KEY_ENTER:
             game.fastStep();
             break;
         }
     }
     
     @UiHandler("resumeButton")
     void handleClick(ClickEvent event) {
         if (!started) {
             game.start();
             started = true;
         } else {
             paused = !paused;
         }
 
         resumeButton.setText(paused ? "Resume" : "Pause");
         resumeButton.setFocus(paused);
         if (paused)
             showMessage("Paused");
         else
             hideMessage();
     }
 
     private void showMessage(String message) {
         popup.setWidget(new Label(message));
         popup.center();
     }
 
     private void hideMessage() {
         popup.hide();
     }
 
     @Override
     public void displayGrid(Grid grid) {
         this.grid = grid;
         updateGrid();
     }
 
     @Override
     public void updateGrid() {
         drawMatrix(grid.getMatrix(), gridPanel);
         gridPanel.add(piecePanel);
     }
 
     private void drawMatrix(int[][] matrix, LayoutPanel panel) {
         drawMatrix(matrix, panel, 0, 0);
     }
     
     private void drawMatrix(int[][] matrix, LayoutPanel panel,
                             int xOffset, int yOffset) {
         panel.clear();
         for (int yCell = 0; yCell < matrix.length; yCell++)
             for (int xCell = 0; xCell < matrix[yCell].length; xCell++)
                 if (matrix[yCell][xCell] > 0) {
                     HTML block = new HTML();
                     block.setStyleName(style.block());
                     panel.add(block);
                     panel.setWidgetLeftWidth(block, 
                             xCell * Constants.BLOCK_HEIGHT + xOffset,
                             Unit.PX, Constants.BLOCK_WIDTH, Unit.PX);
                     panel.setWidgetTopHeight(block,
                             yCell * Constants.BLOCK_WIDTH + yOffset, 
                             Unit.PX, Constants.BLOCK_HEIGHT, Unit.PX);
                 }
     }
 
     @Override
     public void displayPiece(Piece piece) {
         currentPiece = piece;
         updatePiece();
     }
 
     @Override
     public void displayNextPiece(Piece piece) {
         int[][] pieceMatrix = piece.getMatrix();
         int xOffset = previewPanel.getOffsetWidth() / 2
                       - (pieceMatrix[0].length * Constants.BLOCK_WIDTH) / 2;
         int yOffset = previewPanel.getOffsetHeight() / 2
                       - (pieceMatrix.length * Constants.BLOCK_HEIGHT) / 2;
         drawMatrix(pieceMatrix, previewPanel, xOffset, yOffset);
     }
 
     @Override
     public void updatePiece() {
         drawMatrix(currentPiece.getMatrix(), piecePanel);
         gridPanel.setWidgetLeftRight(piecePanel,
                 currentPiece.getX() * Constants.BLOCK_WIDTH,
                 Unit.PX, 0, Unit.PX);
         gridPanel.setWidgetTopBottom(piecePanel,
                 currentPiece.getY() * Constants.BLOCK_HEIGHT,
                 Unit.PX, 0, Unit.PX);
     }
 
     @Override
     public void displayGameOver() {
         paused = false;
         started = false;
         resumeButton.setText("Start");
         showMessage("Game Over");
     }
 }
