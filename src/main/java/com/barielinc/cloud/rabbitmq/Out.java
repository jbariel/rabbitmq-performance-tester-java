package com.barielinc.cloud.rabbitmq;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class Out {

	protected enum LogLevel {
		TRACE(0, "TRACE"), DEBUG(10, "DEBUG"), INFO(20, "INFO"), WARN(30, "WARN"), ERROR(40, "ERROR"),
		FATAL(50, "FATAL");

		int value;
		String name;

		private LogLevel(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int value() {
			return this.value;
		}

		public String level() {
			return this.name;
		}

		public boolean doLog(LogLevel level) {
			return value() <= level.value();
		}

	}

	protected LogLevel currentLogLevel = LogLevel.INFO;

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private String clazzName = "unset";

	private boolean toSystemOut = true;

	private String logFileName = null;

	public Out() {
		super();
	}

	public Out(Class<?> clazz) {
		this();
		this.clazzName = (null == clazz) ? StringUtils.EMPTY : clazz.getName();
	}

	public Out(boolean toSystemOut, String logFileName) {
		this(null, toSystemOut, logFileName);
	}

	public Out(Class<?> clazz, boolean toSystemOut, String logFileName) {
		this(clazz);
		this.toSystemOut = toSystemOut;
		this.logFileName = StringUtils.trimToNull(logFileName);
	}

	public void t(String msg, Object... objects) {
		doLog(LogLevel.TRACE, msg, objects);
	}

	public void d(String msg, Object... objects) {
		doLog(LogLevel.DEBUG, msg, objects);
	}

	public void i(String msg, Object... objects) {
		doLog(LogLevel.INFO, msg, objects);
	}

	public void w(String msg, Object... objects) {
		doLog(LogLevel.WARN, msg, objects);
	}

	public void e(String msg, Object... objects) {
		doLog(LogLevel.ERROR, msg, objects);
	}

	public void f(String msg, Object... objects) {
		doLog(LogLevel.FATAL, msg, objects);
	}

	public void setLogLevel(LogLevel newLevel) {
		currentLogLevel = newLevel;
	}

	protected void doLog(LogLevel level, String msg, Object... objects) {
		if (currentLogLevel.doLog(level)) {
			logger(dateTimeAsString() + String.format("[%-5s] ", level.level())
					+ ((StringUtils.isEmpty(this.clazzName) ? "" : String.format("[%s] ", this.clazzName)))
					+ String.format(msg, objects));
		}
	}

	protected void logger(String msg) {
		if (toSystemOut) {
			System.out.println(msg);
		}
		if (null != this.logFileName) {
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new FileWriter(this.logFileName, true), true);
				pw.println(msg);
				pw.close();
			} catch (IOException e) {
				System.err.println("Could not write to file: '" + this.logFileName + "'");
				e.printStackTrace();
			} finally {
				if (null != pw) {
					pw.close();
				}
			}
		}
	}

	public String dateTimeAsString() {
		return String.format("[%s] ", df.format(new Date()));
	}

}
