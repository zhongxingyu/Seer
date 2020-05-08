 package com.t2.mtbi.activity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.XmlResourceParser;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class XMLItemsBrowserActivity extends ABSNavigationActivity implements OnItemClickListener {
 	public static final String EXTRA_XML_RESOURCE = "xmlResId";
 	public static final String EXTRA_BASE_ITEM_ID = "sectionId";
 	public static final String EXTRA_HEADER_RES_ID = "headerResId";
 	public static final String EXTRA_ITEM_RES_ID = "itemResId";
 
 	private static final String STYLE_NORMAL = "normal";
 	private static final String STYLE_SEPARATED = "separated";
 	private static final String BASE_ITEM_ID = "baseItemId";
 
 	private static final String XML_ITEMS_TAG = "items";
 	private static final String XML_ITEM_TAG = "item";
 	private static final String XML_ID_ATTRIBUTE = "id";
 	private static final String XML_TITLE_ATTRIBUTE = "title";
 	private static final String XML_PARENT_ID_ATTRIBUTE = "parentId";
 	private static final String XML_STYLE_ATTRIBUTE = "style";
 
 	protected static final String LIST_ITEM_TITLE = "title";
 	protected static final String LIST_ITEM_ID = "id";
 
 	private int xmlResource = -1;
 
 	private int seperatorResId = -1;
 	private String[] seperatorFrom;
 	private int[] seperatorTo;
 
 	private int itemResId = -1;
 	private String[] itemFrom;
 	private int[] itemTo;
 	protected LinkedHashMap<String, Item> itemsMap;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Intent intent = this.getIntent();
 
 		this.seperatorResId = intent.getIntExtra(EXTRA_HEADER_RES_ID, this.getHeaderLayoutResId());
 		this.itemResId = intent.getIntExtra(EXTRA_ITEM_RES_ID, this.getItemLayoutResId());
 
 		this.setSeparatorResource(
 				this.seperatorResId,
 				new String[]{
 						LIST_ITEM_TITLE,
 				},
 				new int[] {
 						android.R.id.text1,
 				}
 		);
 		this.setItemResource(
 				this.itemResId,
 				new String[]{
 						LIST_ITEM_TITLE,
 				},
 				new int[] {
 						android.R.id.text1,
 				}
 		);
 
 		// if this hasn't been set by overriding class
 		if(!this.isXMLResourceSet()) {
 			// try to pull it from the intent.
 			this.setXMLResource(intent.getIntExtra(EXTRA_XML_RESOURCE, -1));
 			// still isn't set, close the activity.
 			if(!this.isXMLResourceSet()) {
 				this.finish();
 				return;
 			}
 		}
 
 		String baseXMLItemId = intent.getStringExtra(EXTRA_BASE_ITEM_ID);
 		if(baseXMLItemId == null || baseXMLItemId.length() == 0) {
 			baseXMLItemId = BASE_ITEM_ID;
 		}
 
 		this.itemsMap = this.loadItemsFromXML(this.xmlResource, baseXMLItemId);
 		Item baseItem = this.itemsMap.get(baseXMLItemId);
 
 		if(baseItem == null) {
 			return;
 		}
 
 		this.setTitle(baseItem.title);
 
 		if(baseItem.hasItems(this.itemsMap)) {
 			ListView listView = new ListView(this);
 			listView.setScrollingCacheEnabled(false);
 			listView.setCacheColorHint(Color.TRANSPARENT);
 			if(baseItem.style.equals(STYLE_SEPARATED)) {
 				listView.setAdapter(getSeparatedAdapter(baseItem));
 			} else {
 				listView.setAdapter(getSimpleAdapter(baseItem));
 			}
 			listView.setOnItemClickListener(this);
 
 			this.setContentView(
 					listView,
 					new LayoutParams(
 						LayoutParams.FILL_PARENT,
 						LayoutParams.FILL_PARENT
 					)
 			);
 		} else {
 			this.onItemClick(baseItem);
 			this.finish();
 		}
 	}
 
 	protected int getHeaderLayoutResId() {
 		return android.R.layout.simple_list_item_1;
 	}
 
 	protected int getItemLayoutResId() {
 		return android.R.layout.simple_list_item_1;
 	}
 
 	public boolean isXMLResourceSet() {
 		return this.xmlResource != -1;
 	}
 	public void setXMLResource(int xmlRes) {
 		this.xmlResource = xmlRes;
 	}
 	public int getXMLResource() {
 		return this.xmlResource;
 	}
 
 	public boolean isSeparatorResourceSet() {
 		return this.seperatorResId != -1 && this.seperatorFrom != null && this.seperatorTo != null;
 	}
 
 	public void setSeparatorResource(int layoutId, String[] from, int[] to) {
 		this.seperatorResId = layoutId;
 		this.seperatorFrom = from;
 		this.seperatorTo = to;
 	}
 
 	public boolean isItemResourceSet() {
 		return this.itemResId != -1 && this.itemFrom != null && this.itemTo != null;
 	}
 
 	public void setItemResource(int layoutId, String[] from, int[] to) {
 		this.itemResId = layoutId;
 		this.itemFrom = from;
 		this.itemTo = to;
 	}
 
 	private LinkedHashMap<String,Item> loadItemsFromXML(int xmlResourceId, String baseId) {
 		LinkedHashMap<String,Item> items = new LinkedHashMap<String,Item>();
 		int tagNum = -1;
 
 		// build the list of sections
 		try {
 			XmlResourceParser parser = this.getResources().getXml(xmlResourceId);
 			int eventType = parser.getEventType();
 			while(eventType != XmlPullParser.END_DOCUMENT) {
 				String tag = parser.getName();
 				++tagNum;
 
 				if(eventType == XmlPullParser.START_TAG) {
 					boolean isBaseItem = tag.equals(XML_ITEMS_TAG);
 					if(tag.equals(XML_ITEM_TAG) || isBaseItem) {
 						Item currentItem = new Item(
 							parser.getAttributeValue(null, XML_ID_ATTRIBUTE),
 							parser.getAttributeValue(null, XML_TITLE_ATTRIBUTE),
 							parser.getAttributeValue(null, XML_PARENT_ID_ATTRIBUTE)
 						);
 
 						String destUriStr = parser.getAttributeValue(null, "destUri");
 						if(destUriStr != null) {
 							currentItem.destUri = Uri.parse(destUriStr);
 						}
 
 						if(isBaseItem) {
 							currentItem.id = BASE_ITEM_ID;
 							currentItem.parentId = null;
 						} else {
 							// create an id for the item.
 							if(currentItem.id == null || currentItem.id.length() == 0) {
 								currentItem.id = "genid-"+tagNum;
 							}
 						}
 
 						//Log.v(TAG, "item:"+currentItem.id+","+currentItem.parentId);
 
 						// set the style for the item.
 						String style = parser.getAttributeValue(null, XML_STYLE_ATTRIBUTE);
 						if(style == null || style.length() == 0) {
 							style = STYLE_NORMAL;
 						}
 						if(!style.equals(STYLE_SEPARATED)) {
 							style = STYLE_NORMAL;
 						}
 						currentItem.style = style;
 
 						currentItem.attributes = getAttributes(parser);
 
 						items.put(currentItem.id, currentItem);
 
 						eventType = parser.next();
 						if(eventType == XmlPullParser.TEXT) {
 							if(currentItem != null) {
 								currentItem.content = parser.getText();
 							}
 						}
 						continue;
 					}
 				}
 
 				eventType = parser.next();
 			}
 		} catch (XmlPullParserException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// clean up the item tree.
 		for(Item item: items.values()) {
 			if(item.id.equals(BASE_ITEM_ID)) {
 				continue;
 			}
 
 			if(item.parentId == null || item.parentId.length() == 0) {
 				item.parentId = BASE_ITEM_ID;
 			}
 
 			if(items.get(item.parentId) == null) {
 				item.parentId = BASE_ITEM_ID;
 			}
 		}
 
 		return items;
 	}
 
 
 	private HashMap<String,String> getAttributes(XmlPullParser parser) {
 		HashMap<String,String> attributes = new HashMap<String,String>();
 		for(int i = 0; i < parser.getAttributeCount(); ++i) {
 			attributes.put(
 					parser.getAttributeName(i),
 					parser.getAttributeValue(i)
 			);
 		}
 		return attributes;
 	}
 
 	private BaseAdapter getSimpleAdapter(Item baseItem) {
 		// build the adapter.
 		//return buildAdapter(buildItems(baseItem.items));
 		return new SimpleSeperatorAdapter(
 				this,
 				buildItemsHashList(baseItem.getItems(itemsMap)),
 				this.itemResId,
 				this.itemFrom,
 				this.itemTo,
 				this.seperatorResId,
 				this.seperatorFrom,
 				this.seperatorTo
 		);
 	}
 
 	private SimpleSeperatorAdapter getSeparatedAdapter(Item baseItem) {
 		// build the adapter.
 		ArrayList<HashMap<String,Object>> items = new ArrayList<HashMap<String,Object>>();
 
 		ArrayList<Item> children = baseItem.getItems(itemsMap);
 		for(int i = 0; i < children.size(); ++i) {
 			Item item = children.get(i);
 
 			// add the header item.
 			if(item.hasItems(this.itemsMap)) {
 				//Log.v(TAG, "Add header "+ item.title);
 				HashMap<String,Object> hashItem = item.buildHashItem();
 				hashItem.put(SimpleSeperatorAdapter.IS_SEPERATOR_ITEM_KEY, true);
 				hashItem.put(SimpleSeperatorAdapter.IS_ENABLED_ITEM_KEY, false);
 				items.add(hashItem);
 				items.addAll(buildItemsHashList(item.getItems(this.itemsMap)));
 			} else {
 				items.add(item.buildHashItem());
 			}
 		}
 
 		return new SimpleSeperatorAdapter(
 				this,
 				items,
 				this.itemResId,
 				this.itemFrom,
 				this.itemTo,
 				this.seperatorResId,
 				this.seperatorFrom,
 				this.seperatorTo
 		);
 	}
 
 	private ArrayList<HashMap<String,Object>> buildItemsHashList(ArrayList<Item> itemsIn) {
 		ArrayList<HashMap<String,Object>> items = new ArrayList<HashMap<String,Object>>();
 		for(int i = 0; i < itemsIn.size(); ++i) {
 			Item childItem = itemsIn.get(i);
 			items.add(childItem.buildHashItem());
 		}
 		return items;
 	}
 
 	@Override
 	public final void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		BaseAdapter adapter = (BaseAdapter)arg0.getAdapter();
 		@SuppressWarnings("unchecked")
 		HashMap<String,Object> itemMap = (HashMap<String, Object>) adapter.getItem(arg2);
 		String id = (String) itemMap.get(LIST_ITEM_ID);
 		onItemClick(itemsMap.get(id));
 	}
 
 	public void onItemClick(Item item) {
 		if(item.destUri != null) {
 			Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
 			intent.setData(item.destUri);
 			startActivity(intent);
 
 		} else if(item.hasItems(this.itemsMap)) {
 			Intent intent = new Intent(this, this.getClass());
 			intent.putExtra(XMLItemsBrowserActivity.EXTRA_XML_RESOURCE, this.xmlResource);
 			intent.putExtra(XMLItemsBrowserActivity.EXTRA_BASE_ITEM_ID, item.id);
 			intent.putExtra(XMLItemsBrowserActivity.EXTRA_HEADER_RES_ID, this.seperatorResId);
 			intent.putExtra(XMLItemsBrowserActivity.EXTRA_ITEM_RES_ID, this.itemResId);
 			this.startActivity(intent);
		} else {
 			Intent intent = new Intent(this, WebViewActivity.class);
 			intent.putExtra(WebViewActivity.EXTRA_CONTENT, item.content);
 			intent.putExtra(WebViewActivity.EXTRA_TITLE, item.title);
 			this.startActivity(intent);
 		}
 	}
 
 	public static class Item {
 		public String id;
 		public String title;
 		public String content;
 		public String parentId;
 		public String style;
 		public Uri destUri;
 		private HashMap<String, String> attributes = new HashMap<String,String>();
 
 		public Item(String id, String title, String parentId) {
 			this.id = id;
 			this.title = title;
 			this.parentId = parentId;
 		}
 
 		public Item(String id, String title, HashMap<String,String> atts) {
 			this.id = id;
 			this.title = title;
 			this.attributes  = atts;
 		}
 
 		public boolean hasItems(LinkedHashMap<String,Item> items) {
 			for(Item item: items.values()) {
 				if(item.parentId != null && item.parentId.equals(id)) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		public ArrayList<Item> getItems(LinkedHashMap<String,Item> items) {
 			ArrayList<Item> itemsOut = new ArrayList<Item>();
 
 			for(Item item: items.values()) {
 				if(item.parentId != null && item.parentId.equals(this.id)) {
 					itemsOut.add(item);
 				}
 			}
 
 			return itemsOut;
 		}
 
 		public HashMap<String,Object> buildHashItem() {
 			HashMap<String,Object> item = new HashMap<String,Object>();
 			for(String key: attributes.keySet()) {
 				item.put(key, attributes.get(key));
 			}
 			item.put(LIST_ITEM_ID, id);
 			item.put(LIST_ITEM_TITLE, title);
 			return item;
 		}
 	}
 
 	private static class SimpleSeperatorAdapter extends SimpleAdapter {
 		public static final String IS_SEPERATOR_ITEM_KEY = "isHeaderItem";
 		public static final String IS_ENABLED_ITEM_KEY = "isEnabled";
 		private ArrayList<HashMap<String,Object>> seperators = new ArrayList<HashMap<String,Object>>();
 		private SimpleAdapter seperatorAdapter;
 
		public SimpleSeperatorAdapter(Context context,
 				List<? extends Map<String, Object>> data, int resource,
 				String[] from, int[] to) {
 			super(context, data, resource, from, to);
 
 			this.seperatorAdapter = new SimpleAdapter(
 					context,
 					seperators,
 					resource,
 					from,
 					to
 			);
		}
 
 		public SimpleSeperatorAdapter(Context context, List<? extends Map<String, Object>> data,
 				int itemResource, String[] itemFrom, int[] itemTo,
 				int sepResource, String[] sepFrom, int[] sepTo) {
 
 			super(context, data, itemResource, itemFrom, itemTo);
 
 			this.seperatorAdapter = new SimpleAdapter(
 					context,
 					seperators,
 					sepResource,
 					sepFrom,
 					sepTo
 			);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			@SuppressWarnings("unchecked")
 			HashMap<String,Object> item = (HashMap<String, Object>) this.getItem(position);
 
 			if(isSeperator(item)) {
 				this.seperators.clear();
 				this.seperators.add(item);
 				this.seperatorAdapter.notifyDataSetChanged();
 				return this.seperatorAdapter.getView(0, null, parent);
 			}
 
 			return super.getView(position, null, parent);
 		}
 
 		private boolean isSeperator(HashMap<String,Object> item) {
 			Boolean isSep = (Boolean) item.get(IS_SEPERATOR_ITEM_KEY);
 
 			if(isSep == null || !isSep) {
 				return false;
 			}
 
 			return true;
 		}
 
 		private boolean isEnabled(HashMap<String,Object> item) {
 			Boolean isEn = (Boolean) item.get(IS_ENABLED_ITEM_KEY);
 
 			if(isEn == null) {
 				return true;
 			}
 
 			return isEn;
 		}
 
 		@Override
 		public boolean areAllItemsEnabled() {
 			return false;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public boolean isEnabled(int position) {
 			return isEnabled((HashMap<String, Object>) this.getItem(position));
 		}
 	}
 }
