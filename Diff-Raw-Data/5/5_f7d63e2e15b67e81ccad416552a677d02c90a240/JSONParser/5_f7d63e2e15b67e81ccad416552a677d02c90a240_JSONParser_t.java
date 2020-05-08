 /*
  * Authored By Julian Chu <walkingice@0xlab.org>
  *
  * Copyright (c) 2012 0xlab.org - http://0xlab.org/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.zeroxlab.momome.data;
 
 import org.zeroxlab.momome.Momo;
 import org.zeroxlab.momome.Parser;
 import org.zeroxlab.momome.data.Item;
 import org.zeroxlab.momome.data.Item.ItemEntry;
 import java.util.ArrayList;
 import java.util.List;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 public class JSONParser implements Momo, Parser {
 
     public final static int VERSION_1 = 1; // maybe someday we need another version
 
     protected int mVersion;
 
     public JSONParser() {
         mVersion = VERSION_1;
     }
 
     public List<Item> parse(CharSequence content) throws Exception {
         if (mVersion == VERSION_1) {
             return parseVersion1(content);
         } else {
             Exception e = new Exception("Unknow parser version");
             throw e;
         }
     }
 
     public CharSequence generate(List<Item> items) throws Exception {
         if (mVersion == VERSION_1) {
             return generateVersion1(items);
         } else {
             Exception e = new Exception("Unknow parser version");
             throw e;
         }
     }
 
     private List<Item> parseVersion1(CharSequence content) throws Exception {
         JSONObject root = new JSONObject(content.toString());
         if (!root.has(KEY_VERSION)) {
             throw new Exception("This JSON file does not have version");
         }
 
         if (!root.has(KEY_ITEMS)) {
             throw new Exception("This JSON file does not have version");
         }
 
         JSONArray array = root.optJSONArray(KEY_ITEMS);
         List<Item> items = new ArrayList<Item>();
         for (int i = 0; i < array.length(); i++) {
             JSONObject jsonItem = array.getJSONObject(i);
             items.add(JSONObjectToItem(jsonItem));
         }
 
         return items;
     }
 
     private Item JSONObjectToItem(JSONObject json) throws Exception {
        if (!json.has(KEY_ITEM_TITLE)) {
             throw new Exception("This JSON object does not have title");
         }
 
        if (!json.has(KEY_ITEM_ENTRIES)) {
             throw new Exception("This JSON object does not have entries");
         }
 
         Item item = new Item(json.optString(KEY_ITEM_TITLE));
         JSONArrayToEntries(item, json.optJSONArray(KEY_ITEM_ENTRIES));
         return item;
     }
 
     private void JSONArrayToEntries(Item item, JSONArray array) throws Exception {
         for (int i = 0; i < array.length(); i ++) {
             JSONObject obj = array.optJSONObject(i);
             String name    = obj.optString(KEY_ENTRY_NAME, Item.DEF_NAME);
             String content = obj.optString(KEY_ENTRY_CONTENT, "");
             item.addEntry(name, content);
         }
     }
 
     private CharSequence generateVersion1(List<Item> items) throws Exception {
         JSONObject root = new JSONObject();
         root.put(KEY_VERSION, VERSION_1);
 
         JSONArray array = new JSONArray();
         for (int i = 0; i < items.size(); i++) {
             Item item = items.get(i);
             array.put(itemToJSONObject(item));
         }
 
         root.put(KEY_ITEMS, array);
         return root.toString();
     }
 
     private JSONObject itemToJSONObject(Item item) throws Exception {
         JSONObject json = new JSONObject();
         List<ItemEntry> entries = item.getEntries();
         JSONArray array = entriesToJSONArray(entries);
         json.put(KEY_ITEM_TITLE, item.getTitle());
         json.put(KEY_ITEM_ENTRIES, array);
         return json;
     }
 
     private JSONArray entriesToJSONArray(List<ItemEntry> entries) throws Exception {
         JSONArray array = new JSONArray();
         for (int i = 0; i < entries.size(); i++) {
             ItemEntry entry = entries.get(i);
             JSONObject json = new JSONObject();
             json.put(KEY_ENTRY_NAME, entry.getName());
             json.put(KEY_ENTRY_CONTENT, entry.getContent());
             array.put(json);
         }
 
         return array;
     }
 }
