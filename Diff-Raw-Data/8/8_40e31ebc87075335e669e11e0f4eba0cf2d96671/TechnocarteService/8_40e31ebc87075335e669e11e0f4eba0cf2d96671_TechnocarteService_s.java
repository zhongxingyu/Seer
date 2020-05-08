 package fr.capwebct.capdemat.plugins.externalservices.technocarte.service;
 
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.log4j.Logger;
 import org.apache.soap.Fault;
 import org.apache.soap.rpc.Call;
 import org.apache.soap.rpc.Parameter;
 import org.apache.soap.rpc.Response;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import fr.capwebct.capdemat.Activite;
 import fr.capwebct.capdemat.ArretAller;
 import fr.capwebct.capdemat.CircuitAller;
 import fr.capwebct.capdemat.EcoleDerogType;
 import fr.capwebct.capdemat.EcoleSecteurType;
 import fr.capwebct.capdemat.ListeActiviteDocument;
 import fr.capwebct.capdemat.ListeEcoleDocument;
 import fr.cg95.cvq.business.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.payment.ExternalInvoiceItemDetail;
 import fr.cg95.cvq.business.payment.PurchaseItem;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.ExternalServiceBean;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.service.payment.IPaymentService;
 import fr.cg95.cvq.service.request.school.external.IScholarBusinessProviderService;
 import fr.cg95.cvq.service.users.external.IExternalHomeFolderService;
 
 public class TechnocarteService implements IExternalProviderService, IScholarBusinessProviderService {
     
     private static Logger logger = Logger.getLogger(TechnocarteService.class);
 
     private String label;
     private String urlkiosque;
 
     private IExternalHomeFolderService externalHomeFolderService;
 
     public String sendRequest(XmlObject requestXml) throws CvqException {
         String Method = "ReceptionCapdemat";
 
         HibernateUtil.commitTransaction();
         HibernateUtil.closeSession();
         
         Vector<Parameter> parameters = new Vector<Parameter>();
         String la = "La connexion au serveur a echoué";
         parameters.addElement(new Parameter("var", String.class, requestXml, null));
         parameters.addElement(new Parameter("code_appli", String.class, "Capdemat", null));
         try {
             Call call = new Call();
             String encodingStyleURI = org.apache.soap.Constants.NS_URI_SOAP_ENC;
             call.setEncodingStyleURI(encodingStyleURI);
             call.setTargetObjectURI("urn:WSPocketTechno2");
             call.setMethodName(Method);
             call.setParams(parameters);
             Response soap_response = call.invoke(new java.net.URL(urlkiosque), "");
             if (soap_response.generatedFault()) {
                 logger.error("The call failed: ");
             } else {
                 Parameter soap_result = soap_response.getReturnValue();
                 Object value = soap_result.getValue();
                 String s = getValue(value);
                 if (s.equals("0")) {
                     la = "La famille n existe pas";
                 } else if (s.equals("1")) {
                     la = "La modification est valide";
                 } else if (s.equals("2")) {
                     la = "La demande n'est pas pris en charge par Technocarte";
                 } else if (s.equals("3")) {
                     la = "La famille est créée";
                 } else if (s.equals("4")) {
                     la = "La famille et l'inscription sont créées";
                 } else if (s.equals("5")) {
                     la = "L'inscription est validée";
                 } else if (s.equals("6")) {
                     la = "Inscription Refusée";
                 } else if (s.equals("7")) {
                     la = "L'inscription est en attente de validation dans le BackOffice Technocarte";
                 } else if (s.equals("8")) {
                     la = "Service non connu par Technocarte";
                 } else {
                     la = "Code erreur inconnu";
                 }
                 TestRetourTechnocarte(s);
             }
         } catch (Exception e) {
             throw new CvqException(la);
         } finally {
             HibernateUtil.beginTransaction();
         }
         return null;
     }
 
     public void checkConfiguration(ExternalServiceBean externalServiceBean,
             String localAuthorityName) throws CvqConfigurationException {
         logger.debug("url kiosque = " + externalServiceBean.getProperty("urlkiosque"));
         setUrlKiosque((String) externalServiceBean.getProperty("urlkiosque"));
     }
 
     public void creditHomeFolderAccounts(Collection<PurchaseItem> purchaseItems, String cvqReference,
             String bankReference, Long homeFolderId, String externalHomeFolderId,
             String externalId, Date validationDate) throws CvqException {
         int total = 0;
         String Listfacture = "<listefacture>";
         for (Iterator<PurchaseItem> i = purchaseItems.iterator(); i.hasNext();) {
             ExternalAccountItem eai = (ExternalAccountItem) i.next();
             Listfacture += "<facture>";
             Listfacture += "<numfct>" + eai.getExternalItemId() + "</numfct>";
             Listfacture += "<montant>" + eai.getAmount().intValue() + "</montant>";
             Listfacture += "</facture>";
         }
         for (Iterator<PurchaseItem> i = purchaseItems.iterator(); i.hasNext();) {
             PurchaseItem purchaseItem = (PurchaseItem) i.next();
             total = total + purchaseItem.getAmount().intValue();
         }
         Listfacture += "</listefacture>";
         String Method = "PaiementFactureCap";
         Vector<Parameter> parameters = new Vector<Parameter>();
         parameters.addElement(new Parameter("listnumfct", String.class, Listfacture, null));
         parameters.addElement(new Parameter("montant", String.class, total, null));
         parameters.addElement(new Parameter("cvqReference", String.class, cvqReference, null));
         parameters.addElement(new Parameter("bankReference", String.class, bankReference, null));
         parameters.addElement(new Parameter("homeFolderId", String.class, externalHomeFolderId,
                 null));
         parameters.addElement(new Parameter("code_appli", String.class, "Capdemat", null));
         try {
             Call call = new Call();
             String encodingStyleURI = org.apache.soap.Constants.NS_URI_SOAP_ENC;
             call.setEncodingStyleURI(encodingStyleURI);
             call.setTargetObjectURI("urn:WSPocketTechno2");
             call.setMethodName(Method);
             call.setParams(parameters);
             Response soap_response = call.invoke(new java.net.URL(urlkiosque), "");
 
             if (soap_response.generatedFault()) {
                 Fault fault = soap_response.getFault();
                 logger.debug("The call failed: ");
                 logger.debug("Fault Code   = " + fault.getFaultCode());
                 logger.debug("Fault Entrie   = " + fault.getFaultEntries());
                 logger.debug("Fault String = " + fault.getFaultString());
                 logger.debug("Detail Entrie   = " + fault.getDetailEntries());
                 logger.debug("Fault Actor URI   = " + fault.getFaultActorURI());
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new CvqException();
         }
     }
 
     // Test Jn recupereation de plusieurs return Soap pour faciliter la
     // communiquation
     private static String getValue(Object o) {
         Class c = o.getClass();
         if (!c.isArray())
             return o.toString();
 
         String ret = null;
         Object[] oa = (Object[]) o;
         for (int i = 0; i < oa.length; i++) {
             if (ret == null)
                 ret = "[" + getValue(oa[i]);
             else
                 ret = ret + ", " + getValue(oa[i]);
         }
         return ret + "]";
     }
 
     public Map<String, List<ExternalAccountItem>> getAccountsByHomeFolder(Long homeFolderId,
             String externalHomeFolderId, String externalId) throws CvqException {
         String Method = "CallListFactureCap";
         Vector<Parameter> parameters = new Vector<Parameter>();
         parameters.addElement(new Parameter("homeFolderId", String.class, homeFolderId, null));
         parameters.addElement(new Parameter("externalHomeFolderId", String.class,
                 externalHomeFolderId, null));
         parameters.addElement(new Parameter("externalId", String.class, externalId, null));
         parameters.addElement(new Parameter("code_appli", String.class, "Capdemat", null));
         Map<String, List<ExternalAccountItem>> results = new HashMap<String, List<ExternalAccountItem>>();
         try {
             Call call = new Call();
             String encodingStyleURI = org.apache.soap.Constants.NS_URI_SOAP_ENC;
             call.setEncodingStyleURI(encodingStyleURI);
             call.setTargetObjectURI("urn:WSPocketTechno2");
             call.setMethodName(Method);
             call.setParams(parameters);
             Response soap_response = call.invoke(new java.net.URL(urlkiosque), "");
             if (soap_response.generatedFault()) {
                 Fault fault = soap_response.getFault();
                 logger.debug("The call failed: ");
                 logger.debug("Fault Code   = " + fault.getFaultCode());
                 logger.debug("Fault Entrie   = " + fault.getFaultEntries());
                 logger.debug("Fault String = " + fault.getFaultString());
                 logger.debug("Detail Entrie   = " + fault.getDetailEntries());
                 logger.debug("Fault Actor URI   = " + fault.getFaultActorURI());
             } else {
                 Parameter soap_result = soap_response.getReturnValue();
                 Object value = soap_result.getValue();
                 String Lfact = getValue(value);
                 // Debut du parser
                 if (!Lfact.isEmpty()) {
                     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                     DocumentBuilder db = dbf.newDocumentBuilder();
                     InputSource is = new InputSource(new StringReader(Lfact));
                     Document doc = db.parse(is);
                     NodeList nodes = doc.getElementsByTagName("facture");
                     List<ExternalAccountItem> bills = new ArrayList<ExternalAccountItem>();
 
                     // iterate des factures
                     for (int i = 0; i < nodes.getLength(); i++) {
                         Element element = (Element) nodes.item(i);
 
                         // Libelle de la facture
                         NodeList lib = element.getElementsByTagName("libelle");
                         Element line = (Element) lib.item(0);
                         String libelle = getCharacterDataFromElement(line);
 
                         // Numero de la facture
                         NodeList num = element.getElementsByTagName("numfct");
                         line = (Element) num.item(0);
                         String numfct = getCharacterDataFromElement(line);
 
                         // montant de la facture
                         NodeList how = element.getElementsByTagName("montant");
                         line = (Element) how.item(0);
                         Double montant = Double.parseDouble(getCharacterDataFromElement(line));
 
                         // date d'expiration (avant le lancement du
                         // recouvremenet)
                         NodeList exp = element.getElementsByTagName("expire");
                         line = (Element) exp.item(0);
                         String expire = getCharacterDataFromElement(line);
                         Calendar accountDate2 = Calendar.getInstance();
                         if (!expire.isEmpty()) {
                             String[] tokens = expire.split("/");
                             accountDate2.set(Integer.parseInt(tokens[2]), Integer
                                    .parseInt(tokens[1]), Integer.parseInt(tokens[0]));
                         } else {
                             accountDate2.set(0, 0, 0);
                         }
                         Date expirationDate = accountDate2.getTime();
 
                         // Date de calcul de la facture
                         NodeList cal = element.getElementsByTagName("calcul");
                         line = (Element) cal.item(0);
                         String calcul = getCharacterDataFromElement(line);
                         Calendar accountDate = Calendar.getInstance();
                         if (!calcul.isEmpty()) {
                             String[] tokens = calcul.split("/");
                             accountDate.set(Integer.parseInt(tokens[2]), Integer
                                    .parseInt(tokens[1]), Integer.parseInt(tokens[0]));
                         } else {
                             accountDate.set(0, 0, 0);
                         }
                         Date issueDate = accountDate.getTime();
 
                         // Date de Payment de la facture
                         NodeList pay = element.getElementsByTagName("payment");
                         line = (Element) pay.item(0);
                         String payment = getCharacterDataFromElement(line);
                         Calendar accountDate3 = Calendar.getInstance();
                         if (!payment.isEmpty()) {
                             String[] tokens = payment.split("/");
                             accountDate3.set(Integer.parseInt(tokens[2]), Integer
                                    .parseInt(tokens[1]), Integer.parseInt(tokens[0]));
                         } else {
                             accountDate3.set(0, 0, 0);
                         }
                         Date paymentDate = accountDate3.getTime();
 
                         NodeList ispay = element.getElementsByTagName("etat");
                         line = (Element) ispay.item(0);
                         String etat = getCharacterDataFromElement(line);
                         Boolean ispayed = Boolean.FALSE;
                         if (etat.equalsIgnoreCase("P")) {
                             logger.debug("C payer");
                             ispayed = true;
                         }
                         ExternalInvoiceItem eii = new ExternalInvoiceItem(libelle, montant,
                                 montant, getLabel(), numfct, issueDate, expirationDate,
                                 paymentDate, ispayed, null);
                         bills.add(eii);
                     }
                     results.put(IPaymentService.EXTERNAL_INVOICES, bills);
 
                 }
 
             }
 
         } catch (Exception e) {
             e.printStackTrace();
             throw new CvqException();
         }
         return results;
     }
 
     public static String getCharacterDataFromElement(Element e) {
         Node child = e.getFirstChild();
         if (child instanceof CharacterData) {
             CharacterData cd = (CharacterData) child;
             return cd.getData();
         }
         return "?";
     }
 
     public Map<Date, String> getConsumptions(Long key, Date dateFrom, Date dateTo)
             throws CvqException {
         logger.info("getConsumptionsByRequest() no action associated");
         return null;
     }
 
     public void loadDepositAccountDetails(ExternalDepositAccountItem edai) throws CvqException {
         logger.info("loadDepositAccountDetails() no action associated");
     }
 
     public void loadInvoiceDetails(ExternalInvoiceItem eii) throws CvqException {
         String Method = "CallListFactureDetailCap";
         Vector<Parameter> parameters = new Vector<Parameter>();
         parameters.addElement(new Parameter("externalId", String.class, eii.getExternalItemId(),
                 null));
         parameters.addElement(new Parameter("code_appli", String.class, "Capdemat", null));
         try {
             Call call = new Call();
             String encodingStyleURI = org.apache.soap.Constants.NS_URI_SOAP_ENC;
             call.setEncodingStyleURI(encodingStyleURI);
             call.setTargetObjectURI("urn:WSPocketTechno2");
             call.setMethodName(Method);
 
             call.setParams(parameters);
             Response soap_response = call.invoke(new java.net.URL(urlkiosque), "");
             logger.debug(soap_response);
             if (soap_response.generatedFault()) {
                 Fault fault = soap_response.getFault();
                 logger.debug("The call failed: ");
                 logger.debug("Fault Code   = " + fault.getFaultCode());
                 logger.debug("Fault Entrie   = " + fault.getFaultEntries());
                 logger.debug("Fault String = " + fault.getFaultString());
                 logger.debug("Detail Entrie   = " + fault.getDetailEntries());
                 logger.debug("Fault Actor URI   = " + fault.getFaultActorURI());
             } else {
                 Parameter soap_result = soap_response.getReturnValue();
                 Object value = soap_result.getValue();
                 String Lfact = getValue(value);
                 logger.debug("Lfact : " + Lfact);
                 if (eii.getInvoiceDetails() != null) {
                     eii.getInvoiceDetails().clear();
                 }
 
                 if (!Lfact.isEmpty()) {
                     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                     DocumentBuilder db = dbf.newDocumentBuilder();
                     InputSource is = new InputSource(new StringReader(Lfact));
                     Document doc = db.parse(is);
                     NodeList nodes = doc.getElementsByTagName("lignefacture");
                     for (int i = 0; i < nodes.getLength(); i++) {
                         ExternalInvoiceItemDetail eiiDetail = new ExternalInvoiceItemDetail();
 
                         Element element = (Element) nodes.item(i);
 
                         // Libelle de la ligne de facture
                         NodeList lib = element.getElementsByTagName("libelle");
                         Element line = (Element) lib.item(0);
                         String libelle = getCharacterDataFromElement(line);
                         eiiDetail.setLabel(libelle);
 
                         NodeList name = element.getElementsByTagName("nom");
                         line = (Element) name.item(0);
                         libelle = getCharacterDataFromElement(line);
                         eiiDetail.setSubjectName(libelle);
 
                         NodeList prenom = element.getElementsByTagName("prenom");
                         line = (Element) prenom.item(0);
                         libelle = getCharacterDataFromElement(line);
                         eiiDetail.setSubjectSurname(libelle);
 
                         NodeList val = element.getElementsByTagName("value");
                         line = (Element) val.item(0);
                         libelle = getCharacterDataFromElement(line);
                         eiiDetail.setValue(Integer.valueOf(libelle));
 
                         NodeList qte = element.getElementsByTagName("qte");
                         line = (Element) qte.item(0);
                         libelle = getCharacterDataFromElement(line);
                         eiiDetail.setQuantity(new BigDecimal(libelle));
 
                         NodeList unit = element.getElementsByTagName("unit");
                         line = (Element) unit.item(0);
                         libelle = getCharacterDataFromElement(line);
                         eiiDetail.setUnitPrice(Integer.valueOf(libelle));
 
                         eii.addInvoiceDetail(eiiDetail);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new CvqException();
         }
     }
 
     public String helloWorld() throws CvqException {
         return null;
     }
 
     public String getLabel() {
         if (label != "" || label != null) {
             return label;
         } else {
             return "Technocarte";
         }
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public boolean supportsConsumptions() {
         return false;
     }
 
     public boolean handlesTraces() {
         return false;
     }
 
     public List<String> checkExternalReferential(final XmlObject requestXml) {
         return new ArrayList<String>(0);
     }
 
     public Map<String, Object> loadExternalInformations(XmlObject requestXml) throws CvqException {
         return Collections.emptyMap();
     }
 
     public void setUrlKiosque(String url) {
         this.urlkiosque = url;
     }
 
     public String getUrlKiosque() {
         return this.urlkiosque;
     }
 
     public void TestRetourTechnocarte(String s) throws CvqException {
         if (s.equals("0")) {
             throw new CvqException("");
         } else if (s.equals("6")) {
             throw new CvqException("");
         } else if (s.equals("8")) {
             throw new CvqException("");
         } else if (s.equals("1") || s.equals("2") || s.equals("3") || s.equals("4")
                 || s.equals("5") || s.equals("7")) {
             // nothing;
         } else {
             throw new CvqException("");
         }
     }
 
 
     @Override
     public Map<String, Map<String, String>> getSchools(Child child) {
         String method = "ListeEcoles";
         Map<String, Map<String,String>> schools = new HashMap<String, Map<String,String>>();
 
         // get child info
         String externalChildId = "";
         if (externalHomeFolderService.getIndividualMapping(child, label) != null)
             externalChildId = externalHomeFolderService.getIndividualMapping(child, label).getExternalCapDematId();
         logger.debug("Get externalCapDematId for " + child.getFullName() + " - " + externalChildId);
 
         Vector<Parameter> parameters = new Vector<Parameter>();
         parameters.addElement(new Parameter("idenfantexterne", String.class, externalChildId, null));
         parameters.addElement(new Parameter("nrue", String.class, child.getAddress().getStreetNumber(), null));
         parameters.addElement(new Parameter("rivoli", String.class, child.getAddress().getStreetRivoliCode(), null));
         parameters.addElement(new Parameter("cp", String.class, child.getAddress().getPostalCode(), null));
         parameters.addElement(new Parameter("codeappli", String.class, "Capdemat", null));
         try {
             Call call = new Call();
             String encodingStyleURI = org.apache.soap.Constants.NS_URI_SOAP_ENC;
             call.setEncodingStyleURI(encodingStyleURI);
             call.setTargetObjectURI ("urn:WSPocketTechno2" );
             call.setMethodName(method);
             call.setParams(parameters);
             Response soap_response = call.invoke(new java.net.URL(urlkiosque), "" );
 
             if (soap_response.generatedFault()) {
                 Fault fault = soap_response.getFault ();
                 logger.error("Error : " + fault.getFaultString());
                 return schools;
             } else {
                 Parameter soap_result = soap_response.getReturnValue();
                 // add a header to xml return 'xmlns="http://www.capwebct.fr/capdemat"'
                 String xmlresult = soap_result.getValue().toString()
                     .replaceFirst("<ListeEcole>", "<ListeEcole xmlns=\"http://www.capwebct.fr/capdemat\">");
                 logger.debug("Get soap response for child " + externalChildId + " and rivoli code " + child.getAddress().getStreetRivoliCode());
                 // start treatment
                 logger.debug("Parse : " + xmlresult);
                 ListeEcoleDocument document = ListeEcoleDocument.Factory.parse(xmlresult);
                 Map<String, String> schoolSectors = new HashMap<String, String>();
                 Map<String, String> schoolDerogs = new HashMap<String, String>();
                 if (document.getListeEcole().getListeEcoleSecteur() != null)
                     for (EcoleSecteurType est : document.getListeEcole().getListeEcoleSecteur().getEcoleSecteurArray()) {
                         schoolSectors.put(est.getIdEcoleSecteur(), est.getNomEcoleSecteur());
                     }
                 if (document.getListeEcole().getListeEcoleDerog() != null)
                     for (EcoleDerogType edt : document.getListeEcole().getListeEcoleDerog().getEcoleDerogArray()) {
                         schoolDerogs.put(edt.getIdEcoleDerog(), edt.getNomEcoleDerog());
                     }
                 schools.put("schoolSectors", schoolSectors);
                 schools.put("schoolDerogs", schoolDerogs);
                 return schools;
             }
         } catch (XmlException e) {
             logger.error("XmlException : " + e.getMessage() + " : ");
             e.printStackTrace();
         } catch (Exception e) {
             logger.error("Exception : " + e.getMessage());
             e.printStackTrace();
         }
         return schools;
     }
 
     private ListeActiviteDocument getActivities(String labelActivity, Request request, Child child) throws CvqObjectNotFoundException {
         String method = "DetailActivites";
         ListeActiviteDocument activities = null;
 
         String externalChildId = "";
         if (externalHomeFolderService.getIndividualMapping(child, label) != null)
             externalChildId = externalHomeFolderService.getIndividualMapping(child, label).getExternalCapDematId();
         logger.debug("Get externalCapDematId for " + child.getFullName() + " - " + externalChildId);
         String birthDayDate = "";
         if (child.getBirthDate() != null) {
             DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
             logger.debug("getActivities() sending : " + df.format(child.getBirthDate()));
             birthDayDate = df.format(child.getBirthDate());
         }
         String requestSeasonId= "";
         if (request.getRequestSeason() != null) {
             requestSeasonId = request.getRequestSeason().getId().toString();
         }
 
         Vector<Parameter> parameters = new Vector<Parameter>();
         parameters.addElement(new Parameter("idenfantexterne", String.class, externalChildId, null));
         parameters.addElement(new Parameter("typeactivite", String.class, labelActivity, null));
         parameters.addElement(new Parameter("codeappli", String.class, "Capdemat", null));
         parameters.addElement(new Parameter("datenai", String.class, birthDayDate, null));
         parameters.addElement(new Parameter("requestType", String.class, request.getRequestType().getLabel(), null));
         parameters.addElement(new Parameter("codsaison", String.class, requestSeasonId, null));
 
         try {
             Call call = new Call();
             String encodingStyleURI = org.apache.soap.Constants.NS_URI_SOAP_ENC;
             call.setEncodingStyleURI(encodingStyleURI);
             call.setTargetObjectURI ("urn:WSPocketTechno2" );
             call.setMethodName(method);
             call.setParams(parameters);
             Response soap_response = call.invoke(new java.net.URL(urlkiosque), "" );
 
             if (soap_response.generatedFault()) {
                 Fault fault = soap_response.getFault ();
                 logger.error("Error : " + fault.getFaultString());
                 return activities;
             } else {
                 Parameter soap_result = soap_response.getReturnValue();
                 // add a header to xml return 'xmlns="http://www.capwebct.fr/capdemat"'
                 String xmlresult = soap_result.getValue().toString()
                     .replaceFirst("<ListeActivite>", "<ListeActivite xmlns=\"http://www.capwebct.fr/capdemat\">");
                 logger.debug("Get soap response for child " + externalChildId);
                 // start treatment
                 logger.debug("Parse : " + xmlresult);
                 
                 activities = ListeActiviteDocument.Factory.parse(xmlresult);
             }
         } catch (XmlException e) {
             logger.error("XmlException : " + e.getMessage() + " : ");
             e.printStackTrace();
         } catch (Exception e) {
             logger.error("Exception : " + e.getMessage());
             e.printStackTrace();
         }
         return activities;
     }
 
     @Override
     public Map<String, String> getHolidayCamps(Request request, Child child) {
 
         Map<String, String> camps = new HashMap<String, String>();
         ListeActiviteDocument document = null;
         try {
             document = getActivities("SEJ", request, child);
         } catch (CvqObjectNotFoundException e) {
             e.printStackTrace();
             return camps;
         }
         if (document.getListeActivite() != null)
             for (Activite camp : document.getListeActivite().getActiviteArray()) {
                 camps.put(camp.getIdActivite(), camp.getNomActivite());
             }
 
         return camps;
     }
 
     @Override
     public Map<String, String> getLeisureCenters(Request request, Child child) {
         Map<String, String> centers = new HashMap<String, String>();
         ListeActiviteDocument activities;
         try {
             activities = getActivities("LOI", request, child);
         } catch (CvqObjectNotFoundException e) {
             e.printStackTrace();
             return centers;
         }
         for (Activite center : activities.getListeActivite().getActiviteArray()) {
             centers.put(center.getIdActivite(), center.getNomActivite());
         }
         return centers;
     }
 
     @Override
     public Map<String, String> getLeisureCenterTransportLines(Request request, Child child) {
         Map<String, String> lines = new HashMap<String, String>();
 
         ListeActiviteDocument activities;
         try {
             activities = getActivities("LOI", request, child);
         } catch (Exception e) {
             logger.warn(e.getMessage());
             return lines;
         }
         for (Activite center : activities.getListeActivite().getActiviteArray()) {
             if (center.getCircuitSpecifique()) {
                 for (CircuitAller line : center.getListeCircuitAller().getCircuitAllerArray()) {
                     lines.put(line.getIdCircuitAller(), line.getNomCircuitAller());
                 }
             }
         }
 
         return lines;
     }
 
     @Override
     public Map<String, String> getLeisureCenterTransportStops(Request request, Child child, String lineId) {
         Map<String, String> stops = new HashMap<String, String>();
         ListeActiviteDocument activities;
         try {
             activities = getActivities("LOI", request, child);
         } catch (Exception e) {
             logger.warn(e.getMessage());
             return stops;
         }
         for (Activite center : activities.getListeActivite().getActiviteArray()) {
             if (center.getCircuitSpecifique()) {
                 for (CircuitAller line : center.getListeCircuitAller().getCircuitAllerArray()) {
                     if (line.getIdCircuitAller().equals(lineId)) {
                         for (ArretAller stop : line.getListeArretAller().getArretAllerArray()) {
                             stops.put(stop.getIdArretAller(), stop.getNomArretAller());
                         }
                         break;
                     }
                 }
             }
         }
 
         return stops;
     }
 
     @Override
     public Map<String, String> getTransportLines(Request request, Child child) {
         Map<String, String> lines = new HashMap<String, String>();
 
         ListeActiviteDocument activities;
         try {
             activities = getActivities("TRANS", request, child);
         } catch (Exception e) {
             logger.warn(e.getMessage());
             return lines;
         }
         for (Activite activity : activities.getListeActivite().getActiviteArray()) {
             if (activity.getTypeActivite().equals("TRANS")) {
                 for (CircuitAller line : activity.getListeCircuitAller().getCircuitAllerArray()) {
                     lines.put(line.getIdCircuitAller(), line.getNomCircuitAller());
                 }
                 break; // There should be only one transport activity
             }
         }
 
         return lines;
     }
 
     @Override
     public Map<String, String> getTransportStops(Request request, Child child, String lineId) {
         Map<String, String> stops = new HashMap<String, String>();
         ListeActiviteDocument activities;
         try {
             activities = getActivities("TRANS", request, child);
         } catch (Exception e) {
             logger.warn(e.getMessage());
             return stops;
         }
         for (Activite activity : activities.getListeActivite().getActiviteArray()) {
             if (activity.getTypeActivite().equals("TRANS")) {
                 for (CircuitAller line : activity.getListeCircuitAller().getCircuitAllerArray()) {
                     if (line.getIdCircuitAller().equals(lineId)) {
                         for (ArretAller stop : line.getListeArretAller().getArretAllerArray()) {
                             stops.put(stop.getIdArretAller(), stop.getNomArretAller());
                         }
                         break;
                     }
                 }
                 break; // There should be only one transport activity
             }
         }
 
         return stops;
     }
 
     public void setExternalHomeFolderService(IExternalHomeFolderService externalHomeFolderService) {
         this.externalHomeFolderService = externalHomeFolderService;
     }
 }
