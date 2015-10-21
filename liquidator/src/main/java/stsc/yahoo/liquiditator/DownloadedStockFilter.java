package stsc.yahoo.liquiditator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.XMLConfigurationFactory;

import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooStockNameListGenerator;
import stsc.yahoo.YahooStockNames;

/**
 * {@link DownloadedStockFilter} application filter already downloaded Yahoo
 * end-of-day datafeed. <br/>
 * Require configuration files: <br/>
 * 1. ./config/log4j2.xml - log4j configuration file;<br/>
 * 2. ./config/liquiditator.ini - configuration file for the application with
 * next parameters:<br/>
 * 2.1. thread.amount = 8 (by default), should be integer value. Regulates
 * amount of threads that would be used for stock filtering.
 * <hr/>
 * <b>Execution instructions</b>. Should be started at the folder with Yahoo
 * stock datafeed in it (./data/, ./filtered_data/).<br/>
 * <b>Description.</b> {@link DownloadedStockFilter} algorithm: <br/>
 * 1. Load stock names from datafeed './data/' folder. <br/>
 * 2. Start thread.amount of {@link FilterThread} and {@link Thread#join()} for
 * all of them.
 */
final class DownloadedStockFilter {

	static {
		System.setProperty(XMLConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "./config/log4j2.xml");
	}

	private static int processThreadSize = 8;
	private final static Logger logger = LogManager.getLogger(DownloadedStockFilter.class.getName());

	private void readProperties() throws IOException {
		try (FileInputStream in = new FileInputStream("./config/liquiditator.ini")) {
			Properties p = new Properties();
			p.load(in);
			processThreadSize = Integer.parseInt(p.getProperty("thread.amount", "8"));
		}
	}

	DownloadedStockFilter() throws IOException, InterruptedException {
		readProperties();

		logger.trace("downloaded stock filter started");
		final YahooDatafeedSettings settings = new YahooDatafeedSettings();
		final YahooStockNames.Builder yahooStockNamesBuilder = new YahooStockNames.Builder();
		new YahooStockNameListGenerator().fillWithExistedFilesFromFolder(settings.getDataFolder(), yahooStockNamesBuilder);
		final YahooStockNames yahooStockNames = yahooStockNamesBuilder.build();
		logger.trace("collected stock names to start filter process: {}", yahooStockNames);

		final List<Thread> threads = new ArrayList<Thread>();
		final FilterThread filterThread = new FilterThread(settings, yahooStockNames);

		for (int i = 0; i < processThreadSize; ++i) {
			final Thread newThread = new Thread(filterThread);
			threads.add(newThread);
			newThread.start();
		}

		logger.info("calculating threads started ( {} )", processThreadSize);

		for (Thread thread : threads) {
			thread.join();
		}

		logger.trace("downloaded stock filter finished");
	}

	public static void main(String[] args) {
		try {
			new DownloadedStockFilter();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
