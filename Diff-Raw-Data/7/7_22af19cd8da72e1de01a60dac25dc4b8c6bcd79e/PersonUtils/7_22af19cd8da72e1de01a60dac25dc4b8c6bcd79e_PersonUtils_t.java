 package com.shepherd.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.shepherd.api.Person;
 
 public class PersonUtils {
     private static final String REPORT_ID = "report_id";
     private static final String FIRST_NAME = "first_name";
     private static final String MIDDLE_NAME = "middle_name";
     private static final String LAST_NAME = "last_name";
     private static final String AGE = "age";
     private static final String HEIGHT = "height";
     private static final String WEIGHT = "weight";
     private static final String SEX = "sex";
     private static final String HAIR = "hair_color";
     private static final String EYE = "eye_color";
     private static final String RACE = "race";
     private static final String DESCRIPTION = "description";
     private static final String PHOTOS = "photos";
    private static final String MOBILE = "mobile";
    private static final String THUMB = "thumb";
 
     private static List<Person> getMissingPersons(String jsonResponse, String fuzzyMatch) {
         List<Person> persons = new ArrayList<Person>();
 
         try {
             JSONArray jsonPersons = new JSONArray(jsonResponse);
 
             JSONObject jsonPerson;
             Person person;
             for (int i = 0; i < jsonPersons.length(); i++) {
                 jsonPerson = jsonPersons.getJSONObject(i);
                 person = new Person();
 
                 person.id = jsonPerson.getLong(REPORT_ID);
 
                 person.firstName = jsonPerson.getString(FIRST_NAME);
                 person.middleName = jsonPerson.getString(MIDDLE_NAME);
                 person.lastName = jsonPerson.getString(LAST_NAME);
 
                 person.age = jsonPerson.getInt(AGE);
                 person.height = jsonPerson.getInt(HEIGHT);
                 person.weight = jsonPerson.getInt(WEIGHT);
 
                 person.sex = jsonPerson.getString(SEX);
                 person.hair = jsonPerson.getString(HAIR);
                 person.eye = jsonPerson.getString(EYE);
                 person.race = jsonPerson.getString(RACE);
 
                 person.description = jsonPerson.getString(DESCRIPTION);
                person.photo = jsonPerson.getJSONArray(PHOTOS).getJSONObject(0).getString(MOBILE);
                person.thumb = jsonPerson.getJSONArray(PHOTOS).getJSONObject(0).getString(THUMB);
 
                 if (fuzzyMatch == null) {
                     persons.add(person);
                 } else {
                     if (person.firstName.contains(fuzzyMatch) || person.middleName.contains(fuzzyMatch)
                                     || person.lastName.contains(fuzzyMatch)) {
                         persons.add(person);
                     }
                 }
 
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         return persons;
     }
 }
