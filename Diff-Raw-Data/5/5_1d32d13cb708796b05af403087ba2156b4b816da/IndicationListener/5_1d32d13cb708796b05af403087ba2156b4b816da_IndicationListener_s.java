 /**
  * 
  */
 
 package com.zygon.trade.market.model.indication;
 
 import com.google.common.eventbus.Subscribe;
 import com.zygon.trade.strategy.IndicationProcessor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * The purpose of an IndicationListener is to accept data from the lower 
  * layer and hand it to the interested parties.
  * 
  * @author zygon
  */
 public class IndicationListener<T_IN extends Indication> {
     
     private final String name;
     private final Logger log;
     private final Selector<T_IN> selector;
     private final IndicationProcessor<T_IN> processor;
     
     public IndicationListener(String name, Selector<T_IN> selector, IndicationProcessor<T_IN> processor) {
         this.name = name;
         this.log = LoggerFactory.getLogger(this.name);
         this.selector = selector;
         this.processor = processor;
     }
 
     public String getName() {
         return this.name;
     }
     
     // This may be dangerous..
     private volatile IndicationProcessor.Response response = null;
     
     @Subscribe
     public void handle (T_IN in) {
         this.log.trace("Handling " + in);
         
         if (this.selector.select(in)) {
             
             if (this.processor != null) {
                 IndicationProcessor.Response response = this.processor.process(in);
                 
                 if (this.response != null) {
                     if (!this.response.isEquals(response)) {
                         this.response = response;
                        this.log.info("Indication Processor response: " + this.response);
                     }
                 } else {
                     this.response = response;
                    this.log.info("Indication Processor response: " + this.response);
                 }
                 
             } else {
                 this.log.debug("No processor available to service indication");
             }
         }
     }
 }
