 package net.perkowitz.waves;
 
 import com.google.common.collect.Lists;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 
 public class Process {
 
     private static final String PROCESS_KEY = "process";
     private static final String INPUTS_KEY = "inputs";
     private static final String PARAMETERS_KEY = "parameters";
 
     private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
 
     private static int cycleLength;
     private static List<Wave> argWaves;
 
     public static void main(String args[]) throws IOException {
 
         if (args.length < 2) {
             System.out.println("Usage: Process <process file> <note> <out file> [infile] [infile] ...");
             return;
         }
 
         String processFile = args[0];
         String note = args[1];
         String outFile = args[2] + "_" + note;
         argWaves = Lists.newArrayList();
         for (int i=3; i<args.length; i++) {
             System.out.println("Loading Wave from " + args[i]);
             argWaves.add(Wave.load(args[i]));
         }
 
         cycleLength = Wave.noteToCycleLength(note);
 
         Wave wave = parseFromFile(processFile);
 
         wave.save(outFile + ".snc");
 
         StdAudio.save(outFile + ".wav",wave.toDoubleArray());
 
     }
 
 
     public static Wave parseFromFile(String filename) {
 
         try {
             BufferedReader in = new BufferedReader(new FileReader(filename));
             String line;
             String json = "";
             while ((line = in.readLine()) != null)   {
                 json += line.trim() + " ";
             }
             in.close();
 
             JsonNode node = OBJECT_MAPPER.readTree(json);
             return parse(node);
 
         } catch (IOException e) {
             System.err.println("Error reading from " + filename);
             return null;
         }
     }
 
     public static Wave parse(JsonNode node) {
 
         System.out.println("Parsing " + node);
         String process = node.path(PROCESS_KEY).getTextValue().toLowerCase();
 
         List<String> parameters = Lists.newArrayList();
         Iterator parameterNodes = node.path(PARAMETERS_KEY).getElements();
         while (parameterNodes.hasNext()) {
             parameters.add(((JsonNode)parameterNodes.next()).getTextValue());
         }
 
         List<Wave> inputWaves = Lists.newArrayList();
         Iterator<JsonNode> inputNodes = node.path(INPUTS_KEY).getElements();
         while (inputNodes.hasNext()) {
             JsonNode thisNode = inputNodes.next();
             if (thisNode.isTextual()) {
                 String nodeText = thisNode.asText();
                 if (nodeText.startsWith("input")) {
                     Integer inputIndex = new Integer(nodeText.substring(5));
                     System.out.println("Loading arg Wave #" + inputIndex);
                     inputWaves.add(argWaves.get(inputIndex));
                 }
             } else {
                 inputWaves.add(parse(thisNode));
             }
         }
 
         System.out.println("Finished parsing " + node);
         return apply(process,inputWaves,parameters);
     }
 
     public static Wave apply(String process, List<Wave> inputs, List<String> parameters) {
 
         // sound generation
         if (process.equals("saw")) {
             return Wave.saw(cycleLength);
         } else if (process.equals("square")) {
             return Wave.square(cycleLength);
         } else if (process.equals("pulse")) {
             return Wave.pulse(cycleLength,new Double(parameters.get(0)));
         } else if (process.equals("sine")) {
             return Wave.sine(cycleLength);
         } else if (process.equals("random")) {
             return Wave.random(cycleLength);
         } else if (process.equals("silence")) {
             return Wave.silence(cycleLength);
         } else if (process.equals("file")) {
             return Wave.load(parameters.get(0));
 
             // adjust wave within headroom
         } else if (process.equals("normalize")) {
             Wave wave = inputs.get(0);
             wave.normalize();
             return wave;
         } else if (process.equals("clip")) {
             Wave wave = inputs.get(0);
             wave.clip();
             return wave;
         } else if (process.equals("wrap")) {
             Wave wave = inputs.get(0);
             wave.wrap();
             return wave;
 
         // modify single wave
         } else if (process.equals("pad")) {
             Wave wave = inputs.get(0);
             wave.pad(new Long(parameters.get(0)));
             return wave;
         } else if (process.equals("downsample")) {
             Wave wave = inputs.get(0);
             wave.downsample(new Double(parameters.get(0)));
             return wave;
         } else if (process.equals("sync")) {
             Wave wave = inputs.get(0);
             Integer syncCycleLength = Wave.note2Samples(new Integer(parameters.get(0)),Wave.DEFAULT_SAMPLE_RATE);
             wave.sync(syncCycleLength);
             return wave;
         } else if (process.equals("lpf")) {
             Wave wave = inputs.get(0);
             wave.lowpass(new Integer(parameters.get(0)));
             return wave;
         } else if (process.equals("hpf")) {
             Wave wave = inputs.get(0);
             wave.highpass(new Integer(parameters.get(0)));
             return wave;
 
         // combine two waves
         } else if (process.equals("add")) {
             Wave wave1 = inputs.get(0);
             Wave wave2 = inputs.get(1);
             wave1.add(wave2);
             return wave1;
         } else if (process.equals("subtract")) {
             Wave wave1 = inputs.get(0);
             Wave wave2 = inputs.get(1);
            wave1.subtract(wave2);
             return wave1;
         } else if (process.equals("multiply")) {
             Wave wave1 = inputs.get(0);
             Wave wave2 = inputs.get(1);
            wave1.multiply(wave2);
             return wave1;
         } else if (process.equals("append")) {
             Wave wave1 = inputs.get(0);
             Wave wave2 = inputs.get(1);
             wave1.append(wave2);
             return wave1;
         } else if (process.equals("average")) {
             Wave wave1 = inputs.get(0);
             Wave wave2 = inputs.get(1);
             return Wave.average(wave1,wave2,new Double(parameters.get(0)));
         } else if (process.equals("morph")) {
             Wave wave1 = inputs.get(0);
             Wave wave2 = inputs.get(1);
             return Wave.morph(wave1,wave2,new Integer(parameters.get(0)),cycleLength);
         } else if (process.equals("morphloop")) {
             Wave wave1 = inputs.get(0);
             Wave wave2 = inputs.get(1);
             int length = (new Integer(parameters.get(0)))/2;
             Wave morph1 = Wave.morph(wave1,wave2,length,cycleLength);
             Wave morph2 = Wave.morph(wave2,wave1,length,cycleLength);
             morph1.append(morph2);
             return morph1;
         }
 
         System.out.println("Process not found: " + process);
 
         return null;
     }
 
 }
