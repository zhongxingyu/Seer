 /*
  * Copyright 2013 SURFnet bv, The Netherlands
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.surfnet.cruncher.repository;
 
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.surfnet.cruncher.config.SpringConfiguration;
 import org.surfnet.cruncher.model.LoginData;
 import org.surfnet.cruncher.model.LoginEntry;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = SpringConfiguration.class)
 public class StatisticsRepositoryImplTest  {
 
   private static final Logger LOG = LoggerFactory.getLogger(StatisticsRepositoryImplTest.class);
 
   @Inject
   private StatisticsRepositoryImpl statisticsRepository;
 
   @Inject
   private JdbcTemplate jdbcTemplate;
 
   private String sqlRowCountAggregated = "select count(*) from aggregated_log_logins";
 
   @Test(expected=IllegalArgumentException.class)
   public void aggregateLoginNull() throws Exception {
     statisticsRepository.aggregateLogin(null);
   }
 
   @Test
   public void aggregateEmptyList() {
     statisticsRepository.aggregateLogin(Collections.<LoginEntry>emptyList());
   }
 
   @Test
   public void aggregateList() {
     int rowCountBefore = jdbcTemplate.queryForInt(sqlRowCountAggregated);
     LoginEntry loginEntry = new LoginEntry(1L, "someIdp", "marker0", new Date(), "someSp", "", "", "", "");
     LoginEntry loginEntry2 = new LoginEntry(2L, "someIdp", "marker0", new Date(), "someSp", "", "", "", "");
 
     statisticsRepository.aggregateLogin(Arrays.asList(loginEntry, loginEntry2));
 
     int rowCountAfter = jdbcTemplate.queryForInt(sqlRowCountAggregated);
     assertEquals("Aggregation of 2 records should result in 1 added rows", rowCountBefore + 1, rowCountAfter);
    int aggregatedCount = jdbcTemplate.queryForInt("select entrycount from aggregated_log_logins where idpentityname like 'marker0'");
     assertEquals("Aggegrated records should count 2", 2, aggregatedCount);
   }
 
   @Test
   public void aggregateDifferentSpIdp() {
     int rowCountBefore = jdbcTemplate.queryForInt(sqlRowCountAggregated);
     LoginEntry loginEntry1 = new LoginEntry(1L, "someIdp1", "marker1", new Date(), "someSp1", "", "", "", "");
     LoginEntry loginEntry2 = new LoginEntry(2L, "someIdp2", "marker1", new Date(), "someSp1", "", "", "", "");
     LoginEntry loginEntry3 = new LoginEntry(3L, "someIdp1", "marker1", new Date(), "someSp2", "", "", "", "");
     LoginEntry loginEntry4 = new LoginEntry(4L, "someIdp2", "marker1", new Date(), "someSp2", "", "", "", "");
 
     statisticsRepository.aggregateLogin(Arrays.asList(loginEntry1, loginEntry2, loginEntry3, loginEntry4));
 
     int rowCountAfter = jdbcTemplate.queryForInt(sqlRowCountAggregated);
     assertEquals("Aggregation of 4 records of different sp/idps should result in 4 added rows", rowCountBefore + 4, rowCountAfter);
     LOG.debug("Contents of aggregated_log_logins: {}", jdbcTemplate.queryForList("select * from aggregated_log_logins"));
     List<Integer> aggregatedCount = jdbcTemplate.queryForList("select entrycount from aggregated_log_logins where idpentityname like 'marker1'", Integer.class);
     assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(0));
     assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(1));
     assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(2));
     assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(3));
 
   }
   
   @Test
   public void getUniqueLogins() throws ParseException {
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-DD");
     Timestamp start = new Timestamp(0L);
     Timestamp end = new Timestamp(System.currentTimeMillis());
     List<LoginData> result = statisticsRepository.getUniqueLogins(start, end, "sp1", "idp1");
     assertNotNull(result);
     assertEquals(12, result.size());
     
     result = statisticsRepository.getUniqueLogins(start, end, "unknown", "idp1");
     assertNotNull(result);
     assertEquals(0, result.size());
     
     Date startDate = sdf.parse("2013-01-01");
     Date endDate =sdf.parse("2013-01-04");
     result = statisticsRepository.getUniqueLogins(new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()), null, "idp1");
     assertNotNull(result);
     assertEquals(4, result.size());
     LoginData first = result.get(0);
     assertEquals(20, first.getTotal());
     assertEquals("idp1", first.getIdpEntityId());
   }
 }
