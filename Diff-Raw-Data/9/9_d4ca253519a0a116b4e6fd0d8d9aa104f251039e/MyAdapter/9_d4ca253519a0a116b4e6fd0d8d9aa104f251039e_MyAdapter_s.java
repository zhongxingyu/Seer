 package sussex.ase.android.group5.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.androidhive.googleplacesandmaps.R;
 
 
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MyAdapter extends BaseAdapter{
 	 
     private LayoutInflater mInflater;
     private List<Map<String, Object>> mData; 
   
     
     private ArrayList<TextView> cashe1 =new ArrayList<TextView>();
     private ArrayList<TextView> cashe2 =new ArrayList<TextView>();
  
     private ViewHolder holder;
     public MyAdapter(Context context,List<Map<String, Object>> list){
         this.mInflater = LayoutInflater.from(context);
         mData=list;
         
         
     }
     public int getCount() {
         // TODO Auto-generated method stub
         return mData.size();
     }
 
     public Object getItem(int arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public long getItemId(int arg0) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public View getView(int position,View convertView, ViewGroup parent) {
          final int tempPosition=position;
     	
        
         if (convertView == null) {
              
             holder=new ViewHolder();  
              
             convertView = mInflater.inflate(R.layout.list_item, null);
             holder.username = (TextView)convertView.findViewById(R.id.username);
             holder.comment = (TextView)convertView.findViewById(R.id.comment);
             holder.tlike = (TextView)convertView.findViewById(R.id.tlike);
             cashe1.add(holder.tlike);
             holder.tdislike = (TextView)convertView.findViewById(R.id.tdislike);
             cashe2.add(holder.tdislike);
             holder.bLike = (Button)convertView.findViewById(R.id.blike);
             holder.bDis = (Button)convertView.findViewById(R.id.bdislike);
             convertView.setTag(holder);
              
         }else {
              
             holder = (ViewHolder)convertView.getTag();
         }
          
          
         
         holder.username.setText((String)mData.get(position).get("username"));
         holder.comment.setText((String)mData.get(position).get("comment"));
         
         holder.tlike.setText(String.valueOf((Integer)mData.get(position).get("tlike")));
         
         holder.tdislike.setText(String.valueOf((Integer)mData.get(position).get("tdislike")));
         
         
         holder.bLike.setOnClickListener(new View.OnClickListener() {
         	
             
             public void onClick(View v) {
             
             	TextView t=cashe1.get(tempPosition);
             	int a=Integer.parseInt(t.getText().toString());
             	t.setText(String.valueOf(a+1));
             	
             }
         });
         holder.bDis.setOnClickListener(new View.OnClickListener() {
              
             
             public void onClick(View v) {
             	TextView t=cashe2.get(tempPosition);
             	int a=Integer.parseInt(t.getText().toString());
             	t.setText(String.valueOf(a+1));
             	
             
             	
             }
         });
          
          
         return convertView;
     }
      
 }
