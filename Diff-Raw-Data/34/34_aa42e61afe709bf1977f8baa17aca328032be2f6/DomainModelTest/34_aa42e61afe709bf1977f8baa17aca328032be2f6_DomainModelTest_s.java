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
 package nl.surfnet.bod.domain;
 
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualPortRequestLinkFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
 
 public class DomainModelTest {
 
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
   private final ReservationFactory reservationFactory = new ReservationFactory();
   private final PhysicalResourceGroupFactory prgFactory = new PhysicalResourceGroupFactory();
   private final VirtualResourceGroupFactory vrgFactory = new VirtualResourceGroupFactory();
   private final VirtualPortFactory vpFactory = new VirtualPortFactory();
   private final PhysicalPortFactory ppFactory = new PhysicalPortFactory();
   private final VirtualPortRequestLinkFactory vprlFactory = new VirtualPortRequestLinkFactory();
 
   private Reservation reservation;
   private Reservation reservationTwo;
   private VirtualPortRequestLink link;
   private VirtualResourceGroup vrg;
 
   @Before
   public void onSetup() {
     PhysicalPort pp1 = ppFactory.create();
     PhysicalPort pp2 = ppFactory.create();
     PhysicalResourceGroup prg = prgFactory.addPhysicalPort(pp1, pp2).create();
     pp1.setPhysicalResourceGroup(prg);
     pp2.setPhysicalResourceGroup(prg);
 
     vrg = vrgFactory.create();
     VirtualPort vp1 = vpFactory.setVirtualResourceGroup(vrg).create();
     VirtualPort vp2 = vpFactory.setVirtualResourceGroup(vrg).create();
     vrg.setVirtualPorts(Lists.newArrayList(vp1, vp2));
 
     reservationFactory.setSourcePort(vp1);
     reservationFactory.setDestinationPort(vp2);
     reservation = reservationFactory.create();
     reservationTwo = reservationFactory.create();
 
     reservation.setVirtualResourceGroup(vrg);
     reservationTwo.setVirtualResourceGroup(vrg);
     vrg.setReservations(Lists.newArrayList(reservation, reservationTwo));
 
     link = vprlFactory.setVirtualResourceGroup(vrg).create();
     vrg.setVirtualPortRequestLinks(Lists.newArrayList(link));
   }
 
   /**
    * Bidirectional relations between the domains are treaded different in the
    * specific toString methods, to prevent stackoverFlow.
    */
   @Test
   public void shouldNotOverflowInReservationToString() {
     logger.info(reservation.toString());
   }
 
   /**
    * Bidirectional relations between the domains are treaded different in the
    * specific toString methods, to prevent stackoverFlow.
    */
   @Test
   public void shouldNotOverflowInVirtualPortRequestLinkToString() {
     logger.info(link.toString());
   }
 
   /**
    * Bidirectional relations between the domains are treaded different in the
    * specific equal methods, to prevent stackoverFlow.
    */
   @Test
   public void shouldNotOverflowInReservationEquals() {
     assertTrue(reservation.equals(reservationTwo));
   }
 
   /**
    * Bidirectional relations between the domains are treaded different in the
    * specific equal methods, to prevent stackoverFlow.
    */
   @Test
   public void shouldNotOverflowInReservationHashCode() {
     assertThat(reservation.hashCode(), is(reservationTwo.hashCode()));
   }
 
   /**
    * Bidirectional relations between the domains are treaded different in the
    * specific equal methods, to prevent stackoverFlow.
    */
   @Test
   public void shouldOnlyConsiderIdInVirtualPortRequestLinkEquals() {
     VirtualPortRequestLink requestLink = new VirtualPortRequestLink();
     requestLink.setId(link.getId());
     assertTrue("Only on Id", link.equals(requestLink));
   }
 
   /**
    * Bidirectional relations between the domains are treaded different in the
    * specific equal methods, to prevent stackoverFlow.
    */
   @Test
   public void shouldOnlyConsiderIdInVirtualPortRequestLinkHashCode() {
     VirtualPortRequestLink requestLink = new VirtualPortRequestLink();
     requestLink.setId(link.getId());
     assertThat("Only on Id", link.hashCode(), is(requestLink.hashCode()));
   }
 
   /**
    * Bidirectional relations between the domains are treaded different in the
    * specific equal methods, to prevent stackoverFlow.
    */
   @Test
   public void shouldOnlyConsiderIdInVirtualResourceGroupEquals() {
     VirtualResourceGroup vrgTwo = new VirtualResourceGroup();
     vrgTwo.setId(vrg.getId());
     assertTrue("Only on Id", vrg.equals(vrgTwo));
   }
 
   @Test
   public void shouldOnlyconsiderIdInVirturalResourceGroupHashCode() {
     VirtualResourceGroup vrgTwo = new VirtualResourceGroup();
     vrgTwo.setId(vrg.getId());
     assertTrue("Only on Id", vrg.hashCode() == vrgTwo.hashCode());
   }
 }
