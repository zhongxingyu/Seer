 package org.opentaps.dataimport.domain;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.opentaps.base.constants.StatusItemConstants;
 import org.opentaps.base.entities.AcctgTrans;
 import org.opentaps.base.entities.AcctgTransEntry;
 import org.opentaps.base.entities.AcctgTransPresupuestal;
 import org.opentaps.base.entities.DataImportPresupuestoEgreso;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.Geo;
 import org.opentaps.base.entities.GlAccountOrganization;
 import org.opentaps.base.entities.Party;
 import org.opentaps.base.entities.PartyGroup;
 import org.opentaps.base.entities.ProductCategory;
 import org.opentaps.base.entities.WorkEffort;
 import org.opentaps.domain.DomainService;
 import org.opentaps.domain.dataimport.PresupuestoEgresoDataImportRepositoryInterface;
 import org.opentaps.domain.dataimport.PresupuestoEgresoImportServiceInterface;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.foundation.entity.hibernate.Session;
 import org.opentaps.foundation.entity.hibernate.Transaction;
 import org.opentaps.foundation.infrastructure.Infrastructure;
 import org.opentaps.foundation.infrastructure.InfrastructureException;
 import org.opentaps.foundation.infrastructure.User;
 import org.opentaps.foundation.repository.RepositoryException;
 import org.opentaps.foundation.service.ServiceException;
 
 public class PresupuestoEgresoImportService extends DomainService implements
 		PresupuestoEgresoImportServiceInterface {
 	private static final String MODULE = PresupuestoEgresoImportService.class
 			.getName();
 	// session object, using to store/search pojos.
 	private Session session;
 	public String organizationPartyId;
 	public int importedRecords;
 
 	public PresupuestoEgresoImportService() {
 		super();
 	}
 
 	public PresupuestoEgresoImportService(Infrastructure infrastructure,
 			User user, Locale locale) throws ServiceException {
 		super(infrastructure, user, locale);
 	}
 
 	/** {@inheritDoc} */
 	public void setOrganizationPartyId(String organizationPartyId) {
 		this.organizationPartyId = organizationPartyId;
 	}
 
 	/** {@inheritDoc} */
 	public int getImportedRecords() {
 		return importedRecords;
 	}
 
 	public Party validaParty(
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoEgreso rowdata, String id, String campo)
 			throws RepositoryException {
 		List<Party> parties = ledger_repo.findList(Party.class,
 				ledger_repo.map(Party.Fields.externalId, id));
 		if (parties.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Egreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			Debug.log(message);
 			Debug.log("despues de message");
 			storeImportPresupuestoEgresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		PartyGroup pg = ledger_repo.findOne(PartyGroup.class, ledger_repo.map(
 				PartyGroup.Fields.partyId, parties.get(0).getPartyId()));
 
 		parties.get(0).setDescription(pg.getGroupName());
 		return parties.get(0);
 	}
 
 	public WorkEffort validaWorkEffort(
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoEgreso rowdata, String id, String campo)
 			throws RepositoryException {
 		WorkEffort act = ledger_repo.findOne(WorkEffort.class,
 				ledger_repo.map(WorkEffort.Fields.workEffortId, id));
 		if (act == null) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Egreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			storeImportPresupuestoEgresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		return act;
 	}
 
 	public ProductCategory validaProduct(
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoEgreso rowdata, String id, String tipo,
 			String campo) throws RepositoryException {
 		List<ProductCategory> products = ledger_repo.findList(
 				ProductCategory.class, ledger_repo.map(
 						ProductCategory.Fields.categoryName, id,
 						ProductCategory.Fields.productCategoryTypeId, tipo));
 		if (products.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Egreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			Debug.log(message);
 			Debug.log("despues de message");
 			storeImportPresupuestoEgresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 
 		return products.get(0);
 	}
 
 	public Geo validaGeo(
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoEgreso rowdata, String id, String campo)
 			throws RepositoryException {
 		Geo loc = ledger_repo.findOne(Geo.class,
 				ledger_repo.map(Geo.Fields.geoId, rowdata.getLoc()));
 		if (loc == null) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Egreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			storeImportPresupuestoEgresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		return loc;
 	}
 
 	public Enumeration validaEnumeration(
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoEgreso rowdata, String id, String tipo,
 			String campo) throws RepositoryException {
 		List<Enumeration> enums = ledger_repo.findList(Enumeration.class,
 				ledger_repo.map(Enumeration.Fields.sequenceId, id,
 						Enumeration.Fields.enumTypeId, tipo));
 
 		if (enums.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Egreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			storeImportPresupuestoEgresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		return enums.get(0);
 	}
 
 	public void validaVigencia(
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo,
 			DataImportPresupuestoEgreso rowdata, String campo,
 			Enumeration enumeration, Date fechaTrans)
 			throws RepositoryException {
 
 		if (!enumeration.getFechaInicio().before(fechaTrans)
 				|| !enumeration.getFechaFin().after(fechaTrans)) {
 			Debug.log("Error, " + campo + " no vigente");
 			String message = "Failed to import Presupuesto Egreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no vigente";
 			storeImportPresupuestoEgresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no vigente");
 		}
 	}
 
 	/** {@inheritDoc} */
 	public void importPresupuestoEgreso() throws ServiceException {
 		try {
 			this.session = this.getInfrastructure().getSession();
 
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo = this
 					.getDomainsDirectory().getDataImportDomain()
 					.getPresupuestoEgresoDataImportRepository();
 
 			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
 					.getLedgerDomain().getLedgerRepository();
 
 			List<DataImportPresupuestoEgreso> dataforimp = imp_repo
 					.findNotProcessesDataImportPresupuestoEgresoEntries();
 
 			int imported = 0;
 			Transaction imp_tx1 = null;
 			Transaction imp_tx2 = null;
 			Transaction imp_tx3 = null;
 			Transaction imp_tx4 = null;
 			for (DataImportPresupuestoEgreso rowdata : dataforimp) {
 				// Validaciones
 				Debug.log("Empieza bloque de validaciones");
 				Party ur = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUr(), "ur");
 				Party uo = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUo(), "uo");
 				Party ue = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUe(), "ue");
 				Enumeration fin = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getFin(), "CLAS_FUN", "fin");
 				Enumeration fun = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getFun(), "CLAS_FUN", "fun");
 				Enumeration subf = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSubf(), "CLAS_FUN", "subf");
 				WorkEffort eje = validaWorkEffort(imp_repo, ledger_repo,
 						rowdata, rowdata.getEje(), "eje");
 				WorkEffort pp = validaWorkEffort(imp_repo, ledger_repo,
 						rowdata, rowdata.getPp(), "pp");
 				WorkEffort spp = validaWorkEffort(imp_repo, ledger_repo,
 						rowdata, rowdata.getSpp(), "spp");
 				WorkEffort act = validaWorkEffort(imp_repo, ledger_repo,
 						rowdata, rowdata.getAct(), "act");
 				Enumeration tg = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getTg(), "TIPO_GASTO", "tg");
 				ProductCategory cap = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getCap(), "CA", "cap");
 				ProductCategory con = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getCon(), "CON", "con");
 				ProductCategory pg = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getPg(), "PG", "pg");
 				ProductCategory pe = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getPe(), "PE", "pe");
 				Geo ef = validaGeo(imp_repo, ledger_repo, rowdata,
 						rowdata.getEf(), "ef");
 				Geo reg = validaGeo(imp_repo, ledger_repo, rowdata,
 						rowdata.getReg(), "reg");
 				Geo mun = validaGeo(imp_repo, ledger_repo, rowdata,
 						rowdata.getMun(), "mun");
 				Geo loc = validaGeo(imp_repo, ledger_repo, rowdata,
 						rowdata.getLoc(), "loc");
 				Enumeration f = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getF(), "CLAS_FR", "f");
 				Enumeration sf = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSf(), "CLAS_FR", "sf");
 				Enumeration sfe = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSfe(), "CLAS_FR", "sfe");
 				Enumeration sec = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSec(), "CLAS_SECT", "sec");
 				Enumeration subsec = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSubsec(), "CLAS_SECT", "subsec");
 				Enumeration area = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getArea(), "CLAS_SECT", "area");
 
 				// import Presupuestos Egreso as many as possible
 				try {
 					// id maximo
 					Debug.log("Busqueda idMax");
 					String id = ledger_repo.getNextSeqId("AcctgTrans");
 					for (int mes = 1; mes < 13; mes++) {
 						imp_tx1 = null;
 						imp_tx2 = null;
 						imp_tx3 = null;
 						imp_tx4 = null;
 						AcctgTrans presupuestoEgreso = new AcctgTrans();
 						presupuestoEgreso.setDescription(rowdata.getClavePres()
 								+ "-" + mes);
 
 						// id Transaccion
 						List<AcctgTrans> trans = ledger_repo.findList(
 								AcctgTrans.class, ledger_repo.map(
 										AcctgTrans.Fields.description,
 										presupuestoEgreso.getDescription()));
 
 						if (trans.isEmpty()) {
 							Debug.log("Trans Nueva");
 							presupuestoEgreso.setAcctgTransId(id + " E"
 									+ rowdata.getCiclo() + "-" + mes);
							presupuestoEgreso.setCreatedByUserLogin(rowdata.getUsuario());
 						} else {
 							Debug.log("Trans Modif");
 							presupuestoEgreso.setAcctgTransId(trans.get(0)
 									.getAcctgTransId());
							presupuestoEgreso.setCreatedByUserLogin(trans.get(0)
									.getCreatedByUserLogin());
 
 						}
 
 						Calendar cal = Calendar.getInstance();
 						cal.set(Calendar.MONTH, mes - 1);
 						cal.set(Calendar.DAY_OF_MONTH, 1);
 
 						// Vigencias
 						validaVigencia(imp_repo, rowdata, "subf", subf,
 								cal.getTime());
 						validaVigencia(imp_repo, rowdata, "tg", tg,
 								cal.getTime());
 						validaVigencia(imp_repo, rowdata, "sfe", sfe,
 								cal.getTime());
 						validaVigencia(imp_repo, rowdata, "area", area,
 								cal.getTime());
 						Debug.log("Paso validaciones");
 						presupuestoEgreso.setTransactionDate(new Timestamp(cal
 								.getTimeInMillis()));
 						presupuestoEgreso.setIsPosted("Y");
 						presupuestoEgreso.setPostedDate(new Timestamp(cal
 								.getTimeInMillis()));
 						presupuestoEgreso.setGlFiscalTypeId("BUDGET");
 						presupuestoEgreso
 								.setAcctgTransTypeId("TPRESUPAPROBADO");
						presupuestoEgreso.setLastModifiedByUserLogin(rowdata.getUsuario());
 						presupuestoEgreso.setPartyId(ue.getPartyId());
 						switch (mes) {
 						case 1:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getEnero());
 							break;
 						case 2:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getFebrero());
 							break;
 						case 3:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getMarzo());
 							break;
 						case 4:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getAbril());
 							break;
 						case 5:
 							presupuestoEgreso
 									.setPostedAmount(rowdata.getMayo());
 							break;
 						case 6:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getJunio());
 							break;
 						case 7:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getJulio());
 							break;
 						case 8:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getAgosto());
 							break;
 						case 9:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getSeptiembre());
 							break;
 						case 10:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getOctubre());
 							break;
 						case 11:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getNoviembre());
 							break;
 						case 12:
 							presupuestoEgreso.setPostedAmount(rowdata
 									.getDiciembre());
 							break;
 						}
 						imp_tx1 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(presupuestoEgreso);
 						imp_tx1.commit();
 
 						// ACCTG_TRANS_PRESUPUESTAL
 						Debug.log("ACCTG_TRANS_PRESUPUESTAL");
 						AcctgTransPresupuestal aux = new AcctgTransPresupuestal();
 						aux.setAcctgTransId(presupuestoEgreso.getAcctgTransId());
 						aux.setCiclo(rowdata.getCiclo());
 						aux.setUnidadResponsable(ur.getPartyId());
 						aux.setDescripcionUr(ur.getDescription());
 						aux.setUnidadOrganizacional(uo.getPartyId());
 						aux.setDescripcionUo(uo.getDescription());
 						aux.setUnidadEjecutora(ue.getPartyId());
 						aux.setDescripcionUe(ue.getDescription());
 						aux.setFinalidad(fin.getEnumId());
 						aux.setDescripcionFinalidad(fin.getDescription());
 						aux.setFuncion(fun.getEnumId());
 						aux.setDescripcionFuncion(fun.getDescription());
 						aux.setSubFuncion(subf.getEnumId());
 						aux.setDescripcionSubFuncion(subf.getDescription());
 						aux.setProgramaPlan(rowdata.getEje());
 						aux.setDescripcionProgramaPlan(eje.getDescription());
 						aux.setProgramaPresupuestario(rowdata.getPp());
 						aux.setDescripcionProgramaPres(pp.getDescription());
 						aux.setSubProgramaPresupuestario(rowdata.getSpp());
 						aux.setDescripcionSubProgramaPres(spp.getDescription());
 						aux.setActividad(rowdata.getAct());
 						aux.setDescripcionActividad(act.getDescription());
 						aux.setTipoGasto(tg.getEnumId());
 						aux.setDescripcionTg(tg.getDescription());
 						aux.setCapitulo(cap.getProductCategoryId());
 						aux.setDescripcionCapitulo(cap.getDescription());
 						aux.setConcepto(con.getProductCategoryId());
 						aux.setDescripcionConcepto(con.getDescription());
 						aux.setPartidaGenerica(pg.getProductCategoryId());
 						aux.setDescripcionPg(pg.getDescription());
 						aux.setPartidaEspecifica(pe.getProductCategoryId());
 						aux.setDescripcionPe(pe.getDescription());
 						aux.setFuente(f.getEnumId());
 						aux.setDescripcionFuente(f.getDescription());
 						aux.setSubFuente(sf.getEnumId());
 						aux.setDescripcionSubfuente(sf.getDescription());
 						aux.setSubFuenteEspecifica(sfe.getEnumId());
 						aux.setDescripcionSubfuenteEspecifica(sfe
 								.getDescription());
 						aux.setEntidadFederativa(rowdata.getEf());
 						aux.setDescripcionEntFed(ef.getGeoName());
 						aux.setRegion(rowdata.getReg());
 						aux.setDescripcionRegion(reg.getGeoName());
 						aux.setMunicipio(rowdata.getMun());
 						aux.setDescripcionMunicipio(mun.getGeoName());
 						aux.setLocalidad(rowdata.getLoc());
 						aux.setDescripcionLocalidad(loc.getGeoName());
 						aux.setSector(sec.getEnumId());
 						aux.setDescripcionSector(sec.getDescription());
 						aux.setSubSector(subsec.getEnumId());
 						aux.setDescripcionSubsector(subsec.getDescription());
 						aux.setArea(area.getEnumId());
 						aux.setDescripcionArea(area.getDescription());
 						aux.setAgrupador(rowdata.getAgrupador());
 						aux.setClavePres(rowdata.getClavePres());
 						imp_tx2 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(aux);
 						imp_tx2.commit();
 
 						// C/D
 						Debug.log("C/D");
 						String seqId = "00001", flag = "D", cuenta = "8.2.2";
 
 						for (int j = 0; j < 2; j++) {
 							if (j != 0) {
 								seqId = "00002";
 								flag = "C";
 								cuenta = "8.2.1";
 							}
 							AcctgTransEntry acctgentry = new AcctgTransEntry();
 							acctgentry.setAcctgTransId(presupuestoEgreso
 									.getAcctgTransId());
 							acctgentry.setAcctgTransEntrySeqId(seqId);
 							acctgentry.setAcctgTransEntryTypeId("_NA_");
 							acctgentry.setDescription(presupuestoEgreso
 									.getDescription());
 							acctgentry.setGlAccountId(cuenta);
 							acctgentry
 									.setOrganizationPartyId(organizationPartyId);
 							acctgentry.setAmount(presupuestoEgreso
 									.getPostedAmount());
 							acctgentry.setCurrencyUomId("MXN");
 							acctgentry.setDebitCreditFlag(flag);
 							acctgentry
 									.setReconcileStatusId("AES_NOT_RECONCILED");
 							// Tags seteados.
 							acctgentry.setAcctgTagEnumId1(subf.getEnumId());
 							acctgentry.setAcctgTagEnumId2(tg.getEnumId());
 							acctgentry.setAcctgTagEnumId3(sfe.getEnumId());
 							acctgentry.setAcctgTagEnumId4(area.getEnumId());
 							imp_tx3 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(acctgentry);
 							imp_tx3.commit();
 
 							// GlAccountOrganization
 							Debug.log("Empieza GlAccountOrganization");
 							GlAccountOrganization glAccountOrganization = ledger_repo
 									.findOne(
 											GlAccountOrganization.class,
 											ledger_repo
 													.map(GlAccountOrganization.Fields.glAccountId,
 															cuenta,
 															GlAccountOrganization.Fields.organizationPartyId,
 															organizationPartyId));
 							if (glAccountOrganization.getPostedBalance() == null) {
 								glAccountOrganization
 										.setPostedBalance(presupuestoEgreso
 												.getPostedAmount());
 							} else {
 								glAccountOrganization
 										.setPostedBalance(glAccountOrganization
 												.getPostedBalance()
 												.add(presupuestoEgreso
 														.getPostedAmount()));
 							}
 							imp_tx4 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(glAccountOrganization);
 							imp_tx4.commit();
 						}
 					}
 
 					String message = "Successfully imported Presupuesto Egreso ["
 							+ rowdata.getClavePres() + "].";
 					this.storeImportPresupuestoEgresoSuccess(rowdata, imp_repo);
 					Debug.logInfo(message, MODULE);
 					imported = imported + 1;
 				} catch (Exception ex) {
 					String message = "Failed to import Presupuesto Egreso ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ ex.getMessage();
 					storeImportPresupuestoEgresoError(rowdata, message,
 							imp_repo);
 
 					// rollback all if there was an error when importing item
 					if (imp_tx1 != null) {
 						imp_tx1.rollback();
 					}
 					if (imp_tx2 != null) {
 						imp_tx2.rollback();
 					}
 					if (imp_tx3 != null) {
 						imp_tx3.rollback();
 					}
 					if (imp_tx4 != null) {
 						imp_tx4.rollback();
 					}
 
 					Debug.logError(ex, message, MODULE);
 					throw new ServiceException(ex.getMessage());
 				}
 			}
 			this.importedRecords = imported;
 
 		} catch (InfrastructureException ex) {
 			Debug.logError(ex, MODULE);
 			throw new ServiceException(ex.getMessage());
 		} catch (RepositoryException ex) {
 			Debug.logError(ex, MODULE);
 			throw new ServiceException(ex.getMessage());
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	/**
 	 * Helper method to store Presupuesto Egreso import success into
 	 * <code>DataImportPresupuestoEgreso</code> entity row.
 	 * 
 	 * @param rowdata
 	 *            item of <code>DataImportPresupuestoEgreso</code> entity that
 	 *            was successfully imported
 	 * @param imp_repo
 	 *            repository of accounting
 	 * @throws org.opentaps.foundation.repository.RepositoryException
 	 */
 	private void storeImportPresupuestoEgresoSuccess(
 			DataImportPresupuestoEgreso rowdata,
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// mark as success
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
 		rowdata.setImportError(null);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 	/**
 	 * Helper method to store Presupuesto Egreso import error into
 	 * <code>DataImportPresupuestoEgreso</code> entity row.
 	 * 
 	 * @param rowdata
 	 *            item of <code>DataImportPresupuestoEgreso</code> entity that
 	 *            was unsuccessfully imported
 	 * @param message
 	 *            error message
 	 * @param imp_repo
 	 *            repository of accounting
 	 * @throws org.opentaps.foundation.repository.RepositoryException
 	 */
 	private void storeImportPresupuestoEgresoError(
 			DataImportPresupuestoEgreso rowdata, String message,
 			PresupuestoEgresoDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// store the exception and mark as failed
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
 		rowdata.setImportError(message);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 }
