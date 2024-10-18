package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jdbctemplate.JdbcTemplateItemReposotiryV2;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV2Config {


    /**
     * @Import(JdbcTemplateV2Config.class)로 JdbcTemplateV2Config를 애플리케이션 컨텍스트에 등록.
     * 스프링 부트가 애플리케이션 시작 시 JdbcTemplateV2Config의 빈 설정을 스캔.
     * JdbcTemplateV2Config의 생성자를 호출할 때, 스프링이 DataSource 빈을 찾아 주입.
     * 이후, JdbcTemplateV2Config 내부의 빈 설정 메서드(@Bean)들이 실행되어 필요한 빈들이 생성.
     */
    private final DataSource dataSource;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JdbcTemplateItemReposotiryV2(dataSource);
    }
}
