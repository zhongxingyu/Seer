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
 package nl.surfnet.bod.web.view;
 
 import nl.surfnet.bod.domain.ReservationStatus;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.joda.time.ReadablePeriod;
 
 import com.google.common.base.Objects;
 
 /**
  * View object which holds filter related data regarding Reservations.
  * 
  */
 public class ReservationFilterView {
 
   private final String id;
   private final String label;
   private DateTime start;
   private DateTime end;
   private final boolean filterOnReservationEndOnly;
   private final ReservationStatus[] states;
   private final boolean filterOnStatusOnly;
 
   /**
    * Constructs a filter for the given year. Filtering will take place both on
    * the start and end of Reservation
    * 
    * @param year
    *          Year
    */
   public ReservationFilterView(int year) {
     id = String.valueOf(year);
     label = id;
 
     start = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.JANUARY).withDayOfMonth(01)
         .toDateTime().toDateTime();
 
     end = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(31).toDateTime()
         .toDateTime();
 
     filterOnReservationEndOnly = false;
     filterOnStatusOnly = false;
     states = ReservationStatus.values();
   }
 
   /**
    * Constructs a filter for a given Period. Filtering will take place on the
    * end of Reservation only;
    * 
    * @param id
    *          Id
    * @param label
    *          Label
    * @param period
    *          Period
    * @param endInPast
    *          Indicates that the endDate lies before the start date. This will
    *          be correct in the filter.
    */
   public ReservationFilterView(String id, String label, ReadablePeriod period, boolean endInPast) {
     this.id = id;
     this.label = label;
 
     if (endInPast) {
       this.end = DateTime.now();
       this.start = end.minus(period);
       //In the past, all status must be shown, since they cannot be change anymore.
       states = ReservationStatus.values();
     }
     else {
       this.start = DateTime.now();
       this.end = start.plus(period);
       //For the coming period, only show states which can change in the future.
       states = ReservationStatus.TRANSITION_STATES_AS_ARRAY;
     }
 
    filterOnReservationEndOnly = true;
     filterOnStatusOnly = false;
   }
 
   /**
    * Constructs a filter based on the specified {@link ReservationStatus}.
    * 
    * @param id
    *          Id
    * @param label
    *          Label
    * @param ReservationStatus
    *          status to filter on
    */
   public ReservationFilterView(String id, String label, ReservationStatus status) {
     this.id = id;
     this.label = label;
 
     filterOnStatusOnly = true;
     filterOnReservationEndOnly = false;
     this.states = new ReservationStatus[] { status };
   }
 
   public String getId() {
     return id;
   }
 
   public String getLabel() {
     return label;
   }
 
   @Override
   public int hashCode() {
     return Objects.hashCode(id, label);
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj) {
       return true;
     }
 
     if (obj instanceof ReservationFilterView) {
       ReservationFilterView resFilterView = (ReservationFilterView) obj;
 
       return Objects.equal(this.id, resFilterView.id) && Objects.equal(this.label, resFilterView.label);
     }
     else {
       return false;
     }
   }
 
   public DateTime getStart() {
     return start;
   }
 
   public DateTime getEnd() {
     return end;
   }
 
   public ReservationStatus[] getStates() {
     return states;
   }
 
   public boolean isFilterOnReservationEndOnly() {
     return filterOnReservationEndOnly;
   }
 
   public boolean isFilterOnStatusOnly() {
     return filterOnStatusOnly;
   }
 
   @Override
   public String toString() {
     return id;
   }
 
   public LocalDate getStartAsLocalDate() {
     return start.toLocalDate();
   }
 
   public LocalTime getStartAsLocalTime() {
     return start.toLocalTime();
   }
 
   public LocalDate getEndAsLocalDate() {
     return end.toLocalDate();
   }
 
   public LocalTime getEndAsLocalTime() {
     return end.toLocalTime();
   }
 }
