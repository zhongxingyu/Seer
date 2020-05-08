 package com.android.common;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
import com.google.gson.internal.bind.ArrayTypeAdapter;

 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Adapter;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 
 public class SeparatedListAdapter extends BaseAdapter
 {
 	public final Map<Integer, Adapter> _sections = new LinkedHashMap<Integer, Adapter>();
 	public final ArrayAdapter<String> _headers;
 	public final ArrayList<Integer> _sectionIds = new ArrayList<Integer>();
 	
 	public final static int TYPE_SECTION_HEADER = 0;
 
 	public SeparatedListAdapter(int headerViewResource, Context context)
 	{
 		_headers = new ArrayAdapter<String>(context, headerViewResource);
 	}
 
 	public void addSection(Integer id, String section, Adapter adapter)
 	{
 		this._headers.add(section);
 		this._sectionIds.add(id);
 		this._sections.put(id, adapter);
 	}
 	
 	public Integer getSectionId(String section) {
 		int position = _headers.getPosition(section);
 		
 		if(position >= 0 && position < _sectionIds.size())
 			return _sectionIds.get(_headers.getPosition(section));
 		
 		return -1;
 	}
 
 	public Object getItem(int position)
 	{
 		for (Object section : this._sections.keySet())
 		{
 			Adapter adapter = _sections.get(section);
 			int size = adapter.getCount() + 1;
 			
 			// check if position inside this section
 			if (position == 0) return section;
 			if (position < size) return adapter.getItem(position - 1);
 			
 			// otherwise jump into next section
 			position -= size;
 		}
 		return null;
 	}
 	
 	public int getCount()
 	{
 		// total together all sections, plus one for each section header
 		int total = 0;
 		for (Adapter adapter : this._sections.values())
 			total += adapter.getCount() + 1;
 		return total;
 	}
 	
 	@Override
 	public int getViewTypeCount()
 	{
 		// assume that headers count as one, then total all sections
 		int total = 1;
 		for (Adapter adapter : this._sections.values())
 			total += adapter.getViewTypeCount();
 		return total;
 	}
 	
 	@Override
 	public int getItemViewType(int position)
 	{
 		int type = 1;
 		for (Object section : this._sections.keySet())
 		{
 			Adapter adapter = _sections.get(section);
 			int size = adapter.getCount() + 1;
 			
 			// check if position inside this section
 			if (position == 0) return TYPE_SECTION_HEADER;
 			if (position < size) return type + adapter.getItemViewType(position - 1);
 			
 			// otherwise jump into next section
 			position -= size;
 			type += adapter.getViewTypeCount();
 		}
 		return -1;
 	}
 	
 	public boolean areAllItemsSelectable()
 	{
 		return false;
 	}
 	
 	@Override
 	public boolean isEnabled(int position)
 	{
 		return (getItemViewType(position) != TYPE_SECTION_HEADER);
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		int sectionnum = 0;
 		for (Integer sectionId : this._sections.keySet())
 		{
 			Adapter adapter = _sections.get(sectionId);
 			int size = adapter.getCount() + 1;
 			
 			// check if position inside this section
 			if (position == 0) return _headers.getView(sectionnum, convertView, parent);
 			if (position < size) return adapter.getView(position - 1, convertView, parent);
 			
 			// otherwise jump into next section
 			position -= size;
 			sectionnum++;
 		}
 		return null;
 	}
 	
 	public View getHeaderView(int position, View convertView, ViewGroup parent) {
 		int sectionnum = 0;
 		for (Integer sectionId : this._sections.keySet())
 		{
 			Adapter adapter = _sections.get(sectionId);
 			int size = adapter.getCount() + 1;
 			
 			// check if position inside this section
 			if (position < size) return _headers.getView(sectionnum, convertView, parent);
 			
 			// otherwise jump into next section
 			position -= size;
 			sectionnum++;
 		}
 		return null;
 	}
 	
 	public long getItemId(int position)
 	{
 		return position;
 	}
 	
 	public Class<? extends Adapter> getAdapterType(int position) {		
 		for (Object section : this._sections.keySet())
 		{
 			Adapter adapter = _sections.get(section);
 			int size = adapter.getCount() + 1;
 			
 			// check if position inside this section
 			if (position == 0) return null;
 			if (position < size) return adapter.getClass();
 			
 			// otherwise jump into next section
 			position -= size;
 		}
 		return null;		
 	}
 }
 		
