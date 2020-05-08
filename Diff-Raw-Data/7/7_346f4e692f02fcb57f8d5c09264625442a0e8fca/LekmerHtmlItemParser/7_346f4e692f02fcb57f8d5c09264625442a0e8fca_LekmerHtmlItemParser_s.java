 package com.abudko.reseller.huuto.query.service.item.html.lekmer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.springframework.stereotype.Component;
 
 import com.abudko.reseller.huuto.query.html.HtmlParserConstants;
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.item.html.HtmlItemParser;
 
 @Component
 public class LekmerHtmlItemParser implements HtmlItemParser {
     
     private static final String HTML_PRODUCT_ID = "product_band";
     
     @Override
     public ItemResponse parse(String html) {
         ItemResponse response = new ItemResponse();
         
         Document document = Jsoup.parse(html);
 
         List<String> sizes = parseSizes(document);
         response.setSizes(sizes);
 
         String price = parsePrice(document);
         response.setPrice(price);
 
         String imgSrc = parseImgSrc(document);
         response.setImgBaseSrc(imgSrc);
 
         String id = parseId(document);
         response.setId(id);
         
         return response;
     }
 
     private List<String> parseSizes(Document document) {
         List<String> sizes = new ArrayList<>();
         Elements elements = document.getElementsByAttributeValueMatching("class", Pattern.compile("[^0]erpseparator\\w+"));
         for (Element element : elements) {
             String text = element.text();
             if (text.length() > 2) {
                 if (!text.contains("/")) {
                     sizes.add(text.substring(0, 3).trim());
                 }
                 else {
                     int index = text.indexOf("/");
                     sizes.add(text.substring(0, index).trim());
                     sizes.add(text.substring(index + 1, text.trim().length()).trim());
                 }
             }
             else {
                 sizes.add(text.substring(0, 2).trim());
             }
         }
         return sizes;
     }
 
     private String parseImgSrc(Document document) {
         Elements elements = document.getElementsByAttributeValue("rel", "image_src");
         if (elements.size() > 0) {
             Element element = elements.get(0);
             String imgSrc = element.attributes().get("href");
             return imgSrc;
         }
         return "";
     }
     
     private String parsePrice(Document document) {
         Elements elements = document.getElementsByAttributeValue("class", "campaignprice-value");
         if (elements.size() == 0) {
             elements = document.getElementsByAttributeValue("class", "price-value");
         }
         if (elements.size() > 0) {
             Element element = elements.get(0);
             String price = element.childNode(0).toString();
             return price.replace(HtmlParserConstants.EURO_CHAR, "").trim().replace(",", ".");
         }
         return "";
     }
     
     private String parseId(Document document) {
         Elements elements = document.getElementsByClass(HTML_PRODUCT_ID);
         if (elements.size() > 0) {
             Element element = elements.get(0);
            Element child = element.child(0).child(1);
            return HtmlParserConstants.LEKMER_ID_PREFIX + getValidId(child.ownText());
         }
         return "";
     }
     
     protected String getValidId(String id) {
         try {
             Long.parseLong(id);
         }
         catch (NumberFormatException e) {
             return id;
         }
 
         Matcher matcher = Pattern.compile("[1-9][0-9]{9,1000}$").matcher(id);
         if (matcher.find()) {
             return id.substring(0, 9);
         }
         
         return id;
     }
 }
