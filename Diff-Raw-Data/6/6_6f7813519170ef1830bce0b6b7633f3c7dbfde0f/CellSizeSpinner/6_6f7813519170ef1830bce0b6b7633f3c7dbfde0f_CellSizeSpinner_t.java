 /*
  * CellSizeSpinner.java
  * 
  * date:    18.09.2011
  * auth:    Gomon Sergey
  * 
  * Класс представляет собой графический элемент управления размером клетки
  * пола (JSpinner). Он связан с канвой. Есть 2 способа изменить размер клетки
  * канвы, при помощи скролинга и при помощи данного элемента. Эти способы связаны,
  * например, при изменении размера клетки скролингом, значение размера клетки
  * в компоненте меняется автоматически.
  */
 package controls;
 
 import canva.Canva;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 /**
  * 18.09.2011
  *
  * @author Sergey
  */
 public class CellSizeSpinner {
     private int minSpinnVal;
     private int maxSpinnVal;
     private int spinnStep;
 
     private JSpinner spinner;
     private Canva canva;
 
     public CellSizeSpinner(Canva canva, int min, int max, int step) {
         this.canva = canva;
         this.minSpinnVal = min;
         this.maxSpinnVal = max;
         this.spinnStep = step;

         int cellSize = canva.getCellSize();
         SpinnerModel spModelCell = new SpinnerNumberModel(
                 cellSize, min, max, step);
         spinner = new JSpinner(spModelCell);
         Listener listener = new Listener();
         spinner.addChangeListener(listener);
         canva.addMouseWheelListener(listener);
         canva.setCellSize(cellSize);
     }
 
     public JSpinner getSpinner() {
         return spinner;
     }
 
     private int getInc(int val, int inc) {
         if (val + inc < minSpinnVal) return minSpinnVal;
         if (val + inc > maxSpinnVal) return maxSpinnVal;
         return val + inc;
     }
 
     private class Listener implements MouseWheelListener, ChangeListener {
         public void mouseWheelMoved(MouseWheelEvent e) {
             int val = (Integer) (spinner.getValue());
             spinner.setValue(getInc(val, e.getWheelRotation() * spinnStep));
         }
 
         public void stateChanged(ChangeEvent e) {
             canva.setCellSize((Integer) spinner.getValue());
             canva.update();
         }
     }
 }
