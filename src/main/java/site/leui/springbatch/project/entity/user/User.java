package site.leui.springbatch.project.entity.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.leui.springbatch.project.entity.BaseEntity;
import site.leui.springbatch.project.entity.oders.Orders;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Level level = Level.NORMAL;

    @OneToMany(mappedBy = "user")
    private List<Orders> orders = new ArrayList<>();

    @Builder
    private User(String username) {
        this.username = username;
    }

    public boolean availableLeveUp() {
        return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
    }

    public int getTotalAmount() {
        return orders.stream()
                .mapToInt(Orders::getAmount)
                .sum();
    }

    public Level levelUp() {
        Level nextLevel = Level.getNextLevel(this.getTotalAmount());
        this.level = nextLevel;

        return nextLevel;
    }

    public enum Level {
        VIP(500_000, null),
        GOLD(500_000, VIP),
        SILVER(300_000, GOLD),
        NORMAL(200_000, SILVER);

        private final int nextAmount;
        private final Level nextLevel;

        Level(int nextAmount, Level nextLevel) {
            this.nextAmount = nextAmount;
            this.nextLevel = nextLevel;
        }

        private static boolean availableLevelUp(Level level, int totalAmount) {
            if (Objects.isNull(level)) {
                return false;
            }

            if (Objects.isNull(level.nextLevel)) {
                return false;
            }

            return totalAmount >= level.nextAmount;
        }

        private static Level getNextLevel(int totalAmount) {
            if (totalAmount >= Level.VIP.nextAmount) {
                return VIP;
            }

            if (totalAmount >= Level.GOLD.nextAmount) {
                return GOLD.nextLevel;
            }

            if (totalAmount >= Level.SILVER.nextAmount) {
                return SILVER.nextLevel;
            }

            if (totalAmount >= Level.NORMAL.nextAmount) {
                return NORMAL.nextLevel;
            }

            return NORMAL;
        }
    }
}