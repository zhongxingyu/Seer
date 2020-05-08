 package module.mission.domain;
 
 import module.geography.domain.Country;
 import myorg.util.ClassNameBundle;
 
 import org.joda.time.DateTime;
 import org.joda.time.Hours;
 import org.joda.time.Interval;
 
 @ClassNameBundle(bundle = "resources/MissionResources")
 public class NationalMission extends NationalMission_Base {
 
     public NationalMission(final NationalMissionProcess nationalMissionProcess, final String location,
 	    final DateTime daparture, final DateTime arrival, final String objective, final Boolean isCurrentUserAParticipant,
 	    final Boolean grantOwnerEquivalence) {
 	super();
 	setMissionProcess(nationalMissionProcess);
 	setMissionInformation(location, daparture, arrival, objective, isCurrentUserAParticipant, grantOwnerEquivalence);
     }
 
     @Override
     public Country getCountry() {
 	return MissionSystem.getInstance().getCountry();
     }
 
     @Override
     public double getFirstDayPersonelDayExpensePercentage(final PersonelExpenseItem personelExpenseItem) {
 	double result = 0.0;
 	final DateTime departure = personelExpenseItem.getStart();
 	final DateTime arrival = personelExpenseItem.getEnd();
 	final Interval interval = new Interval(departure, arrival);
 	// Check if include lunch period
 	if (overlapsMeal(interval, departure, 13)) {
 	    result += 0.25;
 	}
 	// Check if include dinner period
 	if (overlapsMeal(interval, departure, 20)) {
 	    result += 0.25;
 	}
 	final DateTime accomodationThreashold = departure.withTime(22, 0, 0, 0);
 	// Check if include accomodations
 	if (arrival.isAfter(accomodationThreashold)) {
 	    result += 0.5;
 	}
 	return result;
     }
 
     @Override
     public double getLastDayPersonelDayExpensePercentage(final PersonelExpenseItem personelExpenseItem) {
 	double result = 0.0;
 	final DateTime departure = personelExpenseItem.getStart();
 	final DateTime arrival = personelExpenseItem.getEnd();
 	if (!departure.toLocalDate().equals(arrival.toLocalDate())) {
 	    final Interval interval = new Interval(departure, arrival);
 	    // Check if include lunch period
 	    if (overlapsMeal(interval, arrival, 13)) {
 		result += 0.25;
 	    }
 	    // Check if include dinner period
 	    if (overlapsMeal(interval, arrival, 20)) {
 		result += 0.25;
 	    }
 	}
 	return result;
     }
 
     private boolean overlapsMeal(final Interval interval, final DateTime dateTime, final int hour) {
 	final Interval mealTime = new Interval(dateTime.withTime(hour, 0, 0, 0), Hours.ONE);
 	return interval.overlaps(mealTime);
     }
 
     @Override
     public int getNunberOfLunchesToDiscountOnFirstPersonelExpenseDay(final PersonelExpenseItem personelExpenseItem) {
 	return personelExpenseItem.getStart().getHourOfDay() < 14 ? 1 : 0;
     }
 
     @Override
     public int getNunberOfLunchesToDiscountOnLastPersonelExpenseDay(final PersonelExpenseItem personelExpenseItem) {
 	return personelExpenseItem.getEnd().getHourOfDay() > 12 ? 1 : 0;
     }
 
     @Override
     public String getDestinationDescription() {
 	final Country country = getCountry();
 	return country == null ? getLocation() : getLocation() + ", " + country.getName().getContent();
     }
 
     @Override
     protected boolean discountLunchDay(final DateTime dateTime) {
 	if (super.discountLunchDay(dateTime)) {
 	    if (isFirstDay(dateTime)) {
 		final Interval interval = new Interval(getDaparture(), dateTime.withTime(23, 59, 59, 0));
 		return overlapsMeal(interval, dateTime, 13);
 	    } else if (isLastDay(dateTime)) {
 		final Interval interval = new Interval(dateTime.withTime(0, 0, 0, 0), getArrival());
 		return overlapsMeal(interval, dateTime, 13);
 	    }
 	    return true;
 	}
         return false;
     }
 
     private boolean isFirstDay(final DateTime dateTime) {
 	final DateTime departure = getDaparture();
 	return matchesDay(departure, dateTime);
     }
 
     private boolean isLastDay(final DateTime dateTime) {
 	final DateTime arrival = getArrival();
 	return matchesDay(arrival, dateTime);
     }
 
     private boolean matchesDay(final DateTime dateTime1, final DateTime dateTime2) {
 	return dateTime1.getYear() == dateTime2.getYear() && dateTime1.getDayOfYear() == dateTime2.getDayOfYear();
     }
 
 }
