 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.outline.cssdialog;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.Constants;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Class for choosing CCS font-family attribute
  *
  * @author dsakovich@exadel.com
  */
 public class FontFamilyDialog extends Dialog implements SelectionListener {
 
     private static final int HEIGHT = 300;
     private static final int BUTTON_TOP_OFFSET = 20;
     private static final int BUTTON_LEFT_OFFSET = 20;
     private static final int BUTTON_RIGHT_OFFSET = -20;
     private static final int BUTTON_RIGHT = 60;
     private static final int RIGHT_BUTTON_TOP = 30;
     private static final int LEFT_BUTTON_TOP = 40;
     private static final int LIST_TOP_OFFSET = 10;
     private static final int LIST_RIGHT_OFFSET = -10;
     private static final int LIST_LEFT_OFFSET = 10;
     private static final int LIST_BOTTOM_OFFSET = -10;
     private static final int LIST_TOP = 5;
     private static final int LIST_BOTTOM = 100;
     private static final int ALL_FONTS_LIST_LEFT = 0;
     private static final int ALL_FONTS_LIST_RIGHT = 40;
     private static final int SELECTED_FONT_LIST_LEFT = 60;
     private static final int SELECTED_FONT_LIST_RIGHT = 100;
 
     /** Font family string */
     private String fontFamily;
 
     /** Existing font family */
     private String existFontFamily;
     private List fontFamilyList;
     private List allFontFamilyList;
     private Button rightButton;
     private Button leftButton;
 
     /**
      * Constructor
      *
      * @param parentShell parent shell
      * @param existingFontFamily existing font family
      */
     public FontFamilyDialog(Shell parentShell, String existingFontFamily) {
         super(parentShell);
         this.existFontFamily = existingFontFamily;
     }
 
     /**
      * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
      */
     @Override
     protected Control createDialogArea(Composite parent) {
         final Composite composite = (Composite) super.createDialogArea(parent);
 
         composite.setLayout(new FormLayout());
 
         allFontFamilyList = new List(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
 
         FormData fd = new FormData();
         fd.top = new FormAttachment(LIST_TOP, LIST_TOP_OFFSET);
         fd.left = new FormAttachment(ALL_FONTS_LIST_LEFT, LIST_LEFT_OFFSET);
         fd.bottom = new FormAttachment(LIST_BOTTOM, LIST_BOTTOM_OFFSET);
         fd.right = new FormAttachment(ALL_FONTS_LIST_RIGHT, LIST_RIGHT_OFFSET);
         fd.height = HEIGHT;
         allFontFamilyList.setLayoutData(fd);
 
         Set<String> s = new HashSet<String>();
         FontData[] fds = composite.getDisplay().getFontList(null, false);
 
         for (int i = 0; i < fds.length; ++i) {
             s.add(fds[i].getName());
         }
         fds = composite.getDisplay().getFontList(null, true);
 
         for (int i = 0; i < fds.length; ++i) {
             s.add(fds[i].getName());
         }
         String[] existFonts = fontFamilyParser();
         Arrays.sort(existFonts);
 
         String[] answer = new String[s.size()];
         s.toArray(answer);
         Arrays.sort(answer);
 
         for (int i = 0; i < answer.length; i++) {
             allFontFamilyList.add(answer[i]);
         }
 
         rightButton = new Button(composite, SWT.PUSH);
         fd = new FormData();
         fd.top = new FormAttachment(RIGHT_BUTTON_TOP, BUTTON_TOP_OFFSET);
         fd.left = new FormAttachment(allFontFamilyList, BUTTON_LEFT_OFFSET);
         fd.right = new FormAttachment(BUTTON_RIGHT, BUTTON_RIGHT_OFFSET);
         rightButton.setLayoutData(fd);
         rightButton.setToolTipText(JstUIMessages.ADD_FONT_FAMILY_TIP);
 
         ImageDescriptor rightDesc = JspEditorPlugin.getImageDescriptor(Constants.IMAGE_RIGHT_FILE_LOCATION);
         Image rightImage = rightDesc.createImage();
         rightButton.setImage(rightImage);
         rightButton.setEnabled(false);
         rightButton.addDisposeListener(new DisposeListener() {
                 public void widgetDisposed(DisposeEvent e) {
                     Button button = (Button) e.getSource();
                     button.getImage().dispose();
                 }
             });
         leftButton = new Button(composite, SWT.PUSH);
         fd = new FormData();
         fd.top = new FormAttachment(LEFT_BUTTON_TOP, BUTTON_TOP_OFFSET);
         fd.left = new FormAttachment(allFontFamilyList, BUTTON_LEFT_OFFSET);
         fd.right = new FormAttachment(BUTTON_RIGHT, BUTTON_RIGHT_OFFSET);
         leftButton.setLayoutData(fd);
         leftButton.setToolTipText(JstUIMessages.REMOVE_FONT_FAMILY_TIP);
 
         ImageDescriptor leftDesc = JspEditorPlugin.getImageDescriptor(Constants.IMAGE_LEFT_FILE_LOCATION);
         Image leftImage = leftDesc.createImage();
         leftButton.setImage(leftImage);
         leftButton.setEnabled(false);
         leftButton.addDisposeListener(new DisposeListener() {
                 public void widgetDisposed(DisposeEvent e) {
                     Button button = (Button) e.getSource();
                     button.getImage().dispose();
                 }
             });
 
         fontFamilyList = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
         fd = new FormData();
         fd.top = new FormAttachment(LIST_TOP, LIST_TOP_OFFSET);
         fd.left = new FormAttachment(SELECTED_FONT_LIST_LEFT, LIST_LEFT_OFFSET);
         fd.bottom = new FormAttachment(LIST_BOTTOM, LIST_BOTTOM_OFFSET);
         fd.right = new FormAttachment(SELECTED_FONT_LIST_RIGHT, LIST_RIGHT_OFFSET);
         fd.height = HEIGHT;
         fontFamilyList.setLayoutData(fd);
 
         if ((existFontFamily != null) && !existFontFamily.equals(Constants.EMPTY)) {
             for (int i = 0; i < existFonts.length; i++) {
                 fontFamilyList.add(existFonts[i]);
             }
         }
 
         /** Control listeners */
         allFontFamilyList.addMouseListener(new MouseListener() {
                 public void mouseDoubleClick(MouseEvent e) {
                     int selectedItem = allFontFamilyList.getSelectionIndex();
                     addFont(fontFamilyList, allFontFamilyList.getItem(selectedItem));
                     allFontFamilyList.remove(selectedItem);
                     rightButton.setEnabled(false);
                 }
 
                 public void mouseDown(MouseEvent e) {
                     if (allFontFamilyList.getSelectionCount() > 0) {
                         fontFamilyList.deselectAll();
                         leftButton.setEnabled(false);
                         rightButton.setEnabled(true);
                     }
                 }
 
                 public void mouseUp(MouseEvent e) {
                 }
             });
 
         fontFamilyList.addMouseListener(new MouseListener() {
                 public void mouseDoubleClick(MouseEvent e) {
                     leftButton.setEnabled(false);
 
                     int selectedItem = fontFamilyList.getSelectionIndex();
                     fontFamilyList.deselectAll();
                     addFont(allFontFamilyList, fontFamilyList.getItem(selectedItem));
                     fontFamilyList.remove(selectedItem);
                 }
 
                 public void mouseDown(MouseEvent e) {
                     if (fontFamilyList.getSelectionCount() > 0) {
                         allFontFamilyList.deselectAll();
                         rightButton.setEnabled(false);
                         leftButton.setEnabled(true);
                     }
                 }
 
                 public void mouseUp(MouseEvent e) {
                 }
             });
 
         rightButton.addSelectionListener(this);
         leftButton.addSelectionListener(this);
 
         /** Add drag and drop */
         Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
 
         final DragSource source = new DragSource(allFontFamilyList, DND.DROP_MOVE);
         source.setTransfer(types);
         source.addDragListener(new DragSourceListener() {
                 public void dragFinished(DragSourceEvent event) {
                     if (event.detail == DND.DROP_MOVE) {
                         int selectedItem = allFontFamilyList.getSelectionIndex();
 
                         if ((allFontFamilyList.getItemCount() > selectedItem) && (selectedItem >= 0)) {
                             allFontFamilyList.remove(selectedItem);
                         }
                     }
                 }
 
                 public void dragSetData(DragSourceEvent event) {
                     int selectedItem = allFontFamilyList.getSelectionIndex();
                     event.data = allFontFamilyList.getItem(selectedItem);
                 }
 
                 public void dragStart(DragSourceEvent event) {
                     event.doit = (allFontFamilyList.getSelectionCount() != 0);
                 }
             });
 
         DropTarget target = new DropTarget(fontFamilyList, DND.DROP_MOVE);
         target.setTransfer(types);
         target.addDropListener(new DropTargetAdapter() {
                 public void drop(DropTargetEvent event) {
                     if (event.data == null) {
                         event.detail = DND.DROP_NONE;
 
                         return;
                     }
 
                     addFont(fontFamilyList, ((String) event.data));
                     rightButton.setEnabled(false);
                 }
             });
 
         final DragSource sourceBack = new DragSource(fontFamilyList, DND.DROP_MOVE);
         sourceBack.setTransfer(types);
         sourceBack.addDragListener(new DragSourceListener() {
                 public void dragFinished(DragSourceEvent event) {
                     if (event.detail == DND.DROP_MOVE) {
                         int selectedItem = fontFamilyList.getSelectionIndex();
 
                         if ((fontFamilyList.getItemCount() > selectedItem) && (selectedItem >= 0)) {
                             fontFamilyList.remove(selectedItem);
                         }
                     }
                 }
 
                 public void dragSetData(DragSourceEvent event) {
                     int selectedItem = fontFamilyList.getSelectionIndex();
                     event.data = fontFamilyList.getItem(selectedItem);
                 }
 
                 public void dragStart(DragSourceEvent event) {
                     event.doit = (fontFamilyList.getSelectionCount() != 0);
                 }
             });
 
         DropTarget targetBack = new DropTarget(allFontFamilyList, DND.DROP_MOVE);
         targetBack.setTransfer(types);
         targetBack.addDropListener(new DropTargetAdapter() {
                 public void drop(DropTargetEvent event) {
                     if (event.data == null) {
                         event.detail = DND.DROP_NONE;
 
                         return;
                     }
 
                     addFont(allFontFamilyList, ((String) event.data));
                     leftButton.setEnabled(false);
                 }
             });
 
         return composite;
     }
 
     /**
      * Set title for dialog
      */
     protected void configureShell(Shell newShell) {
         super.configureShell(newShell);
         newShell.setText(JstUIMessages.FONT_FAMILY_DIALOG_TITLE);
     }
 
     /**
      * @see org.eclipse.jface.dialogs.Dialog#okPressed()
      */
     @Override
     protected void okPressed() {
         String[] items = fontFamilyList.getItems();
         StringBuffer buf = new StringBuffer();
 
         for (int i = 0; i < items.length; i++) {
             buf.append(((i == 0) ? Constants.EMPTY : Constants.COMMA) + items[i]);
         }
 
         fontFamily = buf.toString();
         super.okPressed();
     }
 
     /**
      * Method for add to font to sorted list
      *
      * @param list
      * @param font
      */
     private void addFont(List list, String font) {
         Set<String> s = new HashSet<String>();
         String[] items = list.getItems();
         list.removeAll();
 
         for (int i = 0; i < items.length; i++) {
             s.add(items[i]);
         }
 
         s.add(font);
 
         String[] answer = new String[s.size()];
         s.toArray(answer);
         Arrays.sort(answer);
 
         for (int i = 0; i < answer.length; i++) {
             list.add(answer[i]);
         }
     }
 
     /**
      * Getter for fontFamily attribute
      *
      * @return fontFamily
      */
     public String getFontFamily() {
         return fontFamily;
     }
 
     /**
      * Setter for fontFamily attribute
      *
      * @param fontFamily
      */
     public void setFontFamily(String fontFamily) {
         this.fontFamily = fontFamily;
     }
 
     /**
      * Method for parse font family string
      *
      * @param font
      *                family string
      * @return list font family
      */
     private String[] fontFamilyParser() {
         existFontFamily = existFontFamily.trim();
 
         return existFontFamily.split(Constants.COMMA);
     }
 
     /**
      * Selection listener
      *
      * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(SelectionEvent)
      */
     public void widgetDefaultSelected(SelectionEvent e) {
         Object ob = e.getSource();
 
         if (ob.equals(leftButton)) {
             int[] selectedItems = fontFamilyList.getSelectionIndices();
             String[] items = allFontFamilyList.getItems();
             Set<String> s = new HashSet<String>();
 
             for (int i = 0; i < items.length; i++) {
                 s.add(items[i]);
             }
 
             for (int i = 0; i < selectedItems.length; i++) {
                 s.add(fontFamilyList.getItem(selectedItems[i]));
             }
 
             allFontFamilyList.removeAll();
 
             String[] answer = new String[s.size()];
             s.toArray(answer);
             Arrays.sort(answer);
 
             for (int i = 0; i < answer.length; i++) {
                 allFontFamilyList.add(answer[i]);
             }
 
             fontFamilyList.remove(selectedItems);
             leftButton.setEnabled(false);
         } else if (ob.equals(rightButton)) {
             int[] selectedItems = allFontFamilyList.getSelectionIndices();
             String[] items = fontFamilyList.getItems();
             Set<String> s = new HashSet<String>();
 
             for (int i = 0; i < items.length; i++) {
                 s.add(items[i]);
             }
 
             for (int i = 0; i < selectedItems.length; i++) {
                 s.add(allFontFamilyList.getItem(selectedItems[i]));
             }
 
             fontFamilyList.removeAll();
 
             String[] answer = new String[s.size()];
             s.toArray(answer);
             Arrays.sort(answer);
 
             for (int i = 0; i < answer.length; i++) {
                 fontFamilyList.add(answer[i]);
             }
 
             allFontFamilyList.remove(selectedItems);
             rightButton.setEnabled(false);
         } else if (ob.equals(allFontFamilyList)) {
             fontFamilyList.deselectAll();
             leftButton.setEnabled(false);
             rightButton.setEnabled(true);
         } else if (ob.equals(fontFamilyList)) {
             allFontFamilyList.deselectAll();
             rightButton.setEnabled(false);
             leftButton.setEnabled(true);
         }
     }
 
     /**
      * Selection listener
      *
      * @see org.eclipse.swt.events.SelectionListener#widgetSelected(SelectionEvent)
      */
     public void widgetSelected(SelectionEvent e) {
         Object ob = e.getSource();
 
         if (ob.equals(leftButton)) {
             int[] selectedItems = fontFamilyList.getSelectionIndices();
             String[] items = allFontFamilyList.getItems();
             Set<String> s = new HashSet<String>();
 
             for (int i = 0; i < items.length; i++) {
                 s.add(items[i]);
             }
 
             for (int i = 0; i < selectedItems.length; i++) {
                 s.add(fontFamilyList.getItem(selectedItems[i]));
             }
 
             allFontFamilyList.removeAll();
 
             String[] answer = new String[s.size()];
             s.toArray(answer);
             Arrays.sort(answer);
 
             for (int i = 0; i < answer.length; i++) {
                 allFontFamilyList.add(answer[i]);
             }
 
             fontFamilyList.remove(selectedItems);
             leftButton.setEnabled(false);
         } else if (ob.equals(rightButton)) {
             int[] selectedItems = allFontFamilyList.getSelectionIndices();
             String[] items = fontFamilyList.getItems();
             Set<String> s = new HashSet<String>();
 
             for (int i = 0; i < items.length; i++) {
                 s.add(items[i]);
             }
 
             for (int i = 0; i < selectedItems.length; i++) {
                 s.add(allFontFamilyList.getItem(selectedItems[i]));
             }
 
             fontFamilyList.removeAll();
 
             String[] answer = new String[s.size()];
             s.toArray(answer);
             Arrays.sort(answer);
 
             for (int i = 0; i < answer.length; i++) {
                 fontFamilyList.add(answer[i]);
             }
 
             allFontFamilyList.remove(selectedItems);
             rightButton.setEnabled(false);
         }
     }
 }
