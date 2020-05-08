 package controllers;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.gson.Gson;
 import models.Book;
 import models.dto.*;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 import play.mvc.Controller;
 import utils.Marshalling;
 import utils.cache.CacheUtils;
 import utils.cache.Caches;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
 * Created with IntelliJ IDEA. User: clement Date: 3/29/12 Time: 12:35 AM To change this template use File | Settings |
 * File Templates.
  */
 public class BibleService extends Controller {
 
   private static final Logger logger = Logger.getLogger(BibleService.class.getCanonicalName());
 
   private static final String BIBLIA_KEY = "e331a6d2b3812d505aac944ac5e2e25b";
 
   private static final String BIBLE_VERSIONS_JSON_URL = "http://api.biblia.com/v1/bible/find.txt?key=" + BIBLIA_KEY;
 
   private static final String BIBLE_TEXT_URL = "http://api.biblia.com/v1/bible/content/{0}.json?style=oneVersePerLine&key=" + BIBLIA_KEY + "&passage={1}";
 
   private static final String FORMATTED_BIBLE_URL = "http://api.biblia.com/v1/bible/content/{0}.html?style=fullyFormatted&key=" + BIBLIA_KEY + "&passage={1}";
 
   public static void getBibleVersions() throws IOException {
     BiblesDTO cached = CacheUtils.get(Caches.BIBLE, "VERSIONS", BiblesDTO.class);
     if (cached != null) {
       renderJSON(cached);
     } else {
       String response = readFromUrl(BIBLE_VERSIONS_JSON_URL);
       Gson gson = new Gson();
       BiblesDTO parsed = gson.fromJson(response, BiblesDTO.class);
       for (BibleDTO bible : parsed.bibles) {
         bible.bible = bible.bible.toUpperCase();
       }
       Caches.BIBLE_ASYNC.put("VERSIONS", Marshalling.serialize(parsed));
       renderJSON(parsed);
     }
   }
 
   public static void getBibleVerses(Book book, int chapter, String version, int returnStyle) {
     Preconditions.checkArgument(chapter > 0);
     Preconditions.checkArgument(book.getNumChapters() >= chapter);
     try {
       String passage = book.getFullName() + " " + chapter;
       if (book.getNumChapters() == 1) {
         passage = book.getFullName() + "1:1-1:" + book.getNumVersesInChapter(1);
       }
       renderJSON(readBibliaVerses(passage, version, returnStyle));
     } catch (IOException ex) {
       renderJSON(new ErrorDTO("INVALID_VERSION", version + " does not include the selected verses"));
     }
   }
 
   public static void getBibleVersesInChapterStarting(Book book, int chapter, int startVerse, String version, int returnStyle) {
     Preconditions.checkArgument(chapter > 0);
     Preconditions.checkArgument(startVerse > 0);
     Preconditions.checkArgument(book.getNumChapters() >= chapter);
     Preconditions.checkArgument(book.getNumVersesInChapter(chapter) >= startVerse);
     try {
       if (startVerse == book.getNumVersesInChapter(chapter)) {
         renderJSON(readBibliaVerses(book.getFullName() + " " + chapter + ":" + startVerse, version, returnStyle));
       } else {
         renderJSON(readBibliaVerses(book.getFullName() + " " + chapter + ":" + startVerse + "-" +
             chapter + ":" + book.getNumVersesInChapter(chapter), version, returnStyle));
       }
     } catch (IOException ex) {
       renderJSON(new ErrorDTO("INVALID_VERSION", version + " does not include the selected verses"));
     }
   }
 
   public static void getBibleVersesInChapterEnding(Book book, int chapter, int endVerse, String version, int returnStyle) {
     Preconditions.checkArgument(chapter > 0);
     Preconditions.checkArgument(endVerse > 0);
     Preconditions.checkArgument(book.getNumChapters() >= chapter);
     Preconditions.checkArgument(book.getNumVersesInChapter(chapter) >= endVerse);
     try {
       if (endVerse == 1) {
         renderJSON(readBibliaVerses(book.getFullName() + " " + chapter + ":" + 1, version, returnStyle));
       } else {
         renderJSON(readBibliaVerses(book.getFullName() + " " + chapter + ":" + 1 + "-" +
             chapter + ":" + endVerse, version, returnStyle));
       }
     } catch (IOException ex) {
       renderJSON(new ErrorDTO("INVALID_VERSION", version + " does not include the selected verses"));
     }
   }
 
   public static void getBibleVersesInChapter(Book book, int chapter, int startVerse, int endVerse, String version, int returnStyle) {
     Preconditions.checkArgument(chapter > 0);
     Preconditions.checkArgument(startVerse > 0);
     Preconditions.checkArgument(endVerse > 0);
     Preconditions.checkArgument(book.getNumChapters() >= chapter);
     Preconditions.checkArgument(book.getNumVersesInChapter(chapter) >= endVerse);
     Preconditions.checkArgument(book.getNumVersesInChapter(chapter) >= startVerse);
     try {
       if (startVerse == endVerse) {
         renderJSON(readBibliaVerses(book.getFullName() + " " + chapter + ":" + startVerse, version, returnStyle));
       } else {
         renderJSON(readBibliaVerses(book.getFullName() + " " + chapter + ":" + startVerse + "-" +
             chapter + ":" + endVerse, version, returnStyle));
       }
     } catch (IOException ex) {
       renderJSON(new ErrorDTO("INVALID_VERSION", version + " does not include the selected verses"));
     }
   }
 
   private static Object readBibliaVerses(String passage, String version, int returnStyle) throws IOException {
     if (returnStyle == 0) {
       return readBibliaVersesSimple(passage, version);
     } else if (returnStyle == 1) {
       return readBibliaVersesComplex(passage, version);
     }
     throw new IllegalArgumentException("Unknown style: " + returnStyle);
   }
 
   private static SimpleVersesDTO readBibliaVersesSimple(String passage, String version) throws IOException {
     readBibliaVersesComplex(passage, version);
     SimpleVersesDTO cached = CacheUtils.get(Caches.BIBLE, version + "::" + passage + "::0", SimpleVersesDTO.class);
     if (cached != null) {
       return cached;
     } else {
       String response = readFromUrl(MessageFormat.format(BIBLE_TEXT_URL, URLEncoder.encode(version, "ISO-8859-1"), URLEncoder.encode(passage, "ISO-8859-1")));
       if (response == null || response.length() == 0) {
         throw new IOException("Invalid output");
       }
       Gson gson = new Gson();
       BibliaTextDTO parsed = gson.fromJson(response, BibliaTextDTO.class);
       SimpleVersesDTO toReturn = new SimpleVersesDTO();
       String[] splitted = StringUtils.split(parsed.text, "\r\n");
       toReturn.verses = Lists.newArrayListWithExpectedSize(splitted.length - 1);
       toReturn.verses.addAll(Arrays.asList(splitted).subList(1, splitted.length));
       Caches.BIBLE_ASYNC.put(version + "::" + passage + "::0", Marshalling.serialize(toReturn));
       return toReturn;
     }
   }
 
   private static VersesDTO readBibliaVersesComplex(String passage, String version) throws IOException {
     VersesDTO cached = CacheUtils.get(Caches.BIBLE, version + "::" + passage + "::1", VersesDTO.class);
     if (cached != null) {
       return cached;
     } else {
       String response = readFromUrl(MessageFormat.format(FORMATTED_BIBLE_URL, URLEncoder.encode(version, "ISO-8859-1"), URLEncoder.encode(passage, "ISO-8859-1")));
       if (response == null || response.length() == 0) {
         throw new IOException("Invalid output");
       }
       Document document = Jsoup.parse(response);
       VersesDTO toReturn = new VersesDTO();
       boolean headerParsed = false;
       for (Element element : document.select("p")) {
         if (!headerParsed) {
           headerParsed = true;
           continue;
         }
         ParagraphDTO toAdd = convertNodesToParagraph(element.childNodes());
         for (String styleEntry : element.attr("style").split(";")) {
           String[] styleEntryParts = styleEntry.split(":");
           if (styleEntryParts.length == 2) {
             if (styleEntryParts[0].trim().equals("margin-left")) {
               try {
                 toAdd.indent = Double.parseDouble(StringUtils.removeEnd(styleEntryParts[1], "pt"));
               } catch (NumberFormatException ex) {
                 // ignored.
               }
             } else if (styleEntryParts[0].trim().equals("text-indent")) {
               try {
                 toAdd.startIndent = Double.parseDouble(StringUtils.removeEnd(styleEntryParts[1], "pt"));
               } catch (NumberFormatException ex) {
                 // ignored.
               }
             }
             if (toAdd.indent != null && toAdd.startIndent != null && toAdd.indent + toAdd.startIndent < 0) {
               // fix an issue where the first line indent can be more negative than the line indent itself.
               toAdd.startIndent = -toAdd.indent;
             } else if (toAdd.indent == null && toAdd.startIndent != null && toAdd.startIndent < 0) {
               // no paragraph indent but first line indent is null.
               toAdd.startIndent = null;
             }
           }
         }
         if (element.attr("style").contains("text-align:center;")) {
           toAdd.center = true;
         }
         toReturn.paragraphs.add(toAdd);
       }
       Caches.BIBLE_ASYNC.put(version + "::" + passage + "::1", Marshalling.serialize(toReturn));
       return toReturn;
     }
   }
 
   private static ParagraphDTO convertNodesToParagraph(List<Node> nodes) {
     ParagraphDTO toReturn = new ParagraphDTO();
     TextSpanDTO last = null;
     for (Node node : nodes) {
       TextSpanDTO toAdd = convertNodeToTextSpan(node);
       if (toAdd == null) {
         if (last != null && last.type == null) {
           last.text += " ";
         }
         continue;
       }
       if (last != null && last.type == TextSpanTypeDTO.VERSE_NUMBER && toAdd.type == TextSpanTypeDTO.VERSE_NUMBER) {
         // ignored, no two verse numbers should appear together.
       } else if (last != null && last.type == null &&
           toAdd.type == null &&
           ((last.italicized == null && toAdd.italicized == null) ||
               (last.italicized != null && toAdd.italicized != null && last.italicized == toAdd.italicized))) {
         last.text += toAdd.text;
       } else {
         if (last != null && last.type == null &&
             last.text.charAt(last.text.length() - 1) != ' ' &&
             toAdd.type == null && toAdd.text.charAt(0) != ' ') {
           last.text += " ";
         }
         toReturn.text.add(toAdd);
         last = toAdd;
       }
     }
     return toReturn;
   }
 
   private static TextSpanDTO convertNodeToTextSpan(Node node) {
     TextSpanDTO textSpan = new TextSpanDTO();
     if (node instanceof TextNode) {
       textSpan.text = ((TextNode) node).text();
       if (StringUtils.isBlank(((TextNode) node).text())) {
         return null;
       }
     } else if (node instanceof Element) {
       String rawText = StringUtils.removeEnd(((Element) node).text(), "\u00A0");
       if (StringUtils.isBlank(rawText)) {
         return null;
       }
       try {
         Integer.parseInt(rawText);
         textSpan.type = TextSpanTypeDTO.VERSE_NUMBER;
       } catch (NumberFormatException ex) {
         // ignored.
       }
       textSpan.text = rawText;
       if (((Element) node).tagName().equals("span")) {
         if (node.attr("style").contains("font-style:italic;")) {
           textSpan.italicized = true;
         }
       }
       if (textSpan.type == TextSpanTypeDTO.VERSE_NUMBER && !node.toString().contains("<sup>")) {
         textSpan.type = TextSpanTypeDTO.CHAPTER;
       }
     }
     return textSpan;
   }
 
   private static String readFromUrl(String urlStr) throws IOException {
     logger.info("Reading from URL: " + urlStr);
     URL url = new URL(urlStr);
     URLConnection yc = url.openConnection();
     return IOUtils.toString(yc.getInputStream(), "UTF-8");
   }
 }
