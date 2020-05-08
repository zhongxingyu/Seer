 package pl.shockah.shocky.console;
 
 import pl.shockah.shocky.StoppableThread;
 import com.googlecode.lanterna.TerminalFacade;
 import com.googlecode.lanterna.input.Key;
 import com.googlecode.lanterna.input.Key.Kind;
 import com.googlecode.lanterna.screen.Screen;
 import com.googlecode.lanterna.screen.ScreenWriter;
 
 public class ConsoleThread extends StoppableThread {
 	public class TextInputHandler {
 		private StringBuilder sb;
 		private int pos;
 		
 		public TextInputHandler() {
 			clear();
 		}
 		
 		public String toString() {
 			return sb.toString();
 		}
 		public int getPosition() {
 			return pos;
 		}
 		
 		public void clear() {
 			sb = new StringBuilder();
 			pos = 0;
 		}
 		
 		public void handle(Key key) {
 			if (key.getKind() == Kind.NormalKey && key.getCharacter() == 127) {
 				if (pos == sb.length()) return;
 				sb.delete(pos,pos+1);
 			} else if (key.getKind() == Kind.Backspace) {
 				if (pos == 0) return;
 				sb.delete(pos-1,pos);
 				pos--;
 			} else if (key.getKind() == Kind.Home) {
 				pos = 0;
 			} else if (key.getKind() == Kind.End) {
 				pos = sb.length();
 			} else if (key.getKind() == Kind.ArrowLeft) {
 				pos = Math.max(pos-1,0);;
 			} else if (key.getKind() == Kind.ArrowRight) {
 				pos = Math.min(pos+1,sb.length());
 			} else if (key.getKind() == Kind.NormalKey) {
 				sb.replace(pos,pos,""+key.getCharacter());
 				pos++;
 			}
 		}
 	}
 	
 	public void onRun() {
 		Screen screen = TerminalFacade.createScreen();
 		if (screen == null) return;
 		
		setPriority(Thread.NORM_PRIORITY-1);
 		ScreenWriter sw = new ScreenWriter(screen);
 		
 		TextInputHandler tih = new TextInputHandler();
 		screen.startScreen();
 		while (running) {
 			sw.fillScreen(' ');
 			
			Key key = screen.readInput();
			if (key != null) tih.handle(key);
 			
 			String input = tih.toString();
 			sw.drawString(0,screen.getTerminalSize().getRows()-1,input);
 			
 			screen.setCursorPosition(tih.getPosition(),screen.getTerminalSize().getRows()-1);
 			screen.refresh();
 			
 			try {
 				Thread.sleep(10);
 			} catch (Exception e) {}
 		}
 		screen.stopScreen();
 	}
 }
