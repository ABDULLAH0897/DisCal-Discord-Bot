package org.dreamexposure.discal.core.object.calendar;

import com.google.api.services.calendar.model.Calendar;

import discord4j.core.object.entity.Message;
import discord4j.rest.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class PreCalendar {
    private final Snowflake guildId;

    private String summary;
    private String description;
    private String timezone;

    private boolean editing;
    private String calendarId;

    private Message creatorMessage;

    private long lastEdit;

    /**
     * Creates a new PreCalendar for the Guild.
     *
     * @param _guildId The ID of the guild.
     * @param _summary The summary/name of the calendar.
     */
    public PreCalendar(Snowflake _guildId, String _summary) {
        guildId = _guildId;
        summary = _summary;

        editing = false;

        lastEdit = System.currentTimeMillis();
    }

    public PreCalendar(Snowflake _guildId, Calendar calendar) {
        guildId = _guildId;
        summary = calendar.getSummary();

        if (calendar.getDescription() != null)
            description = calendar.getDescription();

        if (calendar.getTimeZone() != null)
            timezone = calendar.getTimeZone();


        editing = false;

        lastEdit = System.currentTimeMillis();
    }

    //Getters

    /**
     * Gets the ID of the guild this PreCalendar belongs to.
     *
     * @return The ID of the guild this PreCalendar belongs to.
     */
    public Snowflake getGuildId() {
        return guildId;
    }

    /**
     * Gets the summary or name of the calendar.
     *
     * @return The summary or name of the calendar.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Gets the description of the calendar.
     *
     * @return The description of the calendar.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the Timezone of the calendar.
     *
     * @return The Timezone of the calendar.
     */
    public String getTimezone() {
        return timezone;
    }

    public boolean isEditing() {
        return editing;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public Message getCreatorMessage() {
        return creatorMessage;
    }

    public long getLastEdit() {
        return lastEdit;
    }

    //Setters

    /**
     * Sets the summary/name of the calendar.
     *
     * @param _summary The summary/name of the calendar.
     */
    public void setSummary(String _summary) {
        summary = _summary;
    }

    /**
     * Sets the description of the calendar.
     *
     * @param _description The description of the calendar.
     */
    public void setDescription(String _description) {
        description = _description;
    }

    /**
     * Sets the timezone of the calendar.
     *
     * @param _timezone The timezone of the calendar.
     */
    public void setTimezone(String _timezone) {
        timezone = _timezone;
    }

    public void setEditing(boolean _editing) {
        editing = _editing;
    }

    public void setCalendarId(String _id) {
        calendarId = _id;
    }

    public void setCreatorMessage(Message _message) {
        creatorMessage = _message;
    }

    public void setLastEdit(long _lastEdit) {
        lastEdit = _lastEdit;
    }

    //Booleans/Checkers

    /**
     * Checks if the calendar has all required data in order to be created.
     *
     * @return <code>true</code> if required data set, otherwise <code>false</code>.
     */
    public boolean hasRequiredValues() {
        return summary != null && timezone != null;
    }
}