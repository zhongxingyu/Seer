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
 
 import info.carlwithak.mpxg2.model.GenericValue;
 import info.carlwithak.mpxg2.model.Parameter;
 import info.carlwithak.mpxg2.model.Rate;
 import info.carlwithak.mpxg2.model.effects.Chorus;
 
 /**
  * Class for Aerosol parameters.
  *
  * @author Carl Green
  */
 public class Aerosol extends Chorus {
     private static final String NAME = "Aerosol";
 
     private Rate rate1;
     private GenericValue<Integer> pulseWidth1 = new GenericValue<Integer>("PW 1", "%", 0, 100);
     private GenericValue<Integer> depth1 = new GenericValue<Integer>("Dpth1", "%", 0, 100);
     private Rate rate2;
    private GenericValue<Integer> pulseWidth2 = new GenericValue<Integer>("PW 2", "%", 0, 100);
     private GenericValue<Integer> depth2 = new GenericValue<Integer>("Dpth2", "%", 0, 100);
    private GenericValue<Integer> resonance1 = new GenericValue<Integer>("Res 1", "", -100, 100);
    private GenericValue<Integer> resonance2 = new GenericValue<Integer>("Res 2", "", -100, 100);
 
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
                 parameter = depth1;
                 break;
             case 5:
                 parameter = rate2;
                 break;
             case 6:
                 parameter = pulseWidth2;
                 break;
             case 7:
                 parameter = depth2;
                 break;
             case 8:
                 parameter = resonance1;
                 break;
             case 9:
                 parameter = resonance2;
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
 
     public GenericValue<Integer> getPulseWidth1() {
         return pulseWidth1;
     }
 
     public void setPulseWidth1(int pulseWidth1) {
         this.pulseWidth1.setValue(pulseWidth1);
     }
 
     public GenericValue<Integer> getDepth1() {
         return depth1;
     }
 
     public void setDepth1(int depth1) {
         this.depth1.setValue(depth1);
     }
 
     public Rate getRate2() {
         return rate2;
     }
 
     public void setRate2(Rate rate2) {
         this.rate2 = rate2;
     }
 
     public GenericValue<Integer> getPulseWidth2() {
         return pulseWidth2;
     }
 
     public void setPulseWidth2(int pulseWidth2) {
         this.pulseWidth2.setValue(pulseWidth2);
     }
 
     public GenericValue<Integer> getDepth2() {
         return depth2;
     }
 
     public void setDepth2(int depth2) {
         this.depth2.setValue(depth2);
     }
 
     public GenericValue<Integer> getResonance1() {
         return resonance1;
     }
 
     public void setResonance1(int resonance1) {
         this.resonance1.setValue(resonance1);
     }
 
     public GenericValue<Integer> getResonance2() {
         return resonance2;
     }
 
     public void setResonance2(int resonance2) {
         this.resonance2.setValue(resonance2);
     }
 }
