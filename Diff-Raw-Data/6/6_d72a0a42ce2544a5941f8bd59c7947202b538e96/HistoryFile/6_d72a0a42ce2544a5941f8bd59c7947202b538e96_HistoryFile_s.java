 package seclogin;
 
 import com.google.common.hash.HashFunction;
 import com.google.common.hash.Hashing;
 import com.google.common.io.ByteStreams;
 import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
 import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
 
 import java.io.*;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 import static seclogin.Feature.ALPHA;
 import static seclogin.Feature.BETA;
 
 /* A history file for a particular user. */
 public class HistoryFile {
 
     private static final HashFunction USER_HASH_FN = Hashing.sha256();
 
     private final HistoryFileParams historyFileParams;
     private final byte[] userHash;
     private final int numMeasurements;
     private final double[][] measurements;
 
     private HistoryFile(HistoryFileParams historyFileParams, byte[] userHash, int numMeasurements, double[][] measurements) {
         this.historyFileParams = historyFileParams;
         this.userHash = userHash;
         this.numMeasurements = numMeasurements;
         this.measurements = measurements;
     }
 
     public static HistoryFile emptyHistoryFile(String user, HistoryFileParams params) {
         byte[] userHash = USER_HASH_FN.hashString(user).asBytes();
         return new HistoryFile(params, userHash, 0,
             new double[params.maxNrOfEntries()][params.nrOfFeatures()]);
     }
 
     public Encrypted encrypt(BigInteger hpwd) {
         return new Encrypted(asEncryptedByteArray(hpwd));
     }
 
     public static Encrypted read(InputStream inputStream) throws IOException {
         BufferedInputStream in = new BufferedInputStream(inputStream);
         byte[] ciphertext;
         try {
             ciphertext = ByteStreams.toByteArray(in);
         } finally {
             in.close();
         }
         return new Encrypted(ciphertext);
     }
 
     private static HistoryFile fromEncryptedByteArray(byte[] ciphertext, BigInteger hpwd, HistoryFileParams params)
             throws IndecipherableHistoryFileException {
         byte[] plaintext;
         try {
             plaintext = Crypto.aes128Decrypt(hpwd, ciphertext);
         } catch (Exception e) {
             throw new IndecipherableHistoryFileException(e);
         }
         return fromByteArray(plaintext, params);
     }
 
     private byte[] asEncryptedByteArray(BigInteger hpwd) {
         byte[] plaintext = asByteArray();
         return Crypto.aes128Encrypt(hpwd, plaintext);
     }
 
     byte[] asByteArray() {
         byte[] plaintext = new byte[sizeInBytes()];
 
         int offset = 0;
 
         System.arraycopy(userHash, 0, plaintext, offset, userHash.length);
         offset += userHash.length;
 
         ByteBuffer.wrap(plaintext, offset, Integer.SIZE/Byte.SIZE).putInt(numMeasurements);
         offset += Integer.SIZE/Byte.SIZE;
 
         checkState(measurements.length == historyFileParams.maxNrOfEntries());
         for (int j = 0; j < measurements.length; j++) {
             for (int i = 0; i < measurements[j].length; i++) {
                 ByteBuffer.wrap(plaintext, offset + doubleByteOffset(j, i, historyFileParams), DOUBLE_SIZE_IN_BYTES).putDouble(measurements[j][i]);
             }
         }
 
         return plaintext;
     }
 
     private int sizeInBytes() {
         return (USER_HASH_FN.bits() / Byte.SIZE) +
                 (Integer.SIZE/Byte.SIZE) +
                 (historyFileParams.maxNrOfEntries() * historyFileParams.nrOfFeatures() * DOUBLE_SIZE_IN_BYTES);
     }
 
     private static int doubleByteOffset(int j, int i, HistoryFileParams historyFileParams) {
         return (DOUBLE_SIZE_IN_BYTES * ((j* historyFileParams.nrOfFeatures())+i));
     }
 
     private static final int DOUBLE_SIZE_IN_BYTES = Double.SIZE / Byte.SIZE;
 
     static HistoryFile fromByteArray(byte[] plaintext, HistoryFileParams params) {
         int offset = 0;
 
         byte[] userHash = new byte[USER_HASH_FN.bits() / Byte.SIZE];
         System.arraycopy(plaintext, offset, userHash, 0, userHash.length);
         offset += userHash.length;
 
         int numMeasurements = ByteBuffer.wrap(plaintext, offset, Integer.SIZE/Byte.SIZE).getInt();
         offset += Integer.SIZE/Byte.SIZE;
 
         double[][] measurements = new double[params.maxNrOfEntries()][params.nrOfFeatures()];
         for (int j = 0; j < measurements.length; j++) {
             for (int i = 0; i < measurements[j].length; i++) {
                 measurements[j][i] = ByteBuffer.wrap(plaintext, offset + doubleByteOffset(j, i, params), Double.SIZE/Byte.SIZE).getDouble();
             }
         }
 
         return new HistoryFile(params, userHash, numMeasurements, measurements);
     }
 
     public boolean userHashEquals(String user) {
         return Arrays.equals(userHash, USER_HASH_FN.hashString(user).asBytes());
     }
 
     public HistoryFile withMostRecentMeasurements(double[] mostRecentMeasurements) {
         checkArgument(mostRecentMeasurements.length == historyFileParams.nrOfFeatures());
         double[][] shiftedMeasurements = new double[measurements.length][];
         shiftedMeasurements[0] = mostRecentMeasurements;
         System.arraycopy(measurements, 0, shiftedMeasurements, 1, shiftedMeasurements.length - 1);
         return new HistoryFile(historyFileParams, userHash, Math.min(historyFileParams.maxNrOfEntries(), numMeasurements + 1), shiftedMeasurements);
     }
 
 
     public List<Feature> deriveFeatures(List<MeasurementParams> params) {
         checkArgument(params.size() == this.historyFileParams.nrOfFeatures());
 
         SummaryStatistics[] stats = calculateStats();
         List<Feature> features = new ArrayList<Feature>();
         for (int i = 0; i < this.historyFileParams.nrOfFeatures(); i++) {
             StatisticalSummary userStats = stats[i];
             double mu = userStats.getMean();
             double sigma = userStats.getStandardDeviation();
             double t = params.get(i).t();
             double k = params.get(i).k();
            if (numMeasurements < measurements.length || Math.abs(mu - t) > (k * sigma)) {
                features.add(mu < t ? ALPHA : BETA);
            }
         }
         return features;
     }
 
     private SummaryStatistics[] calculateStats() {
         SummaryStatistics[] stats = new SummaryStatistics[historyFileParams.nrOfFeatures()];
         for (int i = 0; i < stats.length; i++) {
             stats[i] = new SummaryStatistics();
             for (double[] measurement : measurements) {
                 stats[i].addValue(measurement[i]);
             }
         }
         return stats;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         HistoryFile that = (HistoryFile) o;
 
         if (!Arrays.equals(userHash, that.userHash)) return false;
         if (!Arrays.deepEquals(measurements, that.measurements)) return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = Arrays.hashCode(userHash);
         result = 31 * result + Arrays.deepHashCode(measurements);
         return result;
     }
 
     @Override
     public String toString() {
         return "HistoryFile{" +
                 "userHash=" + Arrays.toString(userHash) +
                 ", measurements=" + (measurements == null ? null : Arrays.deepToString(measurements)) +
                 '}';
     }
 
     /** An encrypted history file. */
     public static class Encrypted {
         private final byte[] ciphertext;
 
         private Encrypted(byte[] ciphertext) {
             this.ciphertext = ciphertext;
         }
 
         public HistoryFile decrypt(BigInteger hpwd, HistoryFileParams params) throws IndecipherableHistoryFileException {
             return HistoryFile.fromEncryptedByteArray(ciphertext, hpwd, params);
         }
 
         public void write(OutputStream outputStream) throws IOException {
             BufferedOutputStream out = new BufferedOutputStream(outputStream);
             out.write(ciphertext);
             out.flush();
             out.close();
         }
     }
 }
