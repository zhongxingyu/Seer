 package event_handler;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 public class InteractionEvent {
 
 		public static int MOUSE=1;
 		public static int KYB = 0;
 		private MouseEvent me;
 		private KeyEvent ke;
 		private int type;
 		/**
 		 * 
 		 * @param type type of event which triggered this
 		 * @param m - the mouse event to handle
 		 * @param e  - the key event too handle
 		 */
 		public InteractionEvent(int type, MouseEvent m, KeyEvent e) {
 			this.type = type;
			MouseEvent me = m;
			KeyEvent ke = e;
 			
 		}
 		
 		public MouseEvent getMouseEvent(){
 			return me;
 		}
 		public KeyEvent getKeyEvent(){
 			return ke;
 		}
 		public int getType() {
 			return type;
 		}
 }
