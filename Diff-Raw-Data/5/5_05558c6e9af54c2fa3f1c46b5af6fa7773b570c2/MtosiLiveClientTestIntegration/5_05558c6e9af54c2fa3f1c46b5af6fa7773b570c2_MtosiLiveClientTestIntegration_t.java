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
 package nl.surfnet.bod.mtosi;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 import org.tmforum.mtop.nrf.xsd.invdata.v1.InventoryDataType;
 import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagedElementInventoryType;
 import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagementDomainInventoryType;
 
 import com.google.common.collect.Iterables;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 
 public class MtosiLiveClientTestIntegration {
 
   private MtosiLiveClient mtosiLiveClient;
 
   @Before
   public void init() throws IOException {
     final Properties props = new Properties();
     props.load(new ClassPathResource("bod-default.properties").getInputStream());
 
     mtosiLiveClient = new MtosiLiveClient(props.getProperty("mtosi.inventory.retrieval.endpoint"),
         "http://atlas.dlp.surfnet.nl");
     mtosiLiveClient.init();
   }
 
   @Test
  public void retrieveInventory() {
     final InventoryDataType inventory = mtosiLiveClient.getInventory();
     assertThat(inventory, notNullValue());
     final List<ManagementDomainInventoryType> mdits = inventory.getMdList().getMd();
     assertThat(mdits, hasSize(1));
     final ManagementDomainInventoryType mdit = Iterables.getOnlyElement(mdits);
     final List<ManagedElementInventoryType> meits = mdit.getMeList().getMeInv();
     assertThat(meits, hasSize(greaterThan(0)));
   }
 
   @Test
   public void getUnallocatedPorts() {
     final List<PhysicalPort> unallocatedPorts = mtosiLiveClient.getUnallocatedPorts();
    System.out.println(unallocatedPorts);
    System.out.println(unallocatedPorts.size());
     assertThat(unallocatedPorts, hasSize(greaterThan(0)));
   }
 
   @Test
   public void convertPortName() {
     final String mtosiPortName = "/rack=1/shelf=1/slot=1/port=48";
     final String expectedPortName = "1-1-1-48";
     final String convertedPortName = mtosiLiveClient.convertPortName(mtosiPortName);
     assertThat(convertedPortName, equalTo(expectedPortName));
   }
 }
