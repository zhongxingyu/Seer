 package com.laurinka.skga.server.data;
 
 import com.laurinka.skga.server.model.Result;
 import com.laurinka.skga.server.model.Snapshot;
 import com.laurinka.skga.server.rest.CgfMemberResourceRESTService;
 import com.laurinka.skga.server.rest.SearchCgfResourceRESTService;
 import com.laurinka.skga.server.rest.SearchMemberResourceRESTService;
 import com.laurinka.skga.server.rest.SkgaMemberResourceRESTService;
 import com.laurinka.skga.server.rest.model.Hcp;
 import com.laurinka.skga.server.rest.model.NameNumberXml;
 import com.laurinka.skga.server.scratch.CgfGolferNumber;
 import com.laurinka.skga.server.scratch.SkgaGolferNumber;
 import com.laurinka.skga.server.services.WebsiteService;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.ws.rs.WebApplicationException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 @RequestScoped
 @Named("search")
 public class SearchProducer {
 
     static Pattern number = Pattern.compile("\\d{1,7}");
     @Inject
     private EntityManager em;
     @Inject
     private WebsiteService service;
     @Inject
     Logger log;
 
     @Inject
     SkgaMemberResourceRESTService skga;
 
     @Inject
     CgfMemberResourceRESTService cgf;
 
 
     private List<Snapshot> results;
 
     private String q;
     @Inject
     SearchCgfResourceRESTService cgfSearch;
     @Inject
     SearchMemberResourceRESTService skSearch;
 
     @Produces
     @Named
     public List<Snapshot> getResults() {
         return results;
     }
 
     @PostConstruct
     public void retrieveLastSnapshotsOrderedByName() {
         if (null == q || q.isEmpty()) {
             log.info("search string is empty!");
             results = Collections.emptyList();
             return;
         }
         if (number.matcher(q).matches()) {
             log.info("User entered number on page:" + q);
             //search by number
             Result detailSk = service.findDetail(new SkgaGolferNumber(q));
             Result detailCz = service.findDetail(new CgfGolferNumber(q));
 
             if (null == detailCz && null == detailSk) {
                 log.info("Nothing found.");
                 results = Collections.emptyList();
                 return;
             }
             results = new LinkedList<Snapshot>();
             if (detailSk != null) {
                 log.info("Found sk:" + detailSk.toString());
                 Snapshot sk = new Snapshot();
                 sk.setResult(detailSk);
                 results.add(sk);
             }
             if (detailCz != null) {
                 log.info("Found cz: " + detailCz.toString());
                 Snapshot cz = new Snapshot();
                 cz.setResult(detailCz);
                 results.add(cz);
             }
             return;
 
         } else {
             results = new LinkedList<Snapshot>();
             List<NameNumberXml> czs = cgfSearch.lookupMemberByName(q);
             for (NameNumberXml n : czs) {
                 log.info("Query cgf: " + n.getNumber());
                 Hcp hcp = null;
                 try {
                     hcp = cgf.lookupMemberById(n.getNumber());
                 } catch (WebApplicationException e) {
                     log.warning(e.getLocalizedMessage());
                     continue;
                 }
                 Snapshot s = new Snapshot();
                 Result result = Result.newCgf();
                 result.setClub(hcp.getClub());
                 result.setHcp(hcp.getHandicap());
                 result.setName(hcp.getName());
                 result.setSkgaNr(hcp.getNumber());
                 s.setResult(result);
                 results.add(s);
             }
 
             List<NameNumberXml> sks = skSearch.lookupMemberByName(q);
             for (NameNumberXml n : sks) {
                 log.info("Query skga: " + n.getNumber());
                 Hcp hcp = null;
                 try {
                     hcp = cgf.lookupMemberById(n.getNumber());
                 } catch (WebApplicationException e) {
                     log.warning(e.getLocalizedMessage());
                     continue;
                 }
                 Snapshot s = new Snapshot();
                 Result result = Result.newSkga();
                 result.setClub(hcp.getClub());
                 result.setHcp(hcp.getHandicap());
                 result.setName(hcp.getName());
                 result.setSkgaNr(hcp.getNumber());
                 s.setResult(result);
                 results.add(s);
             }
 
            // search by name
            results = Collections.emptyList();
        }
     }
 
     public String getQ() {
         return q;
     }
 
     public void setQ(String q) {
         this.q = q;
     }
 
     public void setService(WebsiteService service) {
         this.service = service;
     }
 }
