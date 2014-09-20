/*
 * Copyright (c) 2014 Mounir Boudraa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perfly.android.job;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import com.perfly.android.core.sys.Application;
import com.perfly.android.event.ApplicationsInstalledEvent;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ListInstalledApplicationsJob extends BaseJob {

    private PackageManager mPackageManager;

    public ListInstalledApplicationsJob(Context context) {
        super(context);
        mPackageManager = context.getPackageManager();
    }

    @Override
    public void onRun() throws Throwable {
        ArrayList<Application> apps = new ArrayList<Application>();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> resolveInfoList = mPackageManager.queryIntentActivities(mainIntent, 0);


        for (ResolveInfo info : resolveInfoList) {
            ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;

            if (!isSystemPackage(applicationInfo)) {
                CharSequence label = mPackageManager.getApplicationLabel(applicationInfo);
                Drawable logo = mPackageManager.getApplicationIcon(applicationInfo);
                String version = mPackageManager.getPackageInfo(applicationInfo.packageName, 0).versionName;
                int uid = applicationInfo.uid;
                boolean debuggable = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
                String packageName = applicationInfo.packageName;

                apps.add(new Application(uid, label.toString(), logo, version, packageName, debuggable));
            }
        }

        EventBus.getDefault().post(new ApplicationsInstalledEvent(apps));

    }

    private boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
