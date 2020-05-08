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
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.DateTimeUtils;
 import org.joda.time.LocalDate;
 import org.joda.time.Months;
 import org.junit.Test;
 
 import com.google.common.collect.Lists;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.nullValue;
 
 public class ReservationFilterViewTest {
 
   private ReservationFilterView reservationFilterView;
 
   @Test
   public void testYearWithLocalDate() {
     reservationFilterView = new ReservationFilterView(2012);
    assertThat(reservationFilterView.getStartAsLocalDate(),
        is(new LocalDate().withYear(2012).withMonthOfYear(DateTimeConstants.JANUARY).withDayOfMonth(01)));
 
    assertThat(reservationFilterView.getEndAsLocalDate(),
        is(new LocalDate().withYear(2012).withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(31)));
 
     assertThat(Lists.newArrayList(reservationFilterView.getStates()), hasItems(ReservationStatus.values()));
   }
 
   @Test
   public void testCommingPeriod() {
     DateTime now = DateTime.now();
 
     try {
       DateTimeUtils.setCurrentMillisFixed(now.toDateTime().getMillis());
       reservationFilterView = new ReservationFilterView("testId", "testLabel", Months.THREE, false);
 
       assertThat(reservationFilterView.getStart(), is(now));
       assertThat(reservationFilterView.getEnd(), is(now.plus(Months.THREE)));
      assertThat(Lists.newArrayList(reservationFilterView.getStates()), hasItems(ReservationStatus.TRANSITION_STATES_AS_ARRAY));
     }
     finally {
       DateTimeUtils.setCurrentMillisSystem();
     }
   }
 
   @Test
   public void testElapsedPeriod() {
     DateTime now = DateTime.now();
 
     try {
       DateTimeUtils.setCurrentMillisFixed(now.toDateTime().getMillis());
       reservationFilterView = new ReservationFilterView("testId", "testLabel", Months.THREE, true);
 
       assertThat(reservationFilterView.getStart(), is(now.minus(Months.THREE)));
       assertThat(reservationFilterView.getEnd(), is(now));
       assertThat(Lists.newArrayList(reservationFilterView.getStates()), hasItems(ReservationStatus.values()));
     }
     finally {
       DateTimeUtils.setCurrentMillisSystem();
     }
   }
 
   @Test
   public void testActive() {
     DateTime now = DateTime.now();
 
     try {
       DateTimeUtils.setCurrentMillisFixed(now.toDateTime().getMillis());
       reservationFilterView = new ReservationFilterView("testId", "testLabel", ReservationStatus.RUNNING);
 
       assertThat(reservationFilterView.getStart(), nullValue());
       assertThat(reservationFilterView.getEnd(), nullValue());
       assertThat(Lists.newArrayList(reservationFilterView.getStates()), hasItem(ReservationStatus.RUNNING));
     }
     finally {
       DateTimeUtils.setCurrentMillisSystem();
     }
   }
 
  
 }
