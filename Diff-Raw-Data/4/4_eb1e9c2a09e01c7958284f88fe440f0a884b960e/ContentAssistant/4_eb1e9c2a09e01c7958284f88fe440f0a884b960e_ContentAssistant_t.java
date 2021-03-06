 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jface.text.contentassist;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTError;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.jface.text.Assert;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension3;
 import org.eclipse.jface.text.IEventConsumer;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.IViewportListener;
 import org.eclipse.jface.text.IWidgetTokenKeeper;
 import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
 import org.eclipse.jface.text.IWidgetTokenOwner;
 import org.eclipse.jface.text.IWidgetTokenOwnerExtension;
 import org.eclipse.jface.text.TextUtilities;
 
 
 
 /**
  * The standard implementation of the <code>IContentAssistant</code> interface.
  * Usually, clients instantiate this class and configure it before using it.
  */
 public class ContentAssistant implements IContentAssistant, IContentAssistantExtension, IWidgetTokenKeeper, IWidgetTokenKeeperExtension {
 	
 	/**
 	 * A generic closer class used to monitor various 
 	 * interface events in order to determine whether
 	 * content-assist should be terminated and all
 	 * associated windows closed.
 	 */
 	class Closer implements ControlListener, MouseListener, FocusListener, DisposeListener, IViewportListener {
 
 		/** The shell that a <code>ControlListener</code> is registered with. */
 		private Control fShell;
 		/** The control that a <code>MouseListener</code>, a<code>FocusListener</code> and a <code>DisposeListener</code> are registered with. */
 		private Control fControl;
 		
 		/**
 		 * Installs this closer on it's viewer's text widget.
 		 */
 		protected void install() {
 			Control control= fContentAssistSubjectAdapter.getControl();
 			fControl= control;
 			if (Helper.okToUse(control)) {
 				
 				Control shell= control.getShell();
 				fShell= shell;
 				shell.addControlListener(this);
 					
 				control.addMouseListener(this);
 				control.addFocusListener(this);
 				
 				/*
 				 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of Internal Errors
 				 */
 				control.addDisposeListener(this);
 			}
 			if (fViewer != null)
 				fViewer.addViewportListener(this);
 		}
 		
 		/**
 		 * Uninstalls this closer from the viewer's text widget.
 		 */
 		protected void uninstall() {
 			Control shell= fShell;
 			fShell= null;
 			if (Helper.okToUse(shell))
 				shell.removeControlListener(this);
 			
 			Control control= fControl;
 			fControl= null;
 			if (Helper.okToUse(control)) {
 				
 				control.removeMouseListener(this);
 				control.removeFocusListener(this);
 				
 				/*
 				 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of Internal Errors
 				 */
 				control.removeDisposeListener(this);
 			}
 			
 			if (fViewer != null)
 				fViewer.removeViewportListener(this);
 		}
 
 		/*
 		 * @see ControlListener#controlResized(ControlEvent)
 		 */
 		public void controlResized(ControlEvent e) {
 			hide();
 		}
 
 		/*
 		 * @see ControlListener#controlMoved(ControlEvent)
 		 */
 		public void controlMoved(ControlEvent e) {
 			hide();
 		}
 
 		/*
 		 * @see MouseListener#mouseDown(MouseEvent)
 		 */
 		public void mouseDown(MouseEvent e) {
 			hide();
 		}
 
 		/*
 		 * @see MouseListener#mouseUp(MouseEvent)
 		 */
 		public void mouseUp(MouseEvent e) {
 		}
 
 		/*
 		 * @see MouseListener#mouseDoubleClick(MouseEvent)
 		 */
 		public void mouseDoubleClick(MouseEvent e) {
 			hide();
 		}
 
 		/* 
 		 * @see FocusListener#focusGained(FocusEvent)
 		 */
 		public void focusGained(FocusEvent e) {
 		}
 
 		/*
 		 * @see FocusListener#focusLost(FocusEvent)
 		 */
 		public void focusLost(FocusEvent e) {
			Control control= fControl;
			if (Helper.okToUse(control)) {
 				Display d= control.getDisplay();
 				if (d != null) {
 					d.asyncExec(new Runnable() {
 						public void run() {
 							if (!fProposalPopup.hasFocus() && (fContextInfoPopup == null || !fContextInfoPopup.hasFocus()))
 								hide();
 						}
 					});
 				}
 			}
 		}
 		
 		/*
 		 * @seeDisposeListener#widgetDisposed(DisposeEvent)
 		 */
 		public void widgetDisposed(DisposeEvent e) {
 			/*
 			 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of Internal Errors
 			 */
 			hide();
 		}
 
 		/*
 		 * @see IViewportListener#viewportChanged(int)
 		 */
 		public void viewportChanged(int topIndex) {
 			hide();
 		}
 	}
 	
 	/**
 	 * An implementation of <code>IContentAssistListener</code>, this class is
 	 * used to monitor key events in support of automatic activation
 	 * of the content assistant. If enabled, the implementation utilizes a
 	 * thread to watch for input characters matching the activation
 	 * characters specified by the content assist processor, and if
 	 * detected, will wait the indicated delay interval before
 	 * activating the content assistant.
 	 */
 	class AutoAssistListener extends KeyAdapter implements KeyListener, Runnable, VerifyKeyListener {
 		
 		private Thread fThread;
 		private boolean fIsReset= false;
 		private Object fMutex= new Object();
 		private int fShowStyle;
 		
 		private final static int SHOW_PROPOSALS= 1;
 		private final static int SHOW_CONTEXT_INFO= 2;
 		
 		protected AutoAssistListener() {
 		}
 
 		protected void start(int showStyle) {
 			fShowStyle= showStyle;
 			fThread= new Thread(this, JFaceTextMessages.getString("ContentAssistant.assist_delay_timer_name")); //$NON-NLS-1$
 			fThread.start();
 		}
 
 		public void run() {
 			try {
 				while (true) {
 					synchronized (fMutex) {
 						if (fAutoActivationDelay != 0)
 							fMutex.wait(fAutoActivationDelay);
 						if (fIsReset) {
 							fIsReset= false;
 							continue;
 						}
 					}
 					showAssist(fShowStyle);
 					break;
 				}
 			} catch (InterruptedException e) {
 			}
 			fThread= null;
 		}
 		
 		protected void reset(int showStyle) {
 			synchronized (fMutex) {
 				fShowStyle= showStyle;
 				fIsReset= true;
 				fMutex.notifyAll();
 			}
 		}
 
 		protected void stop() {
 			if (fThread != null) {
 				fThread.interrupt();
 			}
 		}
 		
 		private boolean contains(char[] characters, char character) {
 			if (characters != null) {
 				for (int i= 0; i < characters.length; i++) {
 					if (character == characters[i])
 						return true;
 				}
 			}
 			return false;
 		}
 		
 		public void keyPressed(KeyEvent e) {
 			// Only act on typed characters and ignore modifier-only events
 			if (e.character == 0 && (e.keyCode & SWT.KEYCODE_BIT) == 0)
 				return;
 
 			int showStyle;
 			int pos= fContentAssistSubjectAdapter.getSelectedRange().x;
 			char[] activation;
 			
 			activation= fContentAssistSubjectAdapter.getCompletionProposalAutoActivationCharacters(ContentAssistant.this, pos);
 			
 			if (contains(activation, e.character) && !fProposalPopup.isActive())
 				showStyle= SHOW_PROPOSALS;
 			else {
 				activation= fContentAssistSubjectAdapter.getContextInformationAutoActivationCharacters(ContentAssistant.this, pos);
 				if (contains(activation, e.character) && fContextInfoPopup != null && !fContextInfoPopup.isActive())
 					showStyle= SHOW_CONTEXT_INFO;
 				else {
 					if (fThread != null && fThread.isAlive())
 						stop();
 					return;
 				}
 			}
 			
 			if (fThread != null && fThread.isAlive())
 				reset(showStyle);
 			else
 				start(showStyle);
 		}
 
 		/*
 		 * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
 		 */
 		public void verifyKey(VerifyEvent event) {
 			keyPressed(event);
 		}
 		
 		protected void showAssist(final int showStyle) {
 			Display d= fContentAssistSubjectAdapter.getControl().getDisplay();
 			if (d != null) {
 				try {
 					d.syncExec(new Runnable() {
 						public void run() {
 							if (showStyle == SHOW_PROPOSALS)
 								fProposalPopup.showProposals(true);
 							else if (showStyle == SHOW_CONTEXT_INFO && fContextInfoPopup != null)
 								fContextInfoPopup.showContextProposals(true);
 						}
 					});
 				} catch (SWTError e) {
 				}
 			}
 		}
 	}
 	
 	/**
 	 * The laypout manager layouts the various
 	 * windows associated with the content assistant based on the
 	 * settings of the content assistant.
 	 */
 	class LayoutManager implements Listener {
 		
 		// Presentation types.
 		public final static int LAYOUT_PROPOSAL_SELECTOR= 0;
 		public final static int LAYOUT_CONTEXT_SELECTOR= 1;
 		public final static int LAYOUT_CONTEXT_INFO_POPUP= 2;
 
 		int fContextType= LAYOUT_CONTEXT_SELECTOR;
 		Shell[] fShells= new Shell[3];
 		Object[] fPopups= new Object[3];
 
 		protected void add(Object popup, Shell shell, int type, int offset) {
 			Assert.isNotNull(popup);
 			Assert.isTrue(shell != null && !shell.isDisposed());
 			checkType(type);
 			
 			if (fShells[type] != shell) {
 				if (fShells[type] != null)
 					fShells[type].removeListener(SWT.Dispose, this);
 				shell.addListener(SWT.Dispose, this);
 				fShells[type]= shell;
 			}
 			
 			fPopups[type]= popup;
 			if (type == LAYOUT_CONTEXT_SELECTOR || type == LAYOUT_CONTEXT_INFO_POPUP)
 				fContextType= type;
 			
 			layout(type, offset);
 			adjustListeners(type);
 		}
 	
 		protected void checkType(int type) {
 			Assert.isTrue(type == LAYOUT_PROPOSAL_SELECTOR ||
 				type == LAYOUT_CONTEXT_SELECTOR || type == LAYOUT_CONTEXT_INFO_POPUP);
 		}
 		
 		public void handleEvent(Event event) {
 			Widget source= event.widget;
 			source.removeListener(SWT.Dispose, this);
 			
 			int type= getShellType(source);
 			checkType(type);
 			fShells[type]= null;
 						
 			switch (type) {
 				case LAYOUT_PROPOSAL_SELECTOR:
 					if (fContextType == LAYOUT_CONTEXT_SELECTOR &&
 							Helper.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR])) {
 						// Restore event notification to the tip popup.
 						addContentAssistListener((IContentAssistListener) fPopups[LAYOUT_CONTEXT_SELECTOR], CONTEXT_SELECTOR);						
 					}
 					break;
 				
 				case LAYOUT_CONTEXT_SELECTOR:
 					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
 						if (fProposalPopupOrientation == PROPOSAL_STACKED)
 							layout(LAYOUT_PROPOSAL_SELECTOR, getSelectionOffset());
 						// Restore event notification to the proposal popup.
 						addContentAssistListener((IContentAssistListener) fPopups[LAYOUT_PROPOSAL_SELECTOR], PROPOSAL_SELECTOR);						
 					}
 					fContextType= LAYOUT_CONTEXT_INFO_POPUP;
 					break;
 				
 				case LAYOUT_CONTEXT_INFO_POPUP:
 					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
 						if (fContextInfoPopupOrientation == CONTEXT_INFO_BELOW)
 							layout(LAYOUT_PROPOSAL_SELECTOR, getSelectionOffset());
 					}
 					fContextType= LAYOUT_CONTEXT_SELECTOR;
 					break;
 			}
 		}
 		
 		protected int getShellType(Widget shell) {
 			for (int i=0; i<fShells.length; i++) {
 				if (fShells[i] == shell)
 					return i;
 			}
 			return -1;
 		}
 		
 		protected void layout(int type, int offset) {
 			switch (type) {
 				case LAYOUT_PROPOSAL_SELECTOR:
 					layoutProposalSelector(offset);
 					break;
 				case LAYOUT_CONTEXT_SELECTOR:
 					layoutContextSelector(offset);
 					break;
 				case LAYOUT_CONTEXT_INFO_POPUP:
 					layoutContextInfoPopup(offset);
 					break;
 			}
 		}
 
 		protected void layoutProposalSelector(int offset) {
 			if (fContextType == LAYOUT_CONTEXT_INFO_POPUP &&
 					fContextInfoPopupOrientation == CONTEXT_INFO_BELOW &&
 					Helper.okToUse(fShells[LAYOUT_CONTEXT_INFO_POPUP])) {
 				// Stack proposal selector beneath the tip box.
 				Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
 				Shell parent= fShells[LAYOUT_CONTEXT_INFO_POPUP];
 				shell.setLocation(getStackedLocation(shell, parent));
 			} else if (fContextType != LAYOUT_CONTEXT_SELECTOR ||
 						!Helper.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR])) {
 				// There are no other presentations to be concerned with,
 				// so place the proposal selector beneath the cursor line.
 				Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
 				shell.setLocation(getBelowLocation(shell, offset));
 			} else {
 				switch (fProposalPopupOrientation) {
 					case PROPOSAL_REMOVE: {
 						// Remove the tip selector and place the
 						// proposal selector beneath the cursor line.
 						fShells[LAYOUT_CONTEXT_SELECTOR].dispose();
 						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
 						shell.setLocation(getBelowLocation(shell, offset));
 						break;
 					}
 					case PROPOSAL_OVERLAY: {
 						// Overlay the tip selector with the proposal selector.
 						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
 						shell.setLocation(getBelowLocation(shell, offset));
 						break;
 					}
 					case PROPOSAL_STACKED: {
 						// Stack the proposal selector beneath the tip selector.
 						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
 						Shell parent= fShells[LAYOUT_CONTEXT_SELECTOR];
 						shell.setLocation(getStackedLocation(shell, parent));
 						break;
 					}
 				}
 			}
 		}
 		
 		protected void layoutContextSelector(int offset) {
 			// Always place the context selector beneath the cursor line.
 			Shell shell= fShells[LAYOUT_CONTEXT_SELECTOR];
 			shell.setLocation(getBelowLocation(shell, offset));
 			
 			if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
 				switch (fProposalPopupOrientation) {
 					case PROPOSAL_REMOVE:
 						// Remove the proposal selector.
 						fShells[LAYOUT_PROPOSAL_SELECTOR].dispose();
 						break;
 	
 					case PROPOSAL_OVERLAY:
 						// The proposal selector has been overlayed by the tip selector.
 						break;
 	
 					case PROPOSAL_STACKED: {
 						// Stack the proposal selector beneath the tip selector.
 						shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
 						Shell parent= fShells[LAYOUT_CONTEXT_SELECTOR];
 						shell.setLocation(getStackedLocation(shell, parent));
 						break;
 					}
 				}
 			}
 		}
 		
 		protected void layoutContextInfoPopup(int offset) {
 			switch (fContextInfoPopupOrientation) {
 				case CONTEXT_INFO_ABOVE: {
 					// Place the popup above the cursor line.
 					Shell shell= fShells[LAYOUT_CONTEXT_INFO_POPUP];
 					shell.setLocation(getAboveLocation(shell, offset));
 					break;
 				}
 				case CONTEXT_INFO_BELOW: {
 					// Place the popup beneath the cursor line.
 					Shell parent= fShells[LAYOUT_CONTEXT_INFO_POPUP];
 					parent.setLocation(getBelowLocation(parent, offset));
 					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
 						// Stack the proposal selector beneath the context info popup.
 						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
 						shell.setLocation(getStackedLocation(shell, parent));
 					}
 					break;
 				}
 			}
 		}
 		
 		protected void shiftHorizontalLocation(Point location, Rectangle shellBounds, Rectangle displayBounds) {
 			if (location.x + shellBounds.width > displayBounds.width)
 				location.x= displayBounds.width - shellBounds.width;
 			
 			if (location.x < displayBounds.x)
 				location.x= displayBounds.x;
 		}
 		
 		protected void shiftVerticalLocation(Point location, Rectangle shellBounds, Rectangle displayBounds) {
 			if (location.y + shellBounds.height > displayBounds.height)
 				location.y= displayBounds.height - shellBounds.height;
 			
 			if (location.y < displayBounds.y)
 				location.y= displayBounds.y;
 		}
 				
 		protected Point getAboveLocation(Shell shell, int offset) {
 			Point location= fContentAssistSubjectAdapter.getLocationAtOffset(offset);
 			location= fContentAssistSubjectAdapter.getControl().toDisplay(location);
 			
 			Rectangle shellBounds= shell.getBounds();
 			Rectangle displayBounds= shell.getDisplay().getClientArea();
 			
 			location.y=location.y - shellBounds.height;
 			
 			shiftHorizontalLocation(location, shellBounds, displayBounds);
 			shiftVerticalLocation(location, shellBounds, displayBounds);
 			
 			return location;
 		}
 		
 		protected Point getBelowLocation(Shell shell, int offset) {
 			Point location= fContentAssistSubjectAdapter.getLocationAtOffset(offset);
 			if (location.x < 0) location.x= 0;
 			if (location.y < 0) location.y= 0;
 			location= fContentAssistSubjectAdapter.getControl().toDisplay(location);
 			
 			Rectangle shellBounds= shell.getBounds();
 			Rectangle displayBounds= shell.getDisplay().getClientArea();
 			
 			location.y= location.y + fContentAssistSubjectAdapter.getLineHeight();
 			shiftHorizontalLocation(location, shellBounds, displayBounds);
 			shiftVerticalLocation(location, shellBounds, displayBounds);
 			
 			return location;
 		}	
 		
 		protected Point getStackedLocation(Shell shell, Shell parent) {
 			Point p= parent.getLocation();
 			Point size= parent.getSize();
 			p.x += size.x / 4;
 			p.y += size.y;
 			
 			p= parent.toDisplay(p);
 
 			Rectangle shellBounds= shell.getBounds();
 			Rectangle displayBounds= shell.getDisplay().getClientArea();
 			shiftHorizontalLocation(p, shellBounds, displayBounds);
 			shiftVerticalLocation(p, shellBounds, displayBounds);
 			
 			return p;
 		}
 
 		protected void adjustListeners(int type) {
 			switch (type) {
 				case LAYOUT_PROPOSAL_SELECTOR:
 					if (fContextType == LAYOUT_CONTEXT_SELECTOR &&
 							Helper.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR]))
 						// Disable event notification to the tip selector.
 						removeContentAssistListener((IContentAssistListener) fPopups[LAYOUT_CONTEXT_SELECTOR], CONTEXT_SELECTOR);						
 					break;
 				case LAYOUT_CONTEXT_SELECTOR:
 					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR]))
 						// Disable event notification to the proposal selector.
 						removeContentAssistListener((IContentAssistListener) fPopups[LAYOUT_PROPOSAL_SELECTOR], PROPOSAL_SELECTOR);						
 					break;
 				case LAYOUT_CONTEXT_INFO_POPUP:
 					break;
 			}
 		}
 	}
 	
 	/**
 	 * Internal key listener and event consumer.
 	 */
 	class InternalListener implements VerifyKeyListener, IEventConsumer {
 		
 		/**
 		 * Verifies key events by notifying the registered listeners.
 		 * Each listener is allowed to indicate that the event has been
 		 * handled and should not be further processed.
 		 *
 		 * @param e the verify event
 		 * @see VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
 		 */
 		public void verifyKey(VerifyEvent e) {
 			IContentAssistListener[] listeners= (IContentAssistListener[]) fListeners.clone();
 			for (int i= 0; i < listeners.length; i++) {
 				if (listeners[i] != null) {
 					if (!listeners[i].verifyKey(e) || !e.doit)
 						break;
 				}
 			}
 			if (fAutoAssistListener != null)
 				fAutoAssistListener.keyPressed(e);
 		}
 				
 		/*
 		 * @see IEventConsumer#processEvent
 		 */
 		public void processEvent(VerifyEvent event) {
 			
 			installKeyListener();
 			
 			IContentAssistListener[] listeners= (IContentAssistListener[])fListeners.clone();
 			for (int i= 0; i < listeners.length; i++) {
 				if (listeners[i] != null) {
 					listeners[i].processEvent(event);
 					if (!event.doit)
 						return;
 				}
 			}
 		}
 	}
 	
 	
 	// Content-Assist Listener types
 	final static int CONTEXT_SELECTOR= 0;
 	final static int PROPOSAL_SELECTOR= 1;
 	final static int CONTEXT_INFO_POPUP= 2;
 	
 	/** 
 	 * The popup priority: &gt; linked position proposals and hover popups.
 	 * Default value: <code>20</code>;
 	 *  
 	 * @since 3.0
 	 */
 	public static final int WIDGET_PRIORITY= 20;
 	
 	private static final int DEFAULT_AUTO_ACTIVATION_DELAY= 500;
 	
 	private IInformationControlCreator fInformationControlCreator;
 	private int fAutoActivationDelay= DEFAULT_AUTO_ACTIVATION_DELAY;
 	private boolean fIsAutoActivated= false;
 	private boolean fIsAutoInserting= false;
 	private int fProposalPopupOrientation= PROPOSAL_OVERLAY;
 	private int fContextInfoPopupOrientation= CONTEXT_INFO_ABOVE;	
 	private Map fProcessors;
 	private String fPartitioning;
 	
 	private Color fContextInfoPopupBackground;
 	private Color fContextInfoPopupForeground;
 	private Color fContextSelectorBackground;
 	private Color fContextSelectorForeground;
 	private Color fProposalSelectorBackground;
 	private Color fProposalSelectorForeground;
 
 	private ITextViewer fViewer;
 	private String fLastErrorMessage;
 	
 	private Closer fCloser;
 	private LayoutManager fLayoutManager;
 	private AutoAssistListener fAutoAssistListener;
 	private InternalListener fInternalListener;
 	private CompletionProposalPopup fProposalPopup;
 	private ContextInformationPopup fContextInfoPopup;
 	
 	private boolean fVerifyKeyListenerHooked= false;
 	private IContentAssistListener[] fListeners= new IContentAssistListener[4];
 	/**
 	 * The content assist subject.
 	 * 
 	 * @since 3.0
 	 */
 	private IContentAssistSubject fContentAssistSubject;
 	/**
 	 * The content assist subject adapter.
 	 * 
 	 * @since 3.0
 	 */
 	private ContentAssistSubjectAdapter fContentAssistSubjectAdapter;
 
 	
 	/**
 	 * Creates a new content assistant. The content assistant is not automatically activated,
 	 * overlays the completion proposals with context information list if necessary, and
 	 * shows the context information above the location at which it was activated. If auto
 	 * activation will be enabled, without further configuration steps, this content assistant
 	 * is activated after a 500 ms delay. It uses the default partitioning.
 	 */	
 	public ContentAssistant() {
 		fPartitioning= IDocumentExtension3.DEFAULT_PARTITIONING;
 	}
 	
 	/**
 	 * Sets the document partitioning this content assistant is using.
 	 * 
 	 * @param partitioning the document partitioning for this content assistant
 	 */
 	public void setDocumentPartitioning(String partitioning) {
 		Assert.isNotNull(partitioning);
 		fPartitioning= partitioning;
 	}
 	
 	/*
 	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension#getDocumentPartitioning()
 	 * @since 3.0
 	 */
 	public String getDocumentPartitioning() {
 		return fPartitioning;
 	}
 	
 	/**
 	 * Registers a given content assist processor for a particular content type.
 	 * If there is already a processor registered for this type, the new processor 
 	 * is registered instead of the old one.
 	 *
 	 * @param processor the content assist processor to register, or <code>null</code> to remove an existing one
 	 * @param contentType the content type under which to register
 	 */	
 	 public void setContentAssistProcessor(IContentAssistProcessor processor, String contentType) {
 		
 		Assert.isNotNull(contentType);
 					
 		if (fProcessors == null)
 			fProcessors= new HashMap();
 			
 		if (processor == null)
 			fProcessors.remove(contentType);
 		else
 			fProcessors.put(contentType, processor);
 	}
 	
 	/*
 	 * @see IContentAssistant#getContentAssistProcessor 	 
 	 */
 	public IContentAssistProcessor getContentAssistProcessor(String contentType) {
 		if (fProcessors == null)
 			return null;
 						
 		return (IContentAssistProcessor) fProcessors.get(contentType);
 	}
 	
 	/**
 	 * Enables the content assistant's auto activation mode.
 	 *
 	 * @param enabled indicates whether auto activation is enabled or not
 	 */
 	public void enableAutoActivation(boolean enabled) {
 		fIsAutoActivated= enabled;
 		manageAutoActivation(fIsAutoActivated);
 	}
 	
 	/**
 	 * Enables the content assistant's auto insertion mode. If enabled,
 	 * the content assistant inserts a proposal automatically if it is
 	 * the only proposal. In the case of ambiguities, the user must
 	 * make the choice.
 	 * 
 	 * @param enabled indicates whether auto insertion is enabled or not
 	 * @since 2.0
 	 */
 	public void enableAutoInsert(boolean enabled) {
 		fIsAutoInserting= enabled;
 	}
 	
 	/**
 	 * Returns whether this content assistant is in the auto insertion
 	 * mode or not.
 	 * 
 	 * @return <code>true</code> if in auto insertion mode
 	 * @since 2.0
 	 */
 	boolean isAutoInserting() {
 		return fIsAutoInserting;
 	}
 	
 	/**
 	 * Installs and uninstall the listeners needed for autoactivation.
 	 * @param start <code>true</code> if listeners must be installed,
 	 * 	<code>false</code> if they must be removed
 	 * @since 2.0
 	 */
 	private void manageAutoActivation(boolean start) {			
 		if (start) {
 			
 			if ((fContentAssistSubjectAdapter != null) && fAutoAssistListener == null) {
 				fAutoAssistListener= new AutoAssistListener();
 				// TODO see bug 49212 
 				if (fContentAssistSubjectAdapter.supportsVerifyKeyListener())
 					fContentAssistSubjectAdapter.appendVerifyKeyListener(fAutoAssistListener);
 				else
 					fContentAssistSubjectAdapter.addKeyListener(fAutoAssistListener);
 			}
 			
 		} else if (fAutoAssistListener != null) {
 			// TODO see bug 49212 
 			if (fContentAssistSubjectAdapter.supportsVerifyKeyListener())
 				fContentAssistSubjectAdapter.removeVerifyKeyListener(fAutoAssistListener);
 			else
 				fContentAssistSubjectAdapter.removeKeyListener(fAutoAssistListener);
 			fAutoAssistListener= null;
 		}
 	}
 	
 	/**
 	 * Sets the delay after which the content assistant is automatically invoked
 	 * if the cursor is behind an auto activation character. 
 	 *
 	 * @param delay the auto activation delay
 	 */
 	public void setAutoActivationDelay(int delay) {
 		fAutoActivationDelay= delay;
 	}
 	
 	/**
 	 * Sets the proposal popups' orientation.
 	 * The following values may be used:
 	 * <ul>
 	 *   <li>PROPOSAL_OVERLAY<p>
 	 *     proposal popup windows should overlay each other
 	 *   </li>
 	 *   <li>PROPOSAL_REMOVE<p>
 	 *     any currently shown proposal popup should be closed
 	 *   </li>
 	 *   <li>PROPOSAL_STACKED<p>
 	 *     proposal popup windows should be vertical stacked, with no overlap,
 	 *     beneath the line containing the current cursor location
 	 *   </li>
 	 * </ul>
 	 *
 	 * @param orientation the popup's orientation
 	 */	 
 	public void setProposalPopupOrientation(int orientation) {
 		fProposalPopupOrientation= orientation;
 	}
 	
 	/**
 	 * Sets the context information popup's orientation.
 	 * The following values may be used:
 	 * <ul>
 	 *   <li>CONTEXT_ABOVE<p>
 	 *     context information popup should always appear above the line containing
 	 *     the current cursor location
 	 *   </li>
 	 *   <li>CONTEXT_BELOW<p>
 	 *     context information popup should always appear below the line containing
 	 *     the current cursor location
 	 *   </li>
 	 * </ul>
 	 *
 	 * @param orientation the popup's orientation
 	 */	
 	public void setContextInformationPopupOrientation(int orientation) {
 		fContextInfoPopupOrientation= orientation;
 	}
 	
 	/**
 	 * Sets the context information popup's background color.
 	 *
 	 * @param background the background color
 	 */
 	public void setContextInformationPopupBackground(Color background) {
 		fContextInfoPopupBackground= background;
 	}
 	
 	/**
 	 * Returns the background of the context information popup.
 	 *
 	 * @return the background of the context information popup
 	 * @since 2.0
 	 */
 	Color getContextInformationPopupBackground() {
 		return fContextInfoPopupBackground;
 	}
 	
 	/**
 	 * Sets the context information popup's foreground color.
 	 *
 	 * @param foreground the foreground color
 	 * @since 2.0
 	 */
 	public void setContextInformationPopupForeground(Color foreground) {
 		fContextInfoPopupForeground= foreground;
 	}
 	
 	/**
 	 * Returns the foreground of the context information popup.
 	 *
 	 * @return the foreground of the context information popup
 	 * @since 2.0
 	 */
 	Color getContextInformationPopupForeground() {
 		return fContextInfoPopupForeground;
 	}
 	
 	/**
 	 * Sets the proposal selector's background color.
 	 *
 	 * @param background the background color
 	 * @since 2.0
 	 */
 	public void setProposalSelectorBackground(Color background) {
 		fProposalSelectorBackground= background;
 	}
 	
 	/**
 	 * Returns the background of the proposal selector.
 	 *
 	 * @return the background of the proposal selector
 	 * @since 2.0
 	 */
 	Color getProposalSelectorBackground() {
 		return fProposalSelectorBackground;
 	}
 	
 	/**
 	 * Sets the proposal's foreground color.
 	 *
 	 * @param foreground the foreground color
 	 * @since 2.0
 	 */
 	public void setProposalSelectorForeground(Color foreground) {
 		fProposalSelectorForeground= foreground;
 	}
 	
 	/**
 	 * Returns the foreground of the proposal selector.
 	 *
 	 * @return the foreground of the proposal selector
 	 * @since 2.0
 	 */
 	Color getProposalSelectorForeground() {
 		return fProposalSelectorForeground;
 	}
 	
 	/**
 	 * Sets the context selector's background color.
 	 *
 	 * @param background the background color
 	 * @since 2.0
 	 */
 	public void setContextSelectorBackground(Color background) {
 		fContextSelectorBackground= background;
 	}
 	
 	/**
 	 * Returns the background of the context selector.
 	 *
 	 * @return the background of the context selector
 	 * @since 2.0
 	 */
 	Color getContextSelectorBackground() {
 		return fContextSelectorBackground;
 	}
 	
 	/**
 	 * Sets the context selector's foreground color.
 	 *
 	 * @param foreground the foreground color
 	 * @since 2.0
 	 */
 	public void setContextSelectorForeground(Color foreground) {
 		fContextSelectorForeground= foreground;
 	}
 	
 	/**
 	 * Returns the foreground of the context selector.
 	 *
 	 * @return the foreground of the context selector
 	 * @since 2.0
 	 */
 	Color getContextSelectorForeground() {
 		return fContextSelectorForeground;
 	}
 	
 	/**
 	 * Sets the information control creator for the additional information control.
 	 * 
 	 * @param creator the information control creator for the additional information control
 	 * @since 2.0
 	 */
 	public void setInformationControlCreator(IInformationControlCreator creator) {
 		fInformationControlCreator= creator;
 	}
 
 	/*
 	 * @see IContentAssistantExtension#install(IContentAssistSubject)
 	 */
 	public void install(IContentAssistSubject contentAssistSubject) {
 		fContentAssistSubject= contentAssistSubject;
 		fContentAssistSubjectAdapter= new ContentAssistSubjectAdapter(fContentAssistSubject);
 		install();
 	}
 	
 	/*
 	 * @see IContentAssist#install
 	 */
 	public void install(ITextViewer textViewer) {
 		fViewer= textViewer;
 		fContentAssistSubjectAdapter= new ContentAssistSubjectAdapter(fViewer);
 		install();
 	}
 	
 	protected void install() {
 		
 		fLayoutManager= new LayoutManager();
 		fInternalListener= new InternalListener();
 		
 		AdditionalInfoController controller= null;
 		if (fInformationControlCreator != null) {
 			int delay= fAutoActivationDelay;
 			if (delay == 0)
 				delay= DEFAULT_AUTO_ACTIVATION_DELAY;
 			delay= Math.round(delay * 1.5f);
 			controller= new AdditionalInfoController(fInformationControlCreator, delay);
 		}
 		
 		fContextInfoPopup= fContentAssistSubjectAdapter.createContextInfoPopup(this);
 		fProposalPopup= fContentAssistSubjectAdapter.createCompletionProposalPopup(this, controller);
 		
 		manageAutoActivation(fIsAutoActivated);
 	}
 	
 	/*
 	 * @see IContentAssist#uninstall
 	 */
 	public void uninstall() {
 		hide();
 		manageAutoActivation(false);
 		
 		if (fCloser != null) {
 			fCloser.uninstall();
 			fCloser= null;
 		}
 		
 		fViewer= null;
 		fContentAssistSubject= null;
 		fContentAssistSubjectAdapter= null;
 	}
 
 	/**
 	 * Adds the given shell of the specified type to the layout.
 	 * Valid types are defined by <code>LayoutManager</code>.
 	 *
 	 * @param popup a content assist popup
 	 * @param shell the shell of the content-assist popup
 	 * @param type the type of popup
 	 * @param visibleOffset the offset at which to layout the popup relative to the offset of the viewer's visible region
 	 * @since 2.0
 	 */
 	void addToLayout(Object popup, Shell shell, int type, int visibleOffset) {
 		fLayoutManager.add(popup, shell, type, visibleOffset);
 	}
 	
 	/**
 	 * Layouts the registered popup of the given type relative to the
 	 * given offset. The offset is relative to the offset of the viewer's visible region.
 	 * Valid types are defined by <code>LayoutManager</code>.
 	 * 
 	 * @param type the type of popup to layout
 	 * @param visibleOffset the offset at which to layout relative to the offset of the viewer's visible region
 	 * @since 2.0
 	 */
 	void layout(int type, int visibleOffset) {
 		fLayoutManager.layout(type, visibleOffset);
 	}
 	
 	/**
 	 * Notifies the controller that a popup has lost focus.
 	 *
 	 * @param e the focus event
 	 */
 	void popupFocusLost(FocusEvent e) {
 		fCloser.focusLost(e);
 	}
 	
 	/**
 	 * Returns the offset of the selection relative to the offset of the visible region.
 	 * 
 	 * @return the offset of the selection relative to the offset of the visible region
 	 * @since 2.0
 	 */
 	int getSelectionOffset() {
 		return fContentAssistSubjectAdapter.getWidgetSelectionRange().x;
 	}
 	
 	/**
 	 * Returns whether the widget token could be acquired.
 	 * The following are valid listener types:
 	 * <ul>
 	 *   <li>AUTO_ASSIST
 	 *   <li>CONTEXT_SELECTOR
 	 *   <li>PROPOSAL_SELECTOR
 	 *   <li>CONTEXT_INFO_POPUP
 	 * <ul>
 	 * @param type the listener type for which to acquire
 	 * @return <code>true</code> if the widget token could be acquired
 	 * @since 2.0
 	 */
 	private boolean acquireWidgetToken(int type) {
 		switch (type) {
 			case CONTEXT_SELECTOR:
 			case PROPOSAL_SELECTOR:
 				if (fContentAssistSubject instanceof IWidgetTokenOwnerExtension)  {
 					IWidgetTokenOwnerExtension extension= (IWidgetTokenOwnerExtension)fContentAssistSubject;
 					return extension.requestWidgetToken(this, WIDGET_PRIORITY);
 				} else if (fContentAssistSubject instanceof IWidgetTokenOwner) {
 					IWidgetTokenOwner owner= (IWidgetTokenOwner)fContentAssistSubject;
 					return owner.requestWidgetToken(this);
 				} else if (fViewer instanceof IWidgetTokenOwnerExtension)  {
 					IWidgetTokenOwnerExtension extension= (IWidgetTokenOwnerExtension) fViewer;
 					return extension.requestWidgetToken(this, WIDGET_PRIORITY);
 				} else if (fViewer instanceof IWidgetTokenOwner) {
 					IWidgetTokenOwner owner= (IWidgetTokenOwner) fViewer;
 					return owner.requestWidgetToken(this);
 				}
 		}	
 		return true;
 	}
 	
 	/**
 	 * Registers a content assist listener.
 	 * The following are valid listener types:
 	 * <ul>
 	 *   <li>AUTO_ASSIST
 	 *   <li>CONTEXT_SELECTOR
 	 *   <li>PROPOSAL_SELECTOR
 	 *   <li>CONTEXT_INFO_POPUP
 	 * <ul>
 	 * Returns whether the listener could be added successfully. A listener
 	 * can not be added if the widget token could not be acquired.
 	 *
 	 * @param listener the listener to register
 	 * @param type the type of listener
 	 * @return <code>true</code> if the listener could be added
 	 */
 	boolean addContentAssistListener(IContentAssistListener listener, int type) {
 		
 		if (acquireWidgetToken(type)) {
 			
 			fListeners[type]= listener;
 			
 			if (getNumberOfListeners() == 1) {
 				fCloser= new Closer();
 				fCloser.install();
 				fContentAssistSubjectAdapter.setEventConsumer(fInternalListener);
 				installKeyListener();
 			} else
 				promoteKeyListener();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Repromotes the keylistener to the first position, using
 	 * prependVerifyKeyListener. This ensures no other instance is
 	 * filtering away the keystrokes underneath, if we've been up for a
 	 * while (e.g. when the context info is showing.
 	 * 
 	 * @since 3.0
 	 */
 	private void promoteKeyListener() {
 		uninstallVerifyKeyListener();
 		installKeyListener();
 	}
 
 	/**
 	 * Installs a key listener on the text viewer's widget.
 	 */
 	private void installKeyListener() {
 		if (!fVerifyKeyListenerHooked) {
 			if (Helper.okToUse(fContentAssistSubjectAdapter.getControl())) {
 				fVerifyKeyListenerHooked= fContentAssistSubjectAdapter.prependVerifyKeyListener(fInternalListener);
 			}
 		}
 	}
 	
 	/**
 	 * Releases the previously acquired widget token if the token
 	 * is no longer necessary.
 	 * The following are valid listener types:
 	 * <ul>
 	 *   <li>AUTO_ASSIST
 	 *   <li>CONTEXT_SELECTOR
 	 *   <li>PROPOSAL_SELECTOR
 	 *   <li>CONTEXT_INFO_POPUP
 	 * <ul>
 	 * 
 	 * @param type the listener type
 	 * @since 2.0
 	 */
 	private void releaseWidgetToken(int type) {
 		if (fListeners[CONTEXT_SELECTOR] == null && fListeners[PROPOSAL_SELECTOR] == null) {
 			IWidgetTokenOwner owner= null;
 			if (fContentAssistSubject instanceof IWidgetTokenOwner)
 				owner= (IWidgetTokenOwner)fContentAssistSubject;
 			else if (fViewer instanceof IWidgetTokenOwner)
 				owner= (IWidgetTokenOwner)fViewer;
 			if (owner != null)
 				owner.releaseWidgetToken(this);
 		}
 	}
 	
 	/**
 	 * Unregisters a content assist listener.
 	 *
 	 * @param listener the listener to unregister
 	 * @param type the type of listener
 	 *
 	 * @see #addContentAssistListener
 	 */
 	void removeContentAssistListener(IContentAssistListener listener, int type) {
 		fListeners[type]= null;
 		
 		if (getNumberOfListeners() == 0) {
 			
 			if (fCloser != null) {
 				fCloser.uninstall();
 				fCloser= null;
 			}
 		
 			uninstallVerifyKeyListener();
 			fContentAssistSubjectAdapter.setEventConsumer(null);
 		}
 		
 		releaseWidgetToken(type);
 	}
 	
 	/**
 	 * Uninstall the key listener from the text viewer's widget.
 	 */
 	private void uninstallVerifyKeyListener() {
 		if (fVerifyKeyListenerHooked) {
 			if (Helper.okToUse(fContentAssistSubjectAdapter.getControl()))
 				fContentAssistSubjectAdapter.removeVerifyKeyListener(fInternalListener);
 			fVerifyKeyListenerHooked= false;
 		}
 	}
 	
 	/**
 	 * Returns the number of listeners.
 	 *
 	 * @return the number of listeners
 	 * @since 2.0
 	 */
 	private int getNumberOfListeners() {
 		int count= 0;
 		for (int i= 0; i <= CONTEXT_INFO_POPUP; i++) {
 			if (fListeners[i] != null)
 				++ count;
 		}
 		return count;
 	}
 		
 	/*
 	 * @see IContentAssist#showPossibleCompletions
 	 */
 	public String showPossibleCompletions() {
 		promoteKeyListener();
 		return fProposalPopup.showProposals(false);
 	}
 	
 	/*
 	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension#completePrefix()
 	 */
 	public String completePrefix() {
 		promoteKeyListener();
 		return fProposalPopup.incrementalComplete();
 	}
 	
 	/**
 	 * Callback to signal this content assistant that the presentation of the possible completions has been stopped.
 	 * @since 2.1 
 	 */
 	protected void possibleCompletionsClosed() {
 	}
 	
 	/*
 	 * @see IContentAssist#showContextInformation
 	 */
 	public String showContextInformation() {
 		promoteKeyListener();
 		if (fContextInfoPopup != null)
 			return fContextInfoPopup.showContextProposals(false);
 		else
 			return null;
 	}
 	
 	/**
 	 * Callback to signal this content assistant that the presentation of the context information has been stopped.
 	 * @since 2.1 
 	 */
 	protected void contextInformationClosed() {
 	}
 	
 	/**
 	 * Requests that the specified context information to be shown.
 	 *
 	 * @param contextInformation the context information to be shown
 	 * @param position the position to which the context information refers to
 	 * @since 2.0
 	 */
 	void showContextInformation(IContextInformation contextInformation, int position) {
 		if (fContextInfoPopup != null)
 			fContextInfoPopup.showContextInformation(contextInformation, position);
 	}
 	
 	/**
 	 * Returns the current content assist error message.
 	 *
 	 * @return an error message or <code>null</code> if no error has occurred
 	 */
 	String getErrorMessage() {
 		return fLastErrorMessage;
 	}
 	
 	/**
 	 * Returns the content assist processor for the content
 	 * type of the specified document position.
 	 *
 	 * @param viewer the text viewer
 	 * @param offset a offset within the document
 	 * @return a content-assist processor or <code>null</code> if none exists
 	 */
 	private IContentAssistProcessor getProcessor(ITextViewer viewer, int offset) {
 		try {
 			
 			IDocument document= viewer.getDocument();
 			String type= TextUtilities.getContentType(document, getDocumentPartitioning(), offset);
 			
 			return getContentAssistProcessor(type);
 		
 		} catch (BadLocationException x) {
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Returns the content assist processor for the content
 	 * type of the specified document position.
 	 *
 	 * @param contentAssistSubject the content assist subject
 	 * @param offset a offset within the document
 	 * @return a content-assist processor or <code>null</code> if none exists
 	 */
 	private IContentAssistProcessor getProcessor(IContentAssistSubject contentAssistSubject, int offset) {
 		try {
 			
 			IDocument document= contentAssistSubject.getDocument();
 			String type;
 			if (document != null)
 				type= TextUtilities.getContentType(document, getDocumentPartitioning(), offset);
 			else 
 				type= IDocument.DEFAULT_CONTENT_TYPE;
 			
 			return getContentAssistProcessor(type);
 		
 		} catch (BadLocationException x) {
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Returns an array of completion proposals computed based on 
 	 * the specified document position. The position is used to 
 	 * determine the appropriate content assist processor to invoke.
 	 *
 	 * @param contentAssistSubject the content assit subject
 	 * @param position a document position
 	 * @return an array of completion proposals
 	 *
 	 * @see IContentAssistProcessor#computeCompletionProposals
 	 */
 	ICompletionProposal[] computeCompletionProposals(IContentAssistSubject contentAssistSubject, int position) {
 		fLastErrorMessage= null;
 		
 		ICompletionProposal[] result= null;
 		
 		IContentAssistProcessor p= getProcessor(contentAssistSubject, position);
 		if (p instanceof IContentAssistProcessorExtension) {
 			result= ((IContentAssistProcessorExtension)p).computeCompletionProposals(contentAssistSubject, position);
 			fLastErrorMessage= p.getErrorMessage();
 		}
 		
 		return result;
 	}
 		
 	/**
 	 * Returns an array of completion proposals computed based on 
 	 * the specified document position. The position is used to 
 	 * determine the appropriate content assist processor to invoke.
 	 *
 	 * @param viewer the viewer for which to compute the prosposals
 	 * @param position a document position
 	 * @return an array of completion proposals
 	 *
 	 * @see IContentAssistProcessor#computeCompletionProposals
 	 */
 	ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int position) {
 		fLastErrorMessage= null;
 		
 		ICompletionProposal[] result= null;
 		
 		IContentAssistProcessor p= getProcessor(viewer, position);
 		if (p != null) {
 			result= p.computeCompletionProposals(viewer, position);
 			fLastErrorMessage= p.getErrorMessage();
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Returns an array of context information objects computed based
 	 * on the specified document position. The position is used to determine 
 	 * the appropriate content assist processor to invoke.
 	 *
 	 * @param viewer the viewer for which to compute the context information
 	 * @param position a document position
 	 * @return an array of context information objects
 	 *
 	 * @see IContentAssistProcessor#computeContextInformation
 	 */
 	IContextInformation[] computeContextInformation(ITextViewer viewer, int position) {
 		fLastErrorMessage= null;
 		
 		IContextInformation[] result= null;
 		
 		IContentAssistProcessor p= getProcessor(viewer, position);
 		if (p != null) {
 			result= p.computeContextInformation(viewer, position);
 			fLastErrorMessage= p.getErrorMessage();
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Returns an array of context information objects computed based
 	 * on the specified document position. The position is used to determine 
 	 * the appropriate content assist processor to invoke.
 	 *
 	 * @param contentAssistSubject the content assit subject
 	 * @param position a document position
 	 * @return an array of context information objects
 	 *
 	 * @see IContentAssistProcessor#computeContextInformation
 	 */
 	IContextInformation[] computeContextInformation(IContentAssistSubject contentAssistSubject, int position) {
 		fLastErrorMessage= null;
 		
 		IContextInformation[] result= null;
 		
 		IContentAssistProcessor p= getProcessor(contentAssistSubject, position);
 		if (p instanceof IContentAssistProcessorExtension) {
 			result= ((IContentAssistProcessorExtension)p).computeContextInformation(contentAssistSubject, position);
 			fLastErrorMessage= p.getErrorMessage();
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Returns the context information validator that should be used to 
 	 * determine when the currently displayed context information should
 	 * be dismissed. The position is used to determine the appropriate 
 	 * content assist processor to invoke.
 	 *
 	 * @param viewer the text viewer
 	 * @param offset a document offset
 	 * @return an validator
 	 *
 	 * @see IContentAssistProcessor#getContextInformationValidator
 	 */
 	IContextInformationValidator getContextInformationValidator(ITextViewer viewer, int offset) {
 		IContentAssistProcessor p= getProcessor(viewer, offset);
 		return p != null ? p.getContextInformationValidator() : null;
 	}
 
 	/**
 	 * Returns the context information validator that should be used to 
 	 * determine when the currently displayed context information should
 	 * be dismissed. The position is used to determine the appropriate 
 	 * content assist processor to invoke.
 	 *
 	 * @param contentAssistSubject the content assist subject
 	 * @param offset a document offset
 	 * @return an validator
 	 * @see IContentAssistProcessor#getContextInformationValidator
 	 * @since 3.0
 	 */
 	IContextInformationValidator getContextInformationValidator(IContentAssistSubject contentAssistSubject, int offset) {
 		IContentAssistProcessor p= getProcessor(contentAssistSubject, offset);
 		return p != null ? p.getContextInformationValidator() : null;
 	}
 
 	/**
 	 * Returns the context information presenter that should be used to 
 	 * display context information. The position is used to determine the appropriate 
 	 * content assist processor to invoke.
 	 *
 	 * @param viewer the text viewer
 	 * @param offset a document offset
 	 * @return a presenter
 	 * @since 2.0
 	 */
 	IContextInformationPresenter getContextInformationPresenter(ITextViewer viewer, int offset) {
 		IContextInformationValidator validator= getContextInformationValidator(viewer, offset);
 		if (validator instanceof IContextInformationPresenter)
 			return (IContextInformationPresenter) validator;
 		return null;
 	}
 	
 	/**
 	 * Returns the context information presenter that should be used to 
 	 * display context information. The position is used to determine the appropriate 
 	 * content assist processor to invoke.
 	 *
 	 * @param contentAssistSubject the content assist subject
 	 * @param offset a document offset
 	 * @return a presenter
 	 * @since 3.0
 	 */
 	IContextInformationPresenter getContextInformationPresenter(IContentAssistSubject contentAssistSubject, int offset) {
 		IContextInformationValidator validator= getContextInformationValidator(contentAssistSubject, offset);
 		if (validator instanceof IContextInformationPresenter)
 			return (IContextInformationPresenter) validator;
 		return null;
 	}
 	
 	/**
 	 * Returns the characters which when typed by the user should automatically
 	 * initiate proposing completions. The position is used to determine the 
 	 * appropriate content assist processor to invoke.
 	 *
 	 * @param contentAssistSubject the content assist subject
 	 * @param offset a document offset
 	 * @return the auto activation characters
 	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters
 	 * @since 3.0
 	 */
 	char[] getCompletionProposalAutoActivationCharacters(IContentAssistSubject contentAssistSubject, int offset) {
 		IContentAssistProcessor p= getProcessor(contentAssistSubject, offset);
 		return p != null ? p.getCompletionProposalAutoActivationCharacters() : null;
 	}
 	
 	/**
 	 * Returns the characters which when typed by the user should automatically
 	 * initiate proposing completions. The position is used to determine the 
 	 * appropriate content assist processor to invoke.
 	 *
 	 * @param viewer the text viewer
 	 * @param offset a document offset
 	 * @return the auto activation characters
 	 *
 	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters
 	 */
 	char[] getCompletionProposalAutoActivationCharacters(ITextViewer viewer, int offset) {
 		IContentAssistProcessor p= getProcessor(viewer, offset);
 		return p != null ? p.getCompletionProposalAutoActivationCharacters() : null;
 	}
 	
 	/**
 	 * Returns the characters which when typed by the user should automatically
 	 * initiate the presentation of context information. The position is used
 	 * to determine the appropriate content assist processor to invoke.
 	 *
 	 * @param viewer the text viewer
 	 * @param offset a document offset
 	 * @return the auto activation characters
 	 *
 	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters
 	 */
 	char[] getContextInformationAutoActivationCharacters(ITextViewer viewer, int offset) {
 		IContentAssistProcessor p= getProcessor(viewer, offset);
 		return p != null ? p.getContextInformationAutoActivationCharacters() : null;
 	}
 	
 	/**
 	 * Returns the characters which when typed by the user should automatically
 	 * initiate the presentation of context information. The position is used
 	 * to determine the appropriate content assist processor to invoke.
 	 *
 	 * @param contentAssistSubject the content assist subject
 	 * @param offset a document offset
 	 * @return the auto activation characters
 	 *
 	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters
 	 */
 	char[] getContextInformationAutoActivationCharacters(IContentAssistSubject contentAssistSubject, int offset) {
 		IContentAssistProcessor p= getProcessor(contentAssistSubject, offset);
 		return p != null ? p.getContextInformationAutoActivationCharacters() : null;
 	}
 	
 	/*
 	 * @see org.eclipse.jface.text.IWidgetTokenKeeper#requestWidgetToken(IWidgetTokenOwner)
 	 * @since 2.0
 	 */
 	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
 		return false;
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenOwner, int)
 	 * @since 3.0
 	 */
 	public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
 		if (priority > WIDGET_PRIORITY) {
 			hide();
 			return true;
 		} else  {
 			return false;
 		}
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#setFocus(org.eclipse.jface.text.IWidgetTokenOwner)
 	 * @since 3.0
 	 */
 	public boolean setFocus(IWidgetTokenOwner owner) {
 		if (fProposalPopup != null) {
 			fProposalPopup.setFocus();
 			return fProposalPopup.hasFocus();
 		}
 		return false;
 	}
 	
 	/**
 	 * Hides any open popups.
 	 */
 	protected void hide() {
 		if (fProposalPopup != null)
 			fProposalPopup.hide();
 			
 		if (fContextInfoPopup != null)
 			fContextInfoPopup.hide();
 	}
 }
