 package vialab.SMT;
 
 import java.awt.event.KeyEvent;
 
 import processing.core.PFont;
 
 /**
  * TextZone displays text, and is selectable by touch, each word is
  * independently highlighted by touch, toggled whenever a TouchDown occurs on
  * the word
  */
 public class TextZone extends Zone {
 
 	private class WordZone extends Zone {
 
 		public String word;
 		private boolean selected = false;
 
 		@SuppressWarnings("unused")
 		public WordZone(WordZone original) {
 			super(original.x, original.y, original.width, original.height);
 			this.word = original.word;
 			this.setDirect(true);
 			this.selected = original.selected;
 		}
 
 		public WordZone(int x, int y, int width, int height) {
 			super(x, y, width, height);
 			this.word = "";
 			TextZone.this.add(this);
 			this.setDirect(true);
 		}
 
 		@Override
 		public void touchMovedImpl(Touch touch) {
 			if (touch.getLastPoint() != null && !word.matches("\\s*") && selectable) {
 				if (touch.getLastPoint().x < touch.x) {
 					// moving right, so highlight/unblur by setting selected to
 					// true
 					selected = true;
 				}
 				else if (touch.getLastPoint().x > touch.x) {
 					// moving left, so unhighlight/blur by setting selected to
 					// false
 					selected = false;
 				}
 			}
 		}
 
 		@Override
 		public void drawImpl() {
 			textSize(fontSize);
 			fill(255);
 			if (blur) {
 				textFont(sFont);
 				if (selected) {
 					textFont(font);
 				}
 				textSize(fontSize);
 				if (width > 0) {
 					noStroke();
 					rect(0, 0, width, height);
 				}
 				fill(0);
 				text(this.word, 0, 0, width, height);
 			}
 			else {
 				if (selected) {
 					fill(0, 0, 255, 127);
 				}
 				if (width > 0) {
 					noStroke();
 					rect(0, 0, width, height);
 				}
 				fill(0);
 				text(this.word, 0, 0, width, height);
 			}
 		}
 
 	}
 
 	private boolean selectable = false;
 
 	private String inputText;
 
 	private WordZone currentWordZone;
 
 	private PFont font = applet.createFont("SansSerif", 16);
 
 	private PFont sFont = applet.createFont("SansSerif", 3.0f);
 
 	private boolean blur = false;
 
 	private boolean keysFromApplet = false;
 
 	private float fontSize = 16;
 
 	public TextZone(TextZone original) {
 		super(original.x, original.y, original.width, original.height);
 		this.currentWordZone = (WordZone) original.currentWordZone.clone();
 		this.inputText = original.inputText;
 		this.blur = original.blur;
 		this.selectable = original.selectable;
 		this.keysFromApplet = original.keysFromApplet;
 		if (this.keysFromApplet) {
 			applet.registerMethod("keyEvent", this);
 		}
 	}
 
 	public TextZone(int x, int y, int width, int height, String inputText, boolean selectable,
 			boolean blur) {
 		this(x, y, width, height, inputText, selectable, blur, 16);
 	}
 
 	public TextZone(int x, int y, int width, int height, String inputText, boolean selectable,
 			boolean blur, float fontSize) {
 		super(x, y, width, height);
 		this.currentWordZone = new WordZone(0, 0, 0, 20);
 		this.inputText = inputText;
 		this.selectable = selectable;
 		this.blur = blur;
 		this.fontSize = fontSize;
 
 		if (fontSize != 16) {
 			font = applet.createFont("SansSerif", fontSize);
 		}
 
 		for (char c : inputText.toCharArray()) {
 			this.addChar(c);
 		}
 	}
 
 	public TextZone(int x, int y, int width, int height) {
 		this(null, x, y, width, height, false);
 	}
 
 	public TextZone(int x, int y, int width, int height, boolean keysRecievedFromApplet) {
 		this(null, x, y, width, height, keysRecievedFromApplet);
 	}
 
 	public TextZone(String name, int x, int y, int width, int height) {
 		this(name, x, y, width, height, false);
 	}
 
 	public TextZone(String name, int x, int y, int width, int height, boolean keysRecievedFromApplet) {
 		super(name, x, y, width, height);
 		this.currentWordZone = new WordZone(0, 0, 0, 20);
 		this.keysFromApplet = keysRecievedFromApplet;
 		if (keysRecievedFromApplet) {
 			applet.registerMethod("keyEvent", this);
 		}
 	}
 
 	@Override
 	public void drawImpl() {
 		fill(255);
 		noStroke();
 		rect(0, 0, width, height);
 	}
 
 	public void addChar(char c) {
 		if (c == ' ') {
 			this.currentWordZone = new WordZone(currentWordZone.x + currentWordZone.width,
 					currentWordZone.y, 0, (int) (fontSize * (20.0 / 16.0)));
 			currentWordZone.word += " ";
 		}
 		else if (c == '\t') {
 			this.currentWordZone = new WordZone(currentWordZone.x + currentWordZone.width,
 					currentWordZone.y, 0, (int) (fontSize * (20.0 / 16.0)));
 			currentWordZone.word += "    ";
 		}
 		else if (c == '\n') {
 			this.currentWordZone = new WordZone(0, currentWordZone.y
 					+ (int) (fontSize * (20.0 / 16.0)), 0, (int) (fontSize * (20.0 / 16.0)));
 		}
 		else {
 			if (currentWordZone.word.trim().equals("")) {
 				currentWordZone = new WordZone(currentWordZone.x + currentWordZone.width,
 						currentWordZone.y, 0, (int) (fontSize * (20.0 / 16.0)));
 			}
			if (currentWordZone.x + currentWordZone.width + fontSize > width && currentWordZone.x != 0) {
 				this.currentWordZone.setData(0, currentWordZone.y
 						+ (int) (fontSize * (20.0 / 16.0)), 0, (int) (fontSize * (20.0 / 16.0)));
 			}
			if(currentWordZone.x + currentWordZone.width + fontSize <= width){
				this.currentWordZone.word += c;
			}
 		}
 		pushStyle();
 		textSize(fontSize);
 		currentWordZone.width = (int) Math.ceil(textWidth(currentWordZone.word));
 		popStyle();
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		this.addChar(e.getKeyChar());
 		super.keyTyped(e);
 	}
 
 	/**
 	 * This method is for use by Processing, override it to change what occurs
 	 * when a Processing KeyEvent is passed to the TextZone
 	 * 
 	 * @param event
 	 *            The Processing KeyEvent that the textZone will use to change
 	 *            its state
 	 */
 	public void keyEvent(processing.event.KeyEvent event) {
 		KeyEvent nevent = (KeyEvent) event.getNative();
 		switch (nevent.getID()) {
 		case KeyEvent.KEY_RELEASED:
 			keyReleased(nevent);
 			break;
 		case KeyEvent.KEY_TYPED:
 			keyTyped(nevent);
 			break;
 		case KeyEvent.KEY_PRESSED:
 			keyPressed(nevent);
 			break;
 		}
 	}
 }
