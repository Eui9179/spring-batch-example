package site.leui.springbatch.project1;

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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class UserConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final UserRepository userRepository;

    @Bean
    public Job userJob() throws Exception {
        return jobBuilderFactory.get("userJob")
                .incrementer(new RunIdIncrementer())
                .start(saveUserStep())
                .next(userLevelUpStep())
                .listener(new UserJobExecutionListener())
                .build();
    }

    @Bean
    public Step saveUserStep() throws Exception {
        return stepBuilderFactory.get("saveUserStep")
                .tasklet(new SaveUserTasklet(userRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step userLevelUpStep() throws Exception {
        return stepBuilderFactory.get("userLevelUpStep")
                .<User, User>chunk(100)
                .reader(userLevelUpReader())
                .processor(userLevelUpProcessor())
                .writer(userLevelUpWriter())
                .build();
    }

    @StepScope
    public JpaPagingItemReader<User> userLevelUpReader() throws Exception {
        JpaPagingItemReader<User> itemReader =  new JpaPagingItemReaderBuilder<User>()
                .name("userLevelUpReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select u from User u")
                .pageSize(100)
                .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }

    @StepScope
    public ItemProcessor<User, User> userLevelUpProcessor() {
        return user -> {
            if (user.availableLeveUp()) {
                return user;
            }
            return null;
        };
    }

    @StepScope
    public ItemWriter<User> userLevelUpWriter() {
        return users -> {
            users.forEach(user -> {
                user.levelUp();
                userRepository.save(user);
            });
        };
    }
}
