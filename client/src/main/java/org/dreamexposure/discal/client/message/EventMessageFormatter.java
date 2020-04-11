package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.EventCreatorResponse;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.ImageUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Image;
import discord4j.core.spec.EmbedCreateSpec;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
public class EventMessageFormatter {

	/**
	 * Gets an EmbedObject for the specified event.
	 *
	 * @param event    The event involved.
	 * @param settings The guild's settings
	 * @return The EmbedObject of the event.
	 */
	public static Consumer<EmbedCreateSpec> getEventEmbed(Event event, GuildSettings settings) {
		return spec -> {
			EventData ed = DatabaseManager.getEventData(settings.getGuildID(), event.getId()).block();
			Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

			if (settings.isBranded() && guild != null)
				spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
			else
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

			spec.setTitle(MessageManager.getMessage("Embed.Event.Info.Title", settings));
			if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild())) {
				spec.setImage(ed.getImageLink());
			}
			if (event.getSummary() != null) {
				String summary = event.getSummary();
				if (summary.length() > 250) {
					summary = summary.substring(0, 250);
					summary = summary + " (continues on Google Calendar View)";
				}

				spec.addField(MessageManager.getMessage("Embed.Event.Info.Summary", settings), summary, true);
			}
			if (event.getDescription() != null) {
				String description = event.getDescription();
				if (description.length() > 500) {
					description = description.substring(0, 500);
					description = description + " (continues on Google Calendar View)";
				}
				spec.addField(MessageManager.getMessage("Embed.Event.Info.Description", settings), description, true);
			}
			spec.addField(MessageManager.getMessage("Embed.Event.Info.StartDate", settings), getHumanReadableDate(event.getStart(), settings, false), true);
			spec.addField(MessageManager.getMessage("Embed.Event.Info.StartTime", settings), getHumanReadableTime(event.getStart(), settings, false), true);
			spec.addField(MessageManager.getMessage("Embed.Event.Info.EndDate", settings), getHumanReadableDate(event.getEnd(), settings, false), true);
			spec.addField(MessageManager.getMessage("Embed.Event.Info.EndTime", settings), getHumanReadableTime(event.getEnd(), settings, false), true);

			try {
				//TODO: add support for multiple calendars...
				CalendarData data = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
				Calendar service = CalendarAuth.getCalendarService(settings);
				String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
				spec.addField(MessageManager.getMessage("Embed.Event.Info.TimeZone", settings), tz, true);
			} catch (Exception e1) {
				spec.addField(MessageManager.getMessage("Embed.Event.Info.TimeZone", settings), "Error/Unknown", true);
			}
			if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
				if (event.getLocation().length() > 300) {
					String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
					spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), location, true);
				} else {
					spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
				}
			}
			//TODO: Add info on recurrence here.
			spec.setUrl(event.getHtmlLink());
			spec.setFooter(MessageManager.getMessage("Embed.Event.Info.ID", "%id%", event.getId(), settings), null);
			try {
				EventColor ec = EventColor.fromId(Integer.valueOf(event.getColorId()));
				spec.setColor(ec.asColor());
			} catch (Exception e) {
				//Color is null, ignore and add our default.
				spec.setColor(GlobalConst.discalColor);
			}

		};
	}

	/**
	 * Gets an EmbedObject for the specified event.
	 *
	 * @param event The event involved.
	 * @return The EmbedObject of the event.
	 */
	public static Consumer<EmbedCreateSpec> getCondensedEventEmbed(Event event, GuildSettings settings) {
		return spec -> {
			Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

			if (settings.isBranded() && guild != null)
				spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
			else
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

			spec.setTitle(MessageManager.getMessage("Embed.Event.Condensed.Title", settings));
			EventData ed = DatabaseManager.getEventData(settings.getGuildID(), event.getId()).block();
			if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild()))
				spec.setThumbnail(ed.getImageLink());

			if (event.getSummary() != null) {
				String summary = event.getSummary();
				if (summary.length() > 250) {
					summary = summary.substring(0, 250);
					summary = summary + " (continues on Google Calendar View)";
				}
				spec.addField(MessageManager.getMessage("Embed.Event.Condensed.Summary", settings), summary, true);
			}
			spec.addField(MessageManager.getMessage("Embed.Event.Condensed.Date", settings), getHumanReadableDate(event.getStart(), settings, false), true);
			if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
				if (event.getLocation().length() > 300) {
					String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
					spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), location, true);
				} else {
					spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
				}
			}
			spec.addField(MessageManager.getMessage("Embed.Event.Condensed.ID", settings), event.getId(), false);
			spec.setUrl(event.getHtmlLink());
			try {
				EventColor ec = EventColor.fromId(Integer.valueOf(event.getColorId()));
				spec.setColor(ec.asColor());
			} catch (Exception e) {
				//Color is null, ignore and add our default.
				spec.setColor(GlobalConst.discalColor);
			}
		};
	}

	/**
	 * Gets an EmbedObject for the specified PreEvent.
	 *
	 * @param event The PreEvent to get an embed for.
	 * @return The EmbedObject of the PreEvent.
	 */
	public static Consumer<EmbedCreateSpec> getPreEventEmbed(PreEvent event, GuildSettings settings) {
		return spec -> {
			Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

			if (settings.isBranded() && guild != null)
				spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
			else
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

			try {
				if (event.getEventData() != null && event.getEventData().getImageLink() != null && ImageUtils.validate(event.getEventData().getImageLink(), settings.isPatronGuild())) {
					spec.setImage(event.getEventData().getImageLink());
				}
			} catch (NullPointerException e) {
				//TODO: find out why this is happening
				LogFeed.log(LogObject
						.forException("[Event]", "Failed to handle event image. Is event null?",
								e, EventMessageFormatter.class));
			}
			if (event.isEditing())
				spec.addField(MessageManager.getMessage("Embed.Event.Pre.Id", settings), event.getEventId(), false);

			if (event.getSummary() != null) {
				String summary = event.getSummary();
				if (summary.length() > 250) {
					summary = summary.substring(0, 250);
					summary = summary + " (continues on Google Calendar View)";
				}
				spec.addField(MessageManager.getMessage("Embed.Event.Pre.Summary", settings), summary, true);
			} else {
				spec.addField(MessageManager.getMessage("Embed.Event.Pre.Summary", settings), "NOT SET", true);
			}
			if (event.getDescription() != null) {
				String description = event.getDescription();
				if (description.length() > 500) {
					description = description.substring(0, 500);
					description = description + " (continues on Google Calendar View)";
				}
				spec.addField(MessageManager.getMessage("Embed.Event.Pre.Description", settings), description, true);
			} else {
				spec.addField(MessageManager.getMessage("Embed.Event.Pre.Description", settings), "NOT SET", true);
			}
			if (event.shouldRecur()) {
				spec.addField(MessageManager.getMessage("Embed.Event.Pre.Recurrence", settings), event.getRecurrence().toHumanReadable(), false);
			} else {
				spec.addField(MessageManager.getMessage("Embed.Event.Pre.Recurrence", settings), "N/a", true);
			}
			spec.addField(MessageManager.getMessage("Embed.Event.Pre.StartDate", settings), getHumanReadableDate(event.getViewableStartDate(), settings, true), true);
			spec.addField(MessageManager.getMessage("Embed.Event.Pre.StartTime", settings), EventMessageFormatter.getHumanReadableTime(event.getViewableStartDate(), settings, true), true);
			spec.addField(MessageManager.getMessage("Embed.Event.Pre.EndDate", settings), getHumanReadableDate(event.getViewableEndDate(), settings, true), true);
			spec.addField(MessageManager.getMessage("Embed.Event.Pre.EndTime", settings), EventMessageFormatter.getHumanReadableTime(event.getViewableEndDate(), settings, true), true);
			spec.addField(MessageManager.getMessage("Embed.Event.Pre.TimeZone", settings), event.getTimeZone(), true);

			if (event.getLocation() != null && !event.getLocation().equalsIgnoreCase("")) {
				if (event.getLocation().length() > 300) {
					String location = event.getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
					spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), location, true);
				} else {
					spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), event.getLocation(), true);
				}
			} else {
				spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), "N/a", true);
			}

			spec.setFooter(MessageManager.getMessage("Embed.Event.Pre.Key", settings), null);
			spec.setColor(event.getColor().asColor());

		};
	}

	/**
	 * Gets an EmbedObject for the specified CreatorResponse.
	 *
	 * @param ecr The CreatorResponse involved.
	 * @return The EmbedObject for the CreatorResponse.
	 */
	public static Consumer<EmbedCreateSpec> getEventConfirmationEmbed(EventCreatorResponse ecr, GuildSettings settings) {
		return spec -> {
			EventData ed = DatabaseManager.getEventData(settings.getGuildID(), ecr.getEvent().getId()).block();
			Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

			if (settings.isBranded() && guild != null)
				spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
			else
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

			spec.setTitle(MessageManager.getMessage("Embed.Event.Confirm.Title", settings));
			if (ed.getImageLink() != null && ImageUtils.validate(ed.getImageLink(), settings.isPatronGuild())) {
				spec.setImage(ed.getImageLink());
			}
			spec.addField(MessageManager.getMessage("Embed.Event.Confirm.ID", settings), ecr.getEvent().getId(), false);
			spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Date", settings), getHumanReadableDate(ecr.getEvent().getStart(), settings, false), false);
			if (ecr.getEvent().getLocation() != null && !ecr.getEvent().getLocation().equalsIgnoreCase("")) {
				if (ecr.getEvent().getLocation().length() > 300) {
					String location = ecr.getEvent().getLocation().substring(0, 300).trim() + "... (cont. on Google Cal)";
					spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), location, true);
				} else {
					spec.addField(MessageManager.getMessage("Embed.Event.Confirm.Location", settings), ecr.getEvent().getLocation(), true);
				}
			}
			spec.setFooter(MessageManager.getMessage("Embed.Event.Confirm.Footer", settings), null);
			spec.setUrl(ecr.getEvent().getHtmlLink());
			try {
				EventColor ec = EventColor.fromId(Integer.valueOf(ecr.getEvent().getColorId()));
				spec.setColor(ec.asColor());
			} catch (Exception e) {
				//Color is null, ignore and add our default.
				spec.setColor(GlobalConst.discalColor);
			}
		};
	}

	/**
	 * Gets a formatted date.
	 *
	 * @param eventDateTime The object to get the date from.
	 * @return A formatted date.
	 */
	public static String getHumanReadableDate(@Nullable EventDateTime eventDateTime, GuildSettings settings, boolean preEvent) {
		try {
			if (eventDateTime == null) {
				return "NOT SET";
			} else {
				//Get timezone
				CalendarData data = DatabaseManager.getMainCalendar(settings.getGuildID()).block();

				String timezone;
				if (!preEvent) {
					timezone = CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute().getTimeZone();
				} else {
					timezone = "UTC";
				}
				if (eventDateTime.getDateTime() != null) {
					long dateTime = eventDateTime.getDateTime().getValue();
					LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.of(timezone));
					DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");

					return format.format(ldt);

				} else {
					long dateTime = eventDateTime.getDate().getValue();
					LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.of(timezone));
					DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");

					return format.format(ldt);
				}
			}
		} catch (Exception e) {
			LogFeed.log(LogObject
					.forException("Failed to format date", e, EventMessageFormatter.class));
			return "ERROR! Code: E001";
		}
	}

	/**
	 * Gets a formatted time.
	 *
	 * @param eventDateTime The object to get the time from.
	 * @return A formatted time.
	 */
	public static String getHumanReadableTime(@Nullable EventDateTime eventDateTime, GuildSettings settings, boolean preEvent) {
		try {
			if (eventDateTime == null) {
				return "NOT SET";
			} else {
				//Get timezone
				CalendarData data = DatabaseManager.getMainCalendar(settings.getGuildID()).block();

				String timezone;
				if (!preEvent) {
					timezone = CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute().getTimeZone();
				} else {
					timezone = "UTC";
				}
				if (eventDateTime.getDateTime() != null) {
					long dateTime = eventDateTime.getDateTime().getValue();
					LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.of(timezone));
					DateTimeFormatter format = DateTimeFormatter.ofPattern("hh:mm:ss a");

					return format.format(ldt);

				} else {
					long dateTime = eventDateTime.getDate().getValue();
					LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.of(timezone));
					DateTimeFormatter format = DateTimeFormatter.ofPattern("hh:mm:ss a");

					return format.format(ldt);
				}
			}
		} catch (Exception e) {
			LogFeed.log(LogObject
					.forException("Failed to format date", e, EventMessageFormatter.class));
			return "ERROR! Code: E002";
		}
	}
}