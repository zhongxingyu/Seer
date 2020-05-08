 package net.miscjunk.aamp;
 
 import net.miscjunk.aamp.common.Playlist;
 import net.miscjunk.aamp.common.Song;
 import android.content.Context;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.os.Messenger;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class SongDisplay extends ListView implements OnScrollListener {
 	private SongAdapter adapter;
     public SongDisplay(Context context, Handler handler) {
 		super(context);
 		adapter = new SongAdapter(handler);
 		setOnItemClickListener(adapter);
 		setOnScrollListener(this);
 		setAdapter(adapter);
 	}
   
     public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
         boolean loadMore = firstVisible + visibleCount >= totalCount;
         if(loadMore) {
             adapter.cycle(visibleCount / 2); // or any other amount
             adapter.notifyDataSetChanged();
         }
     }
 
     public void onScrollStateChanged(AbsListView v, int s) { }    
 
     public class SongAdapter extends BaseAdapter implements Callback, 
     	AdapterView.OnItemClickListener {
         int count = 40;
         Playlist list;
         public Handler mHandler;
         private Handler bg;
         public SongAdapter(Handler bg) {
         	this.list = new Playlist();//init to something non null, dynamic load later
         	mHandler = new Handler(this);
         	Message giveMeData = Message.obtain(bg, ProxyUIBridge.GET_ALL_SONGS);
         	giveMeData.replyTo = new Messenger(this.mHandler);
         	retries = 6;
         	bg.sendMessage(giveMeData);
         	this.bg = bg;
 		}
                
         public void cycle(int i) {
         	if(count < list.size()) {
         		count += 40;
         	}
 		}
         
 		public Object getItem(int pos) { return list.getSong(pos); }
         public long getItemId(int pos) { return pos; }
 
         public View getView(int pos, View convertView, ViewGroup p) {
         		Song item = (Song)getItem(pos);
         		SongView view = new SongView(getContext(), item.getId());
         	    view.setClickable(false);
         	    view.setFocusable(false);
                 view.setText(item.getTitle());
                 view.setHeight(50);
                 return view;
         }
         
 
     	@Override
     	public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
     		System.out.println("GOT A CLICK");
     		SongView song = (SongView) v;
     		Message.obtain(bg, ProxyUIBridge.SKIP_TO, song.getSongId()).sendToTarget();
     	}
 
 		@Override
 		public int getCount() {
 			return Math.min(count, list.size());
 		}
 
 		int retries = 0;
 		@Override
 		public boolean handleMessage(Message msg) {
 			if(msg.what == ProxyUIBridge.GET_ALL_SONGS && msg.obj != null
 					&& ((Playlist) msg.obj).size() > 0) {//We got them back yay
 				this.list.append((Playlist)msg.obj); // #WOOT
				System.out.println("GOT DATA SWEET BABY JESUS");
 				this.notifyDataSetChanged();
 			}else if(msg.what == ProxyUIBridge.GET_ALL_SONGS && retries > 0) {
 	        	Message giveMeData = Message.obtain(bg, ProxyUIBridge.GET_ALL_SONGS);
 	        	giveMeData.replyTo = new Messenger(this.mHandler);
 	        	bg.sendMessageDelayed(giveMeData, 500);
 	        	retries--;
 				System.out.println("Try again more times : " + retries);
 			}else if(msg.what == ProxyUIBridge.USE_PLAYLIST) {
 				this.count = 40;
 				this.list = (Playlist) msg.obj;
 				this.notifyDataSetChanged();
 			}
 			return false;
 		}
     }
     
     public class SongView extends TextView {
     	private String songId;
     	public SongView(Context context, String songID) {
     		super(context);
     		this.songId = songID;
     		
     		setPadding(10, 0, 0, 0);
     	}
     	
     	String getSongId() {return songId; }
     	void setSongId(String id) { songId = id; }
     }
 }
