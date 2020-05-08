 package ontology.similarity;
 
 import static ontology.similarity.Hungarian.hungarian;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import java.util.Set;
 import uk.ac.shef.wit.simmetrics.similaritymetrics.*;
 
 import org.semanticweb.owlapi.model.*;
 import parser.OntologyManager;
 import parser.Restriction;
 
 public class PropertySimilarity {
 
     /** Returns the ratio of common concepts in between the ranges of the ST and
      *  CS properties.
      * 
      * @param Pst
      * @param Pcs
      * @return
      */
     private static double propSynSim (OWLProperty Pst, OWLProperty Pcs, String owlURI) 
     {
         double common = 0.0;
 
         OntologyManager parser = OntologyManager.getInstance(owlURI);
 
         Set<OWLClass> PstRangeSet = parser.getRanges(Pst);
         Set<OWLClass> PcsRangeSet = parser.getRanges(Pcs);
 
         Set<OWLProperty> PstRangePropSet = new HashSet<OWLProperty>();
         Set<OWLProperty> PcsRangePropSet = new HashSet<OWLProperty>();
 
         for (OWLClass cls : PstRangeSet) {
             Set<OWLProperty> propList = parser.getProperties(cls);
             PstRangePropSet.addAll(propList);
         } // for
 
         for (OWLClass cls : PcsRangeSet) {
             Set<OWLProperty> propList = parser.getProperties(cls);
             PcsRangePropSet.addAll(propList);
         } // for
 
         for (OWLProperty PstRangeProp : PstRangePropSet) {
             for (OWLProperty PcsRangeProp : PcsRangePropSet) {
                 if (parser.getLocalPropertyName(PstRangeProp).equalsIgnoreCase(parser.getLocalPropertyName(PcsRangeProp))) {
                     common++;
                 } // if
             } // for
         } // for
 
         double propST = PstRangePropSet.size();
 
         if (propST == 0) {
             return 1;
         } // if
 
         double score = common / propST;
 
         return score;
 
     } // propSynSim
 
     /** Returns the cardinality similarity between two properties.
      * @param Pst
      * @param Pcs
      * @return
      */
     private static double cardinalitySim (OWLProperty Pst, OWLProperty Pcs, String owlURI)
     {
         OntologyManager parser = OntologyManager.getInstance(owlURI);
 
         Set<OWLClass> PstDomainSet = parser.getDomains(Pst);
         Set<OWLClass> PcsDomainSet = parser.getDomains(Pcs);
 
         if ((PstDomainSet != null && PcsDomainSet != null) && (!PstDomainSet.isEmpty() && !PcsDomainSet.isEmpty())) {
 
             OWLClass Cst = new ArrayList<OWLClass>(PstDomainSet).get(0);
             int cardPst = parser.getCardinality(Pst, Cst);
 
             OWLClass Ccs = new ArrayList<OWLClass>(PcsDomainSet).get(0);
             int cardPcs = parser.getCardinality(Pcs, Ccs);
 
             if (cardPst == cardPcs || (parser.isFunctional(Pst) && parser.isFunctional(Pcs))) {
                 return 1;
             } else if (cardPst < cardPcs) {
                 return .9;
             } else {
                 return .7;
             } // if
             
         } else {
             return 0;
         } // if
         
     } // cardinalitySim
 
     /** Returns the range similarity between two properties.
      * @param Pst
      * @param Pcs
     * @param Cst
     * @param Ccs
      * @return
      */
     private static double rangeSim (OWLProperty Pst, OWLProperty Pcs, OWLClass Cst, OWLClass Ccs, String owlURI)
     {
         OntologyManager parser = OntologyManager.getInstance(owlURI);
 
         Set<OWLClass>    PstRangeSet            = new HashSet<OWLClass>();
         Set<OWLClass>    PstRestrictionClassSet = new HashSet<OWLClass>();
         Set<Restriction> CstRestrictions        = Restriction.getRestrictions(Cst, owlURI);
         
         for (Restriction r : CstRestrictions) {
             if (r.getClassTypeName().equalsIgnoreCase("ObjectAllValuesFrom")) {
                 PstRestrictionClassSet.addAll(r.getCls());
             }
         }
         
         if (PstRestrictionClassSet.isEmpty()) {
             PstRangeSet = parser.getRanges(Pst);
         } else {
             PstRangeSet = PstRestrictionClassSet;
         }
         
         Set<OWLClass>    PcsRangeSet            = new HashSet<OWLClass>();
         Set<OWLClass>    PcsRestrictionClassSet = new HashSet<OWLClass>();
        Set<Restriction> CcsRestrictions        = Restriction.getRestrictions(Ccs, owlURI);
         
         for (Restriction r : CcsRestrictions) {
             if (r.getClassTypeName().equalsIgnoreCase("ObjectAllValuesFrom")) {
                 PstRestrictionClassSet.addAll(r.getCls());
             }
         }
         
         if (PcsRestrictionClassSet.isEmpty()) {
             PcsRangeSet = parser.getRanges(Pcs);
         } else {
             PcsRangeSet = PcsRestrictionClassSet;
         }
 
         Double valueToReturn = new Double(0);
         
         if (PstRangeSet == null || PcsRangeSet == null) {
             return 0;
         } else if (PstRangeSet.isEmpty() || PcsRangeSet.isEmpty()) {
             return 0;
         } else if (Pst.isOWLDataProperty() && Pcs.isOWLDataProperty()) {
 
             double sum = 0.0;
             double n = PstRangeSet.size() * PcsRangeSet.size();
 
             for (OWLClass PstRangeCls : PstRangeSet) {
                 for (OWLClass PcsRangeCls : PcsRangeSet) {
 
                     String left = parser.getLocalClassName(PstRangeCls);
                     String right = parser.getLocalClassName(PcsRangeCls);
 
                     if (left.equalsIgnoreCase("decimal")) {
                         sum += 1;
                     } else if (right.equalsIgnoreCase("long") && left.equalsIgnoreCase("decimal")) {
                         sum += .75;
                     } else if (right.equalsIgnoreCase("long") && left.equalsIgnoreCase("int")) {
                         sum += 1;
                     } else if (right.equalsIgnoreCase("int") && left.equalsIgnoreCase("string")) {
                         sum += 1;
                     } else if (right.equalsIgnoreCase("int") && left.equalsIgnoreCase("long")) {
                         sum += .66;
                     } else if (right.equalsIgnoreCase("int") && (left.equalsIgnoreCase("decimal") || left.equalsIgnoreCase("float") || left.equalsIgnoreCase("double"))) {
                         sum += .33;
                     } else if (right.equalsIgnoreCase("string")) {
                         sum += .5;
                     } else {
                         sum += 0;
                     } // if
 
                 } // for
                 
             } // for
 
             double average = sum / n;
 
             valueToReturn = average;
 
         } else if (Pst.isOWLObjectProperty() && Pcs.isOWLObjectProperty()) {
             
             double w12 = 0.5;
             double w13 = 0.5;
             
             double val = (w12 * synSim(Pst, Pcs, owlURI) + w13 * propSynSim(Pst, Pcs, owlURI)) / (w12 + w13);
             
             valueToReturn = val;
             
         } else {
             
             valueToReturn = synSim(Pcs, Pst, owlURI);
             
         } // if
         
         return valueToReturn;
         
     } // rangeSim
 
     /** Compare the syntactic similarity between two two properties.
      * @param Pst
      * @param Pcs
      * @return
      */
     private static double synSim(OWLProperty Pst, OWLProperty Pcs, String owlURI) 
     {
         OntologyManager parser = OntologyManager.getInstance(owlURI);
         
         QGramsDistance mc = new QGramsDistance();
 
         String PstLabel = parser.getPropertyLabel(Pst);
         String PcsLabel = parser.getPropertyLabel(Pcs);
         
         // TODO: Need to compare definitions.
         
         double scoreLabel = mc.getSimilarity(PstLabel, PcsLabel);
         
         return scoreLabel;
         
     } // synSim
     
     private static double restrictionMatch(OWLClass Cst, OWLClass Ccs, String owlURI) 
     {
         OntologyManager parser = OntologyManager.getInstance(owlURI);
         
         Set<Restriction> cstRestrictions = Restriction.getRestrictions(Cst, owlURI);
         Set<Restriction> ccsRestrictions = Restriction.getRestrictions(Ccs, owlURI);
         
         for (Restriction cstRestriction: cstRestrictions) {
             for (Restriction ccsRestriction: ccsRestrictions) {            
                 
                 double synSim = PropertySimilarity.synSim(cstRestriction.getProp(), ccsRestriction.getProp(), owlURI);
                 
                 // cstRestriction.getClassTypeName();
                 
             } // for
         } // for
         
         return 0.0;
         
     } // restrictionMath
     
     /** Return the matching score between two properties.
      * @param Pst
      * @param Pcs
      * @param Cst
      * @param Ccs
      * @return
      */
     private static double propMatch(OWLProperty Pst, OWLProperty Pcs, OWLClass Cst, OWLClass Ccs, String owlURI) 
     {
         OntologyManager parser = OntologyManager.getInstance(owlURI);
         
         double c = 1;
 
         if (!(parser.isInverseFunctional(Pcs) && parser.isInverseFunctional(Pst))) {
             c = 0.8;
         } // if
         
         if (Pst.getIRI().toString().compareTo(Pcs.getIRI().toString()) == 0) {
             c = 1.0;
         } // if
 
         double rangSim = rangeSim(Pst, Pcs, Cst, Ccs, owlURI);
         double synSim = synSim(Pst, Pcs, owlURI);
         
         // @TODO should we noly check cardinality when considering restrictions?
         // double cardSim = cardinalitySim(Pst, Pcs, owlURI);
         
         double val = 0.0;
         
         if (rangSim == 0) {
             val = synSim;
         } else {
             val = (0.5 * rangSim) + (0.5 * synSim);
         } // if
         
         return c * val;
 
     } // propMatch
 
     /** Returns the property similarity between two classes.
      * @param Cst
      * @param Ccs
      * @return
      * 
      */
     public static double getPropertySimScore(OWLClass Cst, OWLClass Ccs, String owlURI) 
     {
         OntologyManager parser = OntologyManager.getInstance(owlURI);
 
         double score = 0.0;
 
         Set<OWLProperty> CstPropSet = parser.getProperties(Cst); 
         List<OWLProperty> CstPropList = new ArrayList<OWLProperty>(CstPropSet);
         int CstPropSize = CstPropSet.size();
 
         Set<OWLProperty> CcsPropSet = parser.getProperties(Ccs);
         List<OWLProperty> CcsPropList = new ArrayList<OWLProperty>(CcsPropSet);
         int CcsPropSize = CcsPropSet.size();
                 
         if (CstPropSize == 0 || CcsPropSize == 0) {
             return 0;
         }
 
         double size = CstPropSize;
 
         if (CstPropSize > CcsPropSize) {
             size = CcsPropSize;
         }
         
         double[][] matrix = new double[CstPropSize][CcsPropSize];
         if (size != 0) {
             if (CstPropSize > CcsPropSize) {
                 matrix = new double[CcsPropSize][CstPropSize];
                 for (int i = 0; i < CcsPropList.size(); i++) {
                     List<Double> inScore = new ArrayList<Double>();
                     OWLProperty p1 = CcsPropList.get(i);
                     for (int j = 0; j < CstPropList.size(); j++) {
                         OWLProperty p2 = CstPropList.get(j);
                         double value = propMatch(p1, p2, Cst, Ccs, owlURI);
                         matrix[i][j] = value;
                     } // for
                 } // for
             } else {
                 matrix = new double[CstPropSize][CcsPropSize];
                 for (int i = 0; i < CstPropList.size(); i++) {
                     List<Double> inScore = new ArrayList<Double>();
                     OWLProperty p1 = CstPropList.get(i);
                     for (int j = 0; j < CcsPropList.size(); j++) {
                         OWLProperty p2 = CcsPropList.get(j);
                         double value = propMatch(p1, p2, Cst, Ccs, owlURI);
                         matrix[i][j] = value;
                     } // for
                 } // for
             } // if
 
             score = hungarian(matrix) / size;
             matrix = null;
 
         } // if
         
         return score;
         
     } // getPropertySimScore
 
 } // PropertySimilarity
