 package tu.kom.uhg;
 
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Bundle;
 
 import java.util.HashMap;
 import android.widget.ImageView;
 import android.widget.MediaController;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 import android.view.View;
 
 public class ParkourActivity extends GenericActivity {
 	
 	@SuppressWarnings("serial")
 	HashMap<String, String[]> stations2data = new HashMap<String, String[]>(){{
 		put("1", new String[] {
 				"video", 
 				"Steps", 
 				"Spring abwechselnd mit deinen Füßen auf die Stange. Beginne mit 15 Wiederholungen pro Fuß.",
 		});
 		put("2", new String[] {
 				"image", 
 				"Wanddrücken", 
 				"Drücke deinen Oberkörper gegen die Wand, sodass deine Füße im 90° Winkel zum Boden stehen. Halte diese position zu Beginn für 30 Sekunden",
 		});
 		put("3", new String[] {
 				"video", 
 				"Liegestützen", 
 				"Lege deine Hände auf die Kübel und beginne mit Liegestützen. Beginne mit 10 Wiederholungen.",
 		});
 		put("4", new String[] {
 				"video", 
 				"Mountain Climbers", 
 				"Lege deine Hände auf die Sitzlehne der Bank und die Füße auf den Boden. Imitiere nun einen Bergsteiger. Beginne mit 30 Wiederholungen.",
 		});
 		put("5", new String[] {
 				"video", 
 				"Cross Trainer", 
 				"Benutze den Crosstrainer und nutze ihn zu beginn für 30 Sekunden.",
 		});
 		put("6", new String[] {
 				"video", 
 				"Nackenkreisen", 
 				"Lege deine Hände auf die Drehscheibe und drehe zu Beginn 50 Runden.",
 		});
 		put("7", new String[] {
 				"image", 
 				"Beinheben", 
 				"Setz dich auf die Tischtennisplatten und halte deinen Oberkörper gerade. Hebe nun deine Beine zu einem 90° Winkel an und halte die Position zu Beginn 30 Sekunden.",
 		});
 		put("8", new String[] {
 				"image", 
 				"Beinheben 2", 
 				"Lege deine Hände auf die Lehne der Bank und strecke jeweils ein Bein zum 90° Winkel an und halte die Position. Zu Beginn 15 Sekunden pro Bein.",
 		});
 		put("9", new String[] {
 				"video", 
 				"Schwimmen", 
 				"Lege dich mt den oberkörper auf die Bank. Rudere mit den Beinen zu Beginn 50 Mal.",
 		});
 		put("10", new String[] {
 				"video", 
 				"Wandschieben", 
 				"Lege deine Hände auf die Mauer und verlager dein Gewicht nach vorn. Drücke dich nun 30 Mal von der Wand weg.",
 		});
 		put("11", new String[] {
 				"video", 
 				"Rückseitiger Stütz", 
 				"Lege deine Händer mit dem Rücken zur Bank auf die Sitzufläche. Hebe und Senke nun deinen Oberkörper 15 Mal.",
 		});
 	}};
 	
 	//hashmap for Chillpoints
 	@SuppressWarnings("serial")
 	HashMap<String, String[]> chillpoints2data = new HashMap<String, String[]>(){{
 		put("1", new String[] {
 				"image", 
 				"Mundraub", 
 				"Um zu neuen Kräften zu kommen bist du hier genau richtig. Die Apfelbäume laden zum Ernten und Verzehr ein, die Bänke bieten dir Sitzmöglichkeiten für eine angenehme Auszeit.",
 		});
 		put("2", new String[] {
 				"image", 
 				"Weiher", 
 				"Im Herrngarten gibt es kaum einen idyllischeren Ort als den Weiher um sich zwischen Luft, Natur und Wasser gut zu erholen.",
 		});
 	}};
 	@SuppressWarnings("serial")
 	HashMap<String, String[]> parkourb2data = new HashMap<String, String[]>(){{
 		put("1", new String[] {
 				"video", 
 				"Aufwärmen 1", 
 				"Der Rücken wird durch das Rotieren der angewinkelteten Arme um die Körperachse aufgewärmt. 10 Wiederholungen.",
 		});
 		put("2", new String[] {
 				"video", 
 				"Aufwärmen 2", 
 				"Ergänzend zur ersten Übung wird das Aufwärmen durch das strecken der Arme bei gerundetem Rücken beendet. 10 Wiederholungen.",
 		});
 		put("3", new String[] {
 				"video", 
 				"Schieben und Ziehen", 
 				"In der Grundposition (Beine Hüftbreit auseinander, Gesäß nach hinten und Rücken Gerade nach vorne gestreckt) wird das Schieben und Ziehen eines Gegenstandes simultiert. 15 Wiederholungen.",
 		});
 		put("4", new String[] {
 				"video", 
 				"Schwimmen", 
 				"In der Grundposition (Beine Hüftbreit auseinander, Gesäß nach hinten und Rücken Gerade nach vorne gestreckt) werden beide Arme nach vorne gestreckt und gegensätzlich zueinander nach oben/unten gehoben. 20 Wiederholungen.",
 		});
 		put("5", new String[] {
 				"video", 
 				"Körperneigen", 
 				"In der Grundposition (Beine Hüftbreit auseinander, Gesäß nach hinten und Rücken Gerade nach vorne gestreckt) wird der Körper nach unten und zurück in die Startposition gehoben. 15 Wiederholungen.",
 		});
 		put("6", new String[] {
 				"video", 
 				"Rotation", 
 				"In der Grundposition (Beine Hüftbreit auseinander, Gesäß nach hinten und Rücken Gerade nach vorne gestreckt) wird der Oberkörper nach unten gesenkt. Die Arme werden senkrecht zum Körper nach oben gehalten und der Oberkörper rotiert. 15 Wiederholungen pro Seite.",
 		});
 	}};
 	
 	String viewSuffix;
 	String markerTitle;
 	String stationNumber;
 	String mediaType;
 	String headerText;
 	String hintText;
 	int mediaId;
 	int viewId;
 	int slideNmbr;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Bundle extras = getIntent().getExtras();
 		markerTitle = extras.getString("markerTitle");
 		
 		if (markerTitle.equals("Parkour B")){			
 			showParkourWithMultipleMedia();
 		} else {
 			showParkourWithSingleMedia();
 		}
 		
 		
 	}
 	
 	private void showParkourWithMultipleMedia() {
 		// TODO Auto-generated method stub
 		showHint("Parkour B");	
 		setContentView(R.layout.activity_parkour_slideshow);
 		slideNmbr = 1;
 		showSlide();
 	}
 	
 	
 
 	private void showSlide() {
 		// TODO Auto-generated method stub
 		//slideNmbr to string
 		String slideNmbr_string =  String.valueOf(slideNmbr);
 		viewSuffix = parkourb2data.get(slideNmbr_string)[0];
 		headerText = parkourb2data.get(slideNmbr_string)[1];
 		hintText = parkourb2data.get(slideNmbr_string)[2];	
 		mediaType = (viewSuffix.equals("video")) ? "mp4" : "png";
 		mediaId = getResources().getIdentifier("raw/parkour_b_"+slideNmbr_string+viewSuffix, mediaType, getPackageName());
 
 		TextView hintTextView = (TextView)findViewById(R.id.hint);
 		TextView headerTextView = (TextView)findViewById(R.id.header);
 		
 		hintTextView.setText(hintText);
 		headerTextView.setText(headerText);
 		
 		VideoView video = (VideoView)findViewById(R.id.video1);
 		Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+mediaId);
 		
 		video.setVideoURI(uri);
 		video.start();
 		
 		video.setOnPreparedListener (new OnPreparedListener() {                    
 		    @Override
 		    public void onPrepared(MediaPlayer mp) {
 		        mp.setLooping(true);
 		    }
 		});
 	}
 
 	private void showParkourWithSingleMedia() {
 		
 		if(markerTitle.contains("Station")) {
 			stationNumber = markerTitle.substring(8);					
 			viewSuffix = stations2data.get(stationNumber)[0];
 			headerText = stations2data.get(stationNumber)[1];
 			hintText = stations2data.get(stationNumber)[2];	
 			mediaType = (viewSuffix.equals("video")) ? "mp4" : "png";
 			viewId = getResources().getIdentifier("layout/activity_parkour_"+viewSuffix, "xml", getPackageName());
 			mediaId = getResources().getIdentifier("raw/parkour_a_station"+stationNumber+viewSuffix, mediaType, getPackageName());
 		} else if(markerTitle.contains("Chillpoint"))  {
 			stationNumber = markerTitle.substring(11);			
 			viewSuffix = chillpoints2data.get(stationNumber)[0];
 			headerText = chillpoints2data.get(stationNumber)[1];
			hintText = chillpoints2data.get(stationNumber)[2];
 			mediaType = (viewSuffix.equals("video")) ? "mp4" : "png";
 			viewId = getResources().getIdentifier("layout/activity_parkour_"+viewSuffix, "xml", getPackageName());
 			mediaId = getResources().getIdentifier("raw/chillpoint"+stationNumber+viewSuffix, mediaType, getPackageName());	
 		}
 		
 		setContentView(viewId);
 		
 		TextView hintTextView = (TextView)findViewById(R.id.hint);
 		TextView headerTextView = (TextView)findViewById(R.id.header);
 		
 		hintTextView.setText(hintText);
 		headerTextView.setText(headerText);
 			
 		if (viewSuffix.equals("image")){
 			ImageView image = (ImageView)findViewById(R.id.picture1);
 			image.setImageResource(mediaId);
 		} else if (viewSuffix.equals("video")){
 			VideoView video = (VideoView)findViewById(R.id.video1);
 			Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+mediaId);
 			
 			video.setVideoURI(uri);
 			video.start();
 			
 			video.setOnPreparedListener (new OnPreparedListener() {                    
 			    @Override
 			    public void onPrepared(MediaPlayer mp) {
 			        mp.setLooping(true);
 			    }
 			});
 			
 		}else {
 			showHint("not implemented yet");
 		}		
 	}
 	
 	public void nextSlide(View view) {
 		slideNmbr++;
 		if (slideNmbr >= parkourb2data.size())
 			slideNmbr = parkourb2data.size() - 1;		
 		showSlide();		
 	}
 	
 	public void prevSlide(View view) {
 		slideNmbr--;
 		if (slideNmbr < 1)
 			slideNmbr = 1;
 		showSlide();
 	}
 
 	public void finisher(View view) {
 		addPoints();
 		finish();
 	}
 
 	private void addPoints() {
 		// TODO Auto-generated method stub
 		showHint("Score successfully updated");
 	}
 }
