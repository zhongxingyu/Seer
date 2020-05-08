 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jgenfit.bussines.experiment;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import jgenfit.bussines.experiment.ModelSingleExperiment;
 import jgenfit.utils.GenfitLogger;
 
 /**
  *
  * @author alex
  */
 public class SingleExperiment{
         private List<ModelSingleExperiment> models = new ArrayList<ModelSingleExperiment>();
         private HashMap<String, String> parameters = new HashMap<String, String>();
         private List<String> scatteringParameters =  new ArrayList<String>();
         private String content;
         private String scatteringLine;
 
         private final String SCATERING_FLAG = "Experimental Scattering";
         private static String MODEL_SEPARATOR = "For each model copy and paste the next three lines";
         private static String END_OF_SECTION = " [End of section]";
         
         public SingleExperiment(String text) {
             this.content = text;                                  
             /** Reading parameters **/
             List<String> parametersModelForExperiment = Arrays.asList(Arrays.asList(text.split(MODEL_SEPARATOR)).get(0).split("\n"));
             for (int i = 1; i < parametersModelForExperiment.size() - 1; i++) {                                
                 List<String> parameter = Arrays.asList(parametersModelForExperiment.get(i).split(":"));
                 
                 String key = parameter.get(0);
                 String value = new String();
                 
                 if (parameter.size() == 2){
                     value = parameter.get(1);
                 }
                 
                 if (key.contains(SCATERING_FLAG)){
                     this.scatteringLine = parametersModelForExperiment.get(i);
                     value = this.parseScatteringCurveLineFile(parametersModelForExperiment.get(i));
                     this.scatteringParameters = this.parseScatteringCurveLineParamters(parametersModelForExperiment.get(i));
                 }                
                 
                 parameters.put(key, value);
             }
             
             /** Reading models **/
             List<String> experimentModelForExperiment = Arrays.asList(Arrays.asList(text.split(MODEL_SEPARATOR)).get(1).split("\n"));
             
             /** First line should be white line and the last line should be [End of section] **/
             //  int numerOfodelsInTheExperiment = (experimentModelForExperiment.size() - 2)/3;                
             
             for (int j = 1; j < (experimentModelForExperiment.size() - 2); j = j +3) {
                    this.models.add(new ModelSingleExperiment(experimentModelForExperiment.get(j), experimentModelForExperiment.get(j + 1), experimentModelForExperiment.get(j + 2)));                   
             }
         }
         
         
         public void saveModelSingleExperiment(int index, ModelSingleExperiment model){
             /** Reading models **/
             List<String> lines = Arrays.asList(this.getContent().split("\n"));            
             StringBuilder br = new StringBuilder();                        
             boolean modelSection = false;
             int modelCounterLines = 0;
             int newModelLinesIndex = 0;
             for (int i = 0; i < lines.size(); i++) {
                 if (lines.get(i).contains(MODEL_SEPARATOR)){
                     modelSection = true;
                     br.append(lines.get(i)).append("\n"); 
                     continue;
                 }
                 
                 if (modelSection == false){
                     br.append(lines.get(i)).append("\n");                
                 }                
                 else{
                     if (((index * 3)<= modelCounterLines && modelCounterLines < ((index*3) + 3))){
                        
                         if (newModelLinesIndex != 2){
                             br.append(Arrays.asList(model.getContent().split("\n")).get(newModelLinesIndex)).append("\n");
                         }
                         else{
                             br.append(Arrays.asList(model.getContent().split("\n")).get(newModelLinesIndex)).append("\n");
                         }
                         
                         newModelLinesIndex++;
                     }
                     else{
                         br.append(lines.get(i)).append("\n");
                     }
                     modelCounterLines++;
                 }
             }            
             this.content = br.toString();               
         }
         
         public void addNewModel(int modelSelected){
            String template = this.readResourceExperimentModelTemplate();          
            template = template.replace("$x", String.valueOf(modelSelected));           
            /*this.content = this.content.replace(" [End of section]", template + " [End of section]");*/
            
            if (this.content.contains(END_OF_SECTION)){
                this.content = this.content.replace(END_OF_SECTION, template + END_OF_SECTION) + "\n";
            }     
            else{
                this.content = this.content + template;
            }
         }
         
         private String readResourceExperimentModelTemplate(){
             StringBuilder sb = new StringBuilder();
             String resourcesPath = "/jgenfit/resources/templates/experimentmodel.txt";
             try {
                 BufferedReader objBin = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(resourcesPath)));
                 if (objBin != null) {
                     String strLine = "";
                     while ((strLine = objBin.readLine()) != null) {
                         sb.append(strLine + "\n" );
                     }
                     objBin.close();
                 } else {
                     GenfitLogger.error("Unable to retrieve InputStream");
                 }
             } catch (Exception ex) {
                 GenfitLogger.error("Unable to retrieve InputStream");            
             }            
             return sb.toString();        
         }
         
         
         public void removeModel(int index){
             /** Reading models **/
             List<String> lines = Arrays.asList(this.getContent().split("\n"));            
             StringBuilder br = new StringBuilder();                        
             boolean modelSection = false;
             int modelCounterLines = 0;
             
             for (int i = 0; i < lines.size(); i++) {
                 if (lines.get(i).contains(MODEL_SEPARATOR)){
                     modelSection = true;
                     br.append(lines.get(i)).append("\n"); 
                     continue;
                 }
                 
                 if (modelSection == false){
                     br.append(lines.get(i)).append("\n");                
                 }                
                 else{
                     if (((index * 3)<= modelCounterLines && modelCounterLines < ((index*3) + 3))){
                         //System.out.println("Eliminar: " + lines.get(i));
                     }
                     else{
                         br.append(lines.get(i)).append("\n");
                     }
 
                     modelCounterLines++;
                 }
             }            
             this.content = br.toString();
         }
         /*
          *                             [---File Name ------------|Par. Symb.|--Value--|Par. Symb.|--Value--|Par. Symb.|--Value--|Par. Symb.|--Value--|
  Experimental Scattering Curve.........:Beta/mio_6_c_30_05.ass       cpro         2.      peso       17300.    vol        22046.     time      50.
                                         40                        66
          */
         private String parseScatteringCurveLineFile(String line){
            // System.out.println(line);
             if (line.trim().length()< 41) return new String();
             
             int max = 140;
             if (line.length() < 140)
                 return line.substring(40, line.length());
             else
                 return line.substring(40, 140);
         }
         
         private List<String> parseScatteringCurveLineParamters(String line){
             while (line.length() < 400){
                 line = line + " ";
             }            
             this.scatteringParameters = new ArrayList<String>();
             /** First index of param position **/
             int paramIndex = 140;            
             for (int i = 0; i < 12; i++) {
                 if (line.length() > paramIndex){
                     /** name **/
                    String value = line.substring(paramIndex, paramIndex + 10);
                     /** value **/
                    String key = line.substring(paramIndex + 10, paramIndex + 20);
                     //System.out.println("Value: " + value.replace(" ", "|") + "   Key: " + key.replace(" ", "*"));
                     paramIndex = paramIndex + 21;                    
                     //values.put(value, key);
                     this.getScatteringParameters().add(value + " = " + key);
                 }
             }
             return this.getScatteringParameters();
         }
         
         
         /*
         public String getModelsNumber(){
             String modelsNumber = new String();
             for (int i = 0; i < this.models.size(); i++) {
                 modelsNumber = modelsNumber + " " + this.models.get(i).getModelNumber();
             }
             return modelsNumber;
         }
          * 
          */
         
         
          public List<Integer> getModelsNumber(){
             List<Integer> numbers = new ArrayList<Integer>();
             for (int i = 0; i < this.getModels().size(); i++) {
                 numbers.add(Integer.valueOf(this.getModels().get(i).getModelNumber()));
             }
             return numbers;
         }                         
     
         public String getDescription(){
           return this.getParam("Description of Experiment");
         }      
         
         public String getExperimentalScatteringCurve(){
           return this.getParam("Scattering Curve");
         }
         
         public void setExperimentalScatteringCurve(String value){                       
             List<String> lines = Arrays.asList(this.content.split("\n"));
             for (String line : lines) {
                 if (line.contains(SCATERING_FLAG)){                
                     String newline = this.setScatteringParameter(line, 40, 140, value);                
                     this.content = this.content.replace(line, newline);
                 }               
             } 
         }
                           
         public boolean getBooleanParam(String key){
             if (getParam(key) != null){
                 if (getParam(key).contains("1")){
                     return true;
                 }
                 else{
                     return false;
                 }            
             }
             return false;
         }
         
         
         public String getParam(String key_param){
             for (String key : this.parameters.keySet()) {
                 //System.out.println("key " + key);
                 if (key.contains(key_param)){
                     return this.parameters.get(key).trim();
                 }
             }
             return "not found";             
         }
         
         
         public void setParam(String key_param, String value){
             for (String key : this.parameters.keySet()) {
                 //System.out.println("key " + key);
                 if (key.contains(key_param)){
                     this.parameters.put(key_param, value);
                     this.changeParamContent(key, value);
                     return;
                 }
             }   
         }
         
         private void changeParamContent(String key, String value){
             List<String> lines = Arrays.asList(this.getContent().split("\n"));            
             for (String line : lines) {
                 if (line.toLowerCase().replaceAll(" ", "").contains(key.toLowerCase().replaceAll(" ", ""))){
                     //System.out.println("Yeha: " + line);
                     String aux = line;
                     //int indexSeparator = line.lastIndexOf(":");
                     line = line.substring(0, 40) + value;
                     //System.out.println("Yeha: " + aux);                                                            
                     this.content = this.getContent().replace(aux, line);
                     //System.out.println(this.content);                    
                 }
             }                   
         }
 
         /**
          * @return the content
          */
         public String getContent() {
             return content;
         }
 
         /**
          * @return the models
          */
         public List<ModelSingleExperiment> getModels() {
             return models;
         }
 
         /**
          * @return the scatteringParameters
          */
         public List<String> getScatteringParameters() {
             return scatteringParameters;
         }
 
         public String getScatteringParametersValue(int index) {            
             return Arrays.asList(this.getScatteringParameters().get(index).split("=")).get(1).trim();
         }
         
         public String getScatteringParametersKey(int index) {         
             return Arrays.asList(this.getScatteringParameters().get(index).split("=")).get(0).trim();
         }
         
         
         public void setScatteringCurveParameters(List<String> parameters){
             int start = 141;
             String aux = this.scatteringLine;
             
             while (this.scatteringLine.length() < 400){
                 this.scatteringLine = this.scatteringLine.concat(" ");
             }            
             
             for (int i = 0; i < parameters.size(); i++) {
                 String key = Arrays.asList(parameters.get(i).split("=")).get(0);
                 String value = Arrays.asList(parameters.get(i).split("=")).get(1);                
                 int position = start + i*21;                
                 this.scatteringLine = this.setScatteringParameter(this.scatteringLine, position, position + 10, key);
                 this.scatteringLine = this.setScatteringParameter(this.scatteringLine, position + 10, position + 20, value);                                
             }
         
             //System.out.println("Antiguo: " + aux);
             //System.out.println("Replace: " + this.scatteringLine);
             this.content = this.getContent().replace(aux, this.scatteringLine);                                
         }        
         
           private String setScatteringParameter(String line, int start, int end, String value){              
             int length = end - start;
             if(value.length()< length){
                 for(int i = value.length(); i < length; i++){
                     value = value.concat(" ");
                 }                
             }
             
             
              if(line.length()< 141){
                 for(int i = line.length(); i < 141; i++){
                     line = line.concat(" ");
                 }                
             }
              
     
             int count = 0;
             for (int i = start; i < end; i++) {
                 line = replaceCharAt(line, i, value.charAt(count));
                 count ++;
             }
             return line;
         
         }
 
          private String replaceCharAt(String s, int pos, char c) {
             return s.substring(0, pos) + c + s.substring(pos + 1);
          }
     }
       
