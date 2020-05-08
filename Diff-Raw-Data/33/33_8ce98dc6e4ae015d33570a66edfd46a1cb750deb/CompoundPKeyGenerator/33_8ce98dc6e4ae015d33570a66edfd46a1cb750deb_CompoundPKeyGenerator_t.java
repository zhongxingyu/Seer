 // CompoundPKeyGenerator.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import java.util.Enumeration;
 import java.util.logging.Logger;
 
 
 import com.webobjects.eoaccess.*;
 import com.webobjects.eocontrol.*;
 import com.webobjects.foundation.*;
 
 public class CompoundPKeyGenerator {
 	
 	public NSDictionary databaseContextNewPrimaryKey(EODatabaseContext dbCtxt,
             Object object, EOEntity entity) {
 		NSArray pKeys = entity.primaryKeyAttributeNames();
 		if(pKeys == null || pKeys.count() < 2)
 			return null;
 		return compoundKey((EOEnterpriseObject)object,
 				entity.primaryKeyAttributeNames(),entity.relationships());
 	}
 
 	public static NSDictionary compoundKey(EOEnterpriseObject eo) {
 		EOEditingContext ec = eo.editingContext();
 		EOEntity entity = EOUtilities.entityForObject(ec, eo);
 		NSArray pKeys = entity.primaryKeyAttributeNames();
 		NSArray relationships = entity.relationships();
 		return compoundKey(eo, pKeys, relationships);
 	}
 	
 	public static NSDictionary compoundKey(EOEnterpriseObject eo, NSArray pKeys,NSArray relationships) {
 		EOEditingContext ec = eo.editingContext();
 		Object[] values = new Object[pKeys.count()];
 		Enumeration enu = relationships.objectEnumerator();
 		while (enu.hasMoreElements() && pKeys.count() > 0) {
 			EORelationship rel = (EORelationship)enu.nextElement();
 			if(rel.isToMany()) continue;
 			Enumeration joins = rel.joins().objectEnumerator();
 			NSDictionary dKey = null;
 			while(joins.hasMoreElements()) {
 				EOJoin join = (EOJoin)joins.nextElement();
 				String sa = join.sourceAttribute().name();
 				int idx = pKeys.indexOfObject(sa);
 				if(idx < 0)
 					continue;
 				if(dKey == null) {
 					EOEnterpriseObject dest = (EOEnterpriseObject)eo.valueForKey(rel.name());
 					dKey = (dest == null)? NSDictionary.EmptyDictionary:
 						EOUtilities.primaryKeyForObject(ec, dest);
 					if(dKey == null)
 						dKey = NSDictionary.EmptyDictionary;
 				}
 				if(dKey != NSDictionary.EmptyDictionary) {
 					String dk = join.destinationAttribute().name();
 					values[idx] = dKey.valueForKey(dk);
 				}
 			}
 		}
		boolean error = false;
 		for (int i = 0; i < values.length; i++) {
 			if(values[i] == null) {
				values[i] = NSKeyValueCoding.NullValue;
				error = true;
 			}
 		}
		NSDictionary result = new NSDictionary (values,pKeys.objects());
		if(error) {
			Object[] args = new Object[] {eo,result};
			Logger.getLogger("rujel").log(WOLogLevel.WARNING,
					"Could not get value for building compound primary key for object",args);
		}
		return (error)?null:result;
 	}
 }
