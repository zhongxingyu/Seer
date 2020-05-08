 package NothingHere;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author C0ldF0x
  * All rights reserved.
  */
 public class Handler extends JFrame {
 
     int nBox=6;
     
     Box[][] grid = new Box[nBox][nBox];
     int remaining = nBox * nBox;
     Box lastSelected=null;
     int score = 0;
     JLabel scoreInfo = new JLabel(("Score: "+score));
     
     JMenuBar bar = new JMenuBar();
     
     Handler() throws InterruptedException {
         Container c = getContentPane();
         c.setBackground(Color.LIGHT_GRAY);
         GridLayout gl = new GridLayout(nBox, nBox, 2, 2);
         c.setLayout(gl);
         this.setJMenuBar(bar);
         bar.add(scoreInfo);
         
         scoreInfo.setText(("Score: "+score));
         for (int i = 0; i < nBox; i++) {
             for (int k = 0; k < nBox; k++) {
                 grid[i][k] = new Box();
                 grid[i][k].addMouseListener(new MouseListener() {
                     
                     
                     @Override
                     public void mouseClicked(MouseEvent e) {
                         Box aux = ((Box)e.getSource());
                         
                         if(lastSelected==null && aux.getBackground()!=Color.white){
                             lastSelected=aux;
                             lastSelected.setBackground(lastSelected.color.darker().darker());
                         }else if(lastSelected!=null && aux.getBackground()==Color.white){
                             aux.setBackground(lastSelected.color);
                             aux.color=lastSelected.color;
                             lastSelected.color=Color.white;
                             lastSelected.setBackground(lastSelected.color);
                             lastSelected=null;
                         }else if(lastSelected!=null && aux.getBackground()!=Color.white){
                             lastSelected.setBackground(lastSelected.color);
                             lastSelected=aux;
                             lastSelected.setBackground(aux.color.darker().darker());
                             
                         }   
                         
                         if(!checkRowH() && !checkRowV() && !checkRowDiagonalR() && !checkRowDiagonalL() && lastSelected==null && aux.color!=Color.white)
                         addBoxs();
                     }
 
                     @Override
                     public void mousePressed(MouseEvent e) {
                         System.out.println("press");
                     }
 
                     @Override
                     public void mouseReleased(MouseEvent e) {
                         System.out.println("release");
                     }
 
                     @Override
                     public void mouseEntered(MouseEvent e) {
                         System.out.println("enter");
                     }
 
                     @Override
                     public void mouseExited(MouseEvent e) {
                         System.out.println("exit");
                     }
                 });
                 add(grid[i][k]);
             }
         }
 
         init();
         
         this.addKeyListener(new KeyListener(){
 
             @Override
             public void keyTyped(KeyEvent e) {
                 
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_SPACE){
                     
                 }
             }
         });
         
         setSize(300, 300);
         setVisible(true);
         //setResizable(false);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
 
     }
 
     public void init() {
         for (int i = 0; i < nBox; i++) {
             for (int k = 0; k < nBox; k++) {
                 grid[i][k].color=Color.white;
                 grid[i][k].selected=false;
                 grid[i][k].setBackground(Color.white);
             }
         }
        
         int left = 4, i, k;
         do {
             i = (int) (Math.random() * nBox);
             k = (int) (Math.random() * nBox);
             if (grid[i][k].getBackground() == Color.white) {
                 grid[i][k].colorize();
                 left--;
                 remaining--;
             }
         } while (left != 0);
     }
 
     public void addBoxs(){
        int left = 3, i, k;
         do {
             i = (int) (Math.random() * nBox);
             k = (int) (Math.random() * nBox);
             if (grid[i][k].getBackground() == Color.white) {
                 grid[i][k].colorize();
                 left--;
                 remaining--;
             }
         } while (left != 0 && remaining > 0);
         checkRowV();    
         checkRowH();
         checkRowDiagonalR();
         checkRowDiagonalL();
         int op=-1;
         if(remaining==0){
             op=JOptionPane.showConfirmDialog(null, "Game Over, no more moves!\nScore: "+score+"\nTry again?", "Game info", JOptionPane.YES_NO_OPTION, JOptionPane.CLOSED_OPTION);
          if(op==0)
             init();
         else
             System.exit(0);
         }
           //  } while (remaining > 0);
        
     }
 
     
     
     public boolean checkRowV() {
         for (int i = 1; i < nBox-1; i++) {
             for (int k = 0; k < nBox; k++) {
                 if(grid[i][k].getBackground()==grid[i-1][k].getBackground() && grid[i][k].getBackground()==grid[i+1][k].getBackground() && grid[i][k].getBackground()!=Color.white){
                     grid[i][k].setBackground(Color.white);
                     grid[i][k].color=Color.white;
                     grid[i-1][k].setBackground(Color.white);
                     grid[i-1][k].color=Color.white;
                     grid[i+1][k].setBackground(Color.white);
                     grid[i+1][k].color=Color.white;
                     remaining+=3;
                     score+=3;
                     scoreInfo.setText(("Score: "+score));
                     return true;
                 }
             }
         }
         return false;
     }
     
     public boolean checkRowH(){
         for (int i = 0; i < nBox; i++) {
             for (int k = 1; k < nBox-1; k++) {
                 if(grid[i][k].getBackground()==grid[i][k-1].getBackground() && grid[i][k].getBackground()==grid[i][k+1].getBackground() && grid[i][k].getBackground()!=Color.white){
                     grid[i][k].setBackground(Color.white);
                     grid[i][k].color=Color.white;
                     grid[i][k-1].setBackground(Color.white);
                     grid[i][k-1].color=Color.white;
                     grid[i][k+1].setBackground(Color.white);
                     grid[i][k+1].color=Color.white;
                     remaining+=3;
                     score+=3;
                     scoreInfo.setText(("Score: "+score));
                     return true;
                 }
             }
         } 
         return false;
     }
     
     public boolean checkRowDiagonalL(){
         for (int i = 1; i < nBox-1; i++) {
             for (int k = 1; k < nBox-1; k++) {
                 if(grid[i][k].getBackground()==grid[i-1][k-1].getBackground() && grid[i][k].getBackground()==grid[i+1][k+1].getBackground() && grid[i][k].getBackground()!=Color.white){
                     grid[i][k].setBackground(Color.white);
                     grid[i][k].color=Color.white;
                     grid[i-1][k-1].setBackground(Color.white);
                     grid[i-1][k-1].color=Color.white;
                     grid[i+1][k+1].setBackground(Color.white);
                     grid[i+1][k+1].color=Color.white;
                     remaining+=3;
                     score+=3;
                     scoreInfo.setText(("Score: "+score));
                     return true;
             }
         }
     
         }
         return false;
     }
     
     public boolean checkRowDiagonalR(){
         for (int i = 1; i < nBox-1; i++) {
             for (int k = 1; k < nBox-1; k++) {
                 if(grid[i][k].getBackground()==grid[i+1][k-1].getBackground() && grid[i][k].getBackground()==grid[i-1][k+1].getBackground() && grid[i][k].getBackground()!=Color.white){
                     grid[i][k].setBackground(Color.white);
                     grid[i][k].color=Color.white;
                     grid[i+1][k-1].setBackground(Color.white);
                     grid[i+1][k-1].color=Color.white;
                     grid[i-1][k+1].setBackground(Color.white);
                     grid[i-1][k+1].color=Color.white;
                     remaining+=3;
                     score+=3;
                     scoreInfo.setText(("Score: "+score));
                     return true;
             }
         }
     
         }
         return false;
     } 
     
     public static void main(String args[]) throws InterruptedException {
         Handler handler = new Handler();
         
         
         System.out.println("Done");
         
     }
 }
