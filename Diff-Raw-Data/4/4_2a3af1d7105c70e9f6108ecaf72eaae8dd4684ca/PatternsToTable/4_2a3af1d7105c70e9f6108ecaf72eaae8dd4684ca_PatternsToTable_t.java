 package org.seasr.meandre.components.vis.ruleassociation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.table.Column;
 import org.seasr.datatypes.table.MutableTable;
 import org.seasr.datatypes.table.TableFactory;
 import org.seasr.datatypes.table.basic.BasicTableFactory;
 import org.seasr.datatypes.table.basic.IntColumn;
 import org.seasr.datatypes.table.basic.StringColumn;
 import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPPattern;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "Generates a table containing two columns: pattern and support",
         name = "Patterns To Table",
         tags = "table, patterns, frequent itemsets",
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/"
 )
 public class PatternsToTable extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
            name = "patterns",
            description = "The patterns" +
                 "<br>TYPE: java.util.ArrayList<org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPPattern>"
     )
     protected static final String IN_PATTERNS = "patterns";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "patterns_table",
             description = "The table containing the patterns and supports" +
                 "<br>TYPE: org.seasr.datatypes.table.MutableTable"
     )
     protected static final String OUT_TABLE = "patterns_table";
 
     //--------------------------------------------------------------------------------------------
 
 
     private static final TableFactory TABLE_FACTORY = new BasicTableFactory();
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void executeCallBack(ComponentContext cc) throws Exception {
         ArrayList<FPPattern> patterns = (ArrayList<FPPattern>)cc.getDataComponentFromInput(IN_PATTERNS);
 
         console.fine(String.format("Processing %d patterns...", patterns.size()));
 
         MutableTable table = (MutableTable) TABLE_FACTORY.createTable();
 
         Column colPattern = new StringColumn();
         colPattern.setLabel("Pattern");
 
         Column colSupport = new IntColumn();
         colSupport.setLabel("Support");
 
         table.addColumns(new Column[] { colPattern, colSupport });
         table.addRows(patterns.size());
 
         for (int i = 0, iMax = patterns.size(); i < iMax; i++) {
             FPPattern fpPattern = patterns.get(i);
 
             ArrayList<String> patternList = new ArrayList<String>();
             for (gnu.trove.TIntIterator it = fpPattern.getPattern(); it.hasNext(); ) {
                 int fte = it.next();
                 patternList.add(FPPattern.getElementLabel(fte));
             }
 
             Collections.sort(patternList);
 
             StringBuilder patternStr = new StringBuilder();
             for (String pat : patternList)
                 patternStr.append(pat).append(" ");
 
             console.finest(String.format("Row %d: pattern: '%s'  support: %d",
                     i, patternStr.toString(), fpPattern.getSupport()));
 
             table.setString(patternStr.toString().trim(), i, 0);
             table.setInt(fpPattern.getSupport(), i, 1);
         }
 
         cc.pushDataComponentToOutput(OUT_TABLE, table);
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
