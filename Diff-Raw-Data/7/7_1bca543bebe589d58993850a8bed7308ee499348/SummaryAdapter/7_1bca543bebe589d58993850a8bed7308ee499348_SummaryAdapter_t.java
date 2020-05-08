 package com.september.tableroids;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.text.SpannableString;
 import android.text.style.StrikethroughSpan;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.september.tableroids.statics.GameBuilder;
 import com.september.tableroids.statics.Scorer.Response;
 
 public class SummaryAdapter extends ArrayAdapter<Response>{
 	 private final Activity context;
 	  private final List<Response> responses;
 	  private final  Typeface tf;
 
 	  static class ViewHolder {
 	    public TextView responseNo;
	    public TextView question;
 	    public TextView yourResponse;
 	    public TextView correctResponse;
 	  }
 
 	  public SummaryAdapter(Activity context, List<Response> responses, Typeface tf) {
 	    super(context, R.layout.summaryrow, responses);
 	    this.context = context;
 	    this.responses = responses;
 	    this.tf = tf;
 	  }
 
 	  @Override
 	  public View getView(int position, View convertView, ViewGroup parent) {
 	    View rowView = convertView;
 	    if (rowView == null) {
 	      LayoutInflater inflater = context.getLayoutInflater();
 	      rowView = inflater.inflate(R.layout.summaryrow, null);
 	      ViewHolder viewHolder = new ViewHolder();
 	      viewHolder.responseNo = (TextView) rowView.findViewById(R.id.responseNo);
	      viewHolder.question = (TextView) rowView.findViewById(R.id.question);
 	      viewHolder.yourResponse = (TextView) rowView.findViewById(R.id.yourResponse);
 	      viewHolder.correctResponse = (TextView) rowView.findViewById(R.id.correctResponse);
 	      rowView.setTag(viewHolder);
 	    }
 
 	    ViewHolder holder = (ViewHolder) rowView.getTag();
 	    
 	    Response response = responses.get(position);
 	    holder.responseNo.setTypeface(tf);
 	    holder.responseNo.setText(response.getResponseNumber() + ")");
 	    
	    holder.question.setTypeface(tf);
	    holder.question.setText(response.getMoltiplicando()+" x "+response.getMoltiplicatore());
	    
 	    holder.yourResponse.setTypeface(tf);
 	    
 	    if(response.getResponse() == response.getMoltiplicando()*response.getMoltiplicatore()) {
 	    	holder.yourResponse.setTextColor(Color.BLUE);
 	    	holder.yourResponse.setText(""+response.getResponse());
 	    }
 	    else {
 	    	holder.yourResponse.setTextColor(Color.RED);
 	    	SpannableString content = new SpannableString(response.getResponse()+"");
 	    	content.setSpan(new StrikethroughSpan(), 0, content.length(), 0);
 	    	holder.yourResponse.setText(content);
 	    	
 	    	holder.correctResponse.setTypeface(tf);
 	    	holder.correctResponse.setTextColor(Color.GREEN);
 	    	holder.correctResponse.setText(""+response.getMoltiplicando()*response.getMoltiplicatore());
 	    }
 	    
 	    return rowView;
 	  }
 }
