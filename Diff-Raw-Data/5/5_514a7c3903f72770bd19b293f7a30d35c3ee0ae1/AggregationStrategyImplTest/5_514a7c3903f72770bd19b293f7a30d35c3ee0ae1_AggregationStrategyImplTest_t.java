 package org.motechproject.ghana.national.repository;
 
 import org.junit.Test;
 import org.motechproject.ghana.national.messagegateway.domain.SMS;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class AggregationStrategyImplTest {
 
     @Test
     public void shouldAggregateManySMSBasedOnWindowNames() {
         AggregationStrategyImpl aggregationStrategy = new AggregationStrategyImpl();
         List<SMS> messagesList = new ArrayList<SMS>() {{
             Comparator<String> alphabeticalOrder = new Comparator<String>() {
                 @Override
                 public int compare(String s, String s1) {
                     return s.compareTo(s1);
                 }
             };
 
             add(SMS.fromText("window1,milestoneName1,motechId,serialNumber,firstName,lastName", "ph", null, null, alphabeticalOrder));
             add(SMS.fromText("window1,milestoneName2,motechId,serialNumber,firstName,lastName", "ph", null, null, alphabeticalOrder));
             add(SMS.fromText("window2,milestoneName,motechId,serialNumber,firstName,lastName", "ph", null, null, alphabeticalOrder));
             add(SMS.fromText("window2,milestoneName,motechId2,serialNumber,firstName2,lastName3", "ph", null, null, alphabeticalOrder));
             add(SMS.fromText("window2,milestoneName,motechId3,serialNumber,firstName2,lastName3", "ph", null, null, alphabeticalOrder));
             add(SMS.fromText("window3,milestoneName,motechId,serialNumber,firstName,lastName", "ph", null, null, alphabeticalOrder));
         }};
 
         assertThat(aggregationStrategy.aggregate(messagesList),
                 is(equalTo("window1: firstName lastName, motechId, serialNumber, milestoneName1, milestoneName2" +
                        "%0Awindow2: firstName lastName, motechId, serialNumber, milestoneName, firstName2 lastName3, " +
                         "motechId2, serialNumber, milestoneName, firstName2 lastName3, motechId3, serialNumber, milestoneName" +
                        "%0Awindow3: firstName lastName, motechId, serialNumber, milestoneName")));
     }
 }
