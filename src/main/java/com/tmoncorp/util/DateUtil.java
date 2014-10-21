package com.tmoncorp.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	private static final Format DB_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Format LOG_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static final String dbDate(Date date) {
		return DB_DATE_FORMATTER.format(date);
	}

	public static final String logDate(Date date) {
		return LOG_DATE_FORMATTER.format(date);
	}
}
