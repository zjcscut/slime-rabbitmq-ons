package org.throwable.common.converter;

import org.springframework.core.convert.converter.Converter;
import org.throwable.common.constants.LocalTransactionStats;

import java.util.Locale;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 20:43
 */
public class LocalTransactionStatsConverter implements Converter<String,LocalTransactionStats> {

    @Override
    public LocalTransactionStats convert(String source) {
        return LocalTransactionStats.valueOf(source.toUpperCase(Locale.US));
    }
}
