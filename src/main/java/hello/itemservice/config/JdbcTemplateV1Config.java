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
     * gradle에 jdbc와 같은 의존성 있으면 applications.config에 적은 내용을 바탕으로 DataSource가 빈으로 자동 등록됨.
     * DataSource가 등록되면, 이에 맞는 DataSourceTransactionManager도 함께 자동으로 등록된다.
     * 결과적으로 @Transactional 사용할 수 있게 된다.
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
