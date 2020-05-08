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
 package se.chalmers.dat255.sleepfighter.challenge;
 
 import se.chalmers.dat255.sleepfighter.activity.ChallengeActivity;
 import android.os.Bundle;
 
 /**
  * Interface implemented by challenges.
  * 
  * It works by giving access to methods in an empty {@code ChallengeActivity}
  * that should be used in order modify its contents. The implementing class
  * should at some point call {@code complete()} in the given
  * {@code ChallengeActivity}, when the user completes the challenge.
  */
 public interface Challenge {
 	/**
 	 * <p>Called from the outside when the challenge is to be started from scratch.</p>
 	 * 
 	 * <p>Here should at least {@code setContentView()},<br/>
 	 * or similar method in ChallengeActivity, be called.</p>
 	 * 
 	 * @param activity the ChallengeActivity that the Challenge modifies.
 	 * @param params the resolved params (may have no params defined).
 	 */
 	public void start(ChallengeActivity activity, ChallengeResolvedParams params );
 
 	/**
 	 * <p>Called from the outside when the challenge is to be started again.</p>
 	 * 
 	 * <p>Here should at least {@code setContentView()},<br/>
 	 * or similar method in ChallengeActivity, be called.</p>
 	 * 
 	 * @param activity the ChallengeActivity that the Challenge modifies
 	 * @param params the resolved params (may have no params defined).
 	 * @param state the state that was saved by Challenge before.
	 * @param challengeResolvedParams 
 	 */
 	public void start(ChallengeActivity activity, ChallengeResolvedParams params, Bundle state);
 
 	/**
 	 * Called when challenge must save its state.
 	 *
	 * @return a Parcelable to save.
 	 */
 	public Bundle savedState();
 
 	/**
 	 * Called when the leaves the activity.
 	 */
 	public void onPause();
 
 	/**
 	 * Called when a users enters the activity, either the first time or when
 	 * returning.
 	 */
 	public void onResume();
 
 	/**
 	 * Called when the activity is destroyed.
 	 */
 	public void onDestroy();
 }
