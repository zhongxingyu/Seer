 package org.opentaps.dataimport;
 
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javolution.util.FastList;
 import javolution.util.FastMap;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.entity.Delegator;
 import org.ofbiz.entity.GenericEntityException;
 import org.ofbiz.entity.GenericValue;
 import org.ofbiz.entity.condition.EntityCondition;
 import org.ofbiz.entity.condition.EntityOperator;
 import org.ofbiz.service.DispatchContext;
 import org.ofbiz.service.GenericServiceException;
 import org.ofbiz.service.LocalDispatcher;
 import org.ofbiz.service.ModelService;
 import org.ofbiz.service.ServiceUtil;
 import org.opentaps.common.util.UtilCommon;
 import org.opentaps.common.util.UtilMessage;
 import org.opentaps.foundation.service.ServiceException;
 
 public class UtilOperacionDiariaServices {
 	
 	private static final String MODULE = UtilOperacionDiariaServices.class.getName();
 	private static final BigDecimal ZERO = BigDecimal.ZERO;
 	
 	/**
 	 * Metodo para obtener workeffortid Padre
 	 * @param dctx
 	 * @param dispatcher
 	 * @param workEffortTypeId
 	 * @return
 	 * @throws GenericServiceException
 	 */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static String obtenPadreWorkEffort(DispatchContext dctx,LocalDispatcher dispatcher,String workEffortTypeId) throws GenericServiceException{
     	
     	String padreWorkEffortTypeId = null;
     	
     	if(workEffortTypeId != null && !workEffortTypeId.isEmpty()){
 
         	Map input = FastMap.newInstance();
         	input.put("workEffortTypeId", workEffortTypeId);
         	input = dctx.getModelService("obtenWorkEffortPadreId").makeValid(input, ModelService.IN_PARAM);
         	Map tmpResult = dispatcher.runSync("obtenWorkEffortPadreId", input);
             padreWorkEffortTypeId = (String) tmpResult.get("workEffortTypeIdPadre");
             
             Debug.logWarning("workEffortTypeId "+workEffortTypeId+"   PADRE  "+padreWorkEffortTypeId, MODULE);
     		
     	}
     	
     	return padreWorkEffortTypeId;
     	
     }	
 	
 	/**
 	 * Servicio obtiene WorkEffortId Padre a partir de uno dado
 	 * @param dctx
 	 * @param context
 	 * @return
 	 */
     @SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Map obtenWorkEffortPadreId(DispatchContext dctx, Map context) {
         Delegator delegator = dctx.getDelegator();
         String workEffortTypeId = (String) context.get("workEffortTypeId");
         
         String workEffortTypeIdPadre = null;
       
 		try {        
 			
 	    	EntityCondition condicionWork = EntityCondition.makeCondition("workEffortTypeId", EntityOperator.EQUALS, workEffortTypeId);
 	    	List<GenericValue> resultadoWorkEff = delegator.findByCondition("WorkEffort", condicionWork , UtilMisc.toList("workEffortTypeId","workEffortParentId"), null);
 	    	
 	    	if(resultadoWorkEff != null && !resultadoWorkEff.isEmpty()){
 	    		
 	    		workEffortTypeIdPadre = resultadoWorkEff.get(0).getString("workEffortParentId");
 	    		
 	    	}
 			
 		} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
         
         Map results = ServiceUtil.returnSuccess();
         results.put("workEffortTypeIdPadre", workEffortTypeIdPadre);
         return results;
     }
 	
     /**
      * Obtiene el padre de un productCategoryId 
      * @param productCategoryId 
      * @return
      * @throws GenericServiceException 
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static String obtenPadreProductCate(DispatchContext dctx,LocalDispatcher dispatcher,String productCategoryId) throws GenericServiceException{
     	
     	String padreProductCategoryId = null;
     	
     	if(productCategoryId != null && !productCategoryId.isEmpty()){
 
         	Map input = FastMap.newInstance();
         	input.put("productCategoryId", productCategoryId);
         	input = dctx.getModelService("obtenProdCategoryPadreId").makeValid(input, ModelService.IN_PARAM);
         	Map tmpResult = dispatcher.runSync("obtenProdCategoryPadreId", input);
             padreProductCategoryId = (String) tmpResult.get("productCategoryIdPadre");
             
             Debug.logWarning("productCategoryId "+productCategoryId+"   PADRE  "+padreProductCategoryId, MODULE);
     		
     	}
     	
     	return padreProductCategoryId;
     	
     }	
 	
     /**
      * Metodo utilizado para obtener el productCategoryId padre a partir de uno dado
      * @param productCategoryId
      * @return productCategoryId(Padre)
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Map obtenProdCategoryPadreId(DispatchContext dctx, Map context) {
         Delegator delegator = dctx.getDelegator();
         String productCategoryId = (String) context.get("productCategoryId");
         
         String productCategoryIdPadre = null;
       
 		try {        
 			
 	    	EntityCondition condicionProd = EntityCondition.makeCondition("productCategoryId", EntityOperator.EQUALS, productCategoryId);
 	    	List<GenericValue> resultadoProdCa = delegator.findByCondition("ProductCategory", condicionProd , UtilMisc.toList("productCategoryId","primaryParentCategoryId"), null);
 	    	
 	    	if(resultadoProdCa != null && !resultadoProdCa.isEmpty()){
 	    		
 	    		productCategoryIdPadre = resultadoProdCa.get(0).getString("primaryParentCategoryId");
 	    		
 	    	}
 			
 		} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
         
         Map results = ServiceUtil.returnSuccess();
         results.put("productCategoryIdPadre", productCategoryIdPadre);
         return results;
     }
    
     /**
      * Obtiene el padre de un enumId 
      * @param enumId (Subfuente Especifica)
      * @return
      * @throws GenericServiceException 
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static String obtenPadreEnumeration(DispatchContext dctx,LocalDispatcher dispatcher,String enumId) throws GenericServiceException{
     	
     	String padreEnumId = null;
     	
     	if(enumId != null && !enumId.isEmpty()){
 
         	Map input = FastMap.newInstance();
         	input.put("enumId", enumId);
         	input = dctx.getModelService("obtenEnumIdPadre").makeValid(input, ModelService.IN_PARAM);
         	Map tmpResult = dispatcher.runSync("obtenEnumIdPadre", input);
             padreEnumId = (String) tmpResult.get("enumIdPadre");
             
             Debug.logWarning("ENUM ID "+enumId+"   PADRE  "+padreEnumId, MODULE);
     		
     	}
     	
     	return padreEnumId;
     	
     }
     
     /**
      * Metodo utilizado para obtener el enumId padre a partir de uno dado
      * @param enumId
      * @return partyId(Padre)
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Map obtenEnumIdPadre(DispatchContext dctx, Map context) {
         Delegator delegator = dctx.getDelegator();
         String enumId = (String) context.get("enumId");
         
         String enumPadre = null;
       
 		try {        
 			
 	    	EntityCondition condicionEnum = EntityCondition.makeCondition("enumId", EntityOperator.EQUALS, enumId);
 	    	List<GenericValue> resultadoEnum = delegator.findByCondition("Enumeration", condicionEnum , UtilMisc.toList("enumId","parentEnumId"), null);
 	    	
 	    	if(resultadoEnum != null && !resultadoEnum.isEmpty()){
 	    		
 	    		enumPadre = resultadoEnum.get(0).getString("parentEnumId");
 	    		
 	    	}
 			
 		} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
         
         Map results = ServiceUtil.returnSuccess();
         results.put("enumIdPadre", enumPadre);
         return results;
     }	
     
     /**
      * Obtiene el padre de un partyId
      * @param dctx
      * @param dispatcher
      * @param partyId
      * @return
      * @throws GenericServiceException
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static String obtenPadrePartyId(DispatchContext dctx,LocalDispatcher dispatcher,String partyId) throws GenericServiceException{
     	
     	String partyIdPadre = null;
     	
     	if(partyId != null && !partyId.isEmpty()){
     		
         	Map input = FastMap.newInstance();
         	input.put("partyId", partyId);
         	input = dctx.getModelService("obtenPartyIdPadre").makeValid(input, ModelService.IN_PARAM);
         	Map tmpResult = dispatcher.runSync("obtenPartyIdPadre", input);
         	partyIdPadre = (String) tmpResult.get("partyIdPadre");
             
             Debug.logWarning("PARTY ID "+partyId+"   PADRE  "+partyIdPadre, MODULE);
             
             
     	}
     	
     	return partyIdPadre;
     	
     }   
 
     /**
      * Metodo utilizado para obtener el enumId padre a partir de uno dado
      * @param enumId
      * @return partyId(Padre)
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Map obtenPartyIdPadre(DispatchContext dctx, Map context) {
         Delegator delegator = dctx.getDelegator();
         String partyId = (String) context.get("partyId");
         
         String partyIdPadre = null;
       
 		try {        
 			
 	    	EntityCondition condicionEnum = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId);
 	    	List<GenericValue> resultadoEnum = delegator.findByCondition("PartyGroup", condicionEnum , UtilMisc.toList("partyId","Parent_id"), null);
 	    	
 	    	if(resultadoEnum != null && !resultadoEnum.isEmpty()){
 	    		
 	    		partyIdPadre = resultadoEnum.get(0).getString("Parent_id");
 	    		
 	    	}
 			
 		} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
         
         Map results = ServiceUtil.returnSuccess();
         results.put("partyIdPadre", partyIdPadre);
         return results;
     }
     
     /**
      * Obtiene los periodos custom que coinciden en una fecha y una organizacion , que se encuentres abiertos
      * @param dctx
      * @param context
      * @return
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Map obtenPeriodosFecha(DispatchContext dctx, Map context){
     	Delegator delegator = dctx.getDelegator();
     	String organizationPartyId = (String) context.get("organizationPartyId");
     	Timestamp fecha = (Timestamp) context.get("fecha");
     	
     	List<GenericValue> listPeriods = FastList.newInstance();
     	
         EntityCondition conditionsPeriods = EntityCondition.makeCondition(EntityOperator.AND,
                 EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS,organizationPartyId),
                 EntityCondition.makeCondition("isClosed", EntityOperator.EQUALS,"N"),
                 EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO,fecha),
                 EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO,fecha));
 
 		try {
 			
 			listPeriods = delegator.findByCondition("CustomTimePeriod", conditionsPeriods, null, UtilMisc.toList("customTimePeriodId"));
 			
 		} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
     	
     	
         Map results = ServiceUtil.returnSuccess();
         results.put("listPeriods", listPeriods);
         return results;
     	
     }
     
     /**
      * Obtiene los productos asociados a una cuenta  
      * @param dctx
      * @param context
      * @return
      * @throws GenericEntityException 
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static Map getAuxiliarProd(DispatchContext dctx, Map context) throws GenericEntityException{
     	Delegator delegator = dctx.getDelegator();
     	String glAccountId = (String) context.get("glAccountId");
     	
     	EntityCondition condicionPrdCat = EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId);
     	List<GenericValue> resultadoPrdCat = delegator.findByCondition("GlAccountCategoryRelation", condicionPrdCat , UtilMisc.toList("glAccountId","productCategoryId"), null);
     	
         Map results = ServiceUtil.returnSuccess();
         results.put("resultadoPrdCat", resultadoPrdCat);
         return results;
     }
     
     
 	/**
 	 * Metodo que se utiliza para registrar los entries relacionados a una operacion de ingresos
 	 * @param dctx
 	 * @param dispatcher
 	 * @param context
 	 * @return
 	 * @throws GenericEntityException 
 	 * @throws GenericServiceException 
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static List<GenericValue> registraEntries(DispatchContext dctx,LocalDispatcher dispatcher,Map context,
 						String organizationPartyId,String acctgTransId,BigDecimal monto,Timestamp fecContable,
 						String acctgTransTypeId, String clasificaEco , String tipoFiscal, 
 						String idProdAbono, String idProdCargo, String idPago,String tipoClasiEco) throws GenericEntityException, GenericServiceException{
 		
 		Delegator delegator = dctx.getDelegator();
 		
         Map input = new HashMap(context);
         input.put("acctgTransTypeId", acctgTransTypeId);
         input.put("clasificaEco", clasificaEco);
         input.put("tipoFis", tipoFiscal);
         input.put("idProdAbono", idProdAbono);
         input.put("idProdCargo", idProdCargo);
         input.put("idPago",idPago);
         input.put("tipoClasiEco",tipoClasiEco);
         input = dctx.getModelService("obtenerCuentasOpDiaria").makeValid(input, ModelService.IN_PARAM);
         Map tmpResult = dispatcher.runSync("obtenerCuentasOpDiaria", input);
         Map<String,String> mapCuentas = (Map<String, String>) tmpResult.get("mapCuentas");
         
         List<GenericValue> listCuentas = UtilOperacionDiariaServices.guardaEntries(delegator, mapCuentas, 
         									acctgTransId, organizationPartyId, monto);
         
         //Aqui se guardan los datos correspondientes en la tabla AccountHistory
         
         List<GenericValue> listHistory = registrarAcctHistory(dctx, dispatcher, context, 
         			fecContable, organizationPartyId, listCuentas);
         
         Debug.logWarning("LISTA DE CUENTAS HISTORY REGISTRADAS  } "+listHistory, MODULE);
         
         List<GenericValue> listOrganization = registrarAcctOrganization(dctx, dispatcher, context,
         			organizationPartyId,monto, mapCuentas);
         
         Debug.logWarning("LISTA DE CUENTAS ORGANIZATION REGISTRADAS  } "+listOrganization, MODULE);
         
 		
         return listCuentas;
 	}
 	
     /**
      * Servicio que obtiene las cuentas a registrar en una operacion diaria 
      * @param dctx
      * @param context
      * @return
      * @throws GenericServiceException 
      * @throws ServiceException 
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static Map obtenerCuentasOpDiaria(DispatchContext dctx, Map context) throws ServiceException, GenericServiceException{
         Delegator delegator = dctx.getDelegator();
         LocalDispatcher dispatcher = dctx.getDispatcher();
         String acctgTransTypeId = (String) context.get("acctgTransTypeId");
         String tipoFis = (String) context.get("tipoFis");
         String clasificaEco = (String) context.get("clasificaEco");
         String idProdAbono = (String) context.get("idProdAbono");
         String idProdCargo = (String) context.get("idProdCargo");
         String idPago = (String) context.get("idPago");
         String tipoClasiEco = (String) context.get("tipoClasiEco");
         
         Debug.logWarning("ENTRO A obtenerCuentasOpDiaria ", MODULE);
         Debug.logWarning("acctgTransTypeId "+acctgTransTypeId, MODULE);
 		Map<String,String> mapCuentas = FastMap.newInstance();
 		
 		String campoClasi = new String();
 		String tablaClasi = new String();
 		
 		if(tipoClasiEco.equalsIgnoreCase("CRI")){
 			tablaClasi = "DataImportMatrizIng";
 			campoClasi = "cri";
 		} else {
 			tablaClasi = "DataImportMatrizEgr";
 			campoClasi = "cog";
 		}
 			
 		
         try {
         	
 			GenericValue miniGuia = delegator.findByPrimaryKey("MiniGuiaContable", UtilMisc.toMap("acctgTransTypeId", acctgTransTypeId));
 
 			
 			Debug.logWarning("miniGuia     {obtenerCuentasOpDiaria}  : "+miniGuia, MODULE);
 			
 			String glFiscalTypeIdPres = miniGuia.getString("glFiscalTypeIdPres");
 			String glFiscalTypeIdCont = miniGuia.getString("glFiscalTypeIdCont");
 			
 	    	mapCuentas.put("GlFiscalTypePresupuesto", glFiscalTypeIdPres);
 	    	mapCuentas.put("GlFiscalTypeContable", glFiscalTypeIdCont);
 			
 			//Si el tipo fiscal es de presupuesto se obtienen las cuentas
 			if(tipoFis.equalsIgnoreCase(glFiscalTypeIdPres)){
 				
 		    	mapCuentas.put("Cuenta_Cargo_Presupuesto", miniGuia.getString("cuentaCargo"));
 		    	mapCuentas.put("Cuenta_Abono_Presupuesto", miniGuia.getString("cuentaAbono"));
 		    	
 			} 
 			
 			if(tipoFis.equalsIgnoreCase(glFiscalTypeIdCont)){
 				
 		    	String referencia = miniGuia.getString("referencia");
 		    	String matrizId = miniGuia.getString("tipoMatriz");
 		    	
 		    	if(referencia.equalsIgnoreCase("M")){
 		    		
 			        EntityCondition conditions = EntityCondition.makeCondition(EntityOperator.AND,
 			                EntityCondition.makeCondition(campoClasi, EntityOperator.EQUALS,clasificaEco),
 			                EntityCondition.makeCondition("matrizId", EntityOperator.EQUALS,matrizId));
 			
 					List<GenericValue> listMatriz = delegator.findByCondition(tablaClasi, conditions, null, null);
 		    		
 					Debug.logWarning("matriz     {obtenerCuentasOpDiaria}  : "+listMatriz, MODULE);
 					
 					if(listMatriz.isEmpty()){
 						Debug.logError("Error, elemento en Matriz no existe",MODULE);
 						return UtilMessage.createAndLogServiceError("Error, elemento en Matriz no existe", MODULE);
 					} else {
 						
 						Debug.log("matriz     {obtenerCuentasOpDiaria} (0) : "+listMatriz.get(0), MODULE);
 						GenericValue matriz = listMatriz.get(0);
 						
 						String cuentaCargo = matriz.getString("cargo");
 						String cuentaAbono = matriz.getString("abono");
 						
 						if(matrizId.equalsIgnoreCase("B.1") || matrizId.equalsIgnoreCase("A.1")){
 							
 							cuentaCargo = verificarAuxiliarProducto(dctx, dispatcher, cuentaCargo, idProdCargo);
 							cuentaAbono = verificarAuxiliarProducto(dctx, dispatcher, cuentaAbono, idProdAbono);
 							
 						} else if(matrizId.equalsIgnoreCase("B.2") || matrizId.equalsIgnoreCase("A.2")){
 							
 							cuentaCargo = verificarBancos(dctx, dispatcher, cuentaCargo, idPago);
 							cuentaAbono = verificarAuxiliarProducto(dctx, dispatcher, cuentaAbono, idProdAbono);
 						}
 						
 						mapCuentas.put("Cuenta_Cargo_Contable",cuentaCargo);
 						mapCuentas.put("Cuenta_Abono_Contable", cuentaAbono);
 						
 					}
 					
 		    		
 		    	}				
 				
 			}
     	
 		} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
         
         Debug.logWarning("CUENTAS REGRESADAS [obtenerCuentasOpDiaria] "+mapCuentas, MODULE);
         
         Map results = ServiceUtil.returnSuccess();
         results.put("mapCuentas", mapCuentas);
         return results;
     }    	
 	
 	/**
 	 * Metodo que registra el GL_ACCOUNT_HISTORY
 	 * @param fechaContable
 	 * @param organizationPartyId
 	 * @param listCuentas
 	 * @throws GenericServiceException 
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static List<GenericValue> registrarAcctHistory(DispatchContext dctx,LocalDispatcher dispatcher,
 					Map context,Timestamp fechaContable, 
 					String organizationPartyId, List<GenericValue> listCuentas) throws GenericServiceException{
 		
         Map input = new HashMap(context);
         input.put("fecContable", fechaContable);
         input.put("organizationPartyId", organizationPartyId);
         input.put("listCuentas", listCuentas);
         input = dctx.getModelService("guardaAccountHistory").makeValid(input, ModelService.IN_PARAM);
         Map tmpResult = dispatcher.runSync("guardaAccountHistory", input);
 
         List<GenericValue> listSaved = (List<GenericValue>) tmpResult.get("listAccountsSaved");
 		
 		return listSaved;
 	}
 	
 	/**
 	 * Metodo que registra el GL_ACCOUNT_ORGANIZATION
 	 * @param fechaContable
 	 * @param organizationPartyId
 	 * @param listCuentas
 	 * @throws GenericServiceException 
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static List<GenericValue> registrarAcctOrganization(DispatchContext dctx,LocalDispatcher dispatcher,Map context,
 					String organizationPartyId,BigDecimal monto, Map<String,String> mapCuentas) throws GenericServiceException{
 		
         Map input = new HashMap(context);
         input.put("organizationPartyId", organizationPartyId);
         input.put("mapCuentas", mapCuentas);
         input.put("monto", monto);
         input = dctx.getModelService("guardaAccountOrganization").makeValid(input, ModelService.IN_PARAM);
         Map tmpResult = dispatcher.runSync("guardaAccountOrganization", input);
 
         List<GenericValue> listSaved = (List<GenericValue>) tmpResult.get("listAccountsSaved");
 		
 		return listSaved;
 	}		
     
     /**
      * Metodo que valida las cuentas auxiliares de los productos a partir de una cuenta dada y regresa 
      * la cuenta correspondiente al catlogo auxiliar si se encuentra ah , si no regresa la misma cuenta
      * @param dctx
      * @param dispatcher
      * @param glAccountId
      * @param productId
      * @return
      * @throws ServiceException
      * @throws GenericServiceException
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
 	public static String verificarAuxiliarProducto(DispatchContext dctx,LocalDispatcher dispatcher,String glAccountId,String productId) throws ServiceException, GenericServiceException{
     	
     	String cuentaRegresa = glAccountId;
     	
     		if(glAccountId != null && !glAccountId.isEmpty()){
     			
             	Map input = FastMap.newInstance();
             	input.put("glAccountId", glAccountId);
             	input = dctx.getModelService("getAuxiliarProd").makeValid(input, ModelService.IN_PARAM);
             	Map tmpResult = dispatcher.runSync("getAuxiliarProd", input);
             	List<GenericValue> resultados = (List<GenericValue>) tmpResult.get("resultadoPrdCat");
             	
             	Debug.logWarning("glAccountId  "+glAccountId+"  getAuxiliarProd   *[ "+resultados+"*]", MODULE);
             	
             	if(resultados != null && !resultados.isEmpty()){
             		
             		if(productId != null && !productId.isEmpty()){
             			
             			resultados.contains(glAccountId);
             			
             			//Iteramos ProductCategory
             			for (GenericValue genericValue : resultados) {
             				if(genericValue.getString("productCategoryId").equalsIgnoreCase(productId))
             					cuentaRegresa = genericValue.getString("glAccountId");
 						}
             			
             		} else {
     					Debug.logError("Debe de proporcionar el Producto",MODULE);
     					throw new ServiceException(String.format("Debe de proporcionar el Producto"));
             		}
             	} 
                 
                 Debug.logWarning("Cuenta Entrada {{ "+glAccountId+"   cuenta Salida ]{ "+cuentaRegresa, MODULE);
     			
     		}
     	
     	return cuentaRegresa;
     	
     }
     
     /**
      * Metodo que valida las cuentas auxiliares de los productos a partir de una cuenta dada y regresa 
      * la cuenta correspondiente al catlogo auxiliar si se encuentra ah , si no regresa la misma cuenta
      * @param dctx
      * @param dispatcher
      * @param glAccountId
      * @param productId
      * @return string
      * @throws ServiceException
      * @throws GenericServiceException
      */
 	public static String verificarBancos(DispatchContext dctx,LocalDispatcher dispatcher,String glAccountId,String recaudadoD) throws ServiceException, GenericEntityException{
 		Delegator delegator = dctx.getDelegator();
 		
     	String cuentaRegresa = glAccountId;
     	
     	if(glAccountId != null && !glAccountId.isEmpty()){
     		
     		GenericValue paymenthMet = delegator.findByPrimaryKey("PaymentMethod", UtilMisc.toMap("paymentMethodId",recaudadoD));
     		if(paymenthMet != null && !paymenthMet.isEmpty())
     			cuentaRegresa = paymenthMet.getString("glAccountId");
     		
     	} 
     	
     	return cuentaRegresa;
     	
     }    
     
     /**
      * Metodo que guarda todas las AcctgEntry de un mapa dado
      * @param delegator
      * @param mapCuentas
      * @param acctgTransId
      * @param organizationPartyId
      * @param monto
      * @return
      * @throws GenericEntityException
      */
     public static List<GenericValue> guardaEntries(Delegator delegator,Map<String,String> mapCuentas,
     					String acctgTransId, String organizationPartyId, BigDecimal monto) throws GenericEntityException{
     	
     	String currencyId = UtilCommon.getOrgBaseCurrency(organizationPartyId, delegator);
     	
     	List<GenericValue> listCuentas = FastList.newInstance();
         
         if(mapCuentas != null && !mapCuentas.isEmpty()){
         	
         	String cargoPres = mapCuentas.get("Cuenta_Cargo_Presupuesto");
         	String abonoPres = mapCuentas.get("Cuenta_Abono_Presupuesto");
         	String cargoCont = mapCuentas.get("Cuenta_Cargo_Contable");
         	String abonoCont = mapCuentas.get("Cuenta_Abono_Contable");
         	
         	Debug.logWarning(" cargoPres "+cargoPres, MODULE);
         	Debug.logWarning(" abonoPres "+abonoPres, MODULE);
         	Debug.logWarning(" cargoCont "+cargoCont, MODULE);
         	Debug.logWarning(" abonoCont "+abonoCont, MODULE);
         	
         	if(cargoPres != null && !cargoPres.isEmpty() && abonoPres != null && !abonoPres.isEmpty()){
         		
         		GenericValue gTransEntryPreC = GenericValue.create(delegator.getModelEntity("AcctgTransEntry"));
         		gTransEntryPreC.set("acctgTransId", acctgTransId);
         		gTransEntryPreC.set("acctgTransEntrySeqId", String.format("%05d",1));
         		gTransEntryPreC.set("acctgTransEntryTypeId", "_NA_");
         		gTransEntryPreC.set("description", "Operacin  diaria PRESUPUESTAL Abono"+acctgTransId);
         		gTransEntryPreC.set("glAccountId", cargoPres);
         		gTransEntryPreC.set("organizationPartyId", organizationPartyId);
         		gTransEntryPreC.set("amount", monto);
         		gTransEntryPreC.set("currencyUomId", currencyId);
         		gTransEntryPreC.set("debitCreditFlag", "D");
         		gTransEntryPreC.set("reconcileStatusId", "AES_NOT_RECONCILED");
         		gTransEntryPreC.set("partyId", organizationPartyId);
         		gTransEntryPreC.create();
         		
         		GenericValue gtransEntryPreA = GenericValue.create(delegator.getModelEntity("AcctgTransEntry"));
         		gtransEntryPreA.set("acctgTransId", acctgTransId);
         		gtransEntryPreA.set("acctgTransEntrySeqId", String.format("%05d",2));
         		gtransEntryPreA.set("acctgTransEntryTypeId", "_NA_");
         		gtransEntryPreA.set("description", "Operacin  diaria PRESUPUESTAL Abono "+acctgTransId);
         		gtransEntryPreA.set("glAccountId", abonoPres);
         		gtransEntryPreA.set("organizationPartyId", organizationPartyId);
         		gtransEntryPreA.set("amount", monto);
         		gtransEntryPreA.set("currencyUomId", currencyId);
         		gtransEntryPreA.set("debitCreditFlag", "C");
         		gtransEntryPreA.set("reconcileStatusId", "AES_NOT_RECONCILED");
         		gtransEntryPreA.set("partyId", organizationPartyId);	 
         		gtransEntryPreA.create();
         		
         		listCuentas.add(gTransEntryPreC);
         		listCuentas.add(gtransEntryPreA);
         	}
         	
         	if(cargoCont != null && !cargoCont.isEmpty() && abonoCont != null && !abonoCont.isEmpty()){
         		
         		GenericValue gTransEntryConC = GenericValue.create(delegator.getModelEntity("AcctgTransEntry"));
         		gTransEntryConC.set("acctgTransId", acctgTransId);
         		gTransEntryConC.set("acctgTransEntrySeqId", String.format("%05d",3));
         		gTransEntryConC.set("acctgTransEntryTypeId", "_NA_");
         		gTransEntryConC.set("description", "Operacin  diaria Contable Abono"+acctgTransId);
         		gTransEntryConC.set("glAccountId", cargoCont);
         		gTransEntryConC.set("organizationPartyId", organizationPartyId);
         		gTransEntryConC.set("amount", monto);
         		gTransEntryConC.set("currencyUomId", currencyId);
         		gTransEntryConC.set("debitCreditFlag", "D");
         		gTransEntryConC.set("reconcileStatusId", "AES_NOT_RECONCILED");
         		gTransEntryConC.set("partyId", organizationPartyId);
         		gTransEntryConC.create();
         		
         		GenericValue gTransEntryConA = GenericValue.create(delegator.getModelEntity("AcctgTransEntry"));
         		gTransEntryConA.set("acctgTransId", acctgTransId);
         		gTransEntryConA.set("acctgTransEntrySeqId", String.format("%05d",4));
         		gTransEntryConA.set("acctgTransEntryTypeId", "_NA_");
         		gTransEntryConA.set("description", "Operacin  diaria Contable Abono"+acctgTransId);
         		gTransEntryConA.set("glAccountId", abonoCont);
         		gTransEntryConA.set("organizationPartyId", organizationPartyId);
         		gTransEntryConA.set("amount", monto);
         		gTransEntryConA.set("currencyUomId", currencyId);
         		gTransEntryConA.set("debitCreditFlag", "C");
         		gTransEntryConA.set("reconcileStatusId", "AES_NOT_RECONCILED");
         		gTransEntryConA.set("partyId", organizationPartyId);
         		gTransEntryConA.create();
         		
         		listCuentas.add(gTransEntryConC);
         		listCuentas.add(gTransEntryConA);
         		
         	}
         	
         }
         
         Debug.logWarning("LISTA DE CUENTAS REGISTRADAS  } "+listCuentas, MODULE);
     	
         return listCuentas;
         
     }
     
     /**
      * Metodo que guarda los registros correspondientes en la tabla GL_ACCOUNT_HISTORY 
      * los realiza a partir de una lista de cuentas a guardar y una fecha contable
      * @param dctx
      * @param context
      * @return 
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static Map guardaAccountHistory(DispatchContext dctx, Map context) {
     	Delegator delegator = dctx.getDelegator();
     	LocalDispatcher dispatcher = dctx.getDispatcher();
     	Timestamp fechaContable = (Timestamp) context.get("fecContable");
     	String organizationPartyId = (String) context.get("organizationPartyId");
     	List<GenericValue> listAccounts = (List<GenericValue>) context.get("listCuentas");
     	List<GenericValue> listAccountsSaved = FastList.newInstance();
     	
     	try {
     	
 	    	//Se obtienen los periodos en lo que se van a guardar la(s) cuenta(s)
 	    	Map input = FastMap.newInstance();
 	    	input.put("fecha", fechaContable);
 	    	input.put("organizationPartyId", organizationPartyId);
 	    	input = dctx.getModelService("obtenPeriodosFecha").makeValid(input, ModelService.IN_PARAM);
 	    	Map tmpResult = dispatcher.runSync("obtenPeriodosFecha", input);
 	    	List<GenericValue> listPeriods = (List<GenericValue>) tmpResult.get("listPeriods");
 	    	
 	    	for (GenericValue cuentas : listAccounts) {
 	    		
 	    		String glAccountId = cuentas.getString("glAccountId");
 	    		BigDecimal monto = cuentas.getBigDecimal("amount");
 	    		String tipoMonto = cuentas.getString("debitCreditFlag");
 				
 	    		for (GenericValue customPeriod : listPeriods) {
 	    			
 	    			String customTimePeriodId = customPeriod.getString("customTimePeriodId");
 	    			
 	    			BigDecimal montoAnt = ZERO;
 	    			
 	    			//Primero buscamos si existe el registro
 	    			GenericValue actHistoryBusca = delegator.findByPrimaryKey("GlAccountHistory", 
 	    						UtilMisc.toMap("glAccountId",glAccountId,"organizationPartyId",organizationPartyId,
 	    								"customTimePeriodId",customTimePeriodId));
 	    			Debug.logWarning("actHistoryBusca   ["+actHistoryBusca+"]", MODULE);
 	    			
 	    			GenericValue accountHistory = GenericValue.create(delegator.getModelEntity("GlAccountHistory"));
 	    			accountHistory.set("glAccountId", glAccountId);
 	    			accountHistory.set("organizationPartyId", organizationPartyId);
 	    			accountHistory.set("customTimePeriodId", customTimePeriodId);
 	    			if(tipoMonto.equalsIgnoreCase("D")){
	    				montoAnt = actHistoryBusca == null ? ZERO : actHistoryBusca.getBigDecimal("postedDebits");
 	    				accountHistory.set("postedDebits", monto.add(montoAnt));
 	    			}
 	    			else {
	    				montoAnt = actHistoryBusca == null ? ZERO : actHistoryBusca.getBigDecimal("postedCredits");
 	    				accountHistory.set("postedCredits", monto.add(montoAnt));
 	    			}
 	    			
 	    			if(actHistoryBusca !=null && !actHistoryBusca.isEmpty()){
 	    				accountHistory.store();
 	    			} else {
 	    				accountHistory.create();
 	    			}
 	    			
 	    			
 	    			listAccountsSaved.add(accountHistory);
 	    			
 				}
 	    		
 			}
     	
 		} catch (GenericServiceException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
     	
         Map results = ServiceUtil.returnSuccess();
         results.put("listAccountsSaved", listAccountsSaved);
         return results;
     }
     
     /**
      * Metodo que guarda los registros correspondientes en la tabla GL_ACCOUNT_ORGANIZATION 
      * los realiza a partir de una lista de cuentas , una organizacion y un monto 
      * @param dctx
      * @param context
      * @return 
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static Map guardaAccountOrganization(DispatchContext dctx, Map context) {
     	Delegator delegator = dctx.getDelegator();
     	String organizationPartyId = (String) context.get("organizationPartyId");
     	Map<String,String> mapCuentas = (Map<String,String>) context.get("mapCuentas");
     	BigDecimal montoPrl = (BigDecimal) context.get("monto");
     	
     	List<String> listAccountId = FastList.newInstance();
     	Map<String,String> accountsNatu = FastMap.newInstance();
     	Map<String,BigDecimal> accountsOrga = FastMap.newInstance();
     	Map<String,GenericValue> accountsOrgaGen = FastMap.newInstance();
     	
     	List<GenericValue> listAccountsSaved = FastList.newInstance();
     	
     	for (String glAccountId : mapCuentas.values()) {
     		listAccountId.add(glAccountId);
     	}
     	
     	try {
     		
         	EntityCondition condicionAcc = EntityCondition.makeCondition("glAccountId", EntityOperator.IN, listAccountId);
         	List<GenericValue> resultadoAcc = delegator.findByCondition("GlAccount", condicionAcc , UtilMisc.toList("glAccountId","naturaleza"), null);
         	
         	for (GenericValue accounts : resultadoAcc) {
         		accountsNatu.put(accounts.getString("glAccountId"), accounts.getString("naturaleza"));
 			}
         	
         	EntityCondition condicionAccOr = EntityCondition.makeCondition(EntityOperator.AND,
         			EntityCondition.makeCondition("glAccountId", EntityOperator.IN, listAccountId),
         			EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));
         	List<GenericValue> resultadoAccOr = delegator.findByCondition("GlAccountOrganization", condicionAccOr , UtilMisc.toList("glAccountId","postedBalance"), null);
         	
         	
         	for (GenericValue accoutOrg : resultadoAccOr) {
         		accountsOrga.put(accoutOrg.getString("glAccountId"), (accoutOrg.getBigDecimal("postedBalance") == null ? ZERO : accoutOrg.getBigDecimal("postedBalance")));
         		accountsOrgaGen.put(accoutOrg.getString("glAccountId"),accoutOrg);
 			}
         	
         	for (Map.Entry<String, String> cuenta : mapCuentas.entrySet())
         	{
         		
         		String tipoDato = cuenta.getKey();
         		Debug.logWarning("tipoDato   "+tipoDato, MODULE);
         		if(!tipoDato.equalsIgnoreCase("GlFiscalTypePresupuesto") && !tipoDato.equalsIgnoreCase("GlFiscalTypeContable") ){
             		
             		String glAccountId = cuenta.getValue();
             		
         			//Primero buscamos si existe el registro
         			GenericValue actOrganizBusca = accountsOrgaGen.get(glAccountId);
         			
             		GenericValue accountOrgani = GenericValue.create(delegator.getModelEntity("GlAccountOrganization"));
             		accountOrgani.set("glAccountId", glAccountId);
             		accountOrgani.set("organizationPartyId", organizationPartyId);
             		
             		BigDecimal monto = ZERO;
             		String natu = null;
             		if(cuenta.getKey().contains("Cuenta_Cargo"))
             			natu = "D";
             		else
             			natu = "A";
             		
             		BigDecimal montoAux = accountsOrga.get(glAccountId) == null ? ZERO : accountsOrga.get(glAccountId);
             		monto = natu.equals(accountsNatu.get(glAccountId)) ? montoAux.add(montoPrl) : montoAux.subtract(montoPrl);
             		accountOrgani.set("postedBalance", monto);
             		
         			if(actOrganizBusca !=null && !actOrganizBusca.isEmpty()){
         				accountOrgani.store();
         			} else {
         				accountOrgani.create();
         			}        		
             		
             		listAccountsSaved.add(accountOrgani);
             		
         		}
 
         		
         	}        	
     		
     	} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		} 
     	
         Map results = ServiceUtil.returnSuccess();
         results.put("listAccountsSaved", listAccountsSaved);
         return results;
     }
 
 }
