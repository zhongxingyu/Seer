 package net.jorgeherskovic.medrec.client;
 
 import net.jorgeherskovic.medrec.client.event.FinishedLoadingEvent;
 import net.jorgeherskovic.medrec.shared.Consolidation;
 import net.jorgeherskovic.medrec.shared.Medication;
 import net.jorgeherskovic.medrec.shared.ReconciledMedication;
 import net.jorgeherskovic.medrec.shared.Reconciliation;
 
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 
 public class MedRecJSONData extends Reconciliation {
 	private final SimpleEventBus bus;
 
 	public MedRecJSONData(String jsonURL, SimpleEventBus bus) {
 		super();
 		// Get the base JSON array
 		String url = URL.encode(jsonURL);
 		this.bus = bus;
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 
 		try {
 			builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					System.out.println("Couldn't retrieve JSON");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					MedRecJSONData.this.parseData(response.getText());
 					MedRecJSONData.this.bus
 							.fireEvent(new FinishedLoadingEvent());
 				}
 			});
 		} catch (com.google.gwt.http.client.RequestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	protected boolean readJSONFieldOrFalse(JSONObject obj, String fieldName) {
 		return obj.containsKey(fieldName) ? obj.get(fieldName).isBoolean()
 				.booleanValue() : false;
 	}
 
 	protected String readJSONFieldOrEmptyString(JSONObject obj, String fieldName) {
 		return obj.containsKey(fieldName) ? obj.get(fieldName).isString()
 				.stringValue() : "";
 	}
 
 	protected Medication medFactory(JSONObject JSONMed) {
 		Medication this_med = new Medication();
 
		this_med.setMedicationName(readJSONFieldOrEmptyString(JSONMed, "medicationName"));
 
 		this_med.setDose(readJSONFieldOrEmptyString(JSONMed, "dose"));
 		this_med.setFormulation(readJSONFieldOrEmptyString(JSONMed, "formulation"));
 		this_med.setUnits(readJSONFieldOrEmptyString(JSONMed, "units"));
 		this_med.setInstructions(readJSONFieldOrEmptyString(JSONMed, "instructions"));
 		this_med.setProvenance(readJSONFieldOrEmptyString(JSONMed, "provenance"));
		this_med.setStartDate(readJSONFieldOrEmptyString(JSONMed, "startDate"));
		this_med.setEndDate(readJSONFieldOrEmptyString(JSONMed, "endDate"));
 		this_med.setOriginalString(readJSONFieldOrEmptyString(JSONMed, "original_string"));
 		// if the parsed field exists, set its value from the input object.
 		// Otherwise, set it to parsed=true if it has a medication name
 		if (JSONMed.containsKey("parsed")) {
 			this_med.setParsed(readJSONFieldOrFalse(JSONMed, "parsed"));
 		} else {
 			this_med.setParsed(this_med.getMedicationName().length() > 0);
 		}
 		
 		return this_med;
 	}
 
 	protected Consolidation consFactory(JSONObject JSONCons) {
 		Medication med1 = medFactory(JSONCons.get("med1").isObject());
 		Medication med2 = medFactory(JSONCons.get("med2").isObject());
 		double score = JSONCons.get("score").isNumber().doubleValue();
 		String mechanism = JSONCons.get("mechanism").isString().stringValue();
 		return new Consolidation(med1, med2, score, mechanism);
 
 	}
 
 	protected void parseData(String text) {
 		JSONValue myData = JSONParser.parseStrict(text);
 		JSONObject myDictionary = myData.isObject();
 		assert (myDictionary != null);
 
 		JSONArray reconciled = myDictionary.get("reconciled").isArray();
 		for (int i = 0; i < reconciled.size(); i++) {
 			Consolidation my_consolidation = consFactory(reconciled.get(i)
 					.isObject());
 			if (my_consolidation.getScore() > 0.99) {
 				this.reconciledMeds.add(new ReconciledMedication(
 						my_consolidation, 0));
 			} else {
 				this.consolidatedMeds.add(my_consolidation);
 			}
 		}
 
 		JSONArray new_list;
 		// Read all the "new_lists", which contain unreconciled meds.
 		int i = 1;
 		while (myDictionary.containsKey("new_list_" + i)) {
 			new_list = myDictionary.get("new_list_" + i).isArray();
 			assert (new_list != null);
 			for (int j = 0; j < new_list.size(); j++) {
 				Medication this_med = medFactory(new_list.get(j).isObject());
 
 				Consolidation this_cons = new Consolidation(this_med,
 						new Medication(), 0.0, "Unique");
 				this.consolidatedMeds.add(this_cons);
 			}
 			i++;
 		}
 
 		// For the sake of completeness, read the original lists.
 		JSONArray original_list;
 		i = 1;
 		while (myDictionary.containsKey("original_list_" + i)) {
 			original_list = myDictionary.get("original_list_" + i).isArray();
 			assert (original_list != null);
 			String[] this_list = new String[original_list.size()];
 			for (int j = 0; j < original_list.size(); j++) {
 				this_list[j] = original_list.get(j).isString().stringValue()
 						.trim();
 			}
 
 			this.originalLists.add(this_list);
 			i++;
 		}
 	}
 
 }
