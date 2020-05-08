 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.action.bean;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.gots.action.AbstractActionGarden;
 import org.gots.action.BaseActionInterface;
 import org.gots.action.GardeningActionInterface;
 import org.gots.action.PermanentActionInterface;
 import org.gots.action.sql.ActionDBHelper;
 import org.gots.action.sql.ActionSeedDBHelper;
 import org.gots.bean.BaseAllotmentInterface;
 import org.gots.seed.GrowingSeedInterface;
 import org.gots.seed.sql.GrowingSeedDBHelper;
 
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 import android.content.Context;
 
 public class SowingAction extends AbstractActionGarden implements PermanentActionInterface, GardeningActionInterface {
 	Context mContext;
 
 	public SowingAction(Context context) {
 		setName("sow");
 		mContext = context;
 	}
 
 	public void setDateActionDone(Date dateActionDone) {
 		super.setDateActionDone(dateActionDone);
 	}
 
 	public Date getDateActionDone() {
 		return super.getDateActionDone();
 	}
 
 	public void setDuration(int duration) {
 		super.setDuration(duration);
 	}
 
 	public int getDuration() {
 		return super.getDuration();
 	}
 
 	public void setDescription(String description) {
 		super.setDescription(description);
 	}
 
 	public String getDescription() {
 		return super.getDescription();
 	}
 
 	public void setName(String name) {
 		super.setName(name);
 	}
 
 	public String getName() {
 		return super.getName();
 	}
 
 	@Override
 	public int execute(BaseAllotmentInterface allotment, GrowingSeedInterface seed) {
 		super.execute(allotment, seed);
 
 		GrowingSeedDBHelper gsdh = new GrowingSeedDBHelper(mContext);
 		seed.setDateSowing(Calendar.getInstance().getTime());
 		seed = gsdh.insertSeed(seed, allotment.getName());
		
 		ActionSeedDBHelper asdh = new ActionSeedDBHelper(mContext);
 		asdh.insertAction(this, seed);
		//asdh.doAction(this, seed);
 
 		for (Iterator<BaseActionInterface> iterator = seed.getActionToDo().iterator(); iterator.hasNext();) {
 			ActionDBHelper actionHelper = new ActionDBHelper(mContext);
 			BaseActionInterface type1 = iterator.next();
 			if (type1 != null) {
 				BaseActionInterface type = actionHelper.getActionByName(type1.getName());
 				asdh.insertAction(type, seed);
 			}
 		}
 		GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
 		tracker.trackEvent("Seed", getName(), seed.getSpecie(), 0);
 		// tracker.dispatch();
 		return 0;
 	}
 
 	@Override
 	public void setId(int id) {
 		super.setId(id);
 	}
 
 	@Override
 	public int getId() {
 
 		return super.getId();
 	}
 
 	@Override
 	public int getState() {
 		return super.getState();
 	}
 
 }
