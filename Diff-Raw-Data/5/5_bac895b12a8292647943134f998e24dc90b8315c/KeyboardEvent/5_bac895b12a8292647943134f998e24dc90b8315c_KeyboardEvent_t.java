 package com.tigervnc.rfb.message;
 
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.tigervnc.rfb.Encodings;
 import com.tigervnc.rfb.RfbUtil;
 import com.tigervnc.vncviewer.Util;
 
 public class KeyboardEvent implements IServerMessage {
 
 	public class KeyUndefinedException extends Exception {}
 
 	// Set from the main vnc response loop VncViewer, refactor that...
 	public static boolean extended_key_event = false;
 
 	/** Maps from keycode to keysym for all currently pressed keys. */
 	private static Map<Integer, Integer> keys_pressed = new HashMap<Integer, Integer>();
 
 	/** Get all pressed keys as a map from keycode to keysym */
 	public static Map<Integer, Integer> getPressedKeys() throws IOException {
 		return keys_pressed;
 	}
 
 	/** Clear all pressed keys */
 	public static void clearPressedKeys() {
 		keys_pressed.clear();
 	}
 	
 	public static final int X11_BACK_SPACE = 0xff08;
 	public static int X11_TAB = 0xff09;
 	public static int X11_ENTER = 0xff0d;
 	public static int X11_ESCAPE = 0xff1b;
 	public static final int X11_ALT = 0xffe9;
 	public static final int X11_ALT_GRAPH = 0xff7e;
 	public static final int X11_CONTROL = 0xffe3;
 	public static final int X11_SHIFT = 0xffe1;
 	public static final int VK_META = 0xffe7;
 
 	protected int _keysym;
 	protected int _keycode;
 	protected boolean _press;
 	protected List<KeyboardEvent> _extra_preceding_events;
 
 	protected static boolean _alt_gr_pressed = false;
 	private boolean bypass_original_event = false;
 
 	public KeyboardEvent(KeyEvent evt) throws KeyUndefinedException {
 		_keycode = evt.getKeyCode();
 		_keysym = evt.getKeyChar();
 		_press = (evt.getID() == KeyEvent.KEY_PRESSED);
 
 		if (_keycode == KeyEvent.VK_UNDEFINED) {
 			throw new KeyUndefinedException();
 		}
 		handleShortcuts(evt);
 		handlePecularaties(evt);
 		
 		// only important if not using extended key events
 		// the kvm vnc client ignores the keysym.
 		// wonder why the keysym is required in that case.
 		if(extended_key_event){
 			handleUndefinedJavaKeysymsConvert2x11(_keycode);
 		}
 	}
 
 	public KeyboardEvent(int keysym, int keycode, boolean down) {
 		this._keysym = keysym;
 		this._keycode = keycode;
 		this._press = down;
 		if(extended_key_event){
 			handleUndefinedJavaKeysymsConvert2x11(keycode);
 		}
 	}
 
 	/**
 	 * Handles shortcuts in the client.
 	 * 
 	 * Ctrl-Alt-Delete = Ctrl-Alt-BackSpace
 	 * 
 	 * @param evt
 	 * @return whether a shortcut was applied.
 	 */
 	protected void handleShortcuts(KeyEvent evt) {		
 		// WTF? no VK alt Gr on Windows, instead Ctrl + Alt
 		// Actually just always do this, so Ctrl + Alt is Alt Gr
 		if (_keycode == KeyEvent.VK_ALT) {
 			
 			if (evt.isControlDown()) {
 				bypass_original_event = true;
 				if(!_alt_gr_pressed){
 					_alt_gr_pressed = true;
 					// release the by user pressed control key
 					addExtraEvent(new KeyboardEvent(X11_CONTROL,KeyEvent.VK_CONTROL, false));
 					addExtraEvent(new KeyboardEvent(X11_ALT_GRAPH, KeyEvent.VK_ALT_GRAPH, true));				
 				}
 			} 
 			else if(_alt_gr_pressed){
 				bypass_original_event = true;
 				// release
 				addExtraEvent(new KeyboardEvent(X11_ALT_GRAPH, KeyEvent.VK_ALT_GRAPH, false));	
 				_alt_gr_pressed = false;				
 			}
 		}
 		else if(_keycode == KeyEvent.VK_CONTROL){
 			
 			if(evt.isAltDown()){
 				bypass_original_event = true;
 				if(!_alt_gr_pressed){
 					_alt_gr_pressed = true;
 					// release the by user pressed alt key
 					addExtraEvent(new KeyboardEvent(X11_ALT,KeyEvent.VK_ALT, false));
 					addExtraEvent(new KeyboardEvent(X11_ALT_GRAPH, KeyEvent.VK_ALT_GRAPH, true));				
 				}
 			}
 			else if(_alt_gr_pressed){
 				bypass_original_event = true;
 				// release
 				addExtraEvent(new KeyboardEvent(X11_ALT_GRAPH, KeyEvent.VK_ALT_GRAPH, false));
 				_alt_gr_pressed = false;
 			}
 		}
 		
 		switch (_keycode) {
 		case KeyEvent.VK_BACK_SPACE:
 			if (!(evt.isAltDown() && evt.isControlDown())) {
 				return;
 			}
 			addExtraEvent(new KeyboardEvent(0xffe3, KeyEvent.VK_CONTROL, _press));
 			addExtraEvent(new KeyboardEvent(0xffe9, KeyEvent.VK_ALT, _press));
 			addExtraEvent(new KeyboardEvent(0xffff, KeyEvent.VK_DELETE, _press));
 			break;
 		case KeyEvent.VK_META: 		
 			// No Win key on Mac use META (cmd)
 			_keycode = KeyEvent.VK_WINDOWS;
 			break;
 		}
 	}
 
 	private void addExtraEvent(KeyboardEvent evt) {
 		if (_extra_preceding_events == null) {
 			// max two additional at the moment
 			_extra_preceding_events = new LinkedList<KeyboardEvent>();
 		}
 		_extra_preceding_events.add(evt);
 	}
 	
 	public byte[] getBytes() {
 		return getKeyEvent();
 	}
 
 	protected void handleUndefinedJavaKeysymsConvert2x11(int keycode) {
 		switch (keycode) {
 		case KeyEvent.VK_BACK_SPACE:
 			_keysym = X11_BACK_SPACE;
 			break;
 		case KeyEvent.VK_TAB:
 			_keysym = X11_TAB;
 			break;
 		case KeyEvent.VK_ENTER:
 			_keysym = X11_ENTER;
 			break;
 		case KeyEvent.VK_ESCAPE:
 			_keysym = X11_ESCAPE;
 			break;
 		case KeyEvent.VK_ALT:
 			_keysym = X11_ALT;
 			break;
 		case KeyEvent.VK_ALT_GRAPH:
 			_keysym = X11_ALT_GRAPH;
 			break;
 		case KeyEvent.VK_CONTROL:
 			_keysym = X11_CONTROL;
 			break;
 		case KeyEvent.VK_SHIFT:
 			_keysym = X11_SHIFT;
 			break;
 		}
 	}
 
 	protected byte[] concat(byte[] a, byte[] b) {
 		byte[] c = new byte[a.length + b.length];
 		System.arraycopy(a, 0, c, 0, a.length);
 		System.arraycopy(b, 0, c, a.length, b.length);
 		return c;
 	}
 
 	protected byte[] getKeyEvent() {
 		byte[] events = new byte[0];
 		if (_extra_preceding_events != null) {
 			for (KeyboardEvent e : _extra_preceding_events) {
 				System.out.println("extra " + e);
 				events = concat(events, e.getBytes());
 			}
 		}
 		
 		if(bypass_original_event){
 			return events;
 		}
 
 		System.out.println(this);
 		byte[] ev;
 		if (extended_key_event) {
 			ev = getExtendedKeyEvent();
 		} else {
 			ev = getSimpleKeyEvent();
 		}
 
 		return concat(events, ev);
 	}
 
 	protected byte[] getExtendedKeyEvent() {
 		int rfbcode = KeyboardEventMap.java2rfb[_keycode];
 		byte[] buf = new byte[12];
 		buf[0] = (byte) Encodings.QEMU;
 		buf[1] = (byte) 0; // *submessage-type*
 		buf[2] = (byte) 0; // downflag
 		buf[3] = (byte) (_press ? 1 : 0); // downflag
 		byte[] b = RfbUtil.toBytes(_keysym); // *keysym*
 		buf[4] = b[0];
 		buf[5] = b[1];
 		buf[6] = b[2];
 		buf[7] = b[3];
 		b = RfbUtil.toBytes(rfbcode, b); // *keycode*
 		buf[8] = b[0];
 		buf[9] = b[1];
 		buf[10] = b[2];
 		buf[11] = b[3];
 		return buf;
 	}
 
 	protected byte[] getSimpleKeyEvent() {
 		byte[] buf = new byte[8];
 		buf[0] = (byte) Encodings.KEYBOARD_EVENT;
 		buf[1] = (byte) (_press ? 1 : 0);
 		buf[2] = (byte) 0;
 		buf[3] = (byte) 0;
 		buf[4] = (byte) ((_keysym >> 24) & 0xff);
 		buf[5] = (byte) ((_keysym >> 16) & 0xff);
 		buf[6] = (byte) ((_keysym >> 8) & 0xff);
 		buf[7] = (byte) (_keysym & 0xff);
 		return buf;
 	}
 	
 	private void handleMacPecularities(KeyEvent evt){
 		char keychar = (char) _keysym;
 
 		// WTF? Mac Problems, VK_LESS is VK_BACK_QUOTE
 		// fix for danish layout
 		if (_keycode == KeyEvent.VK_BACK_QUOTE){
 
 			// WTF? In snow leopard there is no OS event sent for the danish '<' button
 			// when Ctrl-Alt is held down?
 			// Make it possible to send with Alt key instead.
 			if(evt.isAltDown()){
 				bypass_original_event = true;
 				addExtraEvent(new KeyboardEvent(_keysym,KeyEvent.VK_ALT_GRAPH, _press));
 				addExtraEvent(new KeyboardEvent(_keysym,KeyEvent.VK_LESS, _press));
 			}
 			if(keychar == '<' || keychar == '>') {
 				_keycode = KeyEvent.VK_LESS;	
 			}
 		}
 	}
 	
 	private void handleWinPecularities(KeyEvent evt){
 		if (_keycode == KeyEvent.VK_DEAD_ACUTE) {
 			// WTF? When danish layout VK_EQUALS is changed to DEAD_ACUTE
 			_keycode = KeyEvent.VK_EQUALS;
 		}
		else if(_keycode == KeyEvent.VK_QUOTE){
			if(_keysym == '\'' || _keysym == '*'){
 			// on danish layouts pressing backslash button
 			// wrongly produces 222 (VK_QUOTE) which is the keycode for ø!
 			_keycode = KeyEvent.VK_BACK_SLASH;
			}
 		}
 	}
 	
 	private void handleJavaPecularities(KeyEvent evt){
 		// not every key release has a preceding key press!?!
 		// keep track of key presses, and do press ourself if it wasn't 
 		// triggered.
 		if (_press) {
 			keys_pressed.put(_keycode, _keysym);
 		} else {
 			if (!keys_pressed.containsKey(_keycode)) {
 				// Do press ourself.
 				System.out.println("Writing key pressed event for " + _keysym
 						+ " keycode: " + _keycode);
 				addExtraEvent(new KeyboardEvent(_keysym, _keycode, true));
 			} else {
 				keys_pressed.remove(_keycode);
 			}
 		}		
 	}
 
 	private void handlePecularaties(KeyEvent evt) {
 		if (Util.isMac()) {
 			handleMacPecularities(evt);
 		}
 		else if (Util.isWin()) {
 			handleWinPecularities(evt);
 		}
 		
 		handleJavaPecularities(evt);
 	}
 	
 	public String toString(){
 		return (extended_key_event ? "extended" : "simple ")
 				 + "key event, keysym: " + _keysym + " keychar: '" + (char)_keysym + "'"
 				 + " keycode: " + _keycode
 				 + (_press ? " press" : " release");
 	}
 
 	protected List<KeyboardEvent> getAdditionalEvents() {
 		return _extra_preceding_events;
 	}
 	
 }
