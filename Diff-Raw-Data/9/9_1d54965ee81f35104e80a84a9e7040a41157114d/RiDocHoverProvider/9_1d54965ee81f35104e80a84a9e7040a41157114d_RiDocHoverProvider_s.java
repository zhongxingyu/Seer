 package org.rubypeople.rdt.internal.ui.text.ruby.hover;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPartitioningException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension3;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.rubypeople.rdt.core.ICodeAssist;
 import org.rubypeople.rdt.core.IMethod;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.internal.launching.LaunchingPlugin;
 import org.rubypeople.rdt.internal.launching.StandardVMType;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.text.IRubyPartitions;
 import org.rubypeople.rdt.launching.IVMInstall;
 import org.rubypeople.rdt.launching.RubyRuntime;
 
 public class RiDocHoverProvider extends AbstractRubyEditorTextHover {
 	
 	/*
 	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
 	 */
 	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
 		
 		/*
 		 * The region should be a word region an not of length 0.
 		 * This check is needed because codeSelect(...) also finds
 		 * the Ruby element if the offset is behind the word.
 		 */
 		if (hoverRegion.getLength() == 0)
 			return null;
 		
 		ICodeAssist resolve= getCodeAssist();
 		if (resolve != null) {
 			try {
 				IRubyElement[] result= resolve.codeSelect(hoverRegion.getOffset(), hoverRegion.getLength());
 				if (result != null && result.length > 0) {
 					return getHoverInfo(result);
 				}
 			} catch (RubyModelException x) {
 				return null;
 			}
 		}
 		try {
 			IDocument doc = textViewer.getDocument();		
 			if (doc == null) return null;
 			String contentType = null;
 			if (doc instanceof IDocumentExtension3) {
 				IDocumentExtension3 extension = (IDocumentExtension3) doc;		
 				try {
 					contentType = extension.getContentType(IRubyPartitions.RUBY_PARTITIONING, hoverRegion.getOffset(), false);
 				} catch (BadPartitioningException e) {
 					// ignore
 				}
 			}
 			if (contentType != null && !contentType.equals(IRubyPartitions.RUBY_DEFAULT)) {
 				return null;
 			}			
 			String symbol = doc.get(hoverRegion.getOffset(), hoverRegion.getLength());	
 			if (symbol != null && (symbol.startsWith("@") || symbol.startsWith("$") || symbol.startsWith(":"))) return null; // don't try class/instance/global variables or symbols
 			return getRIResult(symbol);
 		} catch (BadLocationException e) {
 			// ignore
 		}
 		return null;
 	}
 	
     protected File getFRIIndexFile() {
 		return getStateFile(".fastri-index");
 	}
     
     private File getStateFile(String name) {
     	IPath location = RubyPlugin.getDefault().getStateLocation();
 		location = location.append(name);
 		return location.toFile();
     }
     
     private String getFastRiServerPath() {
     	return getFilePath(new Path("ruby").append("fastri-server"));
 	}
     
     private String getFastRiPath() {
 		return getFilePath(new Path("ruby").append("fri"));
 	}
     
     private String getFilePath(IPath path) {
     	File file = LaunchingPlugin.getFileInPlugin(path);
 		if (file == null || !file.exists() || !file.isFile()) return null;
 		return file.getAbsolutePath();
     }
     
     private String execAndReadOutput(String file, List<String> commands) {
 		if (file == null) return null;
 		StringBuffer buffer = new StringBuffer();
 		try {
 			List<String> line = new ArrayList<String>();
 			IVMInstall vm = RubyRuntime.getDefaultVMInstall();
 			if (vm == null) return "";
			File executable = StandardVMType.findRubyExecutable(vm.getInstallLocation());
 			if (executable.getName().contains("rubyw")) {
 				String name = executable.getName();
 				name = name.replace("rubyw", "ruby");
 				executable = new File(executable.getParent() + File.separator + name);
 			}
 			line.add(executable.getAbsolutePath());
 			line.add(file);
 			for (String command : commands) {
 				line.add(command);
 			}
 			File workingDirectory = new File(file).getParentFile();
 			String[] cmdLine = new String[line.size()];
 			cmdLine = line.toArray(cmdLine);
 			Process p = DebugPlugin.exec(cmdLine, workingDirectory);    			
 			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			String liner = null;    		
 			while (!reader.ready()) {
 				Thread.yield();
 			}
 			while ((liner = reader.readLine()) != null) {
 				buffer.append(liner);
 				buffer.append("\n");
 				if (!reader.ready()) {
 					Thread.yield();
 				}
 			}
 			p.waitFor();
 		} catch (CoreException e) {
 			RubyPlugin.log(e);
 			return "";
 		} catch (IOException e) {
 			RubyPlugin.log(e);
 			return "";
 		} catch (InterruptedException e) {
 			RubyPlugin.log(e);
 		}
 		buffer.deleteCharAt(buffer.length() - 1); // remove last \n
 		return buffer.toString();
 	}
 	
 	private String getRIResult(String symbol) {
 		if (symbol == null || symbol.trim().length() == 0) return null;		
 		File file = getFRIIndexFile();
     	if (!file.exists()) {
     		List<String> commands = new ArrayList<String>();            	
         	commands.add("--index-file=\"" + file.getAbsolutePath() + "\"");
         	commands.add("-b");
     		String output = execAndReadOutput(getFastRiServerPath(), commands);
     	}
 	
     	List<String> commands = new ArrayList<String>();  
     	commands.add("--no-pager");
     	commands.add("--index-file=\"" + file.getAbsolutePath() + "\"");
 		String content = execAndReadOutput(getFastRiPath(), commands);			
 		if (content == null) return null;	
 		if (content.indexOf("More than one method matched your request") > -1) return null;
 		return content;			
 	}
 	
 	@Override
 	protected String getHoverInfo(IRubyElement[] rubyElements) {
 		if (rubyElements == null || rubyElements.length == 0) return null;
 		String symbol = getRICompatibleName(rubyElements[0]);
 		if (symbol == null) return null;
 		return getRIResult(symbol);
 	}
 
 	private String getRICompatibleName(IRubyElement element) {
 		switch (element.getElementType()) {
 		case IRubyElement.TYPE:
 			return ((IType) element).getFullyQualifiedName();
 		case IRubyElement.METHOD:
 			IMethod method = (IMethod) element;
 			String delimeter = method.isSingleton() ? "::" : "#";
 			return method.getDeclaringType().getFullyQualifiedName() + delimeter + element.getElementName();
 
 		default:
 			return null;
 		}
 	}
 }
