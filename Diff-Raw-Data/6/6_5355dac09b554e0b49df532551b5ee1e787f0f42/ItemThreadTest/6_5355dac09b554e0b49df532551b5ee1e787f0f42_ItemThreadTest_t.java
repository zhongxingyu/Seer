 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.sb;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
import de.escidoc.core.test.common.client.servlet.Constants;

 /**
  * Test the implementation of the item resource threaded.
  * 
  * @author MIH
  * 
  */
 @RunWith(value = Parameterized.class)
 public class ItemThreadTest extends SearchTestBase {
 
     /**
      * @param transport
      *            The transport identifier.
      */
     public ItemThreadTest(final int transport) {
         super(transport);
     }
 
     /**
      * Set up servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Before
     public void initialize() throws Exception {
     }
 
     /**
      * Clean up after servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
     }
 
     /**
      * Test the search.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Ignore
     public void notestThreads() throws Exception {
         int threadCount = 5;
         Thread[] ts = new Thread[threadCount];
         for (int i = 0; i < threadCount; i++) {
             ItemThreadRunnable runnable = new ItemThreadRunnable();
             ts[i] = new Thread(runnable);
             ts[i].start();
         }
         for (int threadlength = 0; threadlength < ts.length; threadlength++) {
             try {
                 ts[threadlength].join();
             }
             catch (Exception e) {
             }
 
         }
     }
 
     /**
      * Test the search.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testServlet() throws Exception {
         HttpRequester requester =
            new HttpRequester(Constants.PROTOCOL + "://"
                + Constants.HOST_PORT + "/fedoradeviation/describe", "mih:mih");
         String response = requester.doGet("");
     }
 
 }
