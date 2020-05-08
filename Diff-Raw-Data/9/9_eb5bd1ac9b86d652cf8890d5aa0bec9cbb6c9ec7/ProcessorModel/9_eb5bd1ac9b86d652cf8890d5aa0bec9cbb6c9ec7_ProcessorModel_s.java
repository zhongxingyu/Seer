 package com.trendmicro.tme.grapheditor;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement
 public class ProcessorModel {
     public static enum RenderView {
         PROCESSOR_EDITOR, GRAPH_EDITOR
     }
     
     private String name;
     private Set<ExchangeModel> inputs = new HashSet<ExchangeModel>();
     private Set<String> outputs = new HashSet<String>();
     
     public ProcessorModel(String name) {
         this.name = name;
     }
     
     public String getName() {
         return name;
     }
     
     public Set<ExchangeModel> getInputs() {
         return inputs;
     }
     
     public Set<String> getOutputs() {
         return outputs;
     }
     
     public void addInput(String input) {
         inputs.add(new ExchangeModel(input));
     }
     
     public void removeInput(String input) {
         inputs.remove(new ExchangeModel(input));
     }
     
     public void addOutput(String output) {
         outputs.add(output);
     }
     
     public void removeOutput(String output) {
         outputs.remove(output);
     }
     
     public String toSubgraph(RenderView view) {
         StringBuilder sb = new StringBuilder();
 
         sb.append("subgraph \"cluster_");
         sb.append(name);
         sb.append("\"{\n");
 
         sb.append("style=filled;\n");
         sb.append("color=\"#F0F9E8\";\n");
         sb.append("nodesep=0.2;\n");
         sb.append("ranksep=0.2;\n");
 
         for(ExchangeModel input : inputs) {
             sb.append(input.toSubgraph());
             sb.append(String.format("\"%s\" -> \"%s\" [style=dotted];\n", input.getFullName(), name));
         }
 
         if(view.equals(RenderView.PROCESSOR_EDITOR)) {
             sb.append(String.format("\"add_input_%s\" [label=\"+\" shape=doublecircle href=\"javascript:add_input();\"];\n", name));
             sb.append(String.format("\"add_input_%s\" -> \"%s\" [dir=none style=invisible];\n", name, name));
         }
 
         sb.append(String.format("\"%s\" [shape=box3d %s];\n", name, view.equals(RenderView.GRAPH_EDITOR) ? "href=\"javascript:processor_onclick('" + name + "');\"": ""));
         
         if(view.equals(RenderView.PROCESSOR_EDITOR)) {
             sb.append(String.format("\"add_output_%s\" [label=\"+\" shape=doublecircle href=\"javascript:add_output();\"];\n", name));
             sb.append(String.format("\"%s\" -> \"add_output_%s\"[dir=none style=invisible];\n", name, name));
         }
 
         for(String output : outputs) {
            sb.append(String.format("\"%s\" [id=\"output-%s\" shape=record color=green href=\"%s\"];\n", output, output, view.equals(RenderView.PROCESSOR_EDITOR) ? String.format("javascript:remove_output('%s');", output): "void(0);"));
             sb.append(String.format("\"%s\" -> \"%s\" [style=dotted];\n", name, output));
         }
         sb.append("}\n");
         return sb.toString();
     }
 }
