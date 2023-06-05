package site.leui.springbatch.part5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;
import site.leui.springbatch.part4.Member;

@Slf4j
public class ExecutionListener {

    public static class JobExecutionListenerExampleImpl implements JobExecutionListener {
        @Override
        public void beforeJob(JobExecution jobExecution) {
            log.info("before job impl");
        }

        @Override
        public void afterJob(JobExecution jobExecution) {

            int sum = jobExecution.getStepExecutions().stream()
                    .mapToInt(StepExecution::getReadCount)
                    .sum();
            log.info("after job impl : {}", sum);
        }
    }

    public static class JobExecutionListenerExampleAnno {
        @BeforeJob
        public void beforeJob(JobExecution jobExecution) {
            log.info("before job anno");
        }

        @AfterJob
        public void afterJob(JobExecution jobExecution) {
            int sum = jobExecution.getStepExecutions().stream()
                    .mapToInt(StepExecution::getReadCount)
                    .sum();
            log.info("after job anno : {}", sum);
        }
    }

    public static class StepExecutionListenerExampleAnno {

        @BeforeStep
        public void beforeStep(StepExecution stepExecution) {
            log.info("before step anno");
        }

        @AfterStep
        public ExitStatus afterStep(StepExecution stepExecution) {
            log.info("after step anno : {}", stepExecution.getWriteCount());
            if (stepExecution.getWriteCount() == 0) {
                return ExitStatus.FAILED;
            }
            return stepExecution.getExitStatus();
        }
    }
}
