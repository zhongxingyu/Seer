 /**
  * 04/03/2012 19:01:25 Copyright (C) 2011 Darío L. García
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
 package net.gaia.vortex.lowlevel.impl.ruteo.jajo;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.gaia.vortex.lowlevel.impl.receptores.ReceptorVortex;
 import net.gaia.vortex.lowlevel.impl.ruteo.ControlDeRuteo;
 import net.gaia.vortex.lowlevel.impl.ruteo.OptimizadorDeRuteo;
 import net.gaia.vortex.lowlevel.impl.ruteo.ReportePerformanceRuteo;
 import net.gaia.vortex.meta.Loggers;
 import net.gaia.vortex.protocol.messages.MensajeVortex;
 
 /**
  * Esta clase representa el optimizador de ruteos que utiliza un estado binario para determinar por
  * dónde circulan los mensajes. <br>
  * Basado en la explicación de lo hecho por Javier y José
  * 
  * @author D. García
  */
 public class OptimizadorJaJo implements OptimizadorDeRuteo {
 
 	private ConcurrentHashMap<ReceptorVortex, FeedbackJajoDeRuteo> feedbacksPorReceptor;
 
 	/**
 	 * @see net.gaia.vortex.lowlevel.impl.ruteo.OptimizadorDeRuteo#debeRecibirMensajeConTag(java.lang.String,
 	 *      net.gaia.vortex.lowlevel.impl.receptores.ReceptorVortex)
 	 */
 	@Override
 	public boolean debeRecibirMensajeConTag(final String tagDelMensaje, final ReceptorVortex interesadoEnElTag) {
 		final FeedbackJajoDeRuteo feedback = feedbacksPorReceptor.get(interesadoEnElTag);
 		if (feedback == null) {
 			// Aún no tenemos feedback para decidir, por default mandamos
 			return true;
 		}
 		final DecisionDeRuteo decision = feedback.getDecisionPara(tagDelMensaje);
 		final boolean debeEnviar = decision.permiteEnvio();
 		return debeEnviar;
 	}
 
 	/**
 	 * @see net.gaia.vortex.lowlevel.impl.ruteo.OptimizadorDeRuteo#nodoAgregado(net.gaia.vortex.lowlevel.impl.receptores.ReceptorVortex)
 	 */
 	@Override
 	public void nodoAgregado(final ReceptorVortex nuevoReceptor) {
 		// En principio no hacemos nada cuando ingresa uno nuevo
 	}
 
 	/**
 	 * @see net.gaia.vortex.lowlevel.impl.ruteo.OptimizadorDeRuteo#nodoQuitado(net.gaia.vortex.lowlevel.impl.receptores.ReceptorVortex)
 	 */
 	@Override
 	public void nodoQuitado(final ReceptorVortex receptorQuitado) {
 		// Obtenemos los tags que deberían re-optimizarse
 		final FeedbackJajoDeRuteo feedback = this.feedbacksPorReceptor.remove(receptorQuitado);
		if (feedback == null) {
			// Ya fue quitado
			return;
		}
 		final Set<String> tagsInvalidadosAlQuitar = feedback.getTagsConDecisionDeEnvio();
 		if (!tagsInvalidadosAlQuitar.isEmpty()) {
 			Loggers.RUTEO.debug("OPTIM. Tags invalidados{} por receptor perdido[{}]", tagsInvalidadosAlQuitar,
 					receptorQuitado);
 		}
 
 		// Avisamos al resto para que blaquee su estado
 		final Collection<FeedbackJajoDeRuteo> values = feedbacksPorReceptor.values();
 		for (final FeedbackJajoDeRuteo feedbackAfectado : values) {
 			feedbackAfectado.invalidarTags(tagsInvalidadosAlQuitar);
 		}
 	}
 
 	/**
 	 * @see net.gaia.vortex.lowlevel.impl.ruteo.OptimizadorDeRuteo#ajustarEnBaseA(net.gaia.vortex.lowlevel.impl.ruteo.ReportePerformanceRuteo)
 	 */
 	@Override
 	public void ajustarEnBaseA(final ReportePerformanceRuteo reportePerformance) {
 		// Obtenemos los tags en los que aplica el feedback
 		final MensajeVortex mensaje = reportePerformance.getMensaje();
 		final List<String> tagsDelMensaje = mensaje.getTagsDestino();
 
 		// A los duplicados los marcamos para no envio por ser rutas redundantes
 		final ControlDeRuteo controlDeRuteo = reportePerformance.getControlDeRuteo();
 		final Set<ReceptorVortex> ruteosDuplicados = controlDeRuteo.getRuteosDuplicados();
 		registrarDecision(DecisionDeRuteo.NO_ENVIAR, ruteosDuplicados, tagsDelMensaje);
 		if (!ruteosDuplicados.isEmpty()) {
 			Loggers.RUTEO.debug("OPTIM. Receptores excluidos por duplicado: {}", ruteosDuplicados);
 		}
 
 		// A los que confirmaron consumo los marcamos como rutas validas
 		final Set<ReceptorVortex> ruteosExitosos = controlDeRuteo.getRuteosExitosos();
 		registrarDecision(DecisionDeRuteo.ENVIAR, ruteosExitosos, tagsDelMensaje);
 		if (!ruteosExitosos.isEmpty()) {
 			Loggers.RUTEO.debug("OPTIM. Receptores reforzados por consumo: {}", ruteosExitosos);
 		}
 
 		// A los que no obtuvimos respuesta los marcamos como rutas no validas
 		final Set<ReceptorVortex> ruteosPerdidos = controlDeRuteo.getRuteosPerdidos();
 		registrarDecision(DecisionDeRuteo.NO_ENVIAR, ruteosPerdidos, tagsDelMensaje);
 		if (!ruteosPerdidos.isEmpty()) {
 			Loggers.RUTEO.debug("OPTIM. Receptores excluidos por falta de consumo: {}", ruteosPerdidos);
 		}
 
 		// A los fallidos los marcamos como rutas incorrectas
 		final Set<ReceptorVortex> ruteosFallidos = controlDeRuteo.getRuteosFallidos();
 		registrarDecision(DecisionDeRuteo.NO_ENVIAR, ruteosFallidos, tagsDelMensaje);
 		if (!ruteosPerdidos.isEmpty()) {
 			Loggers.RUTEO.debug("OPTIM. Receptores excluidos por fallas: {}", ruteosFallidos);
 		}
 
 	}
 
 	/**
 	 * Registra en este optimizador la decisión a tomar con los receptores pasados
 	 * 
 	 * @param decision
 	 *            La decisión a registrar
 	 * @param receptores
 	 *            Los receptores para los que se registrará
 	 * @param tagsDelMensaje
 	 *            Los tags del mensaje usados
 	 */
 	private void registrarDecision(final DecisionDeRuteo decision, final Set<ReceptorVortex> receptores,
 			final List<String> tagsDelMensaje) {
 		for (final ReceptorVortex receptor : receptores) {
 			final FeedbackJajoDeRuteo feedback = getOrCreateFeedbackFor(receptor);
 			feedback.registrarDecision(decision, tagsDelMensaje);
 		}
 	}
 
 	/**
 	 * Crea el feedback si no existe instancia previa
 	 * 
 	 * @param receptor
 	 *            El receptor para el que se crea el feedback
 	 * @return El feedback existente o el nuevo creado
 	 */
 	private FeedbackJajoDeRuteo getOrCreateFeedbackFor(final ReceptorVortex receptor) {
 		FeedbackJajoDeRuteo feedback = this.feedbacksPorReceptor.get(receptor);
 		if (feedback == null) {
 			feedback = FeedbackJajoDeRuteo.create();
 			this.feedbacksPorReceptor.put(receptor, feedback);
 		}
 		return feedback;
 	}
 
 	public static OptimizadorJaJo create() {
 		final OptimizadorJaJo optimizador = new OptimizadorJaJo();
 		optimizador.feedbacksPorReceptor = new ConcurrentHashMap<ReceptorVortex, FeedbackJajoDeRuteo>();
 		return optimizador;
 	}
 
 }
