package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * JPA (Java Persistence API)를 직접 사용하는 리포지토리
 * 직접 엔티티 매니저를 통해 DB와 상호작용.
 * 개발자가 JPA의 내부 동작에 대한 이해 필요.
 * 직접적인 트랜잭션 관리가 필요하며, @Transactional을 사용하여 데이터 변경 작업에 대해 명시적으로 트랜잭션을 설정해야 한다.
 * 영속성 컨텍스트를 활용한 변경 감지와 같은 JPA의 기본 기능을 개발자가 직접 이해하고 관리해야 한다.
 */
@Slf4j
@Repository // 이 어노테이션 붙이면 PersistenceExceptionTranslationPostProcessor 관리 대상. 예외 변환 AOP 적용됨.
@Transactional
@RequiredArgsConstructor
public class JpaItemRepositoryV1 implements ItemRepository {

    private final EntityManager em;

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    /*
     dirty checking(변경 감지) : 트랜잭션이 커밋되는 시점에 JPA는 변경된 엔티티를 자동으로 감지하여 필요한 UPDATE 쿼리를 실행.
    테스트 코드에서는 Transactional을 적용하면 무조건 rollback되기 때문에 commit이 발생하지 않고
    자동으로 update도 되지 않는다. test에서 update 확인하려면 @Commit 붙이고 돌리면 된다.
    */
    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String jpql = "select i from Item i"; // i는 별칭.

        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }

        log.info("jpql={}", jpql);

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        return query.getResultList();
    }
}
