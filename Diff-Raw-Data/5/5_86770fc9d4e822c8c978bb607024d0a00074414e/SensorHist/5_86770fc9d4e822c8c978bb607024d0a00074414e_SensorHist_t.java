 package predict.singletarget;
 
 import mem.OneView;
 
 import java.util.*;
 
 import weka.classifiers.Classifier;
 import weka.classifiers.trees.J48;
 import weka.classifiers.trees.ADTree;
 import weka.classifiers.trees.BFTree;
 import weka.classifiers.trees.DecisionStump;
 import weka.core.Instances;
 import weka.core.Instance;
 import weka.core.Attribute;
 import com.pmstation.common.utils.PrivateFieldGetter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: adenysenko
  * Date: 4/8/2008
  * Time: 17:37:58
  */
 public class SensorHist implements java.io.Serializable{
   final String sensorName;
   Set<Object> vals = new HashSet<Object>();
   Set<String> skippedViewKeys;
 
   Classifier lastUsedClassifier;
   LinkedHashMap<OneView, Object> exampleVals = new LinkedHashMap<OneView, Object>();
 
   List<SRule> srules = new ArrayList<SRule>();
   Object otherRulesResult = null;
 
   public void printRules(){
     System.out.println(srules+" other="+otherRulesResult);
   }
 
   public SensorHist(String sensorName) {
     this.sensorName = sensorName;
   }
 
   public void setSkippedViewKeys(Set<String> skippedViewKeys) {
     this.skippedViewKeys = skippedViewKeys;
   }
 
   /**
    *
    * @param val
    * @param v - next state, v.prev finishes info which can be used for prediction,
    * v usually has info describing the result because of which this categorization is taking place.
    */
   public void add(Object val, OneView v) {
     if( v.prev==null ){
       return;
     }
     addAsCurrent(val, v.prev);
   }
 
   boolean ruleIsExtra(SRule r){
     for( SRule s : srules ){
       if( r.condWiderIn(s) ){
         return true;
       }
     }
     return false;
   }
 
   List<OneView> unexplainedExamples(){
     List<OneView> ret = new ArrayList<OneView>();
     for( OneView ve : exampleVals.keySet() ){
       boolean found=false;
       for( SRule r : srules ){
         if( r.condHolds(ve) ){
           found=true;
           break;
         }
       }
       if( !found ){
         ret.add(ve);
       }
     }
     return ret;
   }
 
   SRule ruleByDecisionStump(Collection<OneView> views){
     DecisionStump myClassif  = new DecisionStump();
     WekaBuilder wf = buildClassifier(myClassif, views);
     Attribute splitAttr = wf.getInstances().attribute((Integer)PrivateFieldGetter.evalNoEx(myClassif,"m_AttIndex"));
 
     String attName = splitAttr.name();
     Object attVal = wf.attVal(attName, ((Double)PrivateFieldGetter.evalNoEx(myClassif,"m_SplitPoint")).intValue() );
     SRule r = new SRule(attName, attVal, true);
     return r;
   }
 
   void analyzeNewExample(Object val, OneView vprev){
     if( exampleVals.size()<2 ){
       return;
     }
 
     boolean explained = verifyRules(val, vprev);
     if( explained ){
       return;
     }
     Object commonResAll = commonResValue(exampleVals.keySet());
     if( commonResAll!=null ){
       otherRulesResult=commonResAll;
       return;
     }
     SRule r = ruleByDecisionStump( exampleVals.keySet() );
     ruleCheckAndAdd(r);
     SRule rn = r.negate();
     ruleCheckAndAdd(rn);
 
     List<OneView> unex = unexplainedExamples();
     for( int j=0; j<10 && !unexplainedExamples().isEmpty(); j++ ){
       for( int i=unex.size()-1; i>=0; i-- ){
         if( singleAttrRuleHunting(unex.get(i)) ){
           //must try to find beautiful solution - break;
         }
         break; // process only last example
       }
       unex = unexplainedExamples();
     }
 
     Object commonResUnex = commonResValue(unex);
     if( commonResUnex!=null ){
       otherRulesResult=commonResUnex;
     }
   }
 
   boolean singleAttrRuleHunting(OneView vprev){
     Map<String, Object> m = vprev.getViewAll();
     for( String s : m.keySet() ){
       SRule r = new SRule(s, m.get(s), true);
       if( ruleCheckAndAdd(r) ){
         //must try to find beautiful solution - return true;
       }
     }
     return false;
   }
 
   private boolean ruleCheckAndAdd(SRule r) {
     if( r.complexity()>2 ){
       return false;
     }
     List<OneView> exList = examplesCondHolds(exampleVals.keySet(), r);
     if( exList.size()<2 ){
       return false;
     }
     Object commonRes = commonResValue(exList);
     if( commonRes!=null ){
       r.setResult(commonRes);
       if( !ruleIsExtra(r) ){
         srules.add(r);
         return true;
       }
     }else{
       SRule subR = ruleByDecisionStump(exList);
       SRule rnew = r.andRule(subR);
       if( rnew.complexity()>r.complexity() ){
         ruleCheckAndAdd(rnew);
      }
      SRule rnewNeg = r.andRule(subR.negate());
      if( rnewNeg.complexity()>r.complexity() ){
        ruleCheckAndAdd(rnewNeg);
       }
 
 //      List<OneView> exList2 = examplesCondHolds(exList, subR);
 //      Object commonRes2 = commonResValue(exList2);
 //      if( commonRes2!=null && exList2.size()>=2 ){
 //        System.na n oTime();
 //      }
     }
     return false;
   }
 
   private boolean verifyRules(Object val, OneView vprev) {
     boolean explained=false;
     for( Iterator<SRule> i = srules.iterator(); i.hasNext(); ){
       SRule sr = i.next();
       if( sr.condHolds(vprev) ){
         if( !sr.getResult().equals(val) ){
           i.remove();
         }else{
           explained=true;
         }
       }
     }
     if( !explained && otherRulesResult!=null ){
       if( !otherRulesResult.equals(val) ){
         otherRulesResult=null;
       }else{
         explained=true;
       }
     }
     return explained;
   }
 
   Object predictWithDecisionStumpBasedRulesNoOther(OneView vprev){
     Object res=null;
     SRule rres=null;
     for( Iterator<SRule> i = srules.iterator(); i.hasNext(); ){
       SRule sr = i.next();
       if( sr.condHolds(vprev) ){
         Object resi = sr.getResult();
         if( resi!=null && res!=null && !resi.equals(res) ){
           //throw new RuntimeException("rule conflict "+sr+" "+rres);
           System.out.println("rule conflict "+sensorName+" "+sr+" "+rres+" view="+vprev);
           return null;
         }
         res = resi;
         rres = sr;
       }
     }
     return res;
   }
 
   Object predictWithDecisionStumpBasedRules(OneView vprev){
     Object res = predictWithDecisionStumpBasedRulesNoOther(vprev);
     if( res!=null ){
       return res;
     }
     if( otherRulesResult==null ){
       // we don't have prediction at hand
       singleAttrRuleHunting(vprev); // maybe we can derive it right now
       res = predictWithDecisionStumpBasedRulesNoOther(vprev);
       if( res!=null ){
         return res;
       }
     }
     return otherRulesResult; // can be null if no global 'other' rule exists
   }
 
   Object commonResValue(Collection<OneView> exList){
     Object com = null;
     for( OneView v : exList ){
       Object r = exampleVals.get(v);
       if( com!=null ){
         if( !com.equals(r) ){
           return null;
         }
       }
       com=r;
     }
     return com;
   }
 
   List<OneView> examplesCondHolds(Collection<OneView> views, SRule r) {
     List<OneView> ret = new ArrayList<OneView>();
     for (OneView v : views) {
       if (r.condHolds(v)) {
         ret.add(v);
       }
     }
     return ret;
   }
 
   public void printAsTestCase() {
     for (OneView v : exampleVals.keySet()) {
       System.out.println(exampleVals.get(v) + "" + v.getViewAll());
     }
   }
 
 
   public void addAsCurrent(Object val, OneView v) {
     vals.add(val);
     exampleVals.put(v, val);
 
     analyzeNewExample(val, v);
   }
 
   public int valsSize(){
     return vals.size();
   }
 
   /**
    * Can be conflicting with other values if our prior experience is limited.
    * @param v
    * @param val
    * @return
    */
   public boolean valAcceptedByRules(OneView v, Object val){
     return val.equals( predict(v) );
     //return val.equals( predictWithWeka(v) );
     //return vals.get(val).acceptedByRules(v)!=null;
   }
 
   public WekaBuilder buildClassifier(Classifier myClassif, Collection<OneView> views){
     WekaBuilder wf = new WekaBuilder(myClassif);
     for( OneView v : views ){
       wf.collectAttrs(v, skippedViewKeys);
     }
 
     for( Object o : vals ){
       wf.addForRes(o);
     }
     wf.mkInstances();
 
 
     for( OneView v : views ){
       wf.addInstance(v, exampleVals.get(v).toString());
     }
 
     Instances ins = wf.getInstances();
     if( ins.numInstances()<1 ){
       return null;
     }
 
 
     try {
       wf.getClassifier().buildClassifier(ins);
     } catch (Exception e) {
       throw new RuntimeException("",e);
     }
     return wf;
   }
 
   public Object predictWithWeka(OneView vnew){
 //    J48 myClassif = new J48();
 //    myClassif.setUnpruned(true);
 //    myClassif.setConfidenceFactor(1);
 
     DecisionStump myClassif  = new DecisionStump();
     lastUsedClassifier = myClassif;
 
     WekaBuilder wf = buildClassifier(myClassif, exampleVals.keySet());
 
     double d;
     try {
 //      { 0.5, 0.5 } - J48 classifies as first, bug! don't use classifyInstance()
 //      source code for J48?
 //      Weka alg comparison?
 
 //      ((SensorHist)((HashMap.Entry)((HashMap)((Pred)p.p.algs.toArray()[0]).singles).entrySet().toArray()[0]).getValue()).lastUsedClassifier = Type is unknown for '((HashMap)((Pred)p.p.algs.toArray()[0]).singles).entrySet()'
 //     -C 0.25 -M 2
 
       // confidence must be 0.99, min=1
 
       //      http://grb.mnsu.edu/grbts/doc/manual/J48_Decision_Trees.html
 
       d = lastUsedClassifier.classifyInstance( wf.mkInstance(vnew) );
       if( d == Instance.missingValue() ){
         return null;
       }
 
       double[] dist = lastUsedClassifier.distributionForInstance( wf.mkInstance(vnew) );
       int di = (int)d;
       double dProb = dist[ di ];
       for( int i=0; i<dist.length; i++ ){
         if( i!=di && dist[i] >= dProb/1.5 ){
           return null; // there is no really outstanding class predicted
         }
       }
       return wf.getForResObj(di);
 
     } catch (Exception e) {
       throw new RuntimeException("",e);
     }
   }
 
   public Object predict(OneView v) {
     return predictWithDecisionStumpBasedRules(v);
     //return predictWithWeka(v);
     //return predictSimpleWay(v);
   }
 
 
   public boolean skipViewKey(String k) {
     return skippedViewKeys!=null && skippedViewKeys.contains(k);
   }
 
   public String getSensorName() {
     return sensorName;
   }
 
   public String toString() {
     StringBuilder sb = new StringBuilder(sensorName);
     sb.append(" {");
     boolean first=true;
     for( Object v : vals ){
       if( !first ){
         sb.append(",");
       }
       sb.append(v);
       first = false;
     }
     sb.append("}");
     return sb.toString();
   }
 }
