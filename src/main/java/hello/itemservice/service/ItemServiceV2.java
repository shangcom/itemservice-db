package hello.itemservice.service;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.v2.ItemQueryRepositoryV2;
import hello.itemservice.repository.v2.ItemRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceV2 implements ItemService {

    /*
    ItemRepositoryV2, ItemQueryRepositoryV2에 의존.

    1. ItemRepositoryV2는 인터페이스로 JpaRepository를 확장하며, 이를 통해 기본적인 crud 작업 가능.
       프록시 객체 만들어서 자동으로 주입됨.

    2. ItemQueryRepositoryV2는 복잡한 쿼리 처리하기 위해 QueryDSL을 사용하는 클래스.
       V1에서는 ItemRepository에 통합하기 위해서 어댑터를 활용하였으나,
       V2에서는 어댑터를 생략하는 대신 Repository를 둘로 나누어 복잡한 쿼리를 처리하는 Repository를 만들고,
       이를 ServiceV2에서 주입받아 사용하는 방식 사용.
     */
    private final ItemRepositoryV2 itemRepositoryV2;
    private final ItemQueryRepositoryV2 itemQueryRepositoryV2;

    @Override

    public Item save(Item item) {
        return itemRepositoryV2.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = itemRepositoryV2.findById(itemId).orElseThrow(); // Optional 객체를 원본 타입으로 반환.
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemRepositoryV2.findById(id);
    }

    @Override
    public List<Item> findItems(ItemSearchCond itemSearch) {
        return itemQueryRepositoryV2.findAll(itemSearch);
    }
}
