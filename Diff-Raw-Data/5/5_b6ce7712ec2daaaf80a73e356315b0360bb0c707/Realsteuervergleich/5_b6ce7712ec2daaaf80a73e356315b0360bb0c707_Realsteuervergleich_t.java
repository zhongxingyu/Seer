 package de.opendatalab.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Realsteuervergleich {
 
 	private static final Map<String, ParserConfiguration> CONFIGS = new HashMap<String, ParserConfiguration>();
 	static {
 		CONFIGS.put("356-11-5.csv", new ParserConfiguration(7, 0, 2, new String[] { "grundsteuerAIstaufkommen",
 				"grundsteuerBIstaufkommen", "gewerbesteuerIstaufkommen", "grundsteuerAGrundbetrag",
 				"grundsteuerBGrundbetrag", "gewerbesteuerGrundbetrag", "grundsteuerAHebesatz", "grundsteuerBHebesatz",
 				"gewerbesteuerHebesatz", "gemeindeanteilAnDerEinkommensteuer", "gemeindeanteilAnDerUmsatzsteuer",
 				"gewerbesteuerumlage", "gewerbesteuereinnahmen" }));
 		CONFIGS.put("001-03-5.csv", new ParserConfiguration(7, 1, 3, new String[] { "betriebe", "beschäftigte",
 				"bruttoentgelte" }));
		CONFIGS.put("659-21-5.csv", new ParserConfiguration(9, 0, 2, new String[] { "Anzahl", "Ausländer",
				"schwerbehindert", "15 bis unter 20 Jahre", "15 bis unter 25 Jahre", "55 bis unter 65 Jahre",
				"langzeitarbeitslos" }));
 		CONFIGS.put("517-01-5.csv", new ParserConfiguration(8, 1, 3, new String[] {
 				"verbrauchsabhängiges Entgelt pro cbm", "haushaltsäbliches verbrauchsunabh. Entgelt p. Jahr" }));
 		CONFIGS.put("469-11-5.csv", new ParserConfiguration(8, 0, 2, new String[] { "Geöffnete Beherbergungsbetriebe",
 				"Angebotene Gästebetten", "Gästeübernachtungen", "Gästeankünfte" }));
 		CONFIGS.put("035-21-5.csv", new ParserConfiguration(8, 1, 3, new String[] { "WohngebäudeInsgesamt",
 				"Wohngebäude1Wohnung", "Wohngebäude2Wohnungen", "WohnflächeinWohngebäuden",
 				"WohnungeninWohnundNichtwohngebäuden_Größe_Insgesamt",
 				"WohnungeninWohnundNichtwohngebäuden_Größe_Wohnungenmit1Raum",
 				"WohnungeninWohnundNichtwohngebäuden_Größe_Wohnungenmit2Räumen",
 				"WohnungeninWohnundNichtwohngebäuden_Größe_Wohnungenmit3Räumen",
 				"WohnungeninWohnundNichtwohngebäuden_Größe_Wohnungenmit4Räumen",
 				"WohnungeninWohnundNichtwohngebäuden_Größe_Wohnungenmit5Räumen",
 				"WohnungeninWohnundNichtwohngebäuden_Größe_Wohnungenmit6Räumen",
 				"WohnungeninWohnundNichtwohngebäuden_Größe_Wohnungenmit7Räumenodermehr",
 				"RäumeinWohnungenmit7odermehrRäumen" }));
 		CONFIGS.put("302-11-5.csv", new ParserConfiguration(7, 0, 2, new String[] { "Unfälle (insgesamt)",
 				"Unfälle mit Personenschaden", "Schwerwiegende Unfälle mit Sachschaden i. e. S.",
 				"Sons.Sachschadensunf.unter d.Einfl.berausch.Mittel", "Getötete Personen", "Verletzte Personen" }));
 		CONFIGS.put("449-01-5.csv", new ParserConfiguration(9, 0, 2, new String[] { "Bodenfläche",
 				"Siedlungs- und Verkehrsfläche", "Gebäude- und Freifläche Insgesamt", "Gebäude- und Freifläche Wohnen",
 				"Gebäude- und Freifläche Gewerbe, Industrie", "Betriebsfläche (ohne Abbauland)",
 				"Erholungsfläche Insgesamt", "Erholungsfläche Grünanlage", "Friedhofsfläche",
 				"Verkehrsfläche Insgesamt", "Verkehrsfläche Straße, Weg, Platz", "Landwirtschaftsfläche Insgesamt",
 				"Landwirtschaftsfläche Moor", "Landwirtschaftsfläche Heide", "Waldfläche", "Wasserfläche", "Abbauland",
 				"Flächen anderer Nutzung Insgesamt", "Flächen anderer Nutzung Unland" }));
 	}
 
 	public static void main(String[] args) {
 		try {
 			Map<String, Object> json = Utils.readGenericJson(ResourceUtils.getResourceAsStream("heilbronn.geojson"),
 					"utf8");
 			Map<String, Object> result = json;
 			for (Entry<String, ParserConfiguration> entry : CONFIGS.entrySet()) {
 				System.out.println(entry.getKey());
 				ConfigurableParser parser = new ConfigurableParser(Utils.readCsvFile(ResourceUtils
 						.getResourceAsStream(entry.getKey())), entry.getValue());
 				Map<String, Map<String, Object>> rsMap = parser.parse();
 				System.out.println("Parsed data: " + rsMap.size());
 				result = filterGeoJsonByKey(result, rsMap);
 				System.out.println("Objects: " + ((List<Object>)result.get("features")).size());
 			}
 			String outputFile = "../viewer/src/data/heilbronn-rs.geojson";
 			Utils.writeData(result, outputFile);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static Map<String, Object> filterGeoJsonByKey(Map<String, Object> geoJson,
 			Map<String, Map<String, Object>> rsMap) {
 		Map<String, Object> result = Utils.asMap("type", geoJson.get("type"));
 		List<Map<String, Object>> filteredFeatures = new ArrayList<>();
 		result.put("features", filteredFeatures);
 		List<Map<String, Object>> features = (List<Map<String, Object>>)geoJson.get("features");
 		Iterator<Map<String, Object>> it = features.iterator();
 		while (it.hasNext()) {
 			Map<String, Object> feature = it.next();
 			Map<String, Object> properties = (Map<String, Object>)feature.get("properties");
 			String ags = (String)properties.get("AGS");
 			Map<String, Object> data = rsMap.get(ags);
 			if (data != null) {
 				Map<String, Object> newProperties = new HashMap<>(properties);
 				newProperties.putAll(data);
 				Map<String, Object> newFeature = Utils.asMap("type", feature.get("type"), "geometry",
 						feature.get("geometry"), "properties", newProperties);
 				filteredFeatures.add(newFeature);
 			}
 		}
 		return result;
 	}
 }
