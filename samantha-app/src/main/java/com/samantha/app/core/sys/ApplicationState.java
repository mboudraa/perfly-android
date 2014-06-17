package com.samantha.app.core.sys;

import android.app.ActivityManager;

public class ApplicationState {

    public static final int PID_NONE = 0;

    public final int pid;
    public final State state;


    public ApplicationState(int pid, State state) {
        this.pid = pid;
        this.state = state;
    }

    public enum State {
        FOREGROUND(ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND), BACKGROUND(
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND), NOT_RUNNING(-1);

        private final int mState;

        private State(int state) {
            mState = state;
        }

        public static State get(int stateInt){
            for(State state : State.values()){
                if(state.mState == stateInt){
                    return state;
                }
            }

            return NOT_RUNNING;
        }
    }
}
