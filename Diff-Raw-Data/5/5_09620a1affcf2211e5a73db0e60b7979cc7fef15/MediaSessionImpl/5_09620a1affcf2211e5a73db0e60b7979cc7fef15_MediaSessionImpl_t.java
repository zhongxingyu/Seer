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
 
 package com.kurento.kas.mscontrol.internal;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Map;
 
 import android.hardware.Camera.Size;
 
 import com.kurento.commons.mscontrol.Configuration;
 import com.kurento.commons.mscontrol.MsControlException;
 import com.kurento.commons.mscontrol.Parameters;
 import com.kurento.commons.mscontrol.mediacomponent.MediaComponent;
 import com.kurento.commons.mscontrol.networkconnection.NetworkConnection;
 import com.kurento.commons.sdp.enums.MediaType;
 import com.kurento.commons.sdp.enums.Mode;
 import com.kurento.kas.media.codecs.AudioCodecType;
 import com.kurento.kas.media.codecs.VideoCodecType;
 import com.kurento.kas.mscontrol.MediaSessionAndroid;
 import com.kurento.kas.mscontrol.mediacomponent.MediaComponentAndroid;
 import com.kurento.kas.mscontrol.mediacomponent.internal.AudioPlayerComponent;
 import com.kurento.kas.mscontrol.mediacomponent.internal.AudioRecorderComponent;
 import com.kurento.kas.mscontrol.mediacomponent.internal.VideoPlayerComponent;
 import com.kurento.kas.mscontrol.mediacomponent.internal.VideoRecorderComponent;
 import com.kurento.kas.mscontrol.networkconnection.NetIF;
 import com.kurento.kas.mscontrol.networkconnection.internal.NetworkConnectionImpl;
 
 public class MediaSessionImpl implements MediaSessionAndroid {
 
 	public final static String LOG_TAG = "MSImpl";
 
 	private MediaSessionConfig mediaSessionConfig;
 
 	public MediaSessionImpl(Parameters params) throws MsControlException {
 		this.mediaSessionConfig = getMediaSessionconfigFromParams(params);
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public NetworkConnection createNetworkConnection()
 			throws MsControlException {
 		return new NetworkConnectionImpl(mediaSessionConfig);
 	}
 
 	@Override
 	public MediaComponentAndroid createMediaComponent(
 			Configuration<MediaComponent> predefinedConfig, Parameters params)
 			throws MsControlException {
 
 		if (predefinedConfig == null)
 			throw new MsControlException("Configuration is NULL");
 
 		if (MediaComponentAndroid.AUDIO_PLAYER.equals(predefinedConfig))
 			return new AudioPlayerComponent();
 		else if (MediaComponentAndroid.AUDIO_RECORDER.equals(predefinedConfig))
 			return new AudioRecorderComponent(params);
 		else if (MediaComponentAndroid.VIDEO_PLAYER.equals(predefinedConfig))
 			return new VideoPlayerComponent(params);
 		else if (MediaComponentAndroid.VIDEO_RECORDER.equals(predefinedConfig))
 			return new VideoRecorderComponent(params);
 
 		throw new MsControlException("Configuration is not supported: "
 				+ predefinedConfig);
 	}
 
 	private MediaSessionConfig getMediaSessionconfigFromParams(Parameters params)
 			throws MsControlException {
 		if (params == null)
 			throw new MsControlException("Parameters are NULL");
 
 		Object obj;
 
 		
 		obj = params.get(STUN_HOST);
 		if (obj == null)
 			throw new MsControlException(
 					"Params must have MediaSessionAndroid.STUN_HOST param");
 		if (!(obj instanceof String))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.STUN_HOST must be instance of String");
 		String stunHost= (String) obj;
 		
 		obj = params.get(STUN_PORT);
 		if (obj == null)
 			throw new MsControlException(
 					"Params must have MediaSessionAndroid.STUN_PORT param");
 		if (!(obj instanceof Integer))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.STUN_PORT must be instance of Integer");
 		Integer stunPort = (Integer) obj;
 		
 		obj = params.get(NET_IF);
 		if (obj == null)
 			throw new MsControlException(
 					"Params must have MediaSessionAndroid.NET_IF param");
 		if (!(obj instanceof NetIF))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.NET_IF must be instance of NetIF");
 		NetIF netIF = (NetIF) obj;
 
 		obj = params.get(LOCAL_ADDRESS);
 		if (obj == null) {
 			throw new MsControlException(
 					"Params must have MediaSessionAndroid.LOCAL_ADDRESS param");
 		} else if (!(obj instanceof InetAddress))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.LOCAL_ADDRESS must be instance of InetAddress");
 		InetAddress localAddress = (InetAddress) obj;
 		
 		obj = params.get(PUBLIC_ADDRESS);
 		if (obj == null) {
 			throw new MsControlException(
 					"Params must have MediaSessionAndroid.PUBLIC_ADDRESS param");
 		} else if (!(obj instanceof InetAddress))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.PUBLIC_ADDRESS must be instance of InetAddress");
 		InetAddress publicAddress = (InetAddress) obj;
 
 		Integer maxBW = null;
 		obj = params.get(MAX_BANDWIDTH);
 		if (obj == null) {
 			// Por defecto
 		} else if (!(obj instanceof Integer))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.MAX_BANDWIDTH must be instance of Integer");
 		else
 			maxBW = (Integer) obj;
 
 		Map<MediaType, Mode> mediaTypeModes = null;
 		obj = params.get(STREAMS_MODES);
 		if (obj == null) {
 			// Por defecto
 		} else
 			try {
 				mediaTypeModes = (Map<MediaType, Mode>) obj;
 			} catch (ClassCastException e) {
 				throw new MsControlException(
 						"Parameter MediaSessionAndroid.STREAMS_MODES must be instance of Map<MediaType, Mode>",
 						e);
 			}
 
 		ArrayList<AudioCodecType> audioCodecs = null;
 		obj = params.get(AUDIO_CODECS);
 		if (obj == null) {
 			// Por defecto
 		} else
 			try {
 				audioCodecs = (ArrayList<AudioCodecType>) obj;
 			} catch (ClassCastException e) {
 				throw new MsControlException(
 						"Parameter MediaSessionAndroid.AUDIO_CODECS must be instance of ArrayList<AudioCodecType>",
 						e);
 			}
 
 		ArrayList<VideoCodecType> videoCodecs = null;
 		obj = params.get(VIDEO_CODECS);
 		if (obj == null) {
 			// Por defecto
 		} else
 			try {
 				videoCodecs = (ArrayList<VideoCodecType>) obj;
 			} catch (ClassCastException e) {
 				throw new MsControlException(
 						"Parameter MediaSessionAndroid.VIDEO_CODECS must be instance of ArrayList<VideoCodecType>",
 						e);
 			}
 
 		Integer frameWidth = null;
 		obj = params.get(FRAME_WIDTH);
 		if (obj == null) {
 			// Por defecto
		} else if (!(obj instanceof Integer))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.FRAME_WIDTH must be instance of Integer");
 		else
 			frameWidth = (Integer) obj;
 		
 		Integer frameHeight = null;
 		obj = params.get(FRAME_HEIGTH);
 		if (obj == null) {
 			// Por defecto
		} else if (!(obj instanceof Integer))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.FRAME_HEIGTH must be instance of Integer");
 		else
 			frameHeight = (Integer) obj;
 
 		Integer maxFrameRate = null;
 		obj = params.get(MAX_FRAME_RATE);
 		if (obj == null) {
 			// Por defecto
 		} else if (!(obj instanceof Integer))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.MAX_FRAME_RATE must be instance of Integer");
 		else
 			maxFrameRate = (Integer) obj;
 
 		Integer gopSize = null;
 		obj = params.get(GOP_SIZE);
 		if (obj == null) {
 			// Por defecto
 		} else if (!(obj instanceof Integer))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.GOP_SIZE must be instance of Integer");
 		else
 			gopSize = (Integer) obj;
 
 		Integer framesQueueSize = null;
 		obj = params.get(FRAMES_QUEUE_SIZE);
 		if (obj == null) {
 			// Por defecto
 		} else if (!(obj instanceof Integer))
 			throw new MsControlException(
 					"Parameter MediaSessionAndroid.FRAMES_QUEUE_SIZE must be instance of Integer");
 		else
 			framesQueueSize = (Integer) obj;
 
 		return new MediaSessionConfig(netIF, localAddress, publicAddress, maxBW,
 				mediaTypeModes, audioCodecs, videoCodecs, frameWidth, frameHeight,
 				maxFrameRate, gopSize, framesQueueSize, stunHost, stunPort);
 	}
 
 }
