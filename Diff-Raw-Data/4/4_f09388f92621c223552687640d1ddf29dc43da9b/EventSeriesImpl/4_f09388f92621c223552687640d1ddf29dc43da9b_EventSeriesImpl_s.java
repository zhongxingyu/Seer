 package ch.unibe.ese.calendar.impl;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.EventSeries;
 import ch.unibe.ese.calendar.Repetition;
 import ch.unibe.ese.calendar.Visibility;
 import ch.unibe.ese.calendar.util.DateUtils;
 
 class EventSeriesImpl extends CalendarEntry implements EventSeries {
 
 	private static final Locale locale = new Locale(
 					"de", "CH");
 
 	
 	private Map<String, CalendarEvent> exceptionalInstance = new HashMap<String, CalendarEvent>();
 	private Repetition repetition;
 	//private int instanceDurationInDays;
 	private final int dayDuration;
 
 	EventSeriesImpl(Date start, Date end, String name, Visibility visibility, 
 			Repetition repetition, EseCalendar calendar, String description) {
 		super(start, end, name, visibility, calendar, description);
 		this.repetition = repetition;
 		dayDuration = (int) (DateUtils.getStartOfDay(getEnd()).getTime() 
 				- DateUtils.getStartOfDay(getStart()).getTime());
 	}
 
 
 	public Repetition getRepetition() {
 		return repetition;
 	}
 
 
 	public Iterator<CalendarEvent> iterator(Date start) {
 		return startingEventIterator(new Date(start.getTime()-dayDuration));
 
 	}
 	
 	private Iterator<CalendarEvent> startingEventIterator(Date start) {
 		Calendar jucStart = java.util.Calendar.getInstance(locale);
 		jucStart.setTime(start);
 		DateUtils.setToStartOfDay(jucStart);
 		while (!dateMatches(jucStart.getTime())) {
 				jucStart.add(Calendar.DAY_OF_MONTH, 1);
 		}
 		long firstConsecutiveNumber = getConsecutiveNumber(jucStart.getTime());
 		Iterator<CalendarEvent> result = new EventsIterator(firstConsecutiveNumber);
 		return result;
 	}
 
 
 
 	/**
 	 * 
 	 * @dayStart Start of the day we want to get a SerialEvent of
 	 * @return true if an instance of this series starts at the specified date
 	 */
 	private boolean dateMatches(Date dayStart) {
 		Repetition repetition = getRepetition();
 		java.util.Calendar jucProtoEventStart = java.util.Calendar.getInstance(locale);
 		java.util.Calendar jucEvaluatedDay = java.util.Calendar.getInstance(locale);
 		jucEvaluatedDay.setTime(dayStart);
 		jucProtoEventStart.setTime(getStart());
 		if (repetition.equals(repetition.DAILY)) {
 			return true;
 		}
 		if (repetition.equals(repetition.WEEKLY)) {
 			int weekDayOfEventSerie = jucProtoEventStart.get(Calendar.DAY_OF_WEEK);
 			int weekDayOfDate = jucEvaluatedDay.get(Calendar.DAY_OF_WEEK);
 			if(weekDayOfEventSerie == weekDayOfDate) {
 				return true;
 			}
 		}
 		if (repetition.equals(repetition.MONTHLY)) {
 			int monthDayOfEventSerie = jucProtoEventStart.get(Calendar.DAY_OF_MONTH);
 			int monthDayOfDate = jucEvaluatedDay.get(Calendar.DAY_OF_MONTH);
 			if(monthDayOfEventSerie == monthDayOfDate) { 
 				return true;
 			}
 		}
 		return false;
 	}
 	/**
 	 * @param date
 	 * @return the consecutive number of an iteration at the day of date
 	 */
 	private long getConsecutiveNumber(Date date) {
 		Calendar startOfEvaluatedDay = java.util.Calendar.getInstance(locale);
 		startOfEvaluatedDay.setTime(date);
 		DateUtils.setToStartOfDay(startOfEvaluatedDay);
 		java.util.Calendar jucProtoEventStart = java.util.Calendar.getInstance(locale);
 		jucProtoEventStart.setTime(getStart());
 		DateUtils.setToStartOfDay(jucProtoEventStart);
 		//TODO apply more efficient algorithm
 		long i = 0;
 		if (jucProtoEventStart.equals(startOfEvaluatedDay)) {
 			return 0;
 		}
 		if (jucProtoEventStart.before(startOfEvaluatedDay)) {
 			while (jucProtoEventStart.before(startOfEvaluatedDay)) {
 				i++;
 				startOfEvaluatedDay.add(repetition.getCalendarField(), -1);
 			}
 		} else {
 			while (jucProtoEventStart.after(startOfEvaluatedDay)) {
 				i--;
 				startOfEvaluatedDay.add(repetition.getCalendarField(), 1);
 			}
 		}
 		return i;
 	}
 
 	@Override
 	public CalendarEvent getEventByConsecutiveNumber(long consecutiveNumber) {
		if (exceptionalInstance.containsKey(this.getId()+"-" + consecutiveNumber))
			return exceptionalInstance.get(this.getId()+"-" + consecutiveNumber);
 		java.util.Calendar jucEventStart = java.util.Calendar.getInstance(locale);
 		jucEventStart.setTime(getStart());
 		java.util.Calendar jucEventEnd = java.util.Calendar.getInstance(locale);
 		jucEventEnd.setTime(getEnd());
 		jucEventStart.add(repetition.getCalendarField(), (int) consecutiveNumber);
 		jucEventEnd.add(repetition.getCalendarField(), (int) consecutiveNumber);		
 		SerialEvent se = new SerialEvent(jucEventStart.getTime(), jucEventEnd.getTime(), getName(), getVisibility(), 
 				this, getCalendar(), getDescription(), consecutiveNumber);
 		return se;
 	}
 
 
 	@Override
 	public void addExceptionalInstance(String id,
 			CalendarEvent exceptionalEvent) {
 		exceptionalInstance.put(id, exceptionalEvent);
 	}
 
 
 	private class EventsIterator implements Iterator<CalendarEvent> {
 
 		private long currentConsecutiveNumber;
 		
 		public EventsIterator(long firstConsecutiveNumber) {
 			currentConsecutiveNumber = firstConsecutiveNumber;
 		}
 
 		/**
 		 * @return true as a Series lasts forever
 		 */
 		@Override
 		public boolean hasNext() {
 			return true;
 		}
 
 		@Override
 		public CalendarEvent next() {
 			CalendarEvent nextEvent = getEventByConsecutiveNumber(currentConsecutiveNumber++);
 			if(nextEvent != null) 
 				return nextEvent;
 			else return next();
 		}
 
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 	}
 
 }
