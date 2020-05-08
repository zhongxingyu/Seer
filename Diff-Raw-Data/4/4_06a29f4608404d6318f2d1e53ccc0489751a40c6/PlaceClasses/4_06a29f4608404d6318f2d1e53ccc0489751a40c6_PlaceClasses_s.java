 package com.celements.crm.place;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.classes.AbstractClassCollection;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 
 @Component("celements.crm.places")
 public class PlaceClasses extends AbstractClassCollection {
   
   private static Log LOGGER = LogFactory.getFactory().getInstance(PlaceClasses.class);
   
   public void initClasses() throws XWikiException {
     getAddressClass();
     getCityClass();
   }
 
   public DocumentReference getAddressClassRef(String wikiName) {
     return new DocumentReference(wikiName, "CelementsPlaces", "AddressClass");
   }
 
   BaseClass getAddressClass() throws XWikiException {
     XWikiDocument doc;
     boolean needsUpdate = false;
     DocumentReference classRef = getAddressClassRef(getContext().getDatabase());
     try {
       doc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get address class document.", exp);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
 
     BaseClass bclass = doc.getXClass();
     bclass.setXClassReference(classRef);
     needsUpdate |= bclass.addTextField("street", "Street", 30);
     needsUpdate |= bclass.addTextField("houseNumber", "House Number", 30);
     needsUpdate |= bclass.addTextField("zip", "ZIP", 30);
     needsUpdate |= bclass.addTextField("city", "City", 30);
     needsUpdate |= bclass.addTextField("country", "Country", 30);
    needsUpdate |= bclass.addDateField("validFrom", "valid from date", "dd.MM.yyy", 0);
    needsUpdate |= bclass.addDateField("validUntil", "valid until date", "dd.MM.yyy", 0);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getCityClassRef(String wikiName) {
     return new DocumentReference(wikiName, "CelementsPlaces", "CityClass");
   }
 
   BaseClass getCityClass() throws XWikiException {
     XWikiDocument doc;
     boolean needsUpdate = false;
     DocumentReference classRef = getCityClassRef(getContext().getDatabase());
     try {
       doc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get city class document.", exp);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
 
     BaseClass bclass = doc.getXClass();
     bclass.setXClassReference(classRef);
     needsUpdate |= bclass.addTextField("zip", "ZIP", 8);
     needsUpdate |= bclass.addTextField("shortName", "City Short Name", 30);
     needsUpdate |= bclass.addTextField("name", "City Name", 30);
     needsUpdate |= bclass.addTextField("county", "County", 30);
     needsUpdate |= bclass.addTextField("primaryLanguage", "Primary Language", 30);
     needsUpdate |= bclass.addNumberField("countryISONum", "Country ISO Number", 3,
         "integer");
     needsUpdate |= bclass.addDateField("validFrom", "valid from date", "dd.MM.yyy", 0);
     needsUpdate |= bclass.addDateField("validUntil", "valid until date", "dd.MM.yyy", 0);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getCityNameClassRef(String wikiName) {
     return new DocumentReference(wikiName, "CelementsPlaces", "CityNameClass");
   }
 
   BaseClass getCityNameClass() throws XWikiException {
     XWikiDocument doc;
     boolean needsUpdate = false;
     DocumentReference classRef = getCityNameClassRef(getContext().getDatabase());
     try {
       doc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get cityName class document.", exp);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
 
     BaseClass bclass = doc.getXClass();
     bclass.setXClassReference(classRef);
     needsUpdate |= bclass.addTextField("lang", "iso language code", 5);
     needsUpdate |= bclass.addTextField("shortName", "City Short Name", 30);
     needsUpdate |= bclass.addTextField("name", "City Name", 30);
     needsUpdate |= bclass.addDateField("validFrom", "valid from date", "dd.MM.yyy", 0);
     needsUpdate |= bclass.addDateField("validUntil", "valid until date", "dd.MM.yyy", 0);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getCountryClassRef(String wikiName) {
     return new DocumentReference(wikiName, "CelementsPlaces", "CountryClass");
   }
 
   BaseClass getCountryClass() throws XWikiException {
     XWikiDocument doc;
     boolean needsUpdate = false;
     DocumentReference classRef = getCountryClassRef(getContext().getDatabase());
     try {
       doc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get country class document.", exp);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
 
     BaseClass bclass = doc.getXClass();
     bclass.setXClassReference(classRef);
     needsUpdate |= bclass.addTextField("name", "City Name", 30);
     needsUpdate |= bclass.addTextField("iso2", "iso country code (two letters)", 2);
     needsUpdate |= bclass.addTextField("iso3", "iso country code (three letters)", 3);
     needsUpdate |= bclass.addNumberField("isoNum", "iso country number code (three"
         + " digits)", 3, "integer");
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getGeotagClassRef(String wikiName) {
     return new DocumentReference(wikiName, "CelementsPlaces", "GeotagClass");
   }
 
   BaseClass getGeotagClass() throws XWikiException {
     XWikiDocument classDoc;
     DocumentReference classRef = getGeotagClassRef(getContext().getDatabase());
     boolean needsUpdate = false;
 
     try {
       classDoc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (Exception exception) {
       LOGGER.error("Exception while getting doc for ClassRef'" + classRef
           + "'", exception);
       classDoc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
 
     BaseClass bclass = classDoc.getXClass();
     bclass.setDocumentReference(classRef);
     needsUpdate |= bclass.addNumberField("longitude", "longitude", 15, "float");
     needsUpdate |= bclass.addNumberField("latitude", "latitude", 15, "float");
     needsUpdate |= bclass.addNumberField("altitude", "altitude", 15, "float");
     needsUpdate |= addTextField(bclass, "altitudeMode", "altitudeMode", 30,
         "/^.{0,128}$/", "celcrm_invalid_geotag_altitudeMode_max");
     needsUpdate |= addDateField(bclass, "validFrom", "Valid from date (dd.MM.yyyy)",
         "dd.MM.yyyy", 20, 0, getRegexDate(false, false),
         "celcrm_invalid_Geotag_validFrom");
     needsUpdate |= addDateField(bclass, "validUntil", "Valid until date (dd.MM.yyyy)",
         "dd.MM.yyyy", 20, 0, getRegexDate(false, false),
         "celcrm_invalid_geotag_validUntil");
 
     if (!"internal".equals(bclass.getCustomMapping())) {
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
 
     setContentAndSaveClassDocument(classDoc, needsUpdate);
     return bclass;
   }
 
   public String getConfigName() {
     return "celcrmPlaces";
   }
   
   @Override
   protected Log getLogger() {
     return LOGGER;
   }
 
   private String getRegexDate(boolean allowEmpty, boolean withTime) {
     String regex = "(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4})";
     if (withTime) {
       regex += " ([01][0-9]|2[0-4])(\\:[0-5][0-9]){2}";
     }
     return "/" + (allowEmpty ? "(^$)|" : "") + "^(" + regex + ")$" + "/";
   }
 
 }
