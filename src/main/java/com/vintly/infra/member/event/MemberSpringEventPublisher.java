package com.vintly.infra.member.event;

import com.vintly.domain.event.MemberEvent;
import com.vintly.domain.event.MemberEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MemberSpringEventPublisher implements MemberEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public MemberSpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(MemberEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
