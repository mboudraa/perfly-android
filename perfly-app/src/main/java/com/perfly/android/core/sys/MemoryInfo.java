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

package com.perfly.android.core.sys;

public class MemoryInfo {

    public final int dalvikLimit;
    public final int appTotal;
    public final int appDalvik;
    public final int appNative;

    public final int pss;
    public final int privateDirty;

    public MemoryInfo(int dalvikLimit, int appTotal, int appDalvik, int pss, int privateDirty) {
        this.dalvikLimit = dalvikLimit;
        this.appTotal = appTotal;
        this.appDalvik = appDalvik;
        this.appNative = appTotal - appDalvik;
        this.pss = pss;
        this.privateDirty = privateDirty;

    }


    @Override
    public String toString() {
        return String.format("Dalvik limit: %dkb, App Total: %dkb, App Dalvik: %dkb, App Native: %dkb",
                             dalvikLimit, appTotal, appDalvik, appNative);

    }


}
