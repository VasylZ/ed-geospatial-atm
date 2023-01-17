package com.ed.geospatial.reader.shared;

import java.util.Map;

public class ResponseMessage<T> {

    private T data;
    private int status;
    private Map<String, Object> errors;

    public ResponseMessage() {
    }

    public ResponseMessage(int status, Map<String, Object> errors) {
        this.status = status;
        this.errors = errors;
    }

    public ResponseMessage(T data, int status) {
        this.data = data;
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, Object> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, Object> errors) {
        this.errors = errors;
    }
}
