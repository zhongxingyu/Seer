 package app;
 
 import java.awt.Frame;
 import java.awt.TextArea;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 final class DialogDebug extends OutputStream
   implements WindowListener
 {
 
 	private static DialogDebug _instance;
 	private PrintStream _writer;
 	private StringBuffer _buffer = new StringBuffer(1024);
 	private Frame _frame;
 	private String _title;
 	private TextArea _textArea;
 	private boolean _loaded = false;
 
 	public final void windowDeactivated(WindowEvent paramWindowEvent) {
 	}
 
 	public final void windowOpened(WindowEvent paramWindowEvent) {
 	}
 
 	public final void windowIconified(WindowEvent paramWindowEvent) {
 	}
 
 	public final void windowDeiconified(WindowEvent paramWindowEvent) {
 	}
 
 	static final PrintStream GetInstance(String paramString) {
 		if (_instance == null) {
 			_instance = new DialogDebug(paramString);
 		}
 		return _instance._writer;
 	}
 
 	public final void windowClosing(WindowEvent paramWindowEvent) {
 		_frame.setVisible(false);
 		_loaded = false;
 	}
 
 	public final void windowClosed(WindowEvent paramWindowEvent) {
 	}
 
 	public final void windowActivated(WindowEvent paramWindowEvent) {
 	}
 
 	public final void write(int i) {
 		synchronized (this) {
 			if (!_loaded) {
 				_frame = new Frame();
 				_frame.add(this._textArea, "Center");
 				_frame.setVisible(true);
 				_frame.setTitle(_title);
 				_frame.setLocation(320, 240);
 				_frame.setSize(720, 260);
 				_frame.addWindowListener(this);
 				_loaded = true;
 			}
 
			_buffer.append((char)i);
			if ((char)i == '\n') {
 				_textArea.append(_buffer.toString());
 				_buffer = new StringBuffer(1024);
 			}
 		}
 	}
 
 	private DialogDebug(String title) {
 		_title = title;
 		_textArea = new TextArea();
 		_textArea.setEditable(false);
 		_writer = new PrintStream(this, true);
 	}
 
 } //class DialogDebug
