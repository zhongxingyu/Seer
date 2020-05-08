 import processing.core.*; 
 import processing.data.*; 
 import processing.event.*; 
 import processing.opengl.*; 
 
 import java.util.*; 
 import java.text.*; 
 
 import java.util.HashMap; 
 import java.util.ArrayList; 
 import java.io.File; 
 import java.io.BufferedReader; 
 import java.io.PrintWriter; 
 import java.io.InputStream; 
 import java.io.OutputStream; 
 import java.io.IOException; 
 
 public class Week3OkavangoHeartrate extends PApplet {
 
 
 //import java.util.Map;
 
 //import java.text.SimpleDateFormat;
 
 String endPoint = "OkavangoHeartrate";
//ArrayList<HRObject> hrList = new ArrayList();
 IntDict personDict = new IntDict();
 
 HashMap<String, ArrayList> hrMap;
 
 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 
 int noHRcount = 0;
 int noSpeedcount = 0;
 int noHRSpeed = 0;
 
 public void setup() {
	size(1280, 720);
 	background(0);
 	stroke(255);
 	fill(255);
 
 	JSONObject myJSON = loadJSONObject(endPoint);
 
 	JSONArray features = myJSON.getJSONArray("features");
 	//println("features.size(): "+features.size());
 
 	// Get personDict
 	for (int i = 0; i<features.size(); i++){
 		JSONObject feature = features.getJSONObject(i);
 		JSONObject properties = feature.getJSONObject("properties");
 
 		String person = properties.getString("Person");
 		personDict.increment(person);
 	}
 
 	String[] keys = personDict.keyArray();
 	hrMap = new HashMap(keys.length);
 
 /*
 	for (String person : keys){
 		println("person: "+person);
 		ArrayList<HRObject> hrList = new ArrayList();
 
 		for (int i = 0; i<features.size(); i++){
 			JSONObject feature = features.getJSONObject(i);
 			JSONObject properties = feature.getJSONObject("properties");
 
 			if (person.equals(properties.getString("Person"))){
 				HRObject hro = new HRObject();	
 				hro.id = feature.getInt("id");
 
 				hro.distance = properties.getInt("Distance");
 				//hro.speed = properties.getFloat("Speed");
 				//hro.hr = properties.getFloat("HR");
 				hro.dateTime = properties.getString("DateTime");
 
 				hro.tpos = new PVector(random(width), random(height));
 
 				hrList.add(hro);
 			}
 		
 		}
 		hrMap.put(person, hrList);
 	}
 */
 	
 	// Get data to hashmap
 	for (int i = 0; i<features.size(); i++){
 		JSONObject feature = features.getJSONObject(i);
 		JSONObject properties = feature.getJSONObject("properties");
 		HRObject hro = new HRObject();
 		
 		hro.id = feature.getInt("id");
 		hro.distance = properties.getInt("Distance");
 		// assign -1 to those that don't have speed and HR field
 		hro.speed = properties.getFloat("Speed", -1);
 		hro.hr = properties.getFloat("HR", -1);
 
 		if (hro.hr == -1){
 			noHRcount++;
 		}
 		if (hro.speed == -1){
 			noSpeedcount++;
 		}
 		if (hro.hr == -1 && hro.speed == -1){
 			noHRSpeed++;
 		}
 
 		try { 
           	hro.dateTime = sdf.parse(properties.getString("DateTime"));
         	//println(hro.date); 
       	} catch (ParseException e) { 
         	System.out.println("Unparseable using " + properties.getString("DateTime")); 
     	}
 
 		hro.energyConsumption = properties.getFloat("EnergyConsumption");
 
		//hro.tpos = new PVector(random(width), random(height));

 		hro.person = properties.getString("Person");
 		if(!hrMap.containsKey(hro.person)) {
 			hrMap.put(hro.person, new ArrayList<HRObject>());
 		}
 		hrMap.get(hro.person).add(hro);
 	}
 
 	println("hrMap: "+hrMap.size());
 	//println(hrMap.get("Steve").size());
 	for (Map.Entry me : hrMap.entrySet()){
 		//ArrayList l = (ArrayList)me.getValue();
 		Collections.sort((ArrayList)me.getValue(), new DateComparator());
 		println(me.getKey() + ": " + ((ArrayList)me.getValue()).size());	
 
 		for (HRObject o : (ArrayList<HRObject>)me.getValue()){
 			//println(me.getKey() + " dateTime: "+o.dateTime);
 			//println("o.dateTime.getTime(): "+o.dateTime.getTime());
 			o.tpos = new PVector(map(o.dateTime.getTime(), 1379051558000L, 1379090232000L, 0, width), 
 								 map(o.hr, 0, 3, height, 0));
 		}
 	}
 	println("noHRcount: "+noHRcount);
 	println("noSpeedcount: "+noSpeedcount);
 	println("noHRSpeed: "+noHRSpeed);
 }
 
 public void draw() {
 	background(0);
	
	for(int i = 0; i < 1000; i++) {
		if(((ArrayList<HRObject>)hrMap.get("GB")).get(i).hr != -1) {
			((ArrayList<HRObject>)hrMap.get("GB")).get(i).update();
			((ArrayList<HRObject>)hrMap.get("GB")).get(i).render();
		}
	}
 
	// for (HRObject o : (ArrayList<HRObject>)hrMap.get("Steve")){
	// 	o.update();
	// 	o.render();
	// }
 }
 class DateComparator implements Comparator<HRObject> {
 	public @Override
 	int compare(HRObject a, HRObject b) {
 		return a.dateTime.compareTo(b.dateTime);
 	}
 }
 class HRObject {
 	int id;
 	int seaLevelPressure;
 	//String sampleType;
 	//int t_utc; 
 	int distance;
 	
 	float speed;
 	Float hr;
 	String person;
 	//int verticalSpeed;
 	//String contentType;
 	//String dateTime;
 	Date dateTime;
 	float energyConsumption;
 	//float time;
 	float temperature;
 	int altitude;
 
 	PVector pos = new PVector();
 	PVector tpos = new PVector();
 
 	public void update() {
 		pos.lerp(tpos, 0.1f);
 	}
 
 	public void render() {
 		pushMatrix();
 		translate(pos.x, pos.y);
 		//text(person, 0, 0);
 		ellipse(0, 0, 1, 1);
 		//line(0, 0, 0, height - pos.y);
 		popMatrix();
 	}
 }
   static public void main(String[] passedArgs) {
     String[] appletArgs = new String[] { "Week3OkavangoHeartrate" };
     if (passedArgs != null) {
       PApplet.main(concat(appletArgs, passedArgs));
     } else {
       PApplet.main(appletArgs);
     }
   }
 }
