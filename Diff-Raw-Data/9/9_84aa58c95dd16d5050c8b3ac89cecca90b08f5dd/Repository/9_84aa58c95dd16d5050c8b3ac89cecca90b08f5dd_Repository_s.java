 package pp.eclipse.open;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import pp.eclipse.open.parse.Parser;
 
 public class Repository
 {
 	private final IContainer root;
 	private final Parser[] parsers;
 
 	public Repository(IContainer root, Parser... parsers) 
 	{
 		this.root = root;
 		this.parsers = parsers;
 	}
 	
 	public List<Container> list(final IProgressMonitor monitor) 
 		throws CoreException
 	{
 	    final List<Container> containers = new ArrayList<Container>();
 	    root.accept(new IResourceProxyVisitor() {
 	        public boolean visit(IResourceProxy proxy) throws CoreException {
 	            if (proxy.getName().matches(".*\\.xml")) {
 	                IResource resource = proxy.requestResource();
 	                if (resource instanceof IFile) {
 	                    Container read = read((IFile) resource);
 	                    if (read != null) {
 	                        containers.add(read);
 	                        monitor.worked(1);
 	                    }
 	                }
 	            }
 	            return true;
 	        }
 
 	    }, 0);
 	    return containers;
 	}
 	       
 	public boolean validate(Item item) {
 		return true;
 	}
 
     private Container read(IFile iResource) 
        throws CoreException 
     {
         InputStream content = null;
         try {
             content = iResource.getContents();
             String charset = iResource.getCharset();
             if (charset == null) {
                 charset = "UTF8";
             }
             try {
                 List<Item> parsed = Collections.emptyList();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(content, charset));
                 reader.mark(1024);
                 for (Parser parser : parsers) {
                     reader.reset();
                     parsed = parser.parse(reader);
                     if (parsed.size() > 0) { 
                         break;
                     }
                 }
                 List<Item> updated = new ArrayList<Item>();
                 IPath fullPath = iResource.getFullPath();
                 for (Item parse : parsed) {
                     updated.add(parse.path(fullPath));
                 }
                 return new Container(fullPath, iResource.getModificationStamp(), updated);
             } catch (UnsupportedEncodingException e) {
                 Logger.getLogger("pp.eclipse.parse").fine("Parse of " + iResource.getName() + " failed: " + e.getMessage());
                 Logger.getLogger("pp.eclipse.parse").log(Level.FINER, "Parse failure", e);
             } catch (Exception e) {
                 Logger.getLogger("pp.eclipse.parse").fine("Parse of " + iResource.getName() + " failed: " + e.getMessage());
                 Logger.getLogger("pp.eclipse.parse").log(Level.FINER, "Parse failure", e);
             }
        } finally {
             if (content != null) {
                 try { 
                     content.close();
                 } catch (Exception e) {
                     // Skip
                 }
             }
         }
         return null;
     }
 }
