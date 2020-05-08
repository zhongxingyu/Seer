 package org.palaso.languageforge.client.lex.dashboard.presenter;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.palaso.languageforge.client.lex.common.ActivityTimeRangeType;
 import org.palaso.languageforge.client.lex.common.DashboardUpdateResultType;
 import org.palaso.languageforge.client.lex.common.EntryFieldType;
 import org.palaso.languageforge.client.lex.common.SettingTaskNameType;
 import org.palaso.languageforge.client.lex.controls.ActivityChartDataSource;
 import org.palaso.languageforge.client.lex.controls.ExtendedComboBox;
 import org.palaso.languageforge.client.lex.controls.LineChart;
 import org.palaso.languageforge.client.lex.controls.ProgressLabel;
 import org.palaso.languageforge.client.lex.dashboard.DashboardEventBus;
 import org.palaso.languageforge.client.lex.dashboard.view.DashboardMainView;
 import org.palaso.languageforge.client.lex.main.model.DashboardActivitiesDto;
 import org.palaso.languageforge.client.lex.main.service.ILexService;
 import org.palaso.languageforge.client.lex.model.ResultDto;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksDashboardSettings;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksDto;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksTaskElementDto;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.core.client.JsArrayInteger;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.inject.Inject;
 import com.googlecode.gchart.client.GChart;
 import com.googlecode.gchart.client.GChart.AnnotationLocation;
 import com.googlecode.gchart.client.GChart.Curve;
 import com.googlecode.gchart.client.GChart.LegendLocation;
 import com.googlecode.gchart.client.GChart.SymbolType;
 import com.googlecode.gchart.client.HoverParameterInterpreter;
 import com.mvp4g.client.annotation.Presenter;
 import com.mvp4g.client.presenter.BasePresenter;
 
 @Presenter(view = DashboardMainView.class)
 public class DashboardMainPresenter extends
 		BasePresenter<DashboardMainView, DashboardEventBus> {
 
 	@Inject
 	private ILexService lexService;
 
 	private JsArrayInteger chartDateArray;
 
 	private SettingTasksDashboardSettings dashboardSpeData = null;
 
 	private Timer dashboardStatePoller = null;
 
 	private boolean poller_locker = false; // before previous request
 											// responding, should not do second.
 
 	private final int POLLER_INTERVAL = 3000;
 
 	Curve entryCurve = null;
 	Curve exampleCurve = null;
 	Curve partOfSpeechCurve = null;
 	Curve definitionCurve = null;
 
 	public interface IDashboardView {
 
 		LineChart getLineChart();
 
 		HasClickHandlers getAddMoreWordsButtonClickHandlers();
 
 		HasClickHandlers getAddMorePosButtonClickHandlers();
 
 		HasClickHandlers getAddMoreMeaningsButtonClickHandlers();
 
 		HasClickHandlers getAddMoreExamplesButtonClickHandlers();
 
 		void setAddMoreWordsButtonVisible(boolean visible);
 
 		void setAddMorePosButtonVisible(boolean visible);
 
 		void setAddMoreMeaningsButtonVisible(boolean visible);
 
 		void setAddMoreExamplesButtonVisible(boolean visible);
 
 		ProgressLabel getProgressWordCount();
 
 		ProgressLabel getProgressPOS();
 
 		ProgressLabel getProgressMeanings();
 
 		ProgressLabel getProgressExamples();
 
 		ExtendedComboBox getActivityTimeRangeListBox();
 
 		HTMLPanel getUpdatingPanel();
 
 		Image getUpdatingPanelImage();
 
 		Label getUpdatingPanelLabel();
 	}
 
 	class ActivityHoverParameterInterpreter implements
 			HoverParameterInterpreter {
 		public String getHoverParameter(String paramName,
 				GChart.Curve.Point hoveredOver) {
 			if (paramName == "x") {
 				return DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT)
 						.format(new Date((long) chartDateArray
 								.get(((int) hoveredOver.getX()) - 1) * 1000));
 			} else if (paramName == "y") {
 				return String.valueOf((int) hoveredOver.getY());
 			} else if (paramName == "t") {
 				return hoveredOver.getParent().getLegendLabel();
 			}
 			return "-";
 		}
 	}
 
 	public void onMainWindowResize(ResizeEvent event) {
 		if (view.isAttached()) {
			try{
				refreshChart();
			} catch (Exception e) {
				// do nothing!
			}
 		}
 	}
 
 	public void onGoToDashboard() {
 
 		JsArray<SettingTasksTaskElementDto> tasksDto = SettingTasksDto
 				.getCurrentUserSetting().getEntries();
 		if (tasksDto.length() > 0) {
 			for (int i = 0; i < tasksDto.length(); i++) {
 				SettingTasksTaskElementDto taskElementDto = tasksDto.get(i);
 
 				if (taskElementDto.getTaskName() == SettingTaskNameType.ADDMISSINGINFO) {
 
 					if (taskElementDto.getField()
 							.equalsIgnoreCase("definition")) {
 						view.setAddMoreMeaningsButtonVisible(taskElementDto
 								.getVisible());
 					} else if (taskElementDto.getField()
 							.equalsIgnoreCase("POS")) {
 						view.setAddMorePosButtonVisible(taskElementDto
 								.getVisible());
 					} else if (taskElementDto.getField().equalsIgnoreCase(
 							"ExampleSentence")) {
 						view.setAddMoreExamplesButtonVisible(taskElementDto
 								.getVisible());
 					}
 				} else if (taskElementDto.getTaskName() == SettingTaskNameType.DICTIONARY) {
 
 					view.setAddMoreWordsButtonVisible(taskElementDto
 							.getVisible());
 				}
 			}
 		}
 
 		dashboardSpeData = getDashboardSettings();
 		switch (dashboardSpeData.getActivityTimeRange()) {
 		case 30:
 			view.getActivityTimeRangeListBox().setSelectedIndex(0);
 			break;
 		case 90:
 			view.getActivityTimeRangeListBox().setSelectedIndex(1);
 			break;
 		case 365:
 			view.getActivityTimeRangeListBox().setSelectedIndex(2);
 			break;
 		case 0:
 			view.getActivityTimeRangeListBox().setSelectedIndex(3);
 			break;
 		default:
 			view.getActivityTimeRangeListBox().setSelectedIndex(0);
 			break;
 		}
 		getDashBoardData(dashboardSpeData.getActivityTimeRange(),
 				dashboardSpeData, true);
 	}
 
 	private SettingTasksDashboardSettings getDashboardSettings() {
 		JsArray<SettingTasksTaskElementDto> tasksDto = SettingTasksDto
 				.getCurrentUserSetting().getEntries();
 		if (tasksDto.length() > 0) {
 			for (int i = 0; i < tasksDto.length(); i++) {
 				SettingTasksTaskElementDto taskElementDto = tasksDto.get(i);
 
 				if (taskElementDto.getTaskName() == SettingTaskNameType.DASHBOARD) {
 					SettingTasksDashboardSettings taskDashboardSpeData = SettingTasksDashboardSettings
 							.<SettingTasksDashboardSettings> decode(taskElementDto
 									.getTaskSpecifiedData());
 					return taskDashboardSpeData;
 				}
 			}
 		}
 		return SettingTasksDashboardSettings.getNew();
 	}
 
 	@Override
 	public void bind() {
 		super.bind();
 
 		view.getActivityTimeRangeListBox().addItem(
 				ActivityTimeRangeType.DAY_30.getValue(),
 				String.valueOf(ActivityTimeRangeType.DAY_30.ordinal()));
 		view.getActivityTimeRangeListBox().addItem(
 				ActivityTimeRangeType.DAY_90.getValue(),
 				String.valueOf(ActivityTimeRangeType.DAY_90.ordinal()));
 		view.getActivityTimeRangeListBox().addItem(
 				ActivityTimeRangeType.DAY_365.getValue(),
 				String.valueOf(ActivityTimeRangeType.DAY_365.ordinal()));
 		view.getActivityTimeRangeListBox().addItem(
 				ActivityTimeRangeType.DAY_ALL.getValue(),
 				String.valueOf(ActivityTimeRangeType.DAY_ALL.ordinal()));
 		// getDashboardSpecifiedData().getActivityTimeRange()
 		view.getActivityTimeRangeListBox().addChangeHandler(
 				new ChangeHandler() {
 
 					@Override
 					public void onChange(ChangeEvent event) {
 
 						switch (Integer.parseInt(view
 								.getActivityTimeRangeListBox().getValue(
 										view.getActivityTimeRangeListBox()
 												.getSelectedIndex()))) {
 						case 0:
 							getDashBoardData(30, dashboardSpeData, false);
 							break;
 						case 1:
 							getDashBoardData(90, dashboardSpeData, false);
 							break;
 						case 2:
 							getDashBoardData(365, dashboardSpeData, false);
 							break;
 						case 3:
 							getDashBoardData(0, dashboardSpeData, false);
 							break;
 						default:
 							getDashBoardData(30, dashboardSpeData, false);
 							break;
 						}
 
 					}
 				});
 
 		view.getAddMoreExamplesButtonClickHandlers().addClickHandler(
 				new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						eventBus.goToLexMissingInfo(EntryFieldType.EXAMPLESENTENCE);
 
 					}
 				});
 
 		view.getAddMoreMeaningsButtonClickHandlers().addClickHandler(
 				new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						eventBus.goToLexMissingInfo(EntryFieldType.DEFINITION);
 					}
 				});
 
 		view.getAddMorePosButtonClickHandlers().addClickHandler(
 				new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						eventBus.goToLexMissingInfo(EntryFieldType.POS);
 
 					}
 				});
 
 		view.getAddMoreWordsButtonClickHandlers().addClickHandler(
 				new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						eventBus.openBrowseAddMoreWord();
 					}
 				});
 		updateChartSettings();
 		if (dashboardStatePoller == null) {
 			dashboardStatePoller = new Timer() {
 				@Override
 				public void run() {
 					// only run it when dashboard is show
 					if (view.isAttached() && !poller_locker) {
 						poller_locker = true;
 						lexService
 								.getIsDashboardUpdateToolRunning(new AsyncCallback<ResultDto>() {
 
 									@Override
 									public void onSuccess(ResultDto result) {
 										DashboardUpdateResultType updateResult = DashboardUpdateResultType
 												.getFromValue(result.getCode());
 
 										switch (updateResult) {
 										case STANDBY:
 											// do nothing
 											break;
 										case RUNNING:
 											// still running
 											view.getUpdatingPanel().setVisible(
 													true);
 											break;
 										case UPDATED:
 											// finished with changes
 											view.getUpdatingPanel().setVisible(
 													false);
 											dashboardStatePoller.cancel();
 											switch (Integer
 													.parseInt(view
 															.getActivityTimeRangeListBox()
 															.getValue(
 																	view.getActivityTimeRangeListBox()
 																			.getSelectedIndex()))) {
 											case 0:
 												getDashBoardData(30,
 														dashboardSpeData, false);
 												break;
 											case 1:
 												getDashBoardData(90,
 														dashboardSpeData, false);
 												break;
 											case 2:
 												getDashBoardData(365,
 														dashboardSpeData, false);
 												break;
 											case 3:
 												getDashBoardData(0,
 														dashboardSpeData, false);
 												break;
 											default:
 												getDashBoardData(30,
 														dashboardSpeData, false);
 												break;
 											}
 											break;
 										case NOCHANGE:
 											// finished without changes
 											view.getUpdatingPanel().setVisible(
 													false);
 											dashboardStatePoller.cancel();
 											break;
 										}
 										poller_locker = false;
 									}
 
 									@Override
 									public void onFailure(Throwable caught) {
 										eventBus.handleError(caught);
 										dashboardStatePoller.cancel();
 										poller_locker = false;
 									}
 								});
 					}
 				}
 			};
 		}
 	}
 
 	private void getDashBoardData(int activityTimeRange,
 			final SettingTasksDashboardSettings dashboardSpeData,
 			final boolean triggerUpdate) {
 		lexService.getDashboardData(activityTimeRange,
 				new AsyncCallback<DashboardActivitiesDto>() {
 
 					@Override
 					public void onSuccess(DashboardActivitiesDto result) {
 						updateDashboard(result, dashboardSpeData);
 						if (triggerUpdate) {
 							dashboardStatePoller
 									.scheduleRepeating(POLLER_INTERVAL);
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						eventBus.handleError(caught);
 					}
 				});
 	}
 
 	private void updateChartSettings() {
 		view.getLineChart().setHoverParameterInterpreter(
 				new ActivityHoverParameterInterpreter());
 		view.getLineChart().setPadding("20px");
 		view.getLineChart().setPlotAreaBorderColor("#c6defa");
 		view.getLineChart().setGridColor("#c6defa");
 		view.getLineChart().getYAxis().setAxisLabel("");
 		view.getLineChart().getYAxis().setHasGridlines(true);
 		view.getLineChart().getYAxis().setTickLabelFormat("#");
 		view.getLineChart().getXAxis().setHasGridlines(true);
 		view.getLineChart().getXAxis().setTickLabelFontSize(0);
 		view.getLineChart().setLegendVisible(false);
 		view.getLineChart().setLegendLocation(LegendLocation.OUTSIDE_RIGHT);
 		view.getLineChart().setOptimizeForMemory(true);
 
 		// Entry Curve
 		view.getLineChart().addCurve();
 		entryCurve = view.getLineChart().getCurve();
 		setCurveBasicSettings(entryCurve);
 		entryCurve.getSymbol().setBorderColor("#E67345");
 		entryCurve.getSymbol().setBackgroundColor("#E8A184");
 		entryCurve.setLegendLabel("Words");
 
 		// Example Curve
 		view.getLineChart().addCurve();
 		exampleCurve = view.getLineChart().getCurve();
 		setCurveBasicSettings(exampleCurve);
 		exampleCurve.getSymbol().setBorderColor("#FF0077");
 		exampleCurve.getSymbol().setBackgroundColor("#FF61AB");
 		exampleCurve.setLegendLabel("Examples");
 
 		// PartOfSpeech Curve
 		view.getLineChart().addCurve();
 		partOfSpeechCurve = view.getLineChart().getCurve();
 		setCurveBasicSettings(partOfSpeechCurve);
 		partOfSpeechCurve.getSymbol().setBorderColor("#73FF00");
 		partOfSpeechCurve.getSymbol().setBackgroundColor("#CAFF9E");
 		partOfSpeechCurve.setLegendLabel("Part of speechs");
 
 		// Definition Curve
 		view.getLineChart().addCurve();
 		definitionCurve = view.getLineChart().getCurve();
 		setCurveBasicSettings(definitionCurve);
 		definitionCurve.getSymbol().setBorderColor("#0062FF");
 		definitionCurve.getSymbol().setBackgroundColor("#80B1FF");
 		definitionCurve.setLegendLabel("Meanings");
 
 	}
 
 	private void setCurveBasicSettings(Curve curve) {
 		curve.getSymbol().setSymbolType(SymbolType.LINE);
 		curve.getSymbol().setHovertextTemplate(
 				GChart.formatAsHovertext("<b>${x}: ${y} ${t}</b>"));
 		curve.getSymbol().setHoverLocation(AnnotationLocation.CENTER);
 		curve.getSymbol().setFillThickness(2); // px line width
 		curve.getSymbol().setBorderWidth(1);
 		curve.getSymbol().setFillSpacing(0);
 		curve.getSymbol().setWidth(2);
 		curve.getSymbol().setHeight(2);
 
 		curve.getSymbol().setHoverWidget(curve.getSymbol().getHoverWidget(), 0,
 				0);
 	}
 
 	private void refreshChart() {
 		view.getLineChart().setXChartSize(
 				view.asWidget().getOffsetWidth() - 100);
 		view.getLineChart().setYChartSize(300);
 		view.getLineChart().getXAxis().setAxisLabel("");
 		view.getLineChart().update();
 	}
 
 	private void updateDashboard(DashboardActivitiesDto dto,
 			SettingTasksDashboardSettings dashboardSpeData) {
 
 		List<ActivityChartDataSource> entryDatasource = new ArrayList<ActivityChartDataSource>();
 		List<ActivityChartDataSource> exampleDatasource = new ArrayList<ActivityChartDataSource>();
 		List<ActivityChartDataSource> partOfSpeechDatasource = new ArrayList<ActivityChartDataSource>();
 		List<ActivityChartDataSource> definitionDatasource = new ArrayList<ActivityChartDataSource>();
 
 		chartDateArray = dto.getActivityDate();
 		int maxActivity = 0;
 		for (int i = 0; i < dto.getEntryActivities().length(); i++) {
 			int actValue = dto.getEntryActivities().get(i);
 			if (maxActivity < actValue) {
 				maxActivity = actValue;
 			}
 			entryDatasource.add(new ActivityChartDataSource(i + 1, actValue));
 		}
 
 		for (int i = 0; i < dto.getExampleActivities().length(); i++) {
 			int actValue = dto.getExampleActivities().get(i);
 			if (maxActivity < actValue) {
 				maxActivity = actValue;
 			}
 			exampleDatasource.add(new ActivityChartDataSource(i + 1, actValue));
 		}
 
 		for (int i = 0; i < dto.getPartOfSpeechActivities().length(); i++) {
 			int actValue = dto.getPartOfSpeechActivities().get(i);
 			if (maxActivity < actValue) {
 				maxActivity = actValue;
 			}
 			partOfSpeechDatasource.add(new ActivityChartDataSource(i + 1,
 					actValue));
 		}
 
 		for (int i = 0; i < dto.getDefinitionActivities().length(); i++) {
 			int actValue = dto.getDefinitionActivities().get(i);
 			if (maxActivity < actValue) {
 				maxActivity = actValue;
 			}
 			definitionDatasource.add(new ActivityChartDataSource(i + 1,
 					actValue));
 		}
 
 		if (dto.getActivityDate().length() < 60) {
 			view.getLineChart().getXAxis()
 					.setTickCount(dto.getDefinitionActivities().length());
 		} else {
 			view.getLineChart().getXAxis()
 					.setTickCount(dto.getDefinitionActivities().length() / 10);
 		}
 		// change chart X/Y size depended on data
 		view.getLineChart().getYAxis()
 				.setAxisMax(maxActivity + (maxActivity * 0.1));
 		view.getLineChart().getXAxis().setAxisMin(1);
 		view.getLineChart().getXAxis()
 				.setAxisMax(dto.getDefinitionActivities().length());
 
 		entryCurve.clearPoints();
 		view.getLineChart().setDataSource(entryCurve, entryDatasource);
 
 		exampleCurve.clearPoints();
 		view.getLineChart().setDataSource(exampleCurve, exampleDatasource);
 
 		partOfSpeechCurve.clearPoints();
 		view.getLineChart().setDataSource(partOfSpeechCurve,
 				partOfSpeechDatasource);
 
 		definitionCurve.clearPoints();
 		view.getLineChart()
 				.setDataSource(definitionCurve, definitionDatasource);
 
 		// calculate percent
 		int targetWordCount = dashboardSpeData.getTargetWordCount();
 
 		int statsWordCountPercent = (dto.getStatsWordCount() * 100)
 				/ targetWordCount;
 		int statsPosPercent = statsWordCountPercent == 0 ? dto.getStatsPos()
 				: (dto.getStatsPos() * 100) / dto.getStatsWordCount();
 		int statsMeaningsPercent = statsWordCountPercent == 0 ? dto
 				.getStatsMeanings() : (dto.getStatsMeanings() * 100)
 				/ dto.getStatsWordCount();
 		int statsExamplesPercent = statsWordCountPercent == 0 ? dto
 				.getStatsExamples() : (dto.getStatsExamples() * 100)
 				/ dto.getStatsWordCount();
 
 		view.getProgressWordCount().setPercent(statsWordCountPercent);
 		view.getProgressPOS().setPercent(statsPosPercent);
 		view.getProgressMeanings().setPercent(statsMeaningsPercent);
 		view.getProgressExamples().setPercent(statsExamplesPercent);
 
 		view.getProgressWordCount().setText(statsWordCountPercent + " %");
 		view.getProgressPOS().setText(statsPosPercent + " %");
 		view.getProgressMeanings().setText(statsMeaningsPercent + " %");
 		view.getProgressExamples().setText(statsExamplesPercent + " %");
 		refreshChart();
 	}
 }
