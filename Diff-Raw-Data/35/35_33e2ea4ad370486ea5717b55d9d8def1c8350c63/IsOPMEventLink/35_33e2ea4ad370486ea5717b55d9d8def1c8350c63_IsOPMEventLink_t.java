 /*******************************************************************************
  * Copyright (c) 2012 Arieh 'Vainolo' Bibliowicz
  * You can use this code for educational purposes. For any other uses
  * please contact me: vainolo@gmail.com
  *******************************************************************************/
 package com.vainolo.phd.opm.utilities.predicates;
 
 import java.util.ArrayList;
 
 import com.google.common.base.Predicate;
 import com.vainolo.phd.opm.model.OPMConsumptionEventLink;
 import com.vainolo.phd.opm.model.OPMEffectEventLink;
 import com.vainolo.phd.opm.model.OPMInstrumentEventLink;
 import com.vainolo.phd.opm.model.OPMProceduralLink;
 
 /**
  * Predicate that returns true for outgoing procedural event links.
  * 
  * @author Arieh 'Vainolo' Bibliowicz
  * @created 9 Jul 2012
  * 
  */
 public enum IsOPMEventLink implements Predicate<OPMProceduralLink> {
   INSTANCE;
 
   private ArrayList<Class<?>> TrueTypes;
   
  private IsOPMEventLink(){
	  TrueTypes = new ArrayList<>();
 	  TrueTypes.add(OPMConsumptionEventLink.class);
 	  TrueTypes.add(OPMInstrumentEventLink.class);
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
