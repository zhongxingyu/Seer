 package eu.ist.fears.client;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.KeyboardListener;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import eu.ist.fears.client.common.State;
 import eu.ist.fears.client.common.communication.Communication;
 import eu.ist.fears.client.common.exceptions.ExceptionsTreatment;
 import eu.ist.fears.client.common.views.ViewFeatureDetailed;
 import eu.ist.fears.client.common.views.ViewFeatureResume;
 import eu.ist.fears.client.interfaceweb.FeatureResumeWidget;
 
 
 public class ListFeatures extends Composite {
 
 	protected Communication _com;
 	protected VerticalPanel _sugPanel;
 	protected String _projectName;
 	protected String _projectID;
 	protected HorizontalPanel _search;
 	protected HorizontalPanel _filter;
 	protected VerticalPanel _featuresList;
 	protected ListBox lb;
 	protected TextBox sBox;
 	protected String _actualFilter;
 	protected Hyperlink[] _filterLinks;
 	protected List<FeatureResumeWidget> _featuresViews;
 
 	public ListFeatures(String projectID, String filter){
 		_com= new Communication("service");
 		_sugPanel = new VerticalPanel();
 		_search = new HorizontalPanel();
 		_filter = new HorizontalPanel();
 		_featuresList = new VerticalPanel();
 		_featuresViews = new ArrayList<FeatureResumeWidget>();
 		_projectID = projectID;
 		_sugPanel.setStyleName("listMain");
 		_actualFilter = filter;
 		initWidget(_sugPanel);
 
 		HorizontalPanel _searchBox = new HorizontalPanel();
 		_searchBox.setStyleName("searchBox");
 		_sugPanel.add(_searchBox);
 		_search.setStyleName("search");
 		_searchBox.add(_search);
 		HorizontalPanel filterBox = new HorizontalPanel();
 		filterBox.setStyleName("filterBox");
 		filterBox.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 		_filter.setStyleName("filter");
 		filterBox.add(_filter);
 		_sugPanel.add(filterBox);
 		_featuresList.setStyleName("featuresList");
 		_sugPanel.add(_featuresList);
 
 		init();
 	}
 
 	private void init(){
 		lb = new ListBox();
 		sBox = new TextBox();
 
 		Fears.getPath().setProject("", _projectID);
 
 
 		_search.clear();
 
 		_search.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
 
 		sBox.setVisibleLength(50);
 		sBox.setStyleName("searchTextBox");
 		_search.add(sBox);
 		sBox.addKeyboardListener(new KeyboardListener(){
 
 			public void onKeyPress(Widget arg0, char arg1, int arg2){
 				if(arg1==13){
 					for(int i=1;i<_filterLinks.length;i++ )
 						_filterLinks[i].setStyleName("filters");
 					_filterLinks[0].setStyleName("currentFilter");
 
 					History.newItem("Project"+_projectID, false);
 					_com.search(_projectID, sBox.getText(), lb.getItemText(lb.getSelectedIndex()), 0,"" ,Cookies.getCookie("Fears"), getFeaturesCB);
 				}
 			}
 
 			public void onKeyDown(Widget arg0, char arg1, int arg2){}
 			public void onKeyUp(Widget arg0, char arg1, int arg2){}			
 		});
 
 
 
 
 		PushButton searchButton = new PushButton(new Image("button01.gif",0,0,105,32),new Image("button01.gif",-2,-2,105,32));
 		_search.add(searchButton);
 		searchButton.addClickListener(new ClickListener(){
 			public void onClick(Widget sender) {
 				for(int i=1;i<_filterLinks.length;i++ )
 					_filterLinks[i].setStyleName("filters");
 				_filterLinks[0].setStyleName("currentFilter");
 
 				History.newItem("Project"+_projectID, false);
 				_com.search(_projectID, sBox.getText(), lb.getItemText(lb.getSelectedIndex()), 0, "",  Cookies.getCookie("Fears"), getFeaturesCB);
 			}
 		});
 
 
 		PushButton addButton = new PushButton(new Image("button02.gif",0,0,135,32),new Image("button02.gif",-2,-2,135,32)); 
 		_search.add(addButton);
 		addButton.addClickListener(new ClickListener(){
 			public void onClick(Widget sender) {
 				History.newItem("Project" + _projectID + "&" + "addFeature");
 			}
 		});
 
 		_filter.clear();
 		_filter.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
 		HorizontalPanel filtersTab = new HorizontalPanel();
 		_filterLinks = new Hyperlink[5];
 
 		_filterLinks[0] = new Hyperlink("Todos","Project"+_projectID);
 		if(_actualFilter.equals(""))
 			_filterLinks[0] .setStyleName("currentFilter");
 		else _filterLinks[0] .setStyleName("filters");
 		filtersTab.add(_filterLinks[0]);
 		filtersTab.add(new HTML("&nbsp;&nbsp;|&nbsp;&nbsp;"));
 
 		int i=1;
 		for(State s: State.values()){
 			if(!s.toString().equals(State.Implementacao.toString())){
 				_filterLinks[i]  = new Hyperlink(s.getHTML()+"s","Project"+_projectID+"&filter"+s.toString());
 				if(_actualFilter.equals(s.toString()))
 					_filterLinks[i] .setStyleName("currentFilter");
 				else _filterLinks[i] .setStyleName("filters");		
 				filtersTab.add(_filterLinks[i]);
 			}else {
 				_filterLinks[i]  = new Hyperlink(State.Implementacao.getHTML(),true,"Project"+_projectID+"&filter"+State.Implementacao.toString());
 				if(_actualFilter.equals(State.Implementacao.toString()))
 					_filterLinks[i].setStyleName("currentFilter");
 				else _filterLinks[i].setStyleName("filters");
 				filtersTab.add(_filterLinks[i]);
 			}
 
			if(i!=4)
 				filtersTab.add(new HTML("&nbsp;&nbsp;|&nbsp;&nbsp;"));			
 
 			i++;
 		}
 
 		_filter.add(filtersTab);
 		_filter.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
 
 		lb.addItem("Ordenar por Votos");
 		lb.addItem("Ordenar por Data");
 		lb.setVisibleItemCount(1);
 		lb.addChangeListener(new ChangeListener(){
 			public void onChange(Widget arg0) {
 				_com.search(_projectID, sBox.getText(), lb.getItemText(lb.getSelectedIndex()), 0,_actualFilter ,Cookies.getCookie("Fears"), getFeaturesCB);
 			}
 
 		});
 		_filter.add(lb);
 
 	}
 
 	public void update(){
 		_com.search(_projectID, "", lb.getItemText(lb.getSelectedIndex()), 0,_actualFilter, Cookies.getCookie("Fears"), getFeaturesCB);
 		_com.getProjectName(_projectID, getProjectName);
 	}
 
 	protected void updateProjectName(String name){
 		Fears.getPath().setProject(name, _projectID);
 	}
 
 
 	public void updateFeatures(List<ViewFeatureResume> features){
 		_featuresList.clear();
 		_featuresViews.clear();
 
 
 		if(features==null || features.size() ==0 ){
 			_featuresList.add(new Label("Nao ha Sugestoes."));
 			return;
 		}
 
 		for(int i=0;i< features.size();i++){
 			FeatureResumeWidget feature =new FeatureResumeWidget((ViewFeatureResume)features.get(i), new VoteOnListCB());
 			feature.setStylePrimaryName("feature");
 			_featuresList.add(feature);
 			_featuresViews.add(feature);
 		}
 	}
 
 	public FeatureResumeWidget getFeature(String webID){
		
 		if(_featuresViews==null || _featuresViews.size() ==0 ){
 			return null;
 		}
			
 		for(FeatureResumeWidget f: _featuresViews){
 			if(f.getWebID().equals(webID))
 				return f;
 		}
 		return null;
 	}
 
 	AsyncCallback getFeaturesCB = new ExceptionsTreatment() {
 		public void onSuccess(Object result){ 
 			updateFeatures((List<ViewFeatureResume>)result);
 		}
 	};
 
 	AsyncCallback getProjectName = new ExceptionsTreatment() {
 		public void onSuccess(Object result){ 
 			updateProjectName((String)result);
 		}
 	};
 
 	protected class VoteOnListCB extends ExceptionsTreatment{
 
 		protected VoteOnListCB(){}
 
 		public void onSuccess(Object result) {
 			ViewFeatureDetailed view = (ViewFeatureDetailed) result;
 			FeatureResumeWidget f = getFeature(new Integer(view.getFeatureID()).toString());
 			if(f!=null){
 				f.update(view, false);
 			}
 			if(_featuresViews!=null && _featuresViews.size()>0){
 				Fears.getHeader().update(_projectID, _featuresViews);
 			}	
 		}
 
 	}
 
 }
