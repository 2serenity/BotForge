package com.rofls.botforge.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    private static final SimpleDateFormat FULL_FORMAT =
            new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

    private DateUtils() {
    }

    public static String format(long timestamp) {
        if (timestamp <= 0L) {
            return "нет";
        }
        return FULL_FORMAT.format(new Date(timestamp));
    }
}
