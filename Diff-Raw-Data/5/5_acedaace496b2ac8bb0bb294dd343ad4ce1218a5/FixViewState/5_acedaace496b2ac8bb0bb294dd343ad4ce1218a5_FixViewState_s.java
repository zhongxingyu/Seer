 package org.icefaces.impl.event;
 
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.component.UIForm;
 import javax.faces.component.UIOutput;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ComponentSystemEvent;
 import javax.faces.event.SystemEvent;
 import javax.faces.event.SystemEventListener;
 import java.io.IOException;
 import java.text.StringCharacterIterator;
 
 public class FixViewState implements SystemEventListener {
     private static final String ID_SUFFIX = "_fixviewstate";
 
     public void processEvent(final SystemEvent event) throws AbortProcessingException {
         final UIForm form = (UIForm) ((ComponentSystemEvent) event).getComponent();
         final String formClientID = form.getClientId();
        final FacesContext context = FacesContext.getCurrentInstance();
        final String id = context.getViewRoot().createUniqueId() + ID_SUFFIX;
 
         UIOutput output = new ScriptWriter(formClientID);
         output.setTransient(true);
        output.setId(id);
         form.getParent().getChildren().add(output);
     }
 
     public boolean isListenerForSource(final Object source) {
         return EnvUtils.isICEfacesView(FacesContext.getCurrentInstance()) && source instanceof UIForm;
     }
 
     private static class ScriptWriter extends UIOutputWriter {
         private String formClientID;
 
         public ScriptWriter(String formClientID) {
             this.formClientID = formClientID;
         }
 
         public void encode(ResponseWriter writer, FacesContext context) throws IOException {
             String clientID = getClientId(context);
             writer.startElement("span", this);
             writer.writeAttribute("id", clientID, null);
             if (context.isPostback()) {
                 writer.startElement("script", this);
                 writer.writeAttribute("type", "text/javascript", null);
                 String viewState = context.getApplication().getStateManager().getViewState(context);
                 writer.writeText("ice.fixViewState('" + formClientID + "', '" + escapeJSString(viewState) + "');", null);
                 writer.endElement("script");
             }
             writer.endElement("span");
         }
     }
 
     public static String escapeJSString(String text) {
         final StringBuilder result = new StringBuilder();
         StringCharacterIterator iterator = new StringCharacterIterator(text);
         char character = iterator.current();
         while (character != StringCharacterIterator.DONE) {
             if (character == '\"') {
                 result.append("\\\"");
             } else if (character == '\\') {
                 result.append("\\\\");
             } else if (character == '/') {
                 result.append("\\/");
             } else if (character == '\b') {
                 result.append("\\b");
             } else if (character == '\f') {
                 result.append("\\f");
             } else if (character == '\n') {
                 result.append("\\n");
             } else if (character == '\r') {
                 result.append("\\r");
             } else if (character == '\t') {
                 result.append("\\t");
             } else {
                 //the char is not a special one
                 //add it to the result as is
                 result.append(character);
             }
             character = iterator.next();
         }
         return result.toString();
     }
 
 }
