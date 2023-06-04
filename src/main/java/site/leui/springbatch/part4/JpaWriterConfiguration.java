package site.leui.springbatch.part4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaWriterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job jpaItemWriterJop() throws Exception {
        return jobBuilderFactory.get("jpaItemWriterJop")
                .incrementer(new RunIdIncrementer())
                .start(jpaItemWriterStep())
                .build();
    }

    @Bean
    public Step jpaItemWriterStep() throws Exception {
        return stepBuilderFactory.get("jpaItemWriterStep")
                .<Member, Member>chunk(10)
                .reader(itemReader())
                .writer(jpaItemWriter())
                .build();
    }

    private ItemReader<Member> itemReader() {
        return new CustomItemReader<>(getItems());
    }

    private JpaItemWriter<Member> jpaItemWriter() throws Exception {
        JpaItemWriter<Member> itemWriter = new JpaItemWriterBuilder<Member>()
                .entityManagerFactory(entityManagerFactory)
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    private List<Member> getItems() {
        List<Member> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new Member("member_" + i));
        }
        return items;
    }
}
