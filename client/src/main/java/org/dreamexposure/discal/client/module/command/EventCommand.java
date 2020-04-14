package org.dreamexposure.discal.client.module.command;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.dreamexposure.discal.client.event.EventCreator;
import org.dreamexposure.discal.client.message.EventMessageFormatter;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.enums.event.EventFrequency;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.object.event.EventCreatorResponse;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.utils.GeneralUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.ImageUtils;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.discal.core.utils.TimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
public class EventCommand implements ICommand {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "event";
    }

    /**
     * Gets the short aliases of the command this object is responsible for.
     * </br>
     * This will return an empty ArrayList if none are present
     *
     * @return The aliases of the command.
     */
    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> a = new ArrayList<>();
        a.add("e");

        return a;
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        CommandInfo info = new CommandInfo("event",
                "User for all event related functions",
                "!event <function> (value(s))"
        );

        info.getSubCommands().put("create", "Creates a new event");
        info.getSubCommands().put("copy", "Copies an existing event");
        info.getSubCommands().put("edit", "Edits an existing event");
        info.getSubCommands().put("cancel", "Cancels the creator/editor");
        info.getSubCommands().put("restart", "Restarts the creator/editor");
        info.getSubCommands().put("delete", "Deletes an existing event");
        info.getSubCommands().put("view", "Views an existing event");
        info.getSubCommands().put("review", "Reviews the event in the creator/editor");
        info.getSubCommands().put("confirm", "Confirms and creates/edits the event");
        info.getSubCommands().put("start", "Sets the start of the event (format: yyyy/MM/dd-hh:mm:ss)");
        info.getSubCommands().put("startdate", "Sets the start of the event (format: yyyy/MM/dd-hh:mm:ss)");
        info.getSubCommands().put("end", "Sets the end of the event (format: yyyy/MM/dd-hh:mm:ss)");
        info.getSubCommands().put("enddate", "Sets the end of the event (format: yyyy/MM/dd-hh:mm:ss)");
        info.getSubCommands().put("summary", "Sets the summary/name of the event");
        info.getSubCommands().put("description", "Sets the description of the event");
        info.getSubCommands().put("color", "Sets the color of the event");
        info.getSubCommands().put("colour", "Sets the colour of the event");
        info.getSubCommands().put("location", "Sets the location of the event");
        info.getSubCommands().put("loc", "Sets the location of the event");
        info.getSubCommands().put("recur", "True/False whether or not the event should recur");
        info.getSubCommands().put("frequency", "Sets how often the event should recur");
        info.getSubCommands().put("freq", "Sets how often the event should recur");
        info.getSubCommands().put("count", "Sets how many times the event should recur (`-1` or `0` for infinite)");
        info.getSubCommands().put("interval", "Sets the interval at which the event should recur according to the frequency");
        info.getSubCommands().put("image", "Sets the event's image");
        info.getSubCommands().put("attachment", "Sets the event's image");

        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args  The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public boolean issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
        //TODO: Add multiple calendar handling.
        CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
        if (args.length < 1) {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Few", settings), event);
        } else {
            switch (args[0].toLowerCase()) {
                case "create":
                    if (PermissionChecker.hasDisCalRole(event, settings).block())
                        moduleCreate(args, event, calendarData, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                    break;
                case "copy":
                    if (PermissionChecker.hasDisCalRole(event, settings).block())
                        moduleCopy(args, event, calendarData, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                    break;
                case "edit":
                    if (PermissionChecker.hasDisCalRole(event, settings).block())
                        moduleEdit(args, event, calendarData, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                    break;
                case "restart":
                    if (PermissionChecker.hasDisCalRole(event, settings).block())
                        moduleRestart(args, event, calendarData, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                    break;
                case "cancel":
                    if (PermissionChecker.hasDisCalRole(event, settings).block())
                        moduleCancel(event, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                    break;
                case "delete":
                    if (PermissionChecker.hasDisCalRole(event, settings).block())
                        moduleDelete(args, event, calendarData, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                    break;
                case "view":
                case "review":
                    moduleView(args, event, calendarData, settings);
                    break;
                case "confirm":
                    if (PermissionChecker.hasDisCalRole(event, settings).block())
                        moduleConfirm(event, calendarData, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                    break;
                case "startdate":
                case "start":
                    moduleStartDate(args, event, settings);
                    break;
                case "enddate":
                case "end":
                    moduleEndDate(args, event, settings);
                    break;
                case "summary":
                    moduleSummary(args, event, settings);
                    break;
                case "description":
                    moduleDescription(args, event, settings);
                    break;
                case "color":
                case "colour":
                    moduleColor(args, event, settings);
                    break;
                case "location":
                case "loc":
                    moduleLocation(args, event, settings);
                    break;
                case "image":
                case "attachment":
                    moduleAttachment(args, event, settings);
                    break;
                case "recur":
                    moduleRecur(args, event, settings);
                    break;
                case "frequency":
                case "freq":
                    moduleFrequency(args, event, settings);
                    break;
                case "count":
                    moduleCount(args, event, settings);
                    break;
                case "interval":
                    moduleInterval(args, event, settings);
                    break;
                default:
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Notification.Args.Invalid", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Invalid", settings), event);
                    }
                    break;
            }
        }
        return false;
    }


    private void moduleCreate(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
            if (EventCreator.getCreator().getPreEvent(settings.getGuildID()).getCreatorMessage() != null) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event);
            }
        } else {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                PreEvent e;
                if (args.length == 1)
                    e = EventCreator.getCreator().init(event, settings, true);
                else
                    e = EventCreator.getCreator().init(event, settings, GeneralUtils.getContent(args, 1), true);

                if (e.getCreatorMessage() == null)
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Create.Init", settings), event);
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
            }
        }
    }

    private void moduleCopy(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
            if (!EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                if (args.length == 2) {
                    String eventId = args[1];
                    if (EventUtils.eventExists(settings, eventId)) {
                        PreEvent preEvent = EventCreator.getCreator().init(event, eventId, settings, true);
                        if (preEvent != null) {
                            if (preEvent.getCreatorMessage() == null) {
                                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Copy.Init", settings), EventMessageFormatter.getPreEventEmbed(preEvent, settings), event);
                            }
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
                        }
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotFound", settings), event);
                    }
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Copy.Specify", settings), event);
                }
            } else {
                if (EventCreator.getCreator().getPreEvent(settings.getGuildID()).getCreatorMessage() != null) {
                    MessageManager.deleteMessage(event);
                    MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                    EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event);
                }
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
        }
    }

    private void moduleEdit(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
            if (!EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                if (args.length == 2) {
                    String eventId = args[1];
                    if (EventUtils.eventExists(settings, eventId)) {
                        PreEvent preEvent = EventCreator.getCreator().edit(event, eventId, settings, true);
                        if (preEvent.getCreatorMessage() == null) {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Edit.Init", settings), EventMessageFormatter.getPreEventEmbed(preEvent, settings), event);
                        }
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotFound", settings), event);
                    }
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Edit.Specify", settings), event);
                }
            } else {
                if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                    MessageManager.deleteMessage(event);
                    MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                    EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event);
                }
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
        }
    }

    private void moduleCancel(MessageCreateEvent event, GuildSettings settings) {
        Message msg = null;
        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID()))
            msg = EventCreator.getCreator().getCreatorMessage(settings.getGuildID());

        if (EventCreator.getCreator().terminate(settings.getGuildID())) {
            if (msg != null) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(msg);
            }
            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Cancel.Success", settings), event);
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
        }
    }

    private void moduleRestart(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        Message msg = null;
        boolean editing = false;
        if (EventCreator.getCreator().hasPreEvent(settings.getGuildID()))
            editing = EventCreator.getCreator().getPreEvent(settings.getGuildID()).isEditing();


        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID()))
            msg = EventCreator.getCreator().getCreatorMessage(settings.getGuildID());

        if (EventCreator.getCreator().terminate(settings.getGuildID())) {
            if (msg != null) {
                MessageManager.deleteMessage(msg);
                MessageManager.deleteMessage(event);
            }
            if (!editing)
                moduleCreate(args, event, calendarData, settings);
            else
                moduleEdit(args, event, calendarData, settings);
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
        }
    }

    private void moduleDelete(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        if (args.length == 2) {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                if (!EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                    if (EventUtils.deleteEvent(settings, args[1])) {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Delete.Success", settings), event);
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotFound", settings), event);
                    }
                } else {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Delete.Failure.Creator", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Delete.Failure.Creator", settings), event);
                    }
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Delete.Specify", settings), event);
        }
    }

    private void moduleView(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        if (args.length == 1) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                    MessageManager.deleteMessage(event);
                    MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                    EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Event.View.Creator.Confirm", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.View.Creator.Confirm", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event);
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Event.View.Args.Few", settings), event);
            }
        } else if (args.length == 2) {
            //Try to get the event by ID.
            if (!EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                    try {
                        Calendar service = CalendarAuth.getCalendarService(settings);

                        Event calEvent = service.events().get(calendarData.getCalendarAddress(), args[1]).execute();
                        MessageManager.sendMessageAsync(EventMessageFormatter.getEventEmbed(calEvent, settings), event);
                    } catch (Exception e) {
                        //Event probably doesn't exist...
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotFound", settings), event);
                    }
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
                }
            } else {
                if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                    MessageManager.deleteMessage(event);
                    MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                    EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Event.View.Creator.Active", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.View.Creator.Active", settings), event);
                }
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Event.View.Specify", settings), event);
        }
    }

    private void moduleConfirm(MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
            if (EventCreator.getCreator().getPreEvent(settings.getGuildID()).hasRequiredValues()) {
                if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                    EventCreatorResponse response = EventCreator.getCreator().confirmEvent(event, settings);
                    if (response.isSuccessful()) {
                        if (!response.isEdited()) {
                            if (response.getCreatorMessage() != null) {
                                MessageManager.deleteMessage(event);
                                MessageManager.deleteMessage(response.getCreatorMessage());
                                MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Confirm.Create", settings), EventMessageFormatter.getEventConfirmationEmbed(response, settings), event);
                            } else {
                                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Confirm.Create", settings), EventMessageFormatter.getEventConfirmationEmbed(response, settings), event);
                            }
                        } else {
                            if (response.getCreatorMessage() != null) {
                                MessageManager.deleteMessage(event);
                                MessageManager.deleteMessage(response.getCreatorMessage());
                                MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Confirm.Edit", settings), EventMessageFormatter.getEventConfirmationEmbed(response, settings), event);
                            } else {
                                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Confirm.Edit", settings), EventMessageFormatter.getEventConfirmationEmbed(response, settings), event);
                            }
                        }
                    } else {
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Confirm.Failure", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Confirm.Failure", settings), event);
                        }
                    }
                } else {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.NoCalendar", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
                    }
                }
            } else {
                if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                    MessageManager.deleteMessage(event);
                    MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                    EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.NoRequired", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NoRequired", settings), event);
                }
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
        }
    }

    private void moduleStartDate(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                String dateRaw = args[1].trim();
                if (dateRaw.length() > 10) {
                    try {
                        //Do a lot of date shuffling to get to proper formats and shit like that.
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                        TimeZone tz = TimeZone.getTimeZone(EventCreator.getCreator().getPreEvent(settings.getGuildID()).getTimeZone());
                        sdf.setTimeZone(tz);
                        Date dateObj = sdf.parse(dateRaw);
                        DateTime dateTime = new DateTime(dateObj);
                        EventDateTime eventDateTime = new EventDateTime();
                        eventDateTime.setDateTime(dateTime);

                        //Wait! Lets check now if its in the future and not the past!
                        if (!TimeUtils.inPast(dateRaw, tz) && !TimeUtils.startAfterEnd(dateRaw, tz, EventCreator.getCreator().getPreEvent(settings.getGuildID()))) {
                            //Date shuffling done, now actually apply all that damn stuff here.
                            EventCreator.getCreator().getPreEvent(settings.getGuildID()).setStartDateTime(eventDateTime);

                            //Apply viewable date/times...
                            SimpleDateFormat sdfV = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                            Date dateObjV = sdfV.parse(dateRaw);
                            DateTime dateTimeV = new DateTime(dateObjV);
                            EventDateTime eventDateTimeV = new EventDateTime();
                            eventDateTimeV.setDateTime(dateTimeV);
                            EventCreator.getCreator().getPreEvent(settings.getGuildID()).setViewableStartDate(eventDateTimeV);


                            //To streamline, check if event end is null, if so, apply 2 hour duration!
                            if (EventCreator.getCreator().getPreEvent(settings.getGuildID()).getEndDateTime() == null) {
                                EventDateTime end = EventCreator.getCreator().getPreEvent(settings.getGuildID()).getStartDateTime().clone();
                                long endLong = end.getDateTime().getValue() + 3600000; //Add an hour

                                end.setDateTime(new DateTime(endLong));

                                EventCreator.getCreator().getPreEvent(settings.getGuildID()).setEndDateTime(end);


                                //Viewable date
                                EventDateTime endV = EventCreator.getCreator().getPreEvent(settings.getGuildID()).getViewableStartDate().clone();
                                long endVLong = endV.getDateTime().getValue() + 3600000; //Add an hour

                                endV.setDateTime(new DateTime(endVLong));

                                EventCreator.getCreator().getPreEvent(settings.getGuildID()).setViewableEndDate(endV);
                            }

                            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                                MessageManager.deleteMessage(event);
                                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Start.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                            } else {
                                String msg = MessageManager.getMessage("Creator.Event.Start.Success", settings);
                                msg = msg.replaceAll("%date%", EventMessageFormatter.getHumanReadableDate(eventDateTimeV, settings, true)).replaceAll("%time%", EventMessageFormatter.getHumanReadableTime(eventDateTimeV, settings, true));
                                MessageManager.sendMessageAsync(msg, event);
                            }
                        } else {
                            //Oops! Time is in the past or after end...
                            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                                MessageManager.deleteMessage(event);
                                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Start.Failure.Illegal", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                            } else {
                                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Start.Failure.Illegal", settings), event);
                            }
                        }
                    } catch (ParseException e) {
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Time.Invalid", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Time.Invalid", settings), event);
                        }
                    }
                } else {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Time.InvalidFormat", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Time.InvalidFormat", settings), event);
                    }
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Start.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Start.Specify", settings), event);
            }
        }
    }

    private void moduleEndDate(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                String dateRaw = args[1].trim();
                if (dateRaw.length() > 10) {
                    try {
                        //Do a lot of date shuffling to get to proper formats and shit like that.
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                        TimeZone tz = TimeZone.getTimeZone(EventCreator.getCreator().getPreEvent(settings.getGuildID()).getTimeZone());
                        sdf.setTimeZone(tz);
                        Date dateObj = sdf.parse(dateRaw);
                        DateTime dateTime = new DateTime(dateObj);
                        EventDateTime eventDateTime = new EventDateTime();
                        eventDateTime.setDateTime(dateTime);

                        //Wait! Lets check now if its in the future and not the past!
                        if (!TimeUtils.inPast(dateRaw, tz) && !TimeUtils.endBeforeStart(dateRaw, tz, EventCreator.getCreator().getPreEvent(settings.getGuildID()))) {
                            //Date shuffling done, now actually apply all that damn stuff here.
                            EventCreator.getCreator().getPreEvent(settings.getGuildID()).setEndDateTime(eventDateTime);

                            //Apply viewable date/times...
                            SimpleDateFormat sdfV = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                            Date dateObjV = sdfV.parse(dateRaw);
                            DateTime dateTimeV = new DateTime(dateObjV);
                            EventDateTime eventDateTimeV = new EventDateTime();
                            eventDateTimeV.setDateTime(dateTimeV);
                            EventCreator.getCreator().getPreEvent(settings.getGuildID()).setViewableEndDate(eventDateTimeV);

                            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                                MessageManager.deleteMessage(event);
                                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.End.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                            } else {
                                String msg = MessageManager.getMessage("Creator.Event.End.Success", settings);
                                msg = msg.replaceAll("%date%", EventMessageFormatter.getHumanReadableDate(eventDateTimeV, settings, true)).replaceAll("%time%", EventMessageFormatter.getHumanReadableTime(eventDateTimeV, settings, true));
                                MessageManager.sendMessageAsync(msg, event);
                            }
                        } else {
                            //Oops! Time is in the past or before the starting time...
                            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                                MessageManager.deleteMessage(event);
                                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.End.Failure.Illegal", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                            } else {
                                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.End.Failure.Illegal", settings), event);
                            }
                        }
                    } catch (ParseException e) {
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Time.Invalid", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Time.Invalid", settings), event);
                        }
                    }
                } else {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Time.InvalidFormat", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Time.InvalidFormat", settings), event);
                    }
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.End.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.End.Specify", settings), event);
            }
        }
    }

    private void moduleSummary(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length > 1) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                String content = GeneralUtils.getContent(args, 1);
                EventCreator.getCreator().getPreEvent(settings.getGuildID()).setSummary(content);
                if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                    MessageManager.deleteMessage(event);
                    MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                    EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Summary.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Summary.Success", "%summary%", GeneralUtils.getContent(args, 1), settings), event);
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Summary.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Summary.Specify", settings), event);
            }
        }
    }

    private void moduleDescription(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length > 1) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                String content = GeneralUtils.getContent(args, 1);
                EventCreator.getCreator().getPreEvent(settings.getGuildID()).setDescription(content);
                if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                    MessageManager.deleteMessage(event);
                    MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                    EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Description.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Description.Success", "%description%", content, settings), event);
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Description.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Description.Specify", settings), event);
            }
        }
    }

    private void moduleColor(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            String value = args[1];
            if (value.equalsIgnoreCase("list") || value.equalsIgnoreCase("colors") || value.equalsIgnoreCase("colours")) {
                Consumer<EmbedCreateSpec> embed = spec -> {
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                    spec.setTitle("Available Colors");
                    spec.setUrl("https://discalbot.com/docs/event/colors");
                    spec.setColor(GlobalConst.discalColor);
                    spec.setFooter("Click Title for previews of the colors!", null);

                    for (EventColor ec : EventColor.values()) {
                        spec.addField(ec.name(), ec.getId() + "", true);
                    }
                };


                if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                    MessageManager.deleteMessage(event);
                    MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                    EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync("All Supported Colors. Use either the name or ID in the command: `!event color <name/id>`", embed, event));
                } else {
                    MessageManager.sendMessageAsync("All Supported Colors. Use either the name or ID in the command: `!event color <name/id>`", embed, event);
                }
            } else {
                if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                    //Attempt to get color.
                    if (EventColor.exists(value)) {
                        EventColor color = EventColor.fromNameOrHexOrID(value);
                        EventCreator.getCreator().getPreEvent(settings.getGuildID()).setColor(color);
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Color.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Color.Success", "%color%", color.name(), settings), event);
                        }
                    } else {
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Color.Invalid", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Color.Invalid", settings), event);
                        }
                    }
                } else {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
                }
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Color.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Color.Specify", settings), event);
            }
        }
    }

    private void moduleLocation(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length > 1) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                String content = GeneralUtils.getContent(args, 1);
                if (!content.equalsIgnoreCase("clear")) {
                    EventCreator.getCreator().getPreEvent(settings.getGuildID()).setLocation(content);
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Location.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Location.Success", "%location%", content, settings), event);
                    }
                } else {
                    EventCreator.getCreator().getPreEvent(settings.getGuildID()).setLocation(null);
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Location.Success.Clear", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Location.Success.Clear", settings), event);
                    }
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Location.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Location.Specify", settings), event);
            }
        }
    }

    private void moduleAttachment(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            String value = args[1];
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                if (value.equalsIgnoreCase("delete") || value.equalsIgnoreCase("remove") || value.equalsIgnoreCase("clear")) {
                    //Delete picture from event
                    EventCreator.getCreator().getPreEvent(settings.getGuildID()).setEventData(EventData.empty());

                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Attachment.Delete", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Attachment.Delete", settings), event);
                    }
                } else if (ImageUtils.validate(value, settings.isPatronGuild())) {
                    PreEvent preEvent = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                    EventData eventData = EventData.fromImage(
                            settings.getGuildID(),
                            preEvent.getEventId(),
                            preEvent.getEndDateTime().getDateTime().getValue(),
                            value
                    );
                    preEvent.setEventData(eventData);


                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Attachment.Success", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Attachment.Success", settings), event);
                    }
                } else {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Attachment.Failure", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    }
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Attachment.Specify", settings), event);
        }
    }

    //Event recurrence settings
    private void moduleRecur(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            String valueString = args[1];
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());
                if (pre.isEditing() && pre.getEventId().contains("_")) {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Recur.Failure.Child", "%id%", pre.getEventId().split("_")[0], settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Recur.Failure.Child", "%id%", pre.getEventId().split("_")[0], settings), event);
                    }
                    return;
                }
                try {
                    boolean value = Boolean.parseBoolean(valueString);
                    EventCreator.getCreator().getPreEvent(settings.getGuildID()).setShouldRecur(value);
                    if (value) {
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Recur.True", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Recur.True", settings), event);
                        }
                    } else {
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Recur.False", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Recur.False", settings), event);
                        }
                    }
                } catch (Exception e) {
                    //Could not convert to boolean
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Recur.Failure.Invalid", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Recur.Failure.Invalid", settings), event);
                    }
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Recur.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Recur.Specify", settings), event);
            }
        }
    }

    private void moduleFrequency(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                if (EventCreator.getCreator().getPreEvent(settings.getGuildID()).shouldRecur()) {
                    String value = args[1];
                    if (EventFrequency.isValid(value)) {
                        EventFrequency freq = EventFrequency.fromValue(value);
                        EventCreator.getCreator().getPreEvent(settings.getGuildID()).getRecurrence().setFrequency(freq);
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Frequency.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Frequency.Success", "%freq%", freq.name(), settings), event);
                        }
                    } else {
                        String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Frequency.List", "%types%", value, settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Frequency.List", "%types%", values, settings), event);
                        }
                    }
                } else {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Recur.Not", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Recur.Not", settings), event);
                    }
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Frequency.Specify", "%types%", values, settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Frequency.Specify", "%types%", values, settings), event);
            }
        }
    }

    private void moduleCount(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                if (EventCreator.getCreator().getPreEvent(settings.getGuildID()).shouldRecur()) {
                    try {
                        Integer amount = Integer.valueOf(args[1]);
                        EventCreator.getCreator().getPreEvent(settings.getGuildID()).getRecurrence().setCount(amount);
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Count.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Count.Success", "%count%", amount + "", settings), event);
                        }
                    } catch (NumberFormatException e) {
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Notification.Args.Value.Integer", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Value.Integer", settings), event);
                        }
                    }
                } else {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Recur.Not", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    } else {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Recur.Not", settings), event);
                    }
                }
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Count.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Count.Specify", settings), event);
            }
        }
    }

    private void moduleInterval(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                if (EventCreator.getCreator().getPreEvent(settings.getGuildID()).shouldRecur()) {
                    try {
                        Integer amount = Integer.valueOf(args[1]);
                        EventCreator.getCreator().getPreEvent(settings.getGuildID()).getRecurrence().setInterval(amount);
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Interval.Success.New", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Interval.Success", "%amount%", amount + "", settings), event);
                        }
                    } catch (NumberFormatException e) {
                        if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                            MessageManager.deleteMessage(event);
                            MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                            EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Notification.Args.Value.Integer", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                        } else {
                            MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Value.Integer", settings), event);
                        }
                    }
                } else {
                    if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                        MessageManager.deleteMessage(event);
                        MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                        EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Recur.Not", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
                    }
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Recur.Not", settings), event);
                }
            } else {
                MessageManager.sendMessageAsync("Event Creator not initialized!", event);
            }
        } else {
            if (EventCreator.getCreator().hasCreatorMessage(settings.getGuildID())) {
                MessageManager.deleteMessage(event);
                MessageManager.deleteMessage(EventCreator.getCreator().getCreatorMessage(settings.getGuildID()));
                EventCreator.getCreator().setCreatorMessage(MessageManager.sendMessageSync(MessageManager.getMessage("Creator.Event.Interval.Specify", settings), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(settings.getGuildID()), settings), event));
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Event.Interval.Specify", settings), event);
            }
        }
    }
}