package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity // JPA에 엔티티로 등록.
//@Table(name = "item") // 매핑할 테이블 지정. 테이블 명이 객체 명과 동일할 때는 생략 가능.
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    객체의 필드를 테이블의 컬럼과 매핑.
    생략하면 필드 이름을 테이블 칼럼 이름으로 사용.
    카멜 케이스를 언더스코어로 자동 변환해줌으로 itemName에서도 생략 가능.
     */
    @Column(name = "item_name", length = 10)
    private String itemName;
    private Integer price;
    private Integer quantity;

    /**
     * JPA 사용할 경우, public 또는 protected 기본 생성자 필수.
     */
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
