package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static hello.itemservice.domain.QItem.*;

@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager em;

    /*
    JPAQueryFactory : 쿼리를 작성하고 실행하는 기능. select(), from(), where() 등의 메서드를 사용해 쿼리를 구성
     */
    private final JPAQueryFactory query;

    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

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

//    @Override
    public List<Item> findAllOld(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        QItem item = QItem.item; // 별칭(item)을 사용하여 생성된 QItem 인스턴스(Q 타입 객체)를 반환
        /*
        * BooleanBuilder
        * 논리적 조건들을 결합하여 Predicate 조건을 만드는 역할.
        * 여러 개의 조건을 and() 또는 or() 등의 메서드를 사용해 하나의 표현식으로 결합.
        * JPAQueryFactory.where(Predicate 조건)에 매개변수로 들어가 where절 구성함.
        * 조건을 순차적으로 추가하면서 논리 연산을 수행.
        * 반면 BooleanExpression은 하나의 조건만을 표시하고, 배열 형식으로 where()에 전달되면 null값은 자동으로 무시됨.
        */

        BooleanBuilder builder = new BooleanBuilder();

        // StringUtils.hasText(String) : 문자열이 null이 아니고, 빈 문자열이 아니며, 공백 문자만으로 이루어지지 않은지를 확인하는 메서드
        if (StringUtils.hasText(itemName)) {
            builder.and(item.itemName.like("%" + itemName + "%"));
        }
        if (maxPrice != null) {
            builder.and(item.price.loe(maxPrice));
        }

        // fetch()는 Querydsl에서 구성된 쿼리를 실행하고 결과를 리스트 형태로 반환하는 메서드

        return query.select(QItem.item).from(QItem.item).where(builder).fetch();
    }

    /*
    * 조건 설정 로직을 별도의 메서드(likeItemName, maxPrice)로 분리
    * 동적 쿼리 작성 시 조건을 추가하는 로직이 메서드로 분리
    * BooleanExpression이 where(predicate 조건)에 배열로 전달될 경우, 자동으로 and로 처리된다.
    * and가 or 등으로 전달하기 위해서는 별로 처리를 한 뒤 전달해야 한다.
    */
    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        return query
                .select(item)
                .from(item)
                .where(likeItemName(cond.getItemName()), maxPrice(cond.getMaxPrice()))
                .fetch();
    }

    /*
    BooleanExpression
     */
    private BooleanExpression likeItemName(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");
        }
        return null;
    }

    private BooleanExpression maxPrice(Integer maxPrice) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }
}
