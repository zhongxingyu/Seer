 /*******************************************************************************
  * Copyright (c) 2012 Arieh 'Vainolo' Bibliowicz
  * You can use this code for educational purposes. For any other uses
  * please contact me: vainolo@gmail.com
  *******************************************************************************/
 package com.vainolo.phd.opm.utilities.predicates;
 
 import java.util.ArrayList;
 
 import com.google.common.base.Predicate;
 import com.vainolo.phd.opm.model.OPMEffectConditionLink;
 import com.vainolo.phd.opm.model.OPMEffectEventLink;
 import com.vainolo.phd.opm.model.OPMEffectLink;
 import com.vainolo.phd.opm.model.OPMProceduralLink;
 import com.vainolo.phd.opm.model.OPMResultLink;
 
 /**
  * Predicate that returns true for links that provide data from a process.
  * 
  * @author Arieh 'Vainolo' Bibliowicz
  * @created 10 Jul 2012
  * 
  * @author Eyal
  */
 public enum IsOPMProcessOutgoingDataLink implements Predicate<OPMProceduralLink> {
   INSTANCE;
 
   private ArrayList<Class<?>> TrueTypes;
   
  IsOPMProcessOutgoingDataLink(){
 	  TrueTypes.add(OPMResultLink.class);
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
