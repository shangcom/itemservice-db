package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jdbctemplate.JdbcTemplateItemReposotiryV1;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV1Config {

    /**
     * gradle에 jdbc와 같은 의존성 있으면 applications.properties 적은 내용을 바탕으로 DataSource가 빈으로 자동 등록됨.
     * DataSource가 등록되면, 이에 맞는 DataSourceTransactionManager도 함께 자동으로 등록된다.
     * 결과적으로 @Transactional 사용할 수 있게 된다.
     *
     * @Import(JdbcTemplateV1Config.class) JdbcTemplateV1Config를 애플리케이션 컨텍스트에 등록.
     * 스프링 부트가 애플리케이션 시작 시 JdbcTemplateV1Config의 빈 설정을 스캔.
     * JdbcTemplateV1Config의 생성자를 호출할 때, 스프링이 DataSource 빈을 찾아 주입.
     * 이후, JdbcTemplateV1Config 내부의 빈 설정 메서드(@Bean)들이 실행되어 필요한 빈들이 생성.
     */
    private final DataSource dataSource;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JdbcTemplateItemReposotiryV1(dataSource);
    }

}
