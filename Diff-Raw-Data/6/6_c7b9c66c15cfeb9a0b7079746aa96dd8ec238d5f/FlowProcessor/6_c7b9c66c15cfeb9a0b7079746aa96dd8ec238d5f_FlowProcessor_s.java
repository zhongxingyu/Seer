 package com.vectorsf.jvoiceframework.flow.processor;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.webflow.execution.RequestContext;
 
 import com.vectorsf.jvoiceframework.core.bean.End;
 import com.vectorsf.jvoiceframework.core.bean.Input;
 import com.vectorsf.jvoiceframework.core.bean.Output;
 import com.vectorsf.jvoiceframework.core.bean.Record;
 import com.vectorsf.jvoiceframework.core.bean.Transfer;
 import com.vectorsf.jvoiceframework.flow.render.Renderer;
 
 /**
  * Procesador de estados IVR. De momento procesar un estado es apilarlo.
  * Los estados se eliminan una vez renderizados
  * 
  * @author dmartina
  */
 
 public class FlowProcessor implements Serializable {
 
 	private static final long serialVersionUID = -8138696103238359798L;
     
     private List<Object> states;
     
     public List<Object> getStates() {
         return states;
     }
 
     public void setStates(List<Object> states) {
         this.states = states;
     }
 
     /**
      * Renderizador de estados
      */
     private Renderer renderer; 
      
     public Renderer getRenderer() {
         return renderer;
     }
 
     public void setRenderer(Renderer renderer) {
         this.renderer = renderer;
     }
 
     public void process(Input input) {
          states.add(input);
     }
     
     public void process(Output output) {
          states.add(output);
     }
 
     public void process(Transfer transfer) {
          states.add(transfer);
     }
 
     public void process(Record record) {
     	states.add(record);
     }
     
     public void process(End end) {
         states.add(end);
    }
     
     /**
      * Renderiza y elimina los estados
      * @param flowURL
      * @return
      */
     public String render(String flowURL){
    	
    	
    	//RequestContext requestContext = org.springframework.webflow.execution.RequestContextHolder.getRequestContext();
        //String flowURL = requestContext.getFlowExecutionUrl();
         
         StringBuilder code = new StringBuilder();
         
         code.append(this.renderer.render(states, flowURL));
         	
         states.clear();
         return code.toString();
         
     }
 }
