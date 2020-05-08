 package org.iplantc.de.client.analysis.widget.proxy;
 
import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.analysis.models.AnalysesAutoBeanFactory;
 import org.iplantc.de.client.analysis.models.AnalysesList;
 import org.iplantc.de.client.analysis.models.Analysis;
 import org.iplantc.de.client.analysis.views.AnalysesView;
 import org.iplantc.de.client.analysis.widget.AnalysisSearchField;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.data.client.loader.RpcProxy;
 import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
 import com.sencha.gxt.data.shared.loader.PagingLoadResult;
 import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
 
 /**
  * An RpcProxy for the loader used by the {@link AnalysisSearchField} and {@link AnalysesView} grid.
  * 
  * @author psarando
  * 
  */
 public class AnalysisRpcProxy extends RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Analysis>> {
     private final AnalysesAutoBeanFactory factory = GWT.create(AnalysesAutoBeanFactory.class);
 
     @Override
     public void load(final FilterPagingLoadConfig loadConfig,
             final AsyncCallback<PagingLoadResult<Analysis>> callback) {
         Services.ANALYSIS_SERVICE.getAnalyses(UserInfo.getInstance().getWorkspaceId(), loadConfig,
                 new AsyncCallback<String>() {
                     @Override
                     public void onSuccess(String response) {
                         AnalysesList results = AutoBeanCodex.decode(factory, AnalysesList.class,
                                 response).as();
 
                         PagingLoadResultBean<Analysis> loadResult = new PagingLoadResultBean<Analysis>(
                                 results.getAnalysisList(), results.getTotal(), loadConfig.getOffset());
 
                         callback.onSuccess(loadResult);
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                        ErrorHandler.post(I18N.DISPLAY.analysesRetrievalFailure(), caught);
                        callback.onFailure(caught);
                     }
                 });
     }
 }
