 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ui;
 
 import java.util.HashMap;
 import matrix.Matrix;
 
 /**
  *
  * @author Lasse
  */
 class OperationsParser {
 
     private HashMap<String, Matrix> variables;
 
     OperationsParser() {
         variables = UserInterface.getInstance().getVariables();
     }
 
     private boolean variablesFound(String[] vars) {
         for (String var : vars) {
             if (!variables.containsKey(var)) {
                 System.err.println("Cannot find variable '" + var + "'!");
                 return false;
             }
         }
         return true;
     }
 
     Matrix scale(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("scale");
         }
         if (!variablesFound(new String[]{split[0]})) {
             return null;
         }
         Matrix m = variables.get(split[0]);
         double k;
         k = ParseUtils.parseDouble(params);
         return m.scale(k);
     }
 
     Matrix add(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("add");
         }
         if (!variablesFound(split)) {
             return null;
         }
         Matrix m1 = variables.get(split[0]);
         Matrix m2 = variables.get(split[1]);
 
         return m1.add(m2);
     }
 
     Matrix sub(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
             ParseUtils.wrongNumberOfParametersInFunction("sub");
         }
         if (!variablesFound(split)) {
             return null;
         }
         Matrix m1 = variables.get(split[0]);
         Matrix m2 = variables.get(split[1]);
 
         return m1.sub(m2);
     }
 
     Matrix mul(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
            ParseUtils.wrongNumberOfParametersInFunction("mul");
         }
         if (!variablesFound(split)) {
             return null;
         }
         Matrix m1 = variables.get(split[0]);
         Matrix m2 = variables.get(split[1]);
 
         return m1.mul(m2);
     }
 
     Matrix mulN(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
            ParseUtils.wrongNumberOfParametersInFunction("mulN");
         }
         if (!variablesFound(split)) {
             return null;
         }
         Matrix m1 = variables.get(split[0]);
         Matrix m2 = variables.get(split[1]);
 
         return m1.mulNaive(m2);
     }
 
     Matrix mulS(String params) {
         String[] split = ParseUtils.getParameterSplit(params);
         if (split.length != 2) {
            ParseUtils.wrongNumberOfParametersInFunction("mulS");
         }
         if (!variablesFound(split)) {
             return null;
         }
         Matrix m1 = variables.get(split[0]);
         Matrix m2 = variables.get(split[1]);
 
         return m1.mulStrassen(m2);
     }
 
     Matrix pow(String params) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     Matrix transpose(String params) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     Matrix det(String params) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     Matrix inv(String params) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     Matrix lu(String params) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 }
