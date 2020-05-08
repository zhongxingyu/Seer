 /*
  * Copyright 2004-2010 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package com.camobile.dig.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.camobile.dig.model.SamplerItem;
 import com.camobile.dig.model.SoundItem;
 
 public class SoxInterfaceService {
 
 	/**
 	 * sox へ直接コマンドを渡す。
 	 * @param nodes
 	 * @param outputFileName
 	 * @throws Exception
 	 */
 	public static void make(List nodes, String outputFileName) throws Exception {
 		if (nodes.size() < 1) {
 			return;
 		}
 //		String sox = "/Users/kuboyama/Downloads/sox-14.3.2/sox";
 		String sox = "sox";
 		String command = sox + " -m ";
 		
 		for (int i = 0; i < nodes.size(); i++) {
 			// とりあえずクラス名で分岐させる
 			if (nodes.get(i).getClass().equals(SoundItem.class)) {
 				SoundItem sound = (SoundItem) nodes.get(i);
 				command += " \"|"+ sox +" ~/music/track/"+ sound.getId() +".wav" +
 						" -p pad " + sound.getStart_time() +
 						" fade " + sound.getFade_in_until() +
 						" " + sound.getEnd_time() +
 						" " + sound.getFade_out_from() +
 						"\"";
 			} else if (nodes.get(i).getClass().equals(SamplerItem.class)) {
 				SamplerItem sampler = (SamplerItem) nodes.get(i);
 				command += " \"|"+ sox +" ~/music/sampler/"+ sampler.getId() +".wav" +
 						" -p pad " + sampler.getStart_time() +
 						" \"";
 			}
 		}
 		
 		command += " ~/music/result/" + outputFileName + ".wav";
 		System.out.println(command);
		String[] commands = {"/bin/sh", "-c", command};
 		Process pr = Runtime.getRuntime().exec(commands);
 		InputStream stderrStream = pr.getErrorStream();
 		System.out.println(inputStreemToString(stderrStream));
		String enc = "lame ~/music/result/" + outputFileName + ".wav ~/music/result/" + outputFileName + ".mp3";
		String scp = "scp -i ~/.ssh/backup/id_rsa ~/music/result/" + outputFileName + ".mp3 ec2-user@10.150.183.215:~/html/web_dj/music/result";
 	}
 	
 	
 	public static String inputStreemToString(InputStream in) throws IOException{
         
 	    BufferedReader reader = 
 	        new BufferedReader(new InputStreamReader(in, "UTF-8"/* 文字コード指定 */));
 	    StringBuffer buf = new StringBuffer();
 	    String str;
 	    while ((str = reader.readLine()) != null) {
 	            buf.append(str);
 	            buf.append("\n");
 	    }
 	    return buf.toString();
 	}
 	public static void main(String[] args) {
 		List l = new ArrayList();
 		SoundItem sound = new SoundItem();
 		sound.setId("kibou_no_wadachi");
 		sound.setStart_time("0");
 		sound.setFade_in_until("0");
 		sound.setFade_out_from("15");
 		sound.setEnd_time("20");
 		l.add(sound);
 		
 		sound = new SoundItem();
 		sound.setId("turn_me_on");
 		sound.setStart_time("15");
 		sound.setFade_in_until("20");
 		sound.setFade_out_from("30");
 		sound.setEnd_time("30");
 		l.add(sound);
 		
 		SamplerItem sampler = new SamplerItem();
 		sampler.setId("sp_8");
 		sampler.setStart_time("3");
 		l.add(sampler);
 		try {
 			make(l, "doya");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
