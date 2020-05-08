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
 package org.opensixen.osgi;
 
 import java.util.List;
 
 import org.compiere.model.MInOut;
 import org.compiere.model.MInOutLine;
 import org.compiere.model.MInvoice;
 import org.compiere.model.MInvoiceLine;
 import org.compiere.model.MOrder;
 import org.compiere.model.MOrderLine;
 import org.opensixen.osgi.interfaces.IDocGenerateModelValidator;
 
 /**
  * AbstractDocGenerateModelValidator 
  * Dummy model validator.
  *
  * @author Eloy Gomez
  * Indeos Consultoria http://www.indeos.es
  */
 public class AbstractDocGenerateModelValidator implements IDocGenerateModelValidator {
 
 	/**
 	 * Return validators for this ID
 	 * @param ID
 	 * @return
 	 */
 	public static IDocGenerateModelValidator[] getDocGenerateModelValidator(String ID)	{
 		ServiceQuery query = new ServiceQuery("ID", ID);
 		List<IDocGenerateModelValidator> validators = Service.list(IDocGenerateModelValidator.class, query);
 		return validators.toArray(new IDocGenerateModelValidator[validators.size()]);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.opensixen.osgi.interfaces.IDocGenerateModelValidator#consolidateAllowed(org.compiere.model.MOrder, org.compiere.model.MInOut)
 	 */
 	@Override
 	public boolean consolidateAllowed(MOrder source, MInOut target) {
 		// TODO Auto-generated method stub
		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opensixen.osgi.interfaces.IDocGenerateModelValidator#consolidateAllowed(org.compiere.model.MOrder, org.compiere.model.MInvoice)
 	 */
 	@Override
 	public boolean consolidateAllowed(MOrder source, MInvoice target) {
 		// TODO Auto-generated method stub
		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opensixen.osgi.interfaces.IDocGenerateModelValidator#consolidateAllowed(org.compiere.model.MInOut, org.compiere.model.MInvoice)
 	 */
 	@Override
 	public boolean consolidateAllowed(MInOut source, MInvoice target) {
 		// TODO Auto-generated method stub
		return false;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.opensixen.osgi.interfaces.IDocGenerateModelValidator#getQueryOrderString()
 	 */
 	@Override
 	public String getQueryOrderString() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.opensixen.osgi.interfaces.IDocGenerateModelValidator#getQueryRestrictionString()
 	 */
 	@Override
 	public String getQueryRestrictionString() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.opensixen.osgi.interfaces.IDocGenerateModelValidator#afterCreate(org.compiere.model.MOrder, org.compiere.model.MInOut)
 	 */
 	@Override
 	public MInOut afterCreate(MOrder source, MInOut target) {
 		// TODO Auto-generated method stub
 		return target;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.opensixen.osgi.interfaces.IDocGenerateModelValidator#afterCreate(org.compiere.model.MOrder, org.compiere.model.MInvoice)
 	 */
 	@Override
 	public MInvoice afterCreate(MOrder source, MInvoice target) {
 		// TODO Auto-generated method stub
 		return target;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.opensixen.osgi.interfaces.IDocGenerateModelValidator#afterCreate(org.compiere.model.MInOut, org.compiere.model.MInvoice)
 	 */
 	@Override
 	public MInvoice afterCreate(MInOut source, MInvoice target) {
 		// TODO Auto-generated method stub
 		return target;
 	}
 
 	@Override
 	public MInOutLine afterCreateLine(MOrderLine source, MInOutLine target) {
 		return target;
 	}
 
 	@Override
 	public MInvoiceLine afterCreateLine(MOrderLine source, MInvoiceLine target) {
 		return target;
 	}
 
 	@Override
 	public MInvoiceLine afterCreateLine(MInOutLine source, MInvoiceLine target) {
 		return target;
 	}
 
 
 	@Override
 	public MInOutLine afterCreateLine(MInvoiceLine source, MInOutLine target) {
 		return target;
 	}	
 }
