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
 
 import info.carlwithak.mpxg2.model.effects.Chorus;
 import info.carlwithak.mpxg2.model.parameters.GenericValue;
 import info.carlwithak.mpxg2.model.parameters.Parameter;
 import info.carlwithak.mpxg2.model.parameters.Rate;
 
 /**
  * Class for Centrifuge2 parameters.
  *
  * @author Carl Green
  */
 public class Centrifuge2 extends Chorus {
     private static final String NAME = "Centrifuge2";
 
     private Rate rate1;
     public final GenericValue<Integer> pulseWidth1 = new GenericValue<Integer>("PW 1", "%", 0, 100);
     public final GenericValue<Integer> sync1 = new GenericValue<Integer>("Sync1", "", -120, 120);
     public final GenericValue<Integer> depth1 = new GenericValue<Integer>("Dpth1", "%", 0, 100);
     private Rate rate2;
     public final GenericValue<Integer> pulseWidth2 = new GenericValue<Integer>("PW 2", "%", 0, 100);
     public final GenericValue<Integer> sync2 = new GenericValue<Integer>("Sync2", "", -120, 120);
     public final GenericValue<Integer> depth2 = new GenericValue<Integer>("Dpth2", "%", 0, 100);
    public final GenericValue<Integer> resonance = new GenericValue<Integer>("Res", "%", -100, 100);
 
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
                 parameter = rate1;
                 break;
             case 3:
                 parameter = pulseWidth1;
                 break;
             case 4:
                 parameter = sync1;
                 break;
             case 5:
                 parameter = depth1;
                 break;
             case 6:
                 parameter = rate2;
                 break;
             case 7:
                 parameter = pulseWidth2;
                 break;
             case 8:
                 parameter = sync2;
                 break;
             case 9:
                 parameter = depth2;
                 break;
             case 10:
                 parameter = resonance;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
     public Rate getRate1() {
         return rate1;
     }
 
     public void setRate1(Rate rate1) {
         this.rate1 = rate1;
     }
 
     public Rate getRate2() {
         return rate2;
     }
 
     public void setRate2(Rate rate2) {
         this.rate2 = rate2;
     }
 
 }
