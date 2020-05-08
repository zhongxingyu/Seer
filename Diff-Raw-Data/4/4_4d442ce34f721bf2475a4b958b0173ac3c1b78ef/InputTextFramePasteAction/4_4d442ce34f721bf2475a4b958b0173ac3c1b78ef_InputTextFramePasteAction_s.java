 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components.frames;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
 import com.dmdirc.addons.ui_swing.dialogs.paste.PasteDialogFactory;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.logger.ErrorLevel;
 
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 
 /**
  * Paste action for input frames.
  */
 public final class InputTextFramePasteAction extends AbstractAction {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 1;
     /** Clipboard to paste from. */
     private final Clipboard clipboard;
     /** Text component to be acted upon. */
     private final InputTextFrame inputFrame;
     /** Event bus to post events to. */
     private final DMDircMBassador eventBus;
     /** Swing input field. */
     private final SwingInputField inputField;
     /** Frame container. */
     private final FrameContainer container;
     /** Paste dialog factory. */
     private final PasteDialogFactory pasteDialogFactory;
     /** Window to parent the dialog on. */
     private final Window window;
 
     /**
      * Instantiates a new paste action.
      *
      * @param clipboard Clipboard to paste from
      * @param inputFrame Component to be acted upon
      */
     public InputTextFramePasteAction(final InputTextFrame inputFrame,
             final SwingInputField inputField,
             final FrameContainer container,
             final Clipboard clipboard,
             final DMDircMBassador eventBus,
             final PasteDialogFactory pasteDialogFactory,
            @MainWindow final Window window) {
         super("Paste");
 
         this.clipboard = clipboard;
         this.inputFrame = inputFrame;
         this.eventBus = eventBus;
         this.inputField = inputField;
         this.container = container;
         this.pasteDialogFactory = pasteDialogFactory;
         this.window = window;
     }
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         try {
             if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                 return;
             }
         } catch (final IllegalStateException ex) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ex,
                     "Unable to past from clipboard.", ""));
             return;
         }
 
         try {
             //get the contents of the input field and combine it with the
             //clipboard
             doPaste((String) Toolkit.getDefaultToolkit()
                     .getSystemClipboard().getData(DataFlavor.stringFlavor));
         } catch (final IOException ex) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ex,
                     "Unable to get clipboard contents: " + ex.getMessage(), ""));
         } catch (final UnsupportedFlavorException ex) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ex,
                     "Unsupported clipboard type", ""));
         }
     }
 
     /**
      * Pastes the specified content into the input area.
      *
      * @param clipboard The contents of the clipboard to be pasted
      *
      * @since 0.6.3m1
      */
     public void doPaste(final String clipboard) {
         final String inputFieldText = inputField.getText();
         //Get the text that would result from the paste (inputfield
         //- selection + clipboard)
         final String text = inputFieldText.substring(0, inputField.getSelectionStart())
                 + clipboard + inputFieldText.substring(inputField.getSelectionEnd());
         final String[] clipboardLines = getSplitLine(text);
         //check theres something to paste
         if (clipboardLines.length > 1) {
             //Clear the input field
             inputField.setText("");
             final Integer pasteTrigger = container.getConfigManager().
                     getOptionInt("ui", "pasteProtectionLimit", false);
             //check whether the number of lines is over the limit
             if (pasteTrigger != null && container.getNumLines(text) > pasteTrigger) {
                 //show the multi line paste dialog
                 pasteDialogFactory.getPasteDialog(inputFrame, text, window).displayOrRequestFocus();
             } else {
                 //send the lines
                 for (final String clipboardLine : clipboardLines) {
                     inputFrame.getContainer().sendLine(clipboardLine);
                 }
             }
         } else {
             //put clipboard text in input field
             inputField.replaceSelection(clipboard);
         }
     }
 
     /**
      * Splits the line on all line endings.
      *
      * @param line Line that will be split
      *
      * @return Split line array
      */
     private String[] getSplitLine(final String line) {
         return line.replace("\r\n", "\n").replace('\r', '\n').split("\n");
     }
 
     @Override
     @SuppressWarnings("PMD.AvoidCatchingNPE")
     public boolean isEnabled() {
         try {
             return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
         } catch (NullPointerException | IllegalStateException ex) { //https://bugs.openjdk.java.net/browse/JDK-7000965
             return false;
         }
     }
 
 }
