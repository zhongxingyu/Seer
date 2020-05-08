 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
public class sudoku {
 
     static int mNum = 0;
     static cell[][] cellArray = new cell[9][9];
     static menuCell[] mCellArray = new menuCell[9];
     static cell[] cellLine = new cell[81];
     static int fps;
     static JButton startButton;
     static int itt;
     public static void main(String[] args) {
         class th extends Thread {
 
             @Override
             public void run() {
                 GUI g = new GUI();
 
             }
         }
         th t = new th();
         t.start();
     }
 
     public static class GUI extends JFrame {
 
         GUI() {
             int FPS_MIN = 0;
             int FPS_MAX = 1000;
             int FPS_INIT = 50;    //initial frames per second
             fps = FPS_INIT;
             
             JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX, FPS_INIT);
             SliderListener fpslistener = new SliderListener();
             framesPerSecond.addChangeListener(fpslistener);
             framesPerSecond.setMajorTickSpacing(200);
             framesPerSecond.setMinorTickSpacing(100);
             framesPerSecond.setPaintTicks(true);
             framesPerSecond.setPaintLabels(true);
             framesPerSecond.setBounds(10, 370, 270, 50);
             
             
             JPanel p = new JPanel(null);
             startButton = new JButton();
 
             p.setBounds(0, 0, 300, 420);
             p.setOpaque(true);
 
             int i, j;
             for (i = 0; i < 9; ++i) {
                 mCellArray[i] = new menuCell(i);
                 p.add(mCellArray[i]);
                 for (j = 0; j < 9; ++j) {
                     cellArray[i][j] = new cell(i, j);
                     p.add(cellArray[i][j]);
                 }
             }
             startButtonListener flistener = new startButtonListener();
 
             startButton.setBounds(10, 325, 270, 40);
             startButton.setText("Solve");
             startButton.addMouseListener(flistener);
             p.add(startButton);
             p.add(framesPerSecond);
             
             this.setSize(300, 420 + 35);
             this.setTitle("Suduku Solver");
             this.setResizable(true);
             this.setLocationRelativeTo(null);
             this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             this.add(p);
             this.setVisible(true);
         }
                 
     }
     
    static class SliderListener implements ChangeListener {
 
             @Override
             public void stateChanged(ChangeEvent e) {
                 JSlider source = (JSlider) e.getSource();
                 fps = (int) source.getValue();
             }
         }
     
 
     public static class startButtonListener extends MouseAdapter {
 
         @Override
         public void mousePressed(MouseEvent me) {            
             for (int i = 0; i < 9; ++i) {
                 for (int j = 0; j < 9; ++j) {
                     cellLine[i + 9*j] = cellArray[i][j];
                 }
             }
              class th1 extends Thread{
                 @Override
                 public void run(){
                    solverR(); 
                 }
             }
              
              
             new th1().start();
             
         }
     }
 
     public static boolean solverR() {
         retObj ro;
         ro = findEmpty();
         ++itt;
         startButton.setText(Integer.toString(itt));
         if (!ro.b) {
             return true; //all cells assigned!
         }
         
         int loc = ro.i;
         for (int num = 1; num <= 9; ++num) {
             if(noConflicts(loc,num)){
                 cellLine[loc].num = num;
                 cellLine[loc].setText(Integer.toString(num));
                 try {
                     Thread.sleep(1000/fps);
                 } catch (InterruptedException ex) {
                    Logger.getLogger(sudoku.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 if(solverR()){
                     return true;
                 }else{
                     cellLine[loc].num = 0;
                     cellLine[loc].setText(" ");
                 }
             }
         }
 
         return false;
     }
     
     public static class retObj{
         boolean b;
         int i;
         retObj(boolean a,int n){
             b = a;
             i = n;
         }
     }
 
     public static boolean noConflicts(int x,int n) {
         for (int k = 0; k < 81; ++k) {
                 if (cellLine[x].row == cellLine[k].row || cellLine[x].column == cellLine[k].column || cellLine[x].square == cellLine[k].square) {
                     if (cellLine[k].num == n) {
                         return false;
                     }
                 }
 
         }
 
         return true;
     }
 
     public static retObj findEmpty() {
         for (int i = 0; i < 81; ++i) {
             if (cellLine[i].num == 0) {
                 return new retObj(true,i);
             }
         }
         return new retObj(false,0);
     }
 
     public static class cellListener extends MouseAdapter {
 
         @Override
         public void mousePressed(final MouseEvent me) {
             ((cell) (me.getSource())).num = mNum;
             if( mNum ==0){
                 ((cell) (me.getSource())).setText("");
             }else{
                 ((cell) (me.getSource())).setText(Integer.toString(mNum));
             }
         }
     }
 
     public static class menuCellListener extends MouseAdapter {
 
         @Override
         public void mousePressed(final MouseEvent me) {
             if (((menuCell) (me.getSource())).isCurrent) {
                 ((menuCell) (me.getSource())).isCurrent = false;
                 ((menuCell) (me.getSource())).setBackground(Color.white);
                 mNum = 0;
             } else {
                 for (int i = 0; i < 9; ++i) {
                     mCellArray[i].isCurrent = false;
                     mCellArray[i].setBackground(Color.white);
                 }
                 mNum = ((menuCell) (me.getSource())).num;
                 ((menuCell) (me.getSource())).isCurrent = true;
                 ((menuCell) (me.getSource())).setBackground(Color.GRAY);
             }
         }
     }
 
     public static class menuCell extends JLabel {
 
         int num;
         boolean isCurrent = false;
 
         menuCell(int i) {
             num = i + 1;
             menuCellListener mcelllistener = new menuCellListener();
             this.setOpaque(true);
             this.setBounds(30 * i + 10 + 5 * ((int) (Math.floor(i / 3))), 0, 25, 25);
             this.setBackground(Color.white);
             this.setText(Integer.toString(i + 1));
             this.setHorizontalTextPosition(RIGHT);
             this.setBorder(BorderFactory.createLineBorder(Color.black));
             this.addMouseListener(mcelllistener);
         }
     }
 
     public static class cell extends JLabel {
 
         boolean[] numsFound;
         boolean foundNum;
         int num;
         int row;
         int column;
         int square;
 
         void incr() {
             if (num == 9) {
                 num = 0;
             } else {
                 ++num;
             }
         }
 
         cell(int i, int j) {
             numsFound = new boolean[9];
             num = 0;
             foundNum = false;
             cellListener cellistener = new cellListener();
             this.setOpaque(true);
             this.setBounds(30 * i + 10 + 5 * ((int) (Math.floor(i / 3))), (30 * j) + 40 + 5 * ((int) (Math.floor(j / 3))), 25, 25);
             this.setBackground(Color.white);
             this.setBorder(BorderFactory.createLineBorder(Color.black));
             this.addMouseListener(cellistener);
             column = i;
             row = j;
             if (row == 0 || row == 1 || row == 2) {
                 if (column == 0 || column == 1 || column == 2) {
                     square = 1;
                 } else if (column == 3 || column == 4 || column == 5) {
                     square = 2;
                 } else if (column == 6 || column == 7 || column == 8) {
                     square = 3;
                 }
             } else if (row == 3 || row == 4 || row == 5) {
                 if (column == 0 || column == 1 || column == 2) {
                     square = 4;
                 } else if (column == 3 || column == 4 || column == 5) {
                     square = 5;
                 } else if (column == 6 || column == 7 || column == 8) {
                     square = 6;
                 }
             } else if (row == 6 || row == 7 || row == 8) {
                 if (column == 0 || column == 1 || column == 2) {
                     square = 7;
                 } else if (column == 3 || column == 4 || column == 5) {
                     square = 8;
                 } else if (column == 6 || column == 7 || column == 8) {
                     square = 9;
                 }
             }
             //this.num = square;
             //this.setText(Integer.toString(num));
 
         }
     }
 }
