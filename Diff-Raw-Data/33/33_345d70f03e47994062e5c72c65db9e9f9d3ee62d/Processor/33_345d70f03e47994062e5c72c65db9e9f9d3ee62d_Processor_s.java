 /*
  * Processor.java
  *
  * This is the class where you can implement your onset detection / tempo extraction methods
  * Of course you may also define additional classes.
  *
  *
  * most of the detection functions are taken from http://www.dafx.ca/proceedings/papers/p_133.pdf
  *
  *
  * also interesting, but nothing implemented: http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.60.9607&rep=rep1&type=pdf
  *
  * http://java-gaming.org/index.php?topic=24191.0
  */
 package at.cp.jku.teaching.amprocessing;
 
 import java.util.LinkedList;
 import java.util.List;
 import static java.lang.Math.*;
 
 /**
  *
  * @author andreas arzt
  */
 public class Processor {
 
     private String m_filename;
     AudioFile m_audiofile;
     // this List should contain your results of the onset detection step (onset times in seconds)
     private LinkedList<Double> m_onsetList;
     // this may contain your intermediate results (in frames, before conversion to time in seconds)
     private LinkedList<Integer> m_onsetListFrames;
     // this variable should contain your result of the tempo estimation algorithm
     private double m_tempo;
     double[] onsetDetectionFunction;
     private int algorithm;
     private Integer m = null;
     private Integer w = null;
     private Double alpha = null;
     private Double delta = null;
 
     public Processor(String filename) {
         Log.log("Initializing Processor...");
         m_filename = filename;
         m_onsetList = new LinkedList<Double>();
         m_onsetListFrames = new LinkedList<Integer>();
 
         Log.log("Reading Audio-File " + filename);
         Log.log("Performing FFT...");
         // an AudioFile object is created with the following Paramters: AudioFile(WAVFILENAME, FFTLENGTH in seconds, HOPLENGTH in seconds)
         // if you would like to work with multiple resolutions you simple create multiple AudioFile objects with different parameters
         // given an audio file with 44.100 Hz the parameters below translate to an FFT with size 2048 points
         // Note that the value is not taken to be precise; it is adjusted so that the FFT Size is always power of 2.
 
         m_audiofile = new AudioFile(m_filename, 0.02322, 0.005);
 
         //m_audiofile = new AudioFile(m_filename, 0.002322, 0.005);
         // this starts the extraction of the basis features (the STFT)
         m_audiofile.processFile();
     }
 
     // This method is called from the Runner and is the starting point of your onset detection / tempo extraction code
     public void analyze() {
         Log.log("Running Analysis...");
 
 
         final int numSamples = m_audiofile.spectralDataContainer.size();
         onsetDetectionFunction = new double[numSamples];
         m_onsetList.clear();
 
         List<Integer> peaks;
         switch (algorithm) {
             case 1:
                 peaks = analyze_phase_deviation();
                 break;
             case 2:
                 peaks = analyze_spectral_flux();
                 break;
             case 3:
                 peaks = analyze_complex_domain();
                 break;
             case 4:
                 peaks = analyze_weighted_phase_deviation();
                 break;
             case 5:
                 peaks = analyze_normalized_weighted_phase_deviation();
                 break;
             case 6:
                 peaks = analyze_rectified_complex_domain();
                 break;
             case 7:
                 peaks = analyze_frame_distance();
                 break;
             default:
                 peaks = null;
         }
 
 
         for (int p : peaks) {
             m_onsetList.add(p * m_audiofile.hopTime);
         }
     }
 
     // alg 1
     private List<Integer> analyze_phase_deviation() {
         // TODO: this implementation is faulty, i think (unwrappedPhases ok?)
         final int numSamples = m_audiofile.spectralDataContainer.size();
 
         for (int n = 0; n < numSamples; n++) {
             final SpectralData currentFrame = m_audiofile.spectralDataContainer.get(n);
             final double[] phi = currentFrame.unwrappedPhases;
 
             double dphi_acc = 0.0;
             for (int k = 2; k < currentFrame.size; k++) {
                 double dphi = phi[k] + phi[k - 2] - 2 * phi[k - 1];
                 dphi_acc += abs(dphi);
             }
             onsetDetectionFunction[n] = dphi_acc / currentFrame.size;
         }
 
         return pickPeaksDixon(onsetDetectionFunction, 0, 0, 0, 0);
     }
 
     // alg 2
     private List<Integer> analyze_spectral_flux() {
 
 
         final int numSamples = m_audiofile.spectralDataContainer.size();
 
         for (int n = 0; n < numSamples; n++) {
             final SpectralData currentFrame = m_audiofile.spectralDataContainer.get(n);
             double acc = 0.0;
             for (int k = 1; k < currentFrame.size; k++) {
                 acc += halfRect(abs(currentFrame.magnitudes[k]) - abs(currentFrame.magnitudes[k - 1]));
             }
             onsetDetectionFunction[n] = acc;
         }
 
         return pickPeaksDixon(onsetDetectionFunction, 3, 4, 0.85, 0.45);
     }
 
     // alg 3
     private List<Integer> analyze_complex_domain() {
         // from dixon, implementation: confident
         final int numSamples = m_audiofile.spectralDataContainer.size();
 
         for (int n = 0; n < numSamples; n++) {
             final SpectralData currentFrame = m_audiofile.spectralDataContainer.get(n);
             final double[] phi = currentFrame.unwrappedPhases;
 
             double deviation_acc = 0.0;
             for (int k = 2; k < currentFrame.size; k++) {
 
                 double phi_t = phi[k - 1] + (phi[k - 1] - phi[k - 2]);
                 double mag_t = currentFrame.magnitudes[k - 1];
 
                 deviation_acc += radialDistance(currentFrame.magnitudes[k], phi[k], mag_t, phi_t);
             }
             onsetDetectionFunction[n] = deviation_acc;
         }
 
         return pickPeaksDixon(onsetDetectionFunction, 3, 4, 0.8, 0.4);
     }
     // alg 4
 
     private List<Integer> analyze_weighted_phase_deviation() {
         final int numSamples = m_audiofile.spectralDataContainer.size();
 
         for (int n = 0; n < numSamples; n++) {
             final SpectralData currentFrame = m_audiofile.spectralDataContainer.get(n);
             final double[] phi = currentFrame.unwrappedPhases;
 
             double dphi_acc = 0.0;
             for (int k = 2; k < currentFrame.size; k++) {
                 double dphi = phi[k] + phi[k - 2] - 2 * phi[k - 1];
                 dphi_acc += abs(currentFrame.magnitudes[k] * dphi);
             }
             onsetDetectionFunction[n] = dphi_acc / currentFrame.size;
         }
 
         return pickPeaksDixon(onsetDetectionFunction, 4, 5, 0.9, 0.2);
     }
     // alg 5
 
     private List<Integer> analyze_normalized_weighted_phase_deviation() {
         final int numSamples = m_audiofile.spectralDataContainer.size();
 
         for (int n = 0; n < numSamples; n++) {
             final SpectralData currentFrame = m_audiofile.spectralDataContainer.get(n);
             final double[] phi = currentFrame.unwrappedPhases;
 
             double dphi_acc = 0.0;
             double mag_acc = 0.0;
             for (int k = 2; k < currentFrame.size; k++) {
                 double dphi2 = phi[k] + phi[k - 2] - 2 * phi[k - 1];
                 double X_n_k = currentFrame.magnitudes[k];
                 dphi_acc += abs(X_n_k * dphi2);
                 mag_acc += abs(X_n_k);
             }
             onsetDetectionFunction[n] = dphi_acc / mag_acc;
         }
 
        return pickPeaksDixon(onsetDetectionFunction, 5, 4, 0.9, 0);
     }
 
     // alg 6
     private List<Integer> analyze_rectified_complex_domain() {
         // from dixon, implementation: confident
         final int numSamples = m_audiofile.spectralDataContainer.size();
 
         for (int n = 0; n < numSamples; n++) {
             final SpectralData currentFrame = m_audiofile.spectralDataContainer.get(n);
             final double[] phi = currentFrame.unwrappedPhases;
 
             double deviation_acc = 0.0;
             for (int k = 2; k < currentFrame.size; k++) {
 
                 double phi_t = phi[k - 1] + (phi[k - 1] - phi[k - 2]);
                 double mag_t = currentFrame.magnitudes[k - 1];
 
                 if (currentFrame.magnitudes[k] >= currentFrame.magnitudes[k - 1]) {
                     deviation_acc += radialDistance(currentFrame.magnitudes[k], phi[k], mag_t, phi_t);
                 }
             }
             onsetDetectionFunction[n] = deviation_acc;
         }
 
         return pickPeaksDixon(onsetDetectionFunction, 3, 4, 0.9, 0.6);
     }
 
     // alg 7
     private List<Integer> analyze_frame_distance() {
 
         final int numSamples = m_audiofile.spectralDataContainer.size();
 
         double maxshift = Double.MIN_VALUE;
 
         for (int n = 1; n < numSamples; n++) {
 
             SpectralData currentFrame = m_audiofile.spectralDataContainer.get(n);
             SpectralData lastFrame = m_audiofile.spectralDataContainer.get(n - 1);
 
             double sum = 0.0;
             for (int k = 0; k < currentFrame.size; k++) {
                 // i assume currentFrame.size == lastFrame.size
 
                 double diff = radialDistance(currentFrame.magnitudes[k], currentFrame.unwrappedPhases[k], lastFrame.magnitudes[k], lastFrame.unwrappedPhases[k]);
                 sum += abs(diff);
             }
 
             sum /= currentFrame.size;
 
 
             if (sum > maxshift) {
                 maxshift = sum;
             }
 
             onsetDetectionFunction[n] = sum;
         }
 
         return pickPeaksDixon(onsetDetectionFunction, 3, 4, 0.5, 0.3);
     }
 
     private Integer[] pickPeaksSimple(double[] data, double threshold) {
         List<Integer> peaks = new LinkedList<Integer>();
 
         // simple peak picking
         for (int n = 1; n < data.length - 1; n++) {
 
             // possible peak
             if (data[n] > threshold && data[n - 1] < data[n] && data[n] > data[n + 1]) {
 
                 // TODO: find a way to remove the constant
                 // either filter the fftshift (matlab: filtfilt)
                 // or calculate something reasonable from hopTime
 
                 peaks.add(n);
 
             }
         }
         return peaks.toArray(new Integer[peaks.size()]);
     }
 
     private List<Integer> pickPeaksDixon(double[] data, int m, int w, double delta, double alpha) {
 
         // this is needed for the parameter study
         // if the member "delta" is set, override the given parameter
         if (this.delta != null) {
             delta = this.delta;
         }
         if (this.alpha != null) {
             alpha = this.alpha;
         }
         if (this.w != null) {
             w = this.w;
         }
         if (this.m != null) {
             m = this.m;
         }
 
         double mean = mean(data);
         double stddev = stddev(data, mean);
         for (int n = 0; n < data.length; n++) {
             data[n] = (data[n] - mean) / stddev;
         }
 
         double ga;
         double ga_next = Double.MIN_VALUE;
 
         List<Integer> peaks = new LinkedList<Integer>();
 
         // simple peak picking
         outer:
         for (int n = w * m; n < data.length - (w + 1); n++) {
 
             if (alpha != Double.NaN) {
 
                 ga = ga_next;
                 ga_next = max(data[n], alpha * ga + (1.0 - alpha) * data[n]);
 
                 if (data[n] < ga) {
                     continue outer;
                 }
             }
 
             inner:
             for (int k = n - w; k <= n + w; k++) {
                 // TODO: the case k <0 should be handled by the outer loop indizes
                 // the error occurs when m = 0;
                 if (k >= 0 && k != n) {
                     if (data[n] < data[k]) {
                         continue outer;
                     }
                 }
             }
 
             if (delta != Double.NaN) {
 
                 double sum1 = 0.0;
                 for (int k = n - m * w; k <= n + w; k++) {
                     sum1 += data[k];
                 }
 
                 sum1 = delta + (sum1 / (m * w + w + 1));
 
                 if (data[n] < sum1) {
                     continue outer;
                 }
 
             }
 
             peaks.add(n);
         }
         return peaks;
     }
 
     private double mean(double[] d) {
         double sum = 0.0;
         for (double e : d) {
             sum += e;
         }
         return sum / d.length;
     }
 
     private double stddev(double[] d, double mean) {
         double sum = 0.0;
         for (double e : d) {
             sum += pow(e - mean, 2);
         }
         return sqrt(sum / d.length);
     }
 
     private double halfRect(double d) {
         return (d + abs(d)) / 2;
     }
 
     private double radialDistance(double m1, double phi1, double m2, double phi2) {
 
         /*
          * http://en.wikipedia.org/wiki/Radial_distance_(geometry)
          *
          */
 
         return sqrt(m1 * m1 + m2 * m2 - 2 * m1 * m2 * cos(phi1 - phi2));
     }
 
    public void setup(Integer algorithm, Integer w, Integer m, Double delta, Double alpha) {
        if (algorithm != null) {
            this.algorithm = algorithm;
        }
        this.w = w;
        this.m = m;
        this.delta = delta;
        this.alpha = alpha;
    }

     public LinkedList<Double> getOnsets() {
         return m_onsetList;
     }
 
     public double getTempo() {
         return m_tempo;
     }
 }
