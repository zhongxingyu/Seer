 /**
  * This file is part of TaxDroid.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright 2013 Kai Körber
  */
 package de.rebreok.taxdroid;
 
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.view.View;
 import android.view.ViewGroup;
 
 import java.util.ArrayList;
 
 
 public class ButtonAdapter extends BaseAdapter {
     private GameActivity parent;
     private ArrayList<Button> buttons;
     
     public ButtonAdapter(GameActivity ga) {
         parent = ga;
         buttons = new ArrayList<Button>();
         for (int i = 0; i < getCount(); i++) {
             Button button = new Button(parent);
             buttons.add(button);
             button.setText(String.valueOf(i + 1));
             button.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View view) {
                         Button b = (Button) view;
                         parent.selectButton(Integer.parseInt(b.getText().toString()));
                     }
                 });
         }
     }
     
     public int getCount() {
         return parent.getLevel();
     }
     
     public Object getItem(int position) {
         return buttons.get(position);
     }
     
     public long getItemId(int position) {
         return 0;
     }
     
     public View getView(int position, View convertView, ViewGroup parent) {
         return buttons.get(position);
     }
 }
