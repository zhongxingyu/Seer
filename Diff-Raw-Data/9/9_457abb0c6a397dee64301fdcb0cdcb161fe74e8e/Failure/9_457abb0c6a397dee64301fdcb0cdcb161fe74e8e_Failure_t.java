 package com.qzx.au.hud;
 
 import net.minecraft.client.gui.ScaledResolution;
 
 import com.qzx.au.core.UI;
 
 public class Failure {
     private static UI ui = new UI();
 	private static int count = 0;
 
 	public static void reset(){
 		Failure.count = 0;
 	}
 
 	public static void log(String location){
 		Failure.count++;
		System.err.println("AU HUD: caught exception in "+location);
 	}
 
 	public static void show(ScaledResolution screen){
 		if(Failure.count == 0) return;
 
 		Failure.ui.setCursor(screen.getScaledWidth()/2, screen.getScaledHeight()-10);
 		Failure.ui.drawString(UI.ALIGN_CENTER, String.format("%d HUD failure%s, check logs", Failure.count, (Failure.count > 1 ? "s" : "")), 0xff6666, 0);
 	}
 }
