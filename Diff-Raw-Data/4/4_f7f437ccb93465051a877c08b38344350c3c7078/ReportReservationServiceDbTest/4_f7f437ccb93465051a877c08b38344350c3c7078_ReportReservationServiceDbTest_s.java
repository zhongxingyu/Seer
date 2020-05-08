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
 
 import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
 import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
 import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
 import static nl.surfnet.bod.web.WebUtils.not;
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.nbi.NbiOfflineClient;
 import nl.surfnet.bod.repo.ReservationRepo;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeUtils;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.AfterTransaction;
 import org.springframework.test.context.transaction.BeforeTransaction;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @TransactionConfiguration(defaultRollback = true)
 @ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml",
     "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml", "/spring/appCtx-vers-client.xml" })
 @Transactional
 public class ReportReservationServiceDbTest {
   private final static long AMOUNT_OF_RESERVATIONS = 8;
 
   // override bod.properties to run test and bod server at the same time
   static {
     System.setProperty("snmp.host", "localhost/1622");
   }
 
   private final Logger logger = LoggerFactory.getLogger(getClass());
 
   @Resource
   private ReservationServiceDbTestHelper reservationHelper;
 
   @Resource
   private ReservationRepo reservationRepo;
 
   @Resource
   private NbiOfflineClient nbiClient;
 
   @Resource
   private ReservationService subject;
 
   private DateTime periodStart;
   private DateTime periodEnd;
 
   private Reservation reservationOnStartPeriodNSI;
   private Reservation reservationOnEndPeriod;
   private Reservation reservationBeforeStartAndAfterEndPeriodNSI;
   private Reservation reservationBeforeStartAndOnEndPeriodGUI;
   private Reservation reservationAfterStartAndOnEndPeriodGUI;
   private Reservation reservationAfterStartAndAfterEndPeriodNSI;
 
   private Reservation reservationBeforePeriodNSI;
   private Reservation reservationInPeriodGUI;
   private Reservation reservationAfterPeriodGUI;
 
   private final List<String> adminGroups = new ArrayList<>();
 
   @BeforeClass
   public static void init() {
     DataBaseTestHelper.clearIntegrationDatabaseSkipBaseData();
   }
 
   @BeforeTransaction
   public void setUp() {
     periodStart = DateTime.now().plusDays(2).plusHours(1).withSecondOfMinute(0).withMillisOfSecond(0);
     periodEnd = periodStart.plusDays(1);
 
     logger.warn("Start of period [{}], end [{}]", periodStart, periodEnd);
     // Speed up setup time
     nbiClient.setShouldSleep(false);
 
     // Five (4) reservations in reporting period, 2 GUI and 2 NSI
     reservationAfterStartAndOnEndPeriodGUI = createAndSaveReservation(periodStart.plusHours(1), periodEnd,
         ReservationStatus.REQUESTED, true);
 
     reservationInPeriodGUI = createAndSaveReservation(periodStart.plusHours(1), periodEnd.minusHours(1),
         ReservationStatus.REQUESTED, true);
 
     reservationOnStartPeriodNSI = createAndSaveReservation(periodStart, periodEnd.plusDays(1),
         ReservationStatus.REQUESTED, false);
 
     reservationAfterStartAndAfterEndPeriodNSI = createAndSaveReservation(periodStart.plusHours(1), periodEnd
         .plusDays(1), ReservationStatus.REQUESTED, false);
 
     // Two (2) reservations related to reporting period, 1 GUI and 1 NSI
     reservationBeforeStartAndOnEndPeriodGUI = createAndSaveReservation(periodStart.minusHours(1), periodEnd,
         ReservationStatus.REQUESTED, true);
 
     reservationBeforeStartAndAfterEndPeriodNSI = createAndSaveReservation(periodStart.minusHours(1), periodEnd
         .plusDays(1), ReservationStatus.REQUESTED, false);
 
     // Two (2) reservations not related to reporting period, 1 GUI and 1 NSI
     reservationAfterPeriodGUI = createAndSaveReservation(periodEnd.plusHours(1), periodEnd.plusDays(1),
         ReservationStatus.REQUESTED, true);
 
     reservationBeforePeriodNSI = createAndSaveReservation(periodStart.minusDays(1), periodStart.minusHours(1),
         ReservationStatus.REQUESTED, false);
   }
 
   @AfterTransaction
   public void teardown() {
     DataBaseTestHelper.clearIntegrationDatabaseSkipBaseData();
   }
 
   @Test
   public void checkSetup() {
     long amountOfReservations = subject.count();
 
     assertThat(amountOfReservations, is(AMOUNT_OF_RESERVATIONS));
   }
 
   @Test
   public void shouldCountExistingStateInPeriodGUI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart, periodEnd, adminGroups,
         ReservationStatus.AUTO_START);
 
     assertThat(count, is(2L));
   }
 
   @Test
   public void shouldCountExistingStateInPeriodNSI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart, periodEnd, adminGroups,
         ReservationStatus.RESERVED);
 
     assertThat(count, is(2L));
   }
 
   @Test
   public void shouldCountExistingStateInPeriodGUIAndNSI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart, periodEnd, adminGroups,
         ReservationStatus.RESERVED, AUTO_START);
 
     assertThat(count, is(4L));
   }
 
   @Test
   public void shouldCountExistingStateBeforePeriodOnCornerGUI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart
         .minusHours(1), adminGroups, ReservationStatus.AUTO_START);
 
     assertThat(count, is(1L));
   }
 
   @Test
   public void shouldCountExistingStateBeforePeriodOnCornerNSI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart
         .minusHours(1), adminGroups, ReservationStatus.RESERVED);
 
     assertThat(count, is(2L));
   }
 
   @Test
   public void shouldCountExistingStateBeforePeriodOnCornerGUIAndNSI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart
         .minusHours(1), adminGroups, ReservationStatus.RESERVED, AUTO_START);
 
     assertThat(count, is(3L));
   }
 
   @Test
   public void shouldCountExistingStateBeforePeriodGUI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart
         .minusHours(2), adminGroups, ReservationStatus.AUTO_START);
 
     assertThat(count, is(0L));
   }
 
   @Test
   public void shouldCountExistingStateBeforePeriodNSI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart
         .minusHours(2), adminGroups, ReservationStatus.RESERVED);
 
     assertThat(count, is(1L));
   }
 
   @Test
   public void shouldCountExistingStateBeforePeriodGUIAndNSI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodStart.minusDays(2), periodStart
         .minusHours(2), adminGroups, ReservationStatus.RESERVED, AUTO_START);
 
     assertThat(count, is(1L));
   }
 
   @Test
   public void shouldCountExistingStateAfterPeriodGUI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodEnd.plusHours(1), periodEnd
         .plusDays(3), adminGroups, ReservationStatus.AUTO_START);
 
     assertThat(count, is(1L));
   }
 
   @Test
   public void shouldCountExistingStateAfterPeriodNSI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodEnd.plusHours(1), periodEnd
         .plusDays(3), adminGroups, ReservationStatus.RESERVED);
 
     assertThat(count, is(0L));
   }
 
   @Test
   public void shouldCountExistingStateAfterPeriodGUIAndNSI() {
     long count = subject.countReservationsBetweenWhichHadStateInAdminGroups(periodEnd.plusHours(1), periodEnd
         .plusDays(3), adminGroups, ReservationStatus.RESERVED, AUTO_START);
 
     assertThat(count, is(1L));
   }
 
   @Test
   public void shouldCountExsitingTransitionInPeriodGUI() {
     long count = subject.countReservationsWhichHadStateTransitionBetweenInAdminGroups(periodStart, periodEnd,
         REQUESTED, AUTO_START, adminGroups);
 
     assertThat(count, is(2L));
   }
 
   @Test
   public void shouldCountExsitingTransitionInPeriodNSI() {
     long count = subject.countReservationsWhichHadStateTransitionBetweenInAdminGroups(periodStart, periodEnd,
         REQUESTED, RESERVED, adminGroups);
 
     assertThat(count, is(2L));
   }
 
   @Test
   public void shouldCountNonExsitingTransitionInPeriod() {
     long count = subject.countReservationsWhichHadStateTransitionBetweenInAdminGroups(periodStart, periodEnd,
         REQUESTED, ReservationStatus.NOT_ACCEPTED, adminGroups);
 
     assertThat(count, is(0L));
   }
 
   @Test
   public void shouldFindActiveReservationsWithState() {
     long count = subject.countActiveReservationsBetweenWithState(periodStart, periodEnd, AUTO_START, adminGroups);
 
     assertThat(count, is(3L));
 
     subject.updateStatus(reservationInPeriodGUI, ReservationStatus.SUCCEEDED);
     count = subject.countActiveReservationsBetweenWithState(periodStart, periodEnd, AUTO_START, adminGroups);
 
     assertThat("Should count one less because of state change", count, is(2L));
   }
 
   @Test
   public void shouldNotFindActiveReservationsBecauseOfState() {
     long count = subject.countActiveReservationsBetweenWithState(periodStart, periodEnd, REQUESTED, adminGroups);
 
     assertThat(count, is(0L));
   }
 
   @Test
   public void shouldNotFindActiveReservationsBecauseBeforePeriod() {
     long count = subject.countActiveReservationsBetweenWithState(periodStart.minusDays(3), periodStart.minusDays(2),
         AUTO_START, adminGroups);
 
     assertThat(count, is(0L));
   }
 
   @Test
   public void shouldCountCreatesThroughNSI() {
     long count = subject.countReservationsCreatedThroughChannelNSIInAdminGroups(periodStart, periodEnd, adminGroups);
 
     assertThat(count, is(2L));
   }
 
   @Test
   public void shouldCountCancelsThroughNSI() {
     long count = subject.countReservationsCancelledThroughChannelNSIInAdminGroups(periodStart, periodEnd, adminGroups);
 
     assertThat(count, is(0L));
   }
 
   @Test
   public void shouldCountCreatesThroughGUI() {
     long count = subject.countReservationsCreatedThroughChannelGUIInAdminGroups(periodStart, periodEnd, adminGroups);
 
     assertThat(count, is(2L));
   }
 
   @Test
   public void shouldCountCancelsThroughGUI() {
     long count = subject.countReservationsCancelledThroughChannelGUInAdminGroups(periodStart, periodEnd, adminGroups);
 
     assertThat(count, is(0L));
   }
 
   @Test
   public void shouldFindReservationIdsBeforeInAdminGroupsWithState() {
     List<Long> reservationIds = subject.findReservationIdsBeforeOrOnInAdminGroupsWithState(periodStart, adminGroups,
         ReservationStatus.TRANSITION_STATES_AS_ARRAY);
 
     assertThat(reservationIds, hasSize(4));
    assertThat(reservationIds, contains(reservationOnStartPeriodNSI.getId(), reservationBeforeStartAndOnEndPeriodGUI
         .getId(), reservationBeforeStartAndAfterEndPeriodNSI.getId(), reservationBeforePeriodNSI.getId()));
   }
 
   private Reservation createAndSaveReservation(DateTime start, DateTime end, ReservationStatus status,
       boolean autoProvision) {
     // Make sure all events are created with the time related to the reservation
     DateTimeUtils.setCurrentMillisFixed(start.getMillis());
     try {
       Reservation reservation = reservationHelper.createReservation(start, end, status);
 
       reservation = reservationHelper.createThroughService(reservation, autoProvision);
 
       // No autoprovision indicates NSI reservation, so add connection to it
       if (not(autoProvision)) {
         reservation = reservationHelper.addConnectionToReservation(reservation);
       }
 
       return reservation;
     }
     finally {
       DateTimeUtils.setCurrentMillisSystem();
     }
   }
 }
