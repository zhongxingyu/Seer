 /*******************************************************************************
  * Copyright (c) 2012 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package uk.co.jwlawson.plan.activities;
 
 import android.graphics.PointF;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import uk.co.jwlawson.plan.R;
 import uk.co.jwlawson.plan.entities.Line;
 import uk.co.jwlawson.plan.views.PegView;
 
 import java.util.ArrayList;
 
 /** Activity to show and manage the peg board room creation. */
 public class PegViewActivity extends SherlockActivity {
 
 	private static final String TAG = "PegViewActivity";
 	private static final boolean DEBUG = true;
 
 	private PegView mPegView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.peg_view_layout);
 
 		mPegView = (PegView) findViewById(R.id.pegView1);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inf = getSupportMenuInflater();
 		inf.inflate(R.menu.peg_view_act_menu, menu);
 		if (DEBUG) Log.d(TAG, "Menu inflated");
 		return true;
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.peg_view_menu_done:
 			done();
 			if (DEBUG) Log.d(TAG, "Menu item DONE selected");
 			break;
 		case R.id.peg_view_menu_reset:
 			reset();
 			if (DEBUG) Log.d(TAG, "Menu item RESET selected");
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	private void done() {
 		ArrayList<Line> lineList = new ArrayList<Line>(mPegView.getLineList());
 
 		if (lineList.isEmpty()) {
 			Toast.makeText(getApplicationContext(), "No walls added", Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		ArrayList<PointF> pointList = new ArrayList<PointF>();
 
 		Line l1 = lineList.get(0);
 		lineList.remove(l1);
 
 		PointF initPoint = l1.getStart();
 		pointList.add(initPoint);
		if (DEBUG) Log.d(TAG, l1.toString());
 		PointF start = l1.getEnd();
 		pointList.add(start);
 
 		boolean flag = false;
 
 		while (!initPoint.equals(start.x, start.y)) {
 
 			flag = false;
 
 			for (Line line : lineList) {
 
 				if (line.getStart().equals(start.x, start.y)) {
					if (DEBUG) Log.d(TAG, line.toString());
 					pointList.add(line.getEnd());
 					start = line.getEnd();
 					lineList.remove(line);
 					flag = true;
 					break;
 
 				} else if (line.getEnd().equals(start.x, start.y)) {
					if (DEBUG) Log.d(TAG, line.toString());
 					pointList.add(line.getStart());
 					start = line.getStart();
 					lineList.remove(line);
 					flag = true;
 					break;
 
 				}
 			}
 			// No match found. Error!
 			if (!flag) {
 				Toast.makeText(getApplicationContext(), "Invalid room", Toast.LENGTH_SHORT).show();
 				return;
 			} else {
 				lineList.remove(start);
 			}
 		}
 
 	}
 
 	private void reset() {
 		mPegView.reset();
 	}
 }
