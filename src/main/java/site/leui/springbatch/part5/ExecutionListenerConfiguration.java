package site.leui.springbatch.part5;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.leui.springbatch.part4.Member;

import javax.persistence.EntityManagerFactory;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class ExecutionListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job executionListenerExampleJob() {
        return jobBuilderFactory.get("executionListenerExampleJob")
                .incrementer(new RunIdIncrementer())
                .start(executionListenerExampleStep())
                .listener(new ExecutionListener.JobExecutionListenerExampleImpl())
                .listener(new ExecutionListener.JobExecutionListenerExampleAnno())
                .build();
    }

    @Bean
    public Step executionListenerExampleStep() {
        return stepBuilderFactory.get("executionListenerExampleStep")
                .<Member, Member>chunk(10)
                .reader(jpaItemReader())
                .writer(itemWriter())
                .build();
    }

    private ItemReader<Member> jpaItemReader() {
        JpaPagingItemReader<Member> itemReader = new JpaPagingItemReaderBuilder<Member>()
                .name("jpaItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select m from Member m")
                .pageSize(10)
                .build();

        try {
            itemReader.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return itemReader;
    }

    private ItemWriter<Member> itemWriter() {
        return items -> {
            log.info(items.stream()
                    .map(Member::getName)
                    .collect(Collectors.joining(", ")));
        };
    }
}
