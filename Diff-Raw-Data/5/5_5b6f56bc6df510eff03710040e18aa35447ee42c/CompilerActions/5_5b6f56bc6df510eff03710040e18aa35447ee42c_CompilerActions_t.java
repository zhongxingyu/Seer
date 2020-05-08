 /*
  * Created on Thursday, August 25 2011 00:29
  */
 package com.mbien.opencl.editor.compiler;
 
 import org.openide.cookies.LineCookie;
 import org.openide.cookies.OpenCookie;
 import org.openide.loaders.DataObject;
 import org.openide.text.Line;
 import org.openide.windows.OutputEvent;
 import org.openide.windows.OutputListener;
 import com.jogamp.opencl.CLContext;
 import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLException;
 import com.jogamp.opencl.CLException.CLBuildProgramFailureException;
 import com.jogamp.opencl.CLPlatform;
 import com.jogamp.opencl.CLProgram;
 import com.mbien.opencl.editor.CLUtil;
 import com.mbien.opencl.service.CLService;
 import com.mbien.opencl.editor.file.CLDataObject;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import org.openide.awt.ActionID;
 import org.openide.awt.ActionReference;
 import org.openide.awt.ActionReferences;
 import org.openide.awt.ActionRegistration;
 import org.openide.util.Exceptions;
 import org.openide.util.actions.Presenter;
 import org.openide.windows.IOProvider;
 import org.openide.windows.InputOutput;
 import org.openide.windows.IOColorLines;
 import java.io.IOException;
 import java.awt.Color;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import javax.swing.DefaultComboBoxModel;
 import org.openide.util.Lookup;
 import static java.awt.Color.*;
 
 /**
  *
  * @author mbien
  */
 public class CompilerActions {
 
     private final static Color DARK_GREEN = new Color(0, 153, 0);
 
     private CompilerActions() { }
 
     @ActionID(category = "CLPlatform", id = "com.mbien.opencl.editor.compiler.CLCompileAction")
     @ActionRegistration(displayName = "Compile Kernel", iconBase = "com/mbien/opencl/editor/compiler/clbuild.png")
     @ActionReferences({
         @ActionReference(path = "Editors/text/x-opencl/Toolbars/Default", position = 20400),
         @ActionReference(path = "Loaders/text/x-opencl/Actions", position = 810),
         @ActionReference(path = "Shortcuts", name="F9")
     })
     public static class CompileAction implements ActionListener {
 
         private final List<CLDataObject> daos;
         private final InputOutput io;
 
         public CompileAction(List<CLDataObject> context) {
             this.daos = context;
             this.io = IOProvider.getDefault().getIO("OpenCL Compiler Output", false);
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             compile(daos);
         }
 
         private void compile(List<CLDataObject> daos) {
 
             io.select();
 
             CLPlatform platform = getCLService().getDefaultPlatform();
 
             println("\nbuild using "+platform.getName() +" platform", GRAY);
 
             CLContext context = CLContext.create(platform);
 
             try{
                 String[] sources = new String[daos.size()];
                 for (int i = 0; i < daos.size(); i++) {
                     sources[i] = daos.get(i).getText();
                 }
 
                 CLProgram program = context.createProgram(sources);
 
                 long delta = System.currentTimeMillis();
                 boolean success = true;
                 try{
                     program.build();
                 }catch(CLBuildProgramFailureException ignore) {
                     success = false;
                }catch(CLException ex) {
                    success = false;
                    println("warning: received "+ex.getCLErrorString(), ORANGE);
                 }
                 delta = System.currentTimeMillis()-delta;
 
                 CLDevice[] devices = context.getDevices();
                 for (CLDevice device : devices) {
                     println("log for "+CLUtil.cleanName(device), GRAY);
 
                     String log = program.getBuildLog(device);
                     if(log.isEmpty()) {
                         println("<empty>", BLACK);
                     }else{
                         try {
                             printLog(daos, device, log);
                         } catch (IOException ex) {
                             Exceptions.printStackTrace(ex);
                             println(log, BLACK);
                         }
                     }
                 }
                 println(delta+"ms", GRAY);
 
                 if(success) {
                     println("success", DARK_GREEN);
                 }else{
                     println("compilation failed", RED);
                 }
                 
             }finally{
                 context.release();
             }
 
             io.getOut().close();
 
         }
 
         private void printLog(List<CLDataObject> daos, CLDevice device, String log) throws IOException {
 
             // check if the compiler wants to tell us something
             // if there are any parsable messages we will hyperlink them in the console
             List<CompilerMessage> messages = CompilerMessage.parse(device, log);
 
             if(messages.isEmpty()) {
 
                 println(log, BLACK);
 
             }else{
 
                 Map<Integer, CompilerMessage> msgMap = new HashMap<>(messages.size());
                 for (CompilerMessage msg : messages) {
                     msgMap.put(msg.getLineInLog(), msg);
                 }
 
                 Scanner scanner = new Scanner(log).useDelimiter("\n");
                 int line = 0;
                 while(scanner.hasNext()) {
                     String part = scanner.next();
                     CompilerMessage msg = msgMap.get(line);
                     if(msg != null) {
                         io.getOut().println(part, new HyperlinkAction(daos.get(0), msg.getLine()));
                     }else{
                         println(part, BLACK);
                     }
 
                     line++;
                 }
             }
 
         }
 
         private void println(String line, Color color) {
             try {
                 IOColorLines.println(io, line, color);
             } catch (IOException ignore) {
                 io.getOut().print(line);
             }
         }
 
         private static class HyperlinkAction implements OutputListener {
 
             private final int line;
             private final DataObject dao;
 
             private HyperlinkAction(DataObject dao, int lineNumber) {
                 this.line = lineNumber;
                 this.dao = dao;
             }
 
             public void outputLineAction(OutputEvent e) {
 
                 //open file in editor and go to annotated line
                 dao.getCookie(OpenCookie.class).open();
 
                 LineCookie lineCookie = dao.getCookie(LineCookie.class);
 
                 if (line > 0 && line < lineCookie.getLineSet().getLines().size()) {
                     Line current = lineCookie.getLineSet().getCurrent(line - 1);
                     current.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FRONT);
                 }
 
             }
 
             public void outputLineCleared(OutputEvent arg0) { }
 
             public void outputLineSelected(OutputEvent arg0) { }
         }
 
     }
 
     @ActionID(category = "CLPlatform", id = "com.mbien.opencl.editor.compiler.CLPlatformAction")
     @ActionRegistration(displayName = "")
     @ActionReference(path = "Editors/text/x-opencl/Toolbars/Default", position = 20300)
     public static class CLPlatformAction extends AbstractAction implements Presenter.Toolbar {
 
         private JComboBox<CLPlatform> box;
         private DefaultComboBoxModel<CLPlatform> model;
 
         @Override
         public void actionPerformed(ActionEvent e) {
             getCLService().setDefaultPlatform((CLPlatform)box.getSelectedItem());
         }
 
         @Override
         public Component getToolbarPresenter() {
 
             CLService service = getCLService();
 
             // one model instance shared between n components
             if(model == null) {
                 CLPlatform[] platforms = service.listCLPlatforms();
                 model = new DefaultComboBoxModel<>(platforms);
             }
 
             box = new JComboBox<>(model);
             box.setRenderer(new DefaultListCellRenderer() {
 
                 @Override
                 public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                     Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                     CLPlatform platform = (CLPlatform)value;
                     setText(platform.getName());
                     return component;
                 }
                 
             });
             box.setPreferredSize(new Dimension(150, box.getPreferredSize().height));
 
             CLPlatform platform = service.getDefaultPlatform();
             if(platform != null) {
                 box.setSelectedItem(platform);
             }
             box.addActionListener(this);
 
             return box;
         }
 
     }
 
     private static CLService getCLService() {
         return Lookup.getDefault().lookup(CLService.class);
     }
 
 }
