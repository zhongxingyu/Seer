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
 
 package info.carlwithak.mpxg2.model.effects.algorithms;
 
 import info.carlwithak.mpxg2.model.effects.Eq;
 import info.carlwithak.mpxg2.model.parameters.GenericValue;
 import info.carlwithak.mpxg2.model.parameters.Parameter;
 
 /**
  * Class for 2-Band (D) parameters.
  *
  * @author Carl Green
  */
 public class TwoBandDual extends Eq {
     private static final String NAME = "2-Band (D)";
 
    public final GenericValue<Integer> gainLeft1 = new GenericValue<Integer>("Gain", "dB", -72, 24);
    public final GenericValue<Integer> gainLeft2 = new GenericValue<Integer>("Gain", "dB", -72, 24);
    public final GenericValue<Integer> fcLeft1 = new GenericValue<Integer>("Fc", "Hz", 20, 20000);
    public final GenericValue<Integer> fcLeft2 = new GenericValue<Integer>("Fc", "Hz", 20, 20000);
    public final GenericValue<Double> qLeft1 = new GenericValue<Double>("Q", "", 0.1, 10.0);
    public final GenericValue<Double> qLeft2 = new GenericValue<Double>("Q", "", 0.1, 10.0);
    public final GenericValue<Integer> modeLeft1 = new GenericValue<Integer>("Mode", "", 0, 2);
    public final GenericValue<Integer> modeLeft2 = new GenericValue<Integer>("Mode", "", 0, 2);
    public final GenericValue<Integer> gainRight1 = new GenericValue<Integer>("Gain", "dB", -72, 24);
    public final GenericValue<Integer> gainRight2 = new GenericValue<Integer>("Gain", "dB", -72, 24);
    public final GenericValue<Integer> fcRight1 = new GenericValue<Integer>("Fc", "Hz", 20, 20000);
    public final GenericValue<Integer> fcRight2 = new GenericValue<Integer>("Fc", "Hz", 20, 20000);
    public final GenericValue<Double> qRight1 = new GenericValue<Double>("Q", "", 0.1, 10.0);
    public final GenericValue<Double> qRight2 = new GenericValue<Double>("Q", "", 0.1, 10.0);
    public final GenericValue<Integer> modeRight1 = new GenericValue<Integer>("Mode", "", 0, 2);
    public final GenericValue<Integer> modeRight2 = new GenericValue<Integer>("Mode", "", 0, 2);
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public Parameter getParameter(final int parameterIndex) {
         Parameter parameter;
         switch (parameterIndex) {
             case 0:
             case 1:
                 parameter = super.getParameter(parameterIndex);
                 break;
             case 2:
                 parameter = gainLeft1;
                 break;
             case 3:
                 parameter = gainLeft2;
                 break;
             case 4:
                 parameter = fcLeft1;
                 break;
             case 5:
                 parameter = fcLeft2;
                 break;
             case 6:
                 parameter = qLeft1;
                 break;
             case 7:
                 parameter = qLeft2;
                 break;
             case 8:
                 parameter = modeLeft1;
                 break;
             case 9:
                 parameter = modeLeft2;
                 break;
             case 10:
                 parameter = gainRight1;
                 break;
             case 11:
                 parameter = gainRight2;
                 break;
             case 12:
                 parameter = fcRight1;
                 break;
             case 13:
                 parameter = fcRight2;
                 break;
             case 14:
                 parameter = qRight1;
                 break;
             case 15:
                 parameter = qRight2;
                 break;
             case 16:
                 parameter = modeRight1;
                 break;
             case 17:
                 parameter = modeRight2;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
 }
