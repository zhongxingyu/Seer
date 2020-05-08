 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod;
 
 import nl.surfnet.bod.support.ReservationFilterViewFactory;
 import nl.surfnet.bod.support.TestExternalSupport;
 
 import org.joda.time.LocalDateTime;
 import org.junit.Before;
 import org.junit.Test;
 
 public class MovePhysicalPortTestSelenium extends TestExternalSupport {
 
   @Before
   public void setup() {
     getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP, "test@example.com");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().switchToNocRole();
 
     getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SARA, ICT_MANAGERS_GROUP_2, "test@example.com");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().switchToNocRole();
 
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "First port", GROUP_SURFNET);
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Second port", GROUP_SARA);
 
     getNocDriver().switchToUserRole();
     getUserDriver().requestVirtualPort("Selenium users");
     getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 1");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().createVirtualPort("First port");
 
     getManagerDriver().switchToUserRole();
     getUserDriver().requestVirtualPort("Selenium users");
     getUserDriver().selectInstituteAndRequest(GROUP_SARA, 1200, "port 2");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().createVirtualPort("Second port");
 
     getManagerDriver().switchToUserRole();
     getUserDriver().createNewReservation("First reservation", LocalDateTime.now().plusDays(1),
         LocalDateTime.now().plusDays(1).plusHours(2));
     getUserDriver().createNewReservation("Second reservation", LocalDateTime.now().plusDays(2),
         LocalDateTime.now().plusDays(2).plusHours(5));
    getUserDriver().verifyAndWaitForReservationIsAutoStart("First reservation");
     getUserDriver().verifyAndWaitForReservationIsAutoStart("Second reservation");
   }
 
   @Test
   public void moveAPhysicalPort() {
     getUserDriver().switchToNocRole();
 
     getNocDriver().movePhysicalPort("First port");
 
     getNocDriver().verifyMovePage(NMS_PORT_ID_1, GROUP_SURFNET, 1, 2, 2);
 
     getNocDriver().movePhysicalPortChooseNewPort(NMS_PORT_ID_3);
 
     getNocDriver().verifyMoveResultPage(2);
 
     // Two reservations should appear in Active filter, since they have a
     // transient state
     getNocDriver().verifyReservationByFilterAndSearch(ReservationFilterViewFactory.COMING, null,
         "First reservation", "Second reservation");
 
     // Four reservations should appear in 2012 filter, the two new above and the
     // two cancelled old ones.
     getNocDriver().verifyReservationByFilterAndSearch("" + LocalDateTime.now().plusDays(1).getYear(), null,
         "First reservation", "First reservation", "Second reservation", "Second reservation");
   }
 
 }
