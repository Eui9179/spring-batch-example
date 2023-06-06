package site.leui.springbatch.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import site.leui.springbatch.project.entity.oders.Orders;
import site.leui.springbatch.project.entity.oders.OrdersRepository;
import site.leui.springbatch.project.entity.user.User;
import site.leui.springbatch.project.entity.user.UserRepository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class UserConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final UserRepository userRepository;
    private final OrdersRepository ordersRepository;
    private final DataSource dataSource;

    @Bean
    public Job userJob() throws Exception {
        return jobBuilderFactory.get("userJob")
                .incrementer(new RunIdIncrementer())
                .start(saveUserStep())
                .next(userLevelUpStep())
                .listener(new UserJobExecutionListener())
                .next(new JobParametersDecide("date"))
                .on(JobParametersDecide.CONTINUE.getName())
                .to(orderStatisticsStep(null))
                .build()
                .build();
    }

    @Bean
    public Step saveUserStep() {
        return stepBuilderFactory.get("saveUserStep")
                .tasklet(new SaveUserTasklet(userRepository, ordersRepository))
                .build();
    }

    @Bean
    public Step userLevelUpStep() throws Exception {
        return stepBuilderFactory.get("userLevelUpStep")
                .<User, User>chunk(100)
                .reader(userLevelUpReader())
                .processor(userLevelUpProcessor())
                .writer(userLevelUpWriter())
                .build();
    }

    @Bean
    @JobScope
    public Step orderStatisticsStep(@Value("#{jobParameters[date]}") String date) throws Exception {
        return stepBuilderFactory.get("orderStatisticsStep")
                .<OrderStatistics, OrderStatistics>chunk(100)
                .reader(orderStatisticsItemReader(date))
                .writer(orderStatisticsItemWriter(date))
                .build();
    }

    private ItemReader<? extends OrderStatistics> orderStatisticsItemReader(String date) throws Exception {
        YearMonth yearMonth = YearMonth.parse(date);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", yearMonth.atDay(1));
        parameters.put("endDate", yearMonth.atEndOfMonth());

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("created_date", Order.ASCENDING);

        JdbcPagingItemReader<OrderStatistics> itemReader = new JdbcPagingItemReaderBuilder<OrderStatistics>()
                .dataSource(dataSource)
                .rowMapper((resultSet, i) -> OrderStatistics.builder()
                        .amount(resultSet.getString(1))
                        .date(LocalDate.parse(resultSet.getString(2), DateTimeFormatter.ISO_DATE))
                        .build())
                .pageSize(100)
                .name("orderStatisticsItemReader")
                .selectClause("sum(amount), created_date")
                .fromClause("orders")
                .whereClause("created_date >= :startDate and created_date <= :endDate")
                .groupClause("created_date")
                .parameterValues(parameters)
                .sortKeys(sortKey)
                .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }

    private ItemWriter<? super OrderStatistics> orderStatisticsItemWriter(String date) throws Exception {
        YearMonth yearMonth = YearMonth.parse(date);

        String fileName = yearMonth.getYear() + "년_" + yearMonth.getMonthValue() + "월_일별_주문_금액.csv";
        BeanWrapperFieldExtractor<OrderStatistics> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"amount", "date"});

        DelimitedLineAggregator<OrderStatistics> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<OrderStatistics> itemWriter = new FlatFileItemWriterBuilder<OrderStatistics>()
                .resource(new FileSystemResource("output/" + fileName))
                .lineAggregator(lineAggregator)
                .name("orderStatisticsItemWriter")
                .encoding("UTF-8")
                .headerCallback(writer -> writer.write("total_amount_date"))
                .build();

        itemWriter.afterPropertiesSet();
        return itemWriter;
    }


    private JpaPagingItemReader<User> userLevelUpReader() throws Exception {
        JpaPagingItemReader<User> itemReader = new JpaPagingItemReaderBuilder<User>()
                .name("userLevelUpReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select u from User u")
                .pageSize(100)
                .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }

    private ItemProcessor<User, User> userLevelUpProcessor() {
        return user -> {
            if (user.availableLeveUp()) {
                return user;
            }
            return null;
        };
    }

    private ItemWriter<User> userLevelUpWriter() {
        return users -> {
            users.forEach(user -> {
                user.levelUp();
                userRepository.save(user);
            });
        };
    }

    private RepositoryItemReader<Orders> orderStatisticsItemReader() throws Exception {
        LocalDateTime yesterday = LocalDateTime.of(
                LocalDate.now().minusDays(1),
                LocalTime.of(0, 0, 0));
        LocalDateTime today = LocalDateTime.of(
                LocalDate.now(),
                LocalTime.of(23, 59, 59));

        RepositoryItemReader<Orders> itemReader = new RepositoryItemReaderBuilder<Orders>()
                .name("orderStatisticsItemReader")
                .repository(ordersRepository)
                .methodName("findByUpdatedDateBetween")
                .pageSize(100)
                .arguments(List.of(yesterday, today))
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();

        itemReader.afterPropertiesSet();

        return itemReader;
    }
}
