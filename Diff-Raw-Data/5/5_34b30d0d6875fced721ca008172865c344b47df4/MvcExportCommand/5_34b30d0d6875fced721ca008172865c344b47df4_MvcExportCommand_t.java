 package mx.itesm.web2mexadl.mvc;
 
 import java.util.Map;
 
 import mx.itesm.web2mexadl.dependencies.ClassDependencies;
 import mx.itesm.web2mexadl.dependencies.DependenciesUtil;
 import mx.itesm.web2mexadl.dependencies.ExportCommand;
 
 /**
  * Export command that adds the MVC data to the graphic export process.
  * 
  * @author jccastrejon
  * 
  */
 public class MvcExportCommand implements ExportCommand {
 
     /**
      * Map of the components' classification.
      */
     Map<String, Layer> classifications;
 
     /**
      * Full constructor.
      * 
      * @param classifications
      *            Classifications map.
      */
     public MvcExportCommand(final Map<String, Layer> classifications) {
         this.classifications = classifications;
     }
 
     @Override
     public String execute(final ClassDependencies classDependencies) {
         Layer classLayer;
         String returnValue;
 
         returnValue = null;
         if (classifications != null) {
             classLayer = classifications.get(classDependencies.getClassName());
 
             if (classLayer != null) {
                 returnValue = "\n\t" + DependenciesUtil.getDotValidName(classDependencies.getClassName())
                         + " [color=\"" + classLayer.getRgbColor() + "\",style=\"" + classLayer.getStyle() + "\"];\n";
             }
         }
 
         return returnValue;
     }
 
     @Override
     public String getDescription() {
         StringBuilder returnValue;
 
        // Create layers descriptions
         returnValue = new StringBuilder();
         for (Layer layer : Layer.values()) {
             returnValue.append("\n\t" + layer + "Layer [label=\"" + layer + "\",color=\"" + layer.getRgbColor()
                     + "\",style=\"" + layer.getStyle() + "\"];");
         }
 
         returnValue.append("\n\tsubgraph clusterMVCLayers {\n\trankdir=\"TB\";fontsize=\"8\"; label=\"MVC Layers\";");
         returnValue.append("color=\"#CCFFFF\"; style=\"bold\";\n\t");
         for (Layer layer : Layer.values()) {
             returnValue.append(layer + "Layer; ");
         }
 
        // Group invalid layers descriptions
         returnValue.append("\n");
         for (Layer layer : Layer.values()) {
             if (layer.toString().contains("Invalid")) {
                 returnValue.append(layer + "Layer -> ");
             }
         }
         returnValue.replace(returnValue.lastIndexOf("->"), returnValue.lastIndexOf("->") + 3, "");
         returnValue.append(" [style=\"invis\"];\n");
 
        // Group valid layers descriptions
         returnValue.append("\n");
         for (Layer layer : Layer.values()) {
             if (!layer.toString().contains("Invalid")) {
                 returnValue.append(layer + "Layer -> ");
             }
         }
         returnValue.replace(returnValue.lastIndexOf("->"), returnValue.lastIndexOf("->") + 3, "");
         returnValue.append(" [style=\"invis\"];\n");
 
         returnValue.append("}");
 
         return returnValue.toString();
     }
 
     /**
      * @return the classifications
      */
     public Map<String, Layer> getClassifications() {
         return classifications;
     }
 
     /**
      * @param classifications
      *            the classifications to set
      */
     public void setClassifications(Map<String, Layer> classifications) {
         this.classifications = classifications;
     }
 }
