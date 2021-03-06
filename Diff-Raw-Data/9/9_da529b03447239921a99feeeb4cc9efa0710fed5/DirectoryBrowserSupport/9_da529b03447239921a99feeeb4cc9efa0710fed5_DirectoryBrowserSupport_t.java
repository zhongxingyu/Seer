 package hudson.model;
 
 import hudson.FilePath;
 import hudson.Util;
 import hudson.FilePath.FileCallable;
 import hudson.remoting.VirtualChannel;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.DirectoryScanner;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
 
 /**
  * Has convenience methods to serve file system.
  *
  * <p>
  * This object can be used in a mix-in style to provide a directory browsing capability
  * to a {@link ModelObject}. 
  *
  * @author Kohsuke Kawaguchi
  */
 public final class DirectoryBrowserSupport {
 
     public final ModelObject owner;
     
     public final String title;
 
     /**
      * @deprecated
      *      Use {@link #DirectoryBrowserSupport(ModelObject, String)}
      */
     public DirectoryBrowserSupport(ModelObject owner) {
         this(owner,owner.getDisplayName());
     }
 
     /**
      * @param owner
      *      The parent model object under which the directory browsing is added.
      * @param title
      *      Used in the HTML caption. 
      */
     public DirectoryBrowserSupport(ModelObject owner, String title) {
         this.owner = owner;
         this.title = title;
     }
 
     /**
      * Serves a file from the file system (Maps the URL to a directory in a file system.)
      *
      * @param icon
      *      The icon file name, like "folder-open.gif"
      * @param serveDirIndex
      *      True to generate the directory index.
      *      False to serve "index.html"
      */
     public final void serveFile(StaplerRequest req, StaplerResponse rsp, FilePath root, String icon, boolean serveDirIndex) throws IOException, ServletException, InterruptedException {
         // handle form submission
         String pattern = req.getParameter("pattern");
         if(pattern==null)
             pattern = req.getParameter("path"); // compatibility with Hudson<1.129
         if(pattern!=null) {
             rsp.sendRedirect2(pattern);
             return;
         }
 
         String path = getPath(req);
         if(path.indexOf("..")!=-1) {
             // don't serve anything other than files in the artifacts dir
             rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return;
         }
 
         // split the path to the base directory portion "abc/def/ghi" which doesn't include any wildcard,
         // and the GLOB portion "**/*.xml" (the rest)
         StringBuilder _base = new StringBuilder();
         StringBuilder _rest = new StringBuilder();
         int restSize=-1; // number of ".." needed to go back to the 'base' level.
         boolean zip=false;  // if we are asked to serve a zip file bundle
         {
             boolean inBase = true;
             StringTokenizer pathTokens = new StringTokenizer(path,"/");
             while(pathTokens.hasMoreTokens()) {
                 String pathElement = pathTokens.nextToken();
                 if(pathElement.contains("?") || pathElement.contains("*"))
                     inBase = false;
                 if(pathElement.equals("*zip*")) {
                     // the expected syntax is foo/bar/*zip*/bar.zip
                     // the last 'bar.zip' portion is to causes browses to set a good default file name.
                     // so the 'rest' portion ends here.
                     zip=true;
                     break;
                 }
 
                 StringBuilder sb = inBase?_base:_rest;
                 if(sb.length()>0)   sb.append('/');
                 sb.append(pathElement);
                 if(!inBase)
                     restSize++;
             }
         }
         restSize = Math.max(restSize,0);
         String base = _base.toString();
         String rest = _rest.toString();
 
         // this is the base file/directory
         FilePath baseFile = new FilePath(root,base);
 
         if(baseFile.isDirectory()) {
             if(zip) {
                 rsp.setContentType("application/zip");
                 baseFile.createZipArchive(rsp.getOutputStream(),rest);
                 return;
             }
 
             if(rest.length()==0) {
                 // if the target page to be displayed is a directory and the path doesn't end with '/', redirect
                 StringBuffer reqUrl = req.getRequestURL();
                 if(reqUrl.charAt(reqUrl.length()-1)!='/') {
                     rsp.sendRedirect2(reqUrl.append('/').toString());
                     return;
                 }
             }
 
             FileCallable<List<List<Path>>> glob = null;
 
             if(rest.length()>0) {
                 // the rest is Ant glob pattern
                 glob = new PatternScanner(rest,createBackRef(restSize));
             } else
             if(serveDirIndex) {
                 // serve directory index
                 glob = new ChildPathBuilder();
             }
 
             if(glob!=null) {
                 // serve glob
                 req.setAttribute("it", this);
                 List<Path> parentPaths = buildParentPath(base,restSize);
                 req.setAttribute("parentPath",parentPaths);
                 req.setAttribute("backPath", createBackRef(restSize));
                 req.setAttribute("topPath", createBackRef(parentPaths.size()+restSize));
                 req.setAttribute("files", baseFile.act(glob));
                 req.setAttribute("icon", icon);
                 req.setAttribute("path", path);
                 req.setAttribute("pattern",rest);
                 req.setAttribute("dir", baseFile);
                 req.getView(this,"dir.jelly").forward(req, rsp);
                 return;
             }
 
             // convert a directory service request to a single file service request by serving
             // 'index.html'
             baseFile = baseFile.child("index.html");
         }
 
         //serve a single file
         if(!baseFile.exists()) {
             rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
 
         boolean view = rest.equals("*view*");
 
         if(rest.equals("*fingerprint*")) {
             rsp.forward(Hudson.getInstance().getFingerprint(baseFile.digest()),"/",req);
             return;
         }
 
         ContentInfo ci = baseFile.act(new ContentInfo());
 
        if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Serving "+baseFile+" with lastModified="+ci.lastModified+", contentLength="+ci.contentLength);

         InputStream in = baseFile.read();
         if (view) {
             // for binary files, provide the file name for download
             rsp.setHeader("Content-Disposition", "inline; filename=" + baseFile.getName());
 
             // pseudo file name to let the Stapler set text/plain
             rsp.serveFile(req, in, ci.lastModified, -1, ci.contentLength, "plain.txt");
         } else {
             rsp.serveFile(req, in, ci.lastModified, -1, ci.contentLength, baseFile.getName() );
         }
     }
 
     private String getPath(StaplerRequest req) {
         String path = req.getRestOfPath();
         if(path.length()==0)
             path = "/";
         return path;
     }
 
     private static final class ContentInfo implements FileCallable<ContentInfo> {
         int contentLength;
         long lastModified;
 
         public ContentInfo invoke(File f, VirtualChannel channel) throws IOException {
             contentLength = (int) f.length();
             lastModified = f.lastModified();
             return this;
         }
 
         private static final long serialVersionUID = 1L;
     }
 
     /**
      * Builds a list of {@link Path} that represents ancestors
      * from a string like "/foo/bar/zot".
      */
     private List<Path> buildParentPath(String pathList, int restSize) {
         List<Path> r = new ArrayList<Path>();
         StringTokenizer tokens = new StringTokenizer(pathList, "/");
         int total = tokens.countTokens();
         int current=1;
         while(tokens.hasMoreTokens()) {
             String token = tokens.nextToken();
             r.add(new Path(createBackRef(total-current+restSize),token,true,0));
             current++;
         }
         return r;
     }
 
     private static String createBackRef(int times) {
         if(times==0)    return "./";
         StringBuffer buf = new StringBuffer(3*times);
         for(int i=0; i<times; i++ )
             buf.append("../");
         return buf.toString();
     }
 
     /**
      * Represents information about one file or folder.
      */
     public static final class Path implements Serializable {
         /**
          * Relative URL to this path from the current page.
          */
         private final String href;
         /**
          * Name of this path. Just the file name portion.
          */
         private final String title;
 
         private final boolean isFolder;
 
         /**
          * File size, or null if this is not a file.
          */
         private final long size;
 
         public Path(String href, String title, boolean isFolder, long size) {
             this.href = href;
             this.title = title;
             this.isFolder = isFolder;
             this.size = size;
         }
 
         public boolean isFolder() {
             return isFolder;
         }
 
         public String getHref() {
             return href;
         }
 
         public String getTitle() {
             return title;
         }
 
         public String getIconName() {
             return isFolder?"folder.gif":"text.gif";
         }
 
         public long getSize() {
             return size;
         }
 
         private static final long serialVersionUID = 1L;
     }
 
 
 
     private static final class FileComparator implements Comparator<File> {
         public int compare(File lhs, File rhs) {
             // directories first, files next
             int r = dirRank(lhs)-dirRank(rhs);
             if(r!=0) return r;
             // otherwise alphabetical
             return lhs.getName().compareTo(rhs.getName());
         }
 
         private int dirRank(File f) {
             if(f.isDirectory())     return 0;
             else                    return 1;
         }
     }
 
     /**
      * Builds a list of list of {@link Path}. The inner
      * list of {@link Path} represents one child item to be shown
      * (this mechanism is used to skip empty intermediate directory.)
      */
     private static final class ChildPathBuilder implements FileCallable<List<List<Path>>> {
         public List<List<Path>> invoke(File cur, VirtualChannel channel) throws IOException {
             List<List<Path>> r = new ArrayList<List<Path>>();
 
             File[] files = cur.listFiles();
             Arrays.sort(files,new FileComparator());
 
             for( File f : files ) {
                 Path p = new Path(f.getName(),f.getName(),f.isDirectory(),f.length());
                 if(!f.isDirectory()) {
                     r.add(Collections.singletonList(p));
                 } else {
                     // find all empty intermediate directory
                     List<Path> l = new ArrayList<Path>();
                     l.add(p);
                     String relPath = f.getName();
                     while(true) {
                         // files that don't start with '.' qualify for 'meaningful files', nor SCM related files
                         File[] sub = f.listFiles(new FilenameFilter() {
                             public boolean accept(File dir, String name) {
                                 return !name.startsWith(".") && !name.equals("CVS") && !name.equals(".svn");
                             }
                         });
                         if(sub.length!=1 || !sub[0].isDirectory())
                             break;
                         f = sub[0];
                         relPath += '/'+f.getName();
                         l.add(new Path(relPath,f.getName(),true,0));
                     }
                     r.add(l);
                 }
             }
 
             return r;
         }
 
         private static final long serialVersionUID = 1L;
     }
 
     /**
      * Runs ant GLOB against the current {@link FilePath} and returns matching
      * paths.
      */
     private static class PatternScanner implements FileCallable<List<List<Path>>> {
         private final String pattern;
         /**
          * Strling like "../../../" that cancels the 'rest' portion. Can be "./"
          */
         private final String baseRef;
 
         public PatternScanner(String pattern,String baseRef) {
             this.pattern = pattern;
             this.baseRef = baseRef;
         }
 
         public List<List<Path>> invoke(File baseDir, VirtualChannel channel) throws IOException {
             FileSet fs = Util.createFileSet(baseDir,pattern);
             DirectoryScanner ds = fs.getDirectoryScanner();
             String[] files = ds.getIncludedFiles();
 
             if (files.length > 0) {
                 List<List<Path>> r = new ArrayList<List<Path>>(files.length);
                 for (String match : files) {
                     List<Path> file = buildPathList(baseDir, new File(baseDir,match));
                     r.add(file);
                 }
                 return r;
             }
 
             return null;
         }
 
         /**
          * Builds a path list from the current workspace directory down to the specified file path.
          */
         private List<Path> buildPathList(File baseDir, File filePath) throws IOException {
             List<Path> pathList = new ArrayList<Path>();
             StringBuilder href = new StringBuilder(baseRef);
 
             buildPathList(baseDir, filePath, pathList, href);
             return pathList;
         }
 
         /**
          * Builds the path list and href recursively top-down.
          */
         private void buildPathList(File baseDir, File filePath, List<Path> pathList, StringBuilder href) throws IOException {
             File parent = filePath.getParentFile();
             if (!baseDir.equals(parent)) {
                 buildPathList(baseDir, parent, pathList, href);
             }
 
             href.append(filePath.getName());
             if (filePath.isDirectory()) {
                 href.append("/");
             }
 
             Path path = new Path(href.toString(), filePath.getName(), filePath.isDirectory(), filePath.length());
             pathList.add(path);
         }
 
         private static final long serialVersionUID = 1L;
     }

    private static final Logger LOGGER = Logger.getLogger(DirectoryBrowserSupport.class.getName());
 }
