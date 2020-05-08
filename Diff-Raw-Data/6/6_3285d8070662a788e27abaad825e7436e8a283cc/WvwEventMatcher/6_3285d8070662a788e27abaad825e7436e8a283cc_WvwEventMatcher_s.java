 /*
  * gw2live - GuildWars 2 Dynamic Map
  * 
  * Website: http://gw2map.com
  *
  * Copyright 2013   zyclonite    networx
  *                  http://zyclonite.net
  * Developer: Lukas Prettenthaler
  */
 package net.zyclonite.gw2live.threads;
 
 import com.hazelcast.core.IMap;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.Callable;
 import net.zyclonite.gw2live.model.GuildDetails;
 import net.zyclonite.gw2live.model.WvwEvent;
 import net.zyclonite.gw2live.model.WvwMap;
 import net.zyclonite.gw2live.model.WvwMatch;
 import net.zyclonite.gw2live.model.WvwMatchDetails;
 import net.zyclonite.gw2live.model.WvwObjective;
 import net.zyclonite.gw2live.model.WvwScore;
 import net.zyclonite.gw2live.service.EsperEngine;
 import net.zyclonite.gw2live.service.Gw2Client;
 import net.zyclonite.gw2live.service.HazelcastCache;
 import net.zyclonite.gw2live.service.MongoDB;
 
 /**
  *
  * @author zyclonite
  */
 public class WvwEventMatcher implements Callable<Boolean> {
 
     private final IMap<Integer, WvwEvent> wvwEventCache;
     private final Gw2Client client;
     private final MongoDB db;
     private final EsperEngine esper;
     private final Date timestamp;
 
     public WvwEventMatcher() {
         final HazelcastCache hz = HazelcastCache.getInstance();
         wvwEventCache = hz.getWvwCacheMap();
         client = Gw2Client.getInstance();
         db = MongoDB.getInstance();
         esper = EsperEngine.getInstance();
         timestamp = new Date();
     }
 
     private Boolean getWvwEvents() {
         final List<WvwEvent> wvwevents = new ArrayList<>();
         final List<WvwMatch> wvwmatches = db.findWvwMatches();
         for (final WvwMatch match : wvwmatches) {
             final List<GuildDetails> guildDetails = new ArrayList<>();
             final List<WvwScore> scores = new ArrayList<>();
             final WvwMatchDetails matchDetails = client.getWvwMatchDetails(match.getWvw_match_id());
             if(matchDetails.getMatch_id() == null) {
                 return false;
             }
             final WvwScore score = new WvwScore();
             score.setMatch_id(matchDetails.getMatch_id());
             score.setMap_type("Total");
             score.setScores(matchDetails.getScores());
             scores.add(score);
             for (final WvwMap map : matchDetails.getMaps()) {
                 final WvwScore mapscore = new WvwScore();
                 mapscore.setMatch_id(matchDetails.getMatch_id());
                 mapscore.setMap_type(map.getType());
                 mapscore.setScores(map.getScores());
                 scores.add(mapscore);
                 for (final WvwObjective objective : map.getObjectives()) {
                     final WvwEvent wvwevent = new WvwEvent();
                     wvwevent.setObjective_id(objective.getId());
                     wvwevent.setOwner(objective.getOwner());
                     wvwevent.setOwner_guild(objective.getOwner_guild());
                     wvwevent.setMap_type(map.getType());
                     wvwevent.setMap_scores(map.getScores());
                     wvwevent.setMatch_id(matchDetails.getMatch_id());
                     wvwevent.setMatch_scores(matchDetails.getScores());
                     if (objective.getOwner_guild() != null) {
                         if (db.findGuildDetailsById(objective.getOwner_guild()).isEmpty()) {
                             guildDetails.add(client.getGuildDetails(objective.getOwner_guild()));
                         }
                     }
                     final WvwEvent oldWveevent = wvwEventCache.get(wvwevent.hashCode());
                     if (!wvwevent.equals(oldWveevent)) {
                        wvwevent.setTimestamp(timestamp);
                         wvwEventCache.putAsync(wvwevent.hashCode(), wvwevent);
                         esper.sendEvent(wvwevent);
                         wvwevents.add(wvwevent);
                     }
                 }
             }
             db.saveWvwScores(scores);
             db.saveGuildDetails(guildDetails);
         }
         db.saveWvwEvents(wvwevents);
         return true;
     }
 
     @Override
     public Boolean call() throws Exception {
         return getWvwEvents();
     }
 }
