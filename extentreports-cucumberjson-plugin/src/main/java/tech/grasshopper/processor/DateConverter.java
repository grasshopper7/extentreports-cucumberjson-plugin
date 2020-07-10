package tech.grasshopper.processor;

import java.time.ZonedDateTime;
import java.util.Date;

public class DateConverter {

	public static Date parseToDate(String timeStamp) {
		return Date.from(ZonedDateTime.parse(timeStamp).toInstant());
	}
	
	public static Date parseToDate(ZonedDateTime zonedDateTime) {
		return Date.from(zonedDateTime.toInstant());
	}
	
	public static ZonedDateTime parseToZonedDateTime(String timeStamp) {
		return ZonedDateTime.parse(timeStamp);
	}
}
