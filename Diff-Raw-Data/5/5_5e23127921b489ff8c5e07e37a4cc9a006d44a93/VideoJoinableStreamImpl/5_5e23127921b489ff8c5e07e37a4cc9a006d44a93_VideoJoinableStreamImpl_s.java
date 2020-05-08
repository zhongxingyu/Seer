 package com.kurento.kas.mscontrol.join;
 
 import java.util.Map;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import android.util.Log;
 
 import com.kurento.commons.media.format.SessionSpec;
 import com.kurento.commons.media.format.SpecTools;
 import com.kurento.commons.mscontrol.MsControlException;
 import com.kurento.commons.mscontrol.join.Joinable;
 import com.kurento.commons.mscontrol.join.JoinableContainer;
 import com.kurento.commons.sdp.enums.MediaType;
 import com.kurento.commons.sdp.enums.Mode;
 import com.kurento.kas.media.VideoCodecType;
 import com.kurento.kas.media.profiles.VideoProfile;
 import com.kurento.kas.media.rx.MediaRx;
 import com.kurento.kas.media.rx.VideoRx;
 import com.kurento.kas.media.tx.MediaTx;
 import com.kurento.kas.media.tx.VideoInfoTx;
 import com.kurento.kas.mscontrol.mediacomponent.VideoSink;
 import com.kurento.kas.mscontrol.networkconnection.RTPInfo;
 
 public class VideoJoinableStreamImpl extends JoinableStreamBase implements
 		VideoSink, VideoRx {
 
 	public final static String LOG_TAG = "VideoJoinableStream";
 
 	private VideoProfile videoProfile = null;
 	private SessionSpec localSessionSpec;
 
 	private VideoTxThread videoTxThread = null;
 	private VideoRxThread videoRxThread = null;
 
 	private class Frame {
 		private byte[] data;
 		private int width;
 		private int height;
 
 		public Frame(byte[] data, int width, int height) {
 			this.data = data;
 			this.width = width;
 			this.height = height;
 		}
 	}
 
 	private int QUEUE_SIZE = 2;
 	private LinkedBlockingDeque<Frame> framesQueue = new LinkedBlockingDeque<Frame>(
 			QUEUE_SIZE);
 	private LinkedBlockingDeque<Long> txTimes = new LinkedBlockingDeque<Long>(
 			QUEUE_SIZE);
 
 	public VideoProfile getVideoProfile() {
 		return videoProfile;
 	}
 
 	public VideoJoinableStreamImpl(JoinableContainer container,
 			StreamType type, SessionSpec remoteSessionSpec,
 			SessionSpec localSessionSpec, Integer framesQueueSize) {
 		super(container, type);
 		this.localSessionSpec = localSessionSpec;
 		if (framesQueueSize != null && framesQueueSize > QUEUE_SIZE)
 			QUEUE_SIZE = framesQueueSize;
 		Log.d(LOG_TAG, "QUEUE_SIZE: " + QUEUE_SIZE);
 		
 		Map<MediaType, Mode> mediaTypesModes = SpecTools
 				.getModesOfFirstMediaTypes(localSessionSpec);
 		Mode videoMode = mediaTypesModes.get(MediaType.VIDEO);
 		RTPInfo remoteRTPInfo = new RTPInfo(remoteSessionSpec);
 
 		if (videoMode != null) {
 			VideoCodecType videoCodecType = remoteRTPInfo.getVideoCodecType();
 			VideoProfile videoProfile = VideoProfile
 					.getVideoProfileFromVideoCodecType(videoCodecType);
			if ((Mode.SENDRECV.equals(videoMode) || Mode.RECVONLY
 					.equals(videoMode)) && videoProfile != null) {
 				VideoInfoTx videoInfo = new VideoInfoTx(videoProfile);
 				videoInfo.setOut(remoteRTPInfo.getVideoRTPDir());
 				videoInfo.setPayloadType(remoteRTPInfo.getVideoPayloadType());
 				int ret = MediaTx.initVideo(videoInfo);
 				if (ret < 0) {
					Log.d(LOG_TAG, "Error in initVideo");
 					MediaTx.finishVideo();
 				}
 				this.videoProfile = videoProfile;
 				this.videoTxThread = new VideoTxThread();
 				this.videoTxThread.start();
 			}
 
 			if ((Mode.SENDRECV.equals(videoMode) || Mode.RECVONLY
 					.equals(videoMode))) {
 				this.videoRxThread = new VideoRxThread(this);
 				this.videoRxThread.start();
 			}
 		}
 
 	}
 
 	@Override
 	public void putVideoFrame(byte[] data, int width, int height) {
 		if (framesQueue.size() >= QUEUE_SIZE)
 			framesQueue.pollLast();
 		framesQueue.offerFirst(new Frame(data, width, height));
 	}
 
 	@Override
 	public void putVideoFrameRx(int[] rgb, int width, int height) {
 		try {
 			for (Joinable j : getJoinees(Direction.SEND))
 				if (j instanceof VideoRx)
 					((VideoRx) j).putVideoFrameRx(rgb, width, height);
 		} catch (MsControlException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void stop() {
 		if (videoTxThread != null)
 			videoTxThread.interrupt();
 
 		Log.d(LOG_TAG, "finishVideo");
 		MediaTx.finishVideo();
 		Log.d(LOG_TAG, "stopVideoRx");
 		MediaRx.stopVideoRx();
 	}
 
 	private class VideoTxThread extends Thread {
 		@Override
 		public void run() {
 			int tFrame = 1000 / videoProfile.getFrameRate();
 			Frame frameProcessed;
 
 			try {
 				for (int i = 0; i < QUEUE_SIZE; i++)
 					txTimes.offerFirst(new Long(0));
 				for (;;) {
 					long t = System.currentTimeMillis();
 					long h = (t - txTimes.takeLast()) / QUEUE_SIZE;
 					if (h < tFrame) {
 						long s = tFrame - h;
 						sleep(s);
 					}
 					frameProcessed = framesQueue.takeLast();
 					txTimes.offerFirst(t);
 					MediaTx.putVideoFrame(frameProcessed.data,
 							frameProcessed.width, frameProcessed.height);
 				}
 			} catch (InterruptedException e) {
 				Log.d(LOG_TAG, "VideoTxThread stopped");
 			}
 		}
 	}
 
 	private class VideoRxThread extends Thread {
 		private VideoRx videoRx;
 
 		public VideoRxThread(VideoRx videoRx) {
 			this.videoRx = videoRx;
 		}
 
 		@Override
 		public void run() {
 			Log.d(LOG_TAG, "startVideoRx");
 			if (!SpecTools.filterMediaByType(localSessionSpec, "video")
 					.getMediaSpec().isEmpty()) {
 				String sdpVideo = SpecTools.filterMediaByType(localSessionSpec,
 						"video").toString();
 				MediaRx.startVideoRx(sdpVideo, this.videoRx);
 			}
 		}
 	}
 
 }
