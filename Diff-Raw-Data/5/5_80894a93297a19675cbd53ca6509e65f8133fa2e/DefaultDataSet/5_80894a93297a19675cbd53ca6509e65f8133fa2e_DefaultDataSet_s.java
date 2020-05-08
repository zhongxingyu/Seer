 package com.seitenbau.testing.dbunit.datasets;
 
 import com.seitenbau.testing.dbunit.model.JobsTable.RowGetters_Jobs;
 import com.seitenbau.testing.dbunit.model.TeamsTable.RowGetters_Teams;
 
 public class DefaultDataSet extends EmptyDataSet
 {
   @Override
   protected void initDataSet()
   {
     RowGetters_Jobs softwareDeveloper = //
     table_Jobs.insertRow() //
         .setTitle("Software Developer") //
         .setDescription("Creating software");
 
     RowGetters_Jobs softwareTester = //
     table_Jobs.insertRow() //
         .setTitle("Software Tester") //
         .setDescription("Testing software");
 
     RowGetters_Jobs teamManager = //
     table_Jobs.insertRow() //
         .setTitle("Team Manager") //
         .setDescription("Nobody knows");
 
     RowGetters_Teams qualityAssurance = //
     table_Teams.insertRow() //
         .setTitle("Quality Assurance") //
        .setDescription("Verifies that requirments for a product is fulfilled").setMembersize(3);
 
     table_Persons.insertRow() //
         .setFirstName("Dennis") //
         .setName("Kaulbersch") //
         .refJobId(softwareDeveloper) //
         .refTeamId(qualityAssurance);
 
     table_Persons.insertRow() //
         .setFirstName("Julien") //
         .setName("Guitton") //
         .refJobId(softwareTester) //
         .refTeamId(qualityAssurance);
 
     table_Persons.insertRow() //
         .setFirstName("Christian") //
        .setName("Kaulbersch") //
         .refJobId(teamManager) //
         .refTeamId(qualityAssurance);
 
   }
 }
