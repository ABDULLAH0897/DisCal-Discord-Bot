package org.dreamexposure.discal.client.module.announcement;

import java.util.Timer;
import java.util.TimerTask;

public class AnnouncementThreader {
    private static AnnouncementThreader threader;

    private Timer timer;

    private AnnouncementThreader() {
    }

    public static AnnouncementThreader getThreader() {
        if (threader == null)
            threader = new AnnouncementThreader();

        return threader;
    }

    public void init() {
        timer = new Timer(true);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                AnnouncementThread t = new AnnouncementThread();
                t.setDaemon(true);
                t.start();
            }
        }, 5 * 1000 * 60, 5 * 1000 * 60);
    }

    public void shutdown() {
        timer.cancel();
    }
}