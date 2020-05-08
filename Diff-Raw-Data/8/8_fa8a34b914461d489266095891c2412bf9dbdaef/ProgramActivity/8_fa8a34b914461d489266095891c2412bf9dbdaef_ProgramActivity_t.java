 package net.tailriver.agoraguide;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.SpannableStringBuilder;
 import android.text.Spanned;
 import android.text.style.BackgroundColorSpan;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ProgramActivity extends AgoraActivity implements OnClickListener {
 	private EntrySummary summary;
 
 	@Override
 	public void onPreInitialize() {
 		setContentView(R.layout.program);
 		setTitle(R.string.program);
 
 		int notificationId = getIntent().getIntExtra(IntentExtra.NOTIFICATION_ID, -1);
 		if (notificationId > -1) {
 			ScheduleAlarm.cancelNotification(getApplicationContext(), notificationId);
 		}
 	}
 
 	@Override
 	public void onPostInitialize(Bundle savedInstanceState) {
 		summary = EntrySummary.get(getIntent().getStringExtra(IntentExtra.ENTRY_ID));
 		EntryDetail detail = new EntryDetail(summary);
 
 		TextView iconView = (TextView) findViewById(R.id.programHeader);
 		iconView.setText(new SpannableStringBuilder(summary.getId()));
 		iconView.append(" ");
 		iconView.append(summary.getCategory().toString());
 
 		if (AgoraActivity.isHoneycomb()) {
 			findViewById(R.id.programIconLayout).setVisibility(View.GONE);
 		} else {
 			View favoriteView = findViewById(R.id.favoriteButton);
 			favoriteView.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Favorite.setFavorite(summary, !Favorite.isFavorite(summary));
 					updateFavoriteView(v);
 				}
 			});
 			updateFavoriteView(favoriteView);
 			findViewById(R.id.mapButton).setOnClickListener(this);
 		}
 
 		TextView titleView = (TextView) findViewById(R.id.programTitle);
 		titleView.setText(summary.toString());
 
 		TextView sponsorView = (TextView) findViewById(R.id.programSponsor);
 		sponsorView.setText(summary.getSponsor());
 		String coSponsor = detail.getValue("cosponsor");
 		if (coSponsor != null) {
 			sponsorView.append("\n");
 			sponsorView.append(coSponsor);
 		}
 
 		TextView scheduleView = (TextView) findViewById(R.id.programSchedule);
 		scheduleView.setText(summary.getSchedule());
 
 		CharSequence delimiter = "\n\n";
 		SpannableStringBuilder text = new SpannableStringBuilder();
 		for (String tag : new String[]{ "abstract", "content", "guest", "website", "reservation", "note" }) {
 			String tagValue = detail.getValue(tag);
 			if (tagValue != null && tagValue.length() != 0) {
 				SpannableStringBuilder section =
 						new SpannableStringBuilder(detail.getName(tag)).append("\n");
 				section.setSpan(new BackgroundColorSpan(Color.LTGRAY),
 						0, section.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 				text.append(section).append(tagValue).append(delimiter);
 			}
 		}
 
 		if (text.length() > delimiter.length()) {
 			text.delete(text.length() - delimiter.length(), text.length());
 		}
 
 		TextView detailView = (TextView) findViewById(R.id.programContent);
 		detailView.setText(text);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.program, menu);
 		if (Favorite.isFavorite(summary)) {
 			menu.findItem(R.id.programFavorite).setIcon(android.R.drawable.btn_star_big_on);
 			menu.findItem(R.id.programFavorite).setTitle(R.string.favoriteRemove);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.programFavorite:
 			Favorite.setFavorite(summary, !Favorite.isFavorite(summary));
 			updateFavoriteView(findViewById(R.id.favoriteButton));
 			supportInvalidateOptionsMenu();
 			return true;
 		case R.id.programLocation:
 			Intent intent = new Intent(getApplicationContext(), MapActivity.class);
 			intent.putExtra(IntentExtra.ENTRY_ID, summary.getId());
 			startActivity(intent);
 			return true;
 		default:
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public void onClick(View v) {
 		if (v.getId() == R.id.mapButton) {
 			Intent intent = new Intent(getApplicationContext(), MapActivity.class);
 			intent.putExtra(IntentExtra.ENTRY_ID, summary.getId());
 			startActivity(intent);
 		}
 	}
 
 	private final void updateFavoriteView(View v) {
 		ImageView favoriteView = (ImageView) v;
 		if (Favorite.isFavorite(summary)) {
 			favoriteView.setImageResource(android.R.drawable.btn_star_big_on);
 			favoriteView.setContentDescription(getString(R.string.favoriteRemove));
 		} else {
 			favoriteView.setImageResource(android.R.drawable.btn_star_big_off);
 			favoriteView.setContentDescription(getString(R.string.favoriteAdd));
 		}
 	}
 }
