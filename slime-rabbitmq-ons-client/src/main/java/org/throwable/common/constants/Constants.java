package org.throwable.common.constants;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/28 10:22
 */
public interface Constants {

    String ENCODING = "UTF-8";

    String QUEUE_KEY = "queue";

    String EXCHANGE_KEY = "exchange";

    String ROUTINGKEY_KEY = "routingKey";

    String ARGS_KEY = "args";

    String MESSAGEID_KEY = "messageId";

    String UNIQUECODE_KEY = "uniqueCode";

    String SENDSTATS_KEY = "sendStats";

    String CHECKERCLASSNAME_KEY = "checkerClassName";

    String LOCALTRANSACTIONSTATS_KEY = "localTransactionStats";

    String TRANSACTIONID_KEY = "transactionId";

    String CONTAINERFACTORY_KEY = "simpleRabbitListenerContainerFactory";

    String RABBITADMIN_KEY = "rabbitAdmin";

    String HALFMESSAGEQUEUE_PROPERTIES_KEY = "#{onsProperties['halfMessageQueue']}";

    String FIRETRANSACTIONQUEUE_PROPERTIES_KEY = "#{onsProperties['fireTransactionQueue']}";

    String TRANSACTIONCHECKERQUEUE_PROPERTIES_KEY = "#{onsProperties['transactionCheckerQueue']}";

}
