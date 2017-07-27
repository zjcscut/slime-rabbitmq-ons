package org.throwable.common.converter;

import org.springframework.core.convert.converter.Converter;
import org.throwable.common.constants.SendStats;

import java.util.Locale;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 20:45
 */
public class SendStatsConverter implements Converter<String, SendStats> {

    @Override
    public SendStats convert(String source) {
        return SendStats.valueOf(source.toUpperCase(Locale.US));
    }
}
