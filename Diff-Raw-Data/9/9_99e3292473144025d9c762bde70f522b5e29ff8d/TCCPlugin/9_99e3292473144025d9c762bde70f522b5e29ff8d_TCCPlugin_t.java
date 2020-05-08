 package jp.ac.osaka_u.ist.sel.metricstool.tcc;
 
 
 import java.util.Set;
 import java.util.SortedSet;
 
import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VariableUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractClassMetricPlugin;
 
 
 /**
  * TCC(Tight Class Cohesion)vZ郁gNX
  * <p>
  * SẴIuWFNgwɑΉ
  * 
  * @author y-higo
  * 
  */
 public class TCCPlugin extends AbstractClassMetricPlugin {
 
     /**
      * ŗ^ꂽNXTCCvZ
      * 
      * @param targetClass TCCvZΏۃNX
      * @return vZ
      */
     @Override
     protected Number measureClassMetric(TargetClassInfo targetClass) {
 
         int couplings = 0;
        final SortedSet<MethodInfo> methods = targetClass.getDefinedMethods();
        METHOD1: for (final MethodInfo method1 : methods) {
 
             // method1 QƂĂϐCĂϐ擾
             final Set<VariableInfo<?>> usedVariables = VariableUsageInfo.getUsedVariables(method1
                     .getVariableUsages());
 
            METHOD2: for (final MethodInfo method2 : methods.tailSet(method1)) {
 
                 // metho1  method2 ꍇ̓XLbv
                 if (method1.equals(method2)) {
                     continue;
                 }
 
                 // method2 QƂĂϐCĂϐ擾
                 Set<VariableUsageInfo<?>> variableUsages = method2.getVariableUsages();
 
                 // method1 QƂĂϐ method2 pĂDDD
                 for (final VariableUsageInfo<?> variableUsage : variableUsages) {
                     final VariableInfo<?> usedVariable = variableUsage.getUsedVariable();
                     if (usedVariables.contains(usedVariable)) {
                         couplings++;
                         continue METHOD2;
                     }
                 }
             }
         }
 
         final int combinations = methods.size() * (methods.size() - 1) / 2;
         return 1 < methods.size() ? new Float((float) couplings / (float) combinations)
                 : new Float(0);
     }
 
     /**
      * ̃vOC̊ȈՐPsŕԂ
      * 
      * @return ȈՐ
      */
     @Override
     protected String getDescription() {
         return "Measuring the TCC metric.";
     }
 
     /**
      * ̃vOCv郁gNX̖OԂ
      * 
      * @return TCC
      */
     @Override
     protected String getMetricName() {
         return "TCC";
     }
 
     /**
      * ̃vOCtB[hɊւ𗘗p邩ǂԂ\bhD trueԂD
      * 
      * @return trueD
      */
     @Override
     protected boolean useFieldInfo() {
         return true;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bhD trueԂD
      * 
      * @return trueD
      */
     @Override
     protected boolean useMethodInfo() {
         return true;
     }
 }
