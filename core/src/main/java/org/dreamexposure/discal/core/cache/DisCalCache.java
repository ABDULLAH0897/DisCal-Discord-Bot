package org.dreamexposure.discal.core.cache;

import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import discord4j.common.util.Snowflake;

public class DisCalCache {
    static {

    }

    //Cache maps
    private final Map<Snowflake, GuildSettings> guildSettings = new ConcurrentHashMap<>();
    private final Map<UUID, Announcement> announcements = new ConcurrentHashMap<>();

    //Prevent initialization
    private DisCalCache() {
    }

    //Getters
    public GuildSettings getSettings(Snowflake id) {
        return this.guildSettings.getOrDefault(id, null);
    }

    public Announcement getAnnouncement(UUID id) {
        return this.announcements.getOrDefault(id, null);
    }

    //Setters
    public void addSettings(GuildSettings settings) {
        this.guildSettings.putIfAbsent(settings.getGuildID(), settings);
    }

    public void addAnnouncement(Announcement announcement) {
        this.announcements.putIfAbsent(announcement.getAnnouncementId(), announcement);
    }

    //Cache clearing
    public void clearCaches() {
        this.guildSettings.clear();
        this.announcements.clear();
    }
}
