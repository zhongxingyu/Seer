 package layout;
 
 import structures.Data;
 import android.content.Context;
 import android.graphics.Color;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class CustomListView extends ArrayAdapter<String>{
 	String[] marks;
 
 	public CustomListView(Context context, int layout, int textViewResourceId, String[] values) {
 		super(context, layout, textViewResourceId, values);
 		marks = values;
 	}
 	
 	@Override
 	public View getView(int position, View v, ViewGroup parent) {
 		View myView = super.getView(position, v, parent);
 		String txt = marks[position];
 		String temp = "";
 		int beginIndex = 0;
 		int endIndex = 0;
 		char value = '`';
 		for(int i = txt.length() - 10; i < txt.length(); i++) {
 			value = txt.charAt(i);
 			if(value == '(') {
 				beginIndex = i;
 			}
 			if(value == ')' && beginIndex != 0) {
 				endIndex = i;
 				temp = txt.substring(beginIndex+1, endIndex);
 				break;
 			}		
 		}
 		
 /********************
  *  	to fix!!!!! *
  ********************/
 		
 		if(temp.equals("-") == false) {	
 			if(temp.equals("A+") == true || temp.equals("A") == true || temp.equals("A-") == true)
 				((TextView) myView).setTextColor(Color.BLUE);
 			else if(temp.equals("B+") == true || temp.equals("B") == true)
 				((TextView) myView).setTextColor(Color.GREEN);
 			else if(temp.equals("B-") == true || temp.equals("C+") == true)
 				((TextView) myView).setTextColor(Color.YELLOW);
 			else if(temp.equals("C") == true || temp.equals("C-") == true)
 				((TextView) myView).setTextColor(0xFFF06D2F);
 			else 
 				((TextView) myView).setTextColor(Color.RED);
 		}
 		return myView;
 	}
 	
 	public static String getLetterGradeWithPercentageGrade(
 			double percentageGrade) {
 		String letterGrade = "";
 
 		for (int i = 0; i < Data.gpaValue.size(); i++) {
 			double bottomLimit = Data.gpaValue.get(i).getPercentLow();
 			double upperLimit = Data.gpaValue.get(i).getPercentHigh();
 
 			if (percentageGrade == bottomLimit
 					|| percentageGrade == upperLimit
 					|| (percentageGrade > bottomLimit && percentageGrade < upperLimit)) {
 				letterGrade = Data.gpaValue.get(i).getLetterGrade();
 				break;
 			}
 		}
 		return letterGrade;
 	}
 }
