 package com.abudko.reseller.huuto.query.html.item;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.springframework.stereotype.Component;
 
 @Component
 public class HtmlItemParser {
 
     private static final String HTML_HV = "Hintavaraus";
 
     private static final String HTML_CONDITION = "Kuntoluokitus";
 
     private static final String HTML_LOCATION = "Tuotteen sijainti";
     
    private static final String IMG_SUFFIX = "-m.jpg";
 
     public ItemResponse parse(String html) {
         ItemResponse response = new ItemResponse();
 
         Boolean hv = parseHv(html);
         response.setHv(hv);
 
         Document document = Jsoup.parse(html);
 
         String conditionString = parseConditionFromDocument(document);
         response.setCondition(conditionString);
 
         String locationString = parseLocationFromDocument(document);
         response.setLocation(locationString);
 
         String imgSrcString = parseImgSrcFromDocument(document);
         response.setImgBaseSrc(imgSrcString);
 
         return response;
     }
 
     private Boolean parseHv(String html) {
         if (html.contains(HTML_HV)) {
             return Boolean.TRUE;
         }
         return Boolean.FALSE;
     }
 
     private String parseConditionFromDocument(Document document) {
         Elements conditionElement = document.getElementsContainingOwnText(HTML_CONDITION);
         Elements parents = conditionElement.parents();
         if (parents.size() > 0) {
             Element parent = parents.get(0);
             String condition = parent.parent().child(1).ownText();
             return condition;
         }
         return "";
     }
 
     private String parseLocationFromDocument(Document document) {
         Elements locationElement = document.getElementsContainingOwnText(HTML_LOCATION);
         Elements parents = locationElement.parents();
         if (parents.size() > 0) {
             Element parent = parents.get(0);
             String location = parent.parent().child(1).ownText();
             return location;
         }
         return "";
     }
 
     private String parseImgSrcFromDocument(Document document) {
         String imgSrc = "";
         Elements select = document.select("[src]");
         for (Element element : select) {
             if ("img".equals(element.tagName())) {
                 String imgSrcAttribute = element.attr("src");
                 if (imgSrcAttribute.contains(IMG_SUFFIX)) {
                     imgSrc = formatImgSrc(imgSrcAttribute);
                 }
             }
         }
         return imgSrc;
     }
 
     private String formatImgSrc(String imgSrc) {
         String formattedImgSrc = imgSrc.replace(IMG_SUFFIX, "");
         return formattedImgSrc;
     }
 }
