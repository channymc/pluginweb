package com.channyanh.channyanhweb.common.exceptions;

import com.channyanh.channyanhweb.common.responses.HttpResponse;

public class HttpException extends Exception {
    public HttpException(HttpResponse response, String message) {
        super(message + " : " + response.code() + " : " + response.message() + " : " + response.body());
    }
}
