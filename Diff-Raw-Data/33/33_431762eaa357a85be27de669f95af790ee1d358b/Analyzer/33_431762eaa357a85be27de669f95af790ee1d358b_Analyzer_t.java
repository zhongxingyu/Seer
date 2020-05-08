 package ru.spbau.bioinf.tagfinder;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import ru.spbau.bioinf.tagfinder.util.XmlUtil;
 import ru.spbau.bioinf.tagfinder.view.Table;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class Analyzer {
     private  Configuration conf;
 
     public Analyzer(Configuration conf) {
         this.conf = conf;
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         Analyzer analyzer = new Analyzer(conf);
         Map<Integer, Scan> scans = conf.getScans();
         int scanId = 3008
         //890
          ;
         Scan scan = scans.get(scanId);
 
         //analyzer.printEdges(scan);
         analyzer.showPasses(scan);
     }
 
     private void printEdges(Scan scan) throws Exception {
         Map<Integer,Integer> msAlignResults = conf.getMSAlignResults();
         int proteinId = msAlignResults.get(scan.getId());
         System.out.println("proteinId = " + proteinId);
         Protein p = conf.getProteins().get(proteinId);
         System.out.println("protein = " + p.getName());
         System.out.println(p.getSimplifiedAcids());
         double precursorMassShift = PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan);
         List<Peak> peaks = scan.createSpectrumWithYPeaks(precursorMassShift);
 
         double[] edges = new double[Acid.acids.size()];
         int cur = 0;
         for (Acid a : Acid.acids.values()) {
             edges[cur] = a.getMass();
             cur++;
         }
 
         int n = peaks.size();
 
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             System.out.print(peak.getValue() + " " + peak.getPeakType().name() + " ");
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double[] limits = conf.getEdgeLimits(peak, next);
                 for (Acid acid : Acid.values()) {
                     if (acid.match(limits)) {
                         System.out.print(acid.name() + " " + next.getValue() + " ");
                     }
                 }
             }
             System.out.println();
         }
     }
 
     public void showPasses(Scan scan) throws IOException {
         double precursorMassShift = PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan);
         List<Peak> peaks = scan.createSpectrumWithYPeaks(precursorMassShift);
         int n = peaks.size();
 
         for (int i = 0; i < n; i++) {
             peaks.get(i).setComponentId(i);
         }

        new KDStatistics(conf).generateEdges(peaks);

        for (Peak peak : peaks) {
            peak.populatePrev();
        }

        int i = 0;
        while (i < peaks.size() - 1) {
            Peak p1 = peaks.get(i);
            Peak p2 = peaks.get(i + 1);
            if (p2.getValue() - p1.getValue() < 0.1) {
                if (p1.isParent(p2)) {
                    peaks.remove(p2);
                    p1.addCopy(p2);
                } else if (p2.isParent(p1)) {
                    peaks.remove(p1);
                    p2.addCopy(p1);
                } else {
                    i++;
                 }
            } else {
                i++;
             }
         }
 
         boolean done;
 
         do {
             done = true;
             for (Peak peak : peaks) {
                 if (peak.updateComponentId()) {
                     done = false;
                 }
             }
         } while (!done);
 
         Document doc = new Document();
         Element root = new Element("scan");
         doc.setRootElement(root);
 
         boolean[] componentDone = new boolean[n];
 
         for (Peak p : peaks) {
             int componentId = p.getComponentId();
             if (!componentDone[componentId]) {
                 List<Peak> component = new ArrayList<Peak>();
                 for (Peak peak : peaks) {
                     if (peak.getComponentId() == componentId) {
                         component.add(peak);
                     }
                 }
                 if (component.size() > 1) {
                     System.out.println("componentId = " + componentId);
                     Table table = getComponentView(component);
                     root.addContent(table.toXML());
                     componentDone[componentId] = true;
                 }
             }
         }
 
         XmlUtil.saveXml(doc, conf.getScanXmlFile(scan));
     }
 
     private Table getComponentView(List<Peak> peaks) {
         for (Peak peak : peaks) {
             peak.setMaxPrefix(-1);
         }
 
         Peak[] bestTag = {};
         for (Peak peak : peaks) {
             bestTag = findBestTag(peak, bestTag, 0, new Peak[500]);
         }
 
         Table table = new Table();
         table.addTag(0, 0, bestTag);
         clearPath(bestTag);
 
         int top = 1;
         for (int i = bestTag.length - 2; i >= 0; i--) {
             Peak peak = bestTag[i];
             for (Peak p : peaks) {
                 p.setMaxPrefix(-1);
             }
             Peak[] nextTag = findBestTag(peak, new Peak[]{}, 0, new Peak[500]);
             if (nextTag.length > 1) {
                 table.addTag(-top, i * 2 , nextTag);
                 clearPath(nextTag);
                 top++;
             }
         }
         return table;
     }
 
     private void clearPath(Peak[] bestTag) {
         for (int i = 0; i < bestTag.length - 1; i++) {
             bestTag[i].removeNext(bestTag[i + 1]);
         }
     }
 
     private Peak[] findBestTag(Peak peak, Peak[] best, int len, Peak[] prefix) {
         if (peak.getMaxPrefix() >= len) {
             return best;
         }
 
         prefix[len] = peak;
 
         if (len >= best.length) {
             best = new Peak[len +1];
             System.arraycopy(prefix, 0, best, 0, len + 1);
         }
 
         for (Peak next : peak.getNext()) {
             best = findBestTag(next, best, len + 1, prefix);
         }
 
         peak.setMaxPrefix(len);
 
         return best;
 
     }
 
 }
