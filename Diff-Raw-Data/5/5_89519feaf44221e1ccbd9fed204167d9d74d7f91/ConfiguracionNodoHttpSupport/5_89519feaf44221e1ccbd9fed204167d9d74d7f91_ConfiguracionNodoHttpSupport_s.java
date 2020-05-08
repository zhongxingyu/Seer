 /**
  * 22/02/2012 23:55:58 Copyright (C) 2011 Darío L. García
  * 
  * <a rel="license" href="http://creativecommons.org/licenses/by/3.0/"><img
  * alt="Creative Commons License" style="border-width:0"
  * src="http://i.creativecommons.org/l/by/3.0/88x31.png" /></a><br />
  * <span xmlns:dct="http://purl.org/dc/terms/" href="http://purl.org/dc/dcmitype/Text"
  * property="dct:title" rel="dct:type">Software</span> by <span
  * xmlns:cc="http://creativecommons.org/ns#" property="cc:attributionName">Darío García</span> is
  * licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative
  * Commons Attribution 3.0 Unported License</a>.
  */
 package net.gaia.vortex.http.api.config;
 
 import java.util.concurrent.TimeUnit;
 
 import net.gaia.vortex.http.api.ConfiguracionDeNodoRemotoHttp;
 import ar.dgarcia.http.simple.api.HttpResponseProvider;
 
 /**
  * Esta clase ofrece comportamiento común a la configuración del nodo remoto http
  * 
  * @author D. García
  */
 public abstract class ConfiguracionNodoHttpSupport implements ConfiguracionDeNodoRemotoHttp {
 
 	/**
	 * Cantidad default de segundos sin actividad
 	 */
	public static final long DEFAULT_MAX_INACTIVITY_SECONDS = TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES);
 	public static final long DEFAULT_POLLING_SECONDS = TimeUnit.SECONDS.convert(30, TimeUnit.SECONDS);
 
 	private HttpResponseProvider httpResponseProvider;
 	public static final String httpResponseProvider_FIELD = "httpResponseProvider";
 
 	private Long maximaCantidadDeSegundosSinActividad;
 	public static final String maximaCantidadDeSegundosSinActividad_FIELD = "maximaCantidadDeSegundosSinActividad";
 
 	private Long periodoDePollingEnSegundos;
 	public static final String periodoDePollingEnSegundos_FIELD = "periodoDePollingEnSegundos";
 
 	@Override
 	public Long getPeriodoDePollingEnSegundos() {
 		return periodoDePollingEnSegundos;
 	}
 
 	public void setPeriodoDePollingEnSegundos(final Long periodoDePollingEnSegundos) {
 		this.periodoDePollingEnSegundos = periodoDePollingEnSegundos;
 	}
 
 	public HttpResponseProvider getHttpResponseProvider() {
 		return httpResponseProvider;
 	}
 
 	public void setHttpResponseProvider(final HttpResponseProvider httpResponseProvider) {
 		this.httpResponseProvider = httpResponseProvider;
 	}
 
 	@Override
 	public Long getMaximaCantidadDeSegundosSinActividad() {
 		return maximaCantidadDeSegundosSinActividad;
 	}
 
 	public void setMaximaCantidadDeSegundosSinActividad(final Long maximaCantidadDeSegundosSinActividad) {
 		this.maximaCantidadDeSegundosSinActividad = maximaCantidadDeSegundosSinActividad;
 	}
 
 	/**
 	 * Inicializa esta instancia
 	 */
 	protected void initialize(final HttpResponseProvider httpResponseProvider) {
 		this.httpResponseProvider = httpResponseProvider;
 		this.maximaCantidadDeSegundosSinActividad = DEFAULT_MAX_INACTIVITY_SECONDS;
 		this.periodoDePollingEnSegundos = DEFAULT_POLLING_SECONDS;
 	}
 }
