package com.yikuan.androidmedia.base;

/**
 * @author yikuan
 * @date 2020/10/21
 */
public abstract class Worker1<T> extends Worker {
    protected abstract void configure(T param);
}
