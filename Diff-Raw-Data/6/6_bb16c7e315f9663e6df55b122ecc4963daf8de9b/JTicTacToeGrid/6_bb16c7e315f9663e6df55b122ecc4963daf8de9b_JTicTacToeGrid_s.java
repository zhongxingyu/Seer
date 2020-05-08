 package com.crudetech.tictactoe.delivery.swing.grid;
 
 import com.crudetech.event.Event;
 import com.crudetech.event.EventListener;
 import com.crudetech.event.EventSupport;
 import com.crudetech.tictactoe.delivery.gui.widgets.GridCellHit;
 import com.crudetech.tictactoe.delivery.gui.widgets.TicTacToeGridModel;
 import com.crudetech.tictactoe.ui.CellEventObject;
 
 import javax.swing.*;
 import javax.swing.plaf.ComponentUI;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 public class JTicTacToeGrid extends JComponent {
     private TicTacToeGridModel model;
     private EventSupport<CellEventObject<JTicTacToeGrid>> clickedEvent = new EventSupport<>();
     private EventListener<TicTacToeGridModel.CellsChangedEventObject> modelCellChangedListener;
     private EventListener<TicTacToeGridModel.ChangedEventObject> modelChangedListener;
 
     static {
         UIManager.getDefaults().put(TicTacToeGridUI.class.getSimpleName(), TicTacToeGridUI.class.getName());
     }
 
     public JTicTacToeGrid() {
         this(new TicTacToeGridModel());
         updateUI();
     }
 
     public JTicTacToeGrid(TicTacToeGridUI gridUi) {
         this(new TicTacToeGridModel());
         setUI(gridUi);
     }
 
     public JTicTacToeGrid(TicTacToeGridModel model) {
         initializeListeners();
         setModel(model);
         updateUI();
     }
 
     public JTicTacToeGrid(TicTacToeGridModel model, TicTacToeGridUI ui) {
         initializeListeners();
         setModel(model);
         setUI(ui);
     }
 
     private void initializeListeners() {
         addMouseListener();
         addMouseMotionListener();
         modelCellChangedListener = new EventListener<TicTacToeGridModel.CellsChangedEventObject>() {
             @Override
             public void onEvent(TicTacToeGridModel.CellsChangedEventObject e) {
                 getUI().repaintCells(e.getChangedCells());
             }
         };
         modelChangedListener = new EventListener<TicTacToeGridModel.ChangedEventObject>() {
             @Override
             public void onEvent(TicTacToeGridModel.ChangedEventObject e) {
                 getUI().repaintAll();
             }
         };
     }
 
     private void addMouseMotionListener() {
         addMouseMotionListener(new MouseAdapter() {
             @Override
             public void mouseMoved(MouseEvent e) {
                 onMouseMoved(e);
             }
         });
     }
 
     private void addMouseListener() {
         addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 onMouseClicked(e);
             }
         });
     }
 
     private void onMouseMoved(MouseEvent e) {
         GridCellHit hit = cellHitFromMouseEvent(e);
         highlightHoveredCell(hit);
     }
 
     private void highlightHoveredCell(GridCellHit hit) {
         if (hit.hasHit()) {
             getModel().highlightCell(hit.getHit());
         } else {
             getModel().unHighlightCell();
         }
     }
 
     private void onMouseClicked(MouseEvent e) {
         GridCellHit hit = cellHitFromMouseEvent(e);
         raiseCellEvent(hit);
     }
 
     private void raiseCellEvent(GridCellHit hit) {
         if (hit.hasHit()) {
             onCellEvent(hit);
         }
     }
 
     private void onCellEvent(GridCellHit hit) {
         clickedEvent.fireEvent(new CellEventObject<>(this, hit.getHit()));
     }
 
     private GridCellHit cellHitFromMouseEvent(MouseEvent e) {
         return getUI().gridCellHit(e.getX(), e.getY());
     }
 
     public void setModel(TicTacToeGridModel model) {
         unhookModelEvents();
         hookModelEvents(model);
         this.model = model;
         repaint();
     }
 
     private void hookModelEvents(TicTacToeGridModel model) {
         model.cellsChanged().addListener(modelCellChangedListener);
         model.changed().addListener(modelChangedListener);
     }
 
     private void unhookModelEvents() {
         if (hasModel()) {
             getModel().cellsChanged().removeListener(modelCellChangedListener);
             getModel().changed().removeListener(modelChangedListener);
         }
     }
 
     private boolean hasModel() {
         return getModel() != null;
     }
 
     @Override
     public String getUIClassID() {
         return TicTacToeGridUI.class.getSimpleName();
     }
 
     TicTacToeGridUI getUI() {
         return (TicTacToeGridUI) this.ui;
     }
 
     @Override
     public void setUI(ComponentUI newUI) {
         super.setUI(newUI);
     }
 
     @Override
     public void updateUI() {
         setUI(UIManager.getUI(this));
     }
 
     public TicTacToeGridModel getModel() {
         return model;
     }
 
     void raiseMouseEvent(MouseEvent e) {
         super.processMouseEvent(e);
         super.processMouseMotionEvent(e);
     }
 
     public Event<CellEventObject<JTicTacToeGrid>> cellClicked() {
         return clickedEvent;
     }
 }
