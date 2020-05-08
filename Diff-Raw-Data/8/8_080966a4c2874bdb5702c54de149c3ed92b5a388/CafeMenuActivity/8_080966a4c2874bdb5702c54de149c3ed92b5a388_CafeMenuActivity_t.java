 package com.remirran.cafemenu;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TableRow;
 
 public class CafeMenuActivity extends Activity {
     /** Called when the activity is first created. */
 	/*Stubs*/
 	String title_buttons[] = {"Европейская кухня", "Кавказская кухня", "Японская кухня", "Бакалея"}; 
 	String section_items[] = {"Холодные закуски", "Горячие закуски", "Салаты рыбные", "Салаты мясные", "Салаты овощные", "Первые блюда", "Вторые блюда", "Гарниры", "Закуски к пиву"};
 	String dishes[][] = {
 			{ "Филе сельди с картошкой", "http://edem.argroup52.ru/assets/images/europa/seld%20s%20kartoph.jpg", "90" },
 			{ "Язык отварной", "http://edem.argroup52.ru/assets/images/europa/2.jpg", "160" },
 			{ "Русский разносол", "http://edem.argroup52.ru/assets/images/europa/raznosol.jpg", "160" },
 			{ "Корейская морковь", "http://edem.argroup52.ru/assets/images/europa/4.jpg", "40" },
 			{ "Квашеная капуста","http://edem.argroup52.ru/assets/images/europa/5.jpg", "40" },
 			{ "Рыбное ассорти", "http://edem.argroup52.ru/assets/images/europa/ribnoe%20assorti.jpg", "160" },
 			{ "Мясная тарека", "http://edem.argroup52.ru/assets/images/europa/myasnaya%20tarelka.jpg", "220" },
 			{ "Сырная тарелка", "http://edem.argroup52.ru/assets/images/europa/8.jpg", "180" },
 			{ "Маслины/оливки", "http://edem.argroup52.ru/assets/images/europa/9.jpg", "75" },
 			{ "Лимонная нарезка с сахаром", "http://edem.argroup52.ru/assets/images/europa/10.jpg", "50" }
 	};
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         /* Start drawing */
         LayoutInflater ltInflatter = getLayoutInflater();
         
         /* Fill titles*/
         LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
         for (int i = 0; i < title_buttons.length; i++ ) {
         	View tView = ltInflatter.inflate(R.layout.main_title_button, tTtlLayout, false);
         	Button tButton = (Button) tView.findViewById(R.id.main_title_button);
         	tButton.setText(title_buttons[i]);
         	tTtlLayout.addView(tView);
         }
         
         /* Fill items for 1st section */
         ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
         ArrayAdapter<String> tLstAdapter = new ArrayAdapter<String>(this,
         		R.layout.main_section_item, section_items);
         tLstLayout.setAdapter(tLstAdapter);
         
         /* Fill table */
         TableRow tTableRows[] = { (TableRow) findViewById(R.id.main_table_row1), 
         		(TableRow) findViewById(R.id.main_table_row2) };
         ImgCache ic = new ImgCache();
         for (int i = 0; i < dishes.length; i++ ) {
         	View tView = ltInflatter.inflate(R.layout.main_table_item, tTableRows[i%2], false);
         	ImageView tImg = (ImageView) tView.findViewById(R.id.main_table_img);
         	ic.fetchImage(this, 3600, dishes[i][1], tImg);
         	tImg.setContentDescription(dishes[i][0]);
         	tTableRows[i%2].addView(tView);
         }
 
     }
 }
