 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.service;
 
 import static nl.surfnet.bod.web.WebUtils.not;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.event.LogEvent;
 import nl.surfnet.bod.event.LogEvent_;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.joda.time.DateTime;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.util.CollectionUtils;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 public final class LogEventPredicatesAndSpecifications {
 
   private LogEventPredicatesAndSpecifications() {
   }
 
   static Specification<LogEvent> specLogEventsByAdminGroups(final Collection<String> adminGroups) {
     return new Specification<LogEvent>() {
       @Override
       public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return inAdminGroups(adminGroups, root, cb);
       }
 
     };
   }
 
   static Specification<LogEvent> specForReservationBeforeInAdminGroupsWithStateIn(final Long reservationId,
       final DateTime before, final Collection<String> adminGroups, final ReservationStatus... states) {
 
     final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);
 
     return new Specification<LogEvent>() {
       @Override
       public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 
         Predicate predicate = getPredicateForDomainObjectBeforeInAdminGroups(root, cb, Lists
             .newArrayList(reservationId), before, domainObjectName, adminGroups);
         Predicate predicateStateIn = getPredicateForStateIn(root, cb, states);
 
         return predicateStateIn == null ? predicate : cb.and(predicate, predicateStateIn);
       }
     };
   }
 
   static Specification<LogEvent> specForReservationBetweenForAdminGroupsWithStateIn(final List<Long> reservationIds,
       final DateTime start, final DateTime end, final Collection<String> adminGroups, final ReservationStatus... states) {
 
     return new Specification<LogEvent>() {
       private final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);
 
       @Override
       public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 
         Predicate predicate = getPredicateForDomainObjectInAdminGroupsBetween(root, cb, reservationIds, start, end,
             domainObjectName, adminGroups);
 
         return cb.and(predicate, getPredicateForStateIn(root, cb, states));
       }
     };
   }
 
   static Specification<LogEvent> specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(
       final ReservationStatus oldStatus, final ReservationStatus newStatus, final List<Long> reservationIds,
       final DateTime start, final DateTime end, final Collection<String> adminGroups) {
 
     return new Specification<LogEvent>() {
       private final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);
 
       @Override
       public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 
         Predicate predicate = getPredicateForDomainObjectInAdminGroupsBetween(root, cb, reservationIds, start, end,
             domainObjectName, adminGroups);
 
         return cb.and(predicate, getPredicateForStateTransition(root, cb, oldStatus, newStatus));
       }
     };
   }
 
   private static Predicate getPredicateForDomainObjectInAdminGroupsBetween(final Root<LogEvent> root,
       final CriteriaBuilder cb, final List<Long> reservationIds, final DateTime start, final DateTime end,
       final String domainObjectName, final Collection<String> adminGroups) {
 
     Predicate predicate = getDomainObjectWithIdsInAdminGroups(root, cb, reservationIds, domainObjectName, adminGroups);
 
     return cb.and(predicate, cb.between(root.get(LogEvent_.created), start, end));
   }
 
   private static Predicate getPredicateForDomainObjectBeforeInAdminGroups(Root<LogEvent> root, CriteriaBuilder cb,
       final List<Long> reservationIds, final DateTime before, final String domainObjectName,
       final Collection<String> adminGroups) {
 
     Predicate predicate = getDomainObjectWithIdsInAdminGroups(root, cb, reservationIds, domainObjectName, adminGroups);
     return cb.and(predicate, cb.lessThanOrEqualTo(root.get(LogEvent_.created), before));
   }
 
   private static Predicate getDomainObjectWithIdsInAdminGroups(final Root<LogEvent> root, final CriteriaBuilder cb,
       final List<Long> reservationIds, final String domainObjectName, Collection<String> adminGroups) {
 
     Predicate predicate = cb.equal(root.get(LogEvent_.domainObjectClass), domainObjectName);
 
     if (not(CollectionUtils.isEmpty(reservationIds))) {
       predicate = cb.and(predicate, root.get(LogEvent_.domainObjectId).in(reservationIds));
     }
 
    Predicate inAdminGroups = inAdminGroups(adminGroups, root, cb);
    if (inAdminGroups != null) {
      predicate = cb.and(predicate, inAdminGroups);
     }
 
     return predicate;
   }
 
   private static Predicate inAdminGroups(final Collection<String> adminGroups, Root<LogEvent> root, CriteriaBuilder cb) {
     Collection<Predicate> restrictions = new ArrayList<>();
     for (String adminGroup: adminGroups) {
       restrictions.add(cb.isMember(adminGroup, root.get(LogEvent_.adminGroups)));
     }
     return cb.or(Iterables.toArray(restrictions, Predicate.class));
   }
 
   private static Predicate getPredicateForStateTransition(Root<LogEvent> root, CriteriaBuilder cb,
       final ReservationStatus oldStatus, final ReservationStatus newStatus) {
 
     return cb.and(cb.equal(root.get(LogEvent_.oldReservationStatus), oldStatus), cb.equal(root
         .get(LogEvent_.newReservationStatus), newStatus));
   }
 
   private static Predicate getPredicateForStateIn(Root<LogEvent> root, CriteriaBuilder cb,
       final ReservationStatus... states) {
     Predicate predicate = null;
 
     if (not(ArrayUtils.isEmpty(states))) {
       predicate = cb.and(cb.isNotNull(root.get(LogEvent_.newReservationStatus)), (root
           .get(LogEvent_.newReservationStatus).in((Object[]) states)));
     }
     return predicate;
   }
 }
