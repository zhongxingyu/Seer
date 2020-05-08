 package com.abudko.reseller.huuto.query.html.list;
 
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Attributes;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.springframework.stereotype.Component;
 
 import com.abudko.reseller.huuto.query.enumeration.Brand;
 
 @Component
 public class HtmlListParser {
 
     public static final String HUUTO_NET = "http://www.huuto.net";
 
     public static final String ITEM_URL_CONTEXT = "/kohteet/";
 
     private static final String IMG_SUFFIX = "-s.jpg";
 
     private static final String HTML_RESULT_LIST_CLASS = "row item ";
     private static final String HTML_ELEMENT_PRICES = "spanprice";
     private static final String HTML_ELEMENT_DESCRIPTION = "spandesc";
     private static final String HTML_ELEMENT_IMAGE = "spanimage";
     private static final String HTML_ELEMENT_BIDS = "spanbuy";
     private static final String HTML_ELEMENT_CLOSING_TIME = "closingtime";
 
    private static final String EURO_CHAR = "€";
 
     public Collection<ListResponse> parse(String htmlResponse) {
         String html = truncateUnused(htmlResponse);
 
         Collection<ListResponse> responses = new LinkedHashSet<ListResponse>();
         Document document = Jsoup.parse(html);
 
         Elements elements = document.getElementsByAttributeValueContaining("class", HTML_RESULT_LIST_CLASS);
         for (Element element : elements) {
             ListResponse queryResponse = new ListResponse();
 
             String description = parseDescription(element);
             queryResponse.setDescription(description);
 
             String currentPrice = parseCurrentPrice(element);
             queryResponse.setCurrentPrice(currentPrice);
 
             String fullPrice = parseFullPrice(element);
             queryResponse.setFullPrice(fullPrice);
 
             String itemUrl = parseItemUrl(element);
             queryResponse.setItemUrl(itemUrl);
 
             String imgBaseSrc = parseImgSrc(element);
             queryResponse.setImgBaseSrc(imgBaseSrc);
 
             String bids = parseBids(element);
             queryResponse.setBids(bids);
 
             String last = parseLast(element);
             queryResponse.setLast(last);
 
             String size = parseSize(description);
             queryResponse.setSize(size);
 
             String brand = parseBrand(description);
             queryResponse.setBrand(brand);
 
             responses.add(queryResponse);
         }
 
         return responses;
     }
 
     private String truncateUnused(String htmlResponse) {
         String str = String.format("<div class=\"%s\"", HTML_RESULT_LIST_CLASS);
         int indexOfResultList = htmlResponse.indexOf(str);
         if (indexOfResultList < 0) {
             return htmlResponse;
         }
         String truncatedHtml = htmlResponse.substring(indexOfResultList);
         return truncatedHtml;
     }
 
     private String parseCurrentPrice(Element element) {
         Element pricesElement = element.getElementsByClass(HTML_ELEMENT_PRICES).get(0);
         Element currentPriceElement = pricesElement.child(0);
         if (currentPriceElement.children().isEmpty() == false) {
             return formatPrice(currentPriceElement.child(0).ownText());
         } else {
             return formatPrice(currentPriceElement.ownText());
         }
     }
 
     private String parseFullPrice(Element element) {
         Element pricesElement = element.getElementsByClass(HTML_ELEMENT_PRICES).get(0);
         Element currentPriceElement = pricesElement.child(0);
         if (pricesElement.children().size() > 1) {
             Element fullPriceElement = pricesElement.child(1);
             return formatPrice(fullPriceElement.ownText());
         }
         if (currentPriceElement.children().isEmpty() == false) {
             return formatPrice(currentPriceElement.child(0).ownText());
         } else {
             return formatPrice(currentPriceElement.ownText());
         }
     }
 
     private String formatPrice(String price) {
         String euro = String.format(" %s", EURO_CHAR);
         String priceDouble = price.substring(0, price.indexOf(euro));
         return priceDouble.replace(",", ".");
     }
 
     private String parseDescription(Element element) {
         Element descriptionElement = element.getElementsByClass(HTML_ELEMENT_DESCRIPTION).get(0);
         Element child = descriptionElement.child(0);
         String description = child.ownText();
         return description;
     }
 
     private String parseItemUrl(Element element) {
         Element descriptionElement = element.getElementsByClass(HTML_ELEMENT_DESCRIPTION).get(0);
         Attributes attributes = descriptionElement.child(0).attributes();
         String itemUrl = attributes.get("href");
         return formatItemUrl(itemUrl);
     }
 
     private String formatItemUrl(String itemUrl) {
         String formattedItemUrl = itemUrl.replace(ITEM_URL_CONTEXT, "");
         return formattedItemUrl;
     }
 
     private String parseBids(Element element) {
         Element bidsElement = element.getElementsByClass(HTML_ELEMENT_BIDS).get(0);
         Elements children = bidsElement.children();
         StringBuilder sb = new StringBuilder();
         for (Element bid : children) {
             sb.append(bid.ownText());
             sb.append(" ");
         }
         return sb.toString();
     }
 
     private String parseLast(Element element) {
         Element lastElement = element.getElementsByClass(HTML_ELEMENT_CLOSING_TIME).get(0);
         String last = lastElement.child(0).ownText();
         return last;
     }
 
     private String parseImgSrc(Element element) {
         Element descriptionElement = element.getElementsByClass(HTML_ELEMENT_IMAGE).get(0);
         Element child = descriptionElement.child(0);
         String imgSrc = child.child(0).attr("src");
         return formatImgSrc(imgSrc);
     }
 
     private String formatImgSrc(String imgSrc) {
         String formattedImgSrc = imgSrc.replace(IMG_SUFFIX, "");
         return formattedImgSrc;
     }
 
     private String parseSize(String description) {
         Matcher matcher = Pattern.compile("\\d+").matcher(description);
         if (matcher.find()) {
             return matcher.group();
         }
         return null;
     }
 
     private String parseBrand(String description) {
         Brand brand = Brand.getBrandFrom(description);
         if (brand != null) {
             return brand.getFullName();
         }
         return null;
     }
 }
