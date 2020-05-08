 package org.motechproject.ghana.national.handler;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.exception.EventHandlerException;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.scheduletracking.api.events.MilestoneEvent;
 import org.motechproject.scheduletracking.api.events.constants.EventSubjects;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.util.HashMap;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
 
 public class ScheduleHandlerTest {
 
     private ScheduleHandler scheduleHandler;
 
     @Mock
     private CareScheduleHandler careScheduleHandler;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         scheduleHandler = new ScheduleHandler();
         ReflectionTestUtils.setField(scheduleHandler, "careScheduleHandler", careScheduleHandler);
     }
 
     @Test
     public void shouldHandleDeliveryNotificationSchedules(){
         fireScheduleHandler(event(ANC_DELIVERY.getName()));
         verify(careScheduleHandler).handlePregnancyAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
     public void shouldHandleTTVaccinationSchedules(){
         fireScheduleHandler(event(TT_VACCINATION.getName()));
         verify(careScheduleHandler).handleTTVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
     public void shouldHandleBCGSchedules(){
         fireScheduleHandler(event(CWC_BCG.getName()));
         verify(careScheduleHandler).handleBCGAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
     public void shouldHandleIPTpVaccinationSchedules(){
         fireScheduleHandler(event(ANC_IPT_VACCINE.getName()));
         verify(careScheduleHandler).handleIPTpVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
     
     @Test
     public void shouldHandleIPTiVaccinationSchedules(){
         fireScheduleHandler(event(CWC_IPT_VACCINE.getName()));
         verify(careScheduleHandler).handleIPTiVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
     
     @Test
     public void shouldHandleMeaslesVaccinationSchedules(){
         fireScheduleHandler(event(CWC_MEASLES_VACCINE.getName()));
         verify(careScheduleHandler).handleMeaslesVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
     public void shouldHandleYellowFeverVaccinationSchedules(){
         fireScheduleHandler(event(CWC_YELLOW_FEVER.getName()));
         verify(careScheduleHandler).handleYellowFeverVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
     public void shouldHandlePentaVaccinationSchedules(){
         fireScheduleHandler(event(CWC_PENTA.getName()));
         verify(careScheduleHandler).handlePentaVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
     public void shouldHandleRotavirusVaccinationSchedules(){
         fireScheduleHandler(event(CWC_ROTAVIRUS.getName()));
         verify(careScheduleHandler).handleRotavirusVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
     public void shouldHandleOPV0VaccinationSchedules(){
         fireScheduleHandler(event(CWC_OPV_0.getName()));
         verify(careScheduleHandler).handleOpvVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
     public void shouldHandleOtherOPVVaccinationSchedules(){
         fireScheduleHandler(event(CWC_OPV_OTHERS.getName()));
         verify(careScheduleHandler).handleOpvVaccinationAlert(Matchers.<MilestoneEvent>any());
     }
 
     @Test
    public void shouldHandlePNCChildSchedules(){
        fireScheduleHandler(event(PNC_CHILD_1.getName()));
        verify(careScheduleHandler).handlePNCChildAlert(Matchers.<MilestoneEvent>any());
        reset(careScheduleHandler);
        fireScheduleHandler(event(PNC_CHILD_2.getName()));
        verify(careScheduleHandler).handlePNCChildAlert(Matchers.<MilestoneEvent>any());
        reset(careScheduleHandler);
        fireScheduleHandler(event(PNC_CHILD_3.getName()));
        verify(careScheduleHandler).handlePNCChildAlert(Matchers.<MilestoneEvent>any());
    }
    @Test
    public void shouldHandlePNCMotherSchedules(){
        fireScheduleHandler(event(PNC_MOTHER_1.getName()));
        verify(careScheduleHandler).handlePNCMotherAlert(Matchers.<MilestoneEvent>any());
        reset(careScheduleHandler);
        fireScheduleHandler(event(PNC_MOTHER_1.getName()));
        verify(careScheduleHandler).handlePNCMotherAlert(Matchers.<MilestoneEvent>any());
        reset(careScheduleHandler);
        fireScheduleHandler(event(PNC_MOTHER_1.getName()));
        verify(careScheduleHandler).handlePNCMotherAlert(Matchers.<MilestoneEvent>any());
    }

    @Test
     public void shouldThrowOnAnyFailureInHandlingAlerts() {
         doThrow(new RuntimeException("some")).when(careScheduleHandler).handleOpvVaccinationAlert(Matchers.<MilestoneEvent>any());
         final MotechEvent event = event(CWC_OPV_OTHERS.getName());
         try {
             fireScheduleHandler(event);
             Assert.fail("expected scheduler handler exception");
         } catch (EventHandlerException she) {
             assertThat(she.getMessage(), is(event.toString()));
         }
     }
 
     private void fireScheduleHandler(MotechEvent event) {
         scheduleHandler.handleAlert(event);
     }
 
     private MotechEvent event(final String scheduleName) {
         return new MotechEvent(EventSubjects.MILESTONE_ALERT, new HashMap<String, Object>(){{
             put("schedule_name", scheduleName);
         }});
     }
 
 
 }
