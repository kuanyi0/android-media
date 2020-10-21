package com.yikuan.androidmedia.base;

/**
 * @author yikuan
 * @date 2020/10/21
 */
abstract class Worker {
    protected volatile State mState = State.UNINITIALIZED;

    protected abstract void start();

    protected abstract void stop();

    protected void reset() {
        release();
        mState = State.UNINITIALIZED;
    }

    protected abstract void release();

    public State getState() {
        return mState;
    }
}
