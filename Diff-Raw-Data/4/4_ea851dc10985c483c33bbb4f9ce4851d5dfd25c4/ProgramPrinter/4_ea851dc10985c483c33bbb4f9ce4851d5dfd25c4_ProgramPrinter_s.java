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
 
 import info.carlwithak.mpxg2.model.Patch;
 import info.carlwithak.mpxg2.model.Program;
 import info.carlwithak.mpxg2.model.effects.algorithms.Chamber;
 import info.carlwithak.mpxg2.model.effects.algorithms.Plate;
 import java.text.DecimalFormat;
 
 import static info.carlwithak.mpxg2.printing.Util.signInt;
 
 /**
  * Class to print out a program nicely.
  *
  * @author carl
  */
 public class ProgramPrinter {
 
     private static final String[] EFFECT1_ALGORITHM_NAMES = {
         "no effect", "Detune (M)", "Detune (S)", "Detune (D)", "Shift (M)", "Shift (S)", "Shift (D)",
         "DiatonicHmy", "Panner", "Auto Pan", "Tremolo (M)", "Tremolo (S)", "UniVybe", "Custom Vybe",
         "Phaser", "OrangePhase", "Red Comp", "Blue Comp", "DigiDrive1", "DigiDrive2", "OctaBuzz",
         "SweepFilter", "1-Band (M)", "Wah  1", "Wah  2", "Pedal Wah 1", "Pedal Wah 2",
         "Volume (M)", "Volume (S)", "Volume (D)", "Pedal Vol", "ExtPedalVol", "Test Tone", "Click"
     };
     private static final String[] EFFECT2_ALGORITHM_NAMES = {
         "no effect", "Panner", "Auto Pan", "Tremolo (M)", "Tremolo (S)", "UniVybe", "Custom Vybe",
         "Phaser", "OrangePhase", "Red Comp", "Blue Comp", "DigiDrive1", "DigiDrive2", "OctaBuzz",
         "SweepFilter", "1-Band (M)", "Wah  1", "Wah  2", "Pedal Wah 1", "Pedal Wah 2",
         "Volume (M)", "Volume (S)", "Volume (D)", "Pedal Vol", "ExtPedalVol", "Test Tone", "Click"
     };
     private static final String[] CHORUS_ALGORITHM_NAMES = {
         "no effect", "Chorus", "Detune (M)", "Flanger (M)", "Flanger24(M)", "Flanger (S)", "Rotary Cab",
         "Aerosol", "Orbits", "Centrifuge1", "Centrifuge2", "Comb 1", "Comb 2",
         "Volume (M)", "Volume (S)", "Volume (D)", "Pedal Vol", "ExtPedalVol"
     };
     private static final String[] DELAY_ALGORITHM_NAMES = {
         "no effect", "Delay (M)", "Delay (S)", "Delay (D)", "Echo (M)", "Echo (S)", "Echo (D)",
         "Looper", "JamMan", "Ducker"
     };
     private static final String[] REVERB_ALGORITHM_NAMES = {
         "no effect", "Chamber", "Hall", "Plate", "Ambience", "Gate"
     };
     private static final String[] EQ_ALGORITHM_NAMES = {
         "no effect", "1-Band (M)", "2-Band (M)", "3-Band (M)", "4-Band (M)",
         "1-Band (S)", "2-Band (S)", "1-Band (D)", "2-Band (D)", "Fc Splitter",
         "Crossover", "Volume (M)", "Volume (S)", "Volume (D)", "Pedal Vol",
         "ExtPedalVol"
     };
     private static final String[] GAIN_ALGORITHM_NAMES = {
         "no effect", "Tone", "Crunch", "Screamer", "Overdrive", "Distortion", "Preamp", "SplitPreamp"
     };
 
     private static final String[] EFFECT_TYPES = {
         "Effect 1", "Effect 2", "Chorus", "Delay", "Reverb", "Equalizer", "Gain",
         "Knob", "LFO1", "LFO2", "Rand", "A/B", "Env", "", "", "Post", "Send",
         "SpkrSim", "NGate", "Tempo"
     };
     private static final String[][][] EFFECT_PARAMETERS = {
         {},
         {},
         {},
         {},
         {},
         {},
         {
             {},
             {
                 "Lo", "Mid", "Hi", "InLvl", "Level"
             },
             {},
             {
                 "Lo", "Mid", "Hi", "Drive", "Tone", "Level"
             },
             {
                 "Lo", "Mid", "Hi", "InLvl", "LoCut", "Feel", "Drive", "Tone", "Level"
             }
         },
         {},
         {},
         {},
         {},
         {
             {
                 "", "ARate", "BRate"
             }
         },
         {},
         {},
         {},
         {},
         {
             {
                 "Level"
             }
         }
     };
     private static final String[][][] EFFECT_PARAMETER_UNITS = {
         {},
         {},
         {},
         {},
         {},
         {},
         {
             {},
             {
                 "Lo", "Mid", "Hi", "InLvl", "Level"
             },
             {},
             {
                 "Lo", "Mid", "Hi", "Drive", "Tone", "Level"
             },
             {
                 "Lo", "Mid", "Hi", "InLvl", "LoCut", "Feel", "Drive", "Tone", "Level"
             }
         },
         {},
         {},
         {},
         {},
         {},
         {},
         {},
         {},
         {},
         {
             {
                 ""
             }
         }
     };
 
     private final static String[] LFO_ON_SOURCES = {
         "none"
     };
 
     private final static String[] TEMPO_SOURCES = {
         "internal"
     };
 
     private final static String[] BEAT_VALUES = {
         "", "", "quarter"
     };
 
     private final static String[] TAP_SOURCES = {
         "none"
     };
 
     private final static String[] SPEAKER_SIMULATOR_CABINETS = {
         "", "ComboNorml"
     };
 
     private final static String[] NOISE_GATE_ENABLES = {
         "", "Guitar Input"
     };
 
     private static final DecimalFormat DECIMAL_2DP = new DecimalFormat("0.00");
 
     static String print(Program program) throws PrintException {
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
         if (program.getEffect1Algorithm() > 0) {
             sb.append("  Effect 1: ").append(effect1AlgorithmToString(program.getEffect1Algorithm())).append(" (").append(program.isEffect1On() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getEffect1ToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getEffect1()));
         }
         if (program.getEffect2Algorithm() > 0) {
             sb.append("  Effect 2: ").append(effect2AlgorithmToString(program.getEffect2Algorithm())).append(" (").append(program.isEffect2On() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getEffect2ToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getEffect2()));
         }
         if (program.getChorusAlgorithm() > 0) {
             sb.append("  Chorus: ").append(chorusAlgorithmToString(program.getChorusAlgorithm())).append(" (").append(program.isChorusOn() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getChorusToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getChorus()));
         }
         if (program.getDelayAlgorithm() > 0) {
             sb.append("  Delay: ").append(delayAlgorithmToString(program.getDelayAlgorithm())).append(" (").append(program.isDelayOn() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getDelayToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getDelay()));
         }
         if (program.getReverbAlgorithm() > 0) {
             sb.append("  Reverb: ").append(reverbAlgorithmToString(program.getReverbAlgorithm())).append(" (").append(program.isReverbOn() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getReverbToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getReverb()));
         }
         if (program.getEqAlgorithm() > 0) {
             sb.append("  EQ: ").append(eqAlgorithmToString(program.getEqAlgorithm())).append(" (").append(program.isEqOn() ? "on" : "off").append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(program.getEqToePatch())).append("\n");
             sb.append(AlgorithmPrinter.print(program.getEq()));
         }
         if (program.getGainAlgorithm() > 0) {
             sb.append("  Gain: ").append(gainAlgorithmToString(program.getGainAlgorithm())).append(" (").append(program.isGainOn() ? "on" : "off").append(")").append("\n");
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
         sb.append("      Mode: ").append(program.getLfo1Mode() == 0 ? "Off" : "On").append("\n");
         sb.append("      Rate: ").append(DECIMAL_2DP.format(program.getLfo1Rate())).append("Hz\n");
         sb.append("      PW: ").append(program.getLfo1PulseWidth()).append("%\n");
         sb.append("      Phase: ").append(program.getLfo1Phase()).append("\n");
         sb.append("      Depth: ").append(program.getLfo1Depth()).append("%\n");
         sb.append("      On Level: ").append(program.getLfo1OnLevel()).append("\n");
         sb.append("      On Source: ").append(lfoOnSourceToString(program.getLfo1OnSource())).append("\n");
         sb.append("    LFO 2:\n");
         sb.append("      Mode: ").append(program.getLfo2Mode() == 0 ? "Off" : "On").append("\n");
         sb.append("      Rate: ").append(DECIMAL_2DP.format(program.getLfo2Rate())).append("Hz\n");
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
         sb.append("      Mode: ").append(program.getABMode() == 0 ? "Trigger" : "?").append("\n");
         sb.append("      A Rate: ").append(program.getARate()).append("\n");
         sb.append("      B Rate: ").append(program.getBRate()).append("\n");
         sb.append("      On Level: ").append(program.getLfo2OnLevel()).append("\n");
         sb.append("      On Source: ").append(lfoOnSourceToString(program.getLfo2OnSource())).append("\n");
         sb.append("    Envelope:\n");
        sb.append("      Src1: ").append(program.getEnvelopeGeneratorSrc1() == 0 ? "off" : program.getEnvelopeGeneratorSrc1()).append("\n");
        sb.append("      Src2: ").append(program.getEnvelopeGeneratorSrc2() == 0 ? "off" : program.getEnvelopeGeneratorSrc2()).append("\n");
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
         sb.append("  Speaker Sim: ").append(program.getSpeakerSimulatorEnable() == 0 ? "off" : "on").append("\n");
         sb.append("    Cabinet: ").append(speakerSimulatorCabinetToString(program.getSpeakerSimulatorCabinet())).append("\n");
         sb.append("  Noise Gate:\n");
         sb.append("    Enable: ").append(noiseGateEnableToString(program.getNoiseGate().getEnable())).append("\n");
         sb.append("    Send: ").append(program.getNoiseGate().getSend() == 0 ? "off" : "on").append("\n");
         sb.append("    Thrsh: ").append(program.getNoiseGate().getThreshold()).append("dB\n");
         sb.append("    Atten: ").append(program.getNoiseGate().getAttenuation()).append("dB\n");
         sb.append("    Offset: ").append(program.getNoiseGate().getOffset()).append("dB\n");
         sb.append("    ATime: ").append(program.getNoiseGate().getATime()).append("\n");
         sb.append("    HTime: ").append(program.getNoiseGate().getHTime()).append("\n");
         sb.append("    RTime: ").append(program.getNoiseGate().getRTime()).append("\n");
         sb.append("    Delay: ").append(program.getNoiseGate().getDelay()).append("\n");
         return sb.toString().trim();
     }
 
     private static String printSoftRow(final Program program, final int i) {
         // TODO should be null if it's not used
         if (program.getSoftRowEffectType(i) == 255 || program.getSoftRowParameter(i) == 255) {
             return "";
         }
         StringBuilder sb = new StringBuilder();
         sb.append("    ").append(i + 1).append(": ");
         sb.append(effectTypeToString(program.getSoftRowEffectType(i))).append(" ");
         int algorithm = getAlgorithmForEffectType(program, program.getSoftRowEffectType(i));
         String effectParameter = null;
         switch (program.getSoftRowEffectType(i)) {
             case 0:
                 effectParameter = program.getEffect1().getParameterName(program.getSoftRowParameter(i));
                 break;
             case 1:
                 effectParameter = program.getEffect2().getParameterName(program.getSoftRowParameter(i));
                 break;
             case 2:
                 effectParameter = program.getChorus().getParameterName(program.getSoftRowParameter(i));
                 break;
             case 3:
                 effectParameter = program.getDelay().getParameterName(program.getSoftRowParameter(i));
                 break;
             case 4:
                 effectParameter = program.getReverb().getParameterName(program.getSoftRowParameter(i));
                 break;
         }
         if (effectParameter == null) {
             effectParameter = effectParameterToString(program.getSoftRowEffectType(i), algorithm, program.getSoftRowParameter(i));
         }
         sb.append(effectParameter).append("\n");
         return sb.toString();
     }
 
     private static String printPatch(final Program program, final Patch patch, final int patchNumber) throws PrintException {
         if (patch.getSourceIndex() == 0) {
             return "";
         }
         int algorithm = getAlgorithmForEffectType(program, patch.getDestinationEffectIndex());
         String patchParameter = null;
         String patchDestinationUnit = null;
         switch (patch.getDestinationEffectIndex()) {
             case 0:
                 patchParameter = program.getEffect1().getParameterName(patch.getDestinationParameter());
                 patchDestinationUnit = program.getEffect1().getParameterUnit(patch.getDestinationParameter());
                 break;
             case 1:
                 patchParameter = program.getEffect2().getParameterName(patch.getDestinationParameter());
                 patchDestinationUnit = program.getEffect2().getParameterUnit(patch.getDestinationParameter());
                 break;
             case 2:
                 patchParameter = program.getChorus().getParameterName(patch.getDestinationParameter());
                 patchDestinationUnit = program.getChorus().getParameterUnit(patch.getDestinationParameter());
                 break;
             case 3:
                 patchParameter = program.getDelay().getParameterName(patch.getDestinationParameter());
                 patchDestinationUnit = program.getDelay().getParameterUnit(patch.getDestinationParameter());
                 break;
             case 4:
                 patchParameter = program.getReverb().getParameterName(patch.getDestinationParameter());
                 patchDestinationUnit = program.getReverb().getParameterUnit(patch.getDestinationParameter());
                 break;
         }
         if (patchParameter == null) {
             patchParameter = effectParameterToString(patch.getDestinationEffectIndex(), algorithm, patch.getDestinationParameter());
         }
         if (patchDestinationUnit == null) {
             patchDestinationUnit = getEffectParameterUnits(patch.getDestinationEffectIndex(), algorithm, patch.getDestinationParameter());
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
         if ("Decay".equals(patchParameter)) {
             int link;
             double size;
             if (program.getReverb() instanceof Chamber) {
                 Chamber chamber = (Chamber) program.getReverb();
                 link = chamber.getLink();
                 size = chamber.getSize();
             } else if (program.getReverb() instanceof Plate) {
                 Plate plate = (Plate) program.getReverb();
                 link = plate.getLink();
                 size = plate.getSize();
             } else {
                 throw new PrintException("Cannot determine reverb decay for class " + program.getReverb().getClass());
             }
             String reverbDecay = Util.reverbDecayToString(link, size, patch.getDestinationMin());
             sb.append(reverbDecay).append("s\n");
         } else if(":".equals(patchDestinationUnit)) {
             sb.append(patch.getDestinationMin() % 0x100).append(patchDestinationUnit).append(patch.getDestinationMin() / 0x100).append("\n");
         } else if ("%".equals(patchDestinationUnit)) {
             sb.append(patch.getDestinationMin()).append(patchDestinationUnit).append("\n");
         } else if ("-%".equals(patchDestinationUnit)) {
             sb.append(signInt(patch.getDestinationMin())).append("%\n");
         } else if ("Hz".equals(patchDestinationUnit)) {
             sb.append(DECIMAL_2DP.format(patch.getDestinationMin() / 100.0)).append(patchDestinationUnit).append("\n");
         } else {
             sb.append(signInt(patch.getDestinationMin())).append(patchDestinationUnit).append("\n");
         }
         sb.append("        Mid: ");
         if ("Hz".equals(patchDestinationUnit)) {
             sb.append(patch.getDestinationMid() == 0x8000 ? "--" : DECIMAL_2DP.format(patch.getDestinationMid() / 100.0)).append(patchDestinationUnit).append("\n");
         } else {
             sb.append(patch.getDestinationMid() == 0x8000 ? "--" : patch.getDestinationMid()).append("\n");
         }
         sb.append("        Max: ");
         if ("Decay".equals(patchParameter)) {
             int link;
             double size;
             if (program.getReverb() instanceof Chamber) {
                 Chamber chamber = (Chamber) program.getReverb();
                 link = chamber.getLink();
                 size = chamber.getSize();
             } else if (program.getReverb() instanceof Plate) {
                 Plate plate = (Plate) program.getReverb();
                 link = plate.getLink();
                 size = plate.getSize();
             } else {
                 throw new PrintException("Cannot determine reverb decay for class " + program.getReverb().getClass());
             }
             String reverbDecay = Util.reverbDecayToString(link, size, patch.getDestinationMax());
             sb.append(reverbDecay).append("s");
         } else if (":".equals(patchDestinationUnit)) {
             sb.append(patch.getDestinationMax() % 0x100).append(patchDestinationUnit).append(patch.getDestinationMax() / 0x100);
         } else if ("%".equals(patchDestinationUnit)) {
             sb.append(patch.getDestinationMax()).append(patchDestinationUnit);
         } else if ("-%".equals(patchDestinationUnit)) {
             sb.append(signInt(patch.getDestinationMax())).append('%');
         } else if ("Hz".equals(patchDestinationUnit)) {
             sb.append(DECIMAL_2DP.format(patch.getDestinationMax() / 100.0)).append(patchDestinationUnit);
         } else {
             sb.append(signInt(patch.getDestinationMax())).append(patchDestinationUnit);
         }
         sb.append("\n");
         return sb.toString();
     }
 
     private static int getAlgorithmForEffectType(final Program program, final int effectType) {
         int algorithm;
         switch (effectType) {
             case 0:
                 algorithm = program.getEffect1Algorithm();
                 break;
             case 1:
                 algorithm = program.getEffect2Algorithm();
                 break;
             case 2:
                 algorithm = program.getChorusAlgorithm();
                 break;
             case 3:
                 algorithm = program.getDelayAlgorithm();
                 break;
             case 4:
                 algorithm = program.getReverbAlgorithm();
                 break;
             case 5:
                 algorithm = program.getEqAlgorithm();
                 break;
             case 6:
                 algorithm = program.getGainAlgorithm();
                 break;
             default:
                 algorithm = 0;
         }
         return algorithm;
     }
 
     private static String effect1AlgorithmToString(final int effect1Algorithm) {
         return EFFECT1_ALGORITHM_NAMES[effect1Algorithm];
     }
 
     private static String effect2AlgorithmToString(final int effect2Algorithm) {
         return EFFECT2_ALGORITHM_NAMES[effect2Algorithm];
     }
 
     private static String chorusAlgorithmToString(final int chorusAlgorithm) {
         return CHORUS_ALGORITHM_NAMES[chorusAlgorithm];
     }
 
     private static String delayAlgorithmToString(final int delayAlgorithm) {
         return DELAY_ALGORITHM_NAMES[delayAlgorithm];
     }
 
     private static String reverbAlgorithmToString(final int reverbAlgorithm) {
         return REVERB_ALGORITHM_NAMES[reverbAlgorithm];
     }
 
     private static String eqAlgorithmToString(final int eqAlgorithm) {
         return EQ_ALGORITHM_NAMES[eqAlgorithm];
     }
 
     private static String gainAlgorithmToString(final int gainAlgorithm) {
         return GAIN_ALGORITHM_NAMES[gainAlgorithm];
     }
 
     private static String toePatchToString(final int toePatch) {
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
         }
         return s;
     }
 
     private static String effectTypeToString(final int effectType) {
         return EFFECT_TYPES[effectType];
     }
 
     private static String effectParameterToString(final int effectType, final int algorithm, final int effectParameter) {
         return EFFECT_PARAMETERS[effectType][algorithm][effectParameter];
     }
 
     private static String getEffectParameterUnits(final int effectType, final int algorithm, final int effectParameter) {
         return EFFECT_PARAMETER_UNITS[effectType][algorithm][effectParameter];
     }
 
     private static String lfoOnSourceToString(final int lfoOnSource) {
         return LFO_ON_SOURCES[lfoOnSource];
     }
 
     private static String tempoSourceToString(final int tempoSource) {
         return TEMPO_SOURCES[tempoSource];
     }
 
     private static String beatValueToString(final int beatValue) {
         return BEAT_VALUES[beatValue];
     }
 
     private static String tapSourceToString(final int tapSource) {
         return TAP_SOURCES[tapSource];
     }
 
     private static String speakerSimulatorCabinetToString(final int speakerSimulatorCabinet) {
         return SPEAKER_SIMULATOR_CABINETS[speakerSimulatorCabinet];
     }
 
     private static String noiseGateEnableToString(final int noiseGateEnable) {
         return NOISE_GATE_ENABLES[noiseGateEnable];
     }
 }
