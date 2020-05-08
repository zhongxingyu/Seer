 package org.youthnet.export.migration;
 
 import org.youthnet.export.domain.vb25.*;
 import org.youthnet.export.domain.vb3.*;
 import org.youthnet.export.io.CSVFileReader;
 import org.youthnet.export.util.CSVUtil;
 import org.youthnet.export.util.MigrationUtil;
 
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * User: VolunteersMigration
  * Date: 06-Jul-2010
  */
 public class VolunteersMigration implements Migratable {
 
     public static final Map<String, Integer> WEEK_MAP;
 
     static {
         Map<String, Integer> weekMap = new HashMap<String, Integer>();
         weekMap.put("sat", 1);
         weekMap.put("sun", 2);
         weekMap.put("mon", 4);
         weekMap.put("tue", 8);
         weekMap.put("wed", 16);
         weekMap.put("thurs", 32);
         weekMap.put("fri", 64);
 
         WEEK_MAP = Collections.unmodifiableMap(weekMap);
     }
 
     private Map<String, Map<String, Lookups>> lookupsMap;
     private Map<String, UUID> availabilityStatusMap;
 
     public VolunteersMigration(Map<String, Map<String, Lookups>> lookupsMap) {
         this.lookupsMap = lookupsMap;
 
         availabilityStatusMap = new HashMap<String, UUID>();
         availabilityStatusMap.put("available", lookupsMap.get("availabilitystatus").get("available") != null ?
                 lookupsMap.get("availabilitystatus").get("available").getId() : null);
         availabilityStatusMap.put("registered an interest", lookupsMap.get("availabilitystatus").get("available") != null ?
                 lookupsMap.get("availabilitystatus").get("available").getId() : null);
         availabilityStatusMap.put("not available", lookupsMap.get("availabilitystatus").get("unavailable") != null ?
                 lookupsMap.get("availabilitystatus").get("unavailable").getId() : null);
         availabilityStatusMap.put("placed", lookupsMap.get("availabilitystatus").get("unknown") != null ?
                 lookupsMap.get("availabilitystatus").get("unknown").getId() : null);
         availabilityStatusMap.put("special skills", lookupsMap.get("availabilitystatus").get("unknown") != null ?
                 lookupsMap.get("availabilitystatus").get("unknown").getId() : null);
     }
 
     @Override
     public void migrate(String csvDir, String outputDir) {
         CSVFileReader csvFileReader = null;
         BufferedWriter volunteersWriter = null;
 
         BufferedWriter volunteerAddressesWriter = null;
 
         BufferedWriter volunteerContactInfoWriter = null;
         BufferedWriter volunteerContactTelephonesWriter = null;
         BufferedWriter volunteerContactEmailsWriter = null;
 
         BufferedWriter volunteerGeoAreaLookupsWriter = null;
         BufferedWriter volunteerTypeOfActivityLookupsWriter = null;
         BufferedWriter volunteerCausesIntsLookupsWriter = null;
         BufferedWriter volunteerTagsLookupsWriter = null;
 
         BufferedWriter addressesWriter = null;
 
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(csvDir + "tblVol.csv"));
             volunteersWriter = new BufferedWriter(new FileWriter(outputDir + "Volunteers.csv"));
 
             volunteerAddressesWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerAddresses.csv"));
 
             volunteerContactInfoWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerContactInfo.csv"));
             volunteerContactTelephonesWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerContactTelephones.csv"));
             volunteerContactEmailsWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerContactEmails.csv"));
 
             volunteerGeoAreaLookupsWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerGeoAreaLookups.csv"));
             volunteerTypeOfActivityLookupsWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerTypeOfActivityLookups.csv"));
             volunteerCausesIntsLookupsWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerCausesIntsLookups.csv"));
             volunteerTagsLookupsWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerTagsLookups.csv"));
 
             addressesWriter = new BufferedWriter(new FileWriter(outputDir + "Addresses.csv"));
 
             Map<Long, List<TblVolTime>> tblVolTimes = CSVUtil.createVidMap(csvDir + "tblVolTime.csv", TblVolTime.class);
             Map<Long, List<TblVolTypeOfActivity>> tblVolTypeOfActivities =
                     CSVUtil.createVidMap(csvDir + "tblVolTypeOfActivity.csv", TblVolTypeOfActivity.class);
             Map<Long, List<TblVolAreasOfInterest>> tblVolAreasOfInterests =
                     CSVUtil.createVidMap(csvDir + "tblVolAreasOfInterest.csv", TblVolAreasOfInterest.class);
             Map<Long, List<TblVolSpecial>> tblVolSpecials =
                     CSVUtil.createVidMap(csvDir + "tblVolSpecial.csv", TblVolSpecial.class);
 
             TblVol tblVol = null;
             Volunteers volunteers = null;
             VolunteerContactInfo dayTimeContactInfo = null;
             VolunteerContactTelephones dayTimeTelephone = null;
             VolunteerContactInfo eveningContactInfo = null;
             VolunteerContactTelephones eveningTelephone = null;
             VolunteerContactInfo faxContactInfo = null;
             VolunteerContactTelephones faxTelephone = null;
             VolunteerContactInfo mobileContactInfo = null;
             VolunteerContactTelephones mobileTelephone = null;
             VolunteerContactInfo emailContactInfo = null;
             VolunteerContactEmails email = null;
             VolunteerGeoAreaLookups volunteerGeoAreas = null;
             List<VolunteerTypeOfActivityLookups> volunteerTypeOfActivities = null;
             List<VolunteerCausesIntsLookups> volunteerCausesInts = null;
             List<VolunteerTagsLookups> volunteerTags = null;
             VolunteerAddresses volunteerAddresses = null;
             Addresses addresses = null;
             String record = "";
             while ((record = csvFileReader.readRecord()) != null) {
                 tblVol = new TblVol(record);
                 volunteers = new Volunteers();
 
                 dayTimeContactInfo = null;
                 dayTimeTelephone = null;
                 eveningContactInfo = null;
                 eveningTelephone = null;
                 faxContactInfo = null;
                 faxTelephone = null;
                 mobileContactInfo = null;
                 mobileTelephone = null;
                 emailContactInfo = null;
                 email = null;
                 volunteerGeoAreas = null;
                 volunteerTypeOfActivities = null;
                 volunteerCausesInts = null;
                 volunteerTags = null;
                 volunteerAddresses = null;
                 addresses = null;
 
                 // -- Migrate the volunteer root  data --
                 volunteers.setId(UUID.randomUUID());
                 volunteers.setVbase2Id(tblVol.getVid());
                 volunteers.setIsActive(true);
                 volunteers.setTitleId(MigrationUtil.getLookupId(lookupsMap, "title", tblVol.getTitle().toLowerCase(),
                         "-"));
                 volunteers.setFirstName(tblVol.getFirstname());
                 volunteers.setLastName(tblVol.getLastname());
                 volunteers.setPreferredName(tblVol.getSalutation() != null &&
                         !tblVol.getSalutation().trim().equals("") ? tblVol.getSalutation() : tblVol.getFirstname());
                 volunteers.setAgreesToBeContacted(tblVol.getContactable());
                 volunteers.setDisabilityDetails(tblVol.getDisabilitystatus());
                 volunteers.setDateOfBirth(tblVol.getDob());
                 volunteers.setGenderId(MigrationUtil.getLookupId(lookupsMap, "gender", tblVol.getGender().toLowerCase()));
                 volunteers.setAgeRangeId(MigrationUtil.getLookupId(lookupsMap, "agerange",
                         tblVol.getAgerange().toLowerCase()));
                 volunteers.setEmploymentStatusId(MigrationUtil.getLookupId(lookupsMap, "employmentstatus",
                         tblVol.getEmploymentstatus().toLowerCase()));
                 volunteers.setEthnicityId(MigrationUtil.getLookupId(lookupsMap, "ethnicity",
                         tblVol.getEthnicity().toLowerCase()));
                 volunteers.setDrivingLicenceId(MigrationUtil.getLookupId(lookupsMap, "drivinglicence",
                         tblVol.getTypeofdrivinglicence().toLowerCase()));
                 volunteers.setNationalityId(MigrationUtil.getLookupId(lookupsMap, "nationality",
                         tblVol.getNationality().toLowerCase()));
                 volunteers.setReligionId(MigrationUtil.getLookupId(lookupsMap, "religion",
                         tblVol.getReligion().toLowerCase()));
                 volunteers.setDisabilityStatusId(MigrationUtil.getLookupId(lookupsMap, "disabilityStatus",
                         tblVol.getDisabilitystatus().toLowerCase()));
                 volunteers.setTransportId(MigrationUtil.getLookupId(lookupsMap, "transport",
                         tblVol.getDriving().toLowerCase()));
                 volunteers.setQualificationsAndExperience(tblVol.getPreviouswork());
                 volunteers.setAmCommitment(createCommitment(tblVolTimes.get(volunteers.getVbase2Id()), "AM"));
                 volunteers.setPmCommitment(createCommitment(tblVolTimes.get(volunteers.getVbase2Id()), "PM"));
                 volunteers.setEveCommitment(createCommitment(tblVolTimes.get(volunteers.getVbase2Id()), "EVE"));
                 volunteers.setAvailabilityStatusId(availabilityStatusMap.get(tblVol.getStatus().toLowerCase()));
                 volunteers.setPlacementStatusId(MigrationUtil.getLookupId(lookupsMap, "placementstatus",
                         tblVol.getStatus().toLowerCase(), "unknown"));
                 volunteers.setHowHeardId(MigrationUtil.getLookupId(lookupsMap, "howheard",
                         tblVol.getHowheard().toLowerCase()));
 
                 // -- Migrate the volunteer generic address data --
                 // If the volunteer has an address, migrate it.
                 if (tblVol.getAddress1() != null && !tblVol.getAddress1().equals("")) {
                     addresses = new Addresses();
                     volunteerAddresses = new VolunteerAddresses();
 
                     addresses.setId(UUID.randomUUID());
                     addresses.setVbase2Id(volunteers.getVbase2Id());
                     addresses.setAddress1(tblVol.getAddress1());
                     addresses.setAddress2(tblVol.getAddress2());
                     addresses.setTown(tblVol.getTown());
                     addresses.setCountryId(MigrationUtil.getLookupId(lookupsMap, "country", "uk"));
                     addresses.setCountyId(MigrationUtil.getLookupId(lookupsMap, "county",
                             tblVol.getCounty().toLowerCase()));
                     addresses.setPostCode(tblVol.getPostcode());
 
                     volunteerAddresses.setVolunteerId(volunteers.getId());
                     volunteerAddresses.setName("Unspecified");
                     volunteerAddresses.setAddressId(addresses.getId());
                     volunteerAddresses.setIsActive(true);
                     volunteerAddresses.setIsDefaultAddress(true);
                 }
 
                 // -- Migrate the volunteer day time telephone number --
                 // If the volunteer has a day time telephone number, migrate it.
                 if (tblVol.getTelday() != null && !tblVol.getTelday().equals("")) {
                     dayTimeContactInfo = new VolunteerContactInfo();
                     dayTimeTelephone = new VolunteerContactTelephones();
 
                     dayTimeContactInfo.setId(UUID.randomUUID());
                     dayTimeContactInfo.setIsActive(true);
                     dayTimeContactInfo.setIsDefault(false);
                     dayTimeContactInfo.setNotes("Daytime");
                     dayTimeTelephone.setVolunteerId(volunteers.getId());
                     dayTimeTelephone.setVolunteerContactInfoId(dayTimeContactInfo.getId());
                     dayTimeTelephone.setTelephoneTypeId(MigrationUtil.getLookupId(lookupsMap, "telephonetype",
                             "unspecified"));
                     dayTimeTelephone.setTel_number(tblVol.getTelday());
                 }
 
                 // -- Migrate the volunteer evening telephone number --
                 // If the volunteer has an evening telephone number, migrate it.
                 if (tblVol.getTelevening() != null && !tblVol.getTelevening().equals("")) {
                     eveningContactInfo = new VolunteerContactInfo();
                     eveningTelephone = new VolunteerContactTelephones();
 
                     eveningContactInfo.setId(UUID.randomUUID());
                     eveningContactInfo.setIsActive(true);
                     eveningContactInfo.setIsDefault(false);
                     eveningContactInfo.setNotes("Evening");
                     eveningTelephone.setVolunteerId(volunteers.getId());
                     eveningTelephone.setVolunteerContactInfoId(eveningContactInfo.getId());
                     eveningTelephone.setTelephoneTypeId(MigrationUtil.getLookupId(lookupsMap, "telephonetype",
                             "unspecified"));
                     eveningTelephone.setTel_number(tblVol.getTelevening());
                 }
 
                 // -- Migrate the volunteer fax telephone number --
                 // If the volunteer has a fax telephone number, migrate it.
                 if (tblVol.getFax() != null && !tblVol.getFax().equals("")) {
                     faxContactInfo = new VolunteerContactInfo();
                     faxTelephone = new VolunteerContactTelephones();
 
                     faxContactInfo.setId(UUID.randomUUID());
                     faxContactInfo.setIsActive(true);
                     faxContactInfo.setIsDefault(false);
                     faxContactInfo.setNotes("");
                     faxTelephone.setVolunteerId(volunteers.getId());
                     faxTelephone.setVolunteerContactInfoId(faxContactInfo.getId());
                     faxTelephone.setTelephoneTypeId(MigrationUtil.getLookupId(lookupsMap, "telephonetype", "fax"));
                     faxTelephone.setTel_number(tblVol.getFax());
                 }
 
                 // -- Migrate the volunteer mobile telephone number --
                 // If the volunteer has a mobile telephone number, migrate it.
                 if (tblVol.getMobile() != null && !tblVol.getMobile().equals("")) {
                     mobileContactInfo = new VolunteerContactInfo();
                     mobileTelephone = new VolunteerContactTelephones();
 
                     mobileContactInfo.setId(UUID.randomUUID());
                     mobileContactInfo.setIsActive(true);
                     mobileContactInfo.setIsDefault(false);
                     mobileContactInfo.setNotes("");
                     mobileTelephone.setVolunteerId(volunteers.getId());
                     mobileTelephone.setVolunteerContactInfoId(mobileContactInfo.getId());
                     mobileTelephone.setTelephoneTypeId(MigrationUtil.getLookupId(lookupsMap, "telephonetype", "mobile"));
                     mobileTelephone.setTel_number(tblVol.getMobile());
                 }
 
                 // -- Migrate the volunteer email --
                 // If the volunteer has a email, migrate it.
                 if (tblVol.getEmail() != null && !tblVol.getEmail().equals("")) {
                     emailContactInfo = new VolunteerContactInfo();
                     email = new VolunteerContactEmails();
 
                     emailContactInfo.setId(UUID.randomUUID());
                     emailContactInfo.setIsActive(true);
                     emailContactInfo.setIsDefault(true);
                     emailContactInfo.setNotes("");
                     email.setVolunteerId(volunteers.getId());
                     email.setVolunteerContactInfoId(emailContactInfo.getId());
                     email.setTelephoneTypeId(MigrationUtil.getLookupId(lookupsMap, "emailtype", "unspecified"));
                     email.setEmail(tblVol.getEmail());
                 }
 
                 // -- Migrate the volunteer day time telephone number --
                 // If the volunteer has a geographical area, migrate it.
                 if (tblVol.getGeographicalarea() != null && !tblVol.getGeographicalarea().equals("")) {
                     volunteerGeoAreas = new VolunteerGeoAreaLookups();
 
                     volunteerGeoAreas.setVolunteerId(volunteers.getId());
                     volunteerGeoAreas.setLookupId(MigrationUtil.getLookupId(lookupsMap, "geographicalarea",
                             tblVol.getGeographicalarea().toLowerCase()));
                 }
 
                 // -- Migrate the volunteer type of activities --
                 // If the volunteer has type of activities related to it, migrate them.
                 if (tblVolTypeOfActivities.get(tblVol.getVid()) != null) {
                     volunteerTypeOfActivities = new ArrayList<VolunteerTypeOfActivityLookups>();
                     VolunteerTypeOfActivityLookups volunteerTypeOfActivity = null;
                     for (TblVolTypeOfActivity activity : tblVolTypeOfActivities.get(tblVol.getVid())) {
                         volunteerTypeOfActivity = new VolunteerTypeOfActivityLookups();
                         volunteerTypeOfActivity.setVolunteerId(volunteers.getId());
                         volunteerTypeOfActivity.setLookupId(MigrationUtil.getLookupId(lookupsMap, "typeofactivity",
                                 activity.getTypeofactivity().toLowerCase()));
 
                         if(volunteerTypeOfActivity.getLookupId() != null) {
                             volunteerTypeOfActivities.add(volunteerTypeOfActivity);
                         } else {
                            System.out.println("Volunteer type of activity " + activity.getTypeofactivity()
                                     + " has no matching type of activity lookup.");
                         }
                     }
                 }
 
                 // -- Migrate the volunteer areas of interest --
                 // If the volunteer has areas of interest related to it, migrate them.
                 if (tblVolAreasOfInterests.get(tblVol.getVid()) != null) {
                     volunteerCausesInts = new ArrayList<VolunteerCausesIntsLookups>();
                     VolunteerCausesIntsLookups volunteerCausesInt = null;
                     for (TblVolAreasOfInterest areasOfInterest : tblVolAreasOfInterests.get(tblVol.getVid())) {
                         volunteerCausesInt = new VolunteerCausesIntsLookups();
                         volunteerCausesInt.setVolunteerId(volunteers.getId());
                         volunteerCausesInt.setLookupId(MigrationUtil.getLookupId(lookupsMap, "causeinterest",
                                 areasOfInterest.getAreasofinterest().toLowerCase()));
 
                         if(volunteerCausesInt.getLookupId() != null) {
                             volunteerCausesInts.add(volunteerCausesInt);
                         } else {
                            System.out.println("Volunteer area of interest " + areasOfInterest.getAreasofinterest()
                                     + " has no matching cause of interest lookup.");
                         }
                     }
                 }
 
                 // -- Migrate the volunteer specials --
                 // If the volunteer has specials related to it, migrate them.
                 if (tblVolSpecials.get(tblVol.getVid()) != null) {
                     volunteerTags = new ArrayList<VolunteerTagsLookups>();
                     VolunteerTagsLookups volunteerTag = null;
                     for (TblVolSpecial special : tblVolSpecials.get(tblVol.getVid())) {
                         volunteerTag = new VolunteerTagsLookups();
                         volunteerTag.setVolunteerId(volunteers.getId());
                         volunteerTag.setLookupId(MigrationUtil.getLookupId(lookupsMap, "taggeddata",
                                 special.getSpecial().toLowerCase()));
 
                         if(volunteerTag.getLookupId() != null) {
                             volunteerTags.add(volunteerTag);
                         } else {
                            System.out.println("Volunteer special " + special.getSpecial()
                                     + " has no matching target lookup.");
                         }
                     }
                 }
 
                 volunteersWriter.write(volunteers.getRecord() + "\n");
 
                 if (volunteerAddresses != null) volunteerAddressesWriter.write(volunteerAddresses.getRecord() + "\n");
 
                 if (dayTimeContactInfo != null) volunteerContactInfoWriter.write(dayTimeContactInfo.getRecord() + "\n");
                 if (eveningContactInfo != null) volunteerContactInfoWriter.write(eveningContactInfo.getRecord() + "\n");
                 if (faxContactInfo != null) volunteerContactInfoWriter.write(faxContactInfo.getRecord() + "\n");
                 if (mobileContactInfo != null) volunteerContactInfoWriter.write(mobileContactInfo.getRecord() + "\n");
                 if (emailContactInfo != null) volunteerContactInfoWriter.write(emailContactInfo.getRecord() + "\n");
 
                 if (dayTimeTelephone != null)
                     volunteerContactTelephonesWriter.write(dayTimeTelephone.getRecord() + "\n");
                 if (eveningTelephone != null)
                     volunteerContactTelephonesWriter.write(eveningTelephone.getRecord() + "\n");
                 if (faxTelephone != null) volunteerContactTelephonesWriter.write(faxTelephone.getRecord() + "\n");
                 if (mobileTelephone != null) volunteerContactTelephonesWriter.write(mobileTelephone.getRecord() + "\n");
                 if (email != null) volunteerContactEmailsWriter.write(email.getRecord() + "\n");
 
                 if (volunteerGeoAreas != null)
                     volunteerGeoAreaLookupsWriter.write(volunteerGeoAreas.getRecord() + "\n");
                 if (volunteerTypeOfActivities != null) {
                     for (VolunteerTypeOfActivityLookups activity : volunteerTypeOfActivities) {
                         volunteerTypeOfActivityLookupsWriter.write(activity.getRecord() + "\n");
                     }
                 }
                 if (volunteerCausesInts != null) {
                     for (VolunteerCausesIntsLookups causesInt : volunteerCausesInts) {
                         volunteerCausesIntsLookupsWriter.write(causesInt.getRecord() + "\n");
                     }
                 }
                 if (volunteerTags != null) {
                     for (VolunteerTagsLookups tag : volunteerTags) {
                         volunteerTagsLookupsWriter.write(tag.getRecord() + "\n");
                     }
                 }
 
                 if (addresses != null) addressesWriter.write(addresses.getRecord() + "\n");
             }
 
         } catch (IOException e) {
             System.out.println("Error while migrating volunteers. Error:" + e.getMessage());
         } finally {
             try {
                 if (csvFileReader != null) {
                     csvFileReader.close();
                 }
                 if (volunteersWriter != null) {
                     volunteersWriter.flush();
                     volunteersWriter.close();
                 }
                 if (volunteerAddressesWriter != null) {
                     volunteerAddressesWriter.flush();
                     volunteerAddressesWriter.close();
                 }
                 if (volunteerContactInfoWriter != null) {
                     volunteerContactInfoWriter.flush();
                     volunteerContactInfoWriter.close();
                 }
                 if (volunteerContactTelephonesWriter != null) {
                     volunteerContactTelephonesWriter.flush();
                     volunteerContactTelephonesWriter.close();
                 }
                 if (volunteerContactEmailsWriter != null) {
                     volunteerContactEmailsWriter.flush();
                     volunteerContactEmailsWriter.close();
                 }
                 if (volunteerGeoAreaLookupsWriter != null) {
                     volunteerGeoAreaLookupsWriter.flush();
                     volunteerGeoAreaLookupsWriter.close();
                 }
                 if (volunteerTypeOfActivityLookupsWriter != null) {
                     volunteerTypeOfActivityLookupsWriter.flush();
                     volunteerTypeOfActivityLookupsWriter.close();
                 }
                 if (volunteerCausesIntsLookupsWriter != null) {
                     volunteerCausesIntsLookupsWriter.flush();
                     volunteerCausesIntsLookupsWriter.close();
                 }
                 if (volunteerTagsLookupsWriter != null) {
                     volunteerTagsLookupsWriter.flush();
                     volunteerTagsLookupsWriter.close();
                 }
                 if (addressesWriter != null) {
                     addressesWriter.flush();
                     addressesWriter.close();
                 }
 
             } catch (IOException e) {
                 System.out.println("Error closing volunteers streams. Error:" + e.getMessage());
             }
 
         }
     }
 
     private Long createCommitment(List<TblVolTime> tblVolTimes, String time) {
         Long commitment = 0L;
         if (tblVolTimes != null) { // Prevent null pointer exception.
             for (TblVolTime tblVolTime : tblVolTimes) {
                 if (tblVolTime.getTimeslot().equals(time)) {
                     commitment = commitment + WEEK_MAP.get(tblVolTime.getDay().toLowerCase());
                 }
             }
         }
 
         return commitment;
     }
 }
