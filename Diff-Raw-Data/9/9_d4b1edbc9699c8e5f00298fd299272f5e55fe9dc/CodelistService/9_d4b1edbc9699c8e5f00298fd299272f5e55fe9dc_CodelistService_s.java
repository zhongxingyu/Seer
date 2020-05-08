 package org.cotrix.service;
 
import org.cotrix.domain.coderelation.RelationContainer;
import org.cotrix.domain.tabular.Tabular;
import org.cotrix.domain.tabularmeta.TabularMeta;
 
 public interface CodelistService {
 
 	/**
 	 * Parse tabular data into a container of codes and relations. Some metadata is needed as input, the relations.
 	 * 
 	 * 
 	 * @param tabular
 	 * @param relations
 	 * @return
 	 */
	public RelationContainer parse(Tabular tabular, TabularMeta tabularMeta);
 
 }
