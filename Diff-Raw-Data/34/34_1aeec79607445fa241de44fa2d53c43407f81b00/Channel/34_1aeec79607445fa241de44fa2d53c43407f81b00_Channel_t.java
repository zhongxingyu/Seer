 /* Channel.java - Handles a channel
 * Copyright (C) 2000-2003 Fredrik Ehnbom
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.gjt.fredde.silence.format.xm;
 
 /**
  * A class that handles a channel
  *
 * @version $Id: Channel.java,v 1.15 2003/09/01 09:07:30 fredde Exp $
  * @author Fredrik Ehnbom
  */
 class Channel {
 	Xm			xm;
 	InstrumentManager	im;
 	EffectManager		em;
 
 	public Channel(Xm xm) {
 		this.xm = xm;
 		im = new InstrumentManager(xm);
 		em = new EffectManager(this);
 	}
 
 	int currentNote		= 0;
 
 	final int skip(Pattern pattern, int patternpos) {
 		int check = pattern.data[patternpos++];
 
 		if ((check & 0x80) != 0) {
 			if ((check & 0x1) != 0) patternpos++;
 			if ((check & 0x2) != 0) patternpos++;
 			if ((check & 0x4) != 0) patternpos++;
 			if ((check & 0x8) != 0) patternpos++;
 			if ((check & 0x10) != 0) patternpos++;
 		} else {
 			patternpos += 4;
 		}
 
 		return patternpos;
 	}
 
 	final int update(Pattern pattern, int patternpos) {
 		int check = pattern.data[patternpos++];
 
 		int newNote = -1;
 		Instrument newInstrument = null;
 		int newVolume = -1;
 		int newEffect = -1;
 		int newEffectParam = -1;
 
 		if ((check & 0x80) != 0) {
 			// note
 			if ((check & 0x1) != 0)	newNote = pattern.data[patternpos++];
 
 			// instrument
 			if ((check & 0x2) != 0) {
 				int tmp = pattern.data[patternpos++] - 1;
 				if (tmp < xm.instrument.length)
 					newInstrument = xm.instrument[tmp];
 			}
 
 			// volume
 			if ((check & 0x4) != 0) newVolume = pattern.data[patternpos++]&0xff;
 
 			// effect
 			if ((check & 0x8) != 0)	newEffect = pattern.data[patternpos++];
 
 			// effect param
 			if ((check & 0x10) != 0) newEffectParam = pattern.data[patternpos++]&0xff;
 		} else {
 			newNote			= check;
 			newInstrument		= xm.instrument[pattern.data[patternpos++] - 1];
 			newVolume		= pattern.data[patternpos++]&0xff;
 			newEffect		= pattern.data[patternpos++];
 			newEffectParam		= pattern.data[patternpos++]&0xff;
 		}
 
 		if (newInstrument != null) {
 			im.setInstrument(newInstrument);
 		}
 
 		em.currentEffect = -1;
 
		newNote = em.setEffect(newEffect, newEffectParam, newNote);
 
 		if (newNote != -1) {
 			if (newNote == 97) {
 				im.release();
 			} else {
 				currentNote = newNote;
 				im.playNote(currentNote);
 			}
 		}
 
		em.setVolume(newVolume);
 
//		if (newNote != -1 && newNote != 97 && newVolume < 0x10) {
//			im.currentVolume = 64;
//			System.out.println
//		}
 
 		return patternpos;
 	}
 
 	public final void updateTick() {
 		em.updateEffects();
		im.update();
 	}
 
 	final void play(int[] buffer, int off, int len) {
 		im.play(buffer, off, len);
 	}
 }
 /*
  * ChangeLog:
  * $Log: Channel.java,v $
 * Revision 1.15  2003/09/01 09:07:30  fredde
 * fixes
 *
  * Revision 1.14  2003/08/23 07:41:19  fredde
  * gets new note from effectmanager
  *
  * Revision 1.13  2003/08/22 12:36:16  fredde
  * moved effects from Channel to EffectManager
  *
  * Revision 1.12  2003/08/22 06:51:26  fredde
  * 0xx,1xx,2xx,rxx implemented. update() from muhmu2-player
  *
  * Revision 1.11  2003/08/21 09:25:35  fredde
  * moved instrument-playing from Channel into InstrumentManager
  *
  * Revision 1.10  2002/03/20 13:37:25  fredde
  * whoa! lots of changes!
  * among others:
  * * fixed looping (so that some chiptunes does not play false anymore :))
  * * pitch, currentPos and some more stuff now uses fixedpoint maths
  * * added a volumeScale variable for easier changing of the volumescale
  * * a couple of effects that I had implemented in my xm-player for muhmuaudio 0.2
  *   have been copied and pasted into the file. they are commented out though
  *
  * Revision 1.9  2001/01/04 18:55:59  fredde
  * some smaller changes
  *
  * Revision 1.8  2000/12/21 17:19:59  fredde
  * volumeenvelopes works better, uses precalced k-values,
  * pingpong loop fixed
  *
  * Revision 1.7  2000/10/14 19:09:04  fredde
  * changed volume stuff back to 32 since
  * sampleData is of type byte[] again
  *
  * Revision 1.6  2000/10/12 15:04:42  fredde
  * fixed volume envelopes after sustain.
  * updated volumes to work with (8-bit sample) << 8
  *
  * Revision 1.5  2000/10/08 18:01:57  fredde
  * changes to play the file even better.
  *
  * Revision 1.4  2000/10/07 13:48:06  fredde
  * Lots of fixes to play correct.
  * Added volume stuff.
  *
  * Revision 1.3  2000/10/01 17:06:38  fredde
  * basic playing abilities added
  *
  * Revision 1.2  2000/09/29 19:39:48  fredde
  * no need to be public
  *
  * Revision 1.1.1.1  2000/09/25 16:34:34  fredde
  * initial commit
  *
  */
 
 
 
