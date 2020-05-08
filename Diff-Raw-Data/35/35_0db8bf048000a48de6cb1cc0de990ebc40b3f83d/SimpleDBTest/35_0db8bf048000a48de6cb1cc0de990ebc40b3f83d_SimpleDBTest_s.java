 /**
 * Elastic Grid
 * Copyright (C) 2007-2008 Elastic Grid, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.amazon.sdb;
 
 import com.elasticgrid.amazon.sdb.impl.SimpleDBImpl;
 import com.elasticgrid.utils.amazon.AWSUtils;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.util.Properties;
 import java.util.List;
 
 public class SimpleDBTest {
     private SimpleDB sdb;
 
     @Test
     public void testDomainCreation() throws SimpleDBException {
         Domain domain = sdb.createDomain("test");
         assert domain != null;
         List<Domain> domains = sdb.listDomains();
         assert domains != null;
         assert domains.size() > 0;
         domain = sdb.findDomain("test");
         assert domain != null;
         assert "test".equals(domain.getName());
         assert domain.listItems() != null;
         assert domain.listItems().getItems() != null;
         assert domain.listItems().getItems().size() == 0;
         sdb.deleteDomain(domain.getName());
     }
 
     @Test
     public void testItemCreation() throws SimpleDBException {
         Domain domain = sdb.createDomain("test");
         assert domain != null;
         Item item = domain.findItem("test");
         assert item != null;
         item.putAttribute("test_attr", "test_value", true);
         domain.deleteItem(item.getIdentifier());
         sdb.deleteDomain(domain.getName());
     }
 
     @BeforeClass
     public void setupSimpleDB() throws Exception {
         sdb = new SimpleDBImpl();
         Properties awsProps = AWSUtils.loadEC2Configuration();
        ((SimpleDBImpl) sdb).setAwsAccessID((String) awsProps.get(AWSUtils.AWS_ACCESS_ID));
        ((SimpleDBImpl) sdb).setAwsSecretKey((String) awsProps.get(AWSUtils.AWS_SECRET_KEY));
         ((SimpleDBImpl) sdb).afterPropertiesSet();
     }
 
 }
