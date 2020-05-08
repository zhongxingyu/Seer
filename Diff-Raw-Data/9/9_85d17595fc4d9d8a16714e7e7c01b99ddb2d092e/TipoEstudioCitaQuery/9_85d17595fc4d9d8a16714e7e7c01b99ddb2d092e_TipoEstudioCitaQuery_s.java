 /**
  * 
  */
 package com.isesalud.controller.query;
 
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ViewScoped;
 import javax.inject.Named;
 import com.isesalud.ejb.query.TipoEstudioCitaEjb;
 import com.isesalud.model.TipoEstudioCita;
 import com.isesalud.model.Tipocita;
 import com.isesalud.support.components.BaseQueryController;
 import com.isesalud.support.exceptions.BaseException;
 
 /**
  * @author Jesus Espinoza Hernandez
  *
  */
 @Named
 @ViewScoped
 public class TipoEstudioCitaQuery extends BaseQueryController<TipoEstudioCita> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8287350645840967475L;
 	
 	@EJB
 	private TipoEstudioCitaEjb manager;
 	
 	private TipoEstudioCita tipoEstudioCita;
 
 	@Override
 	protected List<TipoEstudioCita> getQueryList() {
 		return manager.getTipoEstudioCitaByTipo(tipoEstudioCita);
 	}
 	
 	@Override
 	protected void initComponents() throws BaseException {
 		setTipoEstudioCita(new TipoEstudioCita());
 		Tipocita tipo = new Tipocita();
 		tipo.setId(1L);
 		getTipoEstudioCita().setTipocita(tipo);
 		super.initComponents();
 	}
 	
 	@PostConstruct
 	public void loadData(){
 		setQueryListDM(getQueryList());
 	}
 
 	@Override
 	protected int getQueryRowCount() {
 		return 0;
 	}
 
 	public TipoEstudioCita getTipoEstudioCita() {
 		return tipoEstudioCita;
 	}
 	
 	public void setTipoEstudioCita(TipoEstudioCita tipoEstudioCita) {
 		this.tipoEstudioCita = tipoEstudioCita;
 	}
 }
