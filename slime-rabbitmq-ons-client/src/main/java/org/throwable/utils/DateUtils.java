package org.throwable.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 22:16
 */
public abstract class DateUtils {

    private static final FastDateFormat dateFormater = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    public static String format(Date date) {
        return dateFormater.format(date);
    }
}
