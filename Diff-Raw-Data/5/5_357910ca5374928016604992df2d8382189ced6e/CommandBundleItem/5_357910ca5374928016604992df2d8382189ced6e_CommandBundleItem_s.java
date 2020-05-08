 package kkckkc.jsourcepad.model.bundle;
 
 import com.google.common.base.Function;
 import com.sun.net.httpserver.*;
 import kkckkc.jsourcepad.model.*;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.model.bundle.snippet.Snippet;
 import kkckkc.jsourcepad.util.Config;
 import kkckkc.jsourcepad.util.io.ScriptExecutor;
 import kkckkc.jsourcepad.util.io.ScriptExecutor.Execution;
 import kkckkc.jsourcepad.util.io.TransformingWriter;
 import kkckkc.jsourcepad.util.io.UISupportCallback;
 import kkckkc.syntaxpane.model.Interval;
 
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 import java.awt.*;
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Map;
 import java.util.concurrent.CountDownLatch;
 
 @SuppressWarnings("restriction")
 public class CommandBundleItem implements BundleItem<Void> {
 	
 	public static final String OUTPUT_SHOW_AS_HTML = "showAsHTML";
 	public static final String OUTPUT_DISCARD = "discard";
 	public static final String OUTPUT_REPLACE_SELECTED_TEXT = "replaceSelectedText";
 	public static final String OUTPUT_SHOW_AS_TOOLTIP = "showAsTooltip";
 	public static final String OUTPUT_INSERT_AS_SNIPPET = "insertAsSnippet";
 	public static final String OUTPUT_AFTER_SELECTED_TEXT = "afterSelectedText";
     public static final String OUTPUT_CREATE_NEW_DOCUMENT = "openAsNewDocument";
     public static final String OUTPUT_REPLACE_DOCUMENT = "replaceDocument";
 
 	public static final String INPUT_NONE = "none";
 	public static final String INPUT_DOCUMENT = "document";
 	public static final String INPUT_SELECTION = "selection";
     public static final String INPUT_CHARACTER = "character";
     public static final String INPUT_LINE = "line";
     public static final String INPUT_WORD = "word";
     public static final String INPUT_SCOPE = "scope";
 
     public static final String BEFORE_SAVE_ALL = "saveModifiedFiles";
     public static final String BEFORE_SAVE_ACTIVE = "saveActiveFile";
 
 	private String output;
 	private String command;
 	private String input;
 	private String fallbackInput;
     private String beforeRunning;
 	private BundleItemSupplier bundleItemSupplier;
 	private Interval virtualSelection;
 
     public CommandBundleItem(BundleItemSupplier bundleItemSupplier, String command, String input, String fallbackInput, String output, String beforeRunning) {
 		this.bundleItemSupplier = bundleItemSupplier;
 		this.command = command;
 		this.input = input;
 		this.fallbackInput = fallbackInput;
 		this.output = output;
         this.beforeRunning = beforeRunning;
 	}
 	
 	public static CommandBundleItem create(BundleItemSupplier bundleItemSupplier, Map<?, ?> m) {
 	    return new CommandBundleItem(bundleItemSupplier,
 	    		(String) m.get("command"),
 	    		(String) m.get("input"),
 	    		(String) m.get("fallbackInput"),
 	    		(String) m.get("output"),
                 (String) m.get("beforeRunningCommand"));
     }
 
 	
 	public interface ExecutionMethod {
 		public void start(ScriptExecutor scriptExecutor, String input, Map<String, String> environment) throws IOException, URISyntaxException;
 	}
 	
 	
 	public void execute(Window window, Void context) throws Exception {
         beforeRunning(window);
 
 		ScriptExecutor scriptExecutor = new ScriptExecutor(command, Application.get().getThreadPool());
 
 		WindowManager wm = Application.get().getWindowManager();
 
         String inputText = getInput(window);
 
         if (inputText == null) return;
 
 		ExecutionMethod executionMethod = createExecutionMethod(window, wm);
 		executionMethod.start(scriptExecutor, inputText, EnvironmentProvider.getEnvironment(window, bundleItemSupplier));
 	}
 
     @Override
     public BundleStructure.Type getType() {
         return BundleStructure.Type.COMMAND;
     }
 
     private void beforeRunning(Window window) {
         if (beforeRunning == null || "nop".equals(beforeRunning)) return;
 
         if (BEFORE_SAVE_ALL.equals(beforeRunning)) {
             for (Doc doc : window.getDocList().getDocs()) {
                 doc.save();
             }
         } else if (BEFORE_SAVE_ACTIVE.equals(beforeRunning)) {
             window.getDocList().getActiveDoc().save();
         } else {
             throw new RuntimeException("Unsupported 'beforeRunningCommand'-value: '" + beforeRunning + "'");
         }
     }
 
     private ExecutionMethod createExecutionMethod(Window window, WindowManager wm) {
 	    ExecutionMethod outputMethod;
 	    if (OUTPUT_SHOW_AS_HTML.equals(output)) {
 	    	outputMethod = new HtmlExectuionMethod(window, wm);
 	    } else {
 	    	outputMethod = new DefaultExecutionMethod(output, virtualSelection, window, wm);
 	    }
 	    return outputMethod;
     }
 	
 	private String getInput(Window window) throws BadLocationException {
 		String text;
 		if (! INPUT_NONE.equals(input)) {
 			text = getTextForInput(input, window);
 			if (text == null || "".equals(text)) {
 				text = getTextForInput(fallbackInput == null ? INPUT_DOCUMENT : fallbackInput, window);
 			}
 		} else {
 			text = "";
 		}
 		
 		return text;
     }
 
 	private String getTextForInput(String type, Window window) {
         virtualSelection = null;
 
 		Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
 		if (INPUT_SELECTION.equals(type)) {
 			return buffer.getText(buffer.getSelection());
 		} else if (INPUT_DOCUMENT.equals(type)) {
 			return buffer.getText(buffer.getCompleteDocument());
         } else if (INPUT_CHARACTER.equals(type)) {
             Interval iv = Interval.createWithLength(buffer.getInsertionPoint().getPosition(), 1);
             if (iv.getEnd() >= buffer.getLength()) return null;
             
             virtualSelection = iv;
             return buffer.getText(iv);
         } else if (INPUT_LINE.equals(type)) {
             virtualSelection = buffer.getCurrentLine();
             return buffer.getText(virtualSelection);
         } else if (INPUT_WORD.equals(type)) {
             virtualSelection = buffer.getCurrentWord();
             return buffer.getText(virtualSelection);
         } else if (INPUT_SCOPE.equals(type)) {
             virtualSelection = buffer.getCurrentScope();
             return buffer.getText(virtualSelection);
 
 		} else if (type == null) {
 			return null;
 		} else {
 			throw new RuntimeException("Unsupported input type " + type);
 		}
 	}
 	
 
 	
 	
 
 	public static class HtmlExectuionMethod implements ExecutionMethod {
 		private Window window;
 		private WindowManager wm;
 
 		public HtmlExectuionMethod(Window window, WindowManager wm) {
 			this.window = window;
 			this.wm = wm;
 		}
 
 		@Override
         public void start(final ScriptExecutor scriptExecutor, final String input, final Map<String, String> environment)
                 throws IOException, URISyntaxException {
 			String path = "/command/" + System.currentTimeMillis(); 
 			
 			final HttpServer server = Application.get().getHttpServer();
 			final HttpContext context = server.createContext(path);
 			context.setHandler(new HttpHandler() {
                 public void handle(HttpExchange exchange) throws IOException {
     				String requestMethod = exchange.getRequestMethod();
     				if (requestMethod.equalsIgnoreCase("GET")) {
     					Headers responseHeaders = exchange.getResponseHeaders();
     					responseHeaders.set("Content-Type", "text/html");
     					exchange.sendResponseHeaders(200, 0);
     					
     					final OutputStream responseBody = exchange.getResponseBody();
     					final Writer writer = new OutputStreamWriter(responseBody);
     					
     					final CountDownLatch cdl = new CountDownLatch(1);
     					
     					scriptExecutor.execute(new UISupportCallback(window.getContainer()) {
                             public void onAfterDone() {
 	            				cdl.countDown();
 	        					try {
 	                                writer.close();
                                 } catch (IOException e) {
 	                                throw new RuntimeException(e);
                                 }
                             }
     					}, new StringReader(input),
                                 new TransformingWriter(writer, TransformingWriter.CHUNK_BY_LINE, new Function<String, String>() {
                                     public String apply(String s) {
                                        s = s.replaceAll("txmt://open/\\?([^'\" \\t\\n\\x0B\\f\\r]+)",
                                                 "http://localhost:" + Config.getHttpPort() + "/cmd/open?windowId=" + window.getId() + "&$1");
                                        s = s.replaceAll("file://", "http://localhost:" + Config.getHttpPort() + "/files");
                                         return s;
                                     }
                                 }), environment);
     					
     					
     					try {
 	                        cdl.await();
                         } catch (InterruptedException e) {
                         	throw new RuntimeException(e);
                         }
         				server.removeContext(context);
     				}
                 }
 			});
 			
 			Desktop.getDesktop().browse(new URI("http://localhost:" + server.getAddress().getPort() + path));
         }
 	}
 	
 	public static class DefaultExecutionMethod implements ExecutionMethod {
 		private String output;
 		private Window window;
 		private WindowManager wm;
         private Interval virtualSelection;
 
         public DefaultExecutionMethod(String output, Interval virtualSelection, Window window, WindowManager wm) {
 			this.output = output;
             this.virtualSelection = virtualSelection;
 			this.window = window;
 			this.wm = wm;
 		}
 		
 		@Override
         public void start(ScriptExecutor scriptExecutor, final String input, Map<String, String> environment) throws IOException {
 	        scriptExecutor.execute(new UISupportCallback(window.getContainer()) {
                 public void onAfterSuccess(final Execution execution) {
                     String s = execution.getStdout();
                 	if (s == null) s = "";
 
         			if (OUTPUT_SHOW_AS_TOOLTIP.equals(output)) {
         				JOptionPane.showMessageDialog(window.getContainer(), s);
         			} else if (OUTPUT_REPLACE_SELECTED_TEXT.equals(output)) {
         				Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
         				Interval selection = buffer.getSelection();
                         if (selection == null || selection.isEmpty()) {
                             if (virtualSelection != null) {
                                 selection = virtualSelection;
                             } else {
                                 selection = new Interval(0, buffer.getLength());
                             }
                         }
 
         				buffer.replaceText(selection, s, null);
                     } else if (OUTPUT_AFTER_SELECTED_TEXT.equals(output)) {
                         Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
                         Interval selection = buffer.getSelection();
                         if (selection == null || selection.isEmpty()) {
                             if (virtualSelection != null) {
                                 selection = virtualSelection;
                             } else {
                                 selection = new Interval(0, buffer.getLength());
                             }
                         }
 
                         buffer.insertText(selection.getEnd(), s, null);
 
                     } else if (OUTPUT_REPLACE_DOCUMENT.equals(output)) {
                         Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
         				Interval selection = buffer.getCompleteDocument();
                         buffer.replaceText(selection, s, null);
 
                     } else if (OUTPUT_CREATE_NEW_DOCUMENT.equals(output)) {
                         Doc doc = window.getDocList().create();
                         doc.getActiveBuffer().insertText(0, s, null);
 
         			} else if (OUTPUT_DISCARD.equals(output)) {
         				// Do nothing
 
                     } else if (OUTPUT_INSERT_AS_SNIPPET.equals(output)) {
                         Buffer b = window.getDocList().getActiveDoc().getActiveBuffer();
 
                         Interval selection = b.getSelection();
                         if (selection == null || selection.isEmpty()) {
                             if (virtualSelection != null) {
                                 selection = virtualSelection;
                             } else {
                                 selection = new Interval(0, b.getLength());
                             }
                         }
 
                         b.remove(selection);
 
                         Snippet snippet = new Snippet(s, null);
                         snippet.insert(window, b);
 
         			} else {
         				throw new RuntimeException("Unsupported output " + output);
         			}
                 }
 	        }, new StringReader(input), environment);
         }
 	}
 }
