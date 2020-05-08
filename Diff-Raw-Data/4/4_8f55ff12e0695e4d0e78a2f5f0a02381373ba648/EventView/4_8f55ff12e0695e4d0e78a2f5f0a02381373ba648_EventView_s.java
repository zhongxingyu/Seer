 package org.karmaexchange.resources.msg;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import lombok.AccessLevel;
 import lombok.Data;
 import lombok.Delegate;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 import org.karmaexchange.dao.AggregateRating;
 import org.karmaexchange.dao.BaseDao;
 import org.karmaexchange.dao.Event;
 import org.karmaexchange.dao.KeyWrapper;
 import org.karmaexchange.dao.Organization;
 import org.karmaexchange.dao.PageRef;
 
 @XmlRootElement
 @Data
 @NoArgsConstructor
 public class EventView implements BaseDaoView<Event> {
 
  @Delegate
   @Getter(AccessLevel.NONE)
   @Setter(AccessLevel.NONE)
   private Event event = new Event();
 
   private OrgDetails organizationDetails;
 
   public EventView(Event event) {
     this.event = event;
     Organization org = BaseDao.load(KeyWrapper.toKey(event.getOrganization()));
     if (org != null) {
       organizationDetails = new OrgDetails(org);
     }
   }
 
   @Override
   public Event getDao() {
     return event;
   }
 
   @Data
   @NoArgsConstructor
   public static class OrgDetails {
     private String orgName;
     private PageRef page;
     private long karmaPoints;
     private AggregateRating eventRating;
 
     public OrgDetails(Organization org) {
       orgName = org.getOrgName();
       page = org.getPage();
       karmaPoints = org.getKarmaPoints();
       eventRating = org.getEventRating();
     }
   }
 }
