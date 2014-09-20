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
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

public abstract class BaseJob extends Job {

    private final Context mContext;

    protected BaseJob(Context context) {
        super(new Params(Priority.NORMAL).setRequiresNetwork(false).setPersistent(false).delayInMs(0));
        mContext = context.getApplicationContext();
    }

    @Override
    public void onAdded() {}

    @Override
    protected void onCancel() {}


    protected Context getContext(){
        return mContext;
    }

}
