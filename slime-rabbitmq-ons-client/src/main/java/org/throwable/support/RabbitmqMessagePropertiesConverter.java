package org.throwable.support;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.throwable.common.converter.LocalTransactionStatsConverter;
import org.throwable.common.converter.SendStatsConverter;

import java.util.Date;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/26 23:47
 */
public final class RabbitmqMessagePropertiesConverter {

    public static final String DEFAULT_ENCODING = "UTF-8";

    private volatile MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
    private static final DefaultConversionService conversionService = new DefaultConversionService();

    static {
        conversionService.addConverter(new LocalTransactionStatsConverter());
        conversionService.addConverter(new SendStatsConverter());
    }

    public AMQP.BasicProperties convertToBasicProperties(MessageProperties messageProperties) {
        return messagePropertiesConverter.fromMessageProperties(messageProperties, DEFAULT_ENCODING);
    }

    public void wrapMessageProperties(final Message message,
                                      final String messageId,
                                      final String uniqueCode,
                                      final String queue,
                                      final String exchange,
                                      final String routingKey,
                                      final String checkerClassName,
                                      final String args) {
        MessageProperties messageProperties = message.getMessageProperties();
        messageProperties.setHeader("messageId", messageId);
        messageProperties.setHeader("uniqueCode", uniqueCode);
        if (null != queue)
            messageProperties.setHeader("queue", queue);
        if (null != exchange)
            messageProperties.setHeader("exchange", exchange);
        if (null != routingKey)
            messageProperties.setHeader("routingKey", routingKey);
        if (null != args)
            messageProperties.setHeader("args", args);
        messageProperties.setTimestamp(new Date());
        messageProperties.setHeader("checkerClassName",checkerClassName);
        messageProperties.setContentEncoding(DEFAULT_ENCODING);
    }

    public <T> T getHeaderValue(MessageProperties messageProperties, String key, Class<T> clazz) {
        Object value = messageProperties.getHeaders().get(key);
        if (null == value) return null;
        return conversionService.convert(value, clazz);
    }

    public <T> T getHeaderValue(MessageProperties messageProperties, String key, Class<T> clazz, T def) {
        Object value = messageProperties.getHeaders().get(key);
        if (null == value) return def;
        return conversionService.convert(value, clazz);
    }

    public String getHeaderValue(MessageProperties messageProperties, String key) {
        Object value = messageProperties.getHeaders().get(key);
        if (null == value) return null;
        return conversionService.convert(value, String.class);
    }

    public String getHeaderValue(MessageProperties messageProperties, String key,String def) {
        Object value = messageProperties.getHeaders().get(key);
        if (null == value) return def;
        return conversionService.convert(value, String.class);
    }

}
