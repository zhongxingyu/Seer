 package com.extjs.gxt.ui.client.widget.form;
 
 public class FieldSetDivider extends FieldSet {
 
     public FieldSetDivider() {
         addStyleName("fieldset-divider");
     }

    @Override
    protected void onCollapse() {
        super.onCollapse();
        addStyleName("fieldset-divider-collapsed");
    }

 }
