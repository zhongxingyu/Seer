 /**
  * Licensed to Universite de Rouen under one or more contributor license
  * agreements. See the NOTICE file distributed with this work for
  * additional information regarding copyright ownership.
  *
  * Universite de Rouen licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.normandieuniv.cemnws.services;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Service;
 
 import fr.normandieuniv.cemnws.db.CemnEtu;
 
 @WebService
 @Service
 public class CemnWsSearchService {
 
 	private static Logger log = Logger.getLogger(CemnWsSearchService.class);
 
 	@WebMethod
 	public List<CemnEtu> search(@WebParam(name = "anneeUniv") Integer anneeUniv, @WebParam(name = "codEtu") String codEtu, @WebParam(name = "nom") String nom,
 			@WebParam(name = "operator") Integer operator) {
 
 		long startTime = System.currentTimeMillis();	
 		log.info("search(" + anneeUniv + ", " + codEtu + ", " + nom + ", " + operator + ")");
 		
		if(codEtu.trim().isEmpty())
 			codEtu = null;
 		
		if(nom.trim().isEmpty())
 			nom = null;
 		
 		
 		List<CemnEtu> urcemns = null;
 
 		if (anneeUniv == null) {
 			throw new RuntimeException("Le paramètre anneeUniv doit être renseigné");
 		}
 
 		if (codEtu == null && nom == null) {
 			urcemns = CemnEtu.findCemnEtusByAnneeUniEquals(anneeUniv.toString())
 					.getResultList();
 		}
 
 		else if (codEtu != null && nom == null) {
 			urcemns = CemnEtu.findCemnEtusByAnneeUniEqualsAndCodEtuEquals(anneeUniv.toString(), new BigDecimal(codEtu)).getResultList();
 		}
 
 		else if (codEtu == null && nom != null) {
 			if (nom.length() >= 4) {
 				nom = nom + '%';
 			}
 			urcemns = CemnEtu.findCemnEtusByAnneeUniEqualsAndNomSurCarteLike(
 						anneeUniv.toString(), nom).getResultList();
 		}
 
 		else if (codEtu != null && nom != null) {
 			if (operator == null) {
 				throw new RuntimeException("codEtu et nom sont renseignés, operator doit être renseigné");
 			}
 			if (operator.equals(0)) {
 				if (nom.length() >= 4) {
 					nom = nom + '%';
 				}
 				urcemns = CemnEtu.findCemnEtusByAnneeUniEqualsAndCodEtuEqualsAndNomSurCarteLike(
 									anneeUniv.toString(),
 									new BigDecimal(codEtu), nom).getResultList();
 			} else if (operator.equals(1)) {
 				if (nom.length() >= 4) {
 					nom = nom + '%';
 				}
 				urcemns = CemnEtu
 							.findCemnEtusByAnneeUniEqualsAndCodEtuEqualsOrAnneeUniEqualsAndNomSurCarteLike(
 									anneeUniv.toString(),
 									new BigDecimal(codEtu), nom).getResultList();
 			}
 		}
 
 		long endTime = System.nanoTime();
 		double duration = (endTime - startTime)/1000.0;
 		
 		log.info("return " + urcemns.size() + " in " + duration + " seconds.");
 		
 		return urcemns;
 	}
 
 }
