 package org.openmrs.module.chartsearch;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import org.openmrs.ConceptNumeric;
 import org.openmrs.Encounter;
 import org.openmrs.Form;
 import org.openmrs.Obs;
 import org.openmrs.api.context.Context;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created by Eli on 16/03/14.
  */
 
 public class GeneratingJson {
     final String DATEFORMAT = "dd/MM/yyyy HH:mm:ss";
 
     public static String generateJson() {
 
         JSONObject jsonToReturn = new JSONObject();                     //returning this object
         JSONArray arr_of_groups = new JSONArray();
 
         JSONArray arr_of_locations = new JSONArray();
         JSONArray arr_of_providers = new JSONArray();
         JSONArray arr_of_datatypes = new JSONArray();
 
         for(String location : generateLocationsFromResults()){
             JSONObject jsonLoc = generateLocationJson(location);
             arr_of_locations.add(jsonLoc);
         }
 
         for(String provider : generateProvidersFromResults()){
             JSONObject jsonProvider = generateLocationJson(provider);
             arr_of_providers.add(jsonProvider);
         }
 
         for(String datatype : generateDatatypesFromResults()){
             JSONObject jsonDatatype = generateLocationJson(datatype);
             arr_of_datatypes.add(jsonDatatype);
         }
         jsonToReturn.put("locations", arr_of_locations);
         jsonToReturn.put("providers", arr_of_providers);
         jsonToReturn.put("datatypes", arr_of_datatypes);
 
         Set<Set<Obs>> setOfObsGroups = generateObsGroupFromSearchResults();
         for (Set<Obs> obsGrpSet : setOfObsGroups) {           //for each obs group we go through it's obs
             JSONArray arr_of_obs = new JSONArray();        //array of all the obs in a given obs group
             JSONObject jsonObs = null;
             JSONObject jsonGrp = new JSONObject();
             for (Obs obs : obsGrpSet) {                       //for each obs in a group we create the single obs and add it to the obs array
                 if (obs != null) {
                     jsonObs = createJsonObservation(obs);
                 }
                 arr_of_obs.add(jsonObs);
             }
             int obsGrpId = -1;                                         //init the obs grp id to -1, if there is a grp in
             if (obsGrpSet.iterator().hasNext()) {                         //check inside of group an individual obs, if available, what its grp id
                 Obs obsFromObsGrp = obsGrpSet.iterator().next();
                 Obs obsGrp = obsFromObsGrp.getObsGroup();
                 obsGrpId = obsGrp.getObsId();
 
                 jsonGrp.put("group_Id", obsGrpId);
                 jsonGrp.put("group_name", obsGrp.getConcept().getDisplayString());
 
                 Date obsDate = obsGrp.getObsDatetime() == null ? new Date() : obsGrp.getObsDatetime();
                 SimpleDateFormat formatDateJava = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                 /*String obsDateStr = formatDateJava.format(obsDate);*/
                 String obsDateStr = obsDate.getTime() + "";
 
                 jsonGrp.put("last_taken_date", obsDateStr);
                 jsonGrp.put("observations", arr_of_obs);
                 arr_of_groups.add(jsonGrp);
             }
         }
 
 
         jsonToReturn.put("obs_groups", arr_of_groups); //add the obs groups array to the json
 
 
         JSONObject jsonObs = null;
         JSONArray arr_of_obs = new JSONArray();
         for (Obs obsSingle : generateObsSinglesFromSearchResults()) {
             if (obsSingle != null) {
                 jsonObs = createJsonObservation(obsSingle);
             }
             arr_of_obs.add(jsonObs);
         }
         jsonToReturn.put("obs_singles", arr_of_obs);
 
         JSONObject jsonForms = null;
         JSONArray arr_of_forms = new JSONArray();
         for (Form form : generateFormsFromSearchResults()) {
             if (form != null) {
                 jsonForms = createJsonForm(form);
             }
             arr_of_forms.add(jsonForms);
         }
         jsonToReturn.put("forms", arr_of_forms);
 
         JSONObject jsonEncounters = null;
         JSONArray arr_of_encounters = new JSONArray();
         for (Encounter encounter : generateEncountersFromSearchResults()) {
             if (encounter != null) {
                 jsonEncounters = createJsonEncounter(encounter);
             }
             arr_of_encounters.add(jsonEncounters);
         }
         jsonToReturn.put("encounters", arr_of_encounters);
 
 
         String searchPhrase = SearchAPI.getInstance().getSearchPhrase().getPhrase();
         jsonToReturn.put("search_phrase", searchPhrase);
         return jsonToReturn.toString();
     }
 
 
     private static JSONObject createJsonObservation(Obs obs) {
         JSONObject jsonObs = new JSONObject();
         jsonObs.put("observation_id", obs.getObsId());
         jsonObs.put("concept_name", obs.getConcept().getDisplayString());
 
        Date obsDate = obs.getDateCreated() == null ? new Date() : obs.getDateCreated();
 
         SimpleDateFormat formatDateJava = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
         /*String dateStr = formatDateJava.format(obsDate);*/
         String dateStr = obsDate.getTime() + "";
 
         jsonObs.put("date", dateStr);
 
         if (obs.getConcept().getDatatype().isNumeric()) { // ADD MORE DATATYPES
            jsonObs.put("datatype", obs.getConcept().getDatatype().getName());
 
             ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(obs.getConcept().getId());
             jsonObs.put("units_of_measurement", conceptNumeric.getUnits());
             jsonObs.put("absolute_high", conceptNumeric.getHiAbsolute());
             jsonObs.put("absolute_low", conceptNumeric.getLowAbsolute());
             jsonObs.put("critical_high", conceptNumeric.getHiCritical());
             jsonObs.put("critical_low", conceptNumeric.getLowCritical());
             jsonObs.put("normal_high", conceptNumeric.getHiNormal());
             jsonObs.put("normal_low", conceptNumeric.getLowNormal());
        } else jsonObs.put("value_type", obs.getConcept().getDatatype().getName());
 
 
         jsonObs.put("value", obs.getValueAsString(Context.getLocale()));
         jsonObs.put("location", obs.getLocation().getDisplayString());
         jsonObs.put("provider", obs.getCreator().getDisplayString());
 
         SearchAPI searchAPI = SearchAPI.getInstance();
         if (!searchAPI.getSearchPhrase().getPhrase().equals("") && !searchAPI.getSearchPhrase().getPhrase().equals("*") ) {
             for (ChartListItem item : searchAPI.getResults()) {
                 if (item != null && item instanceof ObsItem && ((ObsItem) item).getObsId() != null) {
                     if (((ObsItem) item).getObsId() == obs.getObsId()) {
                         jsonObs.put("chosen", "true");
                     }
                 }
             }
         }
         
 
         return jsonObs;
     }
 
     private static JSONObject createJsonForm(Form form) {
         JSONObject jsonForm = new JSONObject();
         jsonForm.put("form_id", form.getFormId());
 
         Date formDate = form.getDateCreated() == null ? new Date() : form.getDateCreated();
 
         SimpleDateFormat formatDateJava = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
         /*String dateStr = formatDateJava.format(formDate);*/
         String dateStr = formDate.getTime() + "";
         jsonForm.put("date", dateStr);
         jsonForm.put("encounter_type", form.getEncounterType().getName());
         jsonForm.put("creator", form.getCreator().getName());
 
         formDate = form.getDateChanged() == null ? new Date() : form.getDateChanged();
         dateStr = formatDateJava.format(formDate);
         jsonForm.put("last_changed_date", dateStr);
 
 
        /* for(FormField formField : form.getOrderedFormFields()){
             jsonForm.put(formField.getName(),formField.getDescription());
 
         }*/
 
 
         return jsonForm;
     }
 
     private static JSONObject createJsonEncounter(Encounter encounter) {
         JSONObject jsonEncounter = new JSONObject();
         jsonEncounter.put("encounter_id", encounter.getEncounterId());
 
 
         return jsonEncounter;
     }
 
 
     public static Set<Set<Obs>> generateObsGroupFromSearchResults() {
         Set<Set<Obs>> obsGroups = new HashSet<Set<Obs>>();
 
         SearchAPI searchAPI = SearchAPI.getInstance();
         List<ChartListItem> searchResultsList = searchAPI.getResults();
         for (ChartListItem item : searchResultsList) {               //for each item in results we classify it by its obsGroup, and add all of the group.
             if (item != null && item instanceof ObsItem && ((ObsItem) item).getObsId() != null) {
                 int itemObsId = ((ObsItem) item).getObsId();
                 Obs obsGrp = Context.getObsService().getObs(itemObsId).getObsGroup();
                 if (obsGrp != null) {
                     int groupId = obsGrp.getId();
                     Set<Obs> obsGroup = obsGrp.getGroupMembers();
                     boolean found = false;                                      //if found == ture then we don't need to add the group.
                     for (Set<Obs> grp : obsGroups) {
                         Obs ob = new Obs(-1);
                         if (grp.iterator().hasNext()) {
                             ob = grp.iterator().next();
                         }
                         if (ob.getObsGroup() != null && ob.getObsGroup().getId() != null) {
                             if (ob.getObsGroup().getId() == groupId) {
                                 found = true;
                             }
                         }
                     }
                     if (!found) {
                         obsGroups.add(obsGroup);
                     }
                 }
             }
         }
         return obsGroups;
     }
 
 
     public static Set<Obs> generateObsSinglesFromSearchResults() {
         SearchAPI searchAPI = SearchAPI.getInstance();
         Set<Obs> obsSingles = new HashSet<Obs>();
         for (ChartListItem item : searchAPI.getResults()) {
             if (item != null && item instanceof ObsItem && ((ObsItem) item).getObsId() != null) {
                 int itemObsId = ((ObsItem) item).getObsId();
 
                 Obs obs = Context.getObsService().getObs(itemObsId);
                 if (obs != null && obs.getObsGroup() == null && !obs.isObsGrouping()) {
                     obsSingles.add(Context.getObsService().getObs(itemObsId));
                 }
             }
         }
         return obsSingles;
     }
     public static JSONObject generateLocationJson(String location){
         JSONObject jsonLocation = new JSONObject();
         jsonLocation.put("location", location);
         return jsonLocation;
     }
     public static JSONObject generateProviderJson(String provider){
         JSONObject jsonProvider = new JSONObject();
         jsonProvider.put("provider", provider);
         return jsonProvider;
     }
     public static JSONObject generateDatatypeJson(String datatype){
         JSONObject jsonDatatype = new JSONObject();
         jsonDatatype.put("datatype", datatype);
         return jsonDatatype;
     }
 
     public static Set<String> generateLocationsFromResults() {
         Set<String> res = new HashSet<String>();
         SearchAPI searchAPI = SearchAPI.getInstance();
 
         for (ChartListItem item : searchAPI.getResults()) {
             if (item != null && item instanceof ObsItem && ((ObsItem) item).getObsId() != null) {
                 int itemObsId = ((ObsItem) item).getObsId();
 
                 Obs obs = Context.getObsService().getObs(itemObsId);
                 if (obs != null) {
                     res.add(obs.getLocation().getDisplayString());
                 }
             }
         }
         return res;
     }
     public static Set<String> generateProvidersFromResults() {
         Set<String> res = new HashSet<String>();
         SearchAPI searchAPI = SearchAPI.getInstance();
 
         for (ChartListItem item : searchAPI.getResults()) {
             if (item != null && item instanceof ObsItem && ((ObsItem) item).getObsId() != null) {
                 int itemObsId = ((ObsItem) item).getObsId();
 
                 Obs obs = Context.getObsService().getObs(itemObsId);
                 if (obs != null) {
                     res.add(obs.getCreator().getDisplayString());
                 }
             }
         }
         return res;
     }
     public static Set<String> generateDatatypesFromResults() {
         Set<String> res = new HashSet<String>();
         SearchAPI searchAPI = SearchAPI.getInstance();
 
         for (ChartListItem item : searchAPI.getResults()) {
             if (item != null && item instanceof ObsItem && ((ObsItem) item).getObsId() != null) {
                 int itemObsId = ((ObsItem) item).getObsId();
 
                 Obs obs = Context.getObsService().getObs(itemObsId);
                 if (obs != null) {
                     res.add(obs.getConcept().getDatatype().getName());
                 }
             }
         }
         return res;
     }
 
     public static Set<Form> generateFormsFromSearchResults() {
         SearchAPI searchAPI = SearchAPI.getInstance();
         Set<Form> forms = new HashSet<Form>();
         List<ChartListItem> searchResultsList = searchAPI.getResults();
 
 
         for (ChartListItem item : searchResultsList) {
 
             if (item != null && item instanceof FormItem) {
 
                 int itemFormId = ((FormItem) item).getFormId();
                 Form form = Context.getFormService().getForm(itemFormId);
                 if (form != null) {
                     forms.add(Context.getFormService().getForm(itemFormId));
                 }
             }
         }
         return forms;
     }
 
     public static Set<Encounter> generateEncountersFromSearchResults() {
         SearchAPI searchAPI = SearchAPI.getInstance();
         Set<Encounter> encounters = new HashSet<Encounter>();
         List<ChartListItem> searchResultsList = searchAPI.getResults();
         for (ChartListItem item : searchResultsList) {
             if (item != null && item instanceof EncounterItem && ((EncounterItem) item).getEncounterId() != null) {
                 int itemEncounterId = ((EncounterItem) item).getEncounterId();
 
 
                 Encounter encounter = Context.getEncounterService().getEncounter(itemEncounterId);
                 if (encounter != null) {
                     encounters.add(Context.getEncounterService().getEncounter(itemEncounterId));
                 }
             }
         }
         return encounters;
     }
 }
