 package com.odea.domain;
 
 import java.util.Date;
 import java.util.Map;
 
 public class UsuarioListaHoras {
 	private Usuario usuario;
 	private int dedicacion;
 	private String sector;
 	private Map<Date,Double> diaHoras;
 	
 	/**
 	 * @return the usuario
 	 */
 	public Usuario getUsuario() {
 		return usuario;
 	}
 	/**
 	 * @param usuario the usuario to set
 	 */
 	public void setUsuario(Usuario usuario) {
 		this.usuario = usuario;
 	}
 	/**
 	 * @return the dedicacion
 	 */
 	public int getDedicacion() {
 		return dedicacion;
 	}
 	/**
 	 * @param dedicacion the dedicacion to set
 	 */
 	public void setDedicacion(int dedicacion) {
 		this.dedicacion = dedicacion;
 	}
 	/**
 	 * @return the diaHoras
 	 */
 	public Map<Date, Double> getDiaHoras() {
 		return diaHoras;
 	}
 	/**
 	 * @param diaHoras the diaHoras to set
 	 */
 	public void setDiaHoras(Map<Date, Double> diaHoras) {
 		this.diaHoras = diaHoras;
 	}
 	/**
 	 * @return the sector
 	 */
 	public String getSector() {
 		return sector;
 	}
 	/**
 	 * @param sector the sector to set
 	 */
 	public void setSector(String sector) {
 		this.sector = sector;
 	}
 	public void agregarTodosDiaHoras(Map<Date, Double> diaHoras){
 		this.diaHoras.putAll(diaHoras);
 	}
 	public void agregarDiaHoras(Date key, Double value){
 		this.diaHoras.put(key, value);
 	}
	public boolean tieneDiaMenorDedicacion(){
 		Boolean devuelve= true;
		for (Double hora : diaHoras.values()) {
			if (hora<dedicacion){
				devuelve=false;
 			}
 		}
 		return devuelve;
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "UsuarioListaHoras [usuario=" + usuario + ", dedicacion="
 				+ dedicacion + ", diaHoras=" + diaHoras + "]";
 	}
 	
 }
