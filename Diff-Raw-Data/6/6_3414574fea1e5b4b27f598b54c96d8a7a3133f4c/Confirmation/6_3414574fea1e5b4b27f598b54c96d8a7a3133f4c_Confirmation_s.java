 package edu.colorado.RobotArmies;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class Confirmation extends Activity implements OnClickListener {
 
 	private RobotArmiesDB database = new RobotArmiesDB(this);
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.confirmation);
 
 		int type = this.getIntent().getIntExtra("type", -1);
 		SQLiteDatabase db = database.getWritableDatabase();
 		Cursor c = db.query("users", new String[] { "joules" }, "username=?", new String[] {Home.name}, null, null, null);
 		Cursor d;
 		int joules = -1;
 		int many = 0;
 		int cost = -1;
 		String types = " ";
 		ImageView robot = (ImageView) findViewById(R.id.robot);
 
 		if (c.moveToFirst()) {
 			joules = c.getInt(0);
 		}
 
 		switch(type) {
 		case Marketplace.MINION:
 			robot.setImageResource(R.drawable.minionbotlarge);
 			d = db.query("robots", new String[] { "numberOf" }, "type=?", new String[] { "minion" }, null, null, null);
 			types = "minion";
 			cost = 100;
 			if (d.moveToPosition(0)) {
 				many = d.getInt(0);
 			}
 			d.close();
 			break;
 		case Marketplace.REPAIR:
 			robot.setImageResource(R.drawable.repairbotlarge);
 			d = db.query("robots", new String[] { "numberOf" }, "type=?", new String[] { "repair" }, null, null, null);
 			types = "repair";
 			cost = 200;
 			if (d.moveToPosition(0)) {
 				many = d.getInt(0);
 			}
 			d.close();
 			break;
 		case Marketplace.ROCKET:
 			robot.setImageResource(R.drawable.rocketbotlarge);
 			d = db.query("robots", new String[] { "numberOf" }, "type=?", new String[] { "rocket" }, null, null, null);
 			types = "rocket";
 			cost = 400;
 			if (d.moveToPosition(0)) {
 				many = d.getInt(0);
 			}
 			d.close();
 			break;
 		case Marketplace.MASTER:
 			robot.setImageResource(R.drawable.masterbotlarge);
 			d = db.query("robots", new String[] { "numberOf" }, "type=?", new String[] { "master" }, null, null, null);
 			types = "master";
 			cost = 800;
 			if (d.moveToPosition(0)) {
 				many = d.getInt(0);
 			}
 			d.close();
 			break;
 		}
 
 		joules = joules - cost;
 		many++;
 		ContentValues values = new ContentValues();
 		values.put("joules", joules);
 		db.update("users", values, "username=?", new String[] {Home.name});
 		ContentValues values2 = new ContentValues();
 		values2.put("numberOf", many);
 		db.update("robots", values2, "type=?", new String[] {types});
 
 
 		TextView text1 = (TextView) findViewById(R.id.textView1);
 		
 		View backButton = this.findViewById(R.id.backbutton);
 		View homeButton = this.findViewById(R.id.homebutton);
 		backButton.setOnClickListener(this);
 		homeButton.setOnClickListener(this);
 		
 
 		text1.setText("Thank you for purchasing a " + types + " bot!" +
 				" You now have " + joules + " joules. You now have " +
 				many + " " + types + " bots.");
 
 		c.close();
 	}
 
 	//@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.backbutton:
			//Intent i = new Intent(this, TabWidget.class);
			//startActivity(i);
			finish();
 			break;
 		case R.id.homebutton:
 			Intent j = new Intent(this, TabWidget.class);
 			startActivity(j);
 			break;
 		}
 
 	}
 
 }
