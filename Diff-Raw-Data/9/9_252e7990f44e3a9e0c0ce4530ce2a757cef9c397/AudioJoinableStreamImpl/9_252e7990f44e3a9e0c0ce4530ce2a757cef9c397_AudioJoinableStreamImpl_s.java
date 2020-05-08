 /*
  * Kurento Android MSControl: MSControl implementation for Android.
  * Copyright (C) 2011  Tikal Technologies
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.kurento.kas.mscontrol.join;
 
 import java.util.Map;
 
 import javax.sdp.SdpException;
 
 import android.util.Log;
 
 import com.kurento.commons.media.format.SessionSpec;
 import com.kurento.commons.media.format.conversor.SdpConversor;
 import com.kurento.commons.media.format.enums.MediaType;
 import com.kurento.commons.media.format.enums.Mode;
 import com.kurento.commons.mscontrol.MsControlException;
 import com.kurento.commons.mscontrol.join.Joinable;
 import com.kurento.commons.mscontrol.join.JoinableContainer;
 import com.kurento.kas.media.codecs.AudioCodecType;
 import com.kurento.kas.media.profiles.AudioProfile;
 import com.kurento.kas.media.rx.AudioRx;
 import com.kurento.kas.media.rx.AudioSamples;
 import com.kurento.kas.media.rx.MediaRx;
 import com.kurento.kas.media.tx.AudioInfoTx;
 import com.kurento.kas.media.tx.MediaTx;
 import com.kurento.kas.mscontrol.mediacomponent.internal.AudioSink;
 import com.kurento.kas.mscontrol.networkconnection.internal.RTPInfo;
 
 public class AudioJoinableStreamImpl extends JoinableStreamBase implements AudioSink, AudioRx {
 
 	public final static String LOG_TAG = "AudioJoinableStream";
 
 	private AudioInfoTx audioInfo;
 	private SessionSpec localSessionSpec;
 
 	private AudioRxThread audioRxThread = null;
 
 	private long timeFirstSamples;
 	private long timeLastSamples;
 
 	private int audioPacketTime; // ms
 	private long n;
 
 	public AudioInfoTx getAudioInfoTx() {
 		return audioInfo;
 	}
 
 	public AudioJoinableStreamImpl(JoinableContainer container,
 			StreamType type, SessionSpec remoteSessionSpec,
 			SessionSpec localSessionSpec, Integer maxDelayRx) {
 		super(container, type);
 		this.localSessionSpec = localSessionSpec;
 
 		Map<MediaType, Mode> mediaTypesModes = getModesOfMediaTypes(localSessionSpec);
 		Mode audioMode = mediaTypesModes.get(MediaType.AUDIO);
 		RTPInfo remoteRTPInfo = new RTPInfo(remoteSessionSpec);
 
 		if (audioMode != null) {
 			AudioCodecType audioCodecType = remoteRTPInfo.getAudioCodecType();
 			AudioProfile audioProfile = AudioProfile
 					.getAudioProfileFromAudioCodecType(audioCodecType);
 
 			if (audioProfile != null) {
 				this.audioInfo = new AudioInfoTx(audioProfile);
 				audioInfo.setOut(remoteRTPInfo.getAudioRTPDir());
 				audioInfo.setPayloadType(remoteRTPInfo.getAudioPayloadType());
 
 				if (Mode.SENDRECV.equals(audioMode) || Mode.SENDONLY.equals(audioMode)) {
 					audioInfo.setFrameSize(MediaTx.initAudio(audioInfo));
 					if (audioInfo.getFrameSize() < 0) {
 						Log.e(LOG_TAG, "Error in initAudio");
 						MediaTx.finishAudio();
 						return;
 					}
 				}
 
 				if ((Mode.SENDRECV.equals(audioMode) || Mode.RECVONLY.equals(audioMode))) {
 					this.audioRxThread = new AudioRxThread(this, maxDelayRx);
 					this.audioRxThread.start();
 				}
 			}
 		}

		this.audioPacketTime = (1000 * audioInfo.getFrameSize())
				/ audioInfo.getAudioProfile().getSampleRate();
		this.timeFirstSamples = -1;
 	}
 
 	@Override
 	public void putAudioSamples(short[] in_buffer, int in_size, long time) {
 		if (timeFirstSamples == -1) {
 			n = 0;
 			timeFirstSamples = time;
 		}
 		long diffFirstFrame = time - timeFirstSamples;
 		long diffLastFrame = time - timeLastSamples;
 		long drift = n - diffFirstFrame;
 		if (drift < -2 * audioPacketTime) {
 			Log.w(LOG_TAG, "GAP. Expected time: " + n + "  diffFirstFrame: "
 					+ diffFirstFrame + "  diffLastFrame: " + diffLastFrame
 					+ "  drift: " + drift);
 			n = diffFirstFrame - (diffFirstFrame % audioPacketTime);
 			Log.w(LOG_TAG, "n set to " + n);
 		}
 		MediaTx.putAudioSamples(in_buffer, in_size, n);
 		timeLastSamples = time;
 		n += audioPacketTime;
 	}
 
 	@Override
 	public void putAudioSamplesRx(AudioSamples audioSamples) {
 		try {
 			for (Joinable j : getJoinees(Direction.SEND))
 				if (j instanceof AudioRx)
 					((AudioRx) j).putAudioSamplesRx(audioSamples);
 		} catch (MsControlException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void stop() {
 		Log.d(LOG_TAG, "finishAudio");
 		MediaTx.finishAudio();
 		Log.d(LOG_TAG, "stopAudioRx");
 		MediaRx.stopAudioRx();
 	}
 
 	private class AudioRxThread extends Thread {
 		private AudioRx audioRx;
 		private int maxDelayRx;
 
 		public AudioRxThread(AudioRx audioRx, int maxDelayRx) {
 			this.audioRx = audioRx;
 			this.maxDelayRx = maxDelayRx;
 		}
 
 		@Override
 		public void run() {
 			Log.d(LOG_TAG, "startAudioRx");
 			SessionSpec s = filterMediaByType(localSessionSpec, MediaType.AUDIO);
 			if (!s.getMediaSpecs().isEmpty()) {
 				try {
 					String sdpAudio = SdpConversor.sessionSpec2Sdp(s);
 					MediaRx.startAudioRx(sdpAudio, maxDelayRx, this.audioRx);
 				} catch (SdpException e) {
 					Log.e(LOG_TAG, "Could not start audio rx " + e.toString());
 				}
 			}
 		}
 	}
 
 }
