 package com.jangonera.oscilloscope;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.example.oscilloscope.R;
 import com.example.oscilloscope.R.string;
 import com.jangonera.oscilloscope.ExternalDataContainer.Probe;
 import com.jangonera.oscilloscope.customview.IconView;
 
 public class GraphsFragment extends Fragment {
 	// Contains a list of graphs. A graphs is used by a probe to draw on it.
 	private MainActivity context;
 	// private ArrayList<Graph> graphs;
 	private boolean drawing;
 	private BaseAdapter listAdapter;
 	private ListView listOfGraphs;
     private TextView messageGraphsEmpty;
 	private ExternalDataContainer externalDataContainer;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		context = (MainActivity) getActivity();
 		setDrawing(false);
 		externalDataContainer = ExternalDataContainer.getContainer();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View content = inflater.inflate(R.layout.fragment_graphs, container,
 				false);
 		return content;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		loadListOfGraphs();
 	}
 
 	private void loadListOfGraphs() {
         messageGraphsEmpty = (TextView) context
                 .findViewById(R.id.graphs_message_empty);
 		listOfGraphs = (ListView) context
 				.findViewById(R.id.graphs_listOfGraphs);
 		listAdapter = new BaseAdapter() {
 
 			@Override
 			public View getView(int position, View convertView,
 					ViewGroup viewGroup) {
 				return getGraphView(position, convertView, viewGroup);
 			}
 
 			@Override
 			public long getItemId(int index) {
 				return index;
 			}
 
 			@Override
 			public Object getItem(int index) {
 				return externalDataContainer.getReadyProbe(index);
 			}
 
 			@Override
 			public int getCount() {
                 int quantity = externalDataContainer.readyDeviceQuantity();
                 if(messageGraphsEmpty!= null)
                     if(quantity>0) messageGraphsEmpty.setVisibility(View.INVISIBLE);
                     else messageGraphsEmpty.setVisibility(View.VISIBLE);
 				return quantity;
 			}
 		};
 		listOfGraphs.setAdapter(listAdapter);
 	}
 
 	public void invalidateList() {
 		if (listAdapter != null) {
 			listAdapter.notifyDataSetChanged();
 			//listOfGraphs.invalidate();
 		}
 	}
 
 	public boolean hasNothingToDisplay() {
 		return externalDataContainer.hasNothingToDisplay();
 	}
 	
 	public boolean isDrawing() {
 		return drawing;
 	}
 
 	public void setDrawing(boolean drawing) {
 		this.drawing = drawing;
 	}
 
 	// Methods related to graphs
 	public View getGraphView(int position, View convertView, ViewGroup viewGroup) {
 		// If none convert not available, create new
 		if (convertView == null) {
 			convertView = context.getLayoutInflater().inflate(
 					R.layout.listitem_graph_graphs, viewGroup, false);
 		}
 
 		convertView.setId(position);
 		
 		Probe readyProbe = externalDataContainer.getReadyProbe(position);
 		
 		TextView tvName = (TextView) convertView
 				.findViewById(R.id.graphs_probe_name);
 		tvName.setText(readyProbe.getName());
 
 		TextView tvAddress = (TextView) convertView
 				.findViewById(R.id.graphs_probe_address);
 		tvAddress.setText(readyProbe
 				.getAddress());
 		TextView tvState = (TextView) convertView
 				.findViewById(R.id.graphs_probe_state);
 		if(readyProbe.isActive()) tvState.setText(context.getResources().getString(string.graphs_state_connected));
 		else tvState.setText(context.getResources().getString(string.graphs_state_disconnected));
 		((IconView) convertView.findViewById(R.id.graphs_close_graph))
 				.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						context.removeGraph(((View) v.getParent()).getId());
 					}
 				});
 
 		// Add graph to the view. Graph is taken from the arraylist within
 		// external data container
 		Graph graph = new Graph(context);
 		graph.registerProbe(readyProbe);
 		
 		
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 		params.addRule(RelativeLayout.BELOW, R.id.graphs_probe_state);
 		params.setMargins(10, 10, 10, 10);
 		
 
 		params.addRule(RelativeLayout.BELOW, R.id.graphs_probe_address);
 		((ViewGroup) convertView).addView(graph, params);
 		// Log.i(Const.tag_GF, "graphs added");
 		return convertView;
 	}
 
 	// Graph represents the area on which a ready-probe can draw information
 	// public class Graph extends View{
 	// private Paint paint;
 	//
 	// public Graph(Context context, AttributeSet attrs, int defStyle) {
 	// super(context, attrs, defStyle);
 	// }
 	// public Graph(Context context, AttributeSet attrs) {
 	// super(context, attrs);
 	// }
 	// public Graph(Context context) {
 	// super(context);
 	// }
 	// @Override
 	// protected void onDraw(Canvas canvas) {
 	// Log.i(Const.tag_GF, "onDraw Called");
 	// super.onDraw(canvas);
 	// //canvas.drawLine(0, 0, 20, 20, paint);
 	// //canvas.drawLine(20, 0, 0, 20, paint);
 	// }
 	//
 	// }
 }
