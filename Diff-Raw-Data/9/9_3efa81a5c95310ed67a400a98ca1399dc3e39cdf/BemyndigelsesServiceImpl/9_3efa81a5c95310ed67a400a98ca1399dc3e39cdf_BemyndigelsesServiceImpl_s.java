 package dk.bemyndigelsesregister.bemyndigelsesservice.web;
 
 import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
 import com.trifork.dgws.CareProviderIdType;
 import com.trifork.dgws.DgwsRequestContext;
 import com.trifork.dgws.IdCardData;
 import com.trifork.dgws.IdCardSystemLog;
 import com.trifork.dgws.IdCardType;
 import com.trifork.dgws.IdCardUserLog;
 import com.trifork.dgws.WhitelistChecker;
 import com.trifork.dgws.annotations.Protected;
 import dk.bemyndigelsesregister.bemyndigelsesservice.BemyndigelsesService;
 import dk.bemyndigelsesregister.bemyndigelsesservice.domain.*;
 import dk.bemyndigelsesregister.bemyndigelsesservice.domain.Bemyndigelse;
 import dk.bemyndigelsesregister.bemyndigelsesservice.server.BemyndigelseManager;
 import dk.bemyndigelsesregister.bemyndigelsesservice.server.dao.*;
 import dk.bemyndigelsesregister.shared.service.SystemService;
 import dk.nsi.bemyndigelse._2012._05._01.*;
 import org.apache.commons.collections15.CollectionUtils;
 import org.apache.commons.collections15.Transformer;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ws.server.endpoint.annotation.Endpoint;
 import org.springframework.ws.server.endpoint.annotation.RequestPayload;
 import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
 import org.springframework.ws.soap.SoapHeader;
 
 import javax.inject.Inject;
 import javax.xml.datatype.XMLGregorianCalendar;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import static java.util.Collections.singletonList;
 
 @Repository("bemyndigelsesService")
 @Endpoint
 public class BemyndigelsesServiceImpl implements BemyndigelsesService {
     private static Logger logger = Logger.getLogger(BemyndigelsesServiceImpl.class);
     @Inject
     SystemService systemService;
     @Inject
     BemyndigelseManager bemyndigelseManager;
     @Inject
     BemyndigelseDao bemyndigelseDao;
     @Inject
     DomaeneDao domaeneDao;
     @Inject
     LinkedSystemDao linkedSystemDao;
     @Inject
     ArbejdsfunktionDao arbejdsfunktionDao;
     @Inject
     RettighedDao rettighedDao;
     @Inject
     DelegerbarRettighedDao delegerbarRettighedDao;
     @Inject
     DgwsRequestContext dgwsRequestContext;
     @Inject
     ServiceTypeMapper typeMapper;
     
     @Inject WhitelistChecker whitelistChecker;
 
     public BemyndigelsesServiceImpl() {
     }
 
     @Override
     @Protected
     @Transactional
     @ResponsePayload
     public OpretAnmodningOmBemyndigelserResponse opretAnmodningOmBemyndigelser(
             @RequestPayload OpretAnmodningOmBemyndigelserRequest request, SoapHeader soapHeader) {
 
         Collection<Bemyndigelse> createdBemyndigelser = new ArrayList<Bemyndigelse>();
 
         for (OpretAnmodningOmBemyndigelserRequest.Anmodning anmodning : request.getAnmodning()) {
         	authorizeOperationForCpr("opretAnmodningOmBemyndigelser", "IDCard CPR was different from BemyndigedeCpr", anmodning.getBemyndigedeCpr());
             logger.debug("Creating Bemyndigelse for anmodning=" + anmodning.toString());
             final Bemyndigelse bemyndigelse = bemyndigelseManager.opretAnmodningOmBemyndigelse(
                     anmodning.getSystem(),
                     anmodning.getBemyndigendeCpr(),
                     anmodning.getBemyndigedeCpr(),
                     anmodning.getBemyndigedeCvr(),
                     anmodning.getArbejdsfunktion(),
                     anmodning.getRettighed(),
                     anmodning.getSystem(),
                     nullableDateTime(anmodning.getGyldigFra()),
                     nullableDateTime(anmodning.getGyldigTil()));
             logger.debug("Got bemyndigelse with kode=" + bemyndigelse.getKode());
             createdBemyndigelser.add(bemyndigelse);
         }
 
         final OpretAnmodningOmBemyndigelserResponse response = new OpretAnmodningOmBemyndigelserResponse();
         for (Bemyndigelse bemyndigelse : createdBemyndigelser) {
             response.getBemyndigelse().add(toJaxbType(bemyndigelse));
         }
         return response;
     }
 
     @Override
     @Protected
     @Transactional
     @ResponsePayload
     public GodkendBemyndigelseResponse godkendBemyndigelse(
             @RequestPayload GodkendBemyndigelseRequest request, SoapHeader soapHeader) {
         final List<String> bemyndigelsesKoder = request.getBemyndigelsesKode();
 
         Collection<Bemyndigelse> bemyndigelser = bemyndigelseManager.godkendBemyndigelser(bemyndigelsesKoder);
 
         for (Bemyndigelse bemyndigelse : bemyndigelser) {
         	authorizeOperationForCpr("godkendBemyndigelse", "IDCard CPR var forskelligt fra BemyndigendeCPR på bemyndigelse med koden " + bemyndigelse.getKode(), bemyndigelse.getBemyndigendeCpr());
         }
 
         final GodkendBemyndigelseResponse response = new GodkendBemyndigelseResponse();
         response.getBemyndigelser().addAll(CollectionUtils.collect(
                 bemyndigelser,
                 new Transformer<Bemyndigelse, dk.nsi.bemyndigelse._2012._05._01.Bemyndigelse>() {
                     @Override
                     public dk.nsi.bemyndigelse._2012._05._01.Bemyndigelse transform(Bemyndigelse bemyndigelse) {
                         return toJaxbType(bemyndigelse);
                     }
                 }
         ));
         return response;
     }
 
     @Override
     @Protected
     @ResponsePayload
     public HentBemyndigelserResponse hentBemyndigelser(
             @RequestPayload HentBemyndigelserRequest request, SoapHeader soapHeader) {
         List<Bemyndigelse> foundBemyndigelser = Collections.emptyList();
         if (request.getKode() != null) {
             final Bemyndigelse bemyndigelse = bemyndigelseDao.findByKode(request.getKode());
             foundBemyndigelser = singletonList(bemyndigelse);
             authorizeOperationForCpr("hentBemyndigelser", "IDCard CPR was not found in bemyndigelse", bemyndigelse.getBemyndigendeCpr(), bemyndigelse.getBemyndigedeCpr());
         }
         else if (request.getBemyndigendeCpr() != null) {
             authorizeOperationForCpr("hentBemyndigelser", "IDCard CPR was not equal to BemyndigendeCpr", request.getBemyndigendeCpr());
             foundBemyndigelser = bemyndigelseDao.findByBemyndigendeCpr(request.getBemyndigendeCpr());
         }
         else if (request.getBemyndigedeCpr() != null) {
             authorizeOperationForCpr("hentBemyndigelser", "IDCard CPR was not equal to BemyndigedeCpr", request.getBemyndigedeCpr());
             foundBemyndigelser = bemyndigelseDao.findByBemyndigedeCpr(request.getBemyndigedeCpr());
         }
 
         final Collection<Bemyndigelse> finalFoundBemyndigelser = foundBemyndigelser;
 
         return new HentBemyndigelserResponse() {{
             getBemyndigelse().addAll(CollectionUtils.collect(
                     finalFoundBemyndigelser,
                     new Transformer<Bemyndigelse, dk.nsi.bemyndigelse._2012._05._01.Bemyndigelse>() {
                         @Override
                         public dk.nsi.bemyndigelse._2012._05._01.Bemyndigelse transform(final Bemyndigelse bem) {
                             return toJaxbType(bem);
                         }
                     }
             ));
         }};
     }
     
     void authorizeOperationForCpr(String whitelist, String errorMessage, String... authorizedCprs) {
     	Set<String> authorizedCprSet = new HashSet<String>(Arrays.asList(authorizedCprs));
     	IdCardData idCardData = dgwsRequestContext.getIdCardData();
     	if(idCardData.getIdCardType() == IdCardType.SYSTEM) {
     		IdCardSystemLog systemLog = dgwsRequestContext.getIdCardSystemLog();
     		if(systemLog.getCareProviderIdType() != CareProviderIdType.CVR_NUMBER) {
     	        throw new IllegalAccessError("Attempted to access operation using system id card, but the CareProviderIdType was not CVR, it was " + systemLog.getCareProviderIdType());
     		}
     		String cvr = systemLog.getCareProviderId();
 			if(!whitelistChecker.isSystemWhitelisted(whitelist, cvr)) {
     	        throw new IllegalAccessError("Attempted to access operation using system id card, but the whitelist " + whitelist + " did not contain id card CVR " + cvr);
     		}
 			return;
     	}
     	else if (idCardData.getIdCardType() == IdCardType.USER) {
         	IdCardUserLog userLog = dgwsRequestContext.getIdCardUserLog();
         	if(userLog != null && authorizedCprSet.contains(userLog.cpr)) {
         		return;
         	}
         	else {
         		logger.info("Failed to authorize user id card. Authorized CPRs: " + authorizedCprSet + ". CPR in ID card: " +userLog.cpr);
                 throw new IllegalAccessError(errorMessage);
         	}
     	}
     	else {
     		throw new IllegalAccessError("Could not authorize ID card, it was neither a user or system id card");
     	}
     }
 
     public static dk.nsi.bemyndigelse._2012._05._01.Bemyndigelse toJaxbType(final Bemyndigelse bem) {
         return new dk.nsi.bemyndigelse._2012._05._01.Bemyndigelse() {{
             setKode(bem.getKode());
             setBemyndigendeCpr(bem.getBemyndigendeCpr());
             setBemyndigedeCpr(bem.getBemyndigedeCpr());
             setBemyndigedeCvr(bem.getBemyndigedeCvr());
             setSystem(bem.getLinkedSystem().getKode());
             setArbejdsfunktion(bem.getArbejdsfunktion().getKode());
             setRettighed(bem.getRettighed().getKode());
             setStatus(bem.getStatus() == Status.GODKENDT ? "Godkendt" : "Bestilt");
             if (bem.getGodkendelsesdato() != null) {
                 setGodkendelsesdato(new XMLGregorianCalendarImpl(bem.getGodkendelsesdato().withZone(DateTimeZone.UTC).toGregorianCalendar()));
             }
             setGyldigFra(new XMLGregorianCalendarImpl(bem.getGyldigFra().withZone(DateTimeZone.UTC).toGregorianCalendar()));
             setGyldigTil(new XMLGregorianCalendarImpl(bem.getGyldigTil().withZone(DateTimeZone.UTC).toGregorianCalendar()));
         }};
     }
 
     @Override
     @Protected
     @Transactional
     @ResponsePayload
     public OpretGodkendteBemyndigelserResponse opretGodkendtBemyndigelse(
             @RequestPayload final OpretGodkendteBemyndigelserRequest request, SoapHeader soapHeader) {
         Collection<Bemyndigelse> bemyndigelser = new ArrayList<Bemyndigelse>();
 
         for (final OpretGodkendteBemyndigelserRequest.Bemyndigelse bemyndigelseRequest : request.getBemyndigelse()) {
             authorizeOperationForCpr("opretGodkendtBemyndigelse", "IDCard CPR was different from BemyndigendeCpr", bemyndigelseRequest.getBemyndigendeCpr());
             final Bemyndigelse bemyndigelse = bemyndigelseManager.opretGodkendtBemyndigelse(
                     bemyndigelseRequest.getSystem(),
                     bemyndigelseRequest.getBemyndigendeCpr(),
                     bemyndigelseRequest.getBemyndigedeCpr(),
                     bemyndigelseRequest.getBemyndigedeCvr(),
                     bemyndigelseRequest.getArbejdsfunktion(),
                     bemyndigelseRequest.getRettighed(),
                     bemyndigelseRequest.getSystem(),
                     nullableDateTime(bemyndigelseRequest.getGyldigFra()),
                     nullableDateTime(bemyndigelseRequest.getGyldigTil())
             );
             bemyndigelser.add(bemyndigelse);
         }
 
         final OpretGodkendteBemyndigelserResponse response = new OpretGodkendteBemyndigelserResponse();
         response.getBemyndigelse().addAll(CollectionUtils.collect(
                 bemyndigelser,
                 new Transformer<Bemyndigelse, dk.nsi.bemyndigelse._2012._05._01.Bemyndigelse>() {
                     @Override
                     public dk.nsi.bemyndigelse._2012._05._01.Bemyndigelse transform(Bemyndigelse bemyndigelse) {
                         return toJaxbType(bemyndigelse);
                     }
                 }
         ));
         return response;
     }
 
     @Override
     @Protected
     @Transactional
     @ResponsePayload
     public SletBemyndigelserResponse sletBemyndigelser(
             @RequestPayload SletBemyndigelserRequest request, SoapHeader soapHeader) {
 
         DateTime now = systemService.getDateTime();
 
         SletBemyndigelserResponse response = new SletBemyndigelserResponse();
 
         List<Bemyndigelse> bemyndigelser = bemyndigelseDao.findByKoder(request.getKode());
         for (Bemyndigelse bemyndigelse : bemyndigelser) {
             authorizeOperationForCpr("sletBemyndigelser", "IDCard CPR var forskelligt fra BemyndigendeCPR på bemyndigelse med koden " + bemyndigelse.getKode(), bemyndigelse.getBemyndigendeCpr());
 
             DateTime validTo = bemyndigelse.getGyldigTil();
             if (validTo.isAfter(now)) {
                 logger.info("Deleting bemyndigelse with id=" + bemyndigelse.getId() + " and kode=" + bemyndigelse.getKode());
                 bemyndigelse.setGyldigTil(now);
                 bemyndigelseDao.save(bemyndigelse);
                 response.getKode().add(bemyndigelse.getKode());
             }
             else {
                 logger.info("Bemyndigelse with id=" + bemyndigelse.getId() + " and kode=" + bemyndigelse.getKode() + " was already deleted");
             }
 
         }
 
         return response;
     }
 
     private DateTime nullableDateTime(XMLGregorianCalendar xmlDate) {
         return xmlDate != null ? new DateTime(xmlDate.toGregorianCalendar(), DateTimeZone.UTC) : null;
     }
 
     @Override
     @Transactional
     @ResponsePayload
     @Protected(whitelist = "bemyndigelsesservice.indlaesMetadata")
     public IndlaesMetadataResponse indlaesMetadata(@RequestPayload IndlaesMetadataRequest request, SoapHeader soapHeader) {
         if (request.getArbejdsfunktioner() != null) {
             for (final Arbejdsfunktioner.Arbejdsfunktion jaxbArbejdsfunktion : request.getArbejdsfunktioner().getArbejdsfunktion()) {
                 Domaene domaene = domaeneDao.findByKode(jaxbArbejdsfunktion.getDomaene());
                 LinkedSystem linkedSystem = linkedSystemDao.findByKode(jaxbArbejdsfunktion.getSystem());
                 Arbejdsfunktion arbejdsfunktion = arbejdsfunktionDao.findByKode(linkedSystem, jaxbArbejdsfunktion.getArbejdsfunktion());
                 if(arbejdsfunktion != null) {
                     logger.info("Updating arbejdsfunktion=" + jaxbArbejdsfunktion);
                 } else {
                     logger.info("Adding arbejdsfunktion=" + jaxbArbejdsfunktion);
                     arbejdsfunktion = new Arbejdsfunktion();
                 }
                 arbejdsfunktion.setKode(jaxbArbejdsfunktion.getArbejdsfunktion());
                 arbejdsfunktion.setBeskrivelse(jaxbArbejdsfunktion.getBeskrivelse());
                 arbejdsfunktion.setLinkedSystem(linkedSystem);
                 
                 arbejdsfunktionDao.save(arbejdsfunktion);
             }
         }
 
         if (request.getRettigheder() != null) {
             for (final Rettigheder.Rettighed jaxbRettighed : request.getRettigheder().getRettighed()) {
                 LinkedSystem linkedSystem = linkedSystemDao.findByKode(jaxbRettighed.getSystem());
 
                 Rettighed rettighed = rettighedDao.findByKode(linkedSystem, jaxbRettighed.getRettighed());
                 if(rettighed != null) {
                     logger.info("Updating rettighed=" + jaxbRettighed);
                 } else {
                     logger.info("Adding rettighed=" + jaxbRettighed);
                     rettighed = new Rettighed();
                 }
                 rettighed.setKode(jaxbRettighed.getRettighed());
                 rettighed.setBeskrivelse(jaxbRettighed.getBeskrivelse());
                 rettighed.setLinkedSystem(linkedSystem);
                 
                 rettighedDao.save(rettighed);
             }
         }
 
         if (request.getDelegerbarRettigheder() != null) {
             for (final DelegerbarRettigheder.DelegerbarRettighed jaxbDelegerbarRettighed : request.getDelegerbarRettigheder().getDelegerbarRettighed()) {
                 LinkedSystem linkedSystem = linkedSystemDao.findByKode(jaxbDelegerbarRettighed.getSystem());
                 logger.info("Adding delegerbarRettighed=" + jaxbDelegerbarRettighed.toString());
                 DelegerbarRettighed delegerbarRettighed = new DelegerbarRettighed();
                 delegerbarRettighed.setArbejdsfunktion(arbejdsfunktionDao.findByKode(linkedSystem, jaxbDelegerbarRettighed.getArbejdsfunktion()));
                 delegerbarRettighed.setRettighedskode(rettighedDao.findByKode(linkedSystem, jaxbDelegerbarRettighed.getRettighed()));
                 delegerbarRettighedDao.save(delegerbarRettighed);
             }
         }
 
         return new IndlaesMetadataResponse();
     }
 
     @Override
     @ResponsePayload
     // TODO Verify with Kjeld that there really is no reason 
     // for hentMetadata to be protected.
     //@Protected(whitelist = "bemyndigelsesservice.hentMetadata")
     public HentMetadataResponse hentMetadata(@RequestPayload HentMetadataRequest request, SoapHeader soapHeader) {
         LinkedSystem linkedSystem = linkedSystemDao.findByKode(request.getSystem());
 
         final List<Arbejdsfunktion> foundArbejdsfunktioner = arbejdsfunktionDao.findBy(linkedSystem);
         final List<Rettighed> foundRettigheder = rettighedDao.findBy(linkedSystem);
         final List<DelegerbarRettighed> foundDelegerbarRettigheder = delegerbarRettighedDao.findBy(linkedSystem);
 
         return new HentMetadataResponse() {{
             setArbejdsfunktioner(typeMapper.toJaxbArbejdsfunktioner(foundArbejdsfunktioner));
             setRettigheder(typeMapper.toJaxbRettigheder(foundRettigheder));
             setDelegerbarRettigheder(typeMapper.toJaxbDelegerbarRettigheder(foundDelegerbarRettigheder));
         }};
     }
 }
