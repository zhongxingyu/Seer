 /*
  *    Copyright 2013 Weald Technology Trading Limited
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.wealdtech.schedule;
 
 import static com.wealdtech.Preconditions.*;
 
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Range;
 import com.wealdtech.utils.Accessor;
 import com.wealdtech.utils.PeriodOrdering;
 
 /**
  * A Schedule is a statement of one or more {@link Occurrence} instances.
  * <p/>A simple schedule will contain a single start and end time.
  * More complex schedules will include recurrence on some set pattern,
  * resulting in multiple occurrences.  A recurring schedule may or may not
  * have an end time.
  */
 public class Schedule implements Comparable<Schedule>
 {
   public static Integer ALL = new Integer(0);
 
   private final DateTime start;
   private final Optional<DateTime> end;
 
   private final Period duration;
 
   private final Integer yearGap;
   private final Optional<ImmutableList<Integer>> monthsOfYear;
   private final Optional<ImmutableList<Integer>> weeksOfYear;
   private final Optional<ImmutableList<Integer>> weeksOfMonth;
   private final Optional<ImmutableList<Integer>> daysOfYear;
   private final Optional<ImmutableList<Integer>> daysOfMonth;
   private final Optional<ImmutableList<Integer>> daysOfWeek;
 
   // TODO hours and minutes
 
   private Schedule(final DateTime start,
                             final DateTime end,
                             final Period duration,
                             final Integer yearGap,
                             final ImmutableList<Integer> monthsOfYear,
                             final ImmutableList<Integer> weeksOfYear,
                             final ImmutableList<Integer> weeksOfMonth,
                             final ImmutableList<Integer> daysOfYear,
                             final ImmutableList<Integer> daysOfMonth,
                             final ImmutableList<Integer> daysOfWeek)
   {
     this.start = start;
     this.end = Optional.fromNullable(end);
     this.duration = duration;
     if (yearGap == null)
     {
       this.yearGap = 0;
     }
     else
     {
       this.yearGap = yearGap;
     }
     if (monthsOfYear == null || monthsOfYear.isEmpty())
     {
       this.monthsOfYear = Optional.absent();
     }
     else
     {
       this.monthsOfYear = Optional.of(ImmutableList.copyOf(Ordering.natural().immutableSortedCopy(monthsOfYear)));
     }
     if (weeksOfYear == null || weeksOfYear.isEmpty())
     {
       this.weeksOfYear = Optional.absent();
     }
     else
     {
       this.weeksOfYear = Optional.of(ImmutableList.copyOf(Ordering.natural().immutableSortedCopy(weeksOfYear)));
     }
     if (weeksOfMonth == null || weeksOfMonth.isEmpty())
     {
       this.weeksOfMonth = Optional.absent();
     }
     else
     {
       this.weeksOfMonth = Optional.of(ImmutableList.copyOf(Ordering.natural().immutableSortedCopy(weeksOfMonth)));
     }
     if (daysOfYear == null || daysOfYear.isEmpty())
     {
       this.daysOfYear = Optional.absent();
     }
     else
     {
       this.daysOfYear = Optional.of(ImmutableList.copyOf(Ordering.natural().immutableSortedCopy(daysOfYear)));
     }
     if (daysOfMonth == null || daysOfMonth.isEmpty())
     {
       this.daysOfMonth = Optional.absent();
     }
     else
     {
       this.daysOfMonth = Optional.of(ImmutableList.copyOf(Ordering.natural().immutableSortedCopy(daysOfMonth)));
     }
     if (daysOfWeek == null || daysOfWeek.isEmpty())
     {
       this.daysOfWeek = Optional.absent();
     }
     else
     {
       this.daysOfWeek = Optional.of(ImmutableList.copyOf(Ordering.natural().immutableSortedCopy(daysOfWeek)));
     }
     validate();
   }
 
   // Here we attempt to validate the schedule such that it is deterministic
   // and will always provide values
   private void validate()
   {
     checkNotNull(this.start, "Schedule requires a start");
 
     checkNotNull(this.duration, "Schedule requires a duration");
 
     checkState(this.yearGap >= 0, "Year gap cannot be negative");
 
     checkState(this.daysOfYear.isPresent() || this.daysOfMonth.isPresent() || this.daysOfWeek.isPresent(), "Schedule requires a day");
 
     if (this.daysOfWeek.isPresent())
     {
       checkState(Collections2.filter(this.daysOfWeek.get(), Range.<Integer>lessThan(0)).isEmpty(), "Days of week must not contain negative values");
       checkState(Collections2.filter(this.daysOfWeek.get(), Range.<Integer>greaterThan(7)).isEmpty(), "Days of week must not contain values greater than 7");
       checkState((this.weeksOfMonth.isPresent() && this.monthsOfYear.isPresent()) || this.weeksOfYear.isPresent(), "Schedule not complete");
       checkState(!(this.daysOfMonth.isPresent() && this.daysOfYear.isPresent()), "Schedule cannot have multiple day items");
       checkState(!(this.weeksOfMonth.isPresent() && this.weeksOfYear.isPresent()), "Schedule cannot have multiple week items");
     }
 
     if (this.daysOfMonth.isPresent())
     {
       checkState(Collections2.filter(this.daysOfMonth.get(), Range.<Integer>lessThan(0)).isEmpty(), "Days of month must not contain negative values");
       checkState(Collections2.filter(this.daysOfMonth.get(), Range.<Integer>greaterThan(31)).isEmpty(), "Days of month must not contain values greater than 31");
       checkState(this.monthsOfYear.isPresent(), "Schedule not complete");
       checkState(!(this.daysOfWeek.isPresent() && this.daysOfYear.isPresent()), "Schedule cannot have multiple day items");
     }
 
     if (this.daysOfYear.isPresent())
     {
       checkState(Collections2.filter(this.daysOfYear.get(), Range.<Integer>lessThan(0)).isEmpty(), "Days of year must not contain negative values");
       checkState(Collections2.filter(this.daysOfYear.get(), Range.<Integer>greaterThan(366)).isEmpty(), "Days of year must not contain values greater than 366");
       checkState(!(this.daysOfWeek.isPresent() && this.daysOfMonth.isPresent()), "Schedule cannot have multiple day items");
     }
 
     if (this.weeksOfMonth.isPresent())
     {
       checkState(Collections2.filter(this.weeksOfMonth.get(), Range.<Integer>greaterThan(6)).isEmpty(), "Weeks of month must not contain values greater than 6");
     }
 
     if (this.weeksOfYear.isPresent())
     {
       checkState(Collections2.filter(this.weeksOfYear.get(), Range.<Integer>greaterThan(53)).isEmpty(), "Weeks of year must not contain values greater than 53");
     }
 
     if (this.monthsOfYear.isPresent())
     {
       checkState(Collections2.filter(this.monthsOfYear.get(), Range.<Integer>greaterThan(12)).isEmpty(), "Months of year must not contain values greater than 12");
     }
 
     checkState(isAScheduleStart(this.start), "Start date is not a valid schedule start date");
   }
 
   /**
    * Returns {@code true} if this schedule terminates <em>i.e.</em> it has an end date
    * @return {@code true} if the schedule terminates
    */
   public boolean terminates()
   {
     return (this.end.isPresent());
   }
 
   /**
    * Provide an {@link Accessor} for this schedule.
    * @return an accessor
    */
   public Accessor<Occurrence, DateTime> accessor()
   {
     return new ScheduleAccessor(this);
   }
 
   /**
    * Confirm if a given date/time is a valid start time for this schedule
    * @param datetime the date/time to check
    * @return {@code true} if valid, {@code false} if not
    */
   public boolean isAScheduleStart(final DateTime datetime)
   {
     // Check time
     if ((datetime.getMinuteOfHour() != this.start.getMinuteOfHour()) ||
         (datetime.getHourOfDay() != this.start.getHourOfDay()))
     {
       return false;
     }
 
     if (this.daysOfWeek.isPresent() && !this.daysOfWeek.get().contains(Schedule.ALL))
     {
       if (!this.daysOfWeek.get().contains(datetime.getDayOfWeek()))
       {
         return false;
       }
     }
 
     if (this.daysOfMonth.isPresent() && !this.daysOfMonth.get().contains(Schedule.ALL))
     {
       if (!this.daysOfMonth.get().contains(datetime.getDayOfMonth()))
       {
         return false;
       }
     }
 
     if (this.daysOfYear.isPresent() && !this.daysOfYear.get().contains(Schedule.ALL))
     {
       if (!this.daysOfYear.get().contains(datetime.getDayOfYear()))
       {
         return false;
       }
     }
 
     if (this.weeksOfMonth.isPresent() && !this.weeksOfMonth.get().contains(Schedule.ALL))
     {
       // FIXME work this one out
     }
 
     if (this.weeksOfYear.isPresent() && !this.weeksOfYear.get().contains(Schedule.ALL))
     {
       // FIXME broken
       if (!this.weeksOfYear.get().contains(datetime.getWeekOfWeekyear()))
       {
         return false;
       }
     }
 
     // All checks passed
     return true;
   }
 
   public DateTime getStart()
   {
     return this.start;
   }
 
   public Optional<DateTime> getEnd()
   {
     return this.end;
   }
 
   public Period getDuration()
   {
     return this.duration;
   }
 
   public Integer getYearGap()
   {
     return this.yearGap;
   }
 
   public Optional<ImmutableList<Integer>> getMonthsOfYear()
   {
     return this.monthsOfYear;
   }
 
   public Optional<ImmutableList<Integer>> getWeeksOfYear()
   {
     return this.weeksOfYear;
   }
 
   public Optional<ImmutableList<Integer>> getWeeksOfMonth()
   {
     return this.weeksOfMonth;
   }
 
   public Optional<ImmutableList<Integer>> getDaysOfYear()
   {
     return this.daysOfYear;
   }
 
   public Optional<ImmutableList<Integer>> getDaysOfMonth()
   {
     return this.daysOfMonth;
   }
 
   public Optional<ImmutableList<Integer>> getDaysOfWeek()
   {
     return this.daysOfWeek;
   }
 
   /**
    * Calculate the real week of the year, with the first week
    * of the year being the week in
    * @param datetime
    * @return
    */
   public static int getRealWeekOfYear(final DateTime datetime)
   {
     int weekOfYear;
     int offset = 0;
     int weeksInYear = 52;
     if (datetime.withDayOfMonth(1).withMonthOfYear(1).weekOfWeekyear().get() > 1)
     {
       // This is a year which had less than four days in its first real week.  Need
       // to compensate by adding 1 to the normal week values generated
       offset = 1;
       weeksInYear++;
     }
     if (datetime.dayOfYear().get() < 4)
     {
       weekOfYear = 1;
     }
     else
     {
       weekOfYear = datetime.weekOfWeekyear().get() + offset;
     }
 
     if (datetime.monthOfYear().get() == 12 && datetime.dayOfMonth().get() > 27)
     {
       if (datetime.withDayOfMonth(31).withMonthOfYear(12).weekOfWeekyear().get() == 1)
       {
         // This is a year which had less than four days in its last real week.  Need
         // to compensate by resetting week number if last week of the year
         weeksInYear++;
        if (weekOfYear == 1)
         {
           weekOfYear = weeksInYear;
         }
       }
     }
     return weekOfYear;
   }
 
   public static int getRealWeekOfMonth(final DateTime datetime, final int month)
   {
     return 0;
   }
 
   // Standard object methods follow
   @Override
   public String toString()
   {
     return Objects.toStringHelper(this)
                   .add("start", this.getStart())
                   .add("end", this.getEnd())
                   .add("duration", this.getDuration())
                   .add("yearsGap", this.getYearGap())
                   .add("monthsOfYear", this.getMonthsOfYear())
                   .add("weeksOfYear", this.getWeeksOfYear())
                   .add("weeksOfMonth", this.getWeeksOfMonth())
                   .add("daysOfYear", this.getDaysOfYear())
                   .add("daysOfMonth", this.getDaysOfMonth())
                   .add("daysOfWeek", this.getDaysOfWeek())
                   .omitNullValues()
                   .toString();
   }
 
   @Override
   public boolean equals(final Object that)
   {
     return that instanceof Schedule && this.compareTo((Schedule)that) == 0;
   }
 
   @Override
   public int hashCode()
   {
     return Objects.hashCode(this.getStart(),
                             this.getEnd(),
                             this.getDuration(),
                             this.getYearGap(),
                             this.getMonthsOfYear(),
                             this.getWeeksOfYear(),
                             this.getWeeksOfMonth(),
                             this.getDaysOfYear(),
                             this.getDaysOfMonth(),
                             this.getDaysOfWeek());
   }
 
   @Override
   public int compareTo(final Schedule that)
   {
     return ComparisonChain.start()
                           .compare(this.getStart(), that.getStart())
                           .compare(this.getEnd().orNull(), that.getEnd().orNull(), Ordering.natural().nullsFirst())
                           .compare(this.getDuration(), that.getDuration(), new PeriodOrdering().nullsFirst())
                           .compare(this.getYearGap(), that.getYearGap())
                           .compare(this.getMonthsOfYear().orNull(), that.getMonthsOfYear().orNull(), Ordering.<Integer>natural().lexicographical().nullsFirst())
                           .compare(this.getWeeksOfYear().orNull(), that.getWeeksOfYear().orNull(), Ordering.<Integer>natural().lexicographical().nullsFirst())
                           .compare(this.getWeeksOfMonth().orNull(), that.getWeeksOfMonth().orNull(), Ordering.<Integer>natural().lexicographical().nullsFirst())
                           .compare(this.getDaysOfYear().orNull(), that.getDaysOfYear().orNull(), Ordering.<Integer>natural().lexicographical().nullsFirst())
                           .compare(this.getDaysOfMonth().orNull(), that.getDaysOfMonth().orNull(), Ordering.<Integer>natural().lexicographical().nullsFirst())
                           .compare(this.getDaysOfWeek().orNull(), that.getDaysOfWeek().orNull(), Ordering.<Integer>natural().lexicographical().nullsFirst())
                           .result();
   }
 
   public static class Builder
   {
     private transient DateTime start;
     private transient DateTime end;
     private transient Period duration;
     private transient Integer yearGap;
     private transient ImmutableList<Integer> monthsOfYear;
     private transient ImmutableList<Integer> weeksOfYear;
     private transient ImmutableList<Integer> weeksOfMonth;
     private transient ImmutableList<Integer> daysOfYear;
     private transient ImmutableList<Integer> daysOfMonth;
     private transient ImmutableList<Integer> daysOfWeek;
 
     public Builder()
     {
       // Nothing to do
     }
 
     public Builder(final Schedule prior)
     {
       this.start = prior.getStart();
       this.end = prior.getEnd().orNull();
       this.duration = prior.getDuration();
       this.yearGap = prior.getYearGap();
       this.monthsOfYear = prior.getMonthsOfYear().orNull();
       this.weeksOfYear = prior.getWeeksOfYear().orNull();
       this.weeksOfMonth = prior.getWeeksOfMonth().orNull();
       this.daysOfYear = prior.getDaysOfYear().orNull();
       this.daysOfMonth = prior.getDaysOfMonth().orNull();
       this.daysOfWeek= prior.getDaysOfWeek().orNull();
     }
 
     public Builder start(final DateTime start)
     {
       this.start = start;
       return this;
     }
 
     public Builder end(final DateTime end)
     {
       this.end = end;
       return this;
     }
 
     public Builder duration(final Period duration)
     {
       this.duration = duration;
       return this;
     }
 
     public Builder yearGap(final Integer yearGap)
     {
       this.yearGap = yearGap;
       return this;
     }
 
     public Builder monthsOfYear(final Integer firstMonthOfYear, final Integer ... subsequentMonthsOfYear)
     {
       this.monthsOfYear = ImmutableList.copyOf(Lists.asList(firstMonthOfYear, subsequentMonthsOfYear));
       return this;
     }
 
     public Builder monthsOfYear(final List<Integer> monthsOfYear)
     {
       if (monthsOfYear == null)
       {
         this.monthsOfYear = null;
       }
       else
       {
         this.monthsOfYear = ImmutableList.copyOf(monthsOfYear);
       }
       return this;
     }
 
     public Builder weeksOfYear(final Integer firstWeekOfYear, final Integer ... subsequentWeeksOfYear)
     {
       this.weeksOfYear = ImmutableList.copyOf(Lists.asList(firstWeekOfYear, subsequentWeeksOfYear));
       return this;
     }
 
     public Builder weeksOfYear(final List<Integer> weeksOfYear)
     {
       if (weeksOfYear == null)
       {
         this.weeksOfYear = null;
       }
       else
       {
         this.weeksOfYear = ImmutableList.copyOf(weeksOfYear);
       }
       return this;
     }
 
     public Builder weeksOfMonth(final Integer firstWeekOfMonth, final Integer ... subsequentWeeksOfMonth)
     {
       this.weeksOfMonth = ImmutableList.copyOf(Lists.asList(firstWeekOfMonth, subsequentWeeksOfMonth));
       return this;
     }
 
     public Builder weeksOfMonth(final List<Integer> weeksOfMonth)
     {
       if (weeksOfMonth == null)
       {
         this.weeksOfMonth = null;
       }
       else
       {
         this.weeksOfMonth = ImmutableList.copyOf(weeksOfMonth);
       }
       return this;
     }
 
     public Builder daysOfYear(final Integer firstDayOfYear, final Integer ... subsequentDaysOfYear)
     {
       this.daysOfYear = ImmutableList.copyOf(Lists.asList(firstDayOfYear, subsequentDaysOfYear));
       return this;
     }
 
     public Builder daysOfYear(final List<Integer> daysOfYear)
     {
       if (daysOfYear == null)
       {
         this.daysOfYear = null;
       }
       else
       {
         this.daysOfYear = ImmutableList.copyOf(daysOfYear);
       }
       return this;
     }
 
     public Builder daysOfMonth(final Integer firstDayOfMonth, final Integer ... subsequentDaysOfMonth)
     {
       this.daysOfMonth = ImmutableList.copyOf(Lists.asList(firstDayOfMonth, subsequentDaysOfMonth));
       return this;
     }
 
     public Builder daysOfMonth(final List<Integer> daysOfMonth)
     {
       if (daysOfMonth == null)
       {
         this.daysOfMonth = null;
       }
       else
       {
         this.daysOfMonth = ImmutableList.copyOf(daysOfMonth);
       }
       return this;
     }
 
     public Builder daysOfWeek(final Integer firstDayOfWeek, final Integer ... subsequentDaysOfWeek)
     {
       this.daysOfWeek = ImmutableList.copyOf(Lists.asList(firstDayOfWeek, subsequentDaysOfWeek));
       return this;
     }
 
     public Builder daysOfWeek(final List<Integer> daysOfWeek)
     {
       if (daysOfWeek == null)
       {
         this.daysOfWeek = null;
       }
       else
       {
         this.daysOfWeek = ImmutableList.copyOf(daysOfWeek);
       }
       return this;
     }
 
     public Schedule build()
     {
       return new Schedule(this.start, this.end, this.duration, this.yearGap, this.monthsOfYear, this.weeksOfYear, this.weeksOfMonth, this.daysOfYear, this.daysOfMonth, this.daysOfWeek);
     }
   }
 }
