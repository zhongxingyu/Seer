 package com.mns.mojoinvest.server.pipeline.fund;
 
 import com.google.appengine.tools.pipeline.Job1;
 import com.google.appengine.tools.pipeline.Value;
 import com.googlecode.objectify.ObjectifyFactory;
 import com.googlecode.objectify.ObjectifyService;
 import com.mns.mojoinvest.server.engine.model.Fund;
 import com.mns.mojoinvest.server.engine.model.dao.FundDao;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 public class FundUpdaterJob extends Job1<String, List<Fund>> {
 
     private static final Logger log = Logger.getLogger(FundUpdaterJob.class.getName());
 
     @Override
     public Value<String> run(List<Fund> current) {
 
         //TODO: Figure out how to inject and serialize DAOs
         ObjectifyFactory factory = ObjectifyService.factory();
         FundDao dao = new FundDao(factory);
         dao.registerObjects(factory);
         //
 
         List<Fund> existing = dao.list();

        log.info("Existing " + existing.size() + " - " + existing);
        log.info("Current  " + current.size() + " - " + current);

         //Subtract set of current funds from existing to find inactive.
         existing.removeAll(current);
         for (Fund fund : existing) {
             fund.setActive(false);
         }
         log.info("Setting " + existing.size() + " funds as inactive: " + existing);
         dao.put(existing);
 
         log.info("Updating " + current.size() + " funds");
         dao.put(current);
 
         return immediate("Set " + existing.size() + " funds as inactive: " + existing + "\n" +
                 "Updated " + current.size() + " funds");
     }
 
 
 }
