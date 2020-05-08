 package com.supinfo.geekquote.adapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.supinfo.geekquote.R;
 import com.supinfo.geekquote.model.Quote;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 public class QuoteAdapter extends ArrayAdapter<Quote> {
 	
 	private List<Quote> quotes;
 
 	public QuoteAdapter(Context context, ArrayList<Quote> quotes) {
 		super(context, android.R.layout.simple_list_item_1, quotes);
 		this.quotes = quotes;
 	}
 	
 	@Override
 	public Quote getItem(int position) {
 		return quotes.get(position);
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
 		View row = layoutInflater.inflate(R.layout.list_row, null, false);
 		
 		Quote quote = getItem(position);
 		
 		TextView textView = (TextView) row.findViewById(R.id.row_text);
 		textView.setText(quote.getStrQuote());
 		
 		RatingBar ratingBar = (RatingBar) row.findViewById(R.id.rating_bar);
 		ratingBar.setRating(quote.getRating());
 		
 		return row;
 	}
 
 }
 
