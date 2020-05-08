 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.widgets;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.jface.bindings.keys.KeyStroke;
 import org.eclipse.jface.bindings.keys.ParseException;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.IContentProposalListener2;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 import org.eclipse.jface.fieldassist.TextContentAdapter;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.ui.internal.Workbench;
 
 
 /**
  * A SWT.Text field with a pop up list.
  * @author BREDEX GmbH
  * @created 15.08.2005
  */
 @SuppressWarnings("synthetic-access")
 public final class CompNamePopUpTextField extends CheckedCompNameText {
     
     /**
      * @author BREDEX GmbH
      * @created Dec 8, 2008
      */
     private final class IContentProposalListener2Implementation implements
             IContentProposalListener2 {
         /**
          * {@inheritDoc}
          */
         public void proposalPopupClosed(ContentProposalAdapter adapter) {
             m_popupOpen = false;
         }
 
         /**
          * {@inheritDoc}
          */
         public void proposalPopupOpened(ContentProposalAdapter adapter) {
             m_popupOpen = true;
         }
     }
     
     /**
      * A timer with a task which can be cancelled an (re-)scheduled
      */
     private class DelayableTimer extends Timer {
         /** the task to be executed */
         private Runnable m_task;
         
         /** the timerTask to execute the task */
         private TimerTask m_timerTask;
         
         /** the time to delay the execution of the task */
         private long m_delay;
         
         /**
          * the constructor
          * @param task the task to execute
          * @param delay the delay before an execution of the task
          */
         public DelayableTimer(long delay, Runnable task) {
             m_task = task;
             m_delay = delay;
         }
         
         /**
          * schedules a new delayed execution
          */
         public void schedule() {
             m_timerTask = new TimerTask() { 
                 public void run() { 
                     m_task.run();
                 }
             };
             this.schedule(m_timerTask, m_delay);        
         }
         
         /**
          * cancels the current plan to execute the task
          */
         public void cancel() {
             if (m_timerTask != null) {
                 m_timerTask.cancel();
             }
         }
     }
 
     /** data key to init pop up or not */
     public static final String INITPOPUP = "INITPOPUP"; //$NON-NLS-1$
     
     /** the max. lines of the popup */
     protected static final int HEIGHT = 8;
     
     /** the min. width (pixel) of the popup */
     protected static final int WIDTH = 130;
     
     /** KeyCode for SPACE */
     protected static final char SPACE = ' ';  
 
     /** intern */
     private CompNamesProposalProvider m_contentProposalProvider; 
     
     /** intern */
     private ContentProposalAdapter m_contentProposalAdapter; 
 
     /** intern */
     private ILabelProvider m_labelProvider;
 
     /** is the popup with the content proposal open? */
     private boolean m_popupOpen = false;
     
     /** was the data modified */
     private boolean m_modified = false;
 
     /** track popup state */
     private IContentProposalListener2 m_popupListener;
 
     /**
      * Constructs a text field. When pressing STRG+SPACE a list pops up.
      * 
      * @param compMapper The Component Name mapper to use.
      * @param composite The parent composite.
      * @param style The style of the text field.
      */
     public CompNamePopUpTextField(IComponentNameMapper compMapper,
             Composite composite, int style) {
         
         super(composite, style);
         KeyStroke ks = null;
         try {
             ks = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
         } catch (ParseException e) {
             // ignore until you want to play with the constant above
         }
         m_labelProvider = new LabelProvider() {
 
             public Image getImage(Object element) {
                 CompNamesProposal p = (CompNamesProposal)element;
                 char type = p.getLabel().charAt(0);
                 switch (type) {
                     case 'G': // GLOBAL
                         return IconConstants.GLOBAL_NAME_IMAGE;
                     case 'L': // LOCAL
                         return IconConstants.LOCAL_NAME_IMAGE;
                     case 'A': // AUT
                         return IconConstants.AUT_COMP_NAME_IMAGE;
                     default:
                         return null;
                 }
             }
 
             public String getText(Object element) {
                 CompNamesProposal p = (CompNamesProposal)element;
                 return p.getLabel().substring(1);
             }
 
             public boolean isLabelProperty(Object element, String property) {
                 return false;
             }
 
         };
         m_popupListener = new IContentProposalListener2Implementation();
         m_contentProposalProvider = new CompNamesProposalProvider(compMapper);
         enableContentProposal(m_contentProposalProvider, ks);
         
         int delay = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.MILLIS_TO_OPEN_COMP_NAMES_CONTENT_PROPOSAL);
         final DelayableTimer contentProposalTimer = new DelayableTimer(delay,
                 new Runnable() {
                     public void run() {
                         openContentProposals();
                     }
                 });
         
         addKeyListener(new KeyListener() {
             public void keyReleased(KeyEvent e) {
                 if (!getText().equals(StringConstants.EMPTY)
                         && triggerCharWasTyped(e)) {
                     contentProposalTimer.schedule();
                 }
             }
             
             public void keyPressed(KeyEvent e) {
                 contentProposalTimer.cancel();
             }
         });
     }
 
     /**
      * Checks if a key event was invoked by a character which is supposed to trigger component name proposals
      * @param e the key event
      * @return whether the character is supposed to trigger component name proposals
      */
     protected boolean triggerCharWasTyped(KeyEvent e) {
         char[] triggerChars = createTriggerChars();
         for (char c : triggerChars) {
             if (e.character == c) {
                 return true;
             }            
         }
         return false;
     }
 
     /**
      * used in properties view
      */
     public void activateSelectionEvent() {
         addModifyListener(new ModifyListener() {
 
             public void modifyText(ModifyEvent e) {
                 m_modified = true;
             }
         });
         addFocusListener(new FocusAdapter() {
             public void focusLost(FocusEvent e) {
                 if (!m_popupOpen) {
                     if (m_modified) {
                         fireSelectionEvent();
                     }
                 }
                 
             }
         });
     } 
     
     /**
      * fire a pseudo event to trigger callers
      */
     private void fireSelectionEvent() {
         getDisplay().syncExec(new Runnable() {
 
             public void run() {
                 if (!CompNamePopUpTextField.this.isDisposed()) {
                     Event modifyEvent = new Event();
                     modifyEvent.widget = CompNamePopUpTextField.this;
                     modifyEvent.type = SWT.Modify;
                     CompNamePopUpTextField.this.notifyListeners(SWT.Modify,
                             modifyEvent);
                 }
             }
 
         });
 
     }
 
     /**
      * Do the actual registration of the provider
      * @param contentProposalProvider provider for the content
      * @param keyStroke activation KeyStroke
      * start the proposal
      */
     private void enableContentProposal(
             IContentProposalProvider contentProposalProvider,
             KeyStroke keyStroke) {
         m_contentProposalAdapter = 
             new ContentProposalAdapter(this,
                 new TextContentAdapter(), contentProposalProvider,
                 keyStroke, null);
         m_contentProposalAdapter.setLabelProvider(m_labelProvider);
 
         m_contentProposalAdapter
                 .addContentProposalListener(m_popupListener);
         m_contentProposalAdapter
                 .setProposalAcceptanceStyle(
                         ContentProposalAdapter.PROPOSAL_REPLACE);
     }
 
     /**
      * @return the characters that should trigger a content proposal
      */
     private char[] createTriggerChars() {
         char[] trigger = new char[2 * 26 + 10 + 2];
         int index = 0;
         for (char c = 'a'; c <= 'z'; ++c) {
             trigger[index++] = c;
             trigger[index++] = Character.toUpperCase(c);
         }
         for (int i = 0; i < 10; ++i) {
             trigger[index++] = Character.forDigit(i, 10);
         }
         trigger[index++] = '_';
         trigger[index++] = '\b';
         return trigger;
     }
 
     /**
      * {@inheritDoc}
      */
     protected void checkSubclass() {
         // do nothing
     }
     
     /**
      * @param filter match against type from comp system
      */
     public void setFilter(String filter) {
         if (m_contentProposalProvider != null) {
             m_contentProposalProvider.setTypeFilter(filter);
         }
     }
 
     /**
      * @return the popupOpen
      */
     public boolean isPopupOpen() {
         return m_popupOpen;
     }
     
     /**
      * 
      * @param compMapper The Component Name mapper to use.
      */
     public void setComponentNameMapper(IComponentNameMapper compMapper) {
         if (m_contentProposalProvider != null) {
             m_contentProposalProvider.setComponentNameMapper(compMapper);
         }
     }
 
     /**
      * Opens the proposals for the component name
      */
     private void openContentProposals() {
         Display display = Workbench.getInstance().
                 getDisplay();
         
         Event ctrlEvent = new Event();
         ctrlEvent.type = SWT.KeyDown;
         ctrlEvent.keyCode = SWT.CTRL;
         display.post(ctrlEvent);
         
         Event spaceEvent = new Event();
         spaceEvent.type = SWT.KeyDown;
         spaceEvent.character = SWT.SPACE;
         display.post(spaceEvent);
         
         try {
             Thread.sleep(10);
         } catch (InterruptedException ie) {
             // do nothing
         }
         ctrlEvent.type = SWT.KeyUp;
         display.post(ctrlEvent);
         spaceEvent.type = SWT.KeyUp;
         display.post(spaceEvent);
     }
 }
