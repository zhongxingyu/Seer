 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.db.migration;
 
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.util.Collection;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import com.google.common.base.Strings;
 import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
 
 import nl.surfnet.bod.domain.Institute;
 import nl.surfnet.bod.idd.IddClient;
 import nl.surfnet.bod.idd.IddLiveClient;
 import nl.surfnet.bod.idd.generated.Klanten;
 import nl.surfnet.bod.util.Functions;
 
 public class V0_10_0_8__MigrateInstitutes implements SpringJdbcMigration {
 
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
   private final String iddUrl;
   private final String iddUser;
   private final String iddPassword;
   private IddClient iddClient;
 
   public V0_10_0_8__MigrateInstitutes() throws IOException {
     final Properties properties = new Properties();
     properties.load(new ClassPathResource("bod-default.properties").getInputStream());
 
     iddUrl = getProperty("idd.url", properties);
     iddUser = getProperty("idd.user", properties);
     iddPassword = getProperty("idd.password", properties);
 
     iddClient = new IddLiveClient(iddUser, iddPassword, iddUrl);
   }
 
   private String getProperty(String name, Properties properties) {
     String result = System.getProperty(name);
     if (Strings.isNullOrEmpty(result)) {
       result = properties.getProperty(name);
     }
 
     return result;
   }
 
   @Override
   public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    logger.info("Migrating institutes using IDD @: {} with user {} and password {}",
        new Object[] { iddUrl, iddUser, iddPassword });
 
     Collection<Klanten> klanten = iddClient.getKlanten();
 
     Collection<Institute> institutes = Functions.transformKlanten(klanten, true);
 
    PreparedStatement insertInstitute = jdbcTemplate.getDataSource().getConnection()
         .prepareStatement("INSERT INTO INSTITUTE  (id, name, short_name) VALUES (?,?,?)");

    for (Institute institute : institutes) {
      insertInstitute.setLong(1, institute.getId());
      insertInstitute.setString(2, institute.getName());
      insertInstitute.setString(3, institute.getShortName());

      insertInstitute.execute();
     }

   }
 }
