package hello.itemservice.repository;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.mybatis.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MyBatisItemRepository implements ItemRepository {

    /*
    ItemMapper 인터페이스는 구현체가 없지만, MyBatis 스프링 연동 모듈이 자동으로 처리.
    애플리케이션 로딩 시 @Mapper 애노테이션이 붙은 인터페이스를 탐색하고, 이를 바탕으로 동적 프록시 기술을 사용하여 구현체를 생성
    생성된 구현체는 스프링 빈으로 등록되어 주입될 수 있음.
    동적 프록시를 사용해 생성된 매퍼 구현체 덕분에 인터페이스를 통한 프로그래밍이 가능하며, Spring의 예외 추상화 기능까지 사용할 수 있음.
     */
    private final ItemMapper itemMapper;

    @Override
    public Item save(Item item) {
        log.info("itemMapper class = {} ", itemMapper.getClass());
        itemMapper.save(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        itemMapper.update(itemId, updateParam);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemMapper.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        return itemMapper.findAll(cond);
    }
}