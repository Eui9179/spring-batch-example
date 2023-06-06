package site.leui.springbatch.project.entity.oders;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.leui.springbatch.project.entity.BaseEntity;
import site.leui.springbatch.project.entity.user.User;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Orders extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private int amount;

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Builder
    public Orders(User user, int amount) {
        this.amount = amount;
        this.user = user;
    }
}
