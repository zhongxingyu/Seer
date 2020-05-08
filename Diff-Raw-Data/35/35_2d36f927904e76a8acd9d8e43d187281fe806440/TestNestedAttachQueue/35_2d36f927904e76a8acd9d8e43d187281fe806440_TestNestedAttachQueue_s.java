 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.ftest.a4jAttachQueue;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertFalse;
 
 import java.net.URL;
 
 import org.jboss.cheiron.halt.XHRHalter;
 import org.jboss.test.selenium.encapsulated.JavaScript;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.a4jQueue.QueueAttributes;
 import org.richfaces.tests.metamer.ftest.a4jQueue.QueueModel;
 import org.richfaces.tests.metamer.ftest.a4jQueue.QueueModel.Input;
import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestNestedAttachQueue extends AbstractMetamerTest {
 
     static final Long GLOBAL_DELAY = 10000L;
     static final Long DELAY_A = 3000L;
     static final Long DELAY_B = 5000L;
 
     QueueModel queue = new QueueModel();
 
     AttachQueueAttributes attributesAttachQueue1 = new AttachQueueAttributes(pjq("table.attributes[id$=attributes1]"));
     AttachQueueAttributes attributesAttachQueue2 = new AttachQueueAttributes(pjq("table.attributes[id$=attributes2]"));
     QueueAttributes attributesQueue1 = new QueueAttributes(pjq("table.attributes[id$=queueAttributes]"));
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/a4jAttachQueue/nested.xhtml");
     }
 
     @BeforeMethod
     public void setupDelays() {
         attributesAttachQueue1.setRequestDelay(DELAY_A);
         attributesAttachQueue2.setRequestDelay(DELAY_B);
         attributesQueue1.setRequestDelay(GLOBAL_DELAY);
     }
 
     @Test
     public void testDelay() {
         queue.initializeTimes();
 
         queue.fireEvent(Input.FIRST, 1);
 
         queue.checkTimes(Input.FIRST, DELAY_A);
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RFPL-1186")
     public void testNoDelay() {
         attributesAttachQueue1.setRequestDelay(0);
 
         queue.initializeCounts();
 
         XHRHalter.enable();
 
         queue.fireEvent(Input.FIRST, 4);
         queue.checkCounts(4, 0, 1, 0);
 
         XHRHalter halter = XHRHalter.getHandleBlocking();
         queue.checkCounts(4, 0, 1, 0);
 
         halter.complete();
         queue.checkCounts(4, 0, 2, 1);
 
         halter.waitForOpen();
         queue.checkCounts(4, 0, 2, 1);
 
         halter.complete();
         queue.checkCounts(4, 0, 2, 2);
     }
 
     @Test
    @IssueTracking("https://issues.jboss.org/browse/RFPL-1186")
     public void testTimingOneQueueTwoEvents() {
         queue.initializeTimes();
 
         XHRHalter.enable();
 
         queue.fireEvent(Input.FIRST, 3);
         queue.fireEvent(Input.SECOND, 1);
 
         XHRHalter halter = XHRHalter.getHandleBlocking();
         halter.complete();
         halter.waitForOpen();
         halter.complete();
 
         queue.checkTimes(Input.SECOND, DELAY_B);
         queue.checkNoDelayBetweenEvents();
     }
 
     @Test
     public void testRendered() {
         attributesAttachQueue1.setRequestDelay(1500);
         attributesAttachQueue1.setOnRequestQueue(JavaScript.js("alert('requestQueued')"));
         attributesAttachQueue1.setOnRequestDequeue(JavaScript.js("alert('requestDequeued')"));
         attributesAttachQueue1.setRendered(false);
 
         queue.initializeTimes();
         queue.fireEvent(1);
 
         // check that no requestDelay is applied while renderer=false
         queue.checkTimes(0);
         assertFalse(selenium.isAlertPresent());
     }
 }
