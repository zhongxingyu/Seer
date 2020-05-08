 package com.portfolio.components;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 import com.portfolio.MainActivity;
 import com.portfolio.R;
 import com.portfolio.activities.ContactActivity;
 import com.portfolio.activities.NetworkActivity;
 import com.portfolio.activities.PhotoTextListActivity;
 import com.portfolio.activities.PhotoTextListTwoRowsActivity;
 import com.portfolio.activities.TextActivity;
 import com.portfolio.model.PortfolioModel;
 import com.portfolio.model.interfaces.IPage;
 
 public class menu extends LinearLayout {
         
      
         private LinearLayout menu;
         private TextActivity ta;
         
         public menu(Context context) {
                 super(context);
                 init();
         }
 
         public menu(Context context, AttributeSet attrs) {
                 super(context, attrs);
         }
         
         public void init() {
         	
         	Typeface tf = Typeface.createFromAsset(getContext().getAssets(),"fonts/CopperGothicStd29AB.otf");
         	final PortfolioModel portfolioModel = PortfolioModel.getInstance(getContext());
         	List <String> titles = (List<String>)portfolioModel.getPagesTitles();
         	List <Integer> posicion = (List<Integer>) portfolioModel.getPagesPositions();
         	for(int index=0;index<titles.size();index++  ){
         		String title= titles.get(index);
         		int pos = posicion.get(index);
         		Button but = new Button(getContext());
         		but.setBackgroundResource( R.drawable.custom_menu_1);
         		but.setTextColor(getResources().getColor(R.color.borderGold));
         		but.setHeight(80);
         		but.setTypeface(tf);
         		but.setText(title);
         		but.setTag(pos);
         		but.setOnClickListener(new OnClickListener() {
                     
                     @Override
                     public void onClick(View v) {
                         int pos = (Integer) v.getTag();    
                     	IPage page = portfolioModel.getPageInfo(pos);
                     	 switch (page.getType().getTypeValue()) {
                          
      	               /* case IPage.type_video:
      	                	Intent intent1 = new Intent(getContext(), VideoActivity.class);
      	                    intent1.putExtra("position", 1);
      	                    getContext().startActivity(intent1);
      	                    break;
      	                //listas
      	                case IPage.type_photo_galery:
                          	Intent intent2 = new Intent(MainActivity.this, PhotoTextListActivity.class);
                              intent2.putExtra("position", 2);
                              startActivity(intent2);
                              break;
                         
                  		
                          //la home
                          case IPage.type_image:
                          	Intent intent5 = new Intent(MainActivity.this, TextActivity.class);
                              intent5.putExtra("position", 5);
                              startActivity(intent5);
                              break;
                          //contacto  
                          */
                     	//HOME
        	                case IPage.type_image:
                                    Intent intent4 = new Intent(getContext(), TextActivity.class);
                                    intent4.putExtra("position",pos);
                                    getContext().startActivity(intent4);
                                    break;
                                    
        	                case IPage.type_photo_galery:
        	                	Intent intent2 = new Intent(getContext(), PhotoTextListTwoRowsActivity.class);
       	                	intent2.putExtra("position", pos);
        	                	getContext().startActivity(intent2);
        	                	break;
                     	 //REDES SOCIALES
       	                 case IPage.type_network:
       	                	 	Intent intent3 = new Intent(getContext(), NetworkActivity.class);
       	                	 	intent3.putExtra("position", pos);
       	                	 	getContext().startActivity(intent3);
       	                	 	break;
       	                //CONTACTO
       	                 case IPage.type_contact:
                          	Intent intent6 = new Intent(getContext(), ContactActivity.class);
                              intent6.putExtra("position", pos);
                              getContext().startActivity(intent6);
                              break;
                         //listas de imagenes con textos
      	                case IPage.type_text:
                                  Intent intent5 = new Intent(getContext(), PhotoTextListActivity.class);
                                 intent5.putExtra("position", pos);
                                  getContext().startActivity(intent5);
                                  break;
                                                
                          default:
                                  break;
                          }
                     	 
                     }
             });
         	LinearLayout linear = (LinearLayout) findViewById(R.id.layout);	
         	linear.addView(but);
         	}
      /*       buttonItem2 = (Button) findViewById(R.id.itemMenu2);
             buttonItem2.setOnClickListener(new OnClickListener() {
                         
                         @Override
                         public void onClick(View v) {
                                 getContext().startActivity(new Intent(getContext(), PhotoTextListTwoRowsActivity.class));
                         }
                 });
             buttonItem3 = (Button) findViewById(R.id.itemMenu3);
             buttonItem3.setOnClickListener(new OnClickListener() {
                         
                         @Override
                         public void onClick(View v) {
                                 getContext().startActivity(new Intent(getContext(), NetworkActivity.class));
                         }
                 });
             buttonItem4 = (Button) findViewById(R.id.itemMenu4);
             buttonItem4.setOnClickListener(new OnClickListener() {
                         
                         @Override
                         public void onClick(View v) {
                                 getContext().startActivity(new Intent(getContext(), PhotoTextListActivity.class));
                         }
                 });
             buttonItem5 = (Button) findViewById(R.id.itemMenu4);
             buttonItem5.setOnClickListener(new OnClickListener() {
                         
                         @Override
                         public void onClick(View v) {
                                 getContext().startActivity(new Intent(getContext(), NetworkActivity.class));
                         }
                 });
             buttonItem6 = (Button) findViewById(R.id.itemMenu4);
             buttonItem6.setOnClickListener(new OnClickListener() {
                         
                         @Override
                         public void onClick(View v) {
                                 getContext().startActivity(new Intent(getContext(), ContactActivity.class));
                         }
                 });*/
     }
 }
