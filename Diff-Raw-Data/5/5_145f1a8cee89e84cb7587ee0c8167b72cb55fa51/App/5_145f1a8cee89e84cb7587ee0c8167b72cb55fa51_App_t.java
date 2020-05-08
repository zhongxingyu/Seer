 package ua.org.dector.scad;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import ua.org.dector.scad.model.Document;
 import ua.org.dector.scad.model.Item;
 import ua.org.dector.scad.model.nodes.*;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.*;
 
 /**
  * Main application
  *
  * @author dector
  */
 public class App extends Game {
     public enum Mode { NONE, EDIT, DOWN_ARROW_INSERT }
 
     private Document document;
     private Renderer renderer;
 
     private Mode mode;
     
     private Arrow unpairedArrow;
 
     /**
      * On create
      */
     public void create() {
         renderer = new Renderer(this, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 
         mode = Mode.NONE;
 
         Gdx.input.setInputProcessor(new InputController(this));
     }
 
     /**
      * Create new document
      */
     public void createDocument() {
//        if (document == null)
             document = new Document();
 
         setRendererDirty();
     }
 
     /**
      * Edit document
      */
     public void enterEditMode() {
         if (mode == Mode.EDIT) return;
 
         mode = Mode.EDIT;
 
         setRendererDirty();
     }
 
     /**
      * View document
      */
     public void exitEditMode() {
         if (mode != Mode.EDIT) return;
 
         mode = Mode.NONE;
 
         setRendererDirty();
     }
 
     /**
      * Select previous item
      *
      * @param append false if select only one
      */
     public void selectPrev(boolean append) {
         if (mode != Mode.EDIT
                 && mode != Mode.DOWN_ARROW_INSERT) return;
         
         Item curr = document.getCurrentItem();
         Item prev = curr.getPrev();
         if (prev == null) return;
 
         if (append) {
             if (! document.select(prev, true))
                 document.deselect(curr);
         } else
             document.selectOnly(prev);
         
         document.setCurrentItem(prev);
         setRendererDirty();
     }
 
     /**
      * Select next item
      *
      * @param append false if select only one
      */
     public void selectNext(boolean append) {
         if (mode != Mode.EDIT
                 && mode != Mode.DOWN_ARROW_INSERT) return;
 
         Item curr = document.getCurrentItem();
         Item next = curr.getNext();
         if (next == null) return;
 
         if (append) {
             if (! document.select(next, false))
                 document.deselect(curr);
         } else
             document.selectOnly(next);
 
         document.setCurrentItem(next);
         setRendererDirty();
     }
 
     /**
      * New operational node
      *
      * @param enterId false if init with generated id
      */
     public void createOperationalNode(boolean enterId) {
         if (mode != Mode.EDIT) return;
         if (document.getCurrentItem().getType() == Item.Type.END) return;
         
         int id;
         if (enterId)
             id = enterId(Signal.getLasId() + 1);
         else
             id = Signal.nextId();
         
         if (id != -1) {
             Item prevItem = document.getCurrentItem();
             Item nextItem = prevItem.getNext();
             Item newItem = new Item(Item.Type.Y, id);
 
             insertItemBetweenAndSelect(newItem, prevItem, nextItem);
 
             setRendererDirty();
         }
     }
 
     /**
      * New conditional node
      *
      * @param enterId false if init with generated id
      */
     public void createConditionalNode(boolean enterId) {
         if (mode != Mode.EDIT) return;
         if (document.getCurrentItem().getType() == Item.Type.END) return;
 
         int id;
         if (enterId)
             id = enterId(Condition.getLasId() + 1);
         else
             id = Condition.nextId();
 
         if (id != -1) {
             Item prevItem = document.getCurrentItem();
             Item nextItem = prevItem.getNext();
             Item newItem = new Item(Item.Type.X, id);
 
             insertItemBetweenAndSelect(newItem, prevItem, nextItem);
 
             createArrow(enterId);
 
             setRendererDirty();
         }
     }
 
     /**
      * New arrow
      *
      * @param enterId false if init with generated id
      */
     public void createArrow(boolean enterId) {
         if (mode != Mode.EDIT) return;
         if (document.getCurrentItem().getType() == Item.Type.END) return;
 
         int id;
         if (enterId)
             id = enterId(Arrow.getLasId() + 1);
         else
             id = Arrow.nextId();
 
         if (id != -1) {
             Item prevItem = document.getCurrentItem();
             Item nextItem = prevItem.getNext();
             Item newItem = new Arrow(Item.Type.ARROW_UP, id);
 
             insertItemBetweenAndSelect(newItem, prevItem, nextItem);
 
             mode = Mode.DOWN_ARROW_INSERT;
             unpairedArrow = (Arrow)newItem;
 
             setRendererDirty();
         }
     }
 
     /**
      * Put down arrow here
      */
     public void insertDownArrow() {
         if (mode != Mode.DOWN_ARROW_INSERT) return;
         
         Item currItem = document.getCurrentItem();
         if (currItem.getType() == Item.Type.BEGIN) return;
         if (currItem.getType() == Item.Type.ARROW_UP
                 && currItem.getPrev().getType() == Item.Type.X) return;
 
         Item nextItem = document.getCurrentItem();
         Item prevItem = nextItem.getPrev();
         Arrow newItem = new Arrow(Item.Type.ARROW_DOWN, unpairedArrow.getId());
 
         unpairedArrow.setPair(newItem);
         newItem.setPair(unpairedArrow);
 
         insertItemBetweenAndSelect(newItem, prevItem, nextItem, false);
 
         unpairedArrow = null;
 
         mode = Mode.EDIT;
 
         setRendererDirty();
     }
 
     /**
      * Discard arrow insert changes
      */
     public void cancelArrowInsert() {
         if (mode != Mode.DOWN_ARROW_INSERT) return;
 
         if (unpairedArrow.getPrev().getType() == Item.Type.X) {
             // If it's after x -> cancel insert last x
             document.selectOnly(unpairedArrow.getPrev());
             removeItem();
         }
 
         // Remove last up arrow
         if (unpairedArrow.getId() == Arrow.getLasId())
             Arrow.decLastId();
 
         document.selectOnly(unpairedArrow);
         removeItem();
 
         unpairedArrow = null;
 
         mode = Mode.EDIT;
     }
 
     /**
      * Remove item
      */
     public void removeItem() {
         if (mode != Mode.EDIT) return;
 
         Item itemToRemove = document.getCurrentItem();
         
         if (itemToRemove.getType() == Item.Type.BEGIN) return;
         if (itemToRemove.getType() == Item.Type.END) return;
         
         Item item = document.getHead();
         while (item.hasNext() && itemToRemove != item) {
             item = item.getNext();
         }
         
         if (itemToRemove == item) {
             Item nextItem = item.getNext();
             Item prevItem = item.getPrev();
 
             prevItem.setNext(nextItem);
             nextItem.setPrev(prevItem);
 
             selectPrev(false);
 
             setRendererDirty();
         }
     }
 
     /**
      * Group/ungroup signals
      */
     public void toggleGroup() {
         if (mode != Mode.EDIT) return;
 
         Item currItem = document.getCurrentItem();
         
         if (document.getSelectedCount() > 1) {
             boolean valid = true;
             Item[] selected = document.getSelected();
             
             int selCount = selected.length;
             Item item;
 
             {
                 int i = 0;
                 while (valid && i < selCount) {
                     item = selected[i];
     
                     if (item.getType() != Item.Type.Y
                             || ((Operational)item.getNode()).getSignalsCount() > 1) {
                         valid = false;
                     } else {
                         i++;
                     }
                 }
             }
 
             // Group
             if (valid) {
                 Signal[] signals = new Signal[selCount];
                 
                 for (int i = 0; i < selCount; i++) {
                     signals[i] = ((Operational)selected[i].getNode()).getSignal();
                 }
 
                 Signal[] optSignals = deleteSignalDublication(signals);
 
                 Operational newNode = new Operational(optSignals);
                 Item newItem = new Item(Item.Type.Y, 0);
                 newItem.setNode(newNode);
 
                 Item firstItem = selected[0];
                 Item lastItem = selected[selCount - 1];
 
                 insertItemBetweenAndSelect(newItem, firstItem.getPrev(), lastItem.getNext());
 
                 document.selectOnly(newItem);
 
                 setRendererDirty();
             }
         } else if (currItem.getType() == Item.Type.Y
                 && ((Operational)currItem.getNode()).getSignalsCount() > 1) {
             // Ungroup
             Signal[] signals = ((Operational) currItem.getNode()).getSignals();
             
             Item item = null;
             Item prevItem = currItem.getPrev();
             Item nextItem = currItem.getNext();
 
             // For gc
             currItem.setNext(null);
             currItem.setPrev(null);
 
             for (Signal signal : signals) {
                 item = new Item(Item.Type.Y, signal.getId());
 
                 insertItemBetweenAndSelect(item, prevItem, nextItem);
 
                 prevItem = item;
             }
 
             document.selectOnly(item);
             
             setRendererDirty();
         }
     }
 
     /**
      * Edit item id
      */
     public void editItemId() {
         if (mode != Mode.EDIT) return;
         
         Item currItem = document.getCurrentItem();
         Item.Type currItemType = currItem.getType();
         
         if (currItemType == Item.Type.BEGIN) return;
         if (currItemType == Item.Type.END) return;
         
         if (currItemType == Item.Type.Y
                 && ((Operational) currItem.getNode()).getSignalsCount() > 1) return;
 
         // Can't change arrow id
         /*if (currItemType == Item.Type.ARROW_DOWN
                 || currItemType == Item.Type.ARROW_UP) {
             Arrow currArrow = (Arrow) currItem;
             Arrow pairArrow = currArrow.getPair();
 
             int newId = enterId(currArrow.getId());
             currArrow.setId(newId);
             pairArrow.setId(newId);
 
         } else*/ if (currItemType == Item.Type.X) {
             Condition cond = ((Conditional) currItem.getNode()).getCondition();
             cond.setId(enterId(cond.getId()));
 
             setRendererDirty();
         } else if (currItemType == Item.Type.Y) {
             Signal signal = ((Operational) currItem.getNode()).getSignal();
             signal.setId(enterId(signal.getId()));
 
             setRendererDirty();
         }
     }
 
     /**
      * Move item(s) left
      */
    public void movectLefctedLeft() {
         if (mode != Mode.EDIT) return;
         
         Item firstSelected = document.getFirstSelected();
         Item lastSelected = document.getLastSelected();
 
         if (firstSelected.getType() == Item.Type.BEGIN) return;
         if (firstSelected.getType() == Item.Type.END) return;
         if (lastSelected.getType() == Item.Type.BEGIN) return;
         if (lastSelected.getType() == Item.Type.END) return;
         if (firstSelected.getPrev().getType() == Item.Type.BEGIN) return;
 
         Item prevItem = firstSelected.getPrev();
         Item newPrevItem = prevItem.getPrev();
         Item nextItem = lastSelected.getNext();
 
         newPrevItem.setNext(firstSelected);
         firstSelected.setPrev(newPrevItem);
 
         lastSelected.setNext(prevItem);
         prevItem.setPrev(lastSelected);
 
         prevItem.setNext(nextItem);
         nextItem.setPrev(prevItem);
 
         setRendererDirty();
     }
 
     /**
      * Move item(s) right
      */
     public void moveSelectedRight() {
         if (mode != Mode.EDIT) return;
 
         Item firstSelected = document.getFirstSelected();
         Item lastSelected = document.getLastSelected();
 
         if (firstSelected.getType() == Item.Type.BEGIN) return;
         if (firstSelected.getType() == Item.Type.END) return;
         if (lastSelected.getType() == Item.Type.BEGIN) return;
         if (lastSelected.getType() == Item.Type.END) return;
         if (lastSelected.getNext().getType() == Item.Type.END) return;
 
         Item prevItem = firstSelected.getPrev();
         Item nextItem = lastSelected.getNext();
         Item newNextItem = nextItem.getNext();
 
         prevItem.setNext(nextItem);
         nextItem.setPrev(prevItem);
 
         nextItem.setNext(firstSelected);
         firstSelected.setPrev(nextItem);
 
         lastSelected.setNext(newNextItem);
         newNextItem.setPrev(lastSelected);
 
         setRendererDirty();
     }
 
     /**
      * Save document
      */
     public void saveDocument() {
         String fileName = JOptionPane.showInputDialog(null, "Enter file name to store", "default");
 
         try {
             FileManager.store(document, fileName);
         } catch (IOException e) {
             JOptionPane.showMessageDialog(null, e.getMessage(), "Saving error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
         }
 
         JOptionPane.showMessageDialog(null, "Document <<" + fileName + ">> saved",
                 "Success", JOptionPane.INFORMATION_MESSAGE);
     }
 
     /**
      * Load document
      */
     public void loadDocument() {
         String fileName = JOptionPane.showInputDialog(null, "Enter file name to store", "default");
 
         Document loadedDoc = null;
         try {
             loadedDoc = FileManager.restore(fileName);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null, e.getClass() + ": " + e.getMessage(),
                     "Loading error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
         }
 
         if (loadedDoc != null) {
             document = loadedDoc;
 
             setRendererDirty();
         }
     }
 
     /**
      * Delete dublicated signals
      *
      * @param signals signals array
      * @return crear array
      */
     private Signal[] deleteSignalDublication(Signal[] signals) {
         Set<Signal> signalSet = new HashSet<Signal>();
 
         Collections.addAll(signalSet, signals);
         
         Signal[] optimised = new Signal[signalSet.size()];
 
         signalSet.toArray(optimised);
         Arrays.sort(optimised);
 
         return optimised;
     }
 
     private void insertItemBetweenAndSelect(Item newItem, Item prevItem, Item nextItem) {
         insertItemBetweenAndSelect(newItem, prevItem, nextItem, true);
     }
     
     private void insertItemBetweenAndSelect(Item newItem, Item prevItem, Item nextItem, boolean selectNext) {
         newItem.setPrev(prevItem);
         newItem.setNext(nextItem);
 
         prevItem.setNext(newItem);
         nextItem.setPrev(newItem);
 
         if (selectNext)
             selectNext(false);
         else
             selectPrev(false);
     }
                                              
     private int enterId(int defaultId) {
         final int[] id = new int[1];
 
         String text = JOptionPane.showInputDialog(null, "Input inner signal id", String.valueOf(defaultId));
 
         try {
             id[0] = Integer.valueOf(text);
         } catch (NumberFormatException e) {
             id[0] = -1;
         }
 
         if (id[0] == Signal.getLasId() + 1)
             Signal.nextId();
 
         return id[0];
     }
 
     public void render() {
         if (document != null)
             renderer.render(document);
     }
 
     public Document getDocument() {
         return document;
     }
 
     public Mode getMode() {
         return mode;
     }
 
     public void setRendererDirty() {
         renderer.setDirty(true);
     }
 }
