package com.vintly.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberEvent {

    private String emailAddress;
    private String nickname;
    private String emailTitle;
    private String emailMsg;
    private String emailCode;
    private String baseUrl;

}
