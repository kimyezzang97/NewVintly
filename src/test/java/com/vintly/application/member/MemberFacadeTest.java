package com.vintly.application.member;

import com.vintly.domain.auth.service.AuthService;
import com.vintly.domain.event.MemberEventPublisher;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.service.MemberService;
import com.vintly.interfaces.member.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MemberFacadeTest {

    private static final String EMAIL = "test@example.com";

    @Mock
    private MemberService memberService;

    @Mock
    private MemberEventPublisher memberEventPublisher;

    @Mock
    private AuthService authService;

    @InjectMocks
    private MemberFacade memberFacade;

    @Test
    @DisplayName("회원 탈퇴 시 회원 삭제 후 refresh 토큰까지 제거한다.")
    void withdrawMemberDeletesMemberAndRefreshToken() {
        // Arrange
        Member member = new Member(1L, EMAIL, "encoded", "nickname",
                "code", "ROLE_USER", Use.Y, null, null);
        Mockito.when(memberService.findByEmail(EMAIL)).thenReturn(Optional.of(member));

        // Act
        memberFacade.withdrawMember(EMAIL, "password");

        // Assert
        InOrder inOrder = Mockito.inOrder(memberService, authService);
        inOrder.verify(memberService).withdrawMember(member, "password");
        inOrder.verify(authService).deleteRefreshToken(EMAIL);
    }

    @Test
    @DisplayName("존재하지 않는 회원이 탈퇴를 요청하면 MemberNotFoundException이 발생한다.")
    void withdrawMemberThrowsWhenMemberNotFound() {
        // Arrange
        Mockito.when(memberService.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                MemberException.MemberNotFoundException.class,
                () -> memberFacade.withdrawMember(EMAIL, "password")
        );
        Mockito.verify(authService, Mockito.never()).deleteRefreshToken(ArgumentMatchers.any());
    }
}
