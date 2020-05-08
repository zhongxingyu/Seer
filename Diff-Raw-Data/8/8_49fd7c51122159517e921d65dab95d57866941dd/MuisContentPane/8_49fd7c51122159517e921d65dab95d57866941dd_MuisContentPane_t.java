 /* Created Mar 23, 2009 by Andrew */
 package org.muis.browser;
 
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 import org.muis.core.event.KeyBoardEvent.KeyCode;
 import org.muis.core.event.MouseEvent.MouseEventType;
 
 /** An AWT component that renders a MUIS document */
 public class MuisContentPane extends java.awt.Component
 {
 	private org.muis.core.MuisDocument theContent;
 
 	/** Creates a MuisContentPane */
 	public MuisContentPane()
 	{
 		super();
 		setFocusable(true);
 		addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(ComponentEvent e)
 			{
 				super.componentResized(e);
 				if(getContent() != null)
 				{
 					getContent().setSize(getWidth(), getHeight());
 				}
 			}
 		});
 		addMouseListener(new java.awt.event.MouseListener() {
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
				requestFocusInWindow();
 				moused(MouseEventType.pressed, e);
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e)
 			{
 				moused(MouseEventType.released, e);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				moused(MouseEventType.clicked, e);
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e)
 			{
 				moused(MouseEventType.entered, e);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e)
 			{
 				moused(MouseEventType.exited, e);
 			}
 		});
 		addMouseMotionListener(new java.awt.event.MouseMotionListener() {
 			@Override
 			public void mouseDragged(MouseEvent e)
 			{
 				moused(MouseEventType.moved, e);
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent e)
 			{
 				moused(MouseEventType.moved, e);
 			}
 		});
 		addMouseWheelListener(new java.awt.event.MouseWheelListener() {
 			@Override
 			public void mouseWheelMoved(java.awt.event.MouseWheelEvent e)
 			{
 				scrolled(e);
 			}
 		});
 		addKeyListener(new java.awt.event.KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e)
 			{
 				keyed(Boolean.TRUE, e);
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e)
 			{
 				keyed(Boolean.FALSE, e);
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e)
 			{
 				keyed(null, e);
 			}
 		});
 	}
 
 	/** @return The document that is currently being rendered by this content pane */
 	public org.muis.core.MuisDocument getContent()
 	{
 		return theContent;
 	}
 
 	/**
 	 * Sets this pane's content
 	 * 
 	 * @param doc The document to render
 	 */
 	public void setContent(org.muis.core.MuisDocument doc)
 	{
 		theContent = doc;
 		theContent.setSize(getWidth(), getHeight());
 	}
 
 	@Override
 	public void paint(java.awt.Graphics g)
 	{
 		if(theContent != null)
 			theContent.paint((java.awt.Graphics2D) g);
 	}
 
 	void moused(org.muis.core.event.MouseEvent.MouseEventType type, MouseEvent evt)
 	{
 		if(theContent == null)
 			return;
 		org.muis.core.event.MouseEvent.ButtonType buttonType;
 		switch (evt.getButton())
 		{
 		case MouseEvent.NOBUTTON:
 			buttonType = org.muis.core.event.MouseEvent.ButtonType.NONE;
 			break;
 		case MouseEvent.BUTTON1:
 			buttonType = org.muis.core.event.MouseEvent.ButtonType.LEFT;
 			break;
 		case MouseEvent.BUTTON2:
 			buttonType = org.muis.core.event.MouseEvent.ButtonType.MIDDLE;
 			break;
 		case MouseEvent.BUTTON3:
 			buttonType = org.muis.core.event.MouseEvent.ButtonType.RIGHT;
 			break;
 		default:
 			buttonType = org.muis.core.event.MouseEvent.ButtonType.OTHER;
 			break;
 		}
 		theContent.mouse(evt.getX(), evt.getY(), type, buttonType, evt.getClickCount());
 	}
 
 	void scrolled(java.awt.event.MouseWheelEvent evt)
 	{
 		if(theContent == null)
 			return;
 		theContent.scroll(evt.getX(), evt.getY(), evt.getWheelRotation());
 	}
 
 	void keyed(Boolean pressed, KeyEvent evt)
 	{
 		if(theContent == null)
 			return;
 		if(pressed != null)
 			theContent.keyed(getKeyCodeFromAWT(evt.getKeyCode(), evt.getKeyLocation()), pressed.booleanValue());
 		else
 			theContent.character(evt.getKeyChar());
 	}
 
 	/**
 	 * @param keyCode The key code (see java.awt.KeyEvent.VK_*, {@link KeyEvent#getKeyCode()})
 	 * @param keyLocation The key's location (see java.awt.KeyEvent.KEY_LOCATION_*, {@link KeyEvent#getKeyLocation()}
 	 * @return The MUIS key code for the AWT codes
 	 */
 	public static KeyCode getKeyCodeFromAWT(int keyCode, int keyLocation)
 	{
 		switch (keyCode)
 		{
 		case KeyEvent.VK_ENTER:
 			return KeyCode.ENTER;
 		case KeyEvent.VK_BACK_SPACE:
 			return KeyCode.BACKSPACE;
 		case KeyEvent.VK_TAB:
 			return KeyCode.TAB;
 		case KeyEvent.VK_CANCEL:
 			return KeyCode.CANCEL;
 		case KeyEvent.VK_CLEAR:
 			return KeyCode.CLEAR;
 		case KeyEvent.VK_SHIFT:
 			if(keyLocation == KeyEvent.KEY_LOCATION_LEFT)
 				return KeyCode.SHIFT_LEFT;
 			else
 				return KeyCode.SHIFT_RIGHT;
 		case KeyEvent.VK_CONTROL:
 			if(keyLocation == KeyEvent.KEY_LOCATION_LEFT)
 				return KeyCode.CTRL_LEFT;
 			else
 				return KeyCode.CTRL_RIGHT;
 		case KeyEvent.VK_ALT:
 			if(keyLocation == KeyEvent.KEY_LOCATION_LEFT)
 				return KeyCode.ALT_LEFT;
 			else
 				return KeyCode.ALT_RIGHT;
 		case KeyEvent.VK_PAUSE:
 			return KeyCode.PAUSE;
 		case KeyEvent.VK_CAPS_LOCK:
 			return KeyCode.CAPS_LOCK;
 		case KeyEvent.VK_ESCAPE:
 			return KeyCode.ESCAPE;
 		case KeyEvent.VK_SPACE:
 			return KeyCode.SPACE;
 		case KeyEvent.VK_PAGE_UP:
 			return KeyCode.PAGE_UP;
 		case KeyEvent.VK_PAGE_DOWN:
 			return KeyCode.PAGE_DOWN;
 		case KeyEvent.VK_END:
 			return KeyCode.END;
 		case KeyEvent.VK_HOME:
 			return KeyCode.HOME;
 		case KeyEvent.VK_LEFT:
 			return KeyCode.LEFT_ARROW;
 		case KeyEvent.VK_UP:
 			return KeyCode.UP_ARROW;
 		case KeyEvent.VK_RIGHT:
 			return KeyCode.RIGHT_ARROW;
 		case KeyEvent.VK_DOWN:
 			return KeyCode.DOWN_ARROW;
 		case KeyEvent.VK_COMMA:
 		case KeyEvent.VK_LESS:
 			return KeyCode.COMMA;
 		case KeyEvent.VK_MINUS:
 			if(keyLocation == KeyEvent.KEY_LOCATION_NUMPAD)
 				return KeyCode.PAD_MINUS;
 			else
 				return KeyCode.MINUS;
 		case KeyEvent.VK_UNDERSCORE:
 			return KeyCode.MINUS;
 		case KeyEvent.VK_PERIOD:
 			if(keyLocation == KeyEvent.KEY_LOCATION_NUMPAD)
 				return KeyCode.PAD_DOT;
 			else
 				return KeyCode.DOT;
 		case KeyEvent.VK_GREATER:
 			return KeyCode.DOT;
 		case KeyEvent.VK_SLASH:
 			if(keyLocation == KeyEvent.KEY_LOCATION_NUMPAD)
 				return KeyCode.PAD_SLASH;
 			else
 				return KeyCode.FORWARD_SLASH;
 		case KeyEvent.VK_0:
 		case KeyEvent.VK_RIGHT_PARENTHESIS:
 			return KeyCode.NUM_0;
 		case KeyEvent.VK_1:
 		case KeyEvent.VK_EXCLAMATION_MARK:
 			return KeyCode.NUM_1;
 		case KeyEvent.VK_2:
 		case KeyEvent.VK_AT:
 			return KeyCode.NUM_2;
 		case KeyEvent.VK_3:
 		case KeyEvent.VK_NUMBER_SIGN:
 			return KeyCode.NUM_3;
 		case KeyEvent.VK_4:
 		case KeyEvent.VK_DOLLAR:
 			return KeyCode.NUM_4;
 		case KeyEvent.VK_5:
 			return KeyCode.NUM_5;
 		case KeyEvent.VK_6:
 		case KeyEvent.VK_CIRCUMFLEX:
 			return KeyCode.NUM_6;
 		case KeyEvent.VK_7:
 		case KeyEvent.VK_AMPERSAND:
 			return KeyCode.NUM_7;
 		case KeyEvent.VK_8:
 		case KeyEvent.VK_ASTERISK:
 			return KeyCode.NUM_8;
 		case KeyEvent.VK_9:
 		case KeyEvent.VK_LEFT_PARENTHESIS:
 			return KeyCode.NUM_9;
 		case KeyEvent.VK_SEMICOLON:
 		case KeyEvent.VK_COLON:
 			return KeyCode.SEMICOLON;
 		case KeyEvent.VK_EQUALS:
 			if(keyLocation == KeyEvent.KEY_LOCATION_NUMPAD)
 				return KeyCode.PAD_EQUAL;
 			else
 				return KeyCode.EQUAL;
 		case KeyEvent.VK_A:
 			return KeyCode.A;
 		case KeyEvent.VK_B:
 			return KeyCode.B;
 		case KeyEvent.VK_C:
 			return KeyCode.C;
 		case KeyEvent.VK_D:
 			return KeyCode.D;
 		case KeyEvent.VK_E:
 			return KeyCode.E;
 		case KeyEvent.VK_F:
 			return KeyCode.F;
 		case KeyEvent.VK_G:
 			return KeyCode.G;
 		case KeyEvent.VK_H:
 			return KeyCode.H;
 		case KeyEvent.VK_I:
 			return KeyCode.I;
 		case KeyEvent.VK_J:
 			return KeyCode.J;
 		case KeyEvent.VK_K:
 			return KeyCode.K;
 		case KeyEvent.VK_L:
 			return KeyCode.L;
 		case KeyEvent.VK_M:
 			return KeyCode.M;
 		case KeyEvent.VK_N:
 			return KeyCode.N;
 		case KeyEvent.VK_O:
 			return KeyCode.O;
 		case KeyEvent.VK_P:
 			return KeyCode.P;
 		case KeyEvent.VK_Q:
 			return KeyCode.Q;
 		case KeyEvent.VK_R:
 			return KeyCode.R;
 		case KeyEvent.VK_S:
 			return KeyCode.S;
 		case KeyEvent.VK_T:
 			return KeyCode.T;
 		case KeyEvent.VK_U:
 			return KeyCode.U;
 		case KeyEvent.VK_V:
 			return KeyCode.V;
 		case KeyEvent.VK_W:
 			return KeyCode.W;
 		case KeyEvent.VK_X:
 			return KeyCode.X;
 		case KeyEvent.VK_Y:
 			return KeyCode.Y;
 		case KeyEvent.VK_Z:
 			return KeyCode.Z;
 		case KeyEvent.VK_OPEN_BRACKET:
 		case KeyEvent.VK_BRACELEFT:
 			return KeyCode.LEFT_BRACE;
 		case KeyEvent.VK_BACK_SLASH:
 			return KeyCode.BACK_SLASH;
 		case KeyEvent.VK_CLOSE_BRACKET:
 		case KeyEvent.VK_BRACERIGHT:
 			return KeyCode.RIGHT_BRACE;
 		case KeyEvent.VK_NUMPAD0:
 			return KeyCode.PAD_0;
 		case KeyEvent.VK_NUMPAD1:
 			return KeyCode.PAD_1;
 		case KeyEvent.VK_NUMPAD2:
 		case KeyEvent.VK_KP_DOWN:
 			return KeyCode.PAD_2;
 		case KeyEvent.VK_NUMPAD3:
 			return KeyCode.PAD_3;
 		case KeyEvent.VK_NUMPAD4:
 		case KeyEvent.VK_KP_LEFT:
 			return KeyCode.PAD_4;
 		case KeyEvent.VK_NUMPAD5:
 			return KeyCode.PAD_5;
 		case KeyEvent.VK_NUMPAD6:
 		case KeyEvent.VK_KP_RIGHT:
 			return KeyCode.PAD_6;
 		case KeyEvent.VK_NUMPAD7:
 			return KeyCode.PAD_7;
 		case KeyEvent.VK_NUMPAD8:
 		case KeyEvent.VK_KP_UP:
 			return KeyCode.PAD_8;
 		case KeyEvent.VK_NUMPAD9:
 			return KeyCode.PAD_9;
 		case KeyEvent.VK_MULTIPLY:
 			return KeyCode.PAD_MULTIPLY;
 		case KeyEvent.VK_ADD:
 			return KeyCode.PAD_PLUS;
 		case KeyEvent.VK_SEPARATOR:
 			return KeyCode.PAD_SEPARATOR;
 		case KeyEvent.VK_SUBTRACT:
 			return KeyCode.PAD_MINUS;
 		case KeyEvent.VK_DECIMAL:
 			return KeyCode.PAD_DOT;
 		case KeyEvent.VK_DIVIDE:
 			return KeyCode.PAD_SLASH;
 		case KeyEvent.VK_DELETE:
 			return KeyCode.PAD_BACKSPACE;
 		case KeyEvent.VK_NUM_LOCK:
 			return KeyCode.NUM_LOCK;
 		case KeyEvent.VK_SCROLL_LOCK:
 			return KeyCode.SCROLL_LOCK;
 		case KeyEvent.VK_F1:
 			return KeyCode.F1;
 		case KeyEvent.VK_F2:
 			return KeyCode.F2;
 		case KeyEvent.VK_F3:
 			return KeyCode.F3;
 		case KeyEvent.VK_F4:
 			return KeyCode.F4;
 		case KeyEvent.VK_F5:
 			return KeyCode.F5;
 		case KeyEvent.VK_F6:
 			return KeyCode.F6;
 		case KeyEvent.VK_F7:
 			return KeyCode.F7;
 		case KeyEvent.VK_F8:
 			return KeyCode.F8;
 		case KeyEvent.VK_F9:
 			return KeyCode.F9;
 		case KeyEvent.VK_F10:
 			return KeyCode.F10;
 		case KeyEvent.VK_F11:
 			return KeyCode.F11;
 		case KeyEvent.VK_F12:
 			return KeyCode.F12;
 		case KeyEvent.VK_F13:
 			return KeyCode.F13;
 		case KeyEvent.VK_F14:
 			return KeyCode.F14;
 		case KeyEvent.VK_F15:
 			return KeyCode.F15;
 		case KeyEvent.VK_F16:
 			return KeyCode.F16;
 		case KeyEvent.VK_F17:
 			return KeyCode.F17;
 		case KeyEvent.VK_F18:
 			return KeyCode.F18;
 		case KeyEvent.VK_F19:
 			return KeyCode.F19;
 		case KeyEvent.VK_F20:
 			return KeyCode.F20;
 		case KeyEvent.VK_F21:
 			return KeyCode.F21;
 		case KeyEvent.VK_F22:
 			return KeyCode.F22;
 		case KeyEvent.VK_F23:
 			return KeyCode.F23;
 		case KeyEvent.VK_F24:
 			return KeyCode.F24;
 		case KeyEvent.VK_PRINTSCREEN:
 			return KeyCode.PRINT_SCREEN;
 		case KeyEvent.VK_INSERT:
 			return KeyCode.INSERT;
 		case KeyEvent.VK_HELP:
 			return KeyCode.HELP;
 		case KeyEvent.VK_META:
 			return KeyCode.META;
 		case KeyEvent.VK_BACK_QUOTE:
 			return KeyCode.BACK_QUOTE;
 		case KeyEvent.VK_QUOTE:
 		case KeyEvent.VK_QUOTEDBL:
 			return KeyCode.QUOTE;
 		case KeyEvent.VK_WINDOWS:
 			return KeyCode.COMMAND_KEY;
 		case KeyEvent.VK_CONTEXT_MENU:
 			return KeyCode.CONTEXT_MENU;
 		default:
 			return null;
 		}
 	}
 }
