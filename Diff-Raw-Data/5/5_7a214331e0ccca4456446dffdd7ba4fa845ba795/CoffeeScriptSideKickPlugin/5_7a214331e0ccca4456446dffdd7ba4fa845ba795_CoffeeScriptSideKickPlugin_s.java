 package sidekick.coffeescript;
 
 import org.gjt.sp.util.StringList;
 import org.gjt.sp.jedit.jEdit;
 import org.gjt.sp.jedit.Buffer;
 import org.gjt.sp.jedit.View;
 import org.gjt.sp.jedit.textarea.TextArea;
 import org.gjt.sp.jedit.textarea.Selection;
 import org.gjt.sp.jedit.EditPlugin;
 
 import errorlist.ErrorSource;
 import errorlist.DefaultErrorSource;
 
 public class CoffeeScriptSideKickPlugin extends EditPlugin {
     public static final String NAME = "sidekick.coffeescript";
     public static final String OPTION_PREFIX = "options.coffeescript.";
     private static DefaultErrorSource errorSource;
 
     private static void startErrorSource() {
         if (errorSource == null) {
             errorSource = new DefaultErrorSource("CoffeeScript");
             ErrorSource.registerErrorSource(errorSource);
         }
         errorSource.clear();
     }
 
     public static void compile(View view) {
         StringList results = new StringList();
        ICoffeeScriptParser parser = new CoffeeScriptParser();
        ParserConfig config = new ParserConfig(view.getBuffer(), errorSource);
         TextArea textArea = view.getTextArea();
         startErrorSource();
         if (textArea.getSelectionCount() == 0) {
             results.add(parser.compile(textArea.getText(), config));
         } else {
             for (Selection sel : textArea.getSelection()) {
                 config.line = sel.getStartLine();
                 results.add(
                     parser.compile(textArea.getSelectedText(sel), config));
             }
         }
         if (errorSource.getErrorCount() == 0) {
             Buffer buffer = jEdit.newFile(view.getEditPane());
             buffer.insert(0, results.join("\n"));
             buffer.setMode("javascript");
             buffer.setDirty(false);
         }
     }
 
     @Override
     public void stop() {
         if (errorSource != null) {
             ErrorSource.unregisterErrorSource(errorSource);
         }
     }
 }
