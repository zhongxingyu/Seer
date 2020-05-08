 package org.wingx.plaf.css;
 
 import org.wingx.XRichTextEditor;
 import org.wings.io.Device;
 import org.wings.io.StringBuilderDevice;
 import org.wings.plaf.Update;
 import org.wings.plaf.css.Utils;
 import org.wings.plaf.css.AbstractUpdate;
 import org.wings.plaf.css.UpdateHandler;
 import org.wings.plaf.css.AbstractComponentCG;
 import org.wings.SComponent;
 import org.wings.SLabel;
 import org.wings.SFrame;
 import org.wings.session.SessionManager;
 
 import java.io.IOException;
 import java.util.Random;
 
 public class RichTextEditorCG extends AbstractComponentCG<XRichTextEditor> implements org.wingx.plaf.RichTextEditorCG<XRichTextEditor> {
     public String getText(XRichTextEditor component) {
         String text = component.getText();
         text = text.replaceAll("\n", "<BR />");
         text = text.replaceAll("'", "\'");
 
         return text;
     }
 
     public void writeInternal(Device device, final XRichTextEditor component) throws IOException {
         device.print("<textarea onload=\"javascript:alert('test');\"");
         Utils.optAttribute(device, "id", component.getName());
         Utils.optAttribute(device, "eid", component.getName());
         Utils.optAttribute(device, "name", component.getName());
         device.print(">");
         device.print(getText(component));
         device.print("</textarea>");
     }
 
     public Update getTextUpdate(XRichTextEditor component) {
         return new TextUpdate(component);
     }
 
     private class TextUpdate extends AbstractUpdate {
         XRichTextEditor editor;
 
         public TextUpdate(XRichTextEditor component) {
             super(component);
 
             this.editor = component;
         }
 
         public Handler getHandler() {
             UpdateHandler update = new UpdateHandler("runScript");
             String name = "editor_" + component.getName();
             update.addParameter("wingS.global.onHeadersLoaded(function() { window." + name + ".setEditorHTML('" + getText(editor) + "'); });");
             // TODO: update size, border, etc.
             return update;
         }
     }
 }
