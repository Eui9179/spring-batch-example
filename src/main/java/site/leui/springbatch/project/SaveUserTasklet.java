package site.leui.springbatch.project;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import site.leui.springbatch.project.entity.oders.Orders;
import site.leui.springbatch.project.entity.oders.OrdersRepository;
import site.leui.springbatch.project.entity.user.User;
import site.leui.springbatch.project.entity.user.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaveUserTasklet implements Tasklet {

    private final UserRepository userRepository;
    private final OrdersRepository ordersRepository;

    public SaveUserTasklet(UserRepository userRepository, OrdersRepository ordersRepository) {
        this.userRepository = userRepository;
        this.ordersRepository = ordersRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<User> users = createUsers();

        Collections.shuffle(users);

        userRepository.saveAll(users);
        ordersRepository.saveAll(createOrders(users));

        return RepeatStatus.FINISHED;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .build();
            users.add(user);
        }

        for (int i = 100; i < 200; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .build();
            users.add(user);
        }

        for (int i = 200; i < 300; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .build();
            users.add(user);
        }

        for (int i = 300; i < 400; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .build();
            users.add(user);


        }
        return users;
    }

    private List<Orders> createOrders(List<User> users) {
        List<Orders> orders = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            if (i < 100) {
                orders.add(Orders.builder()
                        .user(users.get(i))
                        .amount(1_000)
                        .build());
            } else if (i < 200) {
                orders.add(Orders.builder()
                        .user(users.get(i))
                        .amount(200_000)
                        .build());
            } else if (i < 300) {
                orders.add(Orders.builder()
                        .user(users.get(i))
                        .amount(300_000)
                        .build());
            } else {
                orders.add(Orders.builder()
                        .user(users.get(i))
                        .amount(500_000)
                        .build());
            }
        }
        return orders;
    }
}
