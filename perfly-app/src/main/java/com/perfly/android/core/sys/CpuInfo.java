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

public class CpuInfo {

    public final int cpuTotal;
    public final int cpuUser;
    public final int cpuKernel;

    public CpuInfo(int cpuTotal, int cpuUser, int cpuKernel) {
        this.cpuTotal = cpuTotal;
        this.cpuUser = cpuUser;
        this.cpuKernel = cpuKernel;
    }


    @Override
    public String toString() {
        return String.format("CPU Total: %d%%, User: %d%%, Kernel: %d%%",
                             cpuTotal, cpuUser, cpuKernel);

    }


}
