 package org.wingx;
 
 import org.wings.*;
 import org.wings.session.SessionManager;
 import org.wings.script.ScriptListener;
 import org.wings.script.JavaScriptListener;
 import org.wings.event.SDocumentEvent;
 import org.wingx.plaf.RichTextEditorCG;
 
 public class XRichTextEditor extends STextComponent {
     public final static int SIMPLE_EDITOR = 0;
     public final static int NORMAL_EDITOR = 1;
 
     private int editorType;

     public XRichTextEditor() {
         this(SIMPLE_EDITOR);
     }
 
     public XRichTextEditor(int editorType) {
         super();
         
         this.editorType = editorType;
 
        final StringBuilder builder = new StringBuilder();
 
         builder.append("wingS.global.onHeadersLoaded(function() {");
         String name = "editor_" + getName();
         String height = "";
         String width = "";
         if(getPreferredSize() != null) {
             height = getPreferredSize().getHeight();
             width = getPreferredSize().getWidth();
         } else {
             height = "100%";
             width = "100%";
         }
         String editor = "SimpleEditor";
         builder.append("window." + name + " = new YAHOO.widget." + editor + "('" + getName() + "', " +
                 "{" +
                     "height:\"" + height + "\"," +
                     "width:\"" + width + "\"," +
                     "toolbar: {" +
                         "titlebar: 'Text Editor'," +
                         "collapse:false,"+
                         "buttons: ["+
                             "{ group: 'textstyle', label: 'Font Style',"+
                                 "buttons: ["+
                                     "{ type: 'push', label: 'Bold', value: 'bold' },"+
                                     "{ type: 'push', label: 'Italic', value: 'italic' },"+
                                     "{ type: 'push', label: 'Underline', value: 'underline' },"+
                                     "{ type: 'separator' },"+
                                     "{ type: 'select', label: 'Arial', value: 'fontname', disabled: true,"+
                                         "menu: ["+
                                             "{ text: 'Arial', checked: true },"+
                                             "{ text: 'Arial Black' },"+
                                             "{ text: 'Comic Sans MS' },"+
                                             "{ text: 'Courier New' },"+
                                             "{ text: 'Lucida Console' },"+
                                             "{ text: 'Tahoma' },"+
                                             "{ text: 'Times New Roman' },"+
                                             "{ text: 'Trebuchet MS' },"+
                                             "{ text: 'Verdana' }"+
                                         "]"+
                                     "},"+
                                     "{ type: 'spin', label: '13', value: 'fontsize', range: [ 9, 75 ], disabled: true },"+
                                     "{ type: 'separator' },"+
                                     "{ type: 'color', label: 'Font Color', value: 'forecolor', disabled: true },"+
                                     "{ type: 'color', label: 'Background Color', value: 'backcolor', disabled: true }"+
                                 "]"+
                             "}"+
                         "]"+
                     "}"+
                 "});\n");
         builder.append(name + ".on('afterNodeChange', function(o) { var elt = document.getElementById('" + getName() + "'); " + name + ".saveHTML(); }, " + name + ", true);");
         builder.append(name + ".on('editorKeyUp', function(o) { var elt = document.getElementById('" + getName() + "'); " + name + ".saveHTML(); }, " + name + ", true);");
         builder.append(name + ".on('afterRender', function(o) { var elt = document.getElementById('" + getName() + "'); " + name + ".setEditorHTML('" + getText(this) + "'); }, true);");
         builder.append(name + ".render();");
         builder.append("});");
 
        SessionManager.getSession().getScriptManager().addScriptListener(new JavaScriptListener(null, null, builder.toString()));
     }
 
     public String getText(XRichTextEditor component) {
         String text = component.getText();
         text = text.replaceAll("\n", "<BR />");
         text = text.replaceAll("'", "\'");
 
         return text;
     }
 
     @Override
     public void changedUpdate(SDocumentEvent e) {
         if(isUpdatePossible()) {
             update(((RichTextEditorCG)getCG()).getTextUpdate(this));
         }
     }
 
     public int getEditorType() {
         return editorType;
     }
 }
