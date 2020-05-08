 package net.sf.e4ftrace.ui.parts;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import net.sf.e4ftrace.ui.handler.RawHandler;
 import net.sf.e4ftrace.ui.model.EventImpl;
 import net.sf.e4ftrace.ui.model.TraceImpl;
 import net.sf.e4ftrace.ui.model.TraceModelImplFactory;
 import net.sf.e4ftrace.ui.provider.TsfImplProvider;
 
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.commands.ParameterizedCommand;
 import org.eclipse.e4.core.commands.ECommandService;
 import org.eclipse.e4.core.commands.EHandlerService;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.core.di.annotations.Optional;
 import org.eclipse.e4.ui.di.UIEventTopic;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.model.application.ui.basic.MPart;
 import org.eclipse.e4.ui.workbench.UIEvents;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphSelectionEvent;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
 import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 
 import org.osgi.service.event.Event;
 
 @SuppressWarnings("restriction")
 public class TimelinePart implements
 ITimeGraphSelectionListener, ITimeGraphTimeListener,
 ITimeGraphRangeListener{
 
 	private TimeGraphViewer tsfviewer;
 	private TraceModelImplFactory fact;
 	
 	
 	@Inject private ECommandService commandService;
 	@Inject private EHandlerService handlerService;
 	
 	private static SimpleDateFormat stimeformat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
 	
 	@PostConstruct
 	public void createPartControl(final Composite parent) {
 		
 		tsfviewer = new TimeGraphViewer(parent, SWT.NONE);
 		tsfviewer.setTimeGraphProvider(new TsfImplProvider());
 		
 		fact = new TraceModelImplFactory();
 		ITimeGraphEntry[] traceArr = fact.createTraces();
 		tsfviewer.setInput(traceArr);
 		tsfviewer.addSelectionListener(this);
 		tsfviewer.addRangeListener(this);
 		tsfviewer.setTimeCalendarFormat(true);
 		
 		String aboutCmd = "org.eclipse.ui.help.aboutAction";
 		String RawCmd   = "net.sf.e4ftrace.ui.command.raw";
 		
//		Command command = commandService.getCommand("net.sf.e4ftrace.ui.command.raw");
//		System.out.println("command.isDefined() : "+ command.isDefined());
//		handlerService.activateHandler("net.sf.e4ftrace.ui.command.raw", new RawHandler());
 		ParameterizedCommand cmd = commandService.createCommand(RawCmd, null);
 		System.out.println("handlerService.canExecute(cmd) : "+ handlerService.canExecute(cmd));
 		handlerService.executeHandler(cmd); 
 	}
 
 	@Override
 	public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
 		
 		if (event == null) {
 			return;
 		}
 		Object source = event.getSource();
 		if (source == null || !(source instanceof TimeGraphViewer)) {
 			return;
 		}
 
 		TimeGraphRangeUpdateEvent rEvent = event;
 		TimeGraphViewer rViewer = (TimeGraphViewer) event.getSource();
 		
 		tsfviewer.setSelectVisTimeWindow(rEvent.getStartTime(),
 				rEvent.getEndTime(), source);
 		
 	}
 
 	@Override
 	public void timeSelected(TimeGraphTimeEvent event) {
 		
 		TimeGraphViewer rViewer = (TimeGraphViewer) event.getSource();
 		
 		long selTimens = event.getTime();
 		long tms = (long) (selTimens * 1E-6);
 		Date date = new Date(tms);
 		String fDate = stimeformat.format(date);
 		String ns = formatNs(selTimens);
 
 		System.out
 				.println("TsfTraceAnalysisView.timeSelected() Selected Event: \nTime: "
 						+ event.getTime()
 						+ "\nSelected Time: "
 						+ selTimens
 						+ " " + fDate + " " + ns);
 
 		tsfviewer.setSelectedTime(event.getTime(), true);
 		
 	}
 
 	@Override
 	public void selectionChanged(TimeGraphSelectionEvent event) {
 		
 		Object source = event.getSource();
 		if (source == null || !(source instanceof TimeGraphViewer)) {
 			return;
 		}
 
 		TimeGraphViewer rViewer = (TimeGraphViewer) event.getSource();
 		
 		Object selection = event.getSelection();
 
 		if (selection instanceof EventImpl) {
 			EventImpl selEvent = (EventImpl) selection;
 			System.out
 					.println("TsfTraceAnalysisView.selectionChanged() Selected Event: \nType: "
 							+ selEvent.getType().toString()
 							+ "\nTime: "
 							+ selEvent.getTime()
 							+ "\nTrace Name: "
 							+ selEvent.getEntry().getName());
 
 			tsfviewer.setSelectedEvent(selEvent, source);
 
 		} else if (selection instanceof TraceImpl) {
 			TraceImpl selTrace = (TraceImpl) selection;
 			System.out
 					.println("TsfTraceAnalysisView.selectionChanged() Selected Trace: \nName: "
 							+ selTrace.getName().toString()
 							+ "\nClass Name: "
 							+ selTrace.getClassName());
 
 			tsfviewer.setSelection(selTrace);
 		} else {
 			System.out
 					.println("TsfTmIncubatorListener.tsfTmProcessEvent() Unexpected event source received: "
 							+ selection.getClass().getName());
 		}
 
 		
 	}
 	
 	/**
 	 * Obtains the remainder fraction on unit Seconds of the entered value in
 	 * nanoseconds. e.g. input: 1241207054171080214 ns The number of seconds can
 	 * be obtain by removing the last 9 digits: 1241207054 the fractional
 	 * portion of seconds, expressed in ns is: 171080214
 	 * 
 	 * @param v
 	 * @return
 	 */
 	public String formatNs(long v) {
 		StringBuffer str = new StringBuffer();
 		boolean neg = v < 0;
 		if (neg) {
 			v = -v;
 			str.append('-');
 		}
 
 		String strVal = String.valueOf(v);
 		if (v < 1000000000) {
 			return strVal;
 		}
 
 		// Extract the last nine digits (e.g. fraction of a S expressed in ns
 		return strVal.substring(strVal.length() - 9);
 	}
 	
 	@Inject
 	@Optional
 	public void partActivation(
 			@UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event,
 			MApplication application) {
 
 		System.out.println("Got Part");
 		
 		// Don't inject MPart and IEclipseContext! Need from root context here
 		MPart activePart = (MPart) event.getProperty(UIEvents.EventTags.ELEMENT);
 		
 		IEclipseContext context = application.getContext();
 		
 		if (activePart != null) {
 			context.set("myactivePartId", activePart.getElementId());
 		}
 	}
 }
