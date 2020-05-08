 package org.youthnet.export.migration;
 
 import org.youthnet.export.domain.vb25.UsysRegistration;
 import org.youthnet.export.domain.vb3.Addresses;
 import org.youthnet.export.domain.vb3.Lookups;
 import org.youthnet.export.domain.vb3.OrganisationAddresses;
 import org.youthnet.export.domain.vb3.Organisations;
 import org.youthnet.export.io.CSVFileReader;
 import org.youthnet.export.util.CSVUtil;
 
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * User: OrganisationsMigration
  * Date: 06-Jul-2010
  */
 public class OrganisationVuoMigration implements Migratable {
 
     private Map<String, Map<String, Lookups>> lookupsMap;
 
     public OrganisationVuoMigration(Map<String, Map<String, Lookups>> lookupsMap) {
         this.lookupsMap = lookupsMap;
     }
 
     @Override
     public void migrate(String csvDir, String outputDir) {
         CSVFileReader csvFileReader = null;
         BufferedWriter organisationsWriter = null;
         BufferedWriter organisationAddressWriter = null;
 
         BufferedWriter addressesWriter = null;
 
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(csvDir + "UsysRegistration.csv"));
             // Append to existing records created by OrganisationsMigration.
             organisationsWriter = new BufferedWriter(new FileWriter(outputDir + "Organisations.csv", true));
             // Append to existing records created by OrganisationsMigration.
             organisationAddressWriter = new BufferedWriter(new FileWriter(outputDir + "OrganisationAddresses.csv", true));
 
             // Append to existing records created by OrganisationsMigration, VolunteersMigration.
             addressesWriter = new BufferedWriter(new FileWriter(outputDir + "Addresses.csv", true));
 
            Map<String, Map<String, Lookups>> lookupsMap =
                    CSVUtil.createDiscriminatorValueMap(outputDir + "Lookups.csv", Lookups.class);

             UsysRegistration usysRegistration = null;
             Organisations organisations = null;
             OrganisationAddresses organisationAddresses = null;
             Addresses addresses = null;
             String record = "";
             while ((record = csvFileReader.readRecord()) != null) {
                 usysRegistration = new UsysRegistration(record);
                 organisations = new Organisations();
 
                 addresses = null;
                 organisationAddresses = null;
 
                 // -- Migrate the organisations root  data --
                 organisations.setId(UUID.randomUUID()); // Create an ID for the new organisation.
                 organisations.setIsVuo(true);
                 organisations.setIsActive(true);
                 organisations.setName(usysRegistration.getShortname());
 
                 // -- Migrate the organisations generic address data --
                 // If the organisation has an address, migrate it.
                 if (usysRegistration.getAddress1() != null && !usysRegistration.getAddress1().equals("")) {
                     addresses = new Addresses();
                     organisationAddresses = new OrganisationAddresses();
 
                     addresses.setId(UUID.randomUUID());
                     addresses.setVbase2Id(organisations.getVbase2Id());
                     addresses.setAddress1(usysRegistration.getAddress1()); // Migrate field.
                     // If the vb2.5 address one does not match the vb 2.5 address two then add it to the new vb3 address.
                     if (usysRegistration.getAddress2() != null // Stop any null pointer exceptions.
                             && !usysRegistration.getAddress1().toLowerCase().equals(
                             usysRegistration.getAddress2().toLowerCase())) {
                         addresses.setAddress2(usysRegistration.getAddress2());
                     }
                     addresses.setAddress3(null); // There is no migration mapping for this.
                     addresses.setCountryId(lookupsMap.get("country") != null &&
                             lookupsMap.get("country").get("uk") != null ?
                             lookupsMap.get("country").get("uk").getId() : null);
                     addresses.setCountyId(lookupsMap.get("county") != null &&
                             lookupsMap.get("county").get(usysRegistration.getCounty().toLowerCase()) != null ?
                            lookupsMap.get("country").get(usysRegistration.getCounty().toLowerCase()).getId() : null);
                     addresses.setPostCode(usysRegistration.getPostcode()); // Migrate field.
                     addresses.setTown(usysRegistration.getTown()); // Migrate field.
 
 
                     // -- Migrate the organisation specific address data --
                     // Associate this new organisation address to the new organisation.
                     organisationAddresses.setOrgAddConInfoId(organisations.getId());
                     // Associate this new organisation address to the new address.
                     organisationAddresses.setAddressId(addresses.getId());
                     organisationAddresses.setOrganisationName(organisations.getName()); // Create some denormalised data...
                     organisationAddresses.setUseCustomOrganisationName(false);
                     organisationAddresses.setEmail(usysRegistration.getEmail()); // Migrate field.
                     organisationAddresses.setFax(usysRegistration.getFax()); // Migrate field.
                     organisationAddresses.setTelephone(usysRegistration.getTel1()); // Migrate field.
                     organisationAddresses.setWebsite(usysRegistration.getWww()); // Migrate field.
                     organisationAddresses.setFriendlyName("Main"); // Set this to be the main address.
                     organisationAddresses.setIsDefaultAddress(true); // Set this to be the default address.
                 }
 
                 // -- Write the new records to their files --
                 organisationsWriter.write(organisations.getRecord() + "\n");
 
                 if (addresses != null) addressesWriter.write(addresses.getRecord() + "\n");
 
                 if (organisationAddresses != null)
                     organisationAddressWriter.write(organisationAddresses.getRecord() + "\n");
             }
         } catch (IOException e) {
             System.out.println("Error while migrating organisations. Error:" + e.getMessage());
         } finally {
             try {
                 if (csvFileReader != null) {
                     csvFileReader.close();
                 }
                 if (organisationsWriter != null) {
                     organisationsWriter.flush();
                     organisationsWriter.close();
                 }
                 if (organisationAddressWriter != null) {
                     organisationAddressWriter.flush();
                     organisationAddressWriter.close();
                 }
                 if (addressesWriter != null) {
                     addressesWriter.flush();
                     addressesWriter.close();
                 }
             } catch (IOException e) {
                 System.out.println("Error closing organisations streams. Error:" + e.getMessage());
             }
 
         }
 
     }
 }
