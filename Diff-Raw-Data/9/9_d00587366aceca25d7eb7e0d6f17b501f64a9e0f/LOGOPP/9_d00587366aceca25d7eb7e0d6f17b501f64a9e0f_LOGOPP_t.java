 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.*;
 import javax.imageio.*;
 import java.io.*;
 
 public class LOGOPP extends JFrame implements KeyListener {
 	static JTextArea prev;
 	//static JTextField cur;
     static JTextArea cur;
 	static final int PREV_HEIGHT = 100;
     static final int CHAR_HEIGHT = 20;
 	static final int CUR_HEIGHT = 60;
 	static final int MARGIN_HEIGHT = 5;
     static final int ADDITIONAL_HEIGHT = 80;
     static final int ADDITIONAL_WIDTH = 10;
 	String cmd;
 	static LOGOIO io = new LOGOIO();
 	static LOGOSymbolTable symboltable = new LOGOSymbolTable();
 	static LOGOErrorHandler errorhandler = new LOGOErrorHandler(io);
 	static LOGOCanvas canvas = new LOGOCanvas("LOGO++", 600, 400);
 	static LOGOInterpreter interpreter = new LOGOInterpreter();
 	static LOGOBasic basic = new LOGOBasic();
 
 	static ArrayList<String> commandHistory = new ArrayList<String>();
 	static int curCmdIndex = 0;
 
     static ChallengeBitmap challengeImage = new ChallengeBitmap();
     static boolean challengeOn = false;
 
 
 	public static void main(String[] args) {
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                createAndShowGUI();
             }
         });
 	}
 	
 	public LOGOPP(String name) {
 		super(name);
 	}
 
 	private static void createAndShowGUI() {
         LOGOPP logoPP = new LOGOPP("LOGOGUI");
         logoPP.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         logoPP.addComponentsToPane();
         logoPP.pack();
         logoPP.setVisible(true);
         logoPP.changeWindowSize(false);
 	//        logoPP.setResizable(false);
         LOGOTurtle tur = new LOGOTurtle("local");
         canvas.putTurtle(tur, canvas.getWidth() / 2, canvas.getHeight() / 2);
         ///////////
 	   LOGOTurtle tur2 = new LOGOTurtle("tur2");
         canvas.putTurtle(tur2, canvas.getWidth() / 2, canvas.getHeight() / 2);
 	
     }
 
     private void addComponentsToPane() {
         JPanel pane = new JPanel();
         pane.setLayout(null);
         //cur = new JTextField();
         cur = new JTextArea();
        cur.setText("");
         cur.setLineWrap(true);
         cur.addKeyListener(this);
         KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
         cur.getInputMap().put(enter, "none");
         prev = new JTextArea();
         prev.setEditable(false);
         JScrollPane scrollPane1 = new JScrollPane(prev);
         scrollPane1.setPreferredSize(new Dimension(canvas.getWidth(), PREV_HEIGHT));
         JScrollPane scrollPane2 = new JScrollPane(cur);
         scrollPane2.setPreferredSize(new Dimension(canvas.getWidth(), CUR_HEIGHT));
         canvas.setBounds(1,1,canvas.getWidth(), canvas.getHeight());
         canvas.setWindow(this);
         canvas.repaint();
         pane.add(challengeImage);
         challengeImage.setBounds(canvas.getWidth() + MARGIN_HEIGHT, 1, canvas.getWidth(), canvas.getHeight());
         pane.add(scrollPane1);
         pane.add(scrollPane2);
         pane.add(canvas);
         scrollPane1.setBounds(1, canvas.getHeight() + MARGIN_HEIGHT, canvas.getWidth(), PREV_HEIGHT);
         scrollPane2.setBounds(1, canvas.getHeight() + MARGIN_HEIGHT * 2 + PREV_HEIGHT, 
                                 canvas.getWidth(), CUR_HEIGHT);
         this.add(pane);
     }
 
     public void changeWindowSize(boolean isExpand) {
         int width = canvas.getWidth() * (isExpand?2:1) + (isExpand?MARGIN_HEIGHT:0)
                     + ADDITIONAL_WIDTH;
         int height = canvas.getHeight() + LOGOPP.PREV_HEIGHT + LOGOPP.CUR_HEIGHT
                     + ADDITIONAL_HEIGHT;
         setSize(width, height);
     }
 
     public void keyTyped(KeyEvent e) {
     }
    
     public void keyPressed(KeyEvent e) {
         switch(e.getKeyCode()) {
         case KeyEvent.VK_ENTER:
             //////////////////////
             if (e.getModifiers() == KeyEvent.CTRL_MASK) {
                 cur.append("\n");
             } else {
                 if (cur.getText().length() >=5 && cur.getText().substring(0,5).equals("tur2:")) {
                     cmd = cur.getText().substring(5);
                     canvas.changeToTurtle("tur2");
                 } else {
                     cmd = cur.getText();
                     canvas.changeToTurtle("local");
                 }
                 new Thread(){
                     public void run() {
                         execute(cmd);
                     }
                 }.start();
                 commandHistory.add(cur.getText());
                 curCmdIndex = commandHistory.size();
                 LOGOPP.io.out(">" + cur.getText());
                 cur.setText("");
                 prev.setCaretPosition(prev.getText().length());
             }
             break;
         case KeyEvent.VK_UP:
             if (e.getModifiers() == KeyEvent.ALT_MASK) {
                 curCmdIndex = (curCmdIndex > 0) ? curCmdIndex - 1 : 0;
                if (commandHistory.size() > 0 || curCmdIndex > 0) {
                    cur.setText(commandHistory.get(curCmdIndex));
                }
             }
             break;
         case KeyEvent.VK_DOWN:
             if (e.getModifiers() == KeyEvent.ALT_MASK) {
                 if (curCmdIndex < commandHistory.size() - 1) {
                     cur.setText(commandHistory.get(curCmdIndex++));
                 } else {
                     cur.setText("");
                     curCmdIndex = commandHistory.size();
                 }
             }
             break;
         }
     }
      
     public void keyReleased(KeyEvent e) {
     }
 
 	public void execute(String str) {
 		if (errorhandler.error())
 			errorhandler.errorOut();
 		errorhandler.reset();
 		try {
 			LOGONode root = interpreter.parse(str);
 			root.run();
 			canvas.repaint();
 		}
 		catch (Exception e) {
 			errorhandler.set(e.toString());
 		}
 	}
 
     public void loadChallenge(String path) {
         challengeImage.loadChallenge(path, this);
         if (challengeOn) {
             challengeImage.repaint();
             changeWindowSize(true);
         }
     }
 
     public void closeChallenge() {
         if (challengeOn) {
             challengeOn = false;
             changeWindowSize(false);
         }
     }
 }
 
 class ChallengeBitmap extends JComponent {
     private int height;
     private int width;
     private int[][] bitmap;
     private LOGOPP windowOn;
 
     public int[][] getBitmap() {return bitmap;}
     public int getHeight() {return height;}
     public int getWidth() {return width;}
 
     public void loadChallenge(String path, LOGOPP window) {
         bitmap = LOGOPP.canvas.getBitmapFromBMP(path);
         window.challengeOn = (bitmap != null);
         if (bitmap != null) {
             windowOn = window;
             height = bitmap.length;
             width = bitmap[0].length;
         }
     }
 
     public void paint(Graphics g) {
         LOGOCanvas.bmpGenerator.saveBMP("challenge.bmp",bitmap);
         
         try {
             File imageFile = new File("challenge.bmp");
             Image image = ImageIO.read(imageFile);
             ImageIcon icon = new ImageIcon(image);
             Graphics2D g2 = (Graphics2D) g;
             g2.drawImage(icon.getImage(), 0, 0, width, height, this);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
