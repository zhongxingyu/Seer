 package ca.mrb0.hydrocitee.it;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 /**
  * TODO: Find a much better (more efficient) way to store/access sample data
  * 
  * @author mrb
  *
  */
 public class ITSampleData {
     private static Logger l = Logger.getLogger(ITSampleData.class);
     
     public enum CompressionType {
         Uncompressed,
         IT214,
         IT215
     };
     
     public final List<Integer> sampleData;
     
     public ITSampleData(List<Integer> sampleData) {
         this.sampleData = ImmutableList.copyOf(sampleData);
     }
     
     public ITSampleData() {
         this(ImmutableList.<Integer>of());
     }
     
     public static ITSampleData newFromData(byte[] data, int offs, int dataLengthSamples, boolean is16Bit, CompressionType compressionType) {
         
         final List<Integer> outData;
         
         if (compressionType != CompressionType.Uncompressed) {
             outData = ITSampleDataDecompressor.decompressSample(data, offs, dataLengthSamples, is16Bit, compressionType);
         } else {
             outData = Lists.newArrayListWithCapacity(dataLengthSamples);
             for(int i = 0; i < dataLengthSamples; i++) {
                 if (is16Bit) {
                    outData.set(i, ((0xff & data[offs + 2 * i + 1]) << 8) | (0xff & data[offs + 2 * i])); 
                 } else {
                    outData.set(i, 0xff & data[offs + i]);
                 }
             }
         }
         
         return new ITSampleData(outData);
     }
 }
