package site.leui.springbatch.part1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class HelloConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    /**
     * job은 배치의 실행단위이다.
     * job을 만들 수 있게 JobBuilderFactory를 지원한다.
     * @return Job
     */
    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("helloJob")
                .incrementer(new RunIdIncrementer())
                .start(this.helloStep()) // 최초로 실행할 스텝
                .build();
    }

    /**
     * step은 job의 실행단위이다.
     * step을 만들 수 있게 StepBuilderFactory를 지원한다.
     * @return Step
     */
    @Bean
    public Step helloStep() {
        return stepBuilderFactory.get("helloStep")
                .tasklet((contribution, chunkContext) -> { // task 기반
                    log.info("hello spring batch");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
