 package de.hswt.hrm.contact.ui.view;
 
 import javax.inject.Inject;
 import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.layout.GridLayout;
 
 public class ContactView {
     @Inject
     public ContactView() {
         // TODO Your code here
     }
 
     @PostConstruct
     public void postConstruct(Composite parent) {
        parent.setLayout(new GridLayout(2, false));
        Button b = new Button(parent, SWT.BORDER);
     }
 
     @Focus
     public void onFocus() {
         // TODO Your code here
     }
 
 }
