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
 package nl.surfnet.bod.web.noc;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.when;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
 
 import java.util.Collection;
 
 import nl.surfnet.bod.matchers.NumberOfLinesMatchers;
 import nl.surfnet.bod.service.ReportingService;
 import nl.surfnet.bod.web.view.ReportIntervalView;
 import nl.surfnet.bod.web.view.ReservationReportView;
 
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 import org.joda.time.YearMonth;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.test.web.servlet.MockMvc;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ReportControllerTest {
 
   @InjectMocks
   private ReportController subject;
 
   @Mock
   private ReportingService reportServiceMock;
 
   private MockMvc mockMvc;
 
   @Before
   public void setup() {
     mockMvc = standaloneSetup(subject).build();
   }
 
   @Test
   public void graphDataShouldReturnCsvFile() throws Exception {
     ReservationReportView dummyReport = new ReservationReportView(DateTime.now().minusMonths(1), DateTime.now());
 
     when(reportServiceMock.determineReportForNoc(any(Interval.class))).thenReturn(dummyReport);
 
    YearMonth month = new YearMonth(2011, 10);
    mockMvc.perform(get("/noc/report/data/" + month.toString("yyyyMM")))
       .andExpect(status().isOk())
       .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
       .andExpect(content().string(containsString("Month,Create,Create_f,Cancel,Cancel_f,NSI,NSI_f")))
      .andExpect(content().string(containsString(month.toString("MMM"))))
      .andExpect(content().string(containsString(month.minusMonths(ReportController.MONTHS_IN_GRAPH - 1).toString("MMM"))))
       .andExpect(content().string(NumberOfLinesMatchers.hasLines(ReportController.MONTHS_IN_GRAPH + 1)));
   }
 
   @Test
   public void graphDataShouldReturnWhenWrongDateIsGiven() throws Exception {
     ReservationReportView dummyReport = new ReservationReportView(DateTime.now().minusMonths(1), DateTime.now());
 
     when(reportServiceMock.determineReportForNoc(any(Interval.class))).thenReturn(dummyReport);
 
     mockMvc.perform(get("/noc/report/data/220a"))
       .andExpect(status().isOk())
       .andExpect(content().string(containsString("Month,Create,Create_f,Cancel,Cancel_f,NSI,NSI_f")));
   }
 
   @Test
   public void shouldShowGraph() throws Exception {
     mockMvc.perform(get("/noc/report/graph"))
       .andExpect(status().isOk())
       .andExpect(model().attribute("dataUrl", is("noc/report/data/" + YearMonth.now().toString("yyyyMM"))))
       .andExpect(view().name("noc/report/graph"));
   }
 
   @Test
   public void shouldShowGraphWithGivenInterval() throws Exception {
     mockMvc.perform(get("/noc/report/graph/201005"))
       .andExpect(status().isOk())
       .andExpect(model().attribute("dataUrl", is("noc/report/data/201005")))
       .andExpect(view().name("noc/report/graph"));
   }
 
   @Test
   public void shouldShowReport() throws Exception {
     ReservationReportView dummyReport = new ReservationReportView(DateTime.now().minusMonths(1), DateTime.now());
 
     when(reportServiceMock.determineReportForNoc(any(Interval.class))).thenReturn(dummyReport);
 
     mockMvc.perform(get("/noc/report"))
       .andExpect(status().isOk())
       .andExpect(model().<Collection<?>>attribute("intervalList", hasSize(ReportController.AMOUNT_OF_REPORT_PERIODS)))
       .andExpect(model().attribute("selectedInterval", is(new ReportIntervalView(YearMonth.now().toInterval()))))
       .andExpect(model().attribute("baseReportIntervalUrl", is("noc/report")))
       .andExpect(model().attribute("graphUrlPart", is("graph/" + DateTime.now().toString("yyyyMM"))));
   }
 
   @Test
   public void shouldShowReportDataForSpecificPeriod() throws Exception {
     ReservationReportView dummyReport = new ReservationReportView(DateTime.now().minusMonths(1), DateTime.now());
 
     when(reportServiceMock.determineReportForNoc(any(Interval.class))).thenReturn(dummyReport);
 
     mockMvc.perform(get("/noc/report/201101"))
       .andExpect(status().isOk())
       .andExpect(model().attribute("selectedInterval", is(new ReportIntervalView(new YearMonth(2011, 1).toInterval()))))
       .andExpect(model().attribute("graphUrlPart", is("graph/201101")));
   }
 }
