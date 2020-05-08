 /**
  * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
  * details.
  */
 package com.razie.pub.base;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import com.razie.pub.comms.Comms;
 import com.razie.pub.draw.DrawStream;
 import com.razie.pub.draw.Drawable;
 import com.razie.pub.draw.Renderer;
 import com.razie.pub.draw.Renderer.Technology;
 import com.razie.pub.draw.widgets.NavButton;
 import com.razie.pub.draw.widgets.NavLink;
 import com.razie.pub.http.LightAuth;
 
 /**
  * this is an instance of an action, meant to be invoked. It is prepared by someone and can be
 * executed on the spot OR presented to the user as a menu or some other invokable and invoked later.
 * It contains everything needed to invoke itself. It can be invoked in the same JVM or remotely
 * (from a web page etc).
  * 
  * it can be placed on a menu, web page, dialog as a button etc - it generally represents a menu
  * item or a button.
  * 
  * @author razvanc99
  */
public class ActionToInvoke extends AttrAccess.Impl implements AttrAccess, Drawable {
     /** this is the action, contains the actual command name and label to display */
     public ActionItem actionItem;
 
     /**
      * the prefix used depending on the drawing technology - for http, it's the URL to append to.
      * This will identify the target of the action
      */
     public String     target;
 
     // TODO presentation in model, not nice
     public boolean    drawTiny  = false;
 
     /**
      * does it navigate or just invoke something in the background? navigation means GET for
      * instance as opposed to POST
      */
     public boolean    navigates = true;
 
     /**
      * constructor
      * 
      * @param target the prefix used depending on the drawing technology - for http, it's the URL to
      *        append to
      * @param item this is the action, contains the actual command name and label to display
      * @param pairs
      */
     public ActionToInvoke(String target, ActionItem item, Object... pairs) {
         super(pairs);
         this.target = target;
         this.actionItem = item;
     }
 
     /**
      * constructor
      * 
      * @param target the prefix used depending on the drawing technology - for http, it's the URL to
      *        append to
      * @param item this is the action, contains the actual command name and label to display
      * @param pairs
      */
     public ActionToInvoke(ActionItem item, Object... pairs) {
         this("", item, pairs);
     }
 
     /** shortcut to render self - don't like controllers that much */
     public Object render(Technology t, DrawStream stream) {
         return Renderer.Helper.draw(this, t, stream);
     }
 
     public ActionToInvoke clone() {
         return new ActionToInvoke(this.target, this.actionItem.clone(), this.toPairs());
     }
 
     /**
      * should not tie this to actual technology, but URLs are the most common form of invoking
      * actions
      */
     public String makeActionUrl() {
         String url = target.endsWith("/") ? target : target + "/";
         url += actionItem.name;
         url = addToUrl(url);
         return LightAuth.wrapUrl(url);
     }
 
     public static ActionToInvoke fromActionUrl(String url) {
         return null;
     }
 
     /**
      * execute this action in a given context. The context must include me as well?
      * 
      * default implementation assumes i need to call an url and get the first line of response
      */
     public Object exec(ScriptContext ctx) {
         try {
             URL url = new URL(this.makeActionUrl());
             return Comms.readUrl(url.toExternalForm());
         } catch (MalformedURLException e) {
             throw new RuntimeException("while getting the command url: " + this.makeActionUrl(), e);
         }
     }
 
     public Renderer getRenderer(Technology technology) {
         return MyRenderer.singleton;
     }
 
     public static class MyRenderer implements Renderer {
         static MyRenderer singleton = new MyRenderer();
 
         public boolean canRender(Object o, Technology technology) {
             return o instanceof ActionToInvoke;
         }
 
         // TODO render on others like swing...
         public Object render(Object o, Technology technology, DrawStream stream) {
             ActionToInvoke ati = (ActionToInvoke) o;
             ActionItem cmd = ati.actionItem;
             String url = ati.makeActionUrl();
 
             NavLink b1 = new NavButton(cmd, url);
             b1.setTiny(ati.drawTiny);
             return b1.render(technology, stream);
         }
     }
 }
