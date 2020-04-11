package org.dreamexposure.discal.core.utils;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.PreEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class TimeUtils {
	/**
	 * Checks whether or not a date has already past (IE: March 3, 1990).
	 *
	 * @param dateRaw  The date to check in format (yyyy/MM/dd-HH:mm:ss).
	 * @param timezone The timezone of the calendar this event is for.
	 * @return <code>true</code> if the date is in the past, otherwise <code>false</code>.
	 */
	public static boolean inPast(String dateRaw, TimeZone timezone) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
			sdf.setTimeZone(timezone);
			Date dateObj = sdf.parse(dateRaw);
			Date now = new Date(System.currentTimeMillis());

			return dateObj.before(now);
		} catch (ParseException e) {
			return true;
		}
	}

	private static boolean inPast(Event event) {
		if (event.getStart().getDateTime() != null)
			return event.getStart().getDateTime().getValue() <= System.currentTimeMillis();
		else
			return event.getStart().getDate().getValue() <= System.currentTimeMillis();
	}

	public static boolean inPast(String eventId, GuildSettings settings) {
		//TODO: Support multiple calendars
		if (EventUtils.eventExists(settings, eventId)) {
			try {
				Calendar service = CalendarAuth.getCalendarService(settings);
				CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID())
						.block();
				if (calendarData == null)
					return false;
				Event e = service.events().get(calendarData.getCalendarId(), eventId).execute();
				return inPast(e);
			} catch (Exception e) {
				LogFeed.log(LogObject.forException("Failed to get calendar auth", e, TimeUtils.class));
				//Return false and allow RSVP so user is not adversely affected.
				return false;
			}
		}
		return false;
	}

	/**
	 * Checks whether or not the end date is before the start date of the event.
	 *
	 * @param endRaw   The date to check in format (yyyy/MM/dd-HH:mm:ss).
	 * @param timezone The timezone of the calendar this event is for.
	 * @param event    The event that is currently being created.
	 * @return <code>true</code> if the end is before the start, otherwise <code>false</code>.
	 */
	public static boolean endBeforeStart(String endRaw, TimeZone timezone, PreEvent event) {
		if (event.getStartDateTime() != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
				sdf.setTimeZone(timezone);
				Date endDate = sdf.parse(endRaw);
				Date startDate = new Date(event.getStartDateTime().getDateTime().getValue());

				return endDate.before(startDate);
			} catch (ParseException e) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether or not the start date is after the end date of the event.
	 *
	 * @param startRaw The date to check in format (yyyy/MM/dd-HH:mm:ss).
	 * @param timezone The timezone of the calendar this event is for.
	 * @param event    The event that is currently being created.
	 * @return <code>true</code> of the start is after the end, otherwise <code>false</code>.
	 */
	public static boolean startAfterEnd(String startRaw, TimeZone timezone, PreEvent event) {
		if (event.getEndDateTime() != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
				sdf.setTimeZone(timezone);
				Date startDate = sdf.parse(startRaw);
				Date endDate = new Date(event.getEndDateTime().getDateTime().getValue());

				return startDate.after(endDate);
			} catch (ParseException e) {
				return true;
			}
		}
		return false;
	}

	public static long applyTimeZoneOffset(long epochTime, String timezone) {
		long timeZoneOffset = TimeZone.getTimeZone(ZoneId.of(timezone)).getRawOffset();
		long chicagoOffset = TimeZone.getTimeZone(ZoneId.of("UTC")).getRawOffset();

		long toAdd = timeZoneOffset - chicagoOffset;

		return epochTime + toAdd;
	}
}