 package com.aqua.music.bo.audio.player;
 
 import java.util.Collection;
 
 import open.music.api.InstrumentRole;
 
 import com.aqua.music.bo.audio.manager.AudioPlayRightsManager;
 import com.aqua.music.model.core.DynamicFrequency;
 import com.aqua.music.model.core.Frequency;
 
 /**
  * @author "Shruti Tiwari"
  * 
  */
 class AudioPlayerImplWithDynamicSound implements AudioPlayer {
 	private volatile AudioPlayRightsManager audioPlayRightsManager;
 	private BasicNotePlayer basicNotePlayer;
 
 	public void setBasicNotePalyer(BasicNotePlayer basicNotePlayer){
 		this.basicNotePlayer = basicNotePlayer;
 	}
 	
 	public void playFrequencies(final Collection<? extends DynamicFrequency> frequencyList, final int repeatCount) {
 		try {
 			audioPlayRightsManager.acquireRightToPlay();
 			logger.debug("acquired right to play");
			basicNotePlayer.start(calculateTotalDuration(frequencyList));
 			generateSound(frequencyList, repeatCount);
 			basicNotePlayer.finish();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			basicNotePlayer.tidyup();
 			logger.debug("releasing right to play");
 			audioPlayRightsManager.releaseRightToPlay();
 		}
 	}
 
 	public void playFrequenciesInLoop(final Collection<? extends DynamicFrequency> frequencyList) {
 		try {
 			audioPlayRightsManager.acquireRightToPlay();
 			logger.debug("acquired right to play");
			basicNotePlayer.start(calculateTotalDuration(frequencyList));
 			while (!audioPlayRightsManager.isMarkedToStopPlaying()) {
 				generateSound(frequencyList, 1);
 			}
 			basicNotePlayer.finish();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			basicNotePlayer.tidyup();
 			logger.debug("releasing right to play");
 			audioPlayRightsManager.releaseRightToPlay();
 		}
 	}
 
 	private int calculateTotalDuration(final Collection<? extends DynamicFrequency> frequencyList) {
 		int totalDuration=0;
 		for(DynamicFrequency each: frequencyList){
 			final int customizedDuration = audioPlayRightsManager.tempoMultiplier(each.duration());
 			totalDuration+=customizedDuration;
 		}
 		return totalDuration;
 	}
 
 	public final Runnable playTask(final Collection<? extends DynamicFrequency> frequencyList, final int repeatCount) {
 		Runnable task = new Runnable() {
 			@Override
 			public void run() {
 				playFrequencies(frequencyList, repeatCount);
 			}
 		};
 		return task;
 	}
 
 	@Override
 	public Runnable playTaskInLoop(final Collection<? extends DynamicFrequency> frequencyList) {
 		Runnable task = new Runnable() {
 			@Override
 			public void run() {
 				playFrequenciesInLoop(frequencyList);
 			}
 		};
 		return task;
 	}
 
 	public void setAudioPlayRigthsManager(AudioPlayRightsManager audioPlayRightsManager) {
 		this.audioPlayRightsManager = audioPlayRightsManager;
 	}
 
 	public void stop() {
 		basicNotePlayer.stop();
 	}
 
 	private void generateSound(final Collection<? extends DynamicFrequency> frequencyList, int repeatCount) {
 		for (int i = 0; i < repeatCount; i++) {
 			for (final DynamicFrequency each : frequencyList) {
 				if (audioPlayRightsManager.isMarkedToStopPlaying()) {
 					logger.debug("oops, marked to stop..breaking now.");
 					return;
 				} else if (audioPlayRightsManager.pauseCurrentPlay()) {
 					while (audioPlayRightsManager.pauseCurrentPlay() && !audioPlayRightsManager.isMarkedToStopPlaying()) {
 						try {
 							Thread.sleep(100);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					logger.debug("Pause cleared Or marked to stop playing!");
 				}
 				if (audioPlayRightsManager.isMarkedToStopPlaying()) {
 					return;
 				} else {
 					final int customizedDuration = audioPlayRightsManager.tempoMultiplier(each.duration());
 					throwExceptionForInsaneInput(customizedDuration);
 					basicNotePlayer.play(each, customizedDuration);
 				}
 			}
 		}
 	}
 
 	private void throwExceptionForInsaneInput(int msecs) {
 		if (msecs <= 0)
 			throw new IllegalArgumentException("Duration <= 0 msecs");
 	}
 
 	@Override
 	public void changeInstrumentTo(String instrument, InstrumentRole instrumentRole) {
 		basicNotePlayer.notifyInstrumentChange(instrument, instrumentRole);
 	}
 
 	@Override
 	public String[] allInstruments() {
 		return basicNotePlayer.allInstruments();
 	}
 }
