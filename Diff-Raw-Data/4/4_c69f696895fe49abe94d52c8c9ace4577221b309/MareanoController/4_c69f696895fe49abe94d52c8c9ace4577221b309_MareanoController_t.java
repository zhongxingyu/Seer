 package no.imr.geoexplorer.admindatabase.controller;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import no.imr.geoexplorer.admindatabase.dao.MareanoAdminDbDao;
 import no.imr.geoexplorer.admindatabase.jsp.pojo.HovedtemaVisning;
 import no.imr.geoexplorer.admindatabase.jsp.pojo.KartbilderVisning;
 import no.imr.geoexplorer.admindatabase.jsp.pojo.KartlagVisning;
 import no.imr.geoexplorer.admindatabase.mybatis.pojo.Hovedtema;
 import no.imr.geoexplorer.admindatabase.mybatis.pojo.HovedtemaEnNo;
 import no.imr.geoexplorer.admindatabase.mybatis.pojo.KartBilderEnNo;
 import no.imr.geoexplorer.admindatabase.mybatis.pojo.Kartbilder;
 import no.imr.geoexplorer.admindatabase.mybatis.pojo.Kartlag;
 import no.imr.geoexplorer.admindatabase.mybatis.pojo.KartlagEnNo;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Gets data from the mareano admin database and returns jsp friendly pojos with
  * the mav. An update to the database is sent if it last was updated more than
  * one day ago.
  *
  * @author endrem
  */
 @Controller
 public class MareanoController {
 
     private List<HovedtemaVisning> visninger = null;
     private long lastupdated = new Date().getTime();
 //	private final static long ADAY = 24 * 60 * 60 * 1000;
     private final static long TENMIN = 10 * 1000;
     private final static String ENGLISH = "en";
     
     @Autowired(required = true)
     private MareanoAdminDbDao dao;
 
     @RequestMapping("/mareano")
     public ModelAndView getMareanoTest(HttpServletResponse resp) {
         ModelAndView mav = new ModelAndView("mareano");
         getMareano(mav, "no");
         
         String heading = getMareanoHeading("");
         mav.addObject("heading", heading);
 
         resp.setCharacterEncoding("UTF-8");
         return mav;
     }
 
     protected ModelAndView getMareano(ModelAndView mav, String language) {
         long now = new Date().getTime();
         if (visninger == null || (lastupdated + TENMIN) < now) {
             visninger = listOrganizedToBrowser(language);
             lastupdated = new Date().getTime();
         }
         mav.addObject("hovedtemaer", visninger);
         return mav;
     }
 
     @RequestMapping("/mareano_en")
     public ModelAndView getMareanoEN(HttpServletResponse resp) {
         ModelAndView mav = new ModelAndView("mareano_en");
         mav = getMareano(mav, "en");
 
         mav.addObject("heading", getMareanoHeading(ENGLISH));
         return mav;
     }
 
     protected List<HovedtemaVisning> listOrganizedToBrowser(String language) {
 
         List<Hovedtema> hovedtemaer = dao.getHovedtemaer();
        List<HovedtemaVisning> hovedtemaVisninger = new ArrayList<HovedtemaVisning>(hovedtemaer.size());
 
         for (Hovedtema hovedtema : hovedtemaer) {
 //            if (!hovedtema.getGenericTitle().equals("Under utvikling")) {
                 HovedtemaVisning hovedtemaVisning = new HovedtemaVisning();
                 if (language.equals("en")) {
                     List<HovedtemaEnNo> en = dao.getHovedtemaEn(hovedtema.getHovedtemaerId());
                     if (en.size() > 0) {
                         hovedtemaVisning.setHovedtema(en.get(0).getAlternateTitle());
                     } else {
                         hovedtemaVisning.setHovedtema(hovedtema.getGenericTitle());
                     }
                 } else {
                     List<HovedtemaEnNo> norsk = dao.getHovedtemaNo(hovedtema.getHovedtemaerId());
                     if (norsk.size() > 0) {
                         hovedtemaVisning.setHovedtema(norsk.get(0).getAlternateTitle());
                     } else {
                         hovedtemaVisning.setHovedtema(hovedtema.getGenericTitle());
                     }
                 }
 
                 for (Kartbilder kartbilde : hovedtema.getKartbilder()) {
                     KartbilderVisning kartbilderVisining = new KartbilderVisning();
 
                     if (language.equals("en")) {
                         List<KartBilderEnNo> en = dao.getKartbilderEn(kartbilde.getKartbilderId());
                         if (en.size() > 0) {
                             kartbilderVisining.setGruppe(en.get(0).getAlternateTitle());
                         } else {
                             kartbilderVisining.setGruppe(kartbilde.getGenericTitle());
                         }
                     } else {
                         List<KartBilderEnNo> norsk = dao.getKartbilderNo(kartbilde.getKartbilderId());
                         if (norsk.size() > 0) {
                             kartbilderVisining.setGruppe(norsk.get(0).getAlternateTitle());
                         } else {
                             kartbilderVisining.setGruppe(kartbilde.getGenericTitle());
                         }
                     }
 
                     if (kartbilderVisining.getGruppe().equals("MAREANO-oversiktskart") || kartbilderVisining.getGruppe().equals("MAREANO - overview")) {
                         kartbilderVisining.setVisible(true);
                     }
                     List<Kartlag> kartlagene = dao.getKartlagene(kartbilde.getKartbilderId());
                     for (Kartlag kartlag : kartlagene) {
                         if (kartlag.isAvailable()) {
                             KartlagVisning kart = new KartlagVisning();
                             kart.setId(kartlag.getKartlagId());
                             kart.setLayers(kartlag.getLayers());
                             kart.setKeyword(kartlag.getKeyword());
                             kart.setExGeographicBoundingBoxEastBoundLongitude(kartlag.getExGeographicBoundingBoxEastBoundLongitude());
                             kart.setExGeographicBoundingBoxWestBoundLongitude(kartlag.getExGeographicBoundingBoxWestBoundLongitude());
                             kart.setExGeographicBoundingBoxNorthBoundLatitude(kartlag.getExGeographicBoundingBoxNorthBoundLatitude());
                             kart.setExGeographicBoundingBoxSouthBoundLatitude(kartlag.getExGeographicBoundingBoxSouthBoundLatitude());
                             kart.setScalemin(kartlag.getScalemin());
                             kart.setScalemax(kartlag.getScalemax());
 
                             if (language.equals("en")) {
                                 List<KartlagEnNo> en = dao.getKartlagEn(kart.getId());
                                 if (en.size() > 0) {
                                     kart.setTitle(en.get(0).getAlternateTitle());
                                     kart.setAbstracts(en.get(0).getAbstracts());
                                 } else {
                                     kart.setTitle(kartlag.getGenericTitle());
                                 }
                             } else {
                                 List<KartlagEnNo> norsk = dao.getKartlagNo(kart.getId());
                                 if (norsk.size() > 0) {
                                     kart.setTitle(norsk.get(0).getAlternateTitle());
                                     kart.setAbstracts(norsk.get(0).getAbstracts());
                                 } else {
                                     kart.setTitle(kartlag.getGenericTitle());
                                 }
                             }
 
                             kart.setUrl(kartlag.getKarttjeneste().getUrl());
                             kartbilderVisining.addKart(kart);
                         }
                     }
                     hovedtemaVisning.addBilder(kartbilderVisining);
                 }
                 if (hovedtemaVisning.getBilder().size() > 0) {
                     hovedtemaVisninger.add(hovedtemaVisning);
                 }
             //} //Under utvikling hovedtema
         }
         return hovedtemaVisninger;
     }
 
     protected String getMareanoHeading(String language) {
         StringBuffer heading = new StringBuffer();
         try {
             URL url = null;
             if (language.equals(ENGLISH)) {
                 url = new URL("http://www.mareano.no/en");
             } else {
                 url = new URL("http://www.mareano.no/");
             }
             BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
             String line;
             boolean headerContent = false;
             while ((line = reader.readLine()) != null) {
                 if (line.contains("<!--mainmenustart-->")) {
                     headerContent = true;
                 }
                 if (headerContent) {
                     heading.append(line);
                 }
                 if (line.contains("<!--mainmenuend-->")) {
                     headerContent = false;
                 }
 
             }
             reader.close();
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         String someHeading = "<table width=\"100%\" cellspacing=\"0\"><tr height=\"45\"> "
                 + "<td valign=\"middle\" height=\"45\" style=\"background-image:url(http://www.mareano.no/kart/images/top/ny_heading_397.gif); background-repeat: repeat;\"> "
                 + "<a style=\"text-decoration: none\" target=\"_top\" href=\"http://www.mareano.no\"> "
                 + "<img border=\"0\" alt=\"MAREANO<br>samler kunnskap om havet\" src=\"http://www.mareano.no/kart/images/top/ny_logo.gif\"> "
                 + "</a> "
                 + "</td> "
                 + "<td width=\"627\" align=\"right\" height=\"45\" style=\"background-image:url(http://www.mareano.no/kart/images/top/ny_heading_627.gif);\"> </td> "
                 + "</tr></table>";
 
         String newHeading = heading.toString();
         newHeading = newHeading.replaceAll("href=\"/", "href=\"http://www.mareano.no/");
         return someHeading + newHeading;
     }
     
 //    @Value("${propertiesMsg_no.advanced}") 
 //    private String test;
 }
