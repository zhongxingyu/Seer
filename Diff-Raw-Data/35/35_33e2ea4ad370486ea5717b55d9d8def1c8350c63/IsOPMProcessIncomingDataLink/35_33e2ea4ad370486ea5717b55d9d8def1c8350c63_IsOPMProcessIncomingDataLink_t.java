 /*******************************************************************************
  * Copyright (c) 2012 Arieh 'Vainolo' Bibliowicz
  * You can use this code for educational purposes. For any other uses
  * please contact me: vainolo@gmail.com
  *******************************************************************************/
 package com.vainolo.phd.opm.utilities.predicates;
 
 import java.util.ArrayList;
 
 import com.google.common.base.Predicate;
 import com.vainolo.phd.opm.model.*;
 
 /**
  * Predicate that returns true for links that provide incoming data.
  * 
  * @author Arieh 'Vainolo' Bibliowicz
  * @created 10 Jul 2012
  * 
  */
 public enum IsOPMProcessIncomingDataLink implements Predicate<OPMProceduralLink> {
   INSTANCE;
 
   private ArrayList<Class<?>> TrueTypes;
   
  private IsOPMProcessIncomingDataLink(){
	  TrueTypes = new ArrayList<>();
 	  TrueTypes.add(OPMAgentLink.class);
 	  TrueTypes.add(OPMConsumptionLink.class);
 	  TrueTypes.add(OPMConsumptionConditionLink.class);
 	  TrueTypes.add(OPMConsumptionEventLink.class);
 	  TrueTypes.add(OPMInstrumentLink.class);
 	  TrueTypes.add(OPMInstrumentConditionLink.class);
 	  TrueTypes.add(OPMInstrumentEventLink.class);
 	  TrueTypes.add(OPMEffectLink.class);
 	  TrueTypes.add(OPMEffectConditionLink.class);
 	  TrueTypes.add(OPMEffectEventLink.class);
   }
   
   
   @Override
   public boolean apply(final OPMProceduralLink link) {
 	  Class<?> linkType = link.getClass();
     for (Class<?> type:TrueTypes)
     	if (type.isAssignableFrom(linkType)) return true;
     return false;
   }
 
 }
