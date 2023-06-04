package site.leui.springbatch.part4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaReaderConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final int CHUNK_SIZE = 5;

    @Bean
    public Job jpaReaderJob() throws Exception {
        return jobBuilderFactory.get("jpaReaderJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaCursorReaderStep())
                .next(jpaPagingItemReaderStep())
                .build();
    }

    @Bean
    public Step jpaCursorReaderStep() throws Exception {
        return stepBuilderFactory.get("jpaReaderStep")
                .<Member, Member>chunk(CHUNK_SIZE)
                .reader(jpaCursorItemReader())
                .writer(itemWriter())
                .build();
    }

    private JpaCursorItemReader<Member> jpaCursorItemReader() throws Exception {
        JpaCursorItemReader<Member> itemReader = new JpaCursorItemReaderBuilder<Member>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select m from Member m")
                .build();
        itemReader.afterPropertiesSet();

        return itemReader;
    }

    @Bean
    public Step jpaPagingItemReaderStep() throws Exception {
        return stepBuilderFactory.get("jpaPagingItemReaderStep")
                .<Member, Member>chunk(CHUNK_SIZE)
                .reader(jpaPagingItemReader())
                .writer(itemWriter())
                .build();
    }

    private JpaPagingItemReader<Member> jpaPagingItemReader() throws Exception {
        JpaPagingItemReader<Member> itemReader = new JpaPagingItemReaderBuilder<Member>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select m from Member m")
                .pageSize(3)
                .build();
        itemReader.afterPropertiesSet();

        return itemReader;
    }

    private ItemWriter<Member> itemWriter() {
        return items -> System.out.println(
                items.stream()
                        .map(Member::getName)
                        .collect(Collectors.joining(", "))
        );
    }
}
