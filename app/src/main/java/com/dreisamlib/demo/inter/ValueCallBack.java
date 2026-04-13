package com.dreisamlib.demo.inter;

public interface ValueCallBack<T>{
    void succ(T t);
    void fail(int code,String msg);
}
