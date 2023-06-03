package site.leui.springbatch.part2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SharedConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job sharedJob() {
        return jobBuilderFactory
                .get("sharedJob")
                .incrementer(new RunIdIncrementer())
                .start(this.sharedStep1())
                .next(this.sharedStep2())
                .build();
    }

    @Bean
    public Step sharedStep1() {
        return stepBuilderFactory
                .get("sharedStep1")
                .tasklet((contribution, chunkContext) -> {
                    // BATCH_STEP_EXECUTION, Step이 실행되는 동안 필요한 데이터 혹은 실행된 결과 저장
                    StepExecution stepExecution = contribution.getStepExecution();

                    // BATCH_STEP_EXECUTION_CONTEXT, Step이 실행되는 동안 공유되는 데이터를 직렬화해서 저장
                    ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();

                    // StepExecutionContext에 공유 데이터 저장
                    stepExecutionContext.putString("stepKey", "Step context data");


                    // BATCH_JOB_EXECUTION, Job이 실행되는 동안 시작/종료 시간, 상태등을 관리
                    JobExecution jobExecution = stepExecution.getJobExecution();

                    // BATCH_JOB_INSTANCE
                    JobInstance jobInstance = jobExecution.getJobInstance();

                    // BATCH_JOB_EXECUTION_CONTEXT, Job이 실행되는 동안 공유되는 데이터를 직렬화해서 저장
                    ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();

                    // JobExecutionContext에 공유 데이터 저장
                    jobExecutionContext.putString("jobKey", "Job context data!!");

                    // BATCH_JOB_PARAMETERS, Job을 실행하기 위해 주입된 Parameters
                    JobParameters jobParameters = jobExecution.getJobParameters();

                    System.out.println("\nsharedStep1" +
                            "==========================\n" +
                            "Job shared data: " + jobExecutionContext.getString("jobKey") + "\n" +
                            "Step shared data: " + stepExecutionContext.getString("stepKey") + "\n" +
                            "parameter: " + jobParameters.getLong("run.id") +
                            "\n=====================================\n"
                    ); // incrementer()로 주입된 id

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step sharedStep2() {
        return stepBuilderFactory.get("sharedStep2")
                .tasklet((contribution, chunkContest) -> {
                    StepExecution stepExecution = contribution.getStepExecution();
                    ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();

                    JobExecution jobExecution = stepExecution.getJobExecution();
                    ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();

                    System.out.println( "\nsharedStep2" +
                            "==========================\n" +
                            "Job shared data: " + jobExecutionContext.getString("jobKey", "No data") + "\n" +
                            "Step shared data: " + stepExecutionContext.getString("stepKey", "No data") +
                            "\n=====================================\n"
                    ); // incrementer()로 주입된 id

                    return RepeatStatus.FINISHED;
                })
                .build();
    }


}
