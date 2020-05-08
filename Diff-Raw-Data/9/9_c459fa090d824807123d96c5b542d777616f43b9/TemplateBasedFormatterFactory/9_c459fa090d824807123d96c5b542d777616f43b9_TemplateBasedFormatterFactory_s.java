 /**
  * 
  */
 package org.webreformatter.ebook.remote.formatters;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.webreformatter.commons.templates.ITemplateProcessor;
 import org.webreformatter.commons.templates.ITemplateProvider;
 import org.webreformatter.commons.uri.Uri;
 import org.webreformatter.commons.xml.XmlException;
 import org.webreformatter.ebook.bom.IBookToc.IBookTocItem;
 import org.webreformatter.ebook.bom.json.JsonBookSection;
 import org.webreformatter.ebook.bom.json.JsonBookToc;
 import org.webreformatter.ebook.io.IOutput;
 import org.webreformatter.ebook.io.server.OutputToStream;
 import org.webreformatter.ebook.remote.ISite;
 import org.webreformatter.ebook.remote.presenter.IContentPresenter;
 import org.webreformatter.ebook.remote.presenter.IPresenter;
 import org.webreformatter.ebook.remote.presenter.IPresenterManager;
 import org.webreformatter.ebook.remote.presenter.IndexPagePresenter;
 import org.webreformatter.ebook.remote.presenter.InnerPagePresenter;
 import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageFormatter;
 import org.webreformatter.ebook.remote.presenter.PageUtils;
 import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
 
 /**
  * @author kotelnikov
  */
 public class TemplateBasedFormatterFactory implements IFormatterFactory {
 
     public static class DocumentContext {
 
         private IndexPagePresenter fIndexPresenter;
 
         private InnerPagePresenter fPresenter;
 
         public DocumentContext(InnerPagePresenter presenter) {
             fPresenter = presenter;
         }
 
         public String esc(String str) {
             return PageUtils.escapeXml(str);
         }
 
         public List<IBookTocItem> getChildren(IBookTocItem item) {
             return item.getChildren();
         }
 
         public String getContent() throws XmlException, IOException {
             JsonBookSection section = fPresenter.getSection();
             String content = section.getContent();
             return content;
         }
 
         protected IndexPagePresenter getIndexPresenter()
             throws IOException,
             XmlException {
             if (fIndexPresenter == null) {
                 IndexPagePresenter indexPresenter = null;
                 ISite provider = fPresenter.getSite();
                 Uri indexUrl = provider.getSiteUrl();
                 IPresenter presenter = fPresenter
                     .getSite()
                     .getPresenterManager()
                     .getPresenter(indexUrl, true);
                 if (presenter instanceof IndexPagePresenter) {
                     indexPresenter = (IndexPagePresenter) presenter;
                 }
                 fIndexPresenter = indexPresenter;
             }
             return fIndexPresenter;
         }
 
         public String getSiteTitle() throws IOException, XmlException {
             IndexPagePresenter indexPresenter = getIndexPresenter();
             String siteTitle = esc(indexPresenter.getBookMeta().getBookTitle());
             return siteTitle;
         }
 
         public String getTitle() throws XmlException, IOException {
             JsonBookSection section = fPresenter.getSection();
             String title = section.getTitle();
             return esc(title);
         }
 
         public List<IBookTocItem> getToc() throws XmlException, IOException {
             IndexPagePresenter indexPresenter = getIndexPresenter();
             JsonBookToc toc = indexPresenter.getBookToc();
             List<IBookTocItem> items = toc.getTocItems();
             return items;
         }
 
         public boolean hasChildren(IBookTocItem item) {
             return !getChildren(item).isEmpty();
         }
 
         public String pathTo(String path) throws IOException {
             if (path.startsWith("/")) {
                 path = path.substring(1);
             }
             return pathTo(new Uri(path));
         }
 
         public String pathTo(Uri path) throws IOException {
             Uri resourcePath = fPresenter.getResourcePath();
             Uri result = resourcePath.getRelative(path);
             return result.toString();
         }
 
     }
 
     /**
      * @author kotelnikov
      */
     public static class TemplateBasedFormatter implements IFormatter {
 
         private final static Logger log = Logger
             .getLogger(TemplateBasedFormatter.class.getName());
 
         private static IOException handleError(String msg, Throwable e) {
             if (e instanceof IOException) {
                 return (IOException) e;
             }
             log.log(Level.WARNING, msg, e);
             return new IOException(msg, e);
         }
 
         private InnerPagePresenter fPresenter;
 
         private File fResourceFolder;
 
         private ITemplateProcessor fTemplateProcessor;
 
         /**
          * @param resourceFolder
          * @param templateProcessor
          * @param presenter
          */
         public TemplateBasedFormatter(
             InnerPagePresenter presenter,
             ITemplateProcessor templateProcessor,
             File resourceFolder) {
             fPresenter = presenter;
             fTemplateProcessor = templateProcessor;
             fResourceFolder = resourceFolder;
         }
 
         private void copyResources(IPresenterManager presenterManager, File file)
             throws IOException,
             XmlException {
             if (file.isDirectory()) {
                 File[] list = file.listFiles();
                 if (list != null) {
                     for (File child : list) {
                         copyResources(presenterManager, child);
                     }
                 }
             } else if (file.isFile()) {
                 String strUrl = file.toURI() + "";
                 if (!strUrl.endsWith(".vm")) {
                     Uri url = new Uri(strUrl);
                     presenterManager.getPresenter(url, true);
                 }
             }
         }
 
         protected String getTemplateName(JsonBookSection section)
             throws IOException {
             String type = section.getString("type");
             String templateName = null;
             if (type != null) {
                 templateName = type + ".vm";
                 ITemplateProvider resourceProvider = fTemplateProcessor
                     .getResourceProvider();
                 if (!resourceProvider.templateExists(templateName)) {
                     templateName = null;
                 }
             }
             if (templateName == null) {
                 templateName = "main.vm";
             }
             String folder = fPresenter.getResourcePathFolder();
             templateName = folder + templateName;
             return templateName;
         }
 
         /**
          * @see org.webreformatter.ebook.remote.formatters.IFormatter#writeTo(org.webreformatter.ebook.io.IOutput)
          */
         @Override
         public void writeTo(IOutput output) throws IOException {
             try {
                 OutputToStream stream = new OutputToStream(output);
                 OutputStreamWriter writer = new OutputStreamWriter(stream);
 
                 JsonBookSection section = fPresenter.getSection();
                 Map<String, Object> params = new HashMap<String, Object>();
                 params.put("context", new DocumentContext(fPresenter));
                 params.put("presenter", fPresenter);
                 params.put("doc", section);
 
                 String templateName = getTemplateName(section);
                 fTemplateProcessor.render(templateName, params, writer);
                 writer.flush();
                 writer.close();
                 stream.close();
 
                 ISite site = fPresenter.getSite();
                 IPresenterManager presenterManager = site.getPresenterManager();
                 copyResources(presenterManager, fResourceFolder);
             } catch (Exception e) {
                 throw handleError("Can not render an XML document.", e);
             } finally {
                 output.close();
             }
         }
     }
 
     private File fResourceFolder;
 
     private ITemplateProcessor fTemplateProcessor;
 
     /**
      * 
      */
     public TemplateBasedFormatterFactory(
         ITemplateProcessor templateProcessor,
         File resources) {
         fTemplateProcessor = templateProcessor;
         fResourceFolder = resources;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <F extends IFormatter, P extends IContentPresenter> F getFormatter(
         P presenter,
         Class<F> viewType) throws IOException {
         IFormatter formatter = null;
         if (viewType == IInnerPageFormatter.class
             && presenter instanceof RemotePagePresenter) {
             formatter = new TemplateBasedFormatter(
                 (InnerPagePresenter) presenter,
                 fTemplateProcessor,
                 fResourceFolder);
         }
         return (F) formatter;
     }
 
 }
