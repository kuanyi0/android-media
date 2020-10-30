package com.yikuan.androidmedia.base;

import java.util.Arrays;

/**
 * @author yikuan
 * @date 2020/10/21
 */
abstract class Worker {
    protected volatile State mState = State.UNINITIALIZED;

    public abstract void start();

    public abstract void stop();

    public void reset() {
        release();
        mState = State.UNINITIALIZED;
    }

    public abstract void release();

    public State getState() {
        return mState;
    }

    public boolean isRunning() {
        return mState == State.RUNNING;
    }

    protected void checkCurrentStateInStates(State... states) {
        for (State state : states) {
            if (mState == state) {
                return;
            }
        }
        throw new IllegalStateException("current state is " + mState + ", must be in " + Arrays.toString(states));
    }

    protected void checkCurrentStateNotInStates(State... states) {
        for (State state : states) {
            if (mState != state) {
                return;
            }
        }
        throw new IllegalStateException("current state isn't " + mState + ", must't be in " + Arrays.toString(states));
    }
}