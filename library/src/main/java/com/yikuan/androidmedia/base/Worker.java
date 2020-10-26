package com.yikuan.androidmedia.base;

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
}
