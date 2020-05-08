 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: FileSystemBrowserPage.java,v 1.3 2004-06-14 01:36:14 shahid.shah Exp $
  */
 
 package com.netspective.sparx.navigate.fs;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 
 import com.netspective.commons.template.TemplateProcessor;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.sparx.navigate.FileSystemContext;
 import com.netspective.sparx.navigate.FileSystemEntry;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.navigate.NavigationPage;
 import com.netspective.sparx.navigate.NavigationTree;
 import com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor;
 import com.netspective.sparx.theme.Theme;
 
 public class FileSystemBrowserPage extends NavigationPage
 {
     private static final String ATTRNAME_FS_CONTEXT = "FILE_SYSTEM_CONTEXT";
     private static final String ATTRNAME_HANDLER = "FILE_SYSTEM_ENTRY_CONTENT_HANDLER";
     private static final String ATTRNAME_INVALID_FILE = "FILE_SYSTEM_ENTRY_INVALID_FILE";
 
     private boolean projectBrowser;
     private boolean serveFilesWithUnknownHandlers = true;
     private boolean showExactEntryNames;
     private TemplateProcessor renderer;
     private TemplateProcessor unableToServeFileTemplate;
     private ValueSource rootPath;
     private ValueSource rootCaption;
     private FileSystemBrowserImages images = new DefaultFileSystemBrowserImages();
     private FileSystemBrowserEntries ignoreEntries;
     private String stuctureTableCssClass = "filesys-browser-struct";
     private String pathTableCssClass = "filesys-browser-path";
     private String filesTableCssClass = "filesys-browser-files";
     private String stuctureLevelPrefixHtml = "<span class='entry-level'>&nbsp;&nbsp;&nbsp;&nbsp;</span>";
     private String pathSeparatorHtml = "<span class='entry-separator'>&nbsp;/&nbsp;</span>";
     private FileSystemBrowserEntryContentHandlers contentHandlers = new DefaultFileSystemBrowserEntryContentHandlers();
 
     public FileSystemBrowserPage(NavigationTree owner)
     {
         super(owner);
 
         FreeMarkerTemplateProcessor renderer = (FreeMarkerTemplateProcessor) createRenderer();
         renderer.setSource(new StaticValueSource("default-file-browser-renderer.ftl"));
         renderer.finalizeContents();
         addRenderer(renderer);
 
         FreeMarkerTemplateProcessor unableToServeBody = (FreeMarkerTemplateProcessor) createUnableToServeBody();
         unableToServeBody.addTemplateContent("Unable to serve file ${entry.file.absolutePath}.");
         unableToServeBody.finalizeContents();
         ;
         addUnableToServeBody(unableToServeBody);
 
         DefaultFileSystemBrowserEntries ignoreEntries = createIgnoreEntries();
         DefaultFileSystemBrowserEntries.DirectoryEntry CVS = ignoreEntries.createDirectory();
         CVS.setName("CVS");
         ignoreEntries.addDirectory(CVS);
         addIgnoreEntries(ignoreEntries);
     }
 
     public boolean isProjectBrowser()
     {
         return projectBrowser;
     }
 
     public void setProjectBrowser(boolean projectBrowser)
     {
         this.projectBrowser = projectBrowser;
         if (projectBrowser)
         {
             setShowExactEntryNames(true);
             SyntaxHighlightingContentHandler.registerAll((DefaultFileSystemBrowserEntryContentHandlers) contentHandlers);
         }
     }
 
     public boolean isServeFilesWithUnknownHandlers()
     {
         return serveFilesWithUnknownHandlers;
     }
 
     public void setServeFilesWithUnknownHandlers(boolean serveFilesWithUnknownHandlers)
     {
         this.serveFilesWithUnknownHandlers = serveFilesWithUnknownHandlers;
     }
 
     public boolean isShowExactEntryNames()
     {
         return showExactEntryNames;
     }
 
     public void setShowExactEntryNames(boolean showExactEntryNames)
     {
         this.showExactEntryNames = showExactEntryNames;
     }
 
     public ValueSource getRootCaption()
     {
         return rootCaption;
     }
 
     public void setRootCaption(ValueSource rootCaption)
     {
         this.rootCaption = rootCaption;
     }
 
     public ValueSource getRootPath()
     {
         return rootPath;
     }
 
     public void setRootPath(ValueSource rootPath)
     {
         this.rootPath = rootPath;
     }
 
     public String getStuctureLevelPrefixHtml()
     {
         return stuctureLevelPrefixHtml;
     }
 
     public void setStuctureLevelPrefixHtml(String stuctureLevelPrefixHtml)
     {
         this.stuctureLevelPrefixHtml = stuctureLevelPrefixHtml;
     }
 
     public String getStuctureTableCssClass()
     {
         return stuctureTableCssClass;
     }
 
     public void setStuctureTableCssClass(String stuctureTableCssClass)
     {
         this.stuctureTableCssClass = stuctureTableCssClass;
     }
 
     public TemplateProcessor createRenderer()
     {
         return new FreeMarkerTemplateProcessor();
     }
 
     public void addRenderer(TemplateProcessor templateProcessor)
     {
         renderer = templateProcessor;
     }
 
     public TemplateProcessor getRenderer()
     {
         return renderer;
     }
 
     public TemplateProcessor createUnableToServeBody()
     {
         return new FreeMarkerTemplateProcessor();
     }
 
     public void addUnableToServeBody(TemplateProcessor templateProcessor)
     {
         unableToServeFileTemplate = templateProcessor;
     }
 
     public TemplateProcessor getUnableToServeFileTemplate()
     {
         return unableToServeFileTemplate;
     }
 
     public FileSystemBrowserImages createImages()
     {
         return images;
     }
 
     public void addImages(FileSystemBrowserImages images)
     {
         // do nothing
     }
 
     public DefaultFileSystemBrowserEntries createIgnoreEntries()
     {
         return new DefaultFileSystemBrowserEntries();
     }
 
     public void addIgnoreEntries(FileSystemBrowserEntries ignoreEntries)
     {
         this.ignoreEntries = ignoreEntries;
     }
 
     public FileSystemBrowserEntryContentHandlers createContentHandlers()
     {
         return contentHandlers;
     }
 
     public void addContentHandlers(FileSystemBrowserEntryContentHandlers contentHandlers)
     {
         // do nothing
     }
 
     public void generateChildEntriesHtml(final Writer writer, final NavigationContext nc, final FileSystemEntry activePath, boolean includeDirs, boolean dirsMixedWithFiles) throws IOException
     {
         final Theme theme = nc.getActiveTheme();
         final List activePathChildren = activePath.getChildren();
         final String requestURI = nc.getHttpRequest().getRequestURI();
         final boolean haveTrailingSlashInURI = requestURI.endsWith("/");
         boolean evenFlag = true;
         String evenText = "even";
 
         writer.write("<table class='" + filesTableCssClass + "'>\n");
         if (includeDirs && !dirsMixedWithFiles)
         {
             for (int i = 0; i < activePathChildren.size(); i++)
             {
                 final FileSystemEntry child = (FileSystemEntry) activePathChildren.get(i);
                 final File childFile = child.getFile();
                 if (ignoreEntries.contains(child))
                     continue;
 
                 if (!childFile.isDirectory())
                     continue;
 
                 writer.write("    <tr class='" + evenText + "'>\n");
                 writer.write("        <td class='image-" + evenText + "'><img src='" + theme.getResourceUrl(images.getFolderClosedImage()) + "'></td>");
                 writer.write("        <td class='spacer-" + evenText + "'>&nbsp;</td>");
                 writer.write("        <td class='entry-" + evenText + "'>");
 
                 String href = haveTrailingSlashInURI
                         ? requestURI + childFile.getName() : requestURI + "/" + childFile.getName();
                 writer.write("<a class='entry' href='" + href + "'>" + (showExactEntryNames
                         ? childFile.getName() : child.getEntryCaption()) + "</a>");
 
                 writer.write("        </td>");
                 writer.write("    </tr>\n");
 
                 evenFlag = !evenFlag;
                 evenText = evenFlag ? "even" : "odd";
             }
         }
 
         for (int i = 0; i < activePathChildren.size(); i++)
         {
             final FileSystemEntry child = (FileSystemEntry) activePathChildren.get(i);
             final File childFile = child.getFile();
             if (ignoreEntries.contains(child))
                 continue;
 
             if (childFile.isDirectory() && (!includeDirs || !dirsMixedWithFiles))
                 continue;
 
             writer.write("    <tr class='" + evenText + "'>\n");
             writer.write("        <td class='image-" + evenText + "'><img src='" + images.getImage(theme, child) + "'></td>");
             writer.write("        <td class='spacer-" + evenText + "'>&nbsp;</td>");
             writer.write("        <td class='entry-" + evenText + "'>");
 
             String href = haveTrailingSlashInURI
                     ? requestURI + childFile.getName() : requestURI + "/" + childFile.getName();
             writer.write("<a class='entry' href='" + href + "'>" + (showExactEntryNames
                     ? childFile.getName() : child.getEntryCaption()) + "</a>");
 
             writer.write("        </td>");
             writer.write("    </tr>\n");
 
             evenFlag = !evenFlag;
             evenText = evenFlag ? "even" : "odd";
         }
 
         writer.write("</table>\n");
     }
 
     public void generateDirectoryStructureHtml(final Writer writer, final NavigationContext nc, final FileSystemEntry activePath, boolean includeFiles, boolean filesMixedWithDirs) throws IOException
     {
         final Theme theme = nc.getActiveTheme();
         final List activePathParents = activePath.getParents();
         final List activePathChildren = activePath.getChildren();
         final String requestURI = nc.getHttpRequest().getRequestURI();
         final boolean haveTrailingSlashInURI = requestURI.endsWith("/");
         int activePathParentsCount = activePathParents.size();
         int ancestorsCount = activePathParentsCount - 1;
         int level = 0;
         boolean evenFlag = true;
         String evenText = "even";
 
         writer.write("<table class='" + stuctureTableCssClass + "'>\n");
         for (int i = 0; i < activePathParents.size(); i++)
         {
             final FileSystemEntry parent = (FileSystemEntry) activePathParents.get(i);
 
             writer.write("    <tr class='parent-" + evenText + "'>\n");
             writer.write("        <td class='parent-" + evenText + "'>");
 
             for (int l = 0; l < level; l++)
                 writer.write(stuctureLevelPrefixHtml);
 
             String href = ".";
             if (ancestorsCount > 0)
             {
                 final StringBuffer hrefBuffer = new StringBuffer();
                 for (int a = 1; a <= ancestorsCount; a++)
                     hrefBuffer.append(a < ancestorsCount ? "../" : "..");
                 href = hrefBuffer.toString();
             }
 
             writer.write("<img class='parent-entry-image' src='" + theme.getResourceUrl(images.getFolderOpenImage()) + "'>&nbsp;");
             writer.write("<a class='parent-entry-name' href='" + href + "'>" + (showExactEntryNames
                     ? parent.getFile().getName() : parent.getEntryCaption()) + "</a>");
 
             writer.write("        </td>");
             writer.write("    </tr>\n");
 
             level++;
             ancestorsCount--;
 
             evenFlag = !evenFlag;
             evenText = evenFlag ? "even" : "odd";
         }
 
         writer.write("    <tr class='active-" + evenText + "'>\n");
         writer.write("        <td class='active-" + evenText + "'>\n");
 
         for (int l = 0; l < level; l++)
             writer.write(stuctureLevelPrefixHtml);
 
         writer.write("            <img class='active-entry-image' src='" + theme.getResourceUrl(images.getFolderActiveImage()) + "'>&nbsp;");
         writer.write("            <span class='active-entry-name'>" + (showExactEntryNames
                 ? activePath.getFile().getName() : activePath.getEntryCaption()) + "</span>");
         writer.write("        </td>\n");
         writer.write("    </tr>\n");
 
         evenFlag = !evenFlag;
         evenText = evenFlag ? "even" : "odd";
         level++;
 
         for (int i = 0; i < activePathChildren.size(); i++)
         {
             final FileSystemEntry child = (FileSystemEntry) activePathChildren.get(i);
             final File childFile = child.getFile();
             if (ignoreEntries.contains(child))
                return;
 
             final boolean isFile = !childFile.isDirectory();
             if (isFile && (!includeFiles || !filesMixedWithDirs))
                 continue;
 
             writer.write("    <tr class='child-" + evenText + "'>\n");
             writer.write("        <td class='child-" + evenText + "'>");
 
             for (int l = 0; l < level; l++)
                 writer.write(stuctureLevelPrefixHtml);
 
             String href = haveTrailingSlashInURI
                     ? requestURI + childFile.getName() : requestURI + "/" + childFile.getName();
             writer.write("<img class='child-entry-image' src='" + images.getImage(theme, child) + "'>&nbsp;");
             writer.write("<a class='child-entry-name' href='" + href + "'>" + (showExactEntryNames
                     ? child.getFile().getName() : child.getEntryCaption()) + "</a>");
 
             writer.write("        </td>");
             writer.write("    </tr>\n");
 
             evenFlag = !evenFlag;
             evenText = evenFlag ? "even" : "odd";
         }
 
         if (includeFiles && (!filesMixedWithDirs))
         {
             for (int i = 0; i < activePathChildren.size(); i++)
             {
                 final FileSystemEntry child = (FileSystemEntry) activePathChildren.get(i);
                 final File childFile = child.getFile();
                 if (ignoreEntries.contains(child))
                    return;
 
                 if (childFile.isDirectory())
                     continue;
 
                 writer.write("    <tr class='child-" + evenText + "'>\n");
                 writer.write("        <td class='child-" + evenText + "'>");
 
                 for (int l = 0; l < level; l++)
                     writer.write(stuctureLevelPrefixHtml);
 
                 String href = haveTrailingSlashInURI
                         ? requestURI + childFile.getName() : requestURI + "/" + childFile.getName();
                 writer.write("<img class='child-entry-image' src='" + images.getImage(theme, child) + "'>&nbsp;");
                 writer.write("<a class='child-entry-name' href='" + href + "'>" + child.getEntryCaption() + "</a>");
 
                 writer.write("        </td>");
                 writer.write("    </tr>\n");
 
                 evenFlag = !evenFlag;
                 evenText = evenFlag ? "even" : "odd";
             }
         }
 
         writer.write("</table>\n");
     }
 
     public void generateDirectoryPathHtml(final Writer writer, final NavigationContext nc, final FileSystemEntry activePath) throws IOException
     {
         final Theme theme = nc.getActiveTheme();
         final List activePathParents = activePath.getParents();
         int activePathParentsCount = activePathParents.size();
         int ancestorsCount = activePathParentsCount - 1;
 
         writer.write("<table class='" + pathTableCssClass + "'>\n");
         writer.write("    <tr class='path'>\n");
         writer.write("        <td class='path-image'><img src='" + theme.getResourceUrl(images.getFolderOpenImage()) + "'></td>");
         writer.write("        <td class='path'>");
         for (int i = 0; i < activePathParents.size(); i++)
         {
             final FileSystemEntry parent = (FileSystemEntry) activePathParents.get(i);
 
             String href = ".";
             if (ancestorsCount > 0)
             {
                 final StringBuffer hrefBuffer = new StringBuffer();
                 for (int a = 1; a <= ancestorsCount; a++)
                     hrefBuffer.append(a < ancestorsCount ? "../" : "..");
                 href = hrefBuffer.toString();
             }
 
             writer.write("<a class='parent-entry-name' href='" + href + "'>" + (showExactEntryNames
                     ? parent.getFile().getName() : parent.getEntryCaption()) + "</a>");
             writer.write(pathSeparatorHtml);
 
             ancestorsCount--;
         }
         writer.write("        </td>");
         writer.write("    </tr>\n");
         writer.write("    <tr class='file'>\n");
         writer.write("        <td class='file-image'><img src='" + images.getImage(theme, activePath) + "'</td>");
         writer.write("        <td class='path'>");
         writer.write("            " + (showExactEntryNames
                 ? activePath.getFile().getName() : activePath.getEntryCaption()));
         writer.write("        </td>");
         writer.write("    </tr>\n");
         writer.write("</table>\n");
     }
 
     public String getChildEntriesHtml(final NavigationContext nc, boolean includeDirs, boolean dirsMixedWithFiles) throws IOException
     {
         final FileSystemContext fileSystemContext = (FileSystemContext) nc.getRequest().getAttribute(ATTRNAME_FS_CONTEXT);
         final FileSystemEntry activePath = fileSystemContext.getActivePath();
 
         StringWriter writer = new StringWriter();
         generateChildEntriesHtml(writer, nc, activePath, includeDirs, dirsMixedWithFiles);
         return writer.toString();
     }
 
     public String getDirectoryStructureHtml(final NavigationContext nc, boolean includeFiles, boolean filesMixedWithDirs) throws IOException
     {
         final FileSystemContext fileSystemContext = (FileSystemContext) nc.getRequest().getAttribute(ATTRNAME_FS_CONTEXT);
         final FileSystemEntry activePath = fileSystemContext.getActivePath();
 
         StringWriter writer = new StringWriter();
         generateDirectoryStructureHtml(writer, nc, activePath, includeFiles, filesMixedWithDirs);
         return writer.toString();
     }
 
     public String getDirectoryPathHtml(final NavigationContext nc) throws IOException
     {
         final FileSystemContext fileSystemContext = (FileSystemContext) nc.getRequest().getAttribute(ATTRNAME_FS_CONTEXT);
         final FileSystemEntry activePath = fileSystemContext.getActivePath();
 
         StringWriter writer = new StringWriter();
         generateDirectoryPathHtml(writer, nc, activePath);
         return writer.toString();
     }
 
     public void handlePageBody(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         final ServletRequest request = nc.getRequest();
         final FileSystemContext fileSystemContext = (FileSystemContext) request.getAttribute(ATTRNAME_FS_CONTEXT);
         final FileSystemBrowserEntryContentHandler handler = (FileSystemBrowserEntryContentHandler) request.getAttribute(ATTRNAME_HANDLER);
         final FileSystemEntry activePath = fileSystemContext.getActivePath();
 
         if (handler != null)
             handler.handleContent(writer, nc, activePath);
         else
         {
             if (request.getAttribute(ATTRNAME_INVALID_FILE) != null)
             {
                 Map templateVars = new HashMap();
                 templateVars.put("entry", activePath);
                 unableToServeFileTemplate.process(writer, nc, templateVars);
             }
             else
                 renderer.process(writer, nc, null);
         }
     }
 
     public void handlePage(Writer writer, NavigationContext nc) throws ServletException, IOException
     {
         final FileSystemContext fileSystemContext = projectBrowser ? nc.getProjectFileSystemContext() :
                 nc.getFileSystemContext(new File(getRootPath().getTextValue(nc)), getRootCaption().getTextValue(nc));
         final FileSystemEntry activePath = fileSystemContext.getActivePath();
         final ServletRequest request = nc.getRequest();
         if (activePath.getFile().isFile())
         {
             final FileSystemBrowserEntryContentHandler handler = contentHandlers.getContentHandler(activePath);
             if (handler != null)
             {
                 if (handler.isDownload())
                     activePath.send(nc.getHttpResponse(), handler.getMimeType());
                 else
                 {
                     request.setAttribute(ATTRNAME_FS_CONTEXT, fileSystemContext);
                     request.setAttribute(ATTRNAME_HANDLER, handler);
                     super.handlePage(writer, nc);
                 }
             }
             else if (serveFilesWithUnknownHandlers)
                 activePath.send(nc.getHttpResponse(), null);
             else
             {
                 request.setAttribute(ATTRNAME_FS_CONTEXT, fileSystemContext);
                 request.setAttribute(ATTRNAME_INVALID_FILE, new Boolean(true));
                 super.handlePage(writer, nc);
             }
         }
         else
         {
             request.setAttribute(ATTRNAME_FS_CONTEXT, fileSystemContext);
             super.handlePage(writer, nc);
         }
     }
 }
