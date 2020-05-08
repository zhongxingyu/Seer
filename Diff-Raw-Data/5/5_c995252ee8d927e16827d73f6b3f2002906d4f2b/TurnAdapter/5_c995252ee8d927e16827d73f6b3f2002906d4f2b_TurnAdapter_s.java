 package be.quentinloos.manille.util;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import java.util.List;
 
 import be.quentinloos.manille.R;
 import be.quentinloos.manille.core.Turn;
 
 /**
  * Adapter for turns in listview
  *
  * @author Quentin Loos <contact@quentinloos.be>
  */
 public class TurnAdapter extends ArrayAdapter<Turn> {
 
     public TurnAdapter(Context context, List<Turn> objects) {
         super(context, android.R.layout.simple_list_item_1, objects);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 
         ViewHolder holder;
         Turn turn = getItem(position);
 
         if (convertView == null) {
             holder = new ViewHolder();
             convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_turn, null);
 
             if (turn.getTeam() == 1) {
                 holder.symbol1  = (TextView) convertView.findViewById(R.id.symbol1);
                 holder.symbol2  = (TextView) convertView.findViewById(R.id.symbol2);
             }
             else if (turn.getTeam() == 2) {
                 holder.symbol1  = (TextView) convertView.findViewById(R.id.symbol2);
                 holder.symbol2  = (TextView) convertView.findViewById(R.id.symbol1);
             }
 
             holder.points1 = (TextView) convertView.findViewById(R.id.scoreteam1);
             holder.points2 = (TextView) convertView.findViewById(R.id.scoreteam2);
 
             if (turn.isDraw()) {
                 holder.points1.setTextColor(getContext().getResources().getColor(R.color.highlight));
                 holder.points2.setTextColor(getContext().getResources().getColor(R.color.highlight));
             }
 
             convertView.setTag(holder);
         }
         else {
             holder = (ViewHolder) convertView.getTag();
         }
 
         // Display the trump suit symbol
         holder.symbol1.setText(turn.getTrump().toString());
         if (turn.getTrump() == Turn.Trump.DIAMONDS || turn.getTrump() == Turn.Trump.HEARTS)
             holder.symbol1.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
         else if(turn.getTrump() == Turn.Trump.CLUBS || turn.getTrump() == Turn.Trump.SPADES || turn.getTrump() == Turn.Trump.NOTRUMP)
             holder.symbol1.setTextColor(getContext().getResources().getColor(android.R.color.black));
         // Display the multiplier
         if (turn.getMult() > 1)
             if (turn.getMultMode())
                holder.symbol2.setText("x" + turn.getMult());
            else
                 holder.symbol2.setText("x" + (int) Math.pow(2, turn.getMult() - 1));
         else
             holder.symbol2.setText("");
 
         holder.points1.setText(Integer.toString(turn.getPoints1()));
         holder.points2.setText(Integer.toString(turn.getPoints2()));
 
         return convertView;
     }
 
     private class ViewHolder {
         TextView symbol1;
         TextView symbol2;
         TextView points1;
         TextView points2;
     }
 }
