package com.ironkey.tracker;

import java.util.GregorianCalendar;
import com.webobjects.foundation.*;
import java.util.*;

public class DateUtil {
    public long days;
    public long weekdays = -1;
    public long hours;
    public long minutes;
    public long seconds;
    public long milliseconds;
    public NSTimestamp day1;
    public NSTimestamp day2;


    public DateUtil(NSTimestamp d1, NSTimestamp d2) {
        day1 = d1;
        day2 = d2;
        init();

    }
    public void init() {
        GregorianCalendar day1Cal = new GregorianCalendar();
        GregorianCalendar day2Cal = new GregorianCalendar();
        day1Cal.setTime(day1);
        day2Cal.setTime(day2);

        long d1 = day1Cal.getTime().getTime();
        long d2 = day2Cal.getTime().getTime();
        long difMil = d2-d1;
        long milPerDay = 1000*60*60*24;
        long milPerHour = 1000*60*60;
        long milPerMin = 1000*60;
        long milPerSec = 1000;

        days = difMil / milPerDay;
        hours = ( difMil - days*milPerDay ) / milPerHour;
        minutes = ( difMil - days*milPerDay - hours*milPerHour ) / milPerMin;
        seconds = ( difMil - days*milPerDay - hours*milPerHour - minutes*milPerMin ) / milPerSec;
        // milliseconds = ( difMil - days*milPerDay - ore*milPerHour - min*milPerMin - sec*milPerSec );

        //System.out.println("days="+days+" hours="+hours+" minutes="+minutes+" seconds="+seconds);
    }
	
	public long calendardays() {
		long numdays = days;

		if(hours > 7) { // if it is early in the day, we  will count today, if it is late in the date we won't (3 pm is cutoff)
			numdays++;
		}
		return numdays;
	}
	public long weekdays() {
		if(weekdays == -1) {
			weekdays = 0;
			NSTimeZone tz = NSTimeZone.timeZoneWithName("America/Los_Angeles", true);
			// NSTimestamp currDatePacific = new NSTimestamp(currDate.getTime(), tz);

			NSTimestamp startDate = new NSTimestamp(day1.getTime(), tz);
			GregorianCalendar cal = new GregorianCalendar();
		
			int num = (int)calendardays();
			for(int i = 0; i < num; i++) {  // will check the start date, all days in-between and the end-date
				NSTimestamp tempDate =  startDate.timestampByAddingGregorianUnits (0, 0, i, 0, 0, 0);
				cal.setTime(tempDate);
				//The day-of-week is an integer value where 1 is Sunday, 2 is Monday, ..., and 7 is Saturday 
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if((dayOfWeek>1) && (dayOfWeek<7)) {
					weekdays++;
				}
				
			}
		
		 } 
		return weekdays;
	}

}

