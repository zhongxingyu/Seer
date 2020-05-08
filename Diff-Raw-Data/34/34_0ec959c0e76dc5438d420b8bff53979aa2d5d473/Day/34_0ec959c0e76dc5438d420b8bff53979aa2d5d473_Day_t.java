 package main;
 
 
 public class Day {
 	public enum Course {HUMANITIES, MATH, SCIENCE};
 	private int day, library;
 	private double midtermScore;
 	private boolean humMidterm, mathMidterm, sciMidterm, finalBattle;
 	private boolean humAttend, mathAttend, sciAttend, libVisit, dayEnd;
 	private Course nextCourse, current;
 	
 	public Day(int d)
 	{
 		day=d-1;
 		nextDay();		
 	}
 	
 	public void nextDay()
 	{
 		//TODO: store data from completed day if wanted
 		day++;
 		
 		humMidterm = (day==4)? true : false;
 		mathMidterm = (day==5) ? true : false;
 		sciMidterm = (day==6) ? true : false;
 		finalBattle = (day==10) ? true : false;	
 		humAttend = mathAttend = sciAttend = false;
 		
 		nextCourse = Course.HUMANITIES;
 		library = (int)(Math.random()*3);
 		libVisit = false;
 		dayEnd = false;
 	}
 	
 	public int getDay()
 	{
 		return day;	
 	}
 	
 	//for minigames
 	public Course getCourse()
 	{
 		return current;
 	}
 	
 	public Course getNextCourse()
 	{
 		return nextCourse;
 	}
 	
	public String getCourseName()
	{
		switch (current)
		{
			case HUMANITIES:
				return "Humanities";
			case MATH: 
				return "Math";
			default: 
				return "Science";
		}
	}
	
 	public String getNextCourseName()
 	{
 		switch (nextCourse)
 		{
 			case HUMANITIES:
 				return "Humanities";
 			case MATH: 
 				return "Math";
 			default: 
 				return "Science";
 		}
 	}
 	
 	public void attendClass()
 	{
 		switch (nextCourse)
 		{
 			case HUMANITIES:
 				humAttend = true;
 				nextCourse = Course.MATH;
 				current = Course.HUMANITIES;
 				break;
 			case MATH:
 				mathAttend = true;
 				nextCourse = Course.SCIENCE;
 				current = Course.MATH;
 				break;
 			case SCIENCE:
 				sciAttend = true;
 				current = Course.SCIENCE;
 				dayEnd = true;
 				break;
 		}
 	}
 	
 	public void missClass()
 	{
 		switch (nextCourse)
 		{
 			case HUMANITIES:
 				nextCourse = Course.MATH;
 				break;
 			case MATH:
 				nextCourse = Course.SCIENCE;
 				break;
 			case SCIENCE:
 				nextDay();
 				break;
 		}
 	}
 	
 	public void visitLibrary()
 	{
 		libVisit = true;
 	}
 	
 	public boolean isMidtermNext()
 	{
 		switch (nextCourse)
 		{
 			case HUMANITIES:
 				return humMidterm;
 			case MATH:
 				return  mathMidterm;
 			default:
 				return sciMidterm;
 		}
 	}
 	
 	public boolean isHumMidtermDay()
 	{
 		return humMidterm;
 	}
 	
 	public boolean isMathMidtermDay()
 	{
 		return mathMidterm;
 	}
 	
 	public boolean isSciMidtermDay()
 	{
 		return sciMidterm;
 	}
 	
 	public boolean isFinalDay()
 	{
 		return finalBattle;
 	}
 	
 	public boolean isLibrary()
 	{		
 		if (libVisit)
 			return false; 
 		
 		if (library == 0 && nextCourse == Course.HUMANITIES)
 			return true;
 		else if (library == 1 && nextCourse == Course.MATH)
 			return true;
 		else if (library == 2 && nextCourse == Course.SCIENCE)
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean isTranscript()
 	{
 		return (day == 3 && current == Course.SCIENCE) ||
 				(day == 6 && current == Course.SCIENCE);
 	}
 	
 	public boolean isDayEnd()
 	{
 		return dayEnd;
 	}
 }
