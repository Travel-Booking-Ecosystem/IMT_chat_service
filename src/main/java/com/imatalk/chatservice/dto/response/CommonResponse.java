package com.imatalk.chatservice.dto.response;

import lombok.Data;

@Data
public class CommonResponse {
    private int status;
    private String message;
    private Object data;

    public static CommonResponse success(String message, Object data) {
        CommonResponse response = new CommonResponse();
        response.setStatus(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    public static CommonResponse success(String message) {
        CommonResponse response = new CommonResponse();
        response.setStatus(200);
        response.setMessage(message);
        response.setData(null);
        return response;
    }



    public static CommonResponse error(String message) {
        CommonResponse response = new CommonResponse();
        response.setStatus(400);
        response.setMessage(message);
        return response;
    }



}
