 package org.motechproject.ananya.referencedata.web.controller;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.ananya.referencedata.contactCenter.service.LocationService;
 import org.motechproject.ananya.referencedata.csv.CsvImporter;
 import org.motechproject.ananya.referencedata.flw.domain.Location;
 import org.motechproject.ananya.referencedata.flw.domain.LocationStatus;
 import org.motechproject.ananya.referencedata.web.domain.CsvUploadRequest;
 import org.springframework.test.web.server.MvcResult;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Arrays;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.motechproject.ananya.referencedata.web.utils.MVCTestUtils.mockMvc;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
 
 @RunWith(MockitoJUnitRunner.class)
 public class HomeControllerTest {
 
     @Mock
     private LocationService locationService;
     @Mock
     private CsvImporter csvImporter;
 
     private HomeController homeController;
 
     @Before
     public void setup(){
         homeController = new HomeController(locationService, csvImporter);
     }
 
     @Test
     public void shouldReturnLocationsAsCSV() throws Exception {
         Location location = new Location("d", "b", "p", LocationStatus.NOT_VERIFIED, null);
         when(locationService.getLocationsToBeVerified()).thenReturn(Arrays.asList(location));
 
        MvcResult mvcResult = mockMvc(homeController).perform(get("/admin/home/download"))
                 .andExpect(status().isOk()).andReturn();
 
         String contentAsString = mvcResult.getResponse().getContentAsString();
         assertTrue(contentAsString.contains("d,b,p"));
     }
 
     @Test
     public void shouldThrowExceptionOnError() throws Exception {
         when(locationService.getLocationsToBeVerified()).thenThrow(new RuntimeException("aragorn"));
 
        MvcResult mvcResult = mockMvc(homeController).perform(get("/admin/home/download"))
                 .andExpect(status().is(500)).andReturn();
 
         assertEquals("/admin/popup-error",mvcResult.getResponse().getForwardedUrl());
     }
 
     @Test
     public void shouldUploadLocationsFile() throws IOException {
         CommonsMultipartFile fileData = mock(CommonsMultipartFile.class);
         HttpServletResponse response = mock(HttpServletResponse.class);
         ServletOutputStream outputStream = mock(ServletOutputStream.class);
         byte[] bytes = new byte[1];
         CsvUploadRequest csvFileRequest = new CsvUploadRequest(fileData);
         when(response.getOutputStream()).thenReturn(outputStream);
         when(csvImporter.importLocation(fileData)).thenReturn(bytes);
 
         homeController.uploadLocations(csvFileRequest, response);
 
         verify(outputStream).write(bytes);
         verify(response).setHeader("Content-Disposition",
                 "attachment; filename=errors.csv");
         verify(outputStream).flush();
     }
 }
