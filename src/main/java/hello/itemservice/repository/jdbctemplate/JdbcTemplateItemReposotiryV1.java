package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class JdbcTemplateItemReposotiryV1 implements ItemRepository {

    private final JdbcTemplate template;

    public JdbcTemplateItemReposotiryV1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    /**
     * 새로운 레코드를 삽입(insert)할 때 데이터베이스가 자동 생성한 id 값을 가져오기 위해 KeyHolder를 사용.
     * KeyHolder : 데이터베이스가 자동 생성하는 키(예: id 값)를 저장하는 객체
     * 여기서는 데이터가 삽입될 때 생성된 id 값을 저장하기 위해 사용
     */
    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price, quantity) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        /**
         * template.update(PreparedStatementCreator preparedStatementCreator, KeyHolder generatedKeyHolder)
         * - 첫 번째 매개변수는 PreparedStatementCreator 인터페이스를 람다식으로 구현.
         *   이 람다식은 Connection을 받아서 PreparedStatement를 생성하고, SQL을 준비하는 역할.
         * - 두 번째 매개변수는 앞서 생성한 keyHolder. 자동으로 생성된 키 값을 저장하기 위해 사용.
         */
        template.update(connection -> {
            /**
             * 자동 증가 키 설정
             * - 현재 데이터베이스에서 자동 생성되는 키는 "id" 컬럼 하나뿐.
             * - 따라서, new String[]{"id"}로 배열을 전달.
             * - 자동 생성된 키가 여러 개일 수 있는 상황을 고려해 PreparedStatement의 두 번째 매개변수는
             *   문자열 배열(String[])로 지정됨. 예를 들어, 다른 키가 추가로 자동 생성되는 경우에도
             *   배열을 통해 여러 개의 컬럼 이름을 지정할 수 있음.
             */
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps; // 준비된 PreparedStatement를 반환
        }, keyHolder);

        /*
         * 자동 생성된 키 값 가져오기
         * - 현재 자동 생성된 키는 "id" 하나뿐이므로 keyHolder.getKey()로 직접 값을 가져올 수 있음.
         * - 만약 자동 생성된 키가 여러 개였다면, keyHolder.getKeyList()를 사용해
         *   List<Map<String, Object>> 형태로 모든 키를 받아와야 함.
         */
        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item; // id가 설정된 Item 객체를 반환.
    }

    /**
     * @param itemId      여기서는 save할 때 이미 아이디가 만들어졌으므로 keyholder 필요 없음.
     * @param updateParam dto객체.
     */
    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";
        template.update(
                sql,
                updateParam.getItemName(), updateParam.getPrice(), updateParam.getQuantity(),
                itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {
        // SQL 쿼리: 주어진 id에 해당하는 레코드를 item 테이블에서 조회
        String sql = "select * from item where id = ?";

        try {
            // queryForObject 메서드를 사용해 데이터베이스에서 단일 행을 조회하고, 결과를 Item 객체로 매핑
            // 첫 번째 매개변수: 실행할 SQL 쿼리
            // 두 번째 매개변수: RowMapper를 통해 ResultSet을 Item 객체로 변환
            // 세 번째 매개변수: SQL 쿼리의 ? 자리에 바인딩할 id 값
            Item item = template.queryForObject(sql,
                    itemRowMapper(), // RowMapper<Item>를 반환하는 메서드
                    id); // 바인딩할 id 값

            // 조회된 결과가 있으면 Optional로 감싸서 반환
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            // 데이터베이스 조회 결과가 없으면 예외 발생, 이때 Optional.empty()를 반환
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "select id, item_name, price, quantity from item";
//동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',?,'%')";
            param.add(itemName);
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice);


        }

        log.info("sql={}", sql);
        return template.query(sql, itemRowMapper(), param.toArray());

    }

    // RowMapper<Item> 인터페이스를 구현해 ResultSet의 데이터를 Item 객체로 매핑하는 메서드
    private RowMapper<Item> itemRowMapper() {
        // 람다식으로 RowMapper를 구현
        // (rs, rowNum) -> {...}은 mapRow 메서드를 람다식으로 표현한 것
        // rs: ResultSet 객체, 데이터베이스 조회 결과를 담고 있음
        // rowNum: 현재 행 번호를 나타냄 (첫 번째 행은 1부터 시작)
        return (rs, rowNum) -> {
            // 새로운 Item 객체를 생성
            Item item = new Item();

            // ResultSet에서 각 컬럼의 값을 가져와 Item 객체의 필드에 설정
            item.setId(rs.getLong("id")); // id 컬럼의 값을 Item 객체의 id 필드에 설정
            item.setItemName(rs.getString("item_name")); // item_name 컬럼의 값을 Item 객체의 itemName 필드에 설정
            item.setPrice(rs.getInt("price")); // price 컬럼의 값을 Item 객체의 price 필드에 설정
            item.setQuantity(rs.getInt("quantity")); // quantity 컬럼의 값을 Item 객체의 quantity 필드에 설정

            // 매핑된 Item 객체를 반환
            return item;
        };
    }
}
