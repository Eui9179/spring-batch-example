package site.leui.springbatch.part6;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import site.leui.springbatch.part4.Member;

public class CustomItemProcessor implements ItemProcessor<Member, Member> {
    private final RetryTemplate retryTemplate;

    public CustomItemProcessor() {
        retryTemplate = new RetryTemplateBuilder()
                .maxAttempts(3)
                .retryOn(Exception.class)
                .build();
    }

    @Override
    public Member process(Member item) throws Exception {
        return retryTemplate.execute(context -> {
            // RetryCallback
            return item;
        },context -> {
            // RecoveryCallback
            return item.recover();
        });
    }
}
