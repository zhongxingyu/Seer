 /*
  * gw2live - GuildWars 2 Dynamic Map
  * 
  * Website: http://gw2map.com
  *
  * Copyright 2013   zyclonite    networx
  *                  http://zyclonite.net
  * Developer: Lukas Prettenthaler
  */
 package net.zyclonite.gw2live.listener;
 
 import com.espertech.esper.client.EventBean;
 import java.util.Date;
 import net.zyclonite.gw2live.model.WvwGuildStatistic;
 import net.zyclonite.gw2live.service.MongoDB;
 import net.zyclonite.gw2live.util.EplUpdateListener;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *
  * @author zyclonite
  */
 public class GuildStatisticUpdateListener extends EplUpdateListener {
 
     private static final Log LOG = LogFactory.getLog(GuildStatisticUpdateListener.class);
    private static final String EPL = "select o.* from pattern [ every o=WvwEvent -> n=WvwEvent(match_id=o.match_id, map_type=o.map_type, objective_id=o.objective_id, o.owner_guild is not null, owner_guild is not o.owner_guild) ] where o.owner_guild is not null";
     private final MongoDB db;
 
     public GuildStatisticUpdateListener() {
         super("GuildStatisticUpdateListener", EPL);
         db = MongoDB.getInstance();
         LOG.debug("GuildStatisticUpdateListener initialized");
     }
 
     @Override
     public void update(EventBean[] newEvents, EventBean[] oldEvents) {
         //TODO: needs threading (nonblocking) + push to eventbus
         if (newEvents.length > 0) {
             final Date now = new Date();
             final Date oldtime = (Date) newEvents[newEvents.length - 1].get("guild_timestamp");
             final WvwGuildStatistic gstats = new WvwGuildStatistic();
             gstats.setGuild_id((String) newEvents[newEvents.length - 1].get("owner_guild"));
             gstats.setHoldtime(now.getTime() - oldtime.getTime());
             gstats.setMap_type((String) newEvents[newEvents.length - 1].get("map_type"));
             gstats.setMatch_id((String) newEvents[newEvents.length - 1].get("match_id"));
             gstats.setObjective_id((Long) newEvents[newEvents.length - 1].get("objective_id"));
             gstats.setTimestamp(now);
             db.saveWvwGuildStatistics(gstats);
         }
     }
 }
