 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.emr.fragment.controller.paperrecord;
 
 import org.junit.Test;
 import org.openmrs.Location;
 import org.openmrs.module.emr.TestUiUtils;
 import org.openmrs.module.emr.paperrecord.PaperRecordRequest;
 import org.openmrs.module.emr.paperrecord.PaperRecordService;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.fragment.action.FailureResult;
 import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
 import org.openmrs.ui.framework.fragment.action.SuccessResult;
 
import java.util.Date;

 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.hamcrest.Matchers.containsString;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 public class ArchivesRoomFragmentControllerTest {
 
     @Test
     public void testControllerShouldReturnFailureResultIfNoMatchingRequestFound() throws Exception {
 
         ArchivesRoomFragmentController controller = new ArchivesRoomFragmentController();
 
         UiUtils ui = new TestUiUtils();
         PaperRecordService paperRecordService = mock(PaperRecordService.class);
 
         when(paperRecordService.getPendingPaperRecordRequestByIdentifier(eq("123"))).thenReturn(null);
         when(paperRecordService.getSentPaperRecordRequestByIdentifier(eq("123"))).thenReturn(null);
 
         FragmentActionResult result = controller.markPaperRecordRequestAsSent("123", paperRecordService, ui);
 
         assertThat(result, instanceOf(FailureResult.class));
         FailureResult failureResult = (FailureResult) result;
         assertThat(((FailureResult) result).getSingleError(), containsString("123"));
 
     }
 
     @Test
     public void testControllerShouldReturnFailureResultIfSentRequestFound() throws Exception {
 
         ArchivesRoomFragmentController controller = new ArchivesRoomFragmentController();
 
         UiUtils ui = new TestUiUtils();
         PaperRecordService paperRecordService = mock(PaperRecordService.class);
 
         PaperRecordRequest request = new PaperRecordRequest();
         Location location = new Location();
         location.setName("Test location");
         request.setRequestLocation(location);
 
         when(paperRecordService.getPendingPaperRecordRequestByIdentifier(eq("123"))).thenReturn(null);
         when(paperRecordService.getSentPaperRecordRequestByIdentifier(eq("123"))).thenReturn(request);
 
         FragmentActionResult result = controller.markPaperRecordRequestAsSent("123", paperRecordService, ui);
 
         assertThat(result, instanceOf(FailureResult.class));
         FailureResult failureResult = (FailureResult) result;
         assertThat(failureResult.getSingleError(), containsString("123"));
         assertThat(failureResult.getSingleError(), containsString(location.getDisplayString()));
     }
 
     @Test
     public void testControllerShouldMarkRecordAsSent() throws Exception {
 
         ArchivesRoomFragmentController controller = new ArchivesRoomFragmentController();
 
         UiUtils ui = new TestUiUtils();
         PaperRecordService paperRecordService = mock(PaperRecordService.class);
 
         PaperRecordRequest request = new PaperRecordRequest();
         Location location = new Location();
         location.setName("Test location");
         request.setRequestLocation(location);
        request.setDateCreated(new Date());
 
         when(paperRecordService.getPendingPaperRecordRequestByIdentifier(eq("123"))).thenReturn(request);
 
         FragmentActionResult result = controller.markPaperRecordRequestAsSent("123", paperRecordService, ui);
 
         verify(paperRecordService).markPaperRequestRequestAsSent(request);
         assertThat(result, instanceOf(SuccessResult.class));
         SuccessResult successResult = (SuccessResult) result;
         assertThat(successResult.getMessage(), containsString("123"));
         assertThat(successResult.getMessage(), containsString("Test location"));
     }
 }
