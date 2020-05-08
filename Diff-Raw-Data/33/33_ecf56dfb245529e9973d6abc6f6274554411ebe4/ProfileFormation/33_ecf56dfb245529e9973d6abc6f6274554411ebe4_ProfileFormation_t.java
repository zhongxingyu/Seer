 package controllers;
 import models.*;
 import java.util.*;
 import play.data.validation.*;
 import play.mvc.*;
 
 public class ProfileFormation extends Secure {
   
     @Before
     static void profileGlobals() {
       Profile.profileGlobals();
     }
     
     public static void index() {
         List<University> universities = University.findAll();
         List<Speciality> specialities = Speciality.findAll();
         List<Diploma> diplomas = Diploma.findAll();
         
         User connected = connectedUser();
         
         List<UniversityYear> myUniversities = UniversityYear.findForUser(connected);
         render(universities,specialities,diplomas,myUniversities);
     }
     
   public static void create(@Required Long university,@Required Long year,@Required Long speciality,@Required Long diplome) {
     if (validation.hasErrors()) {
         validation.keep();
         params.flash();
         informError();
         index();
     }
     University o_university = University.findById(university);
     Speciality o_speciality = Speciality.findById(speciality);
     User connected = connectedUser();
    notFoundIfNull(o_university);
    notFoundIfNull(o_speciality);
    if(UniversityYear.find(o_university, year, o_speciality, connected).size()!=0) {
      informError();
      index();
    }
    Diploma o_diploma = Diploma.findById(diplome);
     
     new UniversityYear(o_university,year,o_speciality,o_diploma,connected).save();
     
     informSuccess();
     index();
   }
   public static void delete(@Required Long id) {
     UniversityYear item = UniversityYear.findById(id);
     notFoundIfNull(item);
     noRightsIfNotMe(item.user);
     item.delete();
     informSuccess();
     index();
   }
 }
