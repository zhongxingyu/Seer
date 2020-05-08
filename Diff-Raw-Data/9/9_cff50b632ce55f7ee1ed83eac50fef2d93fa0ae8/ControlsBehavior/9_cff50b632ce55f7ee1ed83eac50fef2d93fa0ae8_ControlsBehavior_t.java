 package org.jabox.webapp.borders;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.border.BorderBehavior;
 import org.apache.wicket.markup.html.form.FormComponent;
 
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
 public class ControlsBehavior extends BorderBehavior {
     private static final long serialVersionUID = 6231054589227194163L;
 
     /**
      * @see org.apache.wicket.markup.html.border.BorderBehavior#beforeRender(org.apache.wicket.Component)
      */
     @Override
     public void beforeRender(final Component c) {
         if (FormComponent.class.isInstance(c)) {
            c.getResponse().write("<div class=\"controls\">");
             if ("picker".equals(c.getId())) {
                 return;
             }
         }
         super.beforeRender(c);
     }
 
     /**
      * @see org.apache.wicket.markup.html.border.BorderBehavior#afterRender(org.apache.wicket.Component)
      */
     @Override
     public void afterRender(final Component c) {
         if (FormComponent.class.isInstance(c)) {
             if ("picker".equals(c.getId())) {
                 return;
             }
            c.getResponse().write("</div>");
         }
         super.afterRender(c);
     }
 }
