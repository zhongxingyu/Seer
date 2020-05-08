 package com.sk.util.data;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.TreeMap;
 
 import com.google.common.base.Optional;
 import com.google.gson.TypeAdapter;
 import com.google.gson.stream.JsonReader;
 import com.google.gson.stream.JsonWriter;
 
 public class PersonalData extends LinkedHashMap<String, String> {
 
 	private static int dataInc = new Random().nextInt();
 
 	private final String siteId;
 	private final int dataId;
 
 	public PersonalData(String source) {
 		this.siteId = source;
 		dataId = dataInc++;
 	}
 
 	public PersonalData(String source, int initialCapacity) {
 		super(initialCapacity);
 		this.siteId = source;
 		dataId = dataInc++;
 
 	}
 
 	public PersonalData(String source, Map<? extends String, ? extends String> m) {
 		super(m);
 		this.siteId = source;
 		dataId = dataInc++;
 
 	}
 
 	public PersonalData(String source, int initialCapacity, float loadFactor) {
 		super(initialCapacity, loadFactor);
 		this.siteId = source;
 		dataId = dataInc++;
 	}
 
 	public PersonalData(boolean copyAttr, PersonalData source) {
 		super(copyAttr ? source : new TreeMap<String, String>());
 		this.siteId = source.siteId;
 		this.dataId = source.getId();
 	}
 
 	private PersonalData(String site, int id, Map<? extends String, ? extends String> map) {
 		super(map);
		if (id == -1)
			id = dataInc++;
 		this.siteId = site;
 		this.dataId = id;
 	}
 
 	public Optional<String> get(String attribute) {
 		if (containsKey(attribute))
 			return Optional.of(super.get(attribute));
 		else
 			return Optional.absent();
 	}
 
 	public String[] getAllValues(String attribute) {
 		if (containsKey(attribute)) {
 			return super.get(attribute).split("[|]");
 		} else {
 			return new String[0];
 		}
 	}
 
 	/**
 	 * Get the website id of the source of the {@link PersonalData}
 	 * 
 	 * @return The website id
 	 */
 	public String getWebsiteId() {
 		return siteId;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder ret = new StringBuilder();
 		ret.append("PersonalData (");
 		ret.append(siteId);
 		ret.append(", ");
 		ret.append(super.toString());
 		ret.append(")");
 		return ret.toString();
 	}
 
 	private static final long serialVersionUID = 4259139787451939836L;
 
 	public int getId() {
 		return dataId;
 	}
 
 	public static TypeAdapter<PersonalData> getAdapter() {
 		return new PersonalDataAdapter().nullSafe();
 	}
 
 	private static class PersonalDataAdapter extends TypeAdapter<PersonalData> {
 
 		private static final String siteToken = "xxs";
 		private static final String dataToken = "xxd";
 
 		@Override
 		public PersonalData read(JsonReader in) throws IOException {
 			in.beginObject();
 			Map<String, String> values = new HashMap<>();
 			String site = null;
 			Integer id = null;
 			while (in.hasNext()) {
 				String key = in.nextName();
 				if (key.equals(siteToken)) {
 					site = in.nextString();
 				} else if (key.equals(dataToken)) {
 					id = in.nextInt();
 				} else {
 					values.put(key, in.nextString());
 				}
 			}
 			in.endObject();
 
 			return new PersonalData(site, id, values);
 		}
 
 		@Override
 		public void write(JsonWriter out, PersonalData value) throws IOException {
 			out.beginObject();
 			out.name(siteToken).value(value.getWebsiteId());
 			out.name(dataToken).value(value.getId());
 			for (Entry<String, String> entry : value.entrySet()) {
 				out.name(entry.getKey()).value(entry.getValue());
 			}
 			out.endObject();
 		}
 
 	}
 }
