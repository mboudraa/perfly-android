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

import static android.app.ActivityManager.RunningAppProcessInfo.*;

public class ApplicationStatus {

    public static final int PID_NONE = 0;

    public final int pid;
    public final State state;

    public ApplicationStatus() {
        this(PID_NONE, State.NOT_RUNNING);
    }

    public ApplicationStatus(int pid, State state) {

        if (state == null) {
            state = State.NOT_RUNNING;
        }

        if (pid <= 0) {
            pid = PID_NONE;
        }

        this.pid = pid;
        this.state = state;
    }

    public enum State {
        FOREGROUND(new int[]{IMPORTANCE_FOREGROUND, IMPORTANCE_VISIBLE}),
        BACKGROUND(new int[]{IMPORTANCE_BACKGROUND, IMPORTANCE_PERCEPTIBLE, IMPORTANCE_SERVICE}),
        NOT_RUNNING;

        private final int[] mState;

        private State() {
            this(null);
        }

        private State(int[] state) {
            mState = state;
        }

        public static State get(int stateInt) {
            for (State state : State.values()) {
                for (int importance : state.mState) {
                    if (importance == stateInt) {
                        return state;
                    }
                }
            }

            return NOT_RUNNING;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicationStatus)) {
            return false;
        }

        ApplicationStatus that = (ApplicationStatus) o;

        if (pid != that.pid) {
            return false;
        }
        if (state != that.state) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pid;
        result = 31 * result + state.hashCode();
        return result;
    }
}
