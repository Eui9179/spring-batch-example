package site.leui.springbatch.project.entity.oders;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findByUpdatedDateBetween(LocalDateTime yesterday, LocalDateTime today,  Pageable pageable);
    List<Orders> findByUpdatedDateBetween(LocalDateTime yesterday, LocalDateTime today);
}
