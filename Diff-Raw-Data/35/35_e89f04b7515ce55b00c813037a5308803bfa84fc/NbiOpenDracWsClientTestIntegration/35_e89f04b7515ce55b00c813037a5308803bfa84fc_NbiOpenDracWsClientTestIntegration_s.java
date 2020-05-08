 /**
  * Copyright (c) 2012, 2013 SURFnet BV
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
 package nl.surfnet.bod.nbi.opendrac;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.AppConfiguration;
 import nl.surfnet.bod.config.IntegrationDbConfiguration;
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.nbi.NbiClient;
 import nl.surfnet.bod.nbi.PortNotAvailableException;
 import nl.surfnet.bod.nbi.opendrac.NbiOpenDracWsClient;
 import nl.surfnet.bod.util.TestHelper;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.annotation.DirtiesContext.ClassMode;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { AppConfiguration.class, IntegrationDbConfiguration.class })
 @DirtiesContext(classMode = ClassMode.AFTER_CLASS)
 public class NbiOpenDracWsClientTestIntegration {
 
  @Resource
  private NbiOpenDracWsClient subject;
 
   @BeforeClass
   public static void testEnvironment() {
     TestHelper.useAccEnv();
   }
 
   @AfterClass
   public static void clearEnvironment() {
     TestHelper.clearEnv();
   }
 
   @Test
   public void testFindAllPhysicalPorts() throws PortNotAvailableException {
     List<PhysicalPort> allPorts = subject.findAllPhysicalPorts();
 
     assertThat(allPorts, hasSize(greaterThan(0)));
   }
 
   @Test
   public void testFindPhysicalPortByNmsPortId() throws PortNotAvailableException {
     PhysicalPort firstPort = subject.findAllPhysicalPorts().get(0);
 
     PhysicalPort foundPort = subject.findPhysicalPortByNmsPortId(firstPort.getNmsPortId());
 
     assertThat(foundPort.getNmsPortId(), is(firstPort.getNmsPortId()));
   }
 
   @Test(expected = PortNotAvailableException.class)
   public void findNonExistingPortByNmsIdShouldThrowUp() throws PortNotAvailableException {
     subject.findPhysicalPortByNmsPortId("nonExisting");
   }
 
   @Test
   public void portCountShouldMatchSizeOfAllPorts() {
     long count = subject.getPhysicalPortsCount();
     List<PhysicalPort> ports = subject.findAllPhysicalPorts();
 
     assertThat(count, is((long) ports.size()));
   }
 
   @Test
   public void testRequireVlanIdWhenPortIdContainsLabel() {
     for (PhysicalPort port : subject.findAllPhysicalPorts()) {
       assertThat(port.toString(), port.isVlanRequired(),
           not(port.getBodPortId().toLowerCase().contains(NbiClient.VLAN_REQUIRED_SELECTOR)));
     }
   }
 
 }
