 /*
  * mascotDatHandler.java
  *
  * Created on October 4, 2006, 1:19 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package gov.nih.nimh.mass_sieve.io;
 
 import be.proteomics.mascotdatfile.util.mascot.Header;
 import be.proteomics.mascotdatfile.util.mascot.MascotDatfile;
 import be.proteomics.mascotdatfile.util.mascot.ProteinMap;
 import be.proteomics.mascotdatfile.util.mascot.Query;
 import be.proteomics.mascotdatfile.util.mascot.QueryToPeptideMap;
 import gov.nih.nimh.mass_sieve.*;
 import java.awt.Component;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Vector;
 import javax.swing.ProgressMonitor;
 import javax.swing.ProgressMonitorInputStream;
 
 /**
  *
  * @author slotta
  */
 public class mascotDatHandler extends AnalysisHandler {
     private Component parent;
     private MascotDatfile mdf;
     private HashSet<String> minProteins;
     
     /** Creates a new instance of mascotDatHandler */
     public mascotDatHandler(String f, Component p) {
         super(f);
         parent = p;
         minProteins = new HashSet<String>();
         analysisProgram = AnalysisProgramType.MASCOT;
     }
     
     public void getMetaInfo() {
         Header head = mdf.getHeaderSection();
         //Parameters param = mdf.getParametersSection();
         //int date = head.getDate();     // search date
         searchDB = head.getRelease();  // filename of database
         //System.out.println(head.getMaxHits());
         //System.out.println(head.getResidues());
         //System.out.println(head.getSequences());
         //String version = head.getVersion();  // version of database
         //String file = param.getFile();    // input file
         //System.out.println("Date: " + date);
         //System.out.println("Release: " + release);
         //System.out.println("Version: " + version);
         //System.out.println("File: " + file);
     }
     
     public void mascotDatParse() {
         try {
             ProgressMonitorInputStream pin = new ProgressMonitorInputStream(parent, "Loading " + sourceFile, new FileInputStream(sourceFile));
             pin.getProgressMonitor().setMillisToDecideToPopup(30);
             pin.getProgressMonitor().setMillisToPopup(30);
             InputStreamReader isr = new InputStreamReader(pin);
             ProgressMonitor progressMonitor = new ProgressMonitor(parent, "Parsing " + sourceFile,"", 0, 3);
             mdf = new MascotDatfile(new BufferedReader(isr));
             pin.getProgressMonitor().close();
             progressMonitor.setMillisToDecideToPopup(30);
             progressMonitor.setNote("Created MascotDatfile object.");
             progressMonitor.setProgress(0);
             progressMonitor.setNote("Getting query to peptide map...");
             progressMonitor.setProgress(1);
             QueryToPeptideMap q2pm;
             q2pm = mdf.getQueryToPeptideMap();
             progressMonitor.setNote("Getting query list...");
             progressMonitor.setProgress(2);
             Vector AllQueries = mdf.getQueryList();
             progressMonitor.setNote("Done!");
             progressMonitor.setProgress(3);
             progressMonitor.close();
             getMetaInfo();
            for (int i=0; i<=AllQueries.size(); i++) {
                 int numHits = q2pm.getNumberOfPeptideHits(i);
                 pepHitCount += numHits;
                 if (numHits > 0) {
                     ArrayList<PeptideHit> subPeptide_hits = new ArrayList<PeptideHit>();
                     boolean isInderminate = false;
                     Query q = (Query) AllQueries.elementAt(i-1);
                     Vector pephits = q2pm.getAllPeptideHits(i);
                     be.proteomics.mascotdatfile.util.mascot.PeptideHit ph1 =
                             (be.proteomics.mascotdatfile.util.mascot.PeptideHit) pephits.elementAt(0);
                     subPeptide_hits.addAll(mascotPepToPepHitList(ph1, q, i));
                     double ionsScore = ph1.getIonsScore();
                     for (int j=1; j < numHits; j++) {
                         be.proteomics.mascotdatfile.util.mascot.PeptideHit ph2 =
                                 (be.proteomics.mascotdatfile.util.mascot.PeptideHit) pephits.elementAt(j);
                         if (ionsScore == ph2.getIonsScore()) {
                             subPeptide_hits.addAll(mascotPepToPepHitList(ph2, q, i));
                             isInderminate = true;
                         } else {
                             break;
                         }
                     }
                     if (isInderminate) {
                         for (PeptideHit p:subPeptide_hits) {
                             p.setIndeterminate(true);
                         }
                     }
                     peptide_hits.addAll(subPeptide_hits);
                 }
             }
             
             ProteinMap proMap = mdf.getProteinMap();
             
             for (String p:minProteins) {
                 try {
                     ProteinInfo pInfo = new ProteinInfo(p);
                     String desc = proMap.getProteinDescription(p);
                     desc = desc.trim();
                     pInfo.setDescription(desc);  
                     pInfo.setMass(proMap.getProteinID(p).getMass());
                     proteinDB.put(p, pInfo);
                 } catch (IllegalArgumentException ex) {} // Ignore this, why should we care
             }
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
         } catch (NullPointerException ex) {
             return;
         }
     }
     
     private ArrayList<PeptideHit> mascotPepToPepHitList(be.proteomics.mascotdatfile.util.mascot.PeptideHit ph, Query q, Integer i) {
         ArrayList<PeptideHit> peps = new ArrayList<PeptideHit>();
         ArrayList proteins = ph.getProteinHits();
         //for (Object obj:proteins) {
         //be.proteomics.mascotdatfile.util.mascot.ProteinHit pro = (be.proteomics.mascotdatfile.util.mascot.ProteinHit)obj;
         PeptideHit p = new PeptideHit();
         p.setQueryNum(i);
         p.setScanNum(scanFilenameToScanNumber(q.getTitle(), i));
         String rawFile = scanFilenameToRawFile(q.getTitle());
         p.setRawFile(rawFile);
         rawFiles.add(rawFile);
         p.setSourceType(AnalysisProgramType.MASCOT);
         String s = q.getChargeString();
         if (s.endsWith("+")) {
             p.setCharge(s.substring(0,s.length()-1));
         } else {
             p.setCharge(s);
         }
         p.setIonScore(ph.getIonsScore());
         p.setExpect(ph.getExpectancy());
         p.setIdent(ph.calculateIdentityThreshold());
         p.setSequence(ph.getSequence());
         p.setModSequence(ph.getModifiedSequence());
         p.setExpMass(q.getPrecursorMZ());
         p.setExpNeutralMass(q.getPrecursorMass());
         p.setTheoreticalMass(ph.getPeptideMr());
         p.setDiffMass(ph.getDeltaMass());
         //p.setProteinName(pro.getAccession());
         //p.setStart(pro.getStart());
         //p.setEnd(pro.getStop());
         for (Object obj:proteins) {
             be.proteomics.mascotdatfile.util.mascot.ProteinHit pro = (be.proteomics.mascotdatfile.util.mascot.ProteinHit)obj;
             ProteinHit proHit = new ProteinHit(pro.getAccession(), pro.getStart(), pro.getStop());
             minProteins.add(pro.getAccession());
             p.addProteinHit(proHit);
         }
         peps.add(p);
         //}
         return peps;
     }
     
 }
