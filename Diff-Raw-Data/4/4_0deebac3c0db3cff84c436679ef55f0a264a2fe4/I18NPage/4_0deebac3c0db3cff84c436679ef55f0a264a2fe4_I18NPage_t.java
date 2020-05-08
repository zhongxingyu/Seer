 /*
  * Copyright (C) 2003, 2004  Pascal Essiembre, Essiembre Consultant Inc.
  * 
  * This file is part of Essiembre ResourceBundle Editor.
  * 
  * Essiembre ResourceBundle Editor is free software; you can redistribute it 
  * and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * Essiembre ResourceBundle Editor is distributed in the hope that it will be 
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with Essiembre ResourceBundle Editor; if not, write to the 
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
  * Boston, MA  02111-1307  USA
  */
 package com.essiembre.eclipse.i18n.resourcebundle.editors;
 
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Internationalization page where one can edit all resource bundle entries 
  * at once for all supported locales.
  * @author Pascal Essiembre
  * @version $Author$ $Revision$ $Date$
  */
 public class I18NPage extends ScrolledComposite {
 
     /** Minimum height of text fields. */
     private static final int TEXT_MINIMUM_HEIGHT = 50;
 
     /** All bundles. */
     private Bundles bundles;
     
     /** All resource bundle keys. */
     private KeyTree keyTree;
     
     /** Text box to add a new key. */
     private Text addTextBox;
     
     /** Text before it is updated in a field having focus. */
     private String textBeforeUpdate;
         
     /**
      * Constructor.
      * @param parent parent component.
      * @param style  style to apply to this component
      */
     public I18NPage(Composite parent, int style, Bundles bundles) {
         super(parent, style);
         this.bundles = bundles; 
 
         SashForm sashForm = new SashForm(this, SWT.NONE);
         setContent(sashForm);
         setExpandHorizontal(true);
         setExpandVertical(true);
         setMinWidth(400);
         setMinHeight(bundles.count() * TEXT_MINIMUM_HEIGHT);
 
         createSashLeftSide(sashForm);
         createSashRightSide(sashForm);
                 
         sashForm.setWeights(new int[]{25, 75});
     }
 
     /**
      * Creates left side of main sash form.
      * @param sashForm parent sash form
      */
     private void createSashLeftSide(SashForm sashForm) {
         Composite leftComposite = new Composite(sashForm, SWT.BORDER);
         leftComposite.setLayout(new GridLayout(1, false));
 
         // Properties key tree
         keyTree = new KeyTree(leftComposite, bundles);
         
         //--- Bottom section ---
         Composite bottomComposite = new Composite(leftComposite, SWT.NONE);
         GridLayout gridLayout = new GridLayout();
         gridLayout.numColumns = 2;
         gridLayout.horizontalSpacing = 0;
         gridLayout.verticalSpacing = 0;
         gridLayout.marginWidth = 0;
         gridLayout.marginHeight = 0;
         bottomComposite.setLayout(gridLayout);
         GridData gridData = new GridData();
         gridData.horizontalAlignment = GridData.FILL;
         gridData.verticalAlignment = GridData.CENTER;
         gridData.grabExcessHorizontalSpace = true;
         bottomComposite.setLayoutData(gridData);
 
         // Text box
         addTextBox = new Text(bottomComposite, SWT.BORDER);
         gridData = new GridData();
         gridData.grabExcessHorizontalSpace = true;
         gridData.horizontalAlignment = GridData.FILL;
         addTextBox.setLayoutData(gridData);
         addTextBox.addKeyListener(new KeyAdapter() {
             public void keyReleased(KeyEvent event) {
                 if (event.character == SWT.CR) {
                     addPropertyKey();
                 }
             }
         });
         
         // Add button        
         Button addButton = new Button(bottomComposite, SWT.PUSH);
         addButton.setText("Add");
         addButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
                 addPropertyKey();
             }
         });
         
     }
 
     /**
      * Creates right side of main sash form.
      * @param sashForm parent sash form
      */
     private void createSashRightSide(SashForm sashForm) {
         Composite rightComposite = new Composite(sashForm, SWT.BORDER);
         rightComposite.setLayout(new GridLayout(1, false));
         for (int i = 0; i <  bundles.count(); i++) {
             Bundle bundle = bundles.getBundle(i);
 
             Label label = new Label(rightComposite, SWT.NONE);
             label.setText(bundle.getTitle());
             
             Text textBox = new Text(rightComposite, SWT.MULTI | SWT.WRAP | 
                     SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
             GridData gridData = new GridData();
             gridData.verticalAlignment = GridData.FILL;
             gridData.grabExcessVerticalSpace = true;
             gridData.horizontalAlignment = GridData.FILL;
             gridData.grabExcessHorizontalSpace = true;
             textBox.setLayoutData(gridData);
             textBox.addFocusListener(new FocusListener() {
                 public void focusGained(FocusEvent event) {
                     Text textBox = (Text) event.widget;
                     textBeforeUpdate = textBox.getText();
                 }
                 public void focusLost(FocusEvent event) {
                     Text textBox = (Text) event.widget;
                     String text = textBox.getText();
                     if (!text.equals(textBeforeUpdate)) {
                         Bundle bundle = bundles.getBundle(textBox);                        
                         Map data = bundle.getData();
                        data.put(keyTree.getSelectedKey(),
                                  textBox.getText());
                         bundle.refreshEditor();
                     }
                 }
             });
             bundle.setTextBox(textBox);
             
             //label.setFont(new Font());
         }
     }
 
     /**
      * Adds a property key to resource bundle, based on content of 
      * bottom "add" text box.
      */
     private void addPropertyKey(){
         String key = addTextBox.getText();
         if (key != null) {
             bundles.addKey(key);
         }
         keyTree.refresh(key);
         bundles.refreshTextBoxes(keyTree.getSelectedKey());
     }
     
     /**
      * Gets the currently active property key.
      * @return active property key 
      */
     public String getActivePropertyKey(){
         return keyTree.getSelectedKey();
     }
 
 }
