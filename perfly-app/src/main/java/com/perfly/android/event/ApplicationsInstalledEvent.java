package com.perfly.android.event;

import com.perfly.android.core.sys.Application;

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
