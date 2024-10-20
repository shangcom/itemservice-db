package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * Mapper Interface
 * SQL 쿼리를 호출하기 위한 자바 메서드 정의
 * 각 메서드는 XML 매퍼 파일의 SQL 쿼리와 매핑
 * 메서드의 파라미터는 XML 매퍼 파일에서 #{} 구문을 통해 바인딩
 * '@Param' 애노테이션은 파라미터 이름을 명시적으로 지정
 *  XML 매퍼 파일은 MyBatis가 매퍼 인터페이스(ItemMapper)의 메서드를 호출할 때 사용할 SQL 쿼리를 정의하고,
 *  쿼리 실행 결과를 Java 객체로 매핑하는 기능을 제공
 *  다른 곳에서 주입받아 사용하면 구현체 없어도 자동으로 생성됨(동적 프록시)
 */
@Mapper // MyBatis 매퍼
public interface ItemMapper {

    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    /**
     *  1. xml에서 resultType이 List<Item>이 아니라 Item인 이유?
     * 쿼리가 실행되어 여러 행을 반환하면, MyBatis는 각 행을 하나의 Item 객체로 매핑.
     * 매핑된 Item 객체들이 모여 List<Item> 형태로 반환됨.
     * 2. xml에서 itemSearch.itemName가 아니라 바로 itemName 사용 가능한 이유?
     * 단일 객체 파라미터일 때는 해당 파라미터의 필드들에 직접 접근할 수 있음.
     */
    List<Item> findAll(ItemSearchCond itemSearch);
}
