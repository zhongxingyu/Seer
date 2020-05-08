 package org.jabox.webapp.borders;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.border.BorderBehavior;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.model.StringResourceModel;
 
 /**
  * $Id$
  * @author dimitris
  * @date   Apr 20, 2012 8:23:15 PM
  *
  * Copyright (C) 2012 Scytl Secure Electronic Voting SA
  *
  * All rights reserved.
  *
  */
 
 /**
  *
  */
 public class LabelBehavior extends BorderBehavior {
     private static final long serialVersionUID = -6251370873692995058L;
 
     /**
      * @see org.apache.wicket.markup.html.border.BorderBehavior#beforeRender(org.apache.wicket.Component)
      */
     @Override
     public void beforeRender(final Component c) {
         super.beforeRender(c);
         if (FormComponent.class.isInstance(c)) {
             if ("picker".equals(c.getId())) {
                 return;
             }
 
            String name = c.getClass().getName();
             StringResourceModel label =
                 new StringResourceModel(c.getId(), c.getPage(), null);
             c.getResponse().write(
                 "<label class=\"control-label\" >" + label.getObject()
                     + "</label>");
         }
         super.afterRender(c);
     }
 }
