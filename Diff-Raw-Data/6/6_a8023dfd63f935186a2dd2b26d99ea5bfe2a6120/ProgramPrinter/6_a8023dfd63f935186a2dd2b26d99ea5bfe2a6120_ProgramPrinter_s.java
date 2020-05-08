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
 
 import info.carlwithak.mpxg2.model.Ab;
 import info.carlwithak.mpxg2.model.DataObject;
 import info.carlwithak.mpxg2.model.EnvelopeGenerator;
 import info.carlwithak.mpxg2.model.Knob;
 import info.carlwithak.mpxg2.model.Lfo;
 import info.carlwithak.mpxg2.model.NoiseGate;
 import info.carlwithak.mpxg2.model.Patch;
 import info.carlwithak.mpxg2.model.Program;
 import info.carlwithak.mpxg2.model.Random;
 import info.carlwithak.mpxg2.model.SendMix;
 import info.carlwithak.mpxg2.model.effects.EffectObject;
 import info.carlwithak.mpxg2.model.parameters.OnOffValue;
 import info.carlwithak.mpxg2.model.parameters.Parameter;
 
 import static info.carlwithak.mpxg2.printing.Util.printParameter;
 
 /**
  * Class to print out a program nicely.
  *
  * @author carl
  */
 public class ProgramPrinter {
 
     private static final String[] TOE_PATCH_STATES = {
         "disabled", "off=bypass", "on=bypass"
     };
 
     private static final String[] EFFECT_TYPES = {
         "Effect 1", "Effect 2", "Chorus", "Delay", "Reverb", "Equalizer", "Gain",
         "Knob", "LFO1", "LFO2", "Rand", "A/B", "Env", "", "", "Post", "Send",
         "SpkrSim", "NGate", "Tempo"
     };
 
     public static String print(Program program) throws PrintException {
         StringBuilder sb = new StringBuilder();
         sb.append(program.getProgramName().getDisplayString()).append("\n");
         sb.append("  Guitar Style: ").append(printGuitarStyles(program)).append("\n");
         sb.append("  Effect Type: ").append(printEffectTypes(program)).append("\n");
         sb.append("  Application Type: ").append(printApplicationTypes(program)).append("\n");
         // TODO indicate inactive effects
         sb.append("  Effect Routing:\n");
         for (String line : RoutingPrinter.print(program).split("\n")) {
             sb.append("    ").append(line).append("\n");
         }
         printProgram(sb, "Effect 1", program.getEffect1(), program.getEffectsStatus().getEffect1On(), program.getEffect1ToePatch());
         printProgram(sb, "Effect 2", program.getEffect2(), program.getEffectsStatus().getEffect2On(), program.getEffect2ToePatch());
         printProgram(sb, "Chorus", program.getChorus(), program.getEffectsStatus().getChorusOn(), program.getChorusToePatch());
         printProgram(sb, "Delay", program.getDelay(), program.getEffectsStatus().getDelayOn(), program.getDelayToePatch());
         printProgram(sb, "Reverb", program.getReverb(), program.getEffectsStatus().getReverbOn(), program.getReverbToePatch());
         printProgram(sb, "EQ", program.getEq(), program.getEffectsStatus().getEqOn(), program.getEqToePatch());
         printProgram(sb, "Gain", program.getGain(), program.getEffectsStatus().getGainOn(), program.getGainToePatch());
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
         sb.append("    Knob:\n").append(printKnob(program.getKnob()));
         sb.append("    LFO 1:\n").append(printLfo(program.getLfo1()));
         sb.append("    LFO 2:\n").append(printLfo(program.getLfo2()));
         sb.append("    Random:\n").append(printRandom(program.getRandom()));
         sb.append("    A/B:\n").append(printAb(program.getAb()));
         sb.append("    Envelope:\n").append(printEnvelopeGenerator(program.getEnvelopeGenerator()));
         sb.append("  Mix:\n");
         sb.append("    Send:\n").append(printSendMix(program.getSendMix()));
         sb.append("    Post:\n");
         sb.append("  ").append(printParameter(program.getPostMix()));
         sb.append("  ").append(printParameter(program.getPostLevel()));
         sb.append("  ").append(printParameter(program.getPostBypassLevel()));
         if (program.getEffect1() != null) {
             sb.append("    FX1:\n");
             sb.append("  ").append(printParameter(program.getEffect1().getMix()));
             sb.append("  ").append(printParameter(program.getEffect1().getLevel()));
         }
         if (program.getEffect2() != null) {
             sb.append("    FX2:\n");
             sb.append("  ").append(printParameter(program.getEffect2().getMix()));
             sb.append("  ").append(printParameter(program.getEffect2().getLevel()));
         }
         if (program.getChorus() != null) {
             sb.append("    Chorus:\n");
             sb.append("  ").append(printParameter(program.getChorus().getMix()));
             sb.append("  ").append(printParameter(program.getChorus().getLevel()));
         }
         if (program.getDelay() != null) {
             sb.append("    Delay:\n");
             sb.append("  ").append(printParameter(program.getDelay().getMix()));
             sb.append("  ").append(printParameter(program.getDelay().getLevel()));
         }
         if (program.getReverb() != null) {
             sb.append("    Reverb:\n");
             sb.append("  ").append(printParameter(program.getReverb().getMix()));
             sb.append("  ").append(printParameter(program.getReverb().getLevel()));
         }
         if (program.getEq() != null) {
             sb.append("    Eq:\n");
             sb.append("  ").append(printParameter(program.getEq().getMix()));
             sb.append("  ").append(printParameter(program.getEq().getLevel()));
         }
         sb.append("  Tempo:\n");
         sb.append(printParameter(program.getTempo()));
         sb.append(printParameter(program.getTempoSource()));
         sb.append(printParameter(program.getBeatValue()));
         sb.append(printParameter(program.getTapAverage()));
         sb.append(printParameter(program.getTapSource()));
         sb.append("  Speaker Sim: ").append(program.isSpeakerSimulatorEnable().getDisplayString()).append("\n");
         sb.append(printParameter(program.getSpeakerSimulatorCabinet()));
         sb.append("  Noise Gate:\n").append(printNoiseGate(program.getNoiseGate()));
         return sb.toString().trim();
     }
 
     private static String trimDelimitedList(final StringBuilder sb) {
         String result;
         if (sb.length() > 0) {
             result = sb.substring(0, sb.length() - 2);
         } else {
             result = sb.toString();
         }
         return result;
     }
 
     static String printGuitarStyles(final Program program) {
         StringBuilder sb = new StringBuilder();
         if (program.isAcoustic()) {
             sb.append("Acoustic, ");
         }
         if (program.isBass()) {
             sb.append("Bass, ");
         }
         if (program.isBlues()) {
             sb.append("Blues, ");
         }
         if (program.isClean()) {
             sb.append("Clean, ");
         }
         if (program.isCountry()) {
             sb.append("Country, ");
         }
         if (program.isJazz()) {
             sb.append("Jazz, ");
         }
         if (program.isRock()) {
             sb.append("Rock, ");
         }
 
         return trimDelimitedList(sb);
     }
 
     static String printEffectTypes(final Program program) {
         StringBuilder sb = new StringBuilder();
         if (program.isChorus()) {
             sb.append("Chorus, ");
         }
         if (program.isDelay()) {
             sb.append("Delay, ");
         }
         if (program.isDistortion()) {
             sb.append("Distortion, ");
         }
         if (program.isEq()) {
             sb.append("EQ, ");
         }
         if (program.isFlanger()) {
             sb.append("Flanger, ");
         }
         if (program.isGain()) {
             sb.append("Gain, ");
         }
         if (program.isMod()) {
             sb.append("Mod, ");
         }
         if (program.isOverdrive()) {
             sb.append("Overdrive, ");
         }
         if (program.isPhaser()) {
             sb.append("Phaser, ");
         }
         if (program.isPitch()) {
             sb.append("Pitch, ");
         }
         if (program.isReverb()) {
             sb.append("Reverb, ");
         }
         if (program.isSpeakerSim()) {
             sb.append("Speaker Sim, ");
         }
         if (program.isWah()) {
             sb.append("Wah, ");
         }
 
         return trimDelimitedList(sb);
     }
 
     static String printApplicationTypes(final Program program) {
         StringBuilder sb = new StringBuilder();
         if (program.isPrePost()) {
             sb.append("Amp Input + FX Loop, ");
         }
         if (program.isStandAlone()) {
             sb.append("Stand alone, ");
         }
         if (program.isInline()) {
             sb.append("Amp Input Only, ");
         }
 
         return trimDelimitedList(sb);
     }
 
     static void printProgram(final StringBuilder sb, final String label, final EffectObject effect, final OnOffValue effectOn, final int effectToePatch) throws PrintException {
         if (effect != null) {
             sb.append("  ").append(label).append(": ");
             sb.append(effect.getName()).append(" (").append(effectOn.getDisplayString()).append(")").append("\n");
             sb.append("    Toe Switch: ").append(toePatchToString(effectToePatch)).append("\n");
             sb.append(AlgorithmPrinter.print(effect));
         }
     }
 
     static String printSoftRow(final Program program, final int i) throws PrintException {
         // TODO should be null if it's not used
         if (program.getSoftRowEffectType(i) == 255 || program.getSoftRowParameter(i) == 255) {
             return "";
         }
         DataObject effect = program.getEffect(program.getSoftRowEffectType(i));
         Parameter effectParameter = effect == null ? null : effect.getParameter(program.getSoftRowParameter(i));
         if (effectParameter == null) {
             return "";
         }
         StringBuilder sb = new StringBuilder();
         sb.append("    ").append(i + 1).append(": ");
         sb.append(effectTypeToString(program.getSoftRowEffectType(i))).append(" ");
         sb.append(effectParameter.getName()).append("\n");
         return sb.toString();
     }
 
     static String printPatch(final Program program, final Patch patch, final int patchNumber) throws PrintException {
         if (patch.getSourceIndex() == 0) {
             return "";
         }
         DataObject effect = program.getEffect(patch.getDestinationEffectIndex());
        Parameter parameter = effect == null ? null : effect.getParameter(patch.getDestinationParameter());
        String patchParameter = parameter.getName();
         StringBuilder sb = new StringBuilder();
         sb.append("    Patch ").append(patchNumber).append(":\n");
         sb.append("      Source: ").append(patch.getSourceName()).append("\n");
         sb.append("        Min: ").append(patch.getSourceMin()).append("\n");
         sb.append("        Mid: ").append(patch.getSourceMid() == null ? "--" : patch.getSourceMid()).append("\n");
         sb.append("        Max: ").append(patch.getSourceMax()).append("\n");
         String patchEffect = patch.getDestinationEffectName();
         sb.append("      Destination: ").append(patchEffect).append(" ").append(patchParameter).append("\n");
         sb.append("    ").append(printParameter(patch.getDestinationMin()));
         sb.append("    ").append(printParameter(patch.getDestinationMid()));
         sb.append("    ").append(printParameter(patch.getDestinationMax()));
         return sb.toString();
     }
 
     static String printKnob(final Knob knob) {
         final StringBuilder sb = new StringBuilder();
         sb.append("  ").append(printParameter(knob.getValue()));
         sb.append("  ").append(printParameter(knob.getLow()));
         sb.append("  ").append(printParameter(knob.getHigh()));
         sb.append("  ").append(printParameter(knob.getName()));
         return sb.toString();
     }
 
     static String printLfo(final Lfo lfo) throws PrintException {
         final StringBuilder sb = new StringBuilder();
         sb.append("  ").append(printParameter(lfo.getMode()));
         sb.append("  ").append(printParameter(lfo.getRate()));
         sb.append("  ").append(printParameter(lfo.getPulseWidth()));
         sb.append("  ").append(printParameter(lfo.getPhase()));
         sb.append("  ").append(printParameter(lfo.getDepth()));
         sb.append("  ").append(printParameter(lfo.getOnLevel()));
         sb.append("  ").append(printParameter(lfo.getOnSource()));
         return sb.toString();
     }
 
     static String printRandom(final Random random) throws PrintException {
         final StringBuilder sb = new StringBuilder();
         sb.append("  ").append(printParameter(random.getLow()));
         sb.append("  ").append(printParameter(random.getHigh()));
         sb.append("  ").append(printParameter(random.getRate()));
         return sb.toString();
     }
 
     static String printAb(final Ab ab) {
         final StringBuilder sb = new StringBuilder();
         sb.append("  ").append(printParameter(ab.getMode()));
         sb.append("  ").append(printParameter(ab.getARate()));
         sb.append("  ").append(printParameter(ab.getBRate()));
         sb.append("  ").append(printParameter(ab.getOnLevel()));
         sb.append("  ").append(printParameter(ab.getOnSource()));
         return sb.toString();
     }
 
     static String printEnvelopeGenerator(final EnvelopeGenerator envelopeGenerator) throws PrintException {
         final StringBuilder sb = new StringBuilder();
         sb.append("  ").append(printParameter(envelopeGenerator.getSrc1()));
         sb.append("  ").append(printParameter(envelopeGenerator.getSrc2()));
         sb.append("  ").append(printParameter(envelopeGenerator.getATrim()));
         sb.append("  ").append(printParameter(envelopeGenerator.getResponse()));
         return sb.toString();
     }
 
     static String printSendMix(final SendMix sendMix) {
         final StringBuilder sb = new StringBuilder();
         sb.append("  ").append(printParameter(sendMix.getSendLevel()));
         sb.append("  ").append(printParameter(sendMix.getSendBypassLevel()));
         return sb.toString();
     }
 
     static String printNoiseGate(final NoiseGate noiseGate) {
         final StringBuilder sb = new StringBuilder();
         sb.append(printParameter(noiseGate.getEnable()));
         sb.append(printParameter(noiseGate.getSend()));
         sb.append(printParameter(noiseGate.getThreshold()));
         sb.append(printParameter(noiseGate.getAttenuation()));
         sb.append(printParameter(noiseGate.getOffset()));
         sb.append(printParameter(noiseGate.getATime()));
         sb.append(printParameter(noiseGate.getHTime()));
         sb.append(printParameter(noiseGate.getRTime()));
         sb.append(printParameter(noiseGate.getDelay()));
         return sb.toString();
     }
 
     private static String toePatchToString(final int toePatch) throws PrintException {
         return TOE_PATCH_STATES[toePatch];
     }
 
     private static String effectTypeToString(final int effectType) {
         return EFFECT_TYPES[effectType];
     }
 
 }
