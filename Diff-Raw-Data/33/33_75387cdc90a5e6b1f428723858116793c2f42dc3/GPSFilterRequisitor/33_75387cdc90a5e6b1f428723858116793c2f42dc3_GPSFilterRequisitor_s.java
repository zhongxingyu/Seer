 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.gps;
 
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterArea;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterAreaSet;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSLatLng;
 import se.chalmers.dat255.sleepfighter.preference.GlobalPreferencesManager;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 
 /**
  * GPSFilterRequisitor is responsible for checking that the user:s<br/>
  * current location is within allowed areas or not.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 8, 2013
  */
 public class GPSFilterRequisitor {
 	private GPSFilterAreaSet set;
 	private GlobalPreferencesManager prefs;
 
 	/**
 	 * Constructs the GPSFilterRequisitor given a set of areas.
 	 */
 	public GPSFilterRequisitor( GPSFilterAreaSet set, GlobalPreferencesManager prefManager ) {
 		this.set = set;
 		this.prefs = prefManager;
 	}
 
 	/**
 	 * Is the requisitor satisfied? In other words, are the requirements met?
 	 *
 	 * @return true if requirements are met.
 	 */
 	public boolean isSatisfied( Context context ) {
 		// Not globally enabled or no enabled and valid areas? Then we're satisfied!
		if ( this.prefs.isLocationFilterEnabled() || !this.set.hasEnabledAndValid() ) {
 			return true;
 		}
 
 		// Get last known location.
 		GPSFilterLocationRetriever locRet = new GPSFilterLocationRetriever( new Criteria() );
 		Location loc = locRet.getLocation( context );
 
 		int maxAge = this.prefs.getLocationMaxAge();
 		if ( maxAge > 0 ) {
 			long locAge = System.currentTimeMillis() - loc.getTime();
 			if ( locAge > maxAge * 60000 ) {
 				// Last known location is too old, skip location filter.
 				return true;
 			}
 		}
 
 		GPSLatLng pos = new GPSLatLng( loc.getLatitude(), loc.getLongitude() );
 
 		// Partition!
 		GPSFilterAreaSet[] partition = this.modePartitionSet();
 
 		// Exclude areas first.
 		for ( GPSFilterArea area : partition[0] ) {
 			if ( area.contains( pos ) ) {
 				return false;
 			}
 		}
 
 		// Include areas now.
 		if ( partition[1].isEmpty() ) {
 			return true;
 		} else {
 			// At least one match needed.
 			for ( GPSFilterArea area : partition[1] ) {
 				if ( area.contains( pos ) ) {
 					return true;
 				}
 			}
 
 			return false;
 		}
 	}
 
 	/**
 	 * Partitions the set into one for includes, one for excludes.
 	 *
 	 * @return [excludes, includes]
 	 */
 	private GPSFilterAreaSet[] modePartitionSet() {
 		GPSFilterAreaSet includes = new GPSFilterAreaSet();
 		GPSFilterAreaSet excludes = new GPSFilterAreaSet();
 
 		for ( GPSFilterArea area : this.set ) {
 			// Take the opportunity to get rid of non-enabled & invalid areas.
 			if ( !(area.isValid() && area.isEnabled()) ) {
 				continue;
 			}
 
 			// Partition current area.
 			switch ( area.getMode() ) {
			case EXCLUDE:
 				includes.add( area );
 				break;
 
			case INCLUDE:
 				excludes.add( area );
 				break;
 			}
 		}
 
 		// And we're done.
 		return new GPSFilterAreaSet[] { excludes, includes };
 	}
 }
