 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.test.utils;
 
 import android.content.Intent;
 import android.test.AndroidTestCase;
 import edu.chalmers.dat255.group09.Alarmed.utils.AudioHelper;
 
 /**
  * Test class for the AudioHelper class.
  * 
  * @author Adrian Bjugard
  * 
  */
 public class AudioHelperTest extends AndroidTestCase {
 	private AudioHelper hAudio;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		Intent intent = new Intent();
 		hAudio = new AudioHelper(getContext(), intent);
 	}
<<<<<<< HEAD

	public void testGetAlarmToneDialog() {
		assertNotNull(hAudio.getAlarmToneDialog());
	}

=======
	
>>>>>>> toneSelectorRefractor
 	public void testGetVolumeDialog() {
 		assertNotNull(hAudio.getVolumeDialog());
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		hAudio = null;
 	}
 }
