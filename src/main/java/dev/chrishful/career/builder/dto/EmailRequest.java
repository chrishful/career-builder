package dev.chrishful.career.builder.dto;

import java.util.Map;

public class EmailRequest {
    private Map<String, String> headers;
    private String plain;

    public EmailRequest() {
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getPlain() {
        return plain;
    }
    public void setPlain(String plain) {
        this.plain = plain;
    }

}