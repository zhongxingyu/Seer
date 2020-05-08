 /*******************************************************************************
  * Copyright (c) 2004, 2008 John Krasnay and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     John Krasnay - initial implementation
  *     Holger Voormann
  *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
  *******************************************************************************/
 package org.eclipse.wst.xml.vex.ui.internal.swt;
 
 import java.text.MessageFormat;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.PopupDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.preference.JFacePreferences;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StyledCellLabelProvider;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.viewers.StyledString.Styler;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.TextStyle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.xml.vex.core.internal.dom.Element;
 import org.eclipse.wst.xml.vex.core.internal.provisional.dom.I.VEXElement;
 import org.eclipse.wst.xml.vex.ui.internal.Icon;
 import org.eclipse.wst.xml.vex.ui.internal.VexPlugin;
 import org.eclipse.wst.xml.vex.ui.internal.editor.Messages;
 
 /**
  * Content assist dialog that is popped up to show a list of actions to select
  * from. The input field at the top above the list could be used to filter the
  * content.
  */
 public class ContentAssist extends PopupDialog {
 
     private static final String SETTINGS_SECTION =
         "contentAssistant"; //$NON-NLS-1$
 
     private final VexWidget vexWidget;
     private final AbstractVexAction[] actions;
     private final boolean autoExecute;
     private final Point location;
     private Text textWidget;
     private TableViewer viewer;
     private Font boldFont;
 
     /**
      * Constructs a new content assist dialog which can be opened by
      * {@link #open()}.
      *
      * @param vexWidget the vex widget this content assist belongs to
      * @param actions list of actions to select from
      * @param autoExecute if {@code true} and if there is only one action then
      *                    {@link #open()} does not show dialog but executes the
      *                    only action
      */
     private ContentAssist(VexWidget vexWidget,
                           AbstractVexAction[] actions,
                           boolean autoExecute) {
         super(vexWidget.getShell(),
               SWT.RESIZE,
               true,  // take focus on open
              false,  // persist size
               false, // persist location
               false, // show dialog menu
               false, // show persist actions
               null,  // title
               null); // footer line
         this.vexWidget = vexWidget;
         this.actions = actions;
         this.autoExecute = autoExecute;
         location = vexWidget.toDisplay(vexWidget.getLocationForContentAssist());
     }
 
     public int open() {
         if (autoExecute && actions.length == 1) {
             actions[0].execute(vexWidget);
             return Window.OK;
         }
         return super.open();
     }
 
     @Override
     protected IDialogSettings getDialogSettings() {
         IDialogSettings root = VexPlugin.getInstance().getDialogSettings();
         IDialogSettings settings = root.getSection(SETTINGS_SECTION);
         if (settings == null) {
             settings = root.addNewSection(SETTINGS_SECTION);
         }
         return settings;
     }
 
     @Override
     protected Color getBackground() {
         String colorId = JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR;
         return JFaceResources.getColorRegistry().get(colorId);
     }
 
     @Override
     protected Control createDialogArea(Composite parent) {
 
         // dialog area panel
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayoutFactory.fillDefaults()
                          .extendedMargins(0, 0, 4, 0)
                          .applyTo(composite);
 
         // 1. input field
         textWidget = new Text(composite, SWT.SINGLE);
         GridDataFactory.fillDefaults().grab(true, false).applyTo(textWidget);
         textWidget.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 repopulateList();
             }
         });
         textWidget.addKeyListener(new KeyAdapter() {
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.keyCode == SWT.CR) {
                     doAction();
                 } else if (   e.widget == textWidget
                            && e.keyCode == SWT.ARROW_DOWN) {
                     viewer.getControl().setFocus();
                 }
             }
         });
 
         // 2. separator
         int separatorStyle = SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT;
         Label separator = new Label(composite, separatorStyle);
         GridDataFactory.fillDefaults().grab(true, false).applyTo(separator);
 
         // 3. list of proposals
         viewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL);
         Control viewerControl = viewer.getControl();
        GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 300).applyTo(viewerControl);
         boldFont = getModifiedFont(viewerControl.getFont(), SWT.BOLD);
         viewer.setLabelProvider(new MyLabelProvider());
         viewer.setContentProvider(new ArrayContentProvider());
         viewer.getTable().addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 doAction();
             }
         });
         viewer.getTable().addMouseListener(new MouseAdapter() {
             @Override
             public void mouseUp(MouseEvent e) {
                 if (e.button == 1) {
                     doAction();
                 }
             }
         });
 
         // fill with content
         repopulateList();
 
         return composite;
     }
 
     @Override
     protected Point getDefaultLocation(Point initialSize) {
         return location;
     }
 
     @Override
     public boolean close() {
         if (boldFont != null) {
             boldFont.dispose();
         }
         return super.close();
     }
 
     /**
      * Perform the action that is currently selected in the tree view, if any,
      * and close the dialog.
      */
     private void doAction() {
         ISelection selection = viewer.getSelection();
         if (selection instanceof StructuredSelection) {
             Object first = ((StructuredSelection) selection).getFirstElement();
             if (first instanceof AbstractVexAction) {
                 ((AbstractVexAction) first).execute(vexWidget);
             }
         }
         close();
     }
 
     private void repopulateList() {
         String filterText = textWidget.getText();
         List<AbstractVexAction> actionList = new LinkedList<AbstractVexAction>();
         for (AbstractVexAction action : actions) {
         	if (action.getText().contains(filterText)) {
                 actionList.add(action);
             }
 		}
         viewer.setInput(actionList.toArray(new AbstractVexAction[actionList.size()]));
         viewer.getTable().setSelection(0);
     }
 
     private class MyLabelProvider extends StyledCellLabelProvider {
 
         private final Styler boldStyler = new Styler() {
             public void applyStyles(TextStyle textStyle) {
                 textStyle.font = boldFont;
             }
         };
 
         @Override
         public void update(ViewerCell cell) {
             AbstractVexAction action = (AbstractVexAction) cell.getElement();
             String filterText = textWidget.getText();
             String text = action.getText();
             StyledString styledString = new StyledString(action.getText());
 
             // show matching text in bold
             if (text.contains(filterText)) {
                 int start = text.indexOf(filterText);
                 int end = start + filterText.length();
                 styledString = new StyledString(text.substring(0, start));
                 styledString.append(text.substring(start, end), boldStyler);
                 styledString.append(text.substring(end));
             }
 
             cell.setText(styledString.toString());
             cell.setStyleRanges(styledString.getStyleRanges());
             cell.setImage(Icon.get(action.getImage()));
             super.update(cell);
         }
 
     }
 
     private static abstract class AbstractVexAction {
 
         private final VexWidget widget;
         private final String text;
         private final String parameter;
         private final Icon image;
 
         public AbstractVexAction(VexWidget widget,
                                  String text,
                                  Icon image) {
             this(widget, text, null, image);
         }
 
         public AbstractVexAction(VexWidget widget,
                                  String text,
                                  String parameter,
                                  Icon image) {
             this.widget = widget;
             this.text = text;
             this.parameter = parameter;
             this.image = image;
         }
 
         abstract void execute(VexWidget vexWidget);
 
         public VexWidget getWidget() {
             return widget;
         }
 
         public String getText() {
             return text;
         }
 
         public String getParameter() {
             return parameter;
         }
 
         public Icon getImage() {
             return image;
         }
 
     }
 
     /**
      * Shows the content assist to add a new element.
      *
      * @param widget the VexWidget which hosts the content assist
      */
     public static void openAddElementsContentAssist(VexWidget widget) {
         AbstractVexAction[] addActions = computeAddElementsActions(widget);
         ContentAssist assist = new ContentAssist(widget,
                                                  addActions,
                                                  true);
         assist.open();
     }
 
     /**
      * Shows the content assist to convert current element.
      *
      * @param widget the VexWidget which hosts the content assist
      */
     public static void openQuickFixContentAssist(VexWidget widget) {
         AbstractVexAction[] quickFixActions = computeQuickFixActions(widget);
         ContentAssist assist = new ContentAssist(widget,
                                                  quickFixActions,
                                                  true);
         assist.open();
     }
 
     private static AbstractVexAction[] computeAddElementsActions(VexWidget widget) {
         String[] names = widget.getValidInsertElements();
         AbstractVexAction[] actions = new AbstractVexAction[names.length];
         for (int i = 0; i < names.length; i++) {
 			actions[i] = new AbstractVexAction(widget, names[i], Icon.ELEMENT) {
                 public void execute(VexWidget vexWidget) {
                     getWidget().insertElement(new Element(getText()));
                 }
             };
         }
         return actions;
     }
 
     private static AbstractVexAction[] computeQuickFixActions(VexWidget widget) {
         String[] names = widget.getValidMorphElements();
         AbstractVexAction[] actions = new AbstractVexAction[names.length];
         int caretOffset = widget.getCaretOffset();
         VEXElement element = widget.getDocument().getElementAt(caretOffset);
         String sourceName = element.getName();
         for (int i = 0; i < names.length; i++) {
             String message = Messages.getString(
                     "command.convertElement.dynamicCommandName"); //$NON-NLS-1$
             String text = MessageFormat.format(message, sourceName, names[i]);
             Icon icon = Icon.CONVERT;
             actions[i] = new AbstractVexAction(widget, text, names[i], icon) {
                 public void execute(VexWidget vexWidget) {
                     getWidget().morph(getParameter());
                 }
             };
         }
         return actions;
     }
 
     private static Font getModifiedFont(Font baseFont, int additionalStyle) {
         FontData[] baseData = baseFont.getFontData();
         FontData[] styleData = new FontData[baseData.length];
         for (int i = 0; i < styleData.length; i++) {
             FontData data = baseData[i];
             styleData[i] = new FontData(data.getName(),
                                         data.getHeight(),
                                         data.getStyle() | additionalStyle);
         }
            return  new Font(Display.getCurrent(), styleData);
     }
 
 }
