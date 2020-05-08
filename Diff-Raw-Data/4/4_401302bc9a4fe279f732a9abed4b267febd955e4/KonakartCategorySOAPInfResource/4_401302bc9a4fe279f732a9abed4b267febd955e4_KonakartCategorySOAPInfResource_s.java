 /*
  * Copyright 2010 Wyona
  */
 
 package org.wyona.yanel.resources.konakart.category;
 
 import org.wyona.yanel.impl.resources.BasicXMLResource;
 import org.wyona.yanel.resources.konakart.shared.SharedResource;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import org.apache.log4j.Logger;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 import com.konakart.app.Basket;
 import com.konakart.app.EngineConfig;
 import com.konakart.app.KKEng;
 import com.konakart.app.ProductSearch;
 
 import com.konakart.appif.BasketIf;
 import com.konakart.appif.KKEngIf;
 import com.konakart.appif.CategoryIf;
 import com.konakart.appif.LanguageIf;
 import com.konakart.appif.ManufacturerIf;
 import com.konakart.appif.ProductIf;
 import com.konakart.appif.ProductsIf;
 import com.konakart.appif.ProductSearchIf;
 import com.konakart.appif.ProductsIf;
 import com.konakart.util.KKConstants;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Element;
 import java.math.BigDecimal;
 import java.util.Comparator;
 import java.util.Arrays;
 import java.io.ByteArrayInputStream;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import java.net.URLEncoder;
 
 /**
  * KonaKart category
  */
 public class KonakartCategorySOAPInfResource extends BasicXMLResource {
     
     private static Logger log = Logger.getLogger(KonakartCategorySOAPInfResource.class);
 
     private static String KONAKART_NAMESPACE = "http://www.konakart.com/1.0";
 
     /**
      * @see org.wyona.yanel.core.api.attributes.ViewableV2#getView(java.lang.String)
      */
     public View getView(String viewId) throws Exception {
         SharedResource shared = new SharedResource();
         if(!shared.isKKOnline()) {
             // Konakart is offline
             // We return error 503 (temporarily unavailable)
             // because that is the right thing to do! If we
             // returned 404 (not found) some search engines
             // might delete us from their index and we don't
             // want that to happen.
             View view = new View();
             view.setResponse(false);
             HttpServletResponse response = getEnvironment().getResponse();
             response.sendError(503, "The shop is currently unavailable.");
             return view;
         }
 
         // All is well
         return getXMLView(viewId, getContentXML(viewId));
     }
     
     /**
      * Internal class for comparisons of products.
      */
     class ProductComparator implements Comparator {
         public String field;
         public boolean reverse;
 
         public ProductComparator(String field, String reverse) {
             this.field = field;
             this.reverse = reverse == null ? false : reverse.equalsIgnoreCase("true");
         }
 
         public int compare(Object o1, Object o2) {
             ProductIf p1 = (ProductIf) o1;
             ProductIf p2 = (ProductIf) o2;
             
             if(field.equalsIgnoreCase("price")) {
                 BigDecimal price1, price2;
 
                 price1 = p1.getSpecialPriceExTax();
                 if(price1 == null) {
                     price1 = p1.getPriceExTax();
                 }
 
                 price2 = p2.getSpecialPriceExTax();
                 if(price2 == null) {
                     price2 = p2.getPriceExTax();
                 }
 
                 int retval = price1 == price2 ? 0 : price1.compareTo(price2);
                 if(reverse) retval = (-1)*retval;
                 return retval;
             }
 
             return 0;
         }
 
         public boolean equals(Object o) {
             if(o instanceof ProductComparator) {
                 ProductComparator p = (ProductComparator) o;
                 return (this.field.equals(p.field) && this.reverse == p.reverse);
             }
             return false;
         }
     }
 
     /**
      * Generate XML of a KonaKart category
      */
     protected InputStream getContentXML(String viewId) throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("requested viewId: " + viewId);
         }
 
         SharedResource shared = new SharedResource();
         KKEngIf kkEngine = shared.getKonakartEngineImpl();
         int languageId = shared.getLanguageId(getContentLanguage());
         int customerId = shared.getTemporaryCustomerId(getEnvironment().getRequest().getSession(true));
         String sessionId = shared.getSessionId(getEnvironment().getRequest().getSession(true));
 
         org.w3c.dom.Document doc = null;
         try {
             doc = org.wyona.commons.xml.XMLHelper.createDocument(KONAKART_NAMESPACE, "category");
         } catch (Exception e) {
             throw new Exception(e.getMessage(), e);
         }
         Element rootElement = doc.getDocumentElement();
 
         int categoryId = getCategoryId(kkEngine);
         log.debug("Category ID: " + categoryId);
 
         if (languageId == -1) {
             String message = "No such language: " + getContentLanguage();
             log.error(message);
 
             Element exceptionElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "exception"));
             exceptionElement.appendChild(doc.createTextNode(message));
         } else {
             rootElement.setAttributeNS(KONAKART_NAMESPACE, "language-code", getContentLanguage());
             rootElement.setAttributeNS(KONAKART_NAMESPACE, "language-id", "" + languageId);
             log.debug("Content language: " + getContentLanguage());
         }
 
         CategoryIf[] categories = kkEngine.getCategoryTree(languageId, true);
         if (categories != null) {
             appendCategories(categories, rootElement, categoryId);
         } else {
             String message = "No categories for language: " + getContentLanguage();
             Element exceptionElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "exception"));
             exceptionElement.appendChild(doc.createTextNode(message));
         }
 
         CategoryIf category = kkEngine.getCategory(categoryId, languageId);
         if (category != null) {
             Element categoryElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "category"));
             categoryElement.setAttributeNS(KONAKART_NAMESPACE, "id", "" + categoryId);
             categoryElement.setAttributeNS(KONAKART_NAMESPACE, "language", getContentLanguage());
             categoryElement.setAttributeNS(KONAKART_NAMESPACE, "language-id", "" + languageId);
             categoryElement.appendChild(doc.createTextNode(category.getName()));
         } else {
             String message = "No such category: " + categoryId + ", " + getContentLanguage();
             Element exceptionElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "exception"));
             exceptionElement.appendChild(doc.createTextNode(message));
         }
 
         // Pagination
         int current_page;
         int total_pages;
         int items_per_page;
         int first_item;
         int last_item;
 
         try {
             current_page = new Integer(getEnvironment().getRequest().getParameter("page")).intValue();
         } catch(Exception e) {
             current_page = 1;
         }
 
         try {
             items_per_page = new Integer(getResourceConfigProperty("items-per-page")).intValue();
         } catch(Exception e) {
             log.warn("Using default value for items per page.");
             // TODO: Is this a sane default value?
             items_per_page = 7;
         }
 
         first_item = (current_page - 1)*items_per_page;
         last_item = first_item + items_per_page;
 
         ProductsIf prods = kkEngine.getProductsPerCategory(null, null, categoryId, true, languageId);
 
         // Print products
         if (prods != null) {
             log.debug("Number of products for category '" + categoryId + "' and language '" + languageId + "': " + prods.getTotalNumProducts());
             ProductIf[] products = prods.getProductArray();
 
             if (products != null)  {
                 // Pagination
                 if(first_item > products.length) {
                     // Beyond last page, go back to first page..
                     first_item = 0;
                     last_item = items_per_page;
                 }
 
                 if(last_item > products.length) last_item = products.length;
                 total_pages = products.length / items_per_page;
                 if((products.length % items_per_page) > 0) total_pages++;
 
                 String sort_field = getEnvironment().getRequest().getParameter("sort");
                 String reverse = getEnvironment().getRequest().getParameter("reverse");
 
                 if(sort_field == null) sort_field = "price";
 
                 Arrays.sort(products, new ProductComparator(sort_field, reverse));
 
                 Element sortfieldElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "sort-field"));
                 sortfieldElement.appendChild(doc.createTextNode("" + sort_field));
                 Element reverseElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "reverse-sort"));
                 reverseElement.appendChild(doc.createTextNode("" + reverse));
  
                 Element currentPageElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "current-page"));
                 currentPageElement.appendChild(doc.createTextNode("" + current_page));
                 Element lastPageElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "last-page"));
                 lastPageElement.appendChild(doc.createTextNode("" + total_pages));
             
                 Element productsElement = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "products"));
 
                 int productsDescMaxLen;
                 String productsDescMaxLenStr = getResourceConfigProperty("description-chars");
 
                 try {
                     productsDescMaxLen = new Integer(productsDescMaxLenStr).intValue();
                 } catch(Exception e) {
                     log.warn("Using default value for description length.");
                     // TODO: Is this a sane default value?
                     productsDescMaxLen = 100;
                 }
 
                 for (int i = first_item; i < last_item; i++) {
                     // Id, image, name
                     Element productElement = (Element) productsElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "product"));
                     productElement.setAttributeNS(KONAKART_NAMESPACE, "id", "" + products[i].getId());
                     productElement.setAttributeNS(KONAKART_NAMESPACE, "image-name", "" + products[i].getImage2());
                     Element productName = (Element) productElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "name"));
                     productName.appendChild(doc.createTextNode(products[i].getName()));
                     Element sproductName = (Element) productElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "stripped-name"));
                     String stripped_name = products[i].getName().replaceAll("[ /\\\\]+","-");
                     sproductName.appendChild(doc.createTextNode(URLEncoder.encode(stripped_name, "UTF-8")));
 
                     // Prices
                     BigDecimal price, special_price;
 
                     try {
                         price = products[i].getPriceExTax().setScale(2);
                     } catch(Exception e) {
                         price = products[i].getPriceExTax();
                     }
 
                     try {
                         special_price = products[i].getSpecialPriceExTax().setScale(2);
                     } catch(Exception e) {
                         special_price = products[i].getSpecialPriceExTax();
                     }
 
                     Element priceExTax = (Element) productElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "price-ex-tax"));
                     priceExTax.appendChild(doc.createTextNode("" + price));
                     Element specialPriceExTax = (Element) productElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "special-price-ex-tax"));
                     specialPriceExTax.appendChild(doc.createTextNode("" + special_price));
 
                     // Description
                     // Note: The getProductsPerCategory() function of the KonaKart engine returns the products,
                     // but the fields "description" and "category" are always null for some reason. The documentation
                     // says: "The description (which can be very long) and the array of options are not set."
                     // So, we need to call getProduct() if we want the full info - and we certainly do.
                     ProductIf temp_product = kkEngine.getProduct(sessionId, products[i].getId(), languageId);
 
                     // Not needed anymore, description text is not intended for category page.
                     //String desc = temp_product.getDescription();
                     //if(desc == null) desc = "No description found!";
                     //if(desc.length() > productsDescMaxLen) desc = desc.substring(0, productsDescMaxLen - 3) + "...";
                     //Element descElement = (Element) productElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "description"));
                     //descElement.appendChild(doc.createTextNode(desc));
 
                     // Units
                     Element unitElement = (Element) productElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "units"));
                     unitElement.appendChild(doc.createTextNode(shared.getItemUnits(temp_product, getContentLanguage(), false)));
 
                     // Product comparison (can contain html)
                     DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
                     try {
                         // Here we parse and import the content of the field. Xalan doesn't let us
                         // import a whole document, so we need to import the child nodes individually.
                         // So we just loop over all the child nodes and import them...
                         String comparison = "<comparison>" + temp_product.getComparison() + "</comparison>";
                         Node pdoc = (Node) builder.parse(new ByteArrayInputStream(comparison.getBytes()));
                         NodeList children = pdoc.getChildNodes();
                         for(int j = 0; j < children.getLength(); j++) {
                             productElement.appendChild(doc.importNode(children.item(j), true));
                         }
                     } catch(Exception e) {
                         Element descElem = (Element) productElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "comparison"));
                         descElem.appendChild(doc.createTextNode("No description field."));
                         log.warn(e,e);
                     }
  
                     // Category
                     CategoryIf prodcat = kkEngine.getCategory(temp_product.getCategoryId(), languageId);
                     Element catElement = (Element) productElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "category"));
                     catElement.appendChild(doc.createTextNode(prodcat == null ? "null" : prodcat.getName()));
                 }
             }
         }
 
         java.io.ByteArrayOutputStream baout = new java.io.ByteArrayOutputStream();
         org.wyona.commons.xml.XMLHelper.writeDocument(doc, baout);
         return new java.io.ByteArrayInputStream(baout.toByteArray());
     }
 
     /**
      * Get category ID
      */
     private int getCategoryId(KKEngIf eng) throws Exception {
         String categoryIdStr = getResourceConfigProperty("category-id");
         if (categoryIdStr == null) {
             String name = org.wyona.commons.io.PathUtil.getName(getPath());
             categoryIdStr = name.substring(0, name.indexOf(".html"));
         }
         return Integer.parseInt(categoryIdStr);
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.ViewableV2#exists()
      */
     public boolean exists() throws Exception {
         SharedResource shared = new SharedResource();
         if(!shared.isKKOnline()) return true;
         int languageId = shared.getLanguageId(getContentLanguage());
 
         if (languageId == -1) {
             log.error("No such language: " + getContentLanguage());
             return false;
         }
 
         KKEngIf kkEngine = shared.getKonakartEngineImpl();
         int categoryId = getCategoryId(kkEngine);
 
         CategoryIf category = kkEngine.getCategory(categoryId, languageId);
         if (category != null) {
             return true;
         } else {
             log.error("No such category ID: " + categoryId + " (Language: " + getContentLanguage() + ", Path: " + getPath() + ")");
             return false;
         }
     }
 
     /**
      * Append categories
      * @param categories Categories to append
      * @param element Element to which categories shall be appended
      * @param selectedCategoryID Selected category ID
      */
     private boolean appendCategories(CategoryIf[] categories, Element element, int selectedCategoryID) throws Exception {
         boolean selected = false;
         org.w3c.dom.Document doc = element.getOwnerDocument();
         if (categories != null && categories.length > 0) {
             Element categoriesElement = (Element) element.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "categories"));
             for (int i = 0; i < categories.length; i++) {
                 Element categoryElement = (Element) categoriesElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "category"));
                 categoryElement.setAttributeNS(KONAKART_NAMESPACE, "id", "" + categories[i].getId());
                 if (categories[i].getId() == selectedCategoryID) {
                     selected = true;
                     log.debug("The category " + selectedCategoryID + " is selected.");
                     categoryElement.setAttributeNS(KONAKART_NAMESPACE, "selected", "true");
                 }
                 Element nameElement = (Element) categoryElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "name"));
                 nameElement.appendChild(doc.createTextNode(categories[i].getName()));
 
                 CategoryIf[] childCategories = categories[i].getChildren();
                 boolean child_selected = appendCategories(childCategories, categoryElement, selectedCategoryID);
                 if(child_selected) {
                     selected = true;
                     categoryElement.setAttributeNS(KONAKART_NAMESPACE, "selected", "true");
                     log.debug("The category " + categories[i].getId() + " has selected child categories.");
                 }
             }
         } else {
             log.debug("No (sub-)categories.");
         }
 
         return selected;
     }
 }
