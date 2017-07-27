package org.throwable.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/23 10:24
 */
@Component
public class TransactionTemplateProvider implements ApplicationContextAware {

    private PlatformTransactionManager transactionManager;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
    }

    public TransactionTemplate getDefaultTransactionTemplate(String transactionName) {
        return getTransactionTemplate(transactionName,
                TransactionDefinition.PROPAGATION_REQUIRED,
                TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public TransactionTemplate getDefaultTransactionTemplate() {
        return getTransactionTemplate(null,
                TransactionDefinition.PROPAGATION_REQUIRED,
                TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public TransactionTemplate getTransactionTemplate(int propagationBehavior,
                                                      int isolationLevel) {
        return getTransactionTemplate(null, propagationBehavior, isolationLevel);
    }

    public TransactionTemplate getTransactionTemplate(String transactionName,
                                                      int propagationBehavior,
                                                      int isolationLevel) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(propagationBehavior);
        transactionTemplate.setIsolationLevel(isolationLevel);
        if (null != transactionName) {
            transactionTemplate.setName(transactionName);
        }
        return transactionTemplate;
    }

}
