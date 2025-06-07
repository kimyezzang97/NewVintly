package com.vintly.domain.mail.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Mail {
    private String address;
    private String title;
    private String message;

    @Builder
    public Mail(String address, String title, String message){
        this.address = address;
        this.title = title;
        this.message = message;
    }
}
