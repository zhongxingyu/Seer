 /**
  * 
  */
 
 package uk.ac.cam.eng.rulebuilding.retrieval;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.io.hfile.HFile;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.DataInputBuffer;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.Writable;
 
 import uk.ac.cam.eng.extraction.hadoop.datatypes.PairWritable3;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.PairWritable3ArrayWritable;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.RuleWritable;
 
 /**
  * @author jmp84 This class takes the outputs of a source-to-target and a
  *         target-to-source job and converts them to an HFile
  */
 public class HFileLoader2 extends Configured {
 
     private static byte[] long2ByteArray(long l) {
         byte b[] = new byte[8];
         ByteBuffer buf = ByteBuffer.wrap(b);
         buf.putLong(l);
         return b;
     }
 
     private static byte[] object2ByteArray(Writable obj) throws IOException {
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(buffer);
         obj.write(out);
         return buffer.toByteArray();
     }
 
     private static RuleWritable convertValueBytes(byte[] bytes) {
         DataInputBuffer in = new DataInputBuffer();
         // in.reset(bytes.array(), bytes.arrayOffset(), bytes.limit());
         in.reset(bytes, bytes.length);
         RuleWritable value = new RuleWritable();
         try {
             value.readFields(in);
         }
         catch (IOException e) {
             // Byte buffer is memory backed so no exception is possible. Just in
             // case chain it to a runtime exception
             throw new RuntimeException(e);
         }
         return value;
     }
 
     private static ArrayWritable merge(ArrayWritable targetsAndFeatures,
             ArrayWritable targetsAndFeatures2) {
         ArrayWritable res = new ArrayWritable(PairWritable3.class);
         PairWritable3[] targetsAndFeaturesArray =
                 (PairWritable3[]) targetsAndFeatures.get();
         PairWritable3[] targetsAndFeatures2Array =
                 (PairWritable3[]) targetsAndFeatures2.get();
         // TODO add unit tests everywhere
         // TODO logging
         if (targetsAndFeaturesArray.length != targetsAndFeatures2Array.length) {
             System.err.println("Error: the source-to-target and " +
                     "target-to-source jobs should have produced the same " +
                     "number of targets per source: "
                     + targetsAndFeaturesArray.length + " "
                     + targetsAndFeatures2Array.length);
             System.exit(1);
         }
         PairWritable3[] resArray =
                 new PairWritable3[targetsAndFeaturesArray.length];
         for (int i = 0; i < targetsAndFeaturesArray.length; i++) {
             ArrayWritable mergedFeatures =
                     new ArrayWritable(DoubleWritable.class);
             if (!targetsAndFeaturesArray[i].first
                     .equals(targetsAndFeatures2Array[i].first)) {
                 System.err.println("Error: the source-to-target and " +
                         "target-to-source jobs should have produced the same " +
                         "set of targets per source: "
                         + targetsAndFeaturesArray[i].first + " "
                         + targetsAndFeatures2Array[i].first);
                 System.exit(1);
             }
             // DoubleWritable[] features =
             // (DoubleWritable[]) targetsAndFeaturesArray[i].second.get();
             Writable[] features = targetsAndFeaturesArray[i].second.get();
             // DoubleWritable[] features2 =
             // (DoubleWritable[]) targetsAndFeatures2Array[i].second.get();
             Writable[] features2 = targetsAndFeatures2Array[i].second.get();
             if (features.length != features2.length) {
                 System.err.println("Error: the source-to-target and " +
                         "target-to-source jobs should have produced the same " +
                         "number of features: " + features.length + " "
                         + features2.length);
                 System.exit(1);
             }
             DoubleWritable[] mergedFeaturesArray =
                     new DoubleWritable[features.length];
             for (int j = 0; j < features.length; j++) {
                 // for j=0,1 we add the probability
                 // for j>=2, we don't (otherwise the occurrence is doubled for
                 // example)
                 if (j < 2) {
                     mergedFeaturesArray[j] =
                             new DoubleWritable(
                                     ((DoubleWritable) features[j]).get()
                                             + ((DoubleWritable) features2[j])
                                                     .get());
                 }
                 else {
                     mergedFeaturesArray[j] =
                             new DoubleWritable(
                                     ((DoubleWritable) features[j]).get());
                 }
             }
             mergedFeatures.set(mergedFeaturesArray);
             resArray[i] =
                     new PairWritable3(targetsAndFeaturesArray[i].first,
                             mergedFeatures);
         }
         res.set(resArray);
         return res;
     }
 
     protected static void hdfs2HFile(String source2TargetInput,
            String target2SourceInput, String hFile)
            throws IOException {
         // TODO replace this with a logger
         System.out.println("Reading " + source2TargetInput + " "
                 + target2SourceInput
                 + " and writing hfile to "
                 + hFile);
         Configuration conf = new Configuration();
         FileSystem fs = FileSystem.get(conf);
         SequenceFile.Reader sequenceReader = new SequenceFile.Reader(fs,
                 new Path(source2TargetInput), conf);
         SequenceFile.Reader sequenceReader2 = new SequenceFile.Reader(fs,
                 new Path(target2SourceInput), conf);
         Path path = new Path(hFile);
         if (fs.exists(path)) {
             System.out.println("Error: " + hFile + " already exists");
             System.exit(1);
         }
         HFile.Writer writer = new HFile.Writer(fs, path, 16 * 1024, "gz", null);
         // TODO fix this ugly type problem of ArrayWritable vs
         // PairWritable3ArrayWritable
         BytesWritable key = new BytesWritable();
         PairWritable3ArrayWritable value = new PairWritable3ArrayWritable();
         BytesWritable key2 = new BytesWritable();
         PairWritable3ArrayWritable value2 = new PairWritable3ArrayWritable();
         // new ArrayWritable(PairWritable3.class);
         while (sequenceReader.next(key, value)) {
             if (!sequenceReader2.next(key2, value2)) {
                 System.err.println("Error: the source-to-target job and the " +
                         "target-to-source job should have produced the same " +
                         "number of records: " + source2TargetInput + " "
                         + target2SourceInput);
                 System.exit(1);
             }
             if (!key.equals(key2)) {
                 System.err.println("Error: the source-to-target job and the " +
                         "target-to-source job should have produced " +
                         "the same sources as keys in the same order: "
                         + key.toString() + "\n" + key2.toString());
                 System.exit(1);
             }
             ArrayWritable mergedValue = merge(value, value2);
             byte[] valueBytes = object2ByteArray(mergedValue);
             writer.append(key.getBytes().clone(), 0, key.getLength(),
                     valueBytes, 0, valueBytes.length);
         }
         writer.close();
     }
 
     /**
     * @param args
      */
     public static void main(String[] args) throws IOException {
         if (args.length != 3) {
             System.out.println("Usage: HFileLoader source2targetInput " +
                     "target2sourceInput hFile");
             System.exit(1);
         }
         hdfs2HFile(args[0], args[1], args[2]);
     }
 
 }
