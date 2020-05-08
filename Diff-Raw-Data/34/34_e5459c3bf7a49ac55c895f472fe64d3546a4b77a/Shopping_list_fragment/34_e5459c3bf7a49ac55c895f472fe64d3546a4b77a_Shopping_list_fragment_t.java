 package com.cookcook.main.socialfragment;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.view.View.OnClickListener;
 
 import com.cookcook.main.R;
 
 public class Shopping_list_fragment extends Fragment{
 
 	public ArrayList<Model> list_model =new ArrayList<Model>();
 	ListView list;
 	ShoppingListAdapter adapter;
	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		View rootView = inflater.inflate(R.layout.shopping_list_layout, container, false);
 		
 		//Button Add Item
 		Button btn_add_item_shopping_list =(Button)rootView.findViewById(R.id.btn_add_item_shopping_list);
 		btn_add_item_shopping_list.setOnClickListener(OnClick_Add_Item_Shopping_List);
 		list = (ListView)rootView.findViewById(R.id.list_shopping_list);
 		
 		//Button Select All Item
 		Button btn_select_all_shopping_list =(Button)rootView.findViewById(R.id.btn_select_all_shopping_list);
 		btn_select_all_shopping_list.setOnClickListener(OnClick_Select_All_Shopping_List);
 		list = (ListView)rootView.findViewById(R.id.list_shopping_list);
 		
 		//Button Deselect All Item
 		Button btn_deselect_all_shopping_list =(Button)rootView.findViewById(R.id.btn_deselect_all_shopping_list);
 		btn_deselect_all_shopping_list.setOnClickListener(OnClick_Deselect_All_Shopping_List);
 		list = (ListView)rootView.findViewById(R.id.list_shopping_list);
 		
 		//Button Remove Checked
 		Button btn_remove_checked_shopping_list =(Button)rootView.findViewById(R.id.btn_remove_checked_shopping_list);
 		btn_remove_checked_shopping_list.setOnClickListener(OnClick_Remove_Checked_Shopping_List);
 		list = (ListView)rootView.findViewById(R.id.list_shopping_list);
 		
 		adapter = new ShoppingListAdapter(getActivity(), list_model);
 		list.setAdapter(adapter);
 //		list.setOnItemClickListener();
         return rootView;
 	}

 	private final OnClickListener OnClick_Add_Item_Shopping_List = new OnClickListener() 
 	{
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 //			adb.setTitle();
 			adb.setMessage("Add new shopping item");
 			
 			//Set view EditText on Dialog
 			final EditText input =new EditText(getActivity());
 			adb.setView(input);
 			
 			//View Button Ok, Cancel
 			adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					String value = input.getText().toString();
 					Model element = new Model(value);
 					list_model.add(element);
 					adapter.notifyDataSetChanged();
 				}
 			});
 			
 			adb.setNegativeButton("Cancel", null);
 			adb.show();
 		}
 	};
 	
 	private final OnClickListener OnClick_Select_All_Shopping_List = new OnClickListener() 
 	{
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			for (int i=0; i< list_model.size(); i++)
 			{
 				list_model.get(i).setSelected(true);
 			}
 			adapter.notifyDataSetChanged();
 		}
 	};
 	
 	private final OnClickListener OnClick_Deselect_All_Shopping_List = new OnClickListener() 
 	{
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			for (int i=0; i< list_model.size(); i++)
 			{
 				list_model.get(i).setSelected(false);
 			}
 			adapter.notifyDataSetChanged();
 		}
 	};
 	
 	private final OnClickListener OnClick_Remove_Checked_Shopping_List = new OnClickListener() 
 	{
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
			for (int i= list_model.size()-1 ; i >=0 ; i--)
 			{
 				if (list_model.get(i).getSelected() ==true)
 				{
 					list_model.remove(i);
 				}
 			}
 			adapter.notifyDataSetChanged();
 		}
 	};
 		
 }
