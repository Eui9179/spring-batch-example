package site.leui.springbatch.project1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

@Slf4j
public class UserJobExecutionListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        log.info("데이터 수정 총 {} 건",
                jobExecution.getStepExecutions()
                        .stream()
                        .mapToInt(StepExecution::getWriteCount)
                        .sum());

        log.info("실행 시간 : {}",
                jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime());


    }
}
