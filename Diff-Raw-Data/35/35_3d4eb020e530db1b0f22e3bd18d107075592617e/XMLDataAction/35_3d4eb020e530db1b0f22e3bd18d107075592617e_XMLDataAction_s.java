 package com.nkhoang.gae.action;
 
 import com.google.appengine.api.datastore.*;
 import com.google.appengine.api.labs.taskqueue.QueueFactory;
 import com.google.appengine.api.labs.taskqueue.TaskOptions;
 import com.nkhoang.gae.manager.GoldManager;
 import com.nkhoang.gae.model.Currency;
 import com.nkhoang.gae.model.GoldPrice;
 import com.nkhoang.gae.model.GoldPriceSortByTime;
 import com.nkhoang.gae.utils.DateConverter;
 import com.nkhoang.gae.utils.GoldConstants;
 import com.nkhoang.gae.view.XMLView;
 import com.nkhoang.gae.view.constant.ViewConstant;
 import com.ximpleware.AutoPilot;
 import com.ximpleware.VTDGen;
 import com.ximpleware.VTDNav;
 import com.ximpleware.XMLModifier;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.*;
 
 import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
 
 
 @Controller
 @RequestMapping("/data")
 public class XMLDataAction {
     private static final Logger LOGGER = LoggerFactory.getLogger(XMLDataAction.class);
     @Autowired
     private GoldManager goldService;
     // Request params.
     private static final String TASKCOUNT_PARAM = "taskcount";
     private static final String CLEAR_ALL_KIND_PARAM = "kind";
     private static final float Y_VALUE_PADDING = 0.15f;
 
 
     @RequestMapping(value = "/" + ViewConstant.CLEAR_ALL_VN_GOLD_PRICE_REQUEST, method = RequestMethod.GET)
     public String clearAllVNGoldPrice(HttpServletRequest request, HttpServletResponse response) {
         try {
             LOGGER.info("*** deleting VN GoldPrice form.");
             final long start = System.currentTimeMillis();
             int deleted_count = 0;
             boolean is_finished = false;
             List<GoldPrice> list = goldService.getVNGoldPrice("VND");
            LOGGER.info("List size = " + list.size());                                    
             if (list != null && list.size() > 0) {
                 while (System.currentTimeMillis() - start < 16384) {
                     GoldPrice p = list.get(deleted_count);
                     goldService.deleteGoldPrice(p.getId());
 
                     deleted_count++;
                     if (deleted_count == list.size()) {
                         is_finished = true;
                         break;
                     }
                 }
             } else {
                 is_finished = true;
             }
             LOGGER.info("*** deleted " + deleted_count);
             if (is_finished) {
                 LOGGER.info("*** deletion job is completed.");
             } else {
                 LOGGER.info("Posting to queue");
                 QueueFactory.getDefaultQueue().add(url("/data/" + ViewConstant.CLEAR_ALL_VN_GOLD_PRICE_REQUEST + ".html").method(TaskOptions.Method.GET));
             }
         } catch (Exception e) {
             LOGGER.error("Error when deleting all VN gold price", e);
         }
         // just return as a normal HTML.
         return "html";
     }
 
     /**
      * Clear all data from a kind.
      *
      * @param kind            can be any kind of object in String. (e.g. GoldPrice)
      * @param taskCountString starting counter.
      * @param request         HttpServletRequest object.
      * @param response        HttpServletResponse object.
      * @return view name.
      */
     @RequestMapping(value = "/" + ViewConstant.CLEAR_ALL_REQUEST, method = RequestMethod.GET)
     public String clearAll(@RequestParam(CLEAR_ALL_KIND_PARAM) String kind, @RequestParam(TASKCOUNT_PARAM) String taskCountString, HttpServletRequest request, HttpServletResponse response) {
         try {
             // check params.
             if (StringUtils.isEmpty(kind) || StringUtils.isEmpty(taskCountString)) {
                 response.getWriter().write("<h1>Bad Request</h1>");
             }
             LOGGER.info("*** deleting entities form " + kind);
             final long start = System.currentTimeMillis();
             int deleted_count = 0;
             boolean is_finished = false;
             final DatastoreService dss = DatastoreServiceFactory.getDatastoreService();
             while (System.currentTimeMillis() - start < 16384) {
                 final Query query = new Query(kind);
                 query.setKeysOnly();
                 final ArrayList<Key> keys = new ArrayList<Key>();
                 for (final Entity entity : dss.prepare(query).asIterable(FetchOptions.Builder.withLimit(128))) {
                     keys.add(entity.getKey());
                 }
                 keys.trimToSize();
                 if (keys.size() == 0) {
                     is_finished = true;
                     break;
                 }
                 while (System.currentTimeMillis() - start < 16384) {
                     try {
                         dss.delete(keys);
                         deleted_count += keys.size();
                         break;
                     } catch (Throwable ignore) {
                         continue;
                     }
                 }
             }
             LOGGER.info("*** deleted " + deleted_count + " entities form " + kind);
             if (is_finished) {
                 LOGGER.info("*** deletion job for " + kind + " is completed.");
             } else {
                 final int taskCount;
                 final String tcs = taskCountString;
                 if (tcs == null) {
                     taskCount = 0;
                 } else {
                     taskCount = Integer.parseInt(tcs) + 1;
                 }
                 QueueFactory.getDefaultQueue().add(url("/data/clearAll.html?" + CLEAR_ALL_KIND_PARAM + "=" + kind + "&" + TASKCOUNT_PARAM + "=" + taskCount).method(TaskOptions.Method.GET));
 
                 LOGGER.info("*** deletion task # " + taskCount + " for " + kind + " is queued.");
             }
         } catch (Exception e) {
             LOGGER.error("Error when deleting " + CLEAR_ALL_KIND_PARAM + ":" + kind, e);
         }
         // just return as a normal HTML.
         return "html";
     }
 
 
     /**
      * Return XML to be rendered as a chart.
      * <p/>
      * ?fromDate=2008-01-0%2016:00&toDate=2008-05-15%2015:00
      *
      * @param fromDateString in default Gold price format.
      * @param toDateString   in default Gold price format.
      * @return
      */
     @RequestMapping(value = "/" + ViewConstant.XML_DATA_CHART_REQUEST)
     public ModelAndView retrieveXMLData(@RequestParam("fromDate") String fromDateString, @RequestParam("toDate") String toDateString, HttpServletResponse response) {
         // test get price range
         ModelAndView mav = new ModelAndView();
         try {
             if (StringUtils.isNotEmpty(fromDateString) && StringUtils.isNotEmpty(toDateString)) {
                 Date fromDate = DateConverter.convertFromStringToken(fromDateString, DateConverter.defaultGoldDateFormat);
                 Date toDate = DateConverter.convertFromStringToken(toDateString, DateConverter.defaultGoldDateFormat);
 
                 Map<String, List<GoldPrice>> goldMap = buildGoldPrice4Chart(fromDate, toDate);
 
 
                 List<GoldPrice> vnList = goldMap.get("VND");
                 List<GoldPrice> inList = goldMap.get("USD");
 
                 Currency c = goldService.getExchangeRate("USD");
                 if (c == null) {
                     c = new Currency();
                     c.setPriceBuy(19.5f);
                     c.setPriceSell(19.5f);
                 }
                 convertGoldPriceList(inList, c);
 
                 String xmlStr = "";
                 if (vnList.size() != 0 && inList.size() != 0) {
                     // find min max for chart
                     List<Float> minMax = findMinMax(vnList);
                     if (minMax == null) {
                         // just in case.
                         minMax = findMinMax(inList);
                     }
 
                     LOGGER.info("Gold Price vnList: " + vnList.size());
 
                     InputStream is = this.getClass().getResourceAsStream("/MSLine.xml");
 
                     if (is == null) {
                         LOGGER.info("Could not load resources.");
                     }
 
                     VTDGen vg = new VTDGen(); // Instantiate VTDGen
                     StringWriter writer = new StringWriter();
                     IOUtils.copy(is, writer);
                     String theString = writer.toString();
                     vg.setDoc(theString.getBytes());
                     vg.parse(true);
 
                     XMLModifier xm = new XMLModifier(); //Instantiate XMLModifier
                     LOGGER.info("Starting to parse XML");
                     VTDNav vn = vg.getNav();
 
                     xm.bind(vn);
 
                     AutoPilot ap = new AutoPilot(vn);
                     // build caption
                     String caption = "from " + DateConverter.parseDateFromLong(vnList.get(0).getTime()) + " to " + DateConverter.parseDateFromLong(vnList.get(vnList.size() - 1).getTime());
                     // first update "subcaption" attribute of 'chart' tag.
                     int i = vn.getAttrVal("subcaption");
                     if (i != -1) {
                         xm.updateToken(i, caption);
                     }
                     ap.selectXPath("/chart");
 
                     i = vn.getAttrVal("yAxisMaxValue");
                     if (i != -1) {
                         xm.updateToken(i, (minMax.get(1) + Y_VALUE_PADDING) + "");
                     }
 
                     ap.selectXPath("/chart");
 
                     i = vn.getAttrVal("yAxisMinValue");
                     if (i != -1) {
                         float minValue = 0f;
                         if (minMax.get(0) > Y_VALUE_PADDING) {
                             minValue = minMax.get(0) - Y_VALUE_PADDING;
                         }
                         xm.updateToken(i, minValue + "");
                     }
                     ap.selectXPath("/chart/categories");
                     i = -1;
                     while ((i = ap.evalXPath()) != -1) {
                         String categoryTag = "";
                         for (GoldPrice p : vnList) {
 
                             categoryTag += "\n\t<category label='" + DateConverter.parseDateFromLong(p.getTime()) + "'/>";
                         }
                         xm.insertAfterHead(categoryTag);
                     }
                     ap.selectXPath("/chart/dataset[@seriesName='VN']");
                     i = -1;
                     while ((i = ap.evalXPath()) != -1) {
                         String setTag = "";
                         for (GoldPrice p : vnList) {
                             String setVal = "";
                             if (p.getPriceBuy() != null) {
                                 setVal = p.getPriceBuy() != 0 ? p.getPriceBuy() + "" : "";
                             }
                             setTag += "\n\t<set value='" + setVal + "'/>";
 
                         }
                         xm.insertAfterHead(setTag);
                     }
                     ap.selectXPath("/chart/dataset[@seriesName='International']");
                     i = -1;
 
                     while ((i = ap.evalXPath()) != -1) {
                         String setTag = "";
                         for (GoldPrice p : inList) {
                             String setVal = "";
                             if (p.getPriceBuy() != null) {
                                 setVal = (p.getPriceBuy() != 0 ? p.getPriceBuy() + "" : "");
                             }
                             setTag += "\n\t<set value='" + setVal + "'/>";
 
                         }
                         xm.insertAfterHead(setTag);
                     }
 
                     ByteArrayOutputStream bos = new ByteArrayOutputStream();
 
                     xm.output(bos);
 
                     xmlStr = bos.toString();
                 }
 
                 View xmlView = new XMLView();
                 mav.setView(xmlView);
                 mav.addObject("data", xmlStr);
             } else {
                 response.setContentType("text/html");
             }
 
         } catch (Exception ex) {
             LOGGER.error("Could not parse XML file", ex);
             response.setContentType("text/html");
         }
 
         return mav;
     }
 
     private List<Float> findMinMax(List<GoldPrice> list) {
         Float min = 0f, max = 0f;
         for (GoldPrice p : list) {
             Float pBuy = p.getPriceBuy();
             if (pBuy != null && pBuy != 0f) {
                 if (max == 0) {
                     max = pBuy;
                 }
                 if (min == 0) {
                     min = pBuy;
                 }
                 if (pBuy > max) {
                     max = pBuy;
                 }
                 if (pBuy < min) {
                     min = pBuy;
                 }
             }
         }
 
         List<Float> result = new ArrayList();
 
         if (min == 0f && max == 0f) {
             result = null;
         } else {
             result.add(min);
             result.add(max);
         }
         return result;
     }
 
     /**
      * Build gold price for chart.
      *
      * @param fromDate from date.
      * @param toDate   to date.
      * @return a map of gold price for both USD and VND
      */
     private Map<String, List<GoldPrice>> buildGoldPrice4Chart(Date fromDate, Date toDate) {
         Map<String, List<GoldPrice>> result = new HashMap<String, List<GoldPrice>>();
 
         List<GoldPrice> vnList = new ArrayList(0);
         List<GoldPrice> inList = new ArrayList(0);
 
         vnList.addAll(goldService.getGoldPriceWithRange("VND", fromDate, toDate));
         inList.addAll(goldService.getGoldPriceWithRange("USD", fromDate, toDate));
         List<GoldPrice> vnListAdded = new ArrayList();
         List<GoldPrice> inListAdded = new ArrayList();
 
         LOGGER.info("VN list =" + vnList.size());
         LOGGER.info("IN list =" + inList.size());
 
         List<Long> timeList = new ArrayList<Long>();
         for (GoldPrice p : vnList) {
             GoldPrice unit = new GoldPrice();
             unit.setTime(p.getTime());
             unit.setCurrency("USD");
             inListAdded.add(unit);
             timeList.add(p.getTime());
         }
 
         for (GoldPrice p : inList) {
             if (!timeList.contains(p.getTime())) {
                 GoldPrice unit = new GoldPrice();
                 unit.setTime(p.getTime());
                 unit.setCurrency("VND");
                 vnListAdded.add(unit);
                 timeList.add(p.getTime());
             }
         }
 
         vnList.addAll(vnListAdded);
         // perform sorting
         java.util.Collections.sort(vnList, new GoldPriceSortByTime());
 
         inList.addAll(inListAdded);
         java.util.Collections.sort(inList, new GoldPriceSortByTime());
 
         for (int i = 0; i < vnList.size(); i++) {
             GoldPrice p = vnList.get(i);
            if (p.getPriceBuy() == null) {
                 if (i != 0) {
                     GoldPrice previous = vnList.get(i - 1);
                    if (previous.getPriceBuy() != null) {
                        p.setPriceBuy(previous.getPriceBuy());
                        p.setPriceSell(previous.getPriceSell());
                    }
                 } else {
                     p.setPriceBuy(0f);
                     p.setPriceSell(0f);
                 }

             }
 
             GoldPrice g = inList.get(i);
            if (g.getPriceBuy() == null) {
                 if (i != 0) {
                     GoldPrice previous = inList.get(i - 1);
                    if (previous.getPriceBuy() != null) {
                        g.setPriceBuy(previous.getPriceBuy());
                        g.setPriceSell(previous.getPriceSell());
                    }
                 } else {
                     p.setPriceBuy(0f);
                     p.setPriceSell(0f);
                 }
 
             }
         }
 
         LOGGER.info(vnList.toArray().toString());
 
 
         result.put("VND", vnList);
         result.put("USD", inList);
 
         return result;
     }
 
     private void convertGoldPriceList(List<GoldPrice> list, com.nkhoang.gae.model.Currency c) {
         for (GoldPrice p : list) {
             p.setPriceSell(convertGoldUS2VN(p.getPriceSell(), c.getPriceSell()));
             p.setPriceBuy(convertGoldUS2VN(p.getPriceBuy(), c.getPriceBuy()));
         }
     }
 
     private Float convertGoldUS2VN(Float price, Float exchangeRate) {
         Float result = 0f;
         if (price != null && exchangeRate != null) {
             result = ((GoldConstants.vnoz * price) / GoldConstants.oz) / 1000 * exchangeRate;
         }
         return result;
     }
 
     public GoldManager getGoldService() {
         return goldService;
     }
 
     public void setGoldService(GoldManager goldService) {
         this.goldService = goldService;
     }
 }
