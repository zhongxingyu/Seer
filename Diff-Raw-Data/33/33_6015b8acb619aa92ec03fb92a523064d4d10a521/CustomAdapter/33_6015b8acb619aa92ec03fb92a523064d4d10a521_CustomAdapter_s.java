 package com.suresh.whereismycash;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.support.v4.widget.CursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class CustomAdapter extends CursorAdapter implements OnClickListener {
 	
 	private DbHelper dbHelper;
 
 	public CustomAdapter(Context context, Cursor c, int flags, DbHelper dbHelper) {
 		super(context, c, flags);
 		this.dbHelper = dbHelper;
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		view.findViewById(R.id.btDelete).setOnClickListener(this);
 		float amount = cursor.getFloat(cursor.getColumnIndex(DbHelper.KEY_AMOUNT));
 		int color = 0;
 		if (amount < 0) {
 			amount *= -1;
 			color = R.color.amount_green;
 		} else if (amount == 0) {
 			color = R.color.amount_blue;
 		} else if (amount > 0) {
 			color = R.color.amount_red;
 		}
 		String name = cursor.getString(cursor.getColumnIndex(DbHelper.KEY_NAME));
 		
 		TextView tvName = (TextView) view.findViewById(R.id.tvName);
 		tvName.setText(name);
 		
 		TextView tvAmount = (TextView) view.findViewById(R.id.tvAmount);
 		tvAmount.setText(String.valueOf(amount));
 		tvAmount.setTextColor(context.getResources().getColor(color));
 		
 		view.findViewById(R.id.btDelete).setTag(name);
 	}
 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View v = inflater.inflate(R.layout.main_list_item, null);
 		bindView(v, context, cursor);
 		return v;
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btDelete:
 			dbHelper.delete((String)v.getTag());
			this.changeCursor(dbHelper.getAllLoans());
 			break;
 		}
 	}
 
 }
