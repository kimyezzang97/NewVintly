package com.vintly.interfaces.member;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 비밀번호 검증 규칙 테스트.
 *
 * 특수문자를 11개만 허용하던 화이트리스트 때문에 ^ ( ) - _ 등 흔한 문자가 막혔다.
 * 공백을 뺀 출력 가능한 ASCII 전체를 허용하도록 넓혔고, 다시 좁아지지 않도록 고정한다.
 */
class MemberRequestPasswordTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @ParameterizedTest(name = "허용: {0}")
    @DisplayName("영문·숫자·특수문자를 포함한 8~20자 ASCII 비밀번호는 통과한다.")
    @ValueSource(strings = {
            "ans04020^^!",   // 예전 규칙에서 ^ 때문에 막히던 비밀번호
            "p@ssword1",     // 기존에도 되던 것 (회귀 방지)
            "abc-123_x",
            "abc(123).",
            "abc+123=x,",
            "Aa1!Aa1!",      // 최소 길이 8자
            "Aa1!Aa1!Aa1!Aa1!Aa1!" // 최대 길이 20자
    })
    void acceptsAsciiPasswords(String password) {
        assertThat(validate(password)).isEmpty();
    }

    @ParameterizedTest(name = "거부: {0}")
    @DisplayName("길이·구성 요건을 벗어나거나 공백·한글이 섞이면 거부한다.")
    @ValueSource(strings = {
            "Aa1!Aa1",             // 7자
            "Aa1!Aa1!Aa1!Aa1!Aa1!A", // 21자
            "abcdefgh!",           // 숫자 없음
            "12345678!",           // 영문 없음
            "abcd1234",            // 특수문자 없음
            "abc 1234!",           // 공백
            "비밀번호1234!"          // 한글
    })
    void rejectsInvalidPasswords(String password) {
        assertThat(validate(password)).isNotEmpty();
    }

    private java.util.Set<jakarta.validation.ConstraintViolation<MemberRequest.JoinMember>> validate(String password) {
        MemberRequest.JoinMember request =
                new MemberRequest.JoinMember("테스터", "test@example.com", password);
        return validator.validateProperty(request, "password");
    }
}
