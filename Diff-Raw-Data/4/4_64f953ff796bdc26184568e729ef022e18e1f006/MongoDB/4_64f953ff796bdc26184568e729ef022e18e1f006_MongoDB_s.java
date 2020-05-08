 /*
  * gw2live - GuildWars 2 Dynamic Map
  * 
  * Website: http://gw2map.com
  *
  * Copyright 2013   zyclonite    networx
  *                  http://zyclonite.net
  * Developer: Lukas Prettenthaler
  */
 package net.zyclonite.gw2live.service;
 
 import com.mongodb.AggregationOutput;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 import com.mongodb.MongoClientOptions;
 import com.mongodb.MongoClientURI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import net.zyclonite.gw2live.model.ChatMessage;
 import net.zyclonite.gw2live.model.Coordinate;
 import net.zyclonite.gw2live.model.GuildDetails;
 import net.zyclonite.gw2live.model.GwMap;
 import net.zyclonite.gw2live.model.KeyValueLanguage;
 import net.zyclonite.gw2live.model.PveEvent;
 import net.zyclonite.gw2live.model.PveEventDetails;
 import net.zyclonite.gw2live.model.StatsItem;
 import net.zyclonite.gw2live.model.WvwEvent;
 import net.zyclonite.gw2live.model.WvwGuildStatistic;
 import net.zyclonite.gw2live.model.WvwMatch;
 import net.zyclonite.gw2live.model.WvwObjectiveDetails;
 import net.zyclonite.gw2live.model.WvwScore;
 import net.zyclonite.gw2live.util.AppConfig;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mongojack.DBCursor;
 import org.mongojack.DBQuery;
 import org.mongojack.DBSort;
 import org.mongojack.JacksonDBCollection;
 
 /**
  *
  * @author zyclonite
  */
 public final class MongoDB {
 
     private static final Log LOG = LogFactory.getLog(MongoDB.class);
     private static final MongoDB instance;
     private DB database;
     private JacksonDBCollection<PveEvent, String> pveevents;
     private JacksonDBCollection<WvwEvent, String> wvwevents;
     private JacksonDBCollection<KeyValueLanguage, String> pveeventnames;
     private JacksonDBCollection<KeyValueLanguage, String> pvemapnames;
     private JacksonDBCollection<KeyValueLanguage, String> pveworldnames;
     private JacksonDBCollection<WvwMatch, String> wvwmatches;
     private JacksonDBCollection<WvwScore, String> wvwscores;
     private JacksonDBCollection<KeyValueLanguage, String> wvwobjectivenames;
     private JacksonDBCollection<KeyValueLanguage, String> wvwobjectivelongnames;
     private JacksonDBCollection<KeyValueLanguage, String> wvwmapnames;
     private JacksonDBCollection<WvwObjectiveDetails, String> wvwobjectivedetails;
     private JacksonDBCollection<GuildDetails, String> guilddetails;
     private JacksonDBCollection<Coordinate, String> wvwcoordinates;
     private JacksonDBCollection<Coordinate, String> pvecoordinates;
     private JacksonDBCollection<ChatMessage, String> chatmessages;
     private JacksonDBCollection<PveEventDetails, String> pveeventdetails;
     private JacksonDBCollection<GwMap, String> maps;
     private JacksonDBCollection<WvwGuildStatistic, String> wvwguildstatistics;
     private final Map<String, JacksonDBCollection<StatsItem, String>> statscolls;
     private final long statssize;
     private final long chatsize;
 
     static {
         instance = new MongoDB();
     }
 
     private MongoDB() {
         final AppConfig config = AppConfig.getInstance();
         final String uri = config.getString("mongodb.uri", "mongodb://127.0.0.1:27017/gw2live");
         statssize = config.getLong("mongodb.statcollsize", 10485760L);//in bytes 1048576L=1MB, 
         chatsize = config.getLong("mongodb.chatcollsize", 104857600L);//in bytes 1048576L=1MB, 
         statscolls = new HashMap<>();
 
         try {
             final MongoClientOptions.Builder options = new MongoClientOptions.Builder();
             options.autoConnectRetry(true);
             options.maxAutoConnectRetryTime(15); //s
             options.connectTimeout(10000); //ms
             options.socketTimeout(30000); //ms
             options.socketKeepAlive(false);
             options.connectionsPerHost(10);
             options.threadsAllowedToBlockForConnectionMultiplier(5);
             options.maxWaitTime(120000); //ms
             final MongoClientURI mongouri = new MongoClientURI(uri, options);
             final MongoClient mongo = new MongoClient(mongouri);
             database = mongo.getDB(mongouri.getDatabase());
 
             if ((mongouri.getUsername() != null) && (!mongouri.getUsername().trim().isEmpty()) && (!database.authenticate(mongouri.getUsername(), mongouri.getPassword()))) {
                 throw new Exception("Unable to authenticate with MongoDB server.");
             }
             initCollections();
             ensureIndexes();
         } catch (Exception e) {
             database = null;
             LOG.error("Connection to MongoDB could not be established: " + e.getMessage(), e);
         }
         LOG.debug("MongoDB initialized");
     }
 
     private void initCollections() {
         pveevents = JacksonDBCollection.wrap(database.getCollection("pveevents"), PveEvent.class, String.class);
         wvwevents = JacksonDBCollection.wrap(database.getCollection("wvwevents"), WvwEvent.class, String.class);
         pveeventnames = JacksonDBCollection.wrap(database.getCollection("pveeventnames"), KeyValueLanguage.class, String.class);
         pvemapnames = JacksonDBCollection.wrap(database.getCollection("pvemapnames"), KeyValueLanguage.class, String.class);
         pveworldnames = JacksonDBCollection.wrap(database.getCollection("pveworldnames"), KeyValueLanguage.class, String.class);
         wvwmatches = JacksonDBCollection.wrap(database.getCollection("wvwmatches"), WvwMatch.class, String.class);
         wvwscores = JacksonDBCollection.wrap(database.getCollection("wvwscores"), WvwScore.class, String.class);
         wvwobjectivenames = JacksonDBCollection.wrap(database.getCollection("wvwobjectivenames"), KeyValueLanguage.class, String.class);
         wvwobjectivelongnames = JacksonDBCollection.wrap(database.getCollection("wvwobjectivelongnames"), KeyValueLanguage.class, String.class);
         wvwmapnames = JacksonDBCollection.wrap(database.getCollection("wvwmapnames"), KeyValueLanguage.class, String.class);
         wvwobjectivedetails = JacksonDBCollection.wrap(database.getCollection("wvwobjectivedetails"), WvwObjectiveDetails.class, String.class);
         guilddetails = JacksonDBCollection.wrap(database.getCollection("guilddetails"), GuildDetails.class, String.class);
         wvwcoordinates = JacksonDBCollection.wrap(database.getCollection("wvwcoordinates"), Coordinate.class, String.class);
         pvecoordinates = JacksonDBCollection.wrap(database.getCollection("pvecoordinates"), Coordinate.class, String.class);
         pveeventdetails = JacksonDBCollection.wrap(database.getCollection("pveeventdetails"), PveEventDetails.class, String.class);
         maps = JacksonDBCollection.wrap(database.getCollection("maps"), GwMap.class, String.class);
         wvwguildstatistics = JacksonDBCollection.wrap(database.getCollection("wvwguildstatistics"), WvwGuildStatistic.class, String.class);
         final DBCollection chatmessagescoll;
         if (database.collectionExists("chatmessages")) {
             chatmessagescoll = database.getCollection("chatmessages");
         } else {
             final BasicDBObject capped = new BasicDBObject();
             capped.put("capped", true);
             capped.put("size", chatsize);
             chatmessagescoll = database.createCollection("chatmessages", capped);
         }
         chatmessages = JacksonDBCollection.wrap(chatmessagescoll, ChatMessage.class, String.class);
     }
 
     private void ensureIndexes() {
         pveevents.ensureIndex(new BasicDBObject("world_id", 1));
         pveevents.ensureIndex(new BasicDBObject("world_id", 1).append("map_id", 1));
         pveevents.ensureIndex(new BasicDBObject("world_id", 1).append("map_id", 1).append("event_id", 1));
 
         wvwevents.ensureIndex(new BasicDBObject("match_id", 1));
         wvwevents.ensureIndex(new BasicDBObject("match_id", 1).append("map_type", 1));
         wvwevents.ensureIndex(new BasicDBObject("match_id", 1).append("map_type", 1).append("objective_id", 1));
 
         pveeventnames.ensureIndex(new BasicDBObject("lang", 1));
         pveeventnames.ensureIndex(new BasicDBObject("id", 1).append("lang", 1), new BasicDBObject("unique", true));
 
         pvemapnames.ensureIndex(new BasicDBObject("lang", 1));
         pvemapnames.ensureIndex(new BasicDBObject("id", 1).append("lang", 1), new BasicDBObject("unique", true));
 
         pveworldnames.ensureIndex(new BasicDBObject("lang", 1));
         pveworldnames.ensureIndex(new BasicDBObject("id", 1).append("lang", 1), new BasicDBObject("unique", true));
 
         wvwmatches.ensureIndex(new BasicDBObject("wvw_match_id", 1), new BasicDBObject("unique", true));
 
         wvwscores.ensureIndex(new BasicDBObject("match_id", 1));
         wvwscores.ensureIndex(new BasicDBObject("match_id", 1).append("map_type", 1), new BasicDBObject("unique", true));
 
         wvwobjectivenames.ensureIndex(new BasicDBObject("lang", 1));
         wvwobjectivenames.ensureIndex(new BasicDBObject("id", 1).append("lang", 1), new BasicDBObject("unique", true));
 
         wvwobjectivelongnames.ensureIndex(new BasicDBObject("lang", 1));
         wvwobjectivelongnames.ensureIndex(new BasicDBObject("id", 1).append("lang", 1), new BasicDBObject("unique", true));
 
         wvwmapnames.ensureIndex(new BasicDBObject("lang", 1));
         wvwmapnames.ensureIndex(new BasicDBObject("id", 1).append("lang", 1), new BasicDBObject("unique", true));
 
         wvwobjectivedetails.ensureIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true));
 
         guilddetails.ensureIndex(new BasicDBObject("guild_name", 1));
 
         chatmessages.ensureIndex(new BasicDBObject("timestamp", -1));
         chatmessages.ensureIndex(new BasicDBObject("channel", 1));
 
         pveeventdetails.ensureIndex(new BasicDBObject("map_id", 1));
         pveeventdetails.ensureIndex(new BasicDBObject("event_id", 1), new BasicDBObject("unique", true));
 
         maps.ensureIndex(new BasicDBObject("map_id", 1), new BasicDBObject("unique", true));
 
         wvwguildstatistics.ensureIndex(new BasicDBObject("guild_id", 1));
         wvwguildstatistics.ensureIndex(new BasicDBObject("guild_id", 1).append("timestamp", 1));
     }
 
     public void initStatsCollections(final String coll) {
         final String collname = "stats_" + coll;
         final DBCollection collection;
         if (database.collectionExists(collname)) {
             collection = database.getCollection(collname);
         } else {
             final BasicDBObject capped = new BasicDBObject();
             capped.put("capped", true);
             capped.put("size", statssize);
             collection = database.createCollection(collname, capped);
         }
         final JacksonDBCollection<StatsItem, String> statscoll = JacksonDBCollection.wrap(collection, StatsItem.class, String.class);
         statscoll.ensureIndex(new BasicDBObject("timestamp", -1));
         statscolls.put(coll, statscoll);
     }
 
     public List<StatsItem> findStats(final String coll) {
         return findStats(coll, new Date(), 1000);
     }
 
     public List<StatsItem> findStats(final String coll, final Date from) {
         return findStats(coll, from, 1000);
     }
 
     public List<StatsItem> findStats(final String coll, final Date from, int limit) {
         if (limit > 1000) {
             limit = 1000;
         }
         List<StatsItem> result = new ArrayList<>();
         if (statscolls.containsKey(coll)) {
             result = statscolls.get(coll).find().lessThanEquals("timestamp", from).sort(DBSort.desc("$natural").asc("timestamp")).limit(limit).toArray();
         }
         return result;
     }
 
     public DBCursor<PveEvent> findPveEvents() {
         return pveevents.find();
     }
 
     public List<PveEvent> findPveEvents(final Long world) {
         final List<PveEvent> result = pveevents.find().is("world_id", world).toArray();
         return result;
     }
 
     public List<PveEvent> findPveEvents(final Long world, final Long map) {
         final List<PveEvent> result = pveevents.find().is("world_id", world).and(DBQuery.is("map_id", map)).toArray();
         return result;
     }
 
     public DBCursor<WvwEvent> findWvwEvents() {
         return wvwevents.find();
     }
 
     public List<WvwEvent> findWvwEvents(final String match) {
         final List<WvwEvent> result = wvwevents.find().is("match_id", match).toArray();
         return result;
     }
 
     public List<WvwEvent> findWvwEvents(final String match, final String maptype) {
         final List<WvwEvent> result = wvwevents.find().is("match_id", match).and(DBQuery.is("map_type", maptype)).toArray();
         return result;
     }
 
     public List<KeyValueLanguage> findPveEventNames() {
         return findPveEventNames("en");
     }
 
     public List<KeyValueLanguage> findPveEventNames(final String lang) {
         final List<KeyValueLanguage> result = pveeventnames.find().is("lang", lang).toArray();
         return result;
     }
 
     public List<KeyValueLanguage> findPveMapNames() {
         return findPveMapNames("en");
     }
 
     public List<KeyValueLanguage> findPveMapNames(final String lang) {
         final List<KeyValueLanguage> result = pvemapnames.find().is("lang", lang).toArray();
         return result;
     }
 
     public List<KeyValueLanguage> findPveWorldNames() {
         return findPveWorldNames("en");
     }
 
     public List<KeyValueLanguage> findPveWorldNames(final String lang) {
         final List<KeyValueLanguage> result = pveworldnames.find().is("lang", lang).toArray();
         return result;
     }
 
     public List<WvwMatch> findWvwMatches() {
         //final List<WvwMatch> result = wvwmatches.find(DBQuery.is("red_world_id", world_id).or(DBQuery.is("blue_world_id", world_id).or(DBQuery.is("green_world_id", world_id)))).toArray();
         final List<WvwMatch> result = wvwmatches.find().toArray();
         return result;
     }
 
     public WvwMatch findWvwMatch(final String matchid) {
         //final List<WvwMatch> result = wvwmatches.find(DBQuery.is("red_world_id", world_id).or(DBQuery.is("blue_world_id", world_id).or(DBQuery.is("green_world_id", world_id)))).toArray();
         final WvwMatch result = wvwmatches.findOne(DBQuery.is("wvw_match_id", matchid));
         return result;
     }
 
     public List<WvwScore> findWvwScores() {
         final List<WvwScore> result = wvwscores.find().toArray();
         return result;
     }
 
     public List<WvwScore> findWvwScores(final String match_id) {
         final List<WvwScore> result = wvwscores.find().is("match_id", match_id).toArray();
         return result;
     }
 
     public List<KeyValueLanguage> findWvwObjectiveNames() {
         return findWvwObjectiveNames("en");
     }
 
     public List<KeyValueLanguage> findWvwObjectiveNames(final String lang) {
         final List<KeyValueLanguage> result = wvwobjectivenames.find().is("lang", lang).toArray();
         return result;
     }
 
     public List<KeyValueLanguage> findWvwObjectiveLongNames() {
         return findWvwObjectiveLongNames("en");
     }
 
     public List<KeyValueLanguage> findWvwObjectiveLongNames(final String lang) {
         final List<KeyValueLanguage> result = wvwobjectivelongnames.find().is("lang", lang).toArray();
         return result;
     }
 
     public List<KeyValueLanguage> findWvwMapNames() {
         return findWvwMapNames("en");
     }
 
     public List<KeyValueLanguage> findWvwMapNames(final String lang) {
         final List<KeyValueLanguage> result = wvwmapnames.find().is("lang", lang).toArray();
         return result;
     }
 
     public List<WvwObjectiveDetails> findWvwObjectiveDetails() {
         final List<WvwObjectiveDetails> result = wvwobjectivedetails.find().toArray();
         return result;
     }
 
     public GuildDetails findGuildDetailsById(final String guild_id) {
         final GuildDetails result = guilddetails.findOne(DBQuery.is("guild_id", guild_id));
         return result;
     }
 
     public List<GuildDetails> findGuildDetailsByName(final String guild_name) {
         final List<GuildDetails> result = guilddetails.find().is("guild_name", guild_name).toArray();
         return result;
     }
 
     public List<Coordinate> findWvwCoordinates() {
         final List<Coordinate> result = wvwcoordinates.find().toArray();
         return result;
     }
 
     public List<Coordinate> findPveCoordinates() {
         final List<Coordinate> result = pvecoordinates.find().toArray();
         return result;
     }
 
     public List<ChatMessage> findChatMessages() {
         final List<ChatMessage> result = chatmessages.find().sort(DBSort.desc("$natural")).limit(100).toArray();
         return result;
     }
 
     public List<ChatMessage> findChatMessages(final String channel) {
         final List<ChatMessage> result = chatmessages.find().is("channel", channel).sort(DBSort.desc("$natural")).limit(100).toArray();
         return result;
     }
 
     public List<PveEventDetails> findPveEventDetails() {
         final List<PveEventDetails> result = pveeventdetails.find().toArray();
         return result;
     }
 
     public List<PveEventDetails> findPveEventDetails(final Long map) {
         final List<PveEventDetails> result = pveeventdetails.find().is("map_id", map).toArray();
         return result;
     }
 
     public List<PveEventDetails> findPveEventDetails(final String event) {
         final List<PveEventDetails> result = pveeventdetails.find().is("event_id", event).toArray();
         return result;
     }
 
     public List<GwMap> findMap(final String map) {
         final List<GwMap> result = maps.find().is("map_id", map).toArray();
         return result;
     }
 
     public String getTopGuilds(final String matchid) {
         final WvwMatch wvwmatch = findWvwMatch(matchid);
         if(wvwmatch == null){
             return "[]";
         }
 
         final DBObject timerange = new BasicDBObject("$gt", wvwmatch.getStart_time());
         timerange.put("$lt", wvwmatch.getEnd_time());
         final DBObject matchFields = new BasicDBObject("timestamp", timerange);
         matchFields.put("match_id", matchid);
         final DBObject match = new BasicDBObject("$match", matchFields);
 
         final DBObject projectFields = new BasicDBObject("guild_id", 1);
         projectFields.put("holdtime", 1);
         projectFields.put("_id", 0);
         final DBObject project = new BasicDBObject("$project", projectFields);
 
         final DBObject groupFields = new BasicDBObject("_id", "$guild_id");
         groupFields.put("holdtime", new BasicDBObject("$sum", "$holdtime"));
         groupFields.put("count", new BasicDBObject("$sum", 1));
         final DBObject group = new BasicDBObject("$group", groupFields);
 
        final DBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
 
         final DBObject limit = new BasicDBObject("$limit", 10);
 
         final AggregationOutput output = wvwguildstatistics.getDbCollection().aggregate(match, project, group, sort, limit);
         return output.results().toString();
     }
 
     public void saveStats(final String coll, final List<StatsItem> items) {
         if (statscolls.containsKey(coll)) {
             for (final StatsItem item : items) {
                 statscolls.get(coll).save(item);
                 LOG.trace("saved stats-item in " + coll + " to mongodb");
             }
         }
     }
 
     public void savePveEvents(final List<PveEvent> events) {
         for (final PveEvent event : events) {
             pveevents.update(DBQuery.is("world_id", event.getWorld_id()).and(DBQuery.is("map_id", event.getMap_id()).and(DBQuery.is("event_id", event.getEvent_id()))), event, true, false);
             LOG.trace("saved pve-event " + event.getEvent_id() + " to mongodb");
         }
     }
 
     public void saveWvwEvents(final List<WvwEvent> events) {
         for (final WvwEvent event : events) {
             wvwevents.update(DBQuery.is("match_id", event.getMatch_id()).and(DBQuery.is("map_type", event.getMap_type()).and(DBQuery.is("objective_id", event.getObjective_id()))), event, true, false);
             LOG.trace("saved wvw-event " + event.getMatch_id() + " " + event.getObjective_id() + " to mongodb");
         }
     }
 
     public void savePveEventNames(final List<KeyValueLanguage> names, final String lang) {
         for (final KeyValueLanguage name : names) {
             name.setLang(lang);
             pveeventnames.update(DBQuery.is("lang", lang).and(DBQuery.is("id", name.getId())), name, true, false);
             LOG.trace("saved " + lang + " pve-event-name " + name.getName() + " to mongodb");
         }
     }
 
     public void savePveMapNames(final List<KeyValueLanguage> names, final String lang) {
         for (final KeyValueLanguage name : names) {
             name.setLang(lang);
             pvemapnames.update(DBQuery.is("lang", lang).and(DBQuery.is("id", name.getId())), name, true, false);
             LOG.trace("saved " + lang + " pve-map-name " + name.getName() + " to mongodb");
         }
     }
 
     public void savePveWorldNames(final List<KeyValueLanguage> names, final String lang) {
         for (final KeyValueLanguage name : names) {
             name.setLang(lang);
             pveworldnames.update(DBQuery.is("lang", lang).and(DBQuery.is("id", name.getId())), name, true, false);
             LOG.trace("saved " + lang + " pve-map-name " + name.getName() + " to mongodb");
         }
     }
 
     public void saveWvwMatches(final List<WvwMatch> matches) {
         for (final WvwMatch match : matches) {
             wvwmatches.update(DBQuery.is("wvw_match_id", match.getWvw_match_id()), match, true, false);
             LOG.trace("saved wvw-match " + match.getWvw_match_id() + " to mongodb");
         }
     }
 
     public void saveWvwScores(final List<WvwScore> scores) {
         for (final WvwScore score : scores) {
             wvwscores.update(DBQuery.is("match_id", score.getMatch_id()).and(DBQuery.is("map_type", score.getMap_type())), score, true, false);
             LOG.trace("saved wvw-score " + score.getMatch_id() + " " + score.getMap_type() + " to mongodb");
         }
     }
 
     public void saveWvwObjectiveNames(final List<KeyValueLanguage> names, final String lang) {
         for (final KeyValueLanguage name : names) {
             name.setLang(lang);
             wvwobjectivenames.update(DBQuery.is("lang", lang).and(DBQuery.is("id", name.getId())), name, true, false);
             LOG.trace("saved " + lang + " wvw-objective-name " + name.getName() + " to mongodb");
         }
     }
 
     public void saveWvwObjectiveLongNames(final List<KeyValueLanguage> names) {
         for (final KeyValueLanguage name : names) {
             wvwobjectivelongnames.update(DBQuery.is("lang", name.getLang()).and(DBQuery.is("id", name.getId())), name, true, false);
             LOG.trace("saved " + name.getLang() + " wvw-objective-long-name " + name.getName() + " to mongodb");
         }
     }
 
     public void saveWvwMapNames(final List<KeyValueLanguage> names) {
         for (final KeyValueLanguage name : names) {
             wvwmapnames.update(DBQuery.is("lang", name.getLang()).and(DBQuery.is("id", name.getId())), name, true, false);
             LOG.trace("saved " + name.getLang() + " wvw-map-name " + name.getName() + " to mongodb");
         }
     }
 
     public void saveWvwObjectiveDetails(final List<WvwObjectiveDetails> details) {
         for (final WvwObjectiveDetails detail : details) {
             wvwobjectivedetails.update(DBQuery.is("id", detail.getId()), detail, true, false);
             LOG.trace("saved wvw-objective-detail " + detail.getId() + " to mongodb");
         }
     }
 
     public void saveGuildDetails(final List<GuildDetails> guilds) {
         for (final GuildDetails guild : guilds) {
             guild.setTimestamp(new Date());
             guilddetails.update(DBQuery.is("guild_id", guild.getGuild_id()), guild, true, false);
             LOG.trace("saved guild-details " + guild.getGuild_id() + " to mongodb");
         }
     }
 
     public void saveWvwCoordinates(final List<Coordinate> coordinates) {
         for (final Coordinate coordinate : coordinates) {
             wvwcoordinates.update(DBQuery.is("id", coordinate.getId()), coordinate, true, false);
             LOG.trace("saved wvwcoordinates " + coordinate.getId() + " to mongodb");
         }
     }
 
     public void savePveCoordinates(final List<Coordinate> coordinates) {
         for (final Coordinate coordinate : coordinates) {
             pvecoordinates.update(DBQuery.is("id", coordinate.getId()), coordinate, true, false);
             LOG.trace("saved pvecoordinates " + coordinate.getId() + " to mongodb");
         }
     }
 
     public void saveChatMessage(final ChatMessage message) {
         chatmessages.insert(message);
         LOG.trace("saved chatmessage to mongodb");
     }
 
     public void savePveEventDetails(final List<PveEventDetails> eventdetails) {
         for (final PveEventDetails eventdetail : eventdetails) {
             pveeventdetails.update(DBQuery.is("event_id", eventdetail.getEvent_id()), eventdetail, true, false);
             LOG.trace("saved pve-event-details " + eventdetail.getEvent_id() + " to mongodb");
         }
     }
 
     public void saveMaps(final List<GwMap> gwmaps) {
         for (final GwMap map : gwmaps) {
             maps.update(DBQuery.is("map_id", map.getMap_id()), map, true, false);
             LOG.trace("saved maps " + map.getMap_id() + " to mongodb");
         }
     }
 
     public void saveWvwGuildStatistics(final WvwGuildStatistic statsentry) {
         wvwguildstatistics.insert(statsentry);
         LOG.trace("saved wvwguildstatistic " + statsentry.getGuild_id() + " to mongodb");
     }
 
     public static MongoDB getInstance() {
         return instance;
     }
 }
