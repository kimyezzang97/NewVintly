package com.vintly;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트용 MariaDB / Redis 컨테이너를 제공하는 부모 클래스. 통합 테스트는 이 클래스를 상속한다.
 *
 * <p>컨테이너는 {@code static}이라 JVM 당 한 벌만 뜨고 모든 통합 테스트가 공유한다.
 *
 * <p><b>왜 {@code @Import}가 아니라 상속인가</b>
 * <ul>
 *   <li>{@code @DynamicPropertySource}는 <b>테스트 클래스와 그 상위 클래스</b>에서만 처리된다.
 *       {@code @Import}된 {@code @TestConfiguration}에 두면 조용히 무시되어, 컨테이너는 떠 있는데
 *       정작 테스트는 클래스패스의 임베디드 H2에 붙는다.</li>
 *   <li>{@code @ServiceConnection}은 {@code @Import}로도 동작하지만 스프링 부트의 자동 설정을 전제로 한다.
 *       이 프로젝트의 {@code RedisConfig}는 {@code @Value("${spring.data.redis.host}")}로 커넥션 팩토리를
 *       직접 만들기 때문에 Redis 쪽에는 적용되지 않는다 — 그러면 컨테이너가 아니라 개발자 PC의 로컬
 *       Redis에 붙어버린다.</li>
 * </ul>
 * 두 경우 모두 <b>테스트는 통과하는데 의도한 대상이 아닌 곳을 검증</b>하게 되므로, 속성을 명시적으로
 * 주입하는 이 방식을 쓴다.
 *
 * <p>실행에는 Docker 가 필요하다. 그 외 설정은 커밋된 {@code src/test/resources/application-test.yml}에 있어
 * 클론 직후 별도 세팅 없이 {@code ./gradlew test}로 동작한다.
 */
public abstract class TestContainerConfig {

    private static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>(
            DockerImageName.parse("mariadb:10.11"))
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:7.4.2"));

    static {
        MARIADB.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MARIADB::getJdbcUrl);
        registry.add("spring.datasource.username", MARIADB::getUsername);
        registry.add("spring.datasource.password", MARIADB::getPassword);
        registry.add("spring.datasource.driver-class-name", MARIADB::getDriverClassName);

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }
}
