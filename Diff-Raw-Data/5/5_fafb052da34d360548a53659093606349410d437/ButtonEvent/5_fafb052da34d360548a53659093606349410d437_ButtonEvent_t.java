 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.device;
 
 /**
  * Represents a physical key event.
  * 
  * @author ken
  * 
  */
 public class ButtonEvent {
 	/**
 	 * @deprecated This button only available on BUG 1.3 hardware.
 	 */
 	public static final int BUTTON_HOTKEY_1 = 258;
 	/**
 	 * @deprecated This button only available on BUG 1.3 hardware.
 	 */
 	public static final int BUTTON_HOTKEY_2 = 259;
 	/**
 	 * @deprecated This button only available on BUG 1.3 hardware.
 	 */
 	public static final int BUTTON_HOTKEY_3 = 260;
 	/**
 	 * @deprecated This button only available on BUG 1.3 hardware.
 	 */
 	public static final int BUTTON_HOTKEY_4 = 261;
 	/**
 	 * @deprecated This button only available on BUG 1.3 hardware.
 	 */
 	public static final int BUTTON_LEFT = 263;
 	/**
 	 * @deprecated This button only available on BUG 1.3 hardware.
 	 */
 	public static final int BUTTON_RIGHT = 262;
 	/**
 	 * @deprecated This button only available on BUG 1.3 hardware.
 	 */
 	public static final int BUTTON_UP = 264;
 	/**
 	 * @deprecated This button only available on BUG 1.3 hardware.
 	 */
 	public static final int BUTTON_DOWN = 265;
 
 	public static final int BUTTON_CAMERA_ZOOM_IN = 258;
 
 	public static final int BUTTON_CAMERA_ZOOM_OUT = 257;
 
 	public static final int BUTTON_CAMERA_SHUTTER = 256;
 
 	public static final int BUTTON_SELECT = 257;
 
 	public static final int BUTTON_AUDIO = 40;
 
 	// audio module doesn't really fit the normal conventions. the getButton()
 	// returns the same thing for both sides of the rocker, for both
 	// pressed/released events.
 	// The only way to distinguish is by getting the action.
 	// Currently there is no way to determine which side of the rocker was
 	// released
 	// these static final vars should remain until the driver is refactored to
 	// more clearly
 	// express the button events
 	public static final int BUTTON_AUDIO_VOLUP_PRESSED_ACTION = 32;
 
 	// see above
 	public static final int BUTTON_AUDIO_VOLDOWN_PRESSED_ACTION = 16;
 
 	/**
 	 * User button on BUG 2.0.
 	 */
 	public static final int BUTTON_BUG20_USER = 242;
	
	/**
	 * Power button on BUG 2.0.
	 */
	public static final int BUTTON_BUG20_POWER = 116;
 
 	public static final int KEY_DOWN = 1;
 
 	public static final int KEY_UP = 0;
 
 	private final int rawValue;
 
 	private final float duration;
 
 	private final int button;
 
 	private final long action;
 
 	private final String source;
 
 	public ButtonEvent(int key) {
 		this.rawValue = key;
 		this.duration = 0;
 		this.button = 0;
 		this.action = 0;
 		this.source = null;
 	}
 
 	public ButtonEvent(int rawValue, float duration) {
 		this.rawValue = rawValue;
 		this.duration = duration;
 		this.button = 0;
 		this.action = 0;
 		this.source = null;
 	}
 
 	public ButtonEvent(int rawValue, float duration, int button) {
 		this.rawValue = rawValue;
 		this.duration = duration;
 		this.button = button;
 		this.action = 0;
 		this.source = null;
 	}
 
 	public ButtonEvent(int rawValue, float duration, int button, long action) {
 		this.rawValue = rawValue;
 		this.duration = duration;
 		this.button = button;
 		this.action = action;
 		this.source = null;
 	}
 
 	public ButtonEvent(int rawValue, float duration, int button, long action, String source) {
 		this.rawValue = rawValue;
 		this.duration = duration;
 		this.button = button;
 		this.action = action;
 		this.source = source;
 	}
 
 	/**
 	 * @return Code for button pressed.
 	 */
 	public int getButton() {
 		return button;
 	}
 
 	/**
 	 * @return This value is not used. Each button event is discreet.
 	 * @deprecated
 	 */
 	public float getDuration() {
 		return duration;
 	}
 
 	/**
 	 * @return The raw code from the event provider.
 	 */
 	public int getRawValue() {
 		return rawValue;
 	}
 
 	/**
 	 * @return
 	 */
 	public long getAction() {
 		return action;
 	}
 
 	/**
 	 * @return The source of the event.
 	 */
 	public String getSource() {
 		return source;
 	}
 }
