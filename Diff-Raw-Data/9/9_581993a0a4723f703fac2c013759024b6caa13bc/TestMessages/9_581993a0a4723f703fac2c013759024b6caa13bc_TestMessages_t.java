 /*
  * Created on 8 Jan 2008
  */
 package uk.ac.cam.caret.rsf.test.messages;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import uk.org.ponder.dateutil.FieldDateTransit;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsf.bare.ActionResponse;
 import uk.org.ponder.rsf.bare.RenderResponse;
 import uk.org.ponder.rsf.bare.junit.MultipleRSFTests;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /**
  * Test for message propagation issues, and date input infrastructure
  */
 
 public class TestMessages extends MultipleRSFTests {
 
   public TestMessages() {
     contributeRequestConfigLocation("conf/components-requestContext.xml");
     contributeConfigLocation("conf/components-applicationContext.xml");
     contributeRequestConfigLocation("classpath:uk/ac/cam/caret/rsf/test/messages/messages-request-context.xml");
     contributeConfigLocation("classpath:uk/ac/cam/caret/rsf/test/messages/messages-application-context.xml");
   }
 
   public void testSubmitImpl(String value, Date expected, String dateKey, String timeKey,
       String style, int num, boolean late) {
     MessagesViewParams mvp = new MessagesViewParams();
     mvp.invalidDateKey = dateKey;
     mvp.invalidTimeKey = timeKey;
     mvp.inputStyle = style;
 
     RenderResponse render = getRequestLauncher().renderView(mvp);
     UIForm form = (UIForm) render.viewWrapper.queryComponent(new UIForm());
     UIInput dateInput = (UIInput) render.viewWrapper.queryComponent(new UIInput(), "date-field");
     UIInput timeInput = (UIInput) render.viewWrapper.queryComponent(new UIInput(), "time-field");
 
     if (style.equals(FormatAwareDateInputEvolver.DATE_INPUT)) {
       assertEquals(null, timeInput);
     }
     else {
       timeInput.updateValue("09:00");
       if (expected != null) {
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(expected);
         cal.add(Calendar.HOUR, 9);
         expected = cal.getTime();
       }
     }
     
     dateInput.updateValue(value);
 
     ActionResponse response = getRequestLauncher().submitForm(form, null);
     assertActionError(response, expected == null);
 
     DateHolder holder = (DateHolder) response.requestContext.locateBean("dateHolder");
 
     assertEquals(expected, holder.getDate0());
    RenderResponse render2 = 
       getRequestLauncher().renderView((ViewParameters) response.ARIResult.resultingView);
     if (expected == null) {
       TargettedMessageList tml = (TargettedMessageList) response.requestContext
           .locateBean("targettedMessageList");
       assertEquals(1, tml.size());
       TargettedMessage tm = tml.messageAt(0);
       assertEquals(tm.targetid, dateInput.getFullID());
       String code = tm.messagecodes[0];
       if (!late) {
         assertEquals(dateKey == null? FieldDateTransit.INVALID_DATE_KEY : dateKey, code);
       }
      // test for properly scoped message-for branch
      assertContains(render2, "date-branch::rsf-messages::error-messages::");
     }
     else {
       assertContains(render2, "rsf-messages::info-messages::");
     }
 
   }
 
   public void testSubmit(String value, Date expected, boolean late) {
     for (int num = 1; num <= 2; ++num) {
       testSubmitImpl(value, expected, null, null, FormatAwareDateInputEvolver.DATE_INPUT,
           num, late);
       testSubmitImpl(value, expected, "dateKey", null,
           FormatAwareDateInputEvolver.DATE_INPUT, num, late);
       testSubmitImpl(value, expected, "dateKey", null,
           FormatAwareDateInputEvolver.DATE_TIME_INPUT, num, late);
       testSubmitImpl(value, expected, "dateKey", "timeKey",
           FormatAwareDateInputEvolver.DATE_TIME_INPUT, num, late);
     }
   }
 
   public void testMessages() {
     testSubmit("/////", null, false);
     testSubmit("02/01/06", null, true);
     testSubmit("05/12/05", new GregorianCalendar(2005, 11, 05).getTime(), false);
   }
 }
