package com.yikuan.androidmedia.base;

/**
 * @author yikuan
 * @date 2020/10/21
 */
public abstract class Worker3<T, E, K> extends Worker {
    protected abstract void configure(T param1, E param2, K param3);
}
