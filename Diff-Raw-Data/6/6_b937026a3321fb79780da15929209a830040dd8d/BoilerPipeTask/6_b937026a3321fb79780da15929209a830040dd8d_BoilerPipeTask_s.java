 package org.gnuton.newshub.tasks;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import org.gnuton.newshub.db.DbHelper;
 import org.gnuton.newshub.db.RSSEntryDataSource;
 import org.gnuton.newshub.types.RSSEntry;
 import org.gnuton.newshub.utils.MyApp;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import de.l3s.boilerpipe.BoilerpipeExtractor;
 import de.l3s.boilerpipe.BoilerpipeProcessingException;
 import de.l3s.boilerpipe.document.Image;
 import de.l3s.boilerpipe.document.TextDocument;
 import de.l3s.boilerpipe.extractors.CommonExtractors;
 import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
 import de.l3s.boilerpipe.sax.HTMLDocument;
 import de.l3s.boilerpipe.sax.HTMLFetcher;
 import de.l3s.boilerpipe.sax.HTMLHighlighter;
 import de.l3s.boilerpipe.sax.ImageExtractor;
 
 /**
  * Created by gnuton on 5/22/13.
  */
 public class BoilerPipeTask extends AsyncTask<RSSEntry, Void, RSSEntry[]> {
     private static final String TAG = "BOILER PIPE TASK";
 
     private static OnBoilerplateRemovedListener mListener;
     private static final RSSEntryDataSource eds = new RSSEntryDataSource(MyApp.getContext());
 
     public interface OnBoilerplateRemovedListener {
         public void onBoilerplateRemoved(RSSEntry[] entries);
     }
 
     public BoilerPipeTask() {
         mListener = null;
     }
 
     public BoilerPipeTask(Object o) {
         if (o instanceof OnBoilerplateRemovedListener) {
             this.mListener = (OnBoilerplateRemovedListener) o;
         } else {
             throw new ClassCastException(o.toString() + " must implement BoilerPipeTask.OnBoilerplateRemovedListener");
         }
     }
 
     @Override
     protected RSSEntry[] doInBackground(RSSEntry... entries) {
 
         for (RSSEntry e : entries) {
             if (e.content != null || "".equals(e.content)) {
                 if (e.content!=null)
                     Log.d(TAG, e.content);
                 Log.d(TAG, "Skipping...");
                 continue;
             }
 
             URL url = null;
             try {
                 url = new URL(e.link);
             } catch (MalformedURLException e1) {
                 e1.printStackTrace();
                 Log.e(TAG, "Bad URL");
                 continue;
             }
             Log.d(TAG, "Processing " + url);
 
             TextDocument doc = null;
             HTMLDocument htmlDoc = null;
             try {
                 htmlDoc = HTMLFetcher.fetch(url);
                 doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
             } catch (BoilerpipeProcessingException e1) {
                 e1.printStackTrace();
             } catch (SAXException e1) {
                 e1.printStackTrace();
             } catch (IOException e1) {
                 e1.printStackTrace();
             }
             String article = extractArticle(doc, htmlDoc);
             article = extractImages(doc, htmlDoc, article);
             e.content = article;
 
             if (e.content != null) {
                 e.content = sanitizeArticle(e);
                 e.columnsToUpdate.add(DbHelper.ENTRIES_CONTENT);
                 eds.update(e);
             }
         }
         return entries;
     }
 
     private String sanitizeArticle(RSSEntry e) {
         String article = e.content;
 
         // Sanitize html
         if (article != null) {
            article = article.replaceFirst("<style[^>]*>[^<]*</style>","");
            article = article.replaceFirst(e.title,"");
         }
 
         return article;
     }
 
     public String extractImages(TextDocument doc, HTMLDocument htmlDoc, String article){
         if (article == null)
             return null;
 
         final ImageExtractor imageExtractor = ImageExtractor.getInstance();
         //final BoilerpipeExtractor everythingExtractor = CommonExtractors.KEEP_EVERYTHING_EXTRACTOR;
 
         List<Image> images= null;
         try {
             images = imageExtractor.process(doc, htmlDoc.toInputSource());
         } catch (BoilerpipeProcessingException e) {
             e.printStackTrace();
         }
 
         if (images.size() != 0){
             try {
                 article = appendImgs(images, article);
             } catch (ParserConfigurationException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (SAXException e) {
                 e.printStackTrace();
             }
         }
 
         Log.d("Images:", images.toString());
         return article;
     }
 
     public Boolean isArticlempty(final String article){
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 
         DocumentBuilder dBuilder = null;
         try {
             dBuilder = dbFactory.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
             return false;
         }
 
         Document doc = null;
         try {
             doc = dBuilder.parse(new InputSource(new StringReader(article)));
         } catch (SAXException e) {
             e.printStackTrace();
             return false;
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
 
         return (doc.getElementsByTagName("BODY").getLength() == 0);
     }
 
     public String extractArticle(TextDocument doc, HTMLDocument htmlDoc) {
         if (doc == null||htmlDoc == null)
             return null;
 
         String article= null;
         final BoilerpipeExtractor articleExtractor = CommonExtractors.ARTICLE_EXTRACTOR;
         final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();
 
         try {
             articleExtractor.process(doc);
             article = hh.process(doc,htmlDoc.toInputSource());
         } catch (BoilerpipeProcessingException e1) {
             e1.printStackTrace();
         }
 
         // Check article length
         if (isArticlempty(article))
             return null;
         else
             return article;
     }
 
 
 
     private String appendImgs(List<Image> images, String html) throws ParserConfigurationException, IOException, SAXException {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 
         Document doc = dBuilder.parse(new InputSource(new StringReader(html)));
         doc.getDocumentElement().normalize();
 
         for (Image image : images) {
             Element img = doc.createElement("IMG");
             img.setAttribute("src", image.getSrc());
 
             Node body = doc.getElementsByTagName("BODY").item(0);
             body.appendChild(img);
         }
 
         try {
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.setOutputProperty(OutputKeys.METHOD, "xml");
             StreamResult result = new StreamResult(new StringWriter());
             DOMSource source = new DOMSource(doc);
             transformer.transform(source, result);
             return result.getWriter().toString();
         } catch(TransformerException ex) {
             ex.printStackTrace();
             return null;
         }
     }
 
     @Override
     protected void onPostExecute(RSSEntry[] entries) {
         Log.d(TAG, "Boilerpipe thread terminated");
 
         if (mListener != null)
             mListener.onBoilerplateRemoved(entries);
     }
 }
