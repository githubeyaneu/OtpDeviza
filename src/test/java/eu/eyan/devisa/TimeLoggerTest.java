package eu.eyan.devisa;

import static java.util.TimeZone.getTimeZone;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import eu.eyan.logging.Logging;

public class TimeLoggerTest {

	@Test
	public void testTime() {
		
		SimpleDateFormat DÁTUM_FORMÁTUM_PERC = new SimpleDateFormat("yyyy.MMMM.dd HH:mm", new Locale("hu", "HU"));
		DÁTUM_FORMÁTUM_PERC.setTimeZone(getTimeZone("Europe/Berlin"));

		Date now = new Date();
		Logging.logInfo("Date: " + now);
		
		TimeZone tzDefault = TimeZone.getDefault();
		TimeZone tzGMT = TimeZone.getTimeZone("GMT");
		TimeZone timeZoneEuBerlin = getTimeZone("Europe/Berlin");

		printTime(now, tzDefault, DÁTUM_FORMÁTUM_PERC);
		printTime(now, tzGMT, DÁTUM_FORMÁTUM_PERC);
		printTime(now, timeZoneEuBerlin, DÁTUM_FORMÁTUM_PERC);
		
		
		DÁTUM_FORMÁTUM_PERC.setTimeZone(timeZoneEuBerlin);
	}

	private void printTime(Date date, TimeZone timeZone, SimpleDateFormat dateFormat) {
		Logging.logInfo("");
		Logging.logInfo("Timezone: " + timeZone.getID());
		Logging.logInfo("  Date: " + date);
		Logging.logInfo("Formatted time: " + dateFormat.getTimeZone().getID());
		Logging.logInfo("  Formatted Date: " + dateFormat.format(date));
	}

}
