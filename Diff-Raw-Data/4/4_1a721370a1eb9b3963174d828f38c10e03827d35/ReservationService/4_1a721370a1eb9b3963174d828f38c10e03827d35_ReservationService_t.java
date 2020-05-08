 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.service;
 
 import static com.google.common.base.Preconditions.checkState;
 import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
 import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
 import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static org.mockito.Mockito.when;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import nl.surfnet.bod.domain.*;
 import nl.surfnet.bod.nbi.NbiClient;
 import nl.surfnet.bod.repo.ReservationRepo;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 import nl.surfnet.bod.web.view.ReservationFilterView;
 
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.data.jpa.domain.Specifications;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class ReservationService {
 
   private final Logger log = LoggerFactory.getLogger(getClass());
 
   @Autowired
   private ReservationRepo reservationRepo;
 
   @Autowired
   private NbiClient nbiClient;
 
   @Autowired
   private ReservationEventPublisher reservationEventPublisher;
 
   @Autowired
   private EntityManagerFactory entityManagerFactory;
 
   private ExecutorService executorService = Executors.newCachedThreadPool();
 
   /**
    * Reserves a reservation using the {@link NbiClient} asynchronously.
    * 
    * @param reservation
    * @return
    */
   public Future<?> create(Reservation reservation) {
     checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
     checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
 
    when(reservationRepo.save(reservation)).thenReturn(reservation);
    
     // Make sure reservations occur on whole minutes only
     if (reservation.getStartDateTime() != null) {
       reservation.setStartDateTime(reservation.getStartDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
     }
     reservation.setEndDateTime(reservation.getEndDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
     return executorService.submit(new ReservationSubmitter(reservationRepo.save(reservation)));
   }
 
   public Reservation find(Long id) {
     return reservationRepo.findOne(id);
   }
 
   public List<Reservation> findEntries(int firstResult, int maxResults, Sort sort) {
     final RichUserDetails user = Security.getUserDetails();
 
     if (user.getUserGroups().isEmpty()) {
       return Collections.emptyList();
     }
 
     return reservationRepo.findAll(forCurrentUser(user), new PageRequest(firstResult / maxResults, maxResults, sort))
         .getContent();
   }
 
   public Collection<Reservation> findByVirtualPort(VirtualPort port) {
     return reservationRepo.findBySourcePortOrDestinationPort(port, port);
   }
 
   public long countForUser(RichUserDetails user) {
     if (user.getUserGroups().isEmpty()) {
       return 0;
     }
 
     return reservationRepo.count(forCurrentUser(user));
   }
 
   public Reservation update(Reservation reservation) {
     checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
     checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
 
     log.debug("Updating reservation: {}", reservation.getReservationId());
     return reservationRepo.save(reservation);
   }
 
   /**
    * Cancels a reservation if the current user has the correct role and the
    * reservation is allowed to be deleted depending on its state. Updates the
    * state of the reservation.
    * 
    * @param reservation
    *          {@link Reservation} to delete
    * @return true if the reservation was canceld, false otherwise.
    */
   public boolean cancel(Reservation reservation, RichUserDetails user) {
     if (user.isSelectedUserRole() && reservation.getStatus().isDeleteAllowed()) {
       reservation.setStatus(CANCELLED);
       nbiClient.cancelReservation(reservation.getReservationId());
       reservationRepo.save(reservation);
       return true;
     }
     else {
       log.info("Not allowed to cancel reservation {}", reservation.getName());
     }
     return false;
   }
 
   public ReservationStatus getStatus(Reservation reservation) {
     return nbiClient.getReservationStatus(reservation.getReservationId());
   }
 
   /**
    * Finds all reservations which start or ends on the given dateTime and have a
    * status which can still change its status.
    * 
    * @param dateTime
    *          {@link LocalDateTime} to search for
    * @return list of found Reservations
    */
   public List<Reservation> findReservationsToPoll(LocalDateTime dateTime) {
     return reservationRepo.findAll(specReservationsToPoll(dateTime));
   }
 
   private Specification<Reservation> forVirtualResourceGroup(final VirtualResourceGroup vrg) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.equal(root.get(Reservation_.virtualResourceGroup), vrg);
       }
 
     };
   }
 
   private Specification<Reservation> forManager(final RichUserDetails manager) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 
         Long prgId = manager.getSelectedRole().getPhysicalResourceGroupId();
         return cb.and(cb.or(
             cb.equal(
                 root.get(Reservation_.sourcePort).get(VirtualPort_.physicalPort)
                     .get(PhysicalPort_.physicalResourceGroup).get(PhysicalResourceGroup_.id), prgId),
             cb.equal(
                 root.get(Reservation_.destinationPort).get(VirtualPort_.physicalPort)
                     .get(PhysicalPort_.physicalResourceGroup).get(PhysicalResourceGroup_.id), prgId)));
       }
     };
   }
 
   private Specification<Reservation> forCurrentUser(final RichUserDetails user) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.and(root.get(Reservation_.virtualResourceGroup).get(VirtualResourceGroup_.surfconextGroupId)
             .in(user.getUserGroupIds()));
       }
     };
   }
 
   private Specification<Reservation> forStatus(final ReservationStatus status) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.equal(root.get(Reservation_.status), status);
       }
     };
   }
 
   private Specification<Reservation> specReservationsToPoll(final LocalDateTime startOrEndDateTime) {
     return new Specification<Reservation>() {
       @Override
       public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
           CriteriaBuilder cb) {
 
         return cb.and(
             cb.or(cb.equal(reservation.get(Reservation_.startDateTime), startOrEndDateTime),
                 cb.equal(reservation.get(Reservation_.endDateTime), startOrEndDateTime)),
             reservation.get(Reservation_.status).in(ReservationStatus.TRANSITION_STATES));
       }
     };
   }
 
   private Specification<Reservation> specFilteredReservationsForUser(final ReservationFilterView filter,
       final RichUserDetails user) {
 
     return Specifications.where(specFilteredReservations(filter)).and(forCurrentUser(user));
   }
 
   private Specification<Reservation> specFilteredReservationsForManager(final ReservationFilterView filter,
       final RichUserDetails manager) {
 
     return Specifications.where(specFilteredReservations(filter)).and(forManager(manager));
   }
 
   private Specification<Reservation> specFilteredReservations(final ReservationFilterView filter) {
     Specification<Reservation> specficiation = null;
 
     Specification<Reservation> filterSpecOnStart = new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.between(root.get(Reservation_.startDateTime), filter.getStart(), filter.getEnd());
       }
     };
 
     Specification<Reservation> filterSpecOnEnd = new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 
         return cb.between(root.get(Reservation_.endDateTime), filter.getStart(), filter.getEnd());
       }
     };
 
     if (filter.isFilterOnStatusOnly()) {
       specficiation = forStatus(filter.getStatus());
     }
     else {
       specficiation = filterSpecOnEnd;
 
       if (!filter.isFilterOnReservationEndOnly()) {
         specficiation = Specifications.where(filterSpecOnEnd).or(filterSpecOnStart);
       }
     }
 
     return specficiation;
   }
 
   public List<Reservation> findEntriesForUserUsingFilter(final RichUserDetails user,
       final ReservationFilterView filter, int firstResult, int maxResults, Sort sort) {
 
     return reservationRepo.findAll(specFilteredReservationsForUser(filter, user),
         new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
   }
 
   public List<Reservation> findEntriesForManagerUsingFilter(RichUserDetails manager, ReservationFilterView filter,
       int firstResult, int maxResults, Sort sort) {
 
     return reservationRepo.findAll(specFilteredReservationsForManager(filter, manager),
         new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
   }
 
   public List<Reservation> findAllEntriesUsingFilter(final ReservationFilterView filter, int firstResult,
       int maxResults, Sort sort) {
 
     return reservationRepo.findAll(specFilteredReservations(filter),
         new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
   }
 
   public long countForFilterAndUser(RichUserDetails user, ReservationFilterView filter) {
     return reservationRepo.count(specFilteredReservationsForUser(filter, user));
   }
 
   public long countForFilterAndManager(RichUserDetails manager, ReservationFilterView filter) {
     return reservationRepo.count(specFilteredReservationsForManager(filter, manager));
   }
 
   public long countAllEntriesUsingFilter(final ReservationFilterView filter) {
     return reservationRepo.count(specFilteredReservations(filter));
   }
 
   public long countForVirtualResourceGroup(VirtualResourceGroup vrg) {
     return reservationRepo.count(forVirtualResourceGroup(vrg));
   }
 
   public long countScheduledReservationForVirtualResourceGroup(VirtualResourceGroup vrg) {
     return countForVirtualResourceGroup(vrg, SCHEDULED);
   }
 
   public long countActiveReservationForVirtualResourceGroup(VirtualResourceGroup vrg) {
     return countForVirtualResourceGroup(vrg, RUNNING);
   }
 
   private long countForVirtualResourceGroup(VirtualResourceGroup vrg, ReservationStatus status) {
     Specification<Reservation> spec = Specifications.where(forVirtualResourceGroup(vrg)).and(forStatus(status));
 
     return reservationRepo.count(spec);
   }
 
   public List<Double> findUniqueYearsFromReservations() {
 
     // FIXME Franky add userDetails to query
     final String queryString = "select distinct extract(year from start_date_time) startYear "
         + "from reservation UNION select distinct extract(year from end_date_time) from reservation";
 
     @SuppressWarnings("unchecked")
     List<Double> resultList = entityManagerFactory.createEntityManager().createNativeQuery(queryString).getResultList();
     resultList.remove(null);
 
     Collections.sort(resultList);
 
     return resultList;
   }
 
   /**
    * Asynchronous {@link Reservation} creator.
    * 
    */
   private final class ReservationSubmitter implements Runnable {
     private final Reservation reservation;
     private final ReservationStatus originalStatus;
 
     public ReservationSubmitter(Reservation reservation) {
       this.reservation = reservation;
       this.originalStatus = reservation.getStatus();
     }
 
     @Override
     public void run() {
       Reservation createdReservation = nbiClient.createReservation(reservation);
       publishStatusChanged(update(createdReservation));
     }
 
     private void publishStatusChanged(Reservation newReservation) {
       ReservationStatusChangeEvent createEvent = new ReservationStatusChangeEvent(originalStatus, newReservation);
 
       reservationEventPublisher.notifyListeners(createEvent);
     }
   }
 
   public List<Reservation> findReservationWithStatus(ReservationStatus... states) {
     return reservationRepo.findByStatusIn(Arrays.asList(states));
   }
 
   public long count() {
     return reservationRepo.count();
   }
   
   public void flushRepo(){
     reservationRepo.flush();
   }
 
 }
