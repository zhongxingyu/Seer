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
 package nl.surfnet.bod.domain;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.AppConfiguration;
 import nl.surfnet.bod.config.IntegrationDbConfiguration;
 import nl.surfnet.bod.repo.ConnectionV2Repo;
 import nl.surfnet.bod.support.ConnectionV2Factory;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStateChangeRequestType;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import static org.junit.Assert.assertTrue;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { AppConfiguration.class, IntegrationDbConfiguration.class } )
 @Transactional
 @DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles("opendrac-offline")
 public class ConnectionV2TDbTest {
 
   @Resource private ConnectionV2Repo connectionRepo;
 
   @Test
   public void saving_a_connection_with_notifications_should_work() {
     ConnectionV2 connection = new ConnectionV2Factory().create();
 
     connection.addNotification(new DataPlaneStateChangeRequestType().withConnectionId(connection.getConnectionId()).withNotificationId(0));
     ConnectionV2 savedConnection = connectionRepo.save(connection);
 
     assertTrue("the two may not be the same", connection != savedConnection);
     assertTrue(savedConnection.getNotifications().size() > 0);
     savedConnection.addNotification(new DataPlaneStateChangeRequestType().withConnectionId(connection.getConnectionId()).withNotificationId(1));
 
     ConnectionV2 anotherSavedConnection = connectionRepo.save(savedConnection);
     assertTrue(anotherSavedConnection.getNotifications().size() == 2);
   }
 }
