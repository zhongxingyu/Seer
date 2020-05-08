 /*
  * Copyright © 2013 VillageReach.  All Rights Reserved.  This Source Code Form is subject to the terms of the Mozilla Public License, v. 20.
  * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package org.openlmis.performancetesting.builder;
 
 import org.openlmis.core.domain.*;
 
 import static java.lang.Double.valueOf;
 import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
 import static org.apache.commons.lang.RandomStringUtils.randomNumeric;
 import static org.apache.commons.lang.math.RandomUtils.nextBoolean;
import static org.openlmis.performancetesting.Utils.periodStartDate;
 import static org.openlmis.performancetesting.Utils.randomDate;
 
 public class FacilityBuilder {
   public Facility createFacility(GeographicZone geoZone, FacilityType facilityType, FacilityOperator facilityOperator) {
     Facility facility = new Facility();
     facility.setCode(randomAlphanumeric(40));
     facility.setName(randomAlphanumeric(40));
     facility.setDescription(randomAlphanumeric(150));
     facility.setGln(randomAlphanumeric(10));
     facility.setMainPhone(randomAlphanumeric(12));
     facility.setFax(randomAlphanumeric(15));
     facility.setAddress1(randomAlphanumeric(40));
     facility.setAddress2(randomAlphanumeric(40));
     facility.setGeographicZone(geoZone);
     facility.setFacilityType(facilityType);
     facility.setCatchmentPopulation(Long.valueOf(randomNumeric(8)));
     facility.setLatitude(valueOf(randomNumeric(2)));
     facility.setLongitude(valueOf(randomNumeric(2)));
     facility.setAltitude(valueOf(randomNumeric(2)));
     facility.setOperatedBy(facilityOperator);
     facility.setColdStorageGrossCapacity(valueOf(randomNumeric(4)));
     facility.setColdStorageNetCapacity(valueOf(randomNumeric(4)));
     facility.setSuppliesOthers(nextBoolean());
     facility.setSdp(nextBoolean());
     facility.setOnline(nextBoolean());
     facility.setSatellite(nextBoolean());
     facility.setHasElectricity(nextBoolean());
     facility.setHasElectronicScc(nextBoolean());
     facility.setHasElectronicDar(nextBoolean());
    facility.setActive(true);
    facility.setGoLiveDate(periodStartDate(2009, 1));
     facility.setComment(randomAlphanumeric(50));
     facility.setDataReportable(nextBoolean());
     facility.setModifiedBy(Long.valueOf(randomNumeric(5)));
     facility.setModifiedDate(randomDate());
 //    facility.setCreatedBy();
 //    facility.setCreatedDate(randomDate());
 
     return facility;
   }
 
   public FacilityType createFacilityType() {
     FacilityType type = new FacilityType();
     type.setCode(randomAlphanumeric(30));
     type.setName(randomAlphanumeric(25));
     type.setDescription(randomAlphanumeric(100));
     type.setLevelId(Integer.valueOf(randomNumeric(1)));
     type.setNominalMaxMonth(Integer.valueOf(randomNumeric(2)));
     type.setNominalEop(Double.valueOf(randomNumeric(1)));
     type.setDisplayOrder(Integer.valueOf(randomNumeric(3)));
     type.setActive(nextBoolean());
 //    type.setModifiedBy(1);
 //    type.setLastModifiedDate();
 //    type.setCreatedBy();
 //    type.setCreatedDate();
 
     return type;
   }
 
   public GeographicZone createGeographicZone(GeographicLevel geoLevel, GeographicZone parentZone) {
     GeographicZone zone = new GeographicZone();
     zone.setCode(randomAlphanumeric(40));
     zone.setName(randomAlphanumeric(200));
     zone.setLevel(geoLevel);
     zone.setParent(parentZone);
     zone.setModifiedBy(Long.valueOf(randomNumeric(5)));
     zone.setModifiedDate(randomDate());
 //    zone.setCreatedBy();
 //    zone.setCreatedDate();
 
     return zone;
   }
 
   public GeographicLevel createGeoLevel(String name, int levelNumber) {
     GeographicLevel level = new GeographicLevel();
     level.setCode(randomAlphanumeric(40));
     level.setName(name);
     level.setLevelNumber(levelNumber);
 
     return level;
   }
 
   public FacilityOperator createFacilityOperator() {
     FacilityOperator operator = new FacilityOperator();
     operator.setCode(randomAlphanumeric(50));
     operator.setText(randomAlphanumeric(20));
     operator.setDisplayOrder(Integer.valueOf(randomNumeric(3)));
 
     return operator;
   }
 }
