package com.samantha.app.event;

import com.samantha.app.core.sys.Application;

import java.util.ArrayList;
import java.util.Collection;

public class ApplicationsInstalledEvent {

    public final ArrayList<Application> applications = new ArrayList<>();

    public ApplicationsInstalledEvent(Collection<Application> apps) {
        if (apps != null && !apps.isEmpty()) {
            applications.addAll(apps);
        }
    }
}
