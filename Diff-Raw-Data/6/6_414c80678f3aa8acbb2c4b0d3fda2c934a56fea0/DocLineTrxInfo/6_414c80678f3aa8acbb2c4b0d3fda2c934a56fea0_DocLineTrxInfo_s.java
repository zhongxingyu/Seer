  /******* BEGIN LICENSE BLOCK *****
  * Versión: GPL 2.0/CDDL 1.0/EPL 1.0
  *
  * Los contenidos de este fichero están sujetos a la Licencia
  * Pública General de GNU versión 2.0 (la "Licencia"); no podrá
  * usar este fichero, excepto bajo las condiciones que otorga dicha 
  * Licencia y siempre de acuerdo con el contenido de la presente. 
  * Una copia completa de las condiciones de de dicha licencia,
  * traducida en castellano, deberá estar incluida con el presente
  * programa.
  * 
  * Adicionalmente, puede obtener una copia de la licencia en
  * http://www.gnu.org/licenses/gpl-2.0.html
  *
  * Este fichero es parte del programa opensiXen.
  *
  * OpensiXen es software libre: se puede usar, redistribuir, o
  * modificar; pero siempre bajo los términos de la Licencia 
  * Pública General de GNU, tal y como es publicada por la Free 
  * Software Foundation en su versión 2.0, o a su elección, en 
  * cualquier versión posterior.
  *
  * Este programa se distribuye con la esperanza de que sea útil,
  * pero SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
  * MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. Consulte 
  * los detalles de la Licencia Pública General GNU para obtener una
  * información más detallada. 
  *
  * TODO EL CÓDIGO PUBLICADO JUNTO CON ESTE FICHERO FORMA PARTE DEL 
  * PROYECTO OPENSIXEN, PUDIENDO O NO ESTAR GOBERNADO POR ESTE MISMO
  * TIPO DE LICENCIA O UNA VARIANTE DE LA MISMA.
  *
  * El desarrollador/es inicial/es del código es
  *  FUNDESLE (Fundación para el desarrollo del Software Libre Empresarial).
  *  Indeos Consultoria S.L. - http://www.indeos.es
  *
  * Contribuyente(s):
  *  Eloy Gómez García <eloy@opensixen.org> 
  *
  * Alternativamente, y a elección del usuario, los contenidos de este
  * fichero podrán ser usados bajo los términos de la Licencia Común del
  * Desarrollo y la Distribución (CDDL) versión 1.0 o posterior; o bajo
  * los términos de la Licencia Pública Eclipse (EPL) versión 1.0. Una 
  * copia completa de las condiciones de dichas licencias, traducida en 
  * castellano, deberán de estar incluidas con el presente programa.
  * Adicionalmente, es posible obtener una copia original de dichas 
  * licencias en su versión original en
  *  http://www.opensource.org/licenses/cddl1.php  y en  
  *  http://www.opensource.org/licenses/eclipse-1.0.php
  *
  * Si el usuario desea el uso de SU versión modificada de este fichero 
  * sólo bajo los términos de una o más de las licencias, y no bajo los 
  * de las otra/s, puede indicar su decisión borrando las menciones a la/s
  * licencia/s sobrantes o no utilizadas por SU versión modificada.
  *
  * Si la presente licencia triple se mantiene íntegra, cualquier usuario 
  * puede utilizar este fichero bajo cualquiera de las tres licencias que 
  * lo gobiernan,  GPL 2.0/CDDL 1.0/EPL 1.0.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.opensixen.grid;
 import java.math.BigDecimal;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.compiere.model.I_C_AcctSchema;
 import org.compiere.model.MClientInfo;
 import org.compiere.util.CLogger;
 import org.compiere.util.DB;
 import org.compiere.util.Env;
 import org.compiere.util.Msg;
 import org.opensixen.interfaces.DocWithAmounts;
 import org.opensixen.interfaces.DocLine;
 import org.opensixen.model.TaxCalculator;
 import org.opensixen.model.TaxLine;
 import org.opensixen.osgi.interfaces.IStatusBarTrxInfo;
 
 /**
  *  DocLineTrxInfo
  *
  * @author Eloy Gomez
  * Indeos Consultoria http://www.indeos.es
  */
 public abstract class DocLineTrxInfo implements IStatusBarTrxInfo {
 
 	private Properties m_ctx;
 	private Logger log = CLogger.getCLogger(getClass());
 	
 	private static int ctx_C_Currency_ID;
 	
 	static {
 		// Check currency
 		MClientInfo clientInfo = MClientInfo.get(Env.getCtx(), Env.getAD_Client_ID(Env.getCtx()));
 		I_C_AcctSchema acctSchema = clientInfo.getC_AcctSchema1();
 		ctx_C_Currency_ID = acctSchema.getC_Currency_ID();
 	}
 				
 	/**
 	 * Make calculations
 	 * @param ctx
 	 * @param doc
 	 * @param lines
 	 * @return
 	 */
 	protected String extractTrxInfo(Properties ctx, DocWithAmounts doc, DocLine[] lines, String AD_Message) {
 		// If new document, return empty string
 		if (doc.is_new())	{
 			return "";
 		}
 		
 		m_ctx = ctx;
 		BigDecimal totalAmt = doc.getTotalLines();
 		BigDecimal taxAmt = Env.ZERO;
 		int linesCount = lines.length;
 		String currency = doc.getCurrencyISO();
 
 
 		List<TaxLine> taxLines = TaxCalculator.calc(m_ctx, doc, lines);
 		for (TaxLine taxLine:taxLines)	{
 			taxAmt = taxAmt.add(taxLine.getTaxAmt());
 		}
 		BigDecimal grandTotal = totalAmt.add(taxAmt);
		BigDecimal convTotalAmt = totalAmt;
					
 		if (doc.getC_Currency_ID() != ctx_C_Currency_ID)	{		
 			// Delegate conversion to sql function
 			// currencyconvert(p_amount, p_curfrom_id , p_curto_id , p_convdate , p_conversiontype_id, p_client_id , p_org_id)
 			convTotalAmt = convertCurrency(doc, grandTotal);
 		}
 		
 		// Setup parameters
 		Object[] args = new Object[5];
 		args[0] = linesCount;
 		args[1] = totalAmt;
 		args[2] = grandTotal;
 		args[3] = currency;
 		args[4] = convTotalAmt;
  		
 		return formatString(args, AD_Message );
 	}
 	
 	/**
 	 * Convert currency with pgsql procedure
 	 * @param doc
 	 * @param grandTotal
 	 * @return
 	 */
 	private BigDecimal convertCurrency (DocWithAmounts doc, BigDecimal grandTotal)	{
 		String sql = "select currencyconvert(?,?,?,?,?,?,?) as conv";
 		BigDecimal convTotalAmt = Env.ZERO;
 		try {
 			PreparedStatement psmt = DB.prepareStatement(sql, null);
 			psmt.setBigDecimal(1, grandTotal);
 			psmt.setInt(2, doc.getC_Currency_ID());
 			psmt.setInt(3, ctx_C_Currency_ID);
 			psmt.setTimestamp(4, doc.getDateAcct());
 			psmt.setInt(5, doc.getC_ConversionType_ID());
 			psmt.setInt(6, doc.getAD_Client_ID());
 			psmt.setInt(7, doc.getAD_Org_ID());
 			
 			ResultSet rs = psmt.executeQuery();
 			if (rs.next())	{
 				BigDecimal conv  = rs.getBigDecimal("conv");
 				if (conv != null)	{
 					convTotalAmt = conv;										
 				}
 			}
 			
 			rs.close();
 			psmt.close();
 			rs = null;
 			psmt = null;
 		}
 		catch (SQLException e)	{
 			log.log(Level.SEVERE, "Can't convert currency.", e);
 		}
 		return convTotalAmt;
 	}
 	
 	/**
 	 * Format String for Status Bar
 	 *  @param args
 	 *	{0} - Number of lines
 	 *	{1} - Line total
 	 *	{2} - Grand total (including tax, etc.)
 	 *	{3} - Currency
 	 *	(4) - Grand total converted to local currency
 	 *
 	 * @param AD_Message {0} Line(s) - {1,number,#,##0.00} - Total: {2,number,#,##0.00} {3} = {4,number,#,##0.00}
 	 * @return
 	 */
 	private String formatString(Object[] args, String AD_Message)	{
 		
 		try
 		{
 			MessageFormat mf = new MessageFormat(Msg.getMsg(Env.getAD_Language(m_ctx), AD_Message ), Env.getLanguage(m_ctx).getLocale());
 			return mf.format(args);
 			
 		}
 		catch (Exception e)
 		{
 			log.log(Level.SEVERE, "OrderSummary=" + Msg.getMsg(Env.getAD_Language(m_ctx), AD_Message), e);
 			return "@Error@";
 		}
 	}
 	
 
 }
