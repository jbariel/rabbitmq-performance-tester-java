package com.barielinc.cloud.rabbitmq;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Tcp;

public class StatsInfo {
	private static final String DEFAULT_LOG_FILE_DIR = StringUtils.EMPTY;

	private final Out o = new Out(this.getClass());

	private final Sigar sigar = new Sigar();

	private List<String> interfaces;

	private String logDir;

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private final String loadOutName = "load-metrics";
	private Out loadOut;

	private final String cpuMemNetOutName = "cpu-mem-net-metrics";
	private Out cpuMemNetOut;

	private final String tcpOutName = "tcp-info-metrics";
	private Out tcpOut;

	private final String interfaceOutPrefix = "interface";
	private Map<String, Out> interfaceOuts = new HashMap<>();

	public StatsInfo() {
		this(DEFAULT_LOG_FILE_DIR);
	}

	public StatsInfo(String logFileDir) {
		super();
		this.logDir = StringUtils.trimToEmpty(logFileDir);
		if (this.logDir.endsWith(File.pathSeparator)) {
			this.logDir = this.logDir.substring(0, this.logDir.length() - 2);
		}

		try {
			this.interfaces = Arrays.asList(sigar.getNetInterfaceList());
		} catch (SigarException e) {
			o.f("Could not get the list of interfaces!!!");
			e.printStackTrace();
		}

		setupOutputs();
	}

	private String getFullLogFileName(String basename) {
		return String.format("%s%s-%s.log",
				((StringUtils.isEmpty(this.logDir)) ? this.logDir : (this.logDir + File.pathSeparator)),
				df.format(new Date()), basename);
	}

	private void setupOutputs() {
		this.loadOut = new Out(false, getFullLogFileName(this.loadOutName));
		this.cpuMemNetOut = new Out(false, getFullLogFileName(this.cpuMemNetOutName));
		this.tcpOut = new Out(false, getFullLogFileName(this.tcpOutName));

		this.interfaces.forEach(e -> this.interfaceOuts.put(e,
				new Out(false, getFullLogFileName(String.format("%s-%s", this.interfaceOutPrefix, e)))));

		writeHeaders();
	}

	private void writeHeaders() {
		try {
			printLoadAverageHeader();
		} catch (Exception e1) {
			this.o.f("Failed to write header info for load average");
			e1.printStackTrace();
		}

		try {
			printCpuMemNetHeader();
		} catch (Exception e1) {
			this.o.f("Failed to write header info for cpu mem net");
			e1.printStackTrace();
		}

		try {
			printTcpInfoHeader();
		} catch (Exception e1) {
			this.o.f("Failed to write header info for tcp info");
			e1.printStackTrace();
		}

		interfaces.forEach(i -> {
			try {
				printInterfaceInfoHeader(i);
			} catch (Exception e) {
				this.o.f("Failed to write header info for interface: '%s'", i);
				e.printStackTrace();
			}
		});
	}

	public void writeStats() {
		try {
			printLoadAverage();
		} catch (Exception e1) {
			this.o.f("Failed to write stats for load average");
			e1.printStackTrace();
		}

		try {
			printCpuMemNet();
		} catch (Exception e1) {
			this.o.f("Failed to write stats for cpu mem net");
			e1.printStackTrace();
		}

		try {
			printTcpInfo();
		} catch (Exception e1) {
			this.o.f("Failed to write stats for tcp info");
			e1.printStackTrace();
		}

		interfaces.forEach(i -> {
			try {
				printInterfaceInfo(i);
			} catch (Exception e) {
				this.o.f("Failed to write stats for interface: '%s'", i);
				e.printStackTrace();
			}
		});
	}

	private void printLoadAverageHeader() throws Exception {
		this.loadOut.i("Load averages");
	}

	private void printLoadAverage() throws Exception {
		double[] loadAv = sigar.getLoadAverage();
		this.loadOut.i("%1$.4f %2$.4f %3$.4f", loadAv[0], loadAv[1], loadAv[2]);
	}

	private void printCpuMemNetHeader() throws Exception {
		this.cpuMemNetOut.i("%-6s | %-8s | %-8s | %-10s | %-13s | %-6s | %-7s", "CPU", "Mem used", "Mem free",
				"Mem Used %", "Net TCP Bound", "Net In", "Net Out");
	}

	private void printCpuMemNet() throws Exception {
		Mem mem = sigar.getMem();
		NetStat netStat = sigar.getNetStat();
		this.cpuMemNetOut.i("%-6s | %-8s | %-8s | %-10s | %-13s | %-6s | %-7s",
				String.format("%1$.3f%%", sigar.getCpuPerc().getCombined()), Sigar.formatSize(mem.getActualUsed()),
				Sigar.formatSize(mem.getTotal()), String.format("%1$.2f%%", mem.getUsedPercent()),
				netStat.getTcpBound(), netStat.getAllInboundTotal(), netStat.getAllOutboundTotal());
	}

	private void printTcpInfoHeader() throws Exception {
		this.tcpOut.i("%-9s | %-9s | %-9s | %-11s | %-8s | %-8s | %-8s | %-8s | %-7s | %-9s", "Act Opens", "Pas Opens",
				"Att Fails", "Conn Resets", "Conn Est", "Seg Rx", "Seg Tx", "Seg ReTx", "Bad Seg", "Resets Tx");
	}

	private void printTcpInfo() throws Exception {
		Tcp tcp = sigar.getTcp();
		this.tcpOut.i("%-9s | %-9s | %-9s | %-11s | %-8s | %-8s | %-8s | %-8s | %-7s | %-9s", tcp.getActiveOpens(),
				tcp.getPassiveOpens(), tcp.getAttemptFails(), tcp.getEstabResets(), tcp.getCurrEstab(), tcp.getInSegs(),
				tcp.getOutSegs(), tcp.getRetransSegs(), tcp.getInErrs(), tcp.getOutRsts());
	}

	private Out getOutputForInterface(String name) throws IOException {
		Out tmpO = this.interfaceOuts.get(name);
		if (null == tmpO) {
			o.e("Could not find an output for interface '%s'", name);
			throw new IOException("No output found");
		}
		return tmpO;
	}

	private void printInterfaceInfoHeader(String name) throws Exception {
		NetInterfaceConfig lo = sigar.getNetInterfaceConfig(name);
		Out tmpO = getOutputForInterface(name);

		tmpO.i("Interface '%s'", lo.getName());
		final String gap = "     ";
		tmpO.i("%s %8s : %s", gap, "Address", lo.getAddress());
		tmpO.i("%s %8s : %s", gap, "NetMask", lo.getNetmask());
		tmpO.i("%s %8s : %s", gap, "Type", lo.getType());
		tmpO.i("%s %8s : %s", gap, "MTU", lo.getMtu());
		tmpO.i("%s %8s : %s", gap, "HW Addr", lo.getHwaddr());
		tmpO.i("%s %8s : %s", gap, "Speed", sigar.getNetInterfaceStat(name).getSpeed());
		tmpO.i("%-12s | %-10s | %-8s | %-8s | %-12s | %-10s | %-8s | %-8s", "Tx bytes", "Tx pkts", "Tx drop", "Tx err",
				"Rx bytes", "Rx pkts", "Rx drop", "Rx err");
	}

	private void printInterfaceInfo(String name) throws Exception {
		NetInterfaceStat stat = sigar.getNetInterfaceStat(name);
		Out tmpO = getOutputForInterface(name);

		tmpO.i("%-12s | %-10s | %-8s | %-8s | %-12s | %-10s | %-8s | %-8s", stat.getTxBytes(), stat.getTxPackets(),
				stat.getTxDropped(), stat.getTxErrors(), stat.getRxBytes(), stat.getRxPackets(), stat.getRxDropped(),
				stat.getRxErrors());
	}

}
