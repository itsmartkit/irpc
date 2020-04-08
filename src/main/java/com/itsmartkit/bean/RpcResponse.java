package com.itsmartkit.bean;

import java.io.Serializable;

/**
 * @author cyj
 * @name RpcResponse
 * @description TODO
 * @date 2020/4/7 12:33
 * Version 1.0
 */
public class RpcResponse implements Serializable {

    private Long responseId;

    private Throwable error;

    private Object result;

    public boolean hasError(){
        return error != null;
    }

    public Long getResponseId() {
        return responseId;
    }

    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}