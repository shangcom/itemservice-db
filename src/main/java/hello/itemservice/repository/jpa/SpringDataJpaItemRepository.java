package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {


}
