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

package com.perfly.android.service.sys;

import android.content.Context;
import android.content.pm.ApplicationInfo;

abstract class AbstractManager {

    protected final Context mContext;
    protected final ApplicationInfo mApplicationInfo;

    protected AbstractManager(Context context, ApplicationInfo applicationInfo) {

        if (applicationInfo == null) {
            throw new NullPointerException("applicationInfo cannot be null");
        }

        mContext = context.getApplicationContext();
        mApplicationInfo = applicationInfo;

    }

    public abstract void start();

    public abstract void stop();
}
