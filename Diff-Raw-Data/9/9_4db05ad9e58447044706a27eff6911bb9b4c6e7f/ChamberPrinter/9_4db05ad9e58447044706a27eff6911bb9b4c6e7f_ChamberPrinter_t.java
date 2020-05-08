 /*
  *  Copyright (C) 2011 Carl Green
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.carlwithak.mpxg2.printing.effects.algorithms;
 
 import info.carlwithak.mpxg2.model.effects.algorithms.Chamber;
 import info.carlwithak.mpxg2.printing.AlgorithmPrinter.Printer;
 
 import static info.carlwithak.mpxg2.printing.Util.reverbBassToString;
 import static info.carlwithak.mpxg2.printing.Util.reverbDecayToString;
 import static info.carlwithak.mpxg2.printing.Util.reverbRtHCToString;
 import static info.carlwithak.mpxg2.printing.Util.reverbXovrToString;
 import static info.carlwithak.mpxg2.printing.Util.signInt;
 
 /**
  *
  * @author Carl Green
  */
 public class ChamberPrinter implements Printer {
 
     @Override
     public String print(Object algorithm) {
         Chamber chamber = (Chamber) algorithm;
         StringBuilder sb = new StringBuilder();
         sb.append("    Mix: ").append(chamber.getMix()).append("%\n");
         sb.append("    Level: ").append(signInt(chamber.getLevel())).append("dB\n");
         sb.append("    Size: ").append(chamber.getSize()).append("m\n");
         sb.append("    Link: ").append(chamber.getLink() == 0 ? "off" : "on").append("\n");
         sb.append("    Diff: ").append(chamber.getDiff()).append("%\n");
         sb.append("    Pre Delay: ").append(chamber.getPreDelay()).append("ms\n");
         sb.append("    Bass: ").append(reverbBassToString(chamber.getBass())).append("X\n");
        sb.append("    Decay: ").append(reverbDecayToString(chamber.getLink(), chamber.getSize(), chamber.getDecay())).append("s\n");
         sb.append("    Xovr: ").append(reverbXovrToString(chamber.getXovr())).append("\n");
         sb.append("    Rt HC: ").append(reverbRtHCToString(chamber.getRtHC())).append("\n");
         sb.append("    Shape: ").append(chamber.getShape()).append("\n");
         sb.append("    Spred: ").append(chamber.getSpred()).append("\n");
         return sb.toString();
     }
 }
