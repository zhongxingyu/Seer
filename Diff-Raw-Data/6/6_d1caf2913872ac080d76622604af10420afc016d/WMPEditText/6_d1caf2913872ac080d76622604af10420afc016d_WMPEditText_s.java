 package edu.bonn.cs.wmp.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Rect;
 import android.os.RemoteException;
 import android.text.style.BackgroundColorSpan;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.inputmethod.BaseInputConnection;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputConnection;
 import android.view.inputmethod.InputConnectionWrapper;
 import android.widget.EditText;
 import de.hdm.cefx.concurrency.operations.NodePosition;
 import de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService;
 import de.tudresden.inf.rn.mobilis.xmpp.android.Parceller;
 import edu.bonn.cs.wmp.WMPViewRegistry;
 import edu.bonn.cs.wmp.application.WMPApplication;
 import edu.bonn.cs.wmp.awarenesswidgets.ContentChange;
 import edu.bonn.cs.wmp.awarenesswidgets.LineChange;
 import edu.bonn.cs.wmp.awarenesswidgets.ViewportChange;
 import edu.bonn.cs.wmp.awarenesswidgets.WMPAwarenessWidget;
 import edu.bonn.cs.wmp.viewupdater.EditTextViewUpdater;
 import edu.bonn.cs.wmp.xmpp.beans.ViewportBean;
 
 /**
  * This is the collaborative version of a standard Android {@link EditText}. It is recommended
  * to be used together with the {@link RadarView} awareness widget for full collaboration awareness.
  * @author Sven Bendel
  *
  */
 public class WMPEditText extends EditText implements WMPView {
 	private static final boolean TIME_TRIAL = false;
 	private ICollabEditingService collabService;
 	private List<WMPAwarenessWidget> awarenessWidgets = new ArrayList<WMPAwarenessWidget>();
 
 	private WMPApplication app;
 
 	private String wmpName = "wmp_" + Integer.toString(this.getId());
 	public long startTime;
 	
 	public WMPEditText(Context context) {
 		super(context);
 		init();
 	}
 
 	public WMPEditText(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public WMPEditText(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	/**
 	 * Does all initialization work, e.g. registering the {@link ViewUpdater} and
 	 * adding ourselves to the {@link WMPViewRegistry}.
 	 */
 	public void init() {
 		app = WMPApplication.getInstance();
 		collabService = app.getCollabEditingService();
 
 		registerViewUpdater();
 		
 		WMPViewRegistry.getInstance().addWMPView(this);
 	}
 	
 	/**
 	 * Returns the name of the node by which the content of this view is represented
 	 * in the document of the Collaborative Editing Framework. Will be generated
 	 * by using the Android resource ID or from XML in the future.
 	 * @return
 	 * 		the content node's name
 	 */
 	public String getWMPName() {
 		return wmpName;
 	}
 
 	/**
 	 * This method allows setting the name of the node by which the content of this view
 	 * is represented in the document of the Collaborative Editing Framework. Will be
 	 * generated automatically by using the Android resource ID or from XML in the future.
 	 * @param nodeID
 	 */
 	public void setNodeName(String nodeID) {
 		this.wmpName = nodeID;
 	}
 
 	/**
 	 * Creates a new {@link EditTextViewUpdater} for us. This view updater will provide
 	 * us with new content when in collaboration mode.
 	 */
 	private void registerViewUpdater() {
 		new EditTextViewUpdater(this);
 	}
 
 	@Override
 	public boolean onCheckIsTextEditor() {
 		return true;
 	}
 
 	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		refreshViewport(this.getScrollY());
	}
	
	@Override
 	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
 		super.onScrollChanged(l, t, oldl, oldt);
 		
 		refreshViewport(t);
 		
 		// remove all changed text formatting the user has already seen
 		if (t > oldt) {
 			// scroll down
 			int topVisibleLine = this.getLayout().getLineForVertical(t) - 1;
 			int oldTopVisibleLine = this.getLayout().getLineForVertical(oldt);
 			BackgroundColorSpan[] highlights = this.getText().getSpans(
 					this.getLayout().getLineStart(oldTopVisibleLine),
 					this.getLayout().getLineEnd(topVisibleLine),
 					BackgroundColorSpan.class);
 			for (BackgroundColorSpan highlight : highlights) {
 				this.getText().removeSpan(highlight);
 			}
 		} else if (t < oldt) {
 			// scroll up
 			Rect r = new Rect();
 			this.getDrawingRect(r);
 			int bottomVisibleLine = this.getLayout().getLineForVertical(
 					t + r.height());
 			int oldBottomVisibleLine = this.getLayout().getLineForVertical(
 					oldt + r.height());
 			BackgroundColorSpan[] highlights = this.getText().getSpans(
 					this.getLayout().getLineStart(bottomVisibleLine),
 					this.getLayout().getLineEnd(oldBottomVisibleLine),
 					BackgroundColorSpan.class);
 			for (BackgroundColorSpan highlight : highlights) {
 				this.getText().removeSpan(highlight);
 			}
 		}
 	}
 
 	/**
 	 * Report viewport change to the attached awareness widgets.
 	 * @param top
 	 * 			current vertical scroll origin
 	 */
 	private void refreshViewport(int top) {
 		// report viewport change to awareness widgets
 		ViewportChange change = new ViewportChange();
 		change.top = (float) this.getLayout().getLineForVertical(top)/this.getLayout().getLineCount();
 		Rect rect = new Rect();
 		this.getDrawingRect(rect);
 		change.bottom = (float) this.getLayout().getLineForVertical(
 				top + rect.height())/this.getLayout().getLineCount();
 		notifyExternalWMPWidgetsOfContentChange(change);
 		collabService = app.getCollabEditingService();
 		try {
 			if (collabService != null && collabService.isReadyForEditing()) {
 				// TODO: just report relevant changes
 				notifyCollaboratorsOfContentChange(change);
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void notifyCollaboratorsOfContentChange(ContentChange c) {
 		collabService = app.getCollabEditingService();
 		if (c instanceof ViewportChange) {
 			ViewportChange vc = (ViewportChange) c;
 			
 			ViewportBean vb = new ViewportBean(this.getId());
 			vb.setViewportStart(vc.top);
 			vb.setViewportEnd(vc.bottom);
 			try {
 				// TODO: check, if this generates a valid XMPP-ID
 				vb.setFrom(collabService.getUsername());
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			// TODO: check, if this generates a valid muc-room-id which may be used to broadcast the message in the room
 			try {
 				vb.setTo(collabService.getMucRoomName());
 			} catch (RemoteException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			try {
 				collabService.fireAndForgetMUCIQ(Parceller.getInstance().convertXMPPBeanToIQ(vb, true));
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public EditTextInputConnectionWrapper onCreateInputConnection(
 			EditorInfo outAttrs) {
 		InputConnection con = super.onCreateInputConnection(outAttrs);
 		EditTextInputConnectionWrapper inputConnectionWrapper = new EditTextInputConnectionWrapper(
 				con, true);
 		return inputConnectionWrapper;
 	}
 
 	@Override
 	protected void onTextChanged(CharSequence text, int start, int before,
 			int after) {
 		if (TIME_TRIAL) {
 			long endTime = System.currentTimeMillis();
 			Log.i("Time_Trial", "end time = " + String.valueOf(endTime));
 			Log.i("Time_Trial", "delta = " + (startTime - endTime));
 		}
 		super.onTextChanged(text, start, before, after);
 		LineChange c = new LineChange();
 		int count = this.getLineCount();
 		c.lineLengths = new float[count];
 		for (int i = 0; i < count; i++) {
 			float width = this.getLayout().getLineWidth(i);
 			c.lineLengths[i] = width;
 		}
 		notifyExternalWMPWidgetsOfContentChange(c);
 	}
 	
 	@Override
 	public void addWidget(WMPAwarenessWidget w) {
 		awarenessWidgets.add(w);
 	}
 
 	@Override
 	public boolean removeWidget(WMPAwarenessWidget w) {
 		return awarenessWidgets.remove(w);
 	}
 
 	@Override
 	protected void onDetachedFromWindow() {
 		WMPViewRegistry.getInstance().removeWMPView(this.getId());
 		super.onDetachedFromWindow();
 	}
 	
 	@Override
 	public void notifyExternalWMPWidgetsOfContentChange(ContentChange c) {
 		/* 
 		 * It is important to test for null here, as this method is triggered by onTextChanged
 		 * which may be fired before awarenessWidgets is initialized!
 		 */
 		if (awarenessWidgets != null) {
 			for (WMPAwarenessWidget w : awarenessWidgets) {
 				if (w.isInterestedIn(c.getClass())) {
 					w.onViewContentChange(c);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Attention: Overwrite this class (and only this!) if you want to supply your own
 	 * InputConnectionWrapper to a {@link WMPEditText} view!
 	 * 
 	 * This class handles all input to this WMPEditText and routes it through the Collaborative
 	 * Editing Framework when in collaboration mode. The output will be generated by the
 	 * {@link EditTextViewUpdater} when OT is done.
 	 * 
 	 * @author Sven Bendel
 	 */
 	public class EditTextInputConnectionWrapper extends InputConnectionWrapper {
 		InputConnection target;
 
 		@Override
 		public boolean setComposingText(CharSequence text, int newCursorPosition) {
 			if (TIME_TRIAL) {
 				startTime = System.currentTimeMillis();
 				Log.i("Time_Trial", "start time = " + String.valueOf(startTime));
 			}
 			collabService = app.getCollabEditingService();
 			try {
 				if (collabService != null && collabService.isReadyForEditing()) {
 					int start = BaseInputConnection
 							.getComposingSpanStart(getText());
 					int end = BaseInputConnection.getComposingSpanEnd(getText());
 					if (start == -1) {
 						start = getSelectionStart();
 					}
 					if (end == -1) {
 						end = getSelectionEnd();
 					}
 					clearComposingText();
 
 					String nodeId = collabService.getCEFXIDForName(getWMPName());
 										
 					collabService.replaceText(nodeId, null, NodePosition.INSERT_BEFORE,
 							text.toString(), start, end - start);
 					int selectionStart, selectionEnd;
 					selectionStart = start + text.length();
 					selectionEnd = start + text.length();
 					setSelection(selectionStart, selectionEnd);
 					finishComposingText();
 					return true;
 				} else {
 					return super.setComposingText(text, newCursorPosition);
 				}
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		@Override
 		public boolean commitText(CharSequence text, int newCursorPosition) {
 			if (TIME_TRIAL) {
 				startTime = System.currentTimeMillis();
 				Log.i("Time_Trial", "start time = " + String.valueOf(startTime));
 			}
 			collabService = app.getCollabEditingService();
 			try {
 				if (collabService != null && collabService.isReadyForEditing()) {
 					int start = BaseInputConnection
 							.getComposingSpanStart(getText());
 					int end = BaseInputConnection.getComposingSpanEnd(getText());
 					if (start == -1) {
 						start = getSelectionStart();
 					}
 					if (end == -1) {
 						end = getSelectionEnd();
 					}
 					String nodeId = collabService.getCEFXIDForName(getWMPName());
 					collabService.replaceText(nodeId, null, NodePosition.INSERT_BEFORE,
 							text.toString(), start, end - start);
 					int selectionStart, selectionEnd;
 					selectionStart = start + text.length();
 					selectionEnd = start + text.length();
 					setSelection(selectionStart, selectionEnd);
 					return true;
 				} else {
 					return super.commitText(text, newCursorPosition);
 				}
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		@Override
 		public boolean deleteSurroundingText(int leftLength, int rightLength) {
 			collabService = app.getCollabEditingService();
 			try {
 				if (collabService != null && collabService.isReadyForEditing()) {
 					// TODO: do in Background
 					String nodeId = collabService.getCEFXIDForName(getWMPName());
 					collabService.deleteText(nodeId, null, NodePosition.INSERT_BEFORE,
 							getSelectionStart() - leftLength, leftLength
 									+ rightLength);
 					return true;
 				} else {
 					return super.deleteSurroundingText(leftLength, rightLength);
 				}
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		/**
 		 * Deletes text from a given node.
 		 * @param nodeId
 		 * 				the ID of the node to delete text from
 		 * @param start
 		 * 				the position where text deletion starts
 		 * @param end
 		 * 				the position where text deletion ends
 		 * @throws RemoteException
 		 */
 		private void deleteText(String nodeId, int start, int end) throws RemoteException {
 			int deletePos;
 			int length;
 			if (start < end) {
 				deletePos = start;
 				length = end - start;
 			} else if (start > end) {
 				deletePos = end;
 				length = start - end;
 			} else {
 				if (start == 0)
 					return;
 				deletePos = start - 1;
 				length = 1;
 			}
 
 			collabService.deleteText(nodeId, null, NodePosition.INSERT_BEFORE,
 					deletePos, length);
 		}
 
 		public EditTextInputConnectionWrapper(InputConnection target,
 				boolean mutable) {
 			super(target, mutable);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public boolean sendKeyEvent(KeyEvent event) {
 //			Log.v("WMPEditText",
 //					"sendKeyEvent(), keyCode=" + event.getKeyCode());
 			collabService = app.getCollabEditingService();
 			try {
 				if (collabService != null && collabService.isReadyForEditing()) {
 					String nodeId = collabService.getCEFXIDForName(getWMPName());
 					int keyCode = event.getKeyCode();
 
 					int start = BaseInputConnection
 							.getComposingSpanStart(getText());
 					int end = BaseInputConnection.getComposingSpanEnd(getText());
 					if (start == -1) {
 						start = getSelectionStart();
 					}
 					if (end == -1) {
 						end = getSelectionEnd();
 					}
 
 					if (keyCode == KeyEvent.KEYCODE_DEL) {
 						// DELETE
 						/*
 						 * TODO: check if really necessary - seems, that this is
 						 * already handled in the other methods. Maybe necessary for
 						 * hard keyboard?
 						 */
 						deleteText(nodeId, start, end);
 						return true;
 					} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
 						// ENTER
 						// TODO: handle enter key
 						return true;
 					} else
 						return false;
 				} else {
 					return super.sendKeyEvent(event);
 				}
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 	}
 }
