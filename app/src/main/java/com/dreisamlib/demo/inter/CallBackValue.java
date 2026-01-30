package com.dreisamlib.demo.inter;

public interface CallBackValue<T>{
    void succ(T t);
    void fail(int code,String msg);
}
