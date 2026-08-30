package com.vintly.domain.event;

public interface MemberEventPublisher {

    void publish(MemberEvent event);
}
