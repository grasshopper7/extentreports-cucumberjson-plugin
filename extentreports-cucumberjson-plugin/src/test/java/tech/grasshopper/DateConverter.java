package tech.grasshopper;

import java.time.ZonedDateTime;
import java.util.Date;

public class DateConverter {

	public static Date parseToDate(String timeStamp) {
		return Date.from(ZonedDateTime.parse(timeStamp).toInstant());
	}
}
