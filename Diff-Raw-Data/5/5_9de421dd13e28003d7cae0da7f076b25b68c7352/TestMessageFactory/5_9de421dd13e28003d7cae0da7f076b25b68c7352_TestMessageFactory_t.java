 /*
  * #%L
  * Bitrepository Protocol
  * *
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 2.1 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  *
  * You should have received a copy of the GNU General Lesser Public
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol;
 
 import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
 import org.bitrepository.bitrepositoryelements.ProgressResponseCodeType;
 import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
 import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
 
 import java.math.BigInteger;
 
 /**
  * Abstract message factory for use in tests, which are suppose to be subclasses by functionality specific factories.
  */
 public abstract class TestMessageFactory {
     //ToDo All default messages should be converted to be loaded from the ExampleMessageFactory. This means all the
     // default attributes is specified through the example messages and the constants below can be removed
     protected static final String CORRELATION_ID_DEFAULT = "CorrelationID";
     protected static final String SLA_ID_DEFAULT = "SlaID";
     protected static final String REPLY_TO_DEFAULT = "ReplyTo";
     public static final String FILE_ID_DEFAULT = "default-test-file.txt";
     protected static final BigInteger VERSION_DEFAULT = BigInteger.valueOf(1L);
 
     protected static final TimeMeasureTYPE.TimeMeasureUnit TIME_MEASURE_UNIT_DEFAULT =
         TimeMeasureTYPE.TimeMeasureUnit.MILLISECONDS;
     protected static final BigInteger TIME_MEASURE_VALUE_DEFAULT = BigInteger.valueOf(1000L);
    protected static final ProgressResponseCodeType RESPONSE_CODE_DEFAULT = ProgressResponseCodeType.REQUEST_ACCEPTED;
     protected static final String RESPONSE_TEXT_DEFAULT = "Message request has been received and is expected to be met successfully";
     protected static final String COMPLETE_CODE_DEFAULT = "480";
     protected static final String COMPLETE_TEXT_DEFAULT = "successful completion";
 
     protected static final TimeMeasureTYPE TIME_TO_DELIVER_DEFAULT = new TimeMeasureTYPE();
     static {
         TIME_TO_DELIVER_DEFAULT.setTimeMeasureUnit(TIME_MEASURE_UNIT_DEFAULT);
         TIME_TO_DELIVER_DEFAULT.setTimeMeasureValue(TIME_MEASURE_VALUE_DEFAULT);
     }
 
     protected static final ProgressResponseInfo PROGRESS_INFO_DEFAULT = new ProgressResponseInfo();
     static {
        PROGRESS_INFO_DEFAULT.setProgressResponseCode(RESPONSE_CODE_DEFAULT);
         PROGRESS_INFO_DEFAULT.setProgressResponseText("First test progress response message");
     }
 
     protected static final FinalResponseInfo FINAL_INFO_DEFAULT = new FinalResponseInfo();
     static {
         FINAL_INFO_DEFAULT.setFinalResponseCode("T-plus 0");
         FINAL_INFO_DEFAULT.setFinalResponseText("We have liftoff");
     }
 
 }
