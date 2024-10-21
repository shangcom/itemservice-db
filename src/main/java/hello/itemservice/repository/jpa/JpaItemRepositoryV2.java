package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/*
어댑터 패턴.
v1에서는 바로 EntityManager를 사용했음.
여기서는 SpringJPA 인터페이스를 확장한 SpringDataJpaItemRepository 인터페이스를 프록시 객체로 주입 받아 사용한다.
ItemRepository의 메서드를 코드로 직접 구현하는 것이 아니라, 주입받은 SpringDataJpaItemRepository의 메서드를 사용한다.
즉 겉으로는 여전히 ItemRepository를 사용하지만, 속으로는 SpringJPA를 통해 EntityManager를 사용하고 있다.
이러한 방식으로 Service 계층에서는 여전히 ItemRepository만을 주입받아 사용할 수 있다.
SpringDataJpaItemRepository는 ItemRepository 인터페이스를 직접 구현하지 않기 때문에, ItemService에서 바로 사용할 수 없다.
이를 해결하기 위해, JpaItemRepositoryV2가 SpringDataJpaItemRepository를 감싸고, ItemRepository를 구현하는 방식으로
어댑터 역할을 한다. 이렇게 하면, ItemService는 ItemRepository 인터페이스만을 주입받도록 유지할 수 있으며,
JpaItemRepositoryV2를 통해 SpringDataJpaItemRepository를 간접적으로 사용할 수 있다.
만약 JpaItemRepositoryV2와 같은 어댑터가 없다면, ItemService에서는 직접 SpringDataJpaItemRepository와 관련된
코드 변경이 필요해진다.
 */
@Repository
@RequiredArgsConstructor
@Transactional
public class JpaItemRepositoryV2 implements ItemRepository {

    /*
    구현체 없이 SpringDataJpaItemRepository 인터페이스까지만 만들어 둬도
    스프링 데이터 JPA가 런타임에 해당 인터페이스의 구현체를 프록시 객체로 자동으로 생성하여 빈으로 등록하고,
    이 프록시 객체가 JPA를 사용해 DB와 상호작용 한다.
     */
    private final SpringDataJpaItemRepository repository;

    @Override
    public Item save(Item item) {
        return repository.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = repository.findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    /*
     * repository.findById() 메서드 타고 들어가보면 반호나타입 Optional이라서 그대로 반환하면 된다.
     */
    @Override
    public Optional<Item> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        if (StringUtils.hasText(itemName) && maxPrice != null) {
        //return repository.findByItemNameLikeAndPriceLessThanEqual("%" + itemName + "%", maxPrice);
            return repository.findItems("%" + itemName + "%", maxPrice);
        } else if (StringUtils.hasText(itemName)) {
            return repository.findByItemNameLike("%" + itemName + "%");
        } else if (maxPrice != null) {
            return repository.findByPriceLessThanEqual(maxPrice);
        } else {
            return repository.findAll();
        }
    }
}
