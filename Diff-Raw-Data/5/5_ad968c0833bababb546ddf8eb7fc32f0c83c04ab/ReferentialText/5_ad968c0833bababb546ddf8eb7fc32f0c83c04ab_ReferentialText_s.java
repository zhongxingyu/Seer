 package com.psddev.dari.db;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.parser.Tag;
 
 import com.psddev.dari.util.ErrorUtils;
 import com.psddev.dari.util.ObjectUtils;
 
 /** Contains strings and references to other objects. */
 public class ReferentialText extends AbstractList<Object> {
 
     private static final Tag BR_TAG = Tag.valueOf("br");
     private static final Tag P_TAG = Tag.valueOf("p");
 
     private final List<Object> list = new ArrayList<Object>();
 
     /**
      * Creates an empty instance.
      */
     public ReferentialText() {
     }
 
     private static void addByBoundary(
             List<Object> list,
             String html,
             String boundary,
             List<Reference> references) {
 
         int previousBoundaryAt = 0;
 
         for (int boundaryAt = previousBoundaryAt;
                 (boundaryAt = html.indexOf(boundary, previousBoundaryAt)) >= 0;
                 previousBoundaryAt = boundaryAt + boundary.length()) {
             list.add(html.substring(previousBoundaryAt, boundaryAt));
             list.add(references.get(list.size() / 2));
         }
 
         list.add(html.substring(previousBoundaryAt));
     }
 
     /**
      * Creates an instance from the given {@code html}.
      *
      * @param html If {@code null}, creates an empty instance.
      */
     public ReferentialText(String html, boolean finalDraft) {
         if (html == null) {
             return;
         }
 
         Document document = Jsoup.parseBodyFragment(html);
         Element body = document.body();
 
         document.outputSettings().prettyPrint(false);
 
         for (Element element : body.select("*")) {
             String tagName = element.tagName();
 
             if (tagName.startsWith("o:")) {
                 element.unwrap();
 
             } else if (tagName.equals("span")) {
                 if (element.attributes().size() == 0 ||
                         element.attr("class").contains("Mso") ||
                         element.hasAttr("color")) {
                     element.unwrap();
                 }
             }
         }
 
         // Remove editorial markups.
         if (finalDraft) {
             body.getElementsByTag("del").remove();
             body.getElementsByTag("ins").unwrap();
         }
 
         // Convert '<p>text</p>' to 'text<br><br>'.
         for (Element p : body.getElementsByTag("p")) {
             if (p.hasText()) {
                 p.appendChild(new Element(BR_TAG, ""));
                 p.appendChild(new Element(BR_TAG, ""));
                 p.unwrap();
 
             // Remove empty paragraphs.
             } else {
                 p.remove();
             }
         }
 
         // Find object references.
         List<Reference> references = new ArrayList<Reference>();
         String boundary = UUID.randomUUID().toString();
 
         for (Element enhancement : body.getElementsByClass("enhancement")) {
             if (!enhancement.hasClass("state-removing")) {
                 Reference reference = new Reference();
 
                 for (Map.Entry<String, String> entry : enhancement.dataset().entrySet()) {
                     String key = entry.getKey();
 
                     if (!key.startsWith("_")) {
                         reference.put(key, entry.getValue());
                     }
                 }
 
                 UUID referenceId = ObjectUtils.to(UUID.class, reference.remove("id"));
 
                 if (referenceId != null) {
                     reference.put("record", Query.findById(Object.class, referenceId));
                     references.add(reference);
                     enhancement.before(boundary);
                 }
             }
 
             enhancement.remove();
         }
 
         StringBuilder cleaned = new StringBuilder();
 
         for (Node child : body.childNodes()) {
             cleaned.append(child.toString());
         }
 
         addByBoundary(this, cleaned.toString(), boundary, references);
     }
 
     /**
      * Returns a mixed list of well-formed HTML strings and object references
      * that have been converted to publishable forms.
      *
      * @return Never {@code null}.
      */
     public List<Object> toPublishables() {
 
         // Concatenate the items so that it can be fed into an HTML parser.
         StringBuilder html = new StringBuilder();
         String boundary = UUID.randomUUID().toString();
         List<Reference> references = new ArrayList<Reference>();
 
         for (Object item : this) {
             if (item != null) {
                 if (item instanceof Reference) {
                     html.append(boundary);
                     references.add((Reference) item);
 
                 } else {
                     html.append(item.toString());
                 }
             }
         }
 
         // Convert 'text<br><br>' to '<p>text</p>'.
        Element body = Jsoup.parseBodyFragment(html.toString()).body();
 
         for (Element br : body.getElementsByTag("br")) {
             Element previousBr = null;
 
             // Find the closest previous <br> without any intervening content.
             for (Node previousNode = br;
                     (previousNode = previousNode.previousSibling()) != null;
                     ) {
                 if (previousNode instanceof Element) {
                     Element previousElement = (Element) previousNode;
 
                     if (BR_TAG.equals(previousElement.tag())) {
                         previousBr = previousElement;
                     }
 
                     break;
 
                 } if (previousNode instanceof TextNode &&
                         !ObjectUtils.isBlank(((TextNode) previousNode).text())) {
                     break;
                 }
             }
 
             if (previousBr == null) {
                 continue;
             }
 
             List<Node> paragraphChildren = new ArrayList<Node>();
 
             for (Node previous = previousBr;
                     (previous = previous.previousSibling()) != null;
                     ) {
                 if (previous instanceof Element &&
                         ((Element) previous).isBlock()) {
                     break;
 
                 } else {
                     paragraphChildren.add(previous);
                 }
             }
 
             Element paragraph = new Element(P_TAG, "");
 
             for (Node child : paragraphChildren) {
                 child.remove();
                 paragraph.prependChild(child.clone());
             }
 
             br.before(paragraph);
             br.remove();
             previousBr.remove();
         }
 
         // Remove editorial markups.
         body.getElementsByTag("del").remove();
         body.getElementsByTag("ins").unwrap();
 
         // Remove empty paragraphs and stringify.
         StringBuilder cleaned = new StringBuilder();
 
         for (Node child : body.childNodes()) {
             if (child instanceof Element) {
                 Element childElement = (Element) child;
 
                 if (P_TAG.equals(childElement.tag()) &&
                         !childElement.hasText()) {
                     continue;
                 }
             }
 
             cleaned.append(child.toString());
         }
 
         List<Object> publishables = new ArrayList<Object>();
 
         addByBoundary(publishables, cleaned.toString(), boundary, references);
         return publishables;
     }
 
     // --- AbstractList support ---
 
     private Object checkItem(Object item) {
         ErrorUtils.errorIfNull(item, "item");
 
         if (item instanceof Reference) {
             return item;
 
         } else if (item instanceof Map) {
             Reference ref = new Reference();
             for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                 Object key = entry.getKey();
                 ref.put(key != null ? key.toString() : null, entry.getValue());
             }
             return ref;
 
         } else {
             return item.toString();
         }
     }
 
     @Override
     public void add(int index, Object item) {
         list.add(index, checkItem(item));
     }
 
     @Override
     public Object get(int index) {
         return list.get(index);
     }
 
     @Override
     public Object remove(int index) {
         return list.remove(index);
     }
 
     @Override
     public Object set(int index, Object item) {
         return list.set(index, checkItem(item));
     }
 
     @Override
     public int size() {
         return list.size();
     }
 }
