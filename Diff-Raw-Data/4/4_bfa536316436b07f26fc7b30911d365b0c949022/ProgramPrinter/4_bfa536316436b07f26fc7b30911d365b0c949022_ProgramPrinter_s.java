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
 
 package info.carlwithak.mpxg2.printing;
 
 import info.carlwithak.mpxg2.model.FrequencyRate;
 import info.carlwithak.mpxg2.model.GenericValue;
 import info.carlwithak.mpxg2.model.Parameter;
 import info.carlwithak.mpxg2.model.Patch;
 import info.carlwithak.mpxg2.model.Program;
 import info.carlwithak.mpxg2.model.effects.Effect;
 import info.carlwithak.mpxg2.model.effects.Reverb;
 import info.carlwithak.mpxg2.model.effects.algorithms.Chamber;
 import info.carlwithak.mpxg2.model.effects.algorithms.Plate;
 import java.text.DecimalFormat;
 
 import static info.carlwithak.mpxg2.printing.Util.onOffToString;
 import static info.carlwithak.mpxg2.printing.Util.signInt;
 
 /**
  * Class to print out a program nicely.
  *
  * @author carl
  */
 public class ProgramPrinter {
 
     private static final String[] EFFECT_TYPES = {
         "Effect 1", "Effect 2", "Chorus", "Delay", "Reverb", "Equalizer", "Gain",
         "Knob", "LFO1", "LFO2", "Rand", "A/B", "Env", "", "", "Post", "Send",
         "SpkrSim", "NGate", "Tempo"
     };
     private static final String[][] EFFECT_PARAMETERS = {
         {
             "Value", "Low", "High"
         },
         {
             "Mode", "Rate", "PW", "Phase", "Depth", "OnLvl", "OnSrc"
         },
         {
             "Mode", "Rate", "PW", "Phase", "Depth", "OnLvl", "OnSrc"
         },
         {
             "RndLo", "RndHi", "Rate"
         },
         {
             "", "ARate", "BRate"
         },
         {
             "Src1", "Src2", "ATrim", "Resp"
         },
         {},
         {},
         {},
         {
             "Level"
         },
         {},
         {},
         {
             null, "Send", "Thrsh", "Atten", "Offse", "ATime", "HTime", "RTime", "Delay"
         },
         {},
         {},
         {},
         {},
         {
             "FX1", "FX2", "Chrs", "Dly", "Rvb", "EQ", "Gain", "Ins"
         }
     };
     private static final String[][] EFFECT_PARAMETER_UNITS = {
         {
             "", "", ""
         },
         {
             "", null, "%", "", "%", "", ""
         },
         {
             "", null, "%", "", "%", "", ""
         },
         {
             "", "", "Hz"
         },
         {},
         {
             "?", "?", "", ""
         },
         {},
         {},
         {},
         {
             ""
         },
         {},
         {},
         {
             null, "OnOff", "dB", "dB", "dB", "", "", "", ""
         },
         {},
         {},
         {},
         {},
         {
             "OnOff", "OnOff", "OnOff", "OnOff", "OnOff", "OnOff", "OnOff", "OnOff"
         }
     };
 
     private final static String[] LFO_MODES = {
         "Off", "On", "Latch", "Gate", "Once", "Reset", "RTrig"
     };
 
     private final static String[] LFO_ON_SOURCES = {
         "none"
     };
 
     private final static String[] TEMPO_SOURCES = {
         "internal"
     };
 
     private final static String[] BEAT_VALUES = {
         "eighth", "dotted eighth", "quarter", "dotted quarter"
     };
 
     private final static String[] TAP_SOURCES = {
         "none"
     };
 
     private final static String[] AB_MODES = {
         "Trigger", "Gate"
     };
 
     private final static String[] ENVELOPE_GENERATOR_SOURCES = {
         "Off", "In", "Ret L", "Ret R", "Raw L", "Raw R", "FX 1 L", "FX 1 R",
         "FX 2 L", "FX 2 R", "Chrs L", "Chrs R", "EQ L", "EQ R", "Rvb L", "Rvb R",
         "Dly L", "Dly R", "PreOut", "MainL", "MainR"
     };
 
     private final static String[] SPEAKER_SIMULATOR_CABINET_DESIGNS = {
         "Combo1", "Combo2", "Stack1", "Stack2"
     };
 
     private final static String[] SPEAKER_SIMULATOR_SPEAKER_TYPES = {
         "Brite", "Norml", "Warm", "Dark"
     };
 
     private final static String[] NOISE_GATE_ENABLES = {
         "Off", "Guitar Input", "Returns Only"
     };
 
     private static final DecimalFormat DECIMAL_2DP = new DecimalFormat("0.00");
 
     public static String print(Program program) throws PrintException {
         StringBuilder sb = new StringBuilder();
         sb.append(program.getProgramName()).append("\n");
         StringBuilder styleSB = new StringBuilder();
         if (program.isAcoustic()) {
             styleSB.append("Acoustic, ");
         }
         if (program.isBass()) {
             styleSB.append("Bass, ");
         }
         if (program.isBlues()) {
             styleSB.append("Blues, ");
         }
         if (program.isClean()) {
             styleSB.append("Clean, ");
         }
         if (program.isCountry()) {
             styleSB.append("Country, ");
         }
         if (program.isJazz()) {
             styleSB.append("Jazz, ");
         }
         if (program.isRock()) {
             styleSB.append("Rock, ");
         }
         sb.append("  Guitar Style: ");
         if (styleSB.length() > 0) {
             sb.append(styleSB.substring(0, styleSB.length() - 2));
         }
         sb.append("\n");
         StringBuilder effectTypeSB = new StringBuilder();
         if (program.isChorus()) {
             effectTypeSB.append("Chorus, ");
         }
         if (program.isDelay()) {
             effectTypeSB.append("Delay, ");
         }
         if (program.isDistortion()) {
             effectTypeSB.append("Distortion, ");
         }
         if (program.isEq()) {
             effectTypeSB.append("EQ, ");
         }
         if (program.isFlanger()) {
             effectTypeSB.append("Flanger, ");
         }
         if (program.isGain()) {
             effectTypeSB.append("Gain, ");
         }
         if (program.isMod()) {
             effectTypeSB.append("Mod, ");
         }
         if (program.isOverdrive()) {
             effectTypeSB.append("Overdrive, ");
         }
         if (program.isPhaser()) {
             effectTypeSB.append("Phaser, ");
         }
         if (program.isPitch()) {
             effectTypeSB.append("Pitch, ");
         }
         if (program.isReverb()) {
             effectTypeSB.append("Reverb, ");
         }
         if (program.isSpeakerSim()) {
             effectTypeSB.append("Speaker Sim, ");
         }
         if (program.isWah()) {
             effectTypeSB.append("Wah, ");
         }
         sb.append("  Effect Type: ");
         if (effectTypeSB.length() > 0) {
             sb.append(effectTypeSB.substring(0, effectTypeSB.length() - 2));
         }
         sb.append("\n");
         StringBuilder appTypeSB = new StringBuilder();
         if (program.isPrePost()) {
             appTypeSB.append("Amp Input + FX Loop, ");
         }
         if (program.isStandAlone()) {
             appTypeSB.append("Stand alone, ");
         }
         if (program.isInline()) {
             appTypeSB.append("Amp Input Only, ");
         }
         sb.append("  Application Type: ");
         if (appTypeSB.length() > 0) {
             sb.append(appTypeSB.substring(0, appTypeSB.length() - 2));
         }
         sb.append("\n");
         // TODO indicate inactive effects
         sb.append("  Effect Routing:\n");
         for (String line : RoutingPrinter.print(program).split("\n")) {
             sb.append("    ").append(line).append("\n");
         }
         printProgram(sb, "Effect 1", program.getEffect1(), program.isEffect1On(), program.getEffect1ToePatch());
         printProgram(sb, "Effect 2", program.getEffect2(), program.isEffect2On(), program.getEffect2ToePatch());
         if (program.getChorus() != null) {
             sb.append("  Chorus: ").append(program.getChorus().getName()).append(" (").append(onOffToString(program.isChorusOn())).append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getChorusToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getChorus()));
         }
         if (program.getDelay() != null) {
             sb.append("  Delay: ").append(program.getDelay().getName()).append(" (").append(onOffToString(program.isDelayOn())).append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getDelayToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getDelay()));
         }
         if (program.getReverb() != null) {
             sb.append("  Reverb: ").append(program.getReverb().getName()).append(" (").append(onOffToString(program.isReverbOn())).append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getReverbToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getReverb()));
         }
         if (program.getEq() != null) {
             sb.append("  EQ: ").append(program.getEq().getName()).append(" (").append(onOffToString(program.isEqOn())).append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getEqToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getEq()));
         }
         if (program.getGain() != null) {
             sb.append("  Gain: ").append(program.getGain().getName()).append(" (").append(onOffToString(program.isGainOn())).append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getGainToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getGain()));
         }
         sb.append("  Softrow:\n");
         for (int i = 0; i < 10; i++) {
             sb.append(printSoftRow(program, i));
         }
         sb.append("  Patching:\n");
         sb.append(printPatch(program, program.getPatch1(), 1));
         sb.append(printPatch(program, program.getPatch2(), 2));
         sb.append(printPatch(program, program.getPatch3(), 3));
         sb.append(printPatch(program, program.getPatch4(), 4));
         sb.append(printPatch(program, program.getPatch5(), 5));
         sb.append("  Controllers:\n");
         sb.append("    Knob:\n");
         sb.append("      Value: ").append(program.getKnobValue()).append("\n");
         sb.append("      Low: ").append(program.getKnobLow()).append("\n");
         sb.append("      High: ").append(program.getKnobHigh()).append("\n");
         sb.append("      Name: ").append(program.getKnobName()).append("\n");
         sb.append("    LFO 1:\n");
         sb.append("      Mode: ").append(lfoModeToString(program.getLfo1Mode())).append("\n");
         sb.append("      Rate: ").append(RatePrinter.print(program.getLfo1Rate())).append("\n");
         sb.append("      PW: ").append(program.getLfo1PulseWidth()).append("%\n");
         sb.append("      Phase: ").append(program.getLfo1Phase()).append("\n");
         sb.append("      Depth: ").append(program.getLfo1Depth()).append("%\n");
         sb.append("      On Level: ").append(program.getLfo1OnLevel()).append("\n");
         sb.append("      On Source: ").append(lfoOnSourceToString(program.getLfo1OnSource())).append("\n");
         sb.append("    LFO 2:\n");
         sb.append("      Mode: ").append(lfoModeToString(program.getLfo2Mode())).append("\n");
         sb.append("      Rate: ").append(RatePrinter.print(program.getLfo2Rate())).append("\n");
         sb.append("      PW: ").append(program.getLfo2PulseWidth()).append("%\n");
         sb.append("      Phase: ").append(program.getLfo2Phase()).append("\n");
         sb.append("      Depth: ").append(program.getLfo2Depth()).append("%\n");
         sb.append("      On Level: ").append(program.getLfo2OnLevel()).append("\n");
         sb.append("      On Source: ").append(lfoOnSourceToString(program.getLfo2OnSource())).append("\n");
         sb.append("    Random:\n");
         sb.append("      Low: ").append(program.getRandomLow()).append("\n");
         sb.append("      High: ").append(program.getRandomHigh()).append("\n");
         sb.append("      Rate: ").append(DECIMAL_2DP.format(program.getRandomRate())).append("Hz\n");
         sb.append("    A/B:\n");
         sb.append("      Mode: ").append(abModeToString(program.getABMode())).append("\n");
         sb.append("      A Rate: ").append(program.getARate()).append("\n");
         sb.append("      B Rate: ").append(program.getBRate()).append("\n");
        sb.append("      On Level: ").append(program.getLfo2OnLevel()).append("\n");
        sb.append("      On Source: ").append(lfoOnSourceToString(program.getLfo2OnSource())).append("\n");
         sb.append("    Envelope:\n");
         sb.append("      Src1: ").append(envelopeGeneratorSourceToString(program.getEnvelopeGeneratorSrc1())).append("\n");
         sb.append("      Src2: ").append(envelopeGeneratorSourceToString(program.getEnvelopeGeneratorSrc2())).append("\n");
         sb.append("      A Trim: ").append(program.getEnvelopeGeneratorATrim()).append("\n");
         sb.append("      Resp: ").append(program.getEnvelopeGeneratorResponse()).append("\n");
         sb.append("  Mix:\n");
         sb.append("    Send:\n");
         sb.append("      Level: ").append(signInt(program.getSendLevel())).append("\n");
         sb.append("      Bypass Level: ").append(program.getSendBypassLevel()).append("\n");
         sb.append("    Post:\n");
         sb.append("      Mix: ").append(program.getPostMix()).append("%\n");
         sb.append("      Level: ").append(signInt(program.getPostLevel())).append("dB\n");
         sb.append("      Bypass Level: ").append(program.getPostBypassLevel()).append("dB\n");
         if (program.getEffect1() != null) {
             sb.append("    FX1:\n");
             sb.append("      Mix: ").append(program.getEffect1().getMix()).append("%\n");
             sb.append("      Level: ").append(signInt(program.getEffect1().getLevel())).append("dB\n");
         }
         if (program.getEffect2() != null) {
             sb.append("    FX2:\n");
             sb.append("      Mix: ").append(program.getEffect2().getMix()).append("%\n");
             sb.append("      Level: ").append(signInt(program.getEffect2().getLevel())).append("dB\n");
         }
         if (program.getChorus() != null) {
             sb.append("    Chorus:\n");
             sb.append("      Mix: ").append(program.getChorus().getMix()).append("%\n");
             sb.append("      Level: ").append(signInt(program.getChorus().getLevel())).append("dB\n");
         }
         if (program.getDelay() != null) {
             sb.append("    Delay:\n");
             sb.append("      Mix: ").append(program.getDelay().getMix()).append("%\n");
             sb.append("      Level: ").append(signInt(program.getDelay().getLevel())).append("dB\n");
         }
         if (program.getReverb() != null) {
             sb.append("    Reverb:\n");
             sb.append("      Mix: ").append(program.getReverb().getMix()).append("%\n");
             sb.append("      Level: ").append(signInt(program.getReverb().getLevel())).append("dB\n");
         }
         if (program.getEq() != null) {
             sb.append("    Eq:\n");
             sb.append("      Mix: ").append(program.getEq().getMix()).append("%\n");
             sb.append("      Level: ").append(signInt(program.getEq().getLevel())).append("dB\n");
         }
         sb.append("  Tempo:\n");
         sb.append("    Rate: ").append(program.getTempo()).append(" BPM\n");
         sb.append("    Source: ").append(tempoSourceToString(program.getTempoSource())).append("\n");
         sb.append("    Beat Value: ").append(beatValueToString(program.getBeatValue())).append("\n");
         sb.append("    Tap Average: ").append(program.getTapAverage()).append(" beats\n");
         sb.append("    Tap Source: ").append(tapSourceToString(program.getTapSource())).append("\n");
         sb.append("  Speaker Sim: ").append(onOffToString(program.isSpeakerSimulatorEnable())).append("\n");
         sb.append("    Cabinet: ").append(speakerSimulatorCabinetToString(program.getSpeakerSimulatorCabinet())).append("\n");
         sb.append("  Noise Gate:\n");
         sb.append("    Enable: ").append(noiseGateEnableToString(program.getNoiseGate().getEnable())).append("\n");
         sb.append("    Send: ").append(onOffToString(program.getNoiseGate().isSend())).append("\n");
         sb.append("    Thrsh: ").append(program.getNoiseGate().getThreshold()).append("dB\n");
         sb.append("    Atten: ").append(program.getNoiseGate().getAttenuation()).append("dB\n");
         sb.append("    Offset: ").append(program.getNoiseGate().getOffset()).append("dB\n");
         sb.append("    ATime: ").append(program.getNoiseGate().getATime()).append("\n");
         sb.append("    HTime: ").append(program.getNoiseGate().getHTime()).append("\n");
         sb.append("    RTime: ").append(program.getNoiseGate().getRTime()).append("\n");
         sb.append("    Delay: ").append(program.getNoiseGate().getDelay()).append("\n");
         return sb.toString().trim();
     }
 
     private static void printProgram(final StringBuilder sb, final String label, final Effect effect, final boolean effectOn, final int effectToePatch) throws PrintException {
         if (effect != null) {
             sb.append("  ").append(label).append(": ");
             sb.append(effect.getName()).append(" (").append(onOffToString(effectOn)).append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(effectToePatch)).append("\n");
             sb.append(AlgorithmPrinter.print(effect));
         }
     }
 
     private static String printSoftRow(final Program program, final int i) throws PrintException {
         // TODO should be null if it's not used
         if (program.getSoftRowEffectType(i) == 255 || program.getSoftRowParameter(i) == 255) {
             return "";
         }
         StringBuilder sb = new StringBuilder();
         sb.append("    ").append(i + 1).append(": ");
         sb.append(effectTypeToString(program.getSoftRowEffectType(i))).append(" ");
         String effectParameterName;
         if (program.getSoftRowEffectType(i) > 6) {
             effectParameterName = effectParameterToString(program.getSoftRowEffectType(i), program.getSoftRowParameter(i));
         } else {
             Parameter effectParameter = getEffectParameter(program, program.getSoftRowEffectType(i), program.getSoftRowParameter(i));
             if (effectParameter == null) {
                 return "";
             }
             effectParameterName = effectParameter.getName();
         }
         sb.append(effectParameterName).append("\n");
         return sb.toString();
     }
 
     private static String printPatch(final Program program, final Patch patch, final int patchNumber) throws PrintException {
         if (patch.getSourceIndex() == 0) {
             return "";
         }
         String patchParameter;
         String patchDestinationUnit;
         Parameter parameter = getEffectParameter(program, patch.getDestinationEffectIndex(), patch.getDestinationParameter());
         if (parameter == null) {
             patchParameter = effectParameterToString(patch.getDestinationEffectIndex(), patch.getDestinationParameter());
             patchDestinationUnit = getEffectParameterUnits(patch.getDestinationEffectIndex(), patch.getDestinationParameter());
             // get unit from LFO if that is the destination
             // TODO make this more generic
             if (patch.getDestinationEffectIndex() == 8 && patch.getDestinationParameter() == 1) {
                 parameter = program.getLfo1Rate();
                 patchDestinationUnit = parameter.getUnit();
                 if (parameter instanceof FrequencyRate) {
                     // TODO find a better way
                     patchDestinationUnit = "100" + patchDestinationUnit;
                 }
             }
             if (patch.getDestinationEffectIndex() == 9 && patch.getDestinationParameter() == 1) {
                 parameter = program.getLfo2Rate();
                 patchDestinationUnit = parameter.getUnit();
                 if (parameter instanceof FrequencyRate) {
                     // TODO find a better way
                     patchDestinationUnit = "100" + patchDestinationUnit;
                 }
             }
         } else {
             patchParameter = parameter.getName();
             patchDestinationUnit = parameter.getUnit();
             if (parameter instanceof GenericValue && ((GenericValue) parameter).getMinValue() instanceof Integer && ((Integer) ((GenericValue) parameter).getMinValue()) < 0) {
                 patchDestinationUnit = '-' + patchDestinationUnit;
             } else if (parameter instanceof FrequencyRate) {
                 // TODO find a better way
                 patchDestinationUnit = "100" + patchDestinationUnit;
             }
         }
         StringBuilder sb = new StringBuilder();
         sb.append("    Patch ").append(patchNumber).append(":\n");
         sb.append("      Source: ").append(patch.getSourceName()).append("\n");
         sb.append("        Min: ").append(patch.getSourceMin()).append("\n");
         sb.append("        Mid: ").append(patch.getSourceMid() == null ? "--" : patch.getSourceMid()).append("\n");
         sb.append("        Max: ").append(patch.getSourceMax()).append("\n");
         String patchEffect = patch.getDestinationEffectName();
         sb.append("      Destination: ").append(patchEffect).append(" ").append(patchParameter).append("\n");
         sb.append("        Min: ");
         sb.append(formatPatchParameter(patchDestinationUnit, patch.getDestinationMin(), patchEffect, patchParameter, program.getReverb())).append("\n");
         sb.append("        Mid: ");
         if (patch.getDestinationMid() == 0x8000) {
             sb.append("--\n");
         } else {
             sb.append(formatPatchParameter(patchDestinationUnit, patch.getDestinationMid(), patchEffect, patchParameter, program.getReverb())).append("\n");
         }
         sb.append("        Max: ");
         sb.append(formatPatchParameter(patchDestinationUnit, patch.getDestinationMax(), patchEffect, patchParameter, program.getReverb())).append("\n");
         return sb.toString();
     }
 
     private static String formatPatchParameter(final String patchDestinationUnit, final int parameterValue, final String patchEffect, final String patchParameter, final Reverb reverb) throws PrintException {
         StringBuilder sb = new StringBuilder();
         if ("Decay".equals(patchParameter)) {
             boolean link;
             double size;
             if (reverb instanceof Chamber) {
                 Chamber chamber = (Chamber) reverb;
                 link = chamber.isLink();
                 size = chamber.getSize();
             } else if (reverb instanceof Plate) {
                 Plate plate = (Plate) reverb;
                 link = plate.isLink();
                 size = plate.getSize();
             } else {
                 throw new PrintException("Cannot determine reverb decay for class " + reverb.getClass());
             }
             String reverbDecay = Util.reverbDecayToString(link, size, parameterValue);
             sb.append(reverbDecay).append("s");
         } else if(":".equals(patchDestinationUnit)) {
             sb.append(parameterValue % 0x100).append(patchDestinationUnit).append(parameterValue / 0x100);
         } else if ("Hz".equals(patchDestinationUnit)) {
             sb.append(parameterValue).append(patchDestinationUnit);
         } else if ("100Hz".equals(patchDestinationUnit)) {
             sb.append(DECIMAL_2DP.format(parameterValue / 100.0)).append(patchDestinationUnit.substring(3));
         } else if ("OnOff".equals(patchDestinationUnit)) {
             sb.append(Util.onOffToString(parameterValue == 1));
         } else if ("Send".equals(patchEffect) && "Level".equals(patchParameter)) {
             sb.append(signInt(parameterValue)).append(patchDestinationUnit);
         } else {
             // TODO better way of determining what sign is necessary
             if (patchDestinationUnit.indexOf("-") == 0) {
                 String newPatchDestinationUnit = patchDestinationUnit.substring(1);
                 sb.append(signInt(parameterValue)).append(newPatchDestinationUnit);
             } else {
                 sb.append(parameterValue).append(patchDestinationUnit);
             }
         }
         return sb.toString();
     }
 
     private static Parameter getEffectParameter(final Program program, final int effectIndex, final int parameterIndex) {
         Parameter parameter;
         switch (effectIndex) {
             case 0:
                 parameter = program.getEffect1() == null ? null : program.getEffect1().getParameter(parameterIndex);
                 break;
             case 1:
                 parameter = program.getEffect2() == null ? null : program.getEffect2().getParameter(parameterIndex);
                 break;
             case 2:
                 parameter = program.getChorus() == null ? null : program.getChorus().getParameter(parameterIndex);
                 break;
             case 3:
                 parameter = program.getDelay() == null ? null : program.getDelay().getParameter(parameterIndex);
                 break;
             case 4:
                 parameter = program.getReverb() == null ? null : program.getReverb().getParameter(parameterIndex);
                 break;
             case 5:
                 parameter = program.getEq() == null ? null : program.getEq().getParameter(parameterIndex);
                 break;
             case 6:
                 parameter = program.getGain() == null ? null : program.getGain().getParameter(parameterIndex);
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
     private static String toePatchToString(final int toePatch) throws PrintException {
         String s = null;
         switch(toePatch) {
             case 0:
                 s = "disabled";
                 break;
             case 1:
                 s = "off=bypass";
                 break;
             case 2:
                 s = "on=bypass";
                 break;
             default:
                 throw new PrintException("Invalid toe patch index:" + toePatch);
         }
         return s;
     }
 
     private static String effectTypeToString(final int effectType) {
         return EFFECT_TYPES[effectType];
     }
 
     private static String effectParameterToString(final int effectType, final int effectParameter) {
         // remove 7 from effectType as the 7 algorithm types take care of themselves
         return EFFECT_PARAMETERS[effectType - 7][effectParameter];
     }
 
     private static String getEffectParameterUnits(final int effectType, final int effectParameter) {
         // remove 7 from effectType as the 7 algorithm types take care of themselves
         return EFFECT_PARAMETER_UNITS[effectType - 7][effectParameter];
     }
 
     private static String lfoModeToString(final int lfoMode) throws PrintException {
         if (lfoMode < 0 || lfoMode >= LFO_MODES.length) {
             throw new PrintException("LFO Mode out of range");
         }
         return LFO_MODES[lfoMode];
     }
 
     private static String lfoOnSourceToString(final int lfoOnSource) {
         return LFO_ON_SOURCES[lfoOnSource];
     }
 
     private static String tempoSourceToString(final int tempoSource) {
         return TEMPO_SOURCES[tempoSource];
     }
 
     static String beatValueToString(final int beatValue) {
         String value;
         if (beatValue < BEAT_VALUES.length) {
             value = BEAT_VALUES[beatValue];
         } else {
             value = (beatValue - BEAT_VALUES.length + 2) + " beats";
         }
         return value;
     }
 
     private static String tapSourceToString(final int tapSource) {
         return TAP_SOURCES[tapSource];
     }
 
     private static String abModeToString(final int abMode) {
         return AB_MODES[abMode];
     }
 
     private static String envelopeGeneratorSourceToString(final int envelopeGeneratorSource) {
         return ENVELOPE_GENERATOR_SOURCES[envelopeGeneratorSource];
     }
 
     static String speakerSimulatorCabinetToString(final int speakerSimulatorCabinet) {
         String cabinet = SPEAKER_SIMULATOR_CABINET_DESIGNS[speakerSimulatorCabinet / 4];
         String speaker = SPEAKER_SIMULATOR_SPEAKER_TYPES[speakerSimulatorCabinet % 4];
         return cabinet + speaker;
     }
 
     private static String noiseGateEnableToString(final int noiseGateEnable) {
         return NOISE_GATE_ENABLES[noiseGateEnable];
     }
 }
