package org.throwable.support;

import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.ThreadWaitSleeper;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 21:39
 */
@Component
public class RetryTemplateProvider {

    private static final Integer DEFAULT_MAXATTEMPTTIME = 3;
    private static final Integer DEFAULT_INITIALINTERVAL = 100;
    private static final Integer DEFAULT_MAXINTERVAL = 3000;
    private static final Integer DEFAULT_MULTIPLIER = 2;

    public RetryTemplate getRetryTemplate(Integer maxAttemptTime,
                                          Integer initialInterval,
                                          Integer maxInterval,
                                          Integer multiplier) {
        RetryTemplate retryTemplate = new RetryTemplate();
        RetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttemptTime);
        retryTemplate.setRetryPolicy(retryPolicy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMaxInterval(maxInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setSleeper(new ThreadWaitSleeper());
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    public RetryTemplate getDefaultRetryTemplate() {
        return getRetryTemplate(DEFAULT_MAXATTEMPTTIME, DEFAULT_INITIALINTERVAL, DEFAULT_MAXINTERVAL, DEFAULT_MULTIPLIER);
    }
}
