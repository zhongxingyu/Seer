 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.debug;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.util.FileUtils;
 import com.jpii.navalbattle.util.GrammarManager;
 import com.jpii.navalbattle.util.OSUtil;
 
 import java.io.File;
 import javax.swing.*;
 
 import maximusvladimir.dagen.Rand;
 
 public class MaxTests {
 	/**
 	 * Run <code>MaxTests</code>.
 	 */
 	public static void run() {
 		
 		Rand r = new Rand();
 		for (int c = 0; c < 3; c++) {
 			System.out.println(GrammarManager.generateFullName(r.nextInt()));
 		}
 		System.out.println("Seed:" + Game.Settings.seed);
 		
 		System.out.println("Measured system speed as: "+PavoHelper.getCalculatedSystemSpeed().toString());
 		int unit = Runtime.getRuntime().availableProcessors();
		if (unit >= 1 && unit < 3) {
 			JOptionPane.showMessageDialog(null,"Your system does not have a strong enough CPU to run the game.","Fatal error",JOptionPane.OK_OPTION);
 			System.exit(0x300AB);
 		}
 		System.out.println("Number of cocurrent cores: " + unit);
 		System.out.println("Total memory avaliable to the system: " + (OSUtil.getTotalOSRAM()/1024.0f/1024.0f) + " MB");
 	}
 	
 	/**
 	 * Returns if program is running for the first time.
 	 * @return
 	 */
 	public static boolean isFirstRun() {
 		if (!new File(getSettingsPath()).exists())
 			return true;
 		else
 			return false;
 	}
 	
 	/**
 	 * Returns settings file path.
 	 * @return
 	 */
 	public static String getSettingsPath() {
 		return (FileUtils.getSavingDirectory().getAbsolutePath()+"\\settings.inf");
 	}
 }
