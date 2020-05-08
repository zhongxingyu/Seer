 package com.example.tvshowcrawler;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class TVShowAdapter extends ArrayAdapter<TVShow>
 {
 	public TVShowAdapter(Context context, int textViewResourceId, List<TVShow> objects)
 	{
 		super(context, textViewResourceId, objects);
 		this.items = objects;
 		this.context = context;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		View view = convertView;
 		if (view == null)
 		{
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(R.layout.list_row, null);
 
 			ImageView status_icon = (ImageView) view.findViewById(R.id.status_icon);
 			status_icon.setOnClickListener(new OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					Integer position = (Integer) v.getTag();
 					TVShow item = items.get(position);
 					if (item != null)
 					{
 						// start UpdateShowService to refresh listView contents
 						Intent intent = new Intent(getContext(), UpdateShowService.class);
 						intent.setAction(UpdateShowService.BROADCAST_START_SHOW_ACTION);
 						intent.putExtra("com.example.tvshowcrawler.tvShow", item);
 						TVShowAdapter.this.getContext().startService(intent);
 					}
 				}
 			});
 		}
 
 		TVShow item = items.get(position);
 		if (item != null)
 		{
 			TextView title = (TextView) view.findViewById(R.id.title);
 			TextView status = (TextView) view.findViewById(R.id.status);
 			TextView last_episode_content = (TextView) view.findViewById(R.id.last_episode_content);
 			TextView next_episode_content = (TextView) view.findViewById(R.id.next_episode_content);
 			TextView current = (TextView) view.findViewById(R.id.current);
 			ImageView status_icon = (ImageView) view.findViewById(R.id.status_icon);
 			status_icon.setTag(position);
 
 			title.setText(item.getName());
 			status.setText(item.getShowStatus());
 			if (item.getLastEpisode() != null)
 				last_episode_content.setText(item.getLastEpisode().toString());
 			else
 				last_episode_content.setText("-");
 			if (item.getNextEpisode() != null)
 				next_episode_content.setText(item.getNextEpisode().toString());
 			else
 				next_episode_content.setText("-");
 			current.setText(String.format("S%02dE%02d", item.getSeason(), item.getEpisode()));
 
 			// update icon
 			switch (item.getStatus())
 			{
 			case Error:
 				status_icon.setImageResource(R.drawable.error);
 				break;
 			case NewEpisodeAvailable:
 				status_icon.setImageResource(R.drawable.new_episode_available);
 				break;
 			case NotChecked:
 				status_icon.setImageResource(R.drawable.not_checked);
 				break;
 			case TorrentNotFound:
				status_icon.setImageResource(R.drawable.error);
 				break;
 			case UpToDate:
 				status_icon.setImageResource(R.drawable.uptodate);
 				break;
 			case Working:
 				status_icon.setImageResource(R.drawable.working);
 				break;
 			default:
 				break;
 			}
 		}
 
 		return view;
 	}
 
 	private List<TVShow> items;
 
 	private Context context;
 
 	private static final String TAG = "TVShowAdapter";
 }
