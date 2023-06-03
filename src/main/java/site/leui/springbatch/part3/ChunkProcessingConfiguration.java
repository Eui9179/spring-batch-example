package site.leui.springbatch.part3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ChunkProcessingConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkProcessingJob() {
        return jobBuilderFactory.get("chunkProcessingJob")
                .incrementer(new RunIdIncrementer())
                .start(this.taskBaseStep())
                .next(chunkBaseStep())
                .build();
    }

    @Bean
    public Step chunkBaseStep() {
        return stepBuilderFactory.get("chunkBaseStep")
                // 10개씩 실행
                // <String, String>
                // 첫 번째 타입: itemReader에 들어올 INPUT 타입의 타입
                // 두 번째 타입: itemProcessor에서 처리한 OUTPUT 타입의 타입
                .<String, String>chunk(10)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemReader<String> itemReader() {
        return new ListItemReader<>(getItems()); // getItems의 값을 List로서 읽어옴
    }

    private ItemProcessor<? super String, String> itemProcessor() {
        return item -> item + ", Spring Batch"; // 각 아이템에 ", Spring Batch" 를 붙임
    }

    private ItemWriter<? super String> itemWriter() {
//        return items -> log.info("chunk item size : {}", items.size()); // processor의 결과 값을 log로 찍음
        return items -> items.forEach(log::info);
    }

    @Bean
    public Step taskBaseStep() {
        return stepBuilderFactory.get("taskBaseStep")
                .tasklet(this.tasklet())
                .build();
    }

    private Tasklet tasklet() {
        return ((contribution, chunkContext) -> {
            List<String> items = getItems();
            log.info("task item size: {}", items.size());
            return RepeatStatus.FINISHED;
        });
    }

    private List<String> getItems() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(i + "_item");
        }
        return items;
    }
}
