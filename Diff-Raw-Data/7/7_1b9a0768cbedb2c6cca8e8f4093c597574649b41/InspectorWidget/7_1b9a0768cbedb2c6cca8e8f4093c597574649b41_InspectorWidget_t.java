 package nz.ac.vuw.ecs.rprofs.client.views.impl;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.web.bindery.event.shared.EventBus;
 import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
 import nz.ac.vuw.ecs.rprofs.client.views.ProfilerAppView;
 import nz.ac.vuw.ecs.rprofs.client.views.ReportSelectorView;
 
 public class InspectorWidget extends FrameLayout implements ProfilerAppView {
 
 	private final Provider<ReportSelectorView> view;
 
 	@Inject
 	public InspectorWidget(PlaceController pc, EventBus bus, Provider<ReportSelectorView> view) {
 		super(FrameLayout.MAX_HEIGHT | FrameLayout.HIDE_BOTTOM, 15, 50, Unit.PCT);
 
 		this.view = view;
 		getCenter().setWidget(view.get());
 	}
 
 	@Override
 	public AcceptsOneWidget getDatasetContainer() {
 		return getTop();
 	}
 

 	@Override
	public AcceptsOneWidget getInspectorContainer() {
 		return view.get();
 	}
 
 	@Override
	public AcceptsOneWidget getReportContainer() {
 		return getBottom();
 	}
 
 }
