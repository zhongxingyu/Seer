 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package counter;
 
 import javax.microedition.midlet.*;
 import javax.microedition.lcdui.*;
 
 /**
  * @author Adam
  */
 public class Counter 
 extends CustomItem
 implements ItemCommandListener {
     public int current_value ;
     private Command command_inc;
     public Counter(int value) {
         super("" + value);
         current_value = value;
     }
     public void initialize () {
         setItemCommandListener(this);
         command_inc = new Command("Count", Command.OK, 4);
         setDefaultCommand(command_inc);
     }
     public void setValue (int value) {
         setLabel("" + value);
     }
     protected void paint (Graphics g, int x, int y) {
         int x_g    = g.getClipX();
         int y_g    = g.getClipY();
         int width  = g.getClipWidth();
         int height = g.getClipHeight();
         g.setColor(Display.COLOR_BACKGROUND);
         g.fillRect(x_g, y_g, width, height);
     }
     protected int getPrefContentWidth(int height) {
         Font working_font = Font.getDefaultFont();
         return working_font.stringWidth("" + current_value);
     }
     protected int getMinContentWidth() {
         return 0;
     }
     protected int getPrefContentHeight(int width) {
         return 0;
     }
     protected int getMinContentHeight() {
         return 0;
     }
     public void pointerPressed(int x, int y) {
         setValue(++current_value);
     }
     public void commandAction(Command c, Item item) {
//        if (c == command_inc) {
//            setValue(++current_value);
//        }
     }
 }
