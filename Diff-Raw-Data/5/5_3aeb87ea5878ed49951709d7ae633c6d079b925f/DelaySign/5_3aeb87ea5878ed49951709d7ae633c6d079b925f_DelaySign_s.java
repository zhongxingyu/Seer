 package net.eonz.bukkit.psduo.signs.normal;
 
 /*
  * This code is Copyright (C) 2011 Chris Bode, Some Rights Reserved.
  *
  * Copyright (C) 1999-2002 Technical Pursuit Inc., All Rights Reserved. Patent 
  * Pending, Technical Pursuit Inc.
  *
  * Unless explicitly acquired and licensed from Licensor under the Technical 
  * Pursuit License ("TPL") Version 1.0 or greater, the contents of this file are 
  * subject to the Reciprocal Public License ("RPL") Version 1.1, or subsequent 
  * versions as allowed by the RPL, and You may not copy or use this file in 
  * either source code or executable form, except in compliance with the terms and 
  * conditions of the RPL.
  *
  * You may obtain a copy of both the TPL and the RPL (the "Licenses") from 
  * Technical Pursuit Inc. at http://www.technicalpursuit.com.
  *
  * All software distributed under the Licenses is provided strictly on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TECHNICAL
  * PURSUIT INC. HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
  * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the Licenses for specific 
  * language governing rights and limitations under the Licenses. 
  */
 
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 import net.eonz.bukkit.psduo.signs.PSSign;
 import net.eonz.bukkit.psduo.signs.TriggerType;
 
 public class DelaySign extends PSSign {
 
 	private boolean lastState;
 	private boolean out = false;
 	private boolean ticking = false;
 
 	protected void triggersign(TriggerType type, Object args) {
 		InputState is = this.getInput(1, (BlockRedstoneEvent) args);
 
 		if (is == InputState.HIGH && !lastState) {
 			lastState = true;
 			if (!ticking)
 				this.startTicking();
 			ticking = true;
 		} else if ((is == InputState.LOW || is == InputState.DISCONNECTED) && lastState) {
 			lastState = false;
 			if (!ticking)
 				this.startTicking();
 			ticking = true;
 		} else {
 			return;
 		}
 
 	}
 
 	private boolean[] states;
 
 	public boolean tick() {
 		short sum = 0;
 		for (int i = 0; i < (states.length - 1); i++) {
 			states[i] = states[i + 1];
 			sum += states[i] ? 1 : -1;
 		}
 
 		states[states.length - 1] = this.lastState;
 		sum += lastState ? 1 : -1;
 
 		if (out != states[0]) {
 			this.setOutput(states[0]);
 			out = states[0];
 		}
 
 		if (Math.abs(sum) == period) {
 			ticking = false;
 			return false;
 		}
 		return true;
 	}
 
 	public String getData() {
 		// This sign does not use data.
 		return "";
 	}
 
 	protected void setData(String data) {
 		// This sign does not use data.
 	}
 
 	private int period = 20;
 
 	protected void declare(boolean reload, SignChangeEvent event) {
 		String periodLine = this.getLines(event)[1].trim();
 
 		if (periodLine.length() > 0) {
 			try {
 				period = Integer.parseInt(periodLine);
 			} catch (Exception e) {
 				if (!reload)
 					this.main.alert(this.getOwnerName(), "Could not understand period, defaulting to 20. (1sec)");
 				period = 20;
 			}
 			if (period > 500 || period <= 0) {
 				period = 20;
 				if (!reload)
 					this.main.alert(this.getOwnerName(), "The period was either too long or too short. Allowed: 1-500");
 			}
 		} else {
 			period = 20;
 		}
 
 		if (!reload) {
 			this.clearArgLines(event);
 			this.setLine(1, Integer.toString(period), event);
 		}
 
 		main.sgc.register(this, TriggerType.REDSTONE_CHANGE);
 		if (!reload) {
 			this.init("Delay sign accepted.");
 		}
 
 		states = new boolean[period];
 		for (int i = 0; i < states.length; i++) {
			states[i] = false;
 		}
 	}
 
 }
