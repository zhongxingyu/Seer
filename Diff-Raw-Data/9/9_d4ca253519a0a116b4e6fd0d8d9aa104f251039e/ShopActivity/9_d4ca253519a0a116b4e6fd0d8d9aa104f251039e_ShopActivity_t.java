 package sussex.ase.android.group5.views;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import sussex.ase.android.group5.util.MyAdapter;
 import sussex.ase.android.group5.views.MainActivity.LoginTask;
 
 import com.androidhive.googleplacesandmaps.R;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class ShopActivity extends Activity {
 	private String title;
 	private String message;
 	private double latitude;
 	private double longtitude;
 	private String comment;
 
 	List<Map<String, Object>> contents =  new ArrayList<Map<String, Object>>();
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_shop);
         
         Intent intent=getIntent();
         title=intent.getStringExtra("title");
         message=intent.getStringExtra("message");
         latitude=intent.getDoubleExtra("lati", 0);
         longtitude=intent.getDoubleExtra("longti", 0);
         TextView tTitle=(TextView) findViewById(R.id.title);
         TextView tMessage=(TextView) findViewById(R.id.message);
         TextView geo=(TextView) findViewById(R.id.geo);
         tTitle.setText(title);
         tMessage.setText(message);
         geo.setText("laititude :"+latitude+" longtitude :"+longtitude);
         
         Button sendComment= (Button) findViewById(R.id.button1);
         sendComment.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				EditText editComment= (EditText) findViewById(R.id.comment);
 		        comment=editComment.getText().toString();
 		        if(comment!="")
 		        {
 		        }
 
 			}
 		});
 	
         
         
         
         
         for (int i = 0; i < 5; i++) {
 			Map<String, Object> map = new HashMap<String, Object>();
 			map.put("username", "username"+i);
 			map.put("comment", "comment"+i);
 			map.put("tlike", i);
 			map.put("tdislike", 10);
			map.put("tdislike",i);
 			contents.add(map);
 		}
 		MyAdapter adapter=new MyAdapter(this,contents);
 
 		ListView list = (ListView)findViewById(R.id.list);
 		list.setAdapter(adapter);
         
         
 		
 		
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_shop, menu);
         return true;
     }
 }
