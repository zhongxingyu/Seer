 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model.properties.game;
 
 import edu.stanford.multiagent.gamer.BattleOfTheSexes;
 import edu.stanford.multiagent.gamer.Parameters.ParamInfo;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author drew
  */
 public class BattleOfTheSexesProperties extends MatrixGameProperties {
 
     //private BattleOfTheSexes game;
     public BattleOfTheSexesProperties() {
         super();
         try {
 
             game = new BattleOfTheSexes();
             game.initialize();
             game.doGenerate();
             name = game.getName().replaceAll(" ", "");
 
             this.setNumAgents(game.getNumPlayers());
 
         } catch (Exception ex) {
             Logger.getLogger(BattleOfTheSexesProperties.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Can only have 2 agents in Battle of the sexes.
      * @param numAgents 
      */
     @Override
     public void setNumAgents(int numAgents) {
         this.numAgents = game.getNumPlayers();
     }
 
     @Override
     public boolean setField(String fieldAlias, Object val) {
 
 
         for (int i = 0; i < game.getParameters().getNParams(); i++) {
             String fieldName = game.getParameters().getName(i);
             if (fieldName.equals(fieldAlias)) {
                 try {
                     switch (game.getParameters().getParamInfo()[i].type) {
                         case ParamInfo.LONG_PARAM:
                             long lval = (long) parseDouble(val, game.getParameters().getLongParameter(fieldName));
                             game.setParameter(fieldAlias, lval);
                             this.fieldVals.put(fieldAlias, lval);
                             break;
                         case ParamInfo.DOUBLE_PARAM:
                             double dval = parseDouble(val, game.getParameters().getDoubleParameter(fieldName));
                             game.setParameter(fieldAlias, dval);
                             this.fieldVals.put(fieldAlias, dval);
                             break;
                         case ParamInfo.STRING_PARAM:
                             game.setParameter(fieldAlias, val.toString());
                             this.fieldVals.put(fieldAlias, val.toString());
                             break;
                         case ParamInfo.BOOLEAN_PARAM:
                             game.setParameter(fieldAlias, game.getParameters().getParamInfo()[i].defaultValue);
                             System.out.println("Boolean parameters not implemented.");
                             break;
                         case ParamInfo.VECTOR_PARAM:
                             // maybe set the default value if possible
                             System.out.println("Vector parameters not implemented.");
                             break;
                         case ParamInfo.CMDLINE_PARAM:
                             // set default value if possible...
                             System.out.println("CMDLine parameters not implemented.");
                             break;
                         default:
                             System.out.println("Unknown parameter not implemented.");
                             break;
                     }
                     
 
                     return true;
                 } catch (Exception ex) {
                     Logger.getLogger(BattleOfTheSexesProperties.class.getName()).log(Level.SEVERE, null, ex);
                     return false;
                 }
             }
         }
         
        return false;
     }
 
     @Override
     public void generateViewFields() {
         super.generateViewFields();
 
         System.out.println(this.game.getName());
         //System.out.println("Generating views");
         //this.setField("number of strategies", numStrats);
         //this.setFieldClass("number of strategies", Integer.class);
         for (int i = 0; i < game.getParameters().getNParams(); i++) {
             this.fieldVals.put(game.getParameters().getName(i), game.getParameter(game.getParameters().getName(i)).toString());
         }
 
     }
 
     @Override
     public String toString() {
         //System.out.println("In toString " + name + "  " + );
         return name;
     }
 }
