 package com.remuladgryta.modjam.test;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.ServerChatEvent;
 
 import com.remuladgryta.modjam.API;
 import com.remuladgryta.modjam.LogHelper;
 import com.remuladgryta.modjam.RecordingTracker;
 import com.remuladgryta.modjam.SpeechDetectionEvent;
 
 public class ClientProxy extends CommonProxy {
 	@Override
 	public void init(){
 		
 		API.get().addDictionaryEntry(this.getClass().getResource("/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz/dict/cmudict.0.6d"));
 		API.get().addDictionaryEntry(
 				this.getClass().getResource(
 						"/com/remuladgryta/modjam/res/testaddenda.dict"));
		API.get().addGrammarEntry("person", "paul | evandro | will", false);
 		//API.get().addGrammarEntry("greeting", "Hello | Good morning", false);
 		API.get().addGrammarEntry("phrase","(hello | good morning) <person>",true);
 		API.get().addGrammarEntry("thuum", "fus", true);
 		//API.get().addGrammarEntry("testhello", "hello anton",true);
 		MinecraftForge.EVENT_BUS.register(this);
 	}
 	
 	private int refcount = 0;
 
 	@ForgeSubscribe
 	public void onChat(ServerChatEvent e) {
 		if (e.message.equals("start")) {
 			RecordingTracker.instance().requestRecording("tester");
 			refcount++;
 		} else if (e.message.equals("stop")) {
 			if (refcount > 0) {
 				RecordingTracker.instance().releaseRecordingHandler("tester");
 				refcount--;
 			} else {
 				e.player.sendChatToPlayer("Can't close what you didn't open...");
 			}
 		}
 	}
 
 	@ForgeSubscribe
 	public void onSpeech(SpeechDetectionEvent e) {
 		LogHelper.log(Level.INFO, "Detected " + e.detectedSpeech);
 	}
 }
