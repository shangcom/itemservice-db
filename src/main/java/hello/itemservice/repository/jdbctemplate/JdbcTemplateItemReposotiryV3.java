package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SimpleJdbcInsert : 스프링에서 제공하는 JDBC 유틸리티
 * update, delete는 없음. Insert만 제공.
 */
@Slf4j
@Repository
public class JdbcTemplateItemReposotiryV3 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateItemReposotiryV3(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("item")
                .usingGeneratedKeyColumns("id")
                .usingColumns("item_name", "price", "quantity"); // 이 줄은 생략 가능.
    }

    @Override
    public Item save(Item item) {
//        String sql = "insert into item(item_name, price, quantity) " +
//                "values (:itemName, :price, :quantity)";
//        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
//
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        template.update(sql, param, keyHolder);
//
//        long key = keyHolder.getKey().longValue();
//        item.setId(key);
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        Number key = jdbcInsert.executeAndReturnKey(param);
        item.setId(key.longValue());
        return item;
    }

    /**
     * MapSqlParameterSource를 사용하여 sql의 ':파라미터'와 필드 값을 수동으로 매핑.
     * addValue 메서드 순서 상관 없이 이름(:파라미터) 기준으로 값이 매핑됨.
     * 즉, 파라미터 이름만 정확히 맞추면, 쿼리에서 올바르게 바인딩 된다.
     */
    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item " +
                "set item_name= :itemName, price= :price, quantity= :quantity " +
                "where id= :id";

        /**
         * ItemUpdateDto에는 id 필드가 없어서, BeanPropertySqlParameterSource를 사용하면
         * id 파라미터를 채울 수 없음.
         * 즉, 파라미터와 필드 이름이 다르거나 부족한 파라미터가 있을 경우에는
         * MapSqlParameterSource를 사용해야 한다.
         * 단,  도메인 객체의 필드와 파라미터명이 일치하지 않아도 sql문에 as를 사용해서 파라미터에
         * 도메인 객체의 필드명과 같은 별칭을 붙여주면 매핑시킬 수 있다. 즉, BeanPropertySqlParameterSource를
         * 사용할 수 있다. 그러나 이 방식은 확장성을 저해하고 책임 분리 원칙에서 멀어지기 때문에 지양하자.
         */
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);

        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select * from item where id = :id";

        try {
            Map<String, Long> param = Map.of("id", id);
            /**
             * itemRowMapper()는 RowMapper<Item> 객체를 반환하는 메서드로,
             * 이 RowMapper는 ResultSet에서 각 컬럼의 값을 읽어와 Item 객체의 필드에 설정하는 기능을 가지고 있다.
             * queryForObject 메서드는 SQL 쿼리를 실행한 후, 결과를 itemRowMapper()가 제공하는 RowMapper를 통해
             * 각 행의 데이터를 Item 객체로 매핑한다.
             *
             * 이때, NamedParameterJdbcTemplate은 param에 지정된 이름 기반의 파라미터("id" 값을 가진 Map)를 사용하여
             * SQL 쿼리에서 :id 부분을 바인딩하고, 쿼리를 실행한다.
             *
             * queryForObject는 매핑된 Item 객체를 반환하며,
             * 조회된 결과가 없으면 EmptyResultDataAccessException 예외를 던진다.
             * queryForObject는 반환 결과가 1개일 때 사용. 복수일 경우 List를 반환하는 query() 사용.
             */
            Item item = template.queryForObject(sql, param, itemRowMapper());
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "select id, item_name, price, quantity from item";

        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',:itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";
        }

        log.info("sql={}", sql);
        return template.query(sql, param, itemRowMapper());

    }

    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);
    }

}

