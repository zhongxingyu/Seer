 package com.ioabsoftware.gameraven.views;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 
 import com.ioabsoftware.gameraven.views.rowview.AMPRowView;
 import com.ioabsoftware.gameraven.views.rowview.AdRowView;
 import com.ioabsoftware.gameraven.views.rowview.BoardRowView;
 import com.ioabsoftware.gameraven.views.rowview.GameSearchRowView;
 import com.ioabsoftware.gameraven.views.rowview.HeaderRowView;
 import com.ioabsoftware.gameraven.views.rowview.MessageRowView;
 import com.ioabsoftware.gameraven.views.rowview.PMDetailRowView;
 import com.ioabsoftware.gameraven.views.rowview.PMRowView;
 import com.ioabsoftware.gameraven.views.rowview.TopicRowView;
 import com.ioabsoftware.gameraven.views.rowview.TrackedTopicRowView;
 import com.ioabsoftware.gameraven.views.rowview.UserDetailRowView;
 
 public class ViewAdapter extends BaseAdapter {
 	
 	ArrayList<BaseRowData> rows;
 	Context context;
 
 	public ViewAdapter(Context contextIn, ArrayList<BaseRowData> rowsIn) {
 		context = contextIn;
 		rows = rowsIn;
 	}
 	
 	@Override
 	public int getCount() {
 		return rows.size();
 	}
 
 	@Override
 	public BaseRowData getItem(int position) {
 		return rows.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 	
 	@Override
 	public int getViewTypeCount() {
 		return RowType.values().length;
 	}
 	
 	@Override
 	public int getItemViewType(int position) {
		if (position < rows.size())
			return rows.get(position).getRowType().ordinal();
		else
			return IGNORE_ITEM_VIEW_TYPE;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		BaseRowView view;
 		BaseRowData data = rows.get(position);
 		
 		if (convertView != null) {
 			view = (BaseRowView) convertView;
 		}
 		else {
 			switch (data.getRowType()) {
 			case HEADER:
 				view = new HeaderRowView(context);
 				break;
 			case BOARD:
 				view = new BoardRowView(context);
 				break;
 			case GAME_SEARCH:
 				view = new GameSearchRowView(context);
 				break;
 			case MESSAGE:
 				view = new MessageRowView(context);
 				break;
 			case PM:
 				view = new PMRowView(context);
 				break;
 			case PM_DETAIL:
 				view = new PMDetailRowView(context);
 				break;
 			case TOPIC:
 				view = new TopicRowView(context);
 				break;
 			case AMP_TOPIC:
 				view = new AMPRowView(context);
 				break;
 			case TRACKED_TOPIC:
 				view = new TrackedTopicRowView(context);
 				break;
 			case USER_DETAIL:
 				view = new UserDetailRowView(context);
 				break;
 			case AD:
 				view = new AdRowView(context, data);
 				break;
 			default:
 				throw new IllegalArgumentException("row type not handled in ViewAdapter: " + data.getRowType().toString());
 			
 			}
 		}
 		
 		view.showView(data);
 		
 		return view;
 	}
 
 }
