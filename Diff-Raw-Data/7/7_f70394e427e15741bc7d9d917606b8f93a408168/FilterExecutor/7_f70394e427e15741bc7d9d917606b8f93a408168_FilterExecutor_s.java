 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.controller.filter;
 
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import br.octahedron.cotopaxi.CotopaxiConfigView;
 import br.octahedron.cotopaxi.RequestWrapper;
 import br.octahedron.cotopaxi.controller.ModelController;
 import br.octahedron.cotopaxi.metadata.annotation.Action.ActionMetadata;
 import br.octahedron.cotopaxi.model.response.ActionResponse;
 import br.octahedron.cotopaxi.view.response.ViewResponse;
 import br.octahedron.util.reflect.InstanceHandler;
 
 /**
  * This entity is responsible by the {@link Filter} execution. Filters are executed before the
  * {@link ModelController} executes the controller and after the {@link ViewResponse} be created.
  * 
  * @see Filter
  * 
  * @author Danilo Penna Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class FilterExecutor {
 
 	private static final Logger logger = Logger.getLogger(FilterExecutor.class.getName());
 	private InstanceHandler<Filter> filters = new InstanceHandler<Filter>();
 	private CotopaxiConfigView config;
 
 	public FilterExecutor(CotopaxiConfigView config) {
 		this.config = config;
 	}
 
 	/**
 	 * Executes the <b>before</b> filters.
 	 * 
 	 * @see Filter#doBefore(RequestWrapper)
 	 */
 	public void executeFiltersBefore(ActionMetadata actionMetadata, RequestWrapper request) throws FilterException {
 		logger.info("Executing the before filters for " + request.getURL());
 		if (this.config.hasGlobalFilters()) {
 			this.executeFiltersBefore(this.config.getGlobalFilters(), request);
 		}
 		if (actionMetadata.hasFilters()) {
 			this.executeFiltersBefore(actionMetadata.getFilters(), request);
 		}
 	}
 
 	/**
 	 * Executes the <b>afeter</b> filters.
 	 * 
 	 * @see Filter#doAfter(RequestWrapper, ActionResponse)
 	 */
 	public void executeFiltersAfter(ActionMetadata actionMetadata, RequestWrapper request, ActionResponse actionResp) throws FilterException {
 		logger.info("Executing the after filters for " + request.getURL());
		if (this.config.hasGlobalFilters()) {
			this.executeFiltersAfter(this.config.getGlobalFilters(), request, actionResp);
		}
 		if (actionMetadata.hasFilters()) {
 			this.executeFiltersAfter(actionMetadata.getFilters(), request, actionResp);
 		}
 	}
 
 	/**
 	 * Execute the {@link Filter#doBefore(RequestWrapper)}
 	 */
 	private void executeFiltersBefore(Collection<Class<? extends Filter>> filters, RequestWrapper request) throws FilterException {
 		for (Class<? extends Filter> filterClass : filters) {
 			logger.fine("Executing the before filter "+ filterClass.getSimpleName() +" for " + request.getURL());
 			Filter filter = this.filters.getInstance(filterClass);
 			filter.doBefore(request);
 		}
 	}
 
 	/**
 	 * Execute the {@link Filter#doAfter(RequestWrapper, ActionResponse))}
 	 */
 	private void executeFiltersAfter(Collection<Class<? extends Filter>> filters, RequestWrapper request, ActionResponse response)
 			throws FilterException {
 		for (Class<? extends Filter> filterClass : filters) {
 			logger.fine("Executing the after filter "+ filterClass.getSimpleName() +" for " + request.getURL());
 			Filter filter = this.filters.getInstance(filterClass);
 			filter.doAfter(request, response);
 		}
 	}
 
 }
