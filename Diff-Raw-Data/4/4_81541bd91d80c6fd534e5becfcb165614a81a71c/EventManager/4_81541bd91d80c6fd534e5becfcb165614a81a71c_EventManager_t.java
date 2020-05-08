 package de.flower.rmt.service;
 
 import com.mysema.query.types.EntityPath;
 import com.mysema.query.types.Order;
 import com.mysema.query.types.OrderSpecifier;
 import com.mysema.query.types.expr.BooleanExpression;
 import de.flower.common.model.EntityHelper;
 import de.flower.common.util.Check;
 import de.flower.rmt.model.db.entity.*;
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.model.db.entity.event.Event_;
 import de.flower.rmt.model.db.entity.event.QEvent;
 import de.flower.rmt.model.db.type.EventType;
 import de.flower.rmt.model.db.type.RSVPStatus;
 import de.flower.rmt.model.db.type.activity.EventUpdateMessage;
 import de.flower.rmt.model.dto.Notification;
 import de.flower.rmt.repository.IEventRepo;
 import de.flower.rmt.service.mail.IMailService;
 import de.flower.rmt.service.mail.INotificationService;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.support.MessageSourceAccessor;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.metamodel.Attribute;
 import java.util.List;
 
 import static de.flower.rmt.repository.Specs.eq;
 import static de.flower.rmt.repository.Specs.fetch;
 import static org.springframework.data.jpa.domain.Specifications.where;
 
 /**
  * @author flowerrrr
  */
 @Service
 @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 public class EventManager extends AbstractService implements IEventManager {
 
     @Autowired
     private IEventRepo eventRepo;
 
     @Autowired
     private IInvitationManager invitationManager;
 
     @Autowired
     private INotificationService notificationService;
 
     @Autowired
     private IUserManager userManager;
 
     @Autowired
     private IMailService mailService;
 
     @Autowired
     private IActivityManager activityManager;
 
     @Autowired
     private ILineupManager lineupManager;
 
     @Value("${event.closed.before.hours}")
     private Integer eventClosedBeforeHours;
 
     @Autowired
     private MessageSourceAccessor messageSource;
 
     @Override
     @Transactional(readOnly = false)
     public void save(Event entity) {
         validate(entity);
         EventUpdateMessage.Type type = (entity.isNew()) ? EventUpdateMessage.Type.CREATED : EventUpdateMessage.Type.UPDATED;
         eventRepo.save(entity);
         activityManager.onCreateOrUpdateEvent(entity, type);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void create(final Event entity, final boolean createInvitations) {
         Check.notNull(entity);
         save(entity);
         if (createInvitations) {
             // for every user that is a player of the team of this event a invitation will be created
             List<User> users = userManager.findAllByTeam(entity.getTeam());
             invitationManager.addUsers(entity, EntityHelper.convertEntityListToIdList(users));
 
             lineupManager.createLineup(entity);
         }
     }
 
     @Override
     public Event loadById(Long id, final Attribute... attributes) {
         Specification fetch = fetch(attributes);
         Event entity = eventRepo.findOne(where(eq(Event_.id, id)).and(fetch));
         Check.notNull(entity, "Event [" + id + "] not found");
         assertClub(entity);
         // init transient date fields
         entity.initTransientFields();
         return entity;
     }
 
     @Override
     public long getNumEventsByUser(final User user) {
         if (user != null) {
             BooleanExpression isUser = QEvent.event.invitations.any().user.eq(user);
             return eventRepo.count(isUser);
         } else {
             return eventRepo.count();
         }
     }
 
     @Override
     public List<Event> findAll(final int page, final int size, final User user, final EntityPath<?>... attributes) {
         BooleanExpression isUser = null;
         if (user != null) {
             isUser = QEvent.event.invitations.any().user.eq(user);
         }
         return eventRepo.findAll(isUser, new PageRequest(page, size, Sort.Direction.DESC, Event_.dateTime.getName()), attributes).getContent();
     }
 
     @Override
     public Event findNextEvent(final User user) {
         BooleanExpression isUser = null;
         if (user != null) {
             isUser = QEvent.event.invitations.any().user.eq(user);
         }
         BooleanExpression isUpcomming = QEvent.event.dateTime.after(new LocalDate().toDateTimeAtStartOfDay());
         BooleanExpression notCanceled = QEvent.event.canceled.isTrue().not();
         List<Event> upcoming = eventRepo.findAll(isUpcomming.and(isUser).and(notCanceled), new PageRequest(0, 1, Sort.Direction.ASC, Event_.dateTime.getName())).getContent();
         return (upcoming.isEmpty()) ? null : upcoming.get(0);
     }
 
     @Override
     public List<Event> findAllNextNHours(final int hours) {
         // when: 5 days before event, but at least 48 h after invitation mail
         DateTime now = new DateTime();
         BooleanExpression insideNextNDays = QEvent.event.dateTime.between(now, now.plusHours(hours));
         BooleanExpression notCanceled = QEvent.event.canceled.ne(true);
         return eventRepo.findAll(insideNextNDays.and(notCanceled));
     }
 
     @Override
     public List<Event> findAllByDateRange(final DateTime start, final DateTime end, EntityPath<?>... attributes) {
         BooleanExpression isBeetween = QEvent.event.dateTime.between(start, end);
         return eventRepo.findAll(isBeetween, attributes);
     }
 
     @Override
     public List<Event> findAllByDateRangeAndUser(final DateTime start, final DateTime end, final User user, EntityPath<?>... attributes) {
         BooleanExpression isBeetween = QEvent.event.dateTime.between(start, end);
         BooleanExpression isUser = null;
         if (user != null) {
             isUser = QEvent.event.invitations.any().user.eq(user);
         }
         return eventRepo.findAll(isBeetween.and(isUser), new OrderSpecifier(Order.DESC, QEvent.event.dateTime), attributes);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void delete(Long id) {
         Event entity = loadById(id);
         assertClub(entity);
         List<Invitation> invitations = invitationManager.findAllByEvent(entity);
         for (Invitation invitation : invitations) {
             invitationManager.delete(invitation.getId());
         }
         Lineup lineup = lineupManager.findLineup(entity);
         if (lineup != null) {
             lineupManager.delete(lineup.getId());
         }
         eventRepo.delete(entity);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void deleteByTeam(Team team) {
         for (Event event : eventRepo.findAllByTeam(team)) {
             eventRepo.softDelete(event);
         }
     }
 
     @Override
     public Event newInstance(EventType eventType) {
         Check.notNull(eventType);
         Event event = eventType.newInstance(getClub());
         event.setCreatedBy(securityService.getUser());
         return event;
     }
 
     @Override
     public void sendInvitationMail(final Long eventId, final Notification notification) {
         Event event = loadById(eventId);
         mailService.sendMassMail(notification);
         event.setInvitationSent(true);
         eventRepo.save(event);
         // update all invitations and mark them also with invitationSent = true (allows to later add invitations and send mails to new participants)
         invitationManager.markInvitationSent(event, notification.getAddressList(), null);
         activityManager.onInvitationMailSent(event);
     }
 
     @Override
     public boolean isEventClosed(Event event) {
        // RMT-733
        if (event.isCanceled()) {
            return false;
        }
         DateTime now = new DateTime();
         // eventRepo.reattach(event);
         return now.plusHours(eventClosedBeforeHours).isAfter(event.getDateTime());
     }
 
     @Override
     public void cancelEvent(final Long id) {
         Event event = loadById(id);
         event.setCanceled(true);
         String summary = messageSource.getMessage("event.summary.canceled.prefix") + " " + event.getSummary();
         event.setSummary(summary);
         eventRepo.save(event);
 
         // notify accepted and unsure invitees
         sendEventCanceledMessage(event);
 
         activityManager.onCreateOrUpdateEvent(event, EventUpdateMessage.Type.CANCELED);
     }
 
     private void sendEventCanceledMessage(final Event event) {
         List<Invitation> invitations = invitationManager.findAllByEventAndStatus(event, RSVPStatus.ACCEPTED, Invitation_.user);
         invitations.addAll(invitationManager.findAllByEventAndStatus(event, RSVPStatus.UNSURE, Invitation_.user));
         notificationService.sendEventCanceledMessage(event, invitations);
     }
 }
