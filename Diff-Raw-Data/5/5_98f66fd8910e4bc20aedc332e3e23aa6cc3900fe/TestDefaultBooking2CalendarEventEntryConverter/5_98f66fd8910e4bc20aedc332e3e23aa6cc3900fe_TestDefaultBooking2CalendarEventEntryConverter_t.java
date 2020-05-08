 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package co.altruix.booking2calendarevententry;
 
import java.util.Date;

 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ru.altruix.commons.api.di.PccException;
 
 import com.google.gdata.data.calendar.CalendarEventEntry;
 
 import at.silverstrike.pcc.api.model.Booking;
 import at.silverstrike.pcc.api.model.Task;
 import at.silverstrike.pcc.impl.mockpersistence.MockObjectFactory;
 
 import co.altruix.pcc.api.booking2calendarevententry.Booking2CalendarEventEntryConverter;
 import co.altruix.pcc.api.booking2calendarevententry.Booking2CalendarEventEntryConverterFactory;
 import co.altruix.pcc.impl.booking2calendarevententry.DefaultBooking2CalendarEventEntryConverterFactory;
 
 /**
  * @author DP118M
  * 
  */
 public final class TestDefaultBooking2CalendarEventEntryConverter {
     public static final Logger LOGGER = LoggerFactory
             .getLogger(TestDefaultBooking2CalendarEventEntryConverter.class);
 
     @Test
     public void testAppendingPccMarker() {
         final Booking2CalendarEventEntryConverterFactory factory =
                 new DefaultBooking2CalendarEventEntryConverterFactory();
         final Booking2CalendarEventEntryConverter objectUnderTest =
                 factory.create();
         final MockObjectFactory mockObjectFactory = new MockObjectFactory();
         final Booking booking = mockObjectFactory.createBooking();
         final Task task = mockObjectFactory.createTask();
 
        booking.setStartDateTime(new Date());
        booking.setDuration(1.);
         booking.setProcess(task);
 
         objectUnderTest.setBooking(booking);
         try {
             objectUnderTest.run();
         } catch (final PccException exception) {
             LOGGER.error("", exception);
             Assert.fail(exception.getMessage());
         }
 
         final CalendarEventEntry event =
                 objectUnderTest.getCalendarEventEntry();
 
         Assert.assertTrue(event.getTitle().getPlainText()
                 .endsWith(Booking2CalendarEventEntryConverter.PCC_EVENT_MARKER));
     }
 }
