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
 
 import info.carlwithak.mpxg2.model.effects.algorithms.Plate;
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
 public class PlatePrinter implements Printer {
 
     private static final String[] REVERB_SPRED = {
         "0", "0", "0", "0", "1", "1", "1", "2", "2", "2", "3", "3", "3", "4",
         "4", "4", "5", "5", "5", "6", "6", "6", "7", "7", "7", "8", "8", "8",
         "9", "9", "9", "10", "10", "10", "11", "11", "11", "12", "12", "12",
         "13", "13", "13", "14", "14", "14", "15", "15", "15", "16", "16", "16",
         "17", "17", "17", "18", "18", "18", "19", "19", "19", "20", "20", "20",
         "21", "21", "21", "22", "22", "22", "23", "23", "23", "24", "24", "24",
         "25", "25", "25", "26", "26", "26", "27", "27", "27", "28", "28", "28",
         "29", "29", "29", "30", "30", "30", "31", "31", "31", "32", "32", "32",
         "33", "33", "33", "34", "34", "34", "35", "35", "35", "36", "36", "36",
         "37", "37", "37", "38", "38", "38", "39", "39", "39", "40", "40", "40",
         "41", "41", "41", "42", "42", "42", "43", "43", "43", "44", "44", "44",
         "45", "45", "45", "46", "46", "46", "47", "47", "47", "48", "48", "48",
         "49", "49", "49", "50", "50", "50", "51", "51", "51", "52", "52", "52",
         "53", "53", "53", "53", "54", "54", "54", "55", "55", "55", "56", "56",
         "56", "57", "57", "57", "58", "58", "58", "59", "59", "59", "60", "60",
         "60", "61", "61", "61", "62", "62", "62", "63", "63", "63", "64", "64",
         "64", "65", "65", "65", "66", "66", "66", "67", "67", "67", "68", "68",
         "68", "69", "69", "69", "70", "70", "70", "71", "71", "71", "72", "72",
         "72", "73", "73", "73", "74", "74", "74", "75", "75", "75", "76", "76",
         "76", "77", "77", "77", "78", "78", "78", "79", "79", "79", "80", "80",
         "80", "81", "81", "81", "82", "82", "82", "83", "83", "83", "84", "84"
     };
 
     @Override
     public String print(Object algorithm) {
         Plate plate = (Plate) algorithm;
         StringBuilder sb = new StringBuilder();
         sb.append("    Mix: ").append(plate.getMix()).append("%\n");
         sb.append("    Level: ").append(signInt(plate.getLevel())).append("dB\n");
         sb.append("    Size: ").append(plate.getSize()).append("m\n");
         sb.append("    Link: ").append(plate.getLink() == 0 ? "off" : "on").append("\n");
         sb.append("    Diff: ").append(plate.getDiff()).append("%\n");
         sb.append("    Pre Delay: ").append(plate.getPreDelay()).append("ms\n");
         sb.append("    Bass: ").append(reverbBassToString(plate.getBass())).append("X\n");
        sb.append("    Decay: ").append(reverbDecayToString(plate.getLink(), plate.getSize(), plate.getDecay())).append("s\n");
         sb.append("    Xovr: ").append(reverbXovrToString(plate.getXovr())).append("\n");
         sb.append("    Rt HC: ").append(reverbRtHCToString(plate.getRtHC())).append("\n");
         sb.append("    Shape: ").append(plate.getShape()).append("\n");
         sb.append("    Spred: ").append(reverbSpredToString(plate.getSpred())).append("\n");
         return sb.toString();
     }
 
     private static String reverbSpredToString(final int reverbSpred) {
         return REVERB_SPRED[reverbSpred];
     }
 }
