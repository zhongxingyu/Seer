 package com.royvandewater.django_testing.handlers;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class TestHandler extends AbstractHandler
 {
     private static final Pattern functionPattern = Pattern.compile("(\\w+)\\s+fed\\s*$", Pattern.MULTILINE);
     private static final Pattern classPattern = Pattern.compile("(\\w+)\\s+ssalc\\s*$", Pattern.MULTILINE);
     private static final String consoleName = "Django test console";
     private static final String lastCommand = "lastCommand";
 
     public TestHandler()
     {
     }
 
     public Object execute(ExecutionEvent event) throws ExecutionException
     {
         startTest();
         return null;
     }
 
     private static void startTest()
     {
         final String currentTest = getCurrentTest();
         if (currentTest == null) {
 
             Shell parentOrShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
             MessageDialog.openError(parentOrShell, "Test name not found.", "Are you in a django application file?");
 
             return;
         }
 
         final IProject project = getCurrentFile().getProject();
         try {
             project.setPersistentProperty(new QualifiedName("", lastCommand), currentTest);
         } catch (CoreException e) {
             e.printStackTrace();
         }
 
         new Job("Running Django Test") {
             protected IStatus run(IProgressMonitor monitor)
             {
                 runTest(currentTest, project, monitor);
                 return Status.OK_STATUS;
             }
         }.schedule();
     }
 
     private static void runTest(String currentTest, IProject project, IProgressMonitor monitor)
     {
         Process process;
         try {
 
             File current_directory = project.getLocation().append("src").append(project.getName()).toFile();
             String[] cmd = new String[] { "python", "-u", "manage.py", "test", currentTest };
             process = Runtime.getRuntime().exec(cmd, null, current_directory);
 
             MessageConsoleStream messageStream = getConsole().newMessageStream();
             messageStream.setActivateOnWrite(true);
 
             InputStreamReader stdout = new InputStreamReader(process.getInputStream());
             InputStreamReader stderr = new InputStreamReader(process.getErrorStream());
 
             while (!monitor.isCanceled()) {
                 boolean done;
                 try {
                     process.exitValue();
                     done = true;
                 } catch (IllegalThreadStateException e) {
                     done = false;
                 }
 
                 while (stdout.ready())
                     messageStream.print(String.valueOf((char)stdout.read()));
                 while (stderr.ready())
                     messageStream.print(String.valueOf((char)stderr.read()));
 
                 if (done)
                     break;
                 messageStream.flush();
                 Thread.sleep(100);
             }
 
             if (monitor.isCanceled())
                 process.destroy();
 
         } catch (IOException e) {
             throw new RuntimeException(e);
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     private static MessageConsole getConsole()
     {
         IConsoleManager conMan = ConsolePlugin.getDefault().getConsoleManager();
 
         for (IConsole console : conMan.getConsoles())
             if (console.getName().equals(consoleName))
                 return (MessageConsole)console;
 
         MessageConsole console = new MessageConsole(consoleName, null);
         conMan.addConsoles(new IConsole[] { console });
         return console;
     }
 
     private static String getCurrentTest()
     {
         IEditorPart active_editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
         if (!(active_editor instanceof ITextEditor))
             return null;
 
         ITextEditor editor = (ITextEditor)active_editor;
         IFile editorFile = (IFile)editor.getEditorInput().getAdapter(IFile.class);
         IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
         ISelection selection = editor.getSelectionProvider().getSelection();
 
         if (!(selection instanceof TextSelection))
             return null;
 
         TextSelection textSelection = (TextSelection)selection;
 
         String full_text = document.get();
         String full_reversed_text = reverseString(full_text);
         int reversed_offset = full_reversed_text.length() - textSelection.getOffset();
 
         String functionTitle = getBackwardsThing(full_reversed_text, reversed_offset, functionPattern);
         String classTitle = getBackwardsThing(full_reversed_text, reversed_offset, classPattern);
 
         if (editorFile == null)
             return null;
 
         IPath fullPath = editorFile.getFullPath();
         String folderName = fullPath.removeLastSegments(1).lastSegment();
 
         if (folderName == null)
             return ""; // Should run all python tests
         
        if (!folderName.equals("tests.py"))
             return getLastTest(editorFile);
 
         if (classTitle == null)
             return folderName;

         if (functionTitle == null)
             return join(".", folderName, classTitle);
 
         if (!functionTitle.startsWith("test"))
             return getLastTest(editorFile);
         
         
         
 
         return join(".", folderName, classTitle, functionTitle);
     }
     
     private static String getLastTest(IFile editorFile) {
         try {
             return editorFile.getProject().getPersistentProperty(new QualifiedName("", lastCommand));
         } catch (CoreException e) {
             return null;
         }    
     }
 
     private static IFile getCurrentFile()
     {
         IEditorPart active_editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
         if (!(active_editor instanceof ITextEditor))
             return null;
 
         ITextEditor editor = (ITextEditor)active_editor;
         return (IFile)editor.getEditorInput().getAdapter(IFile.class);
 
     }
 
     private static String getBackwardsThing(String full_reversed_text, int reversed_offset, Pattern pattern)
     {
         Matcher matcher = pattern.matcher(full_reversed_text);
 
         if (!matcher.find(reversed_offset))
             return null;
 
         return reverseString(matcher.group(1));
     }
 
     private static String reverseString(String str)
     {
         char[] reversed_str = new char[str.length()];
         char[] char_array = str.toCharArray();
         for (int i = 0; i < char_array.length; i++)
             reversed_str[char_array.length - i - 1] = char_array[i];
 
         return new String(reversed_str);
     }
 
     public static <T> String join(T[] array, String delimiter)
     {
         if (array.length == 0)
             return "";
         StringBuilder builder = new StringBuilder();
         builder.append(array[0]);
         for (int i = 1; i < array.length; i++)
             builder.append(delimiter).append(array[i]);
         return builder.toString();
     }
 
     public static <T> String join(String delimiter, T... array)
     {
         return join(array, delimiter);
     }
 
 }
