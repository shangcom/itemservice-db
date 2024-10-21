package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 스프링 데이터 JPA가 프록시 객체를 통해 자동으로 구현체를 생성하고, 필요한 모든 기능을 제공함.
 * '@Transactional' 필요할 때 자동으로 설정됨.
 * 얘도(SpringJPA) 결국에는 EntityManager 통해 작업한다.
 */
public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {

    /*
    여기까지만 해도, 즉 아무런 코드도 입력하지 않아도 이 인터페이스를 주입받으면
    JpaRepository에 존재하는 기본 CRUD 메서드는 전부 사용 가능하다.
    */
    List<Item> findByItemNameLike(String itemNAme);

    List<Item> findByPriceLessThanEqual(Integer price);


    /**
     * 아래 두 메서드는 같은 메서드이다. 첫번째는 쿼리메서드로, 자동 구현 기능 사용.
     * 두번째는 쿼리 직접 실행.
     * 첫번째와 같이 너무 길어질 때는 개발자가 직접 쿼리 입력하는 것이 낫다.
     */
    List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);

    /*
    Item 엔티티와 매핑된 테이블의 각 행을 대상으로 하여, 조건을 만족하는 모든 행을 Item 객체로 변환해 조회
    여기서 i는 Item 엔티티의 별칭으로 사용되며, itemName과 price 조건을 만족하는 모든 Item 객체를 조회해 리스트 형태로 반환
     */
    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);

/*
    // 예시1. itemName 필드 값들의 리스트가 반환
    @Query("select i.itemName from Item i where i.price > :minPrice")
    List<String> findItemNames(@Param("minPrice") Integer minPrice);
*/

/*
    // 예시 2. 각 행에 대해 itemName과 price 값을 가진 배열(Object[])이 반환
    @Query("select i.itemName, i.price from Item i where i.price > :minPrice")
    List<Object[]> findItemNameAndPrice(@Param("minPrice") Integer minPrice);
*/
}
