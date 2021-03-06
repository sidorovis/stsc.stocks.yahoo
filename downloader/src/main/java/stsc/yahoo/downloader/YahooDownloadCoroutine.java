package stsc.yahoo.downloader;

import java.util.ArrayList;
import java.util.List;

import stsc.common.service.statistics.DownloaderLogger;
import stsc.stocks.repo.MetaIndicesRepositoryIncodeImpl;
import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooStockNameListGenerator;
import stsc.yahoo.YahooStockNames;

/**
 * This is an application coroutine (used at YahooDownloadService
 * https://github.com/sidorovis/stsc.as.service.yahoo.downloader project). <br/>
 * On {@link #start()} will choose necessary algorithm for download, create
 * download queue and download stocks in separate threads.
 */
public final class YahooDownloadCoroutine {

	private final DownloaderLogger logger;

	private YahooStockDownloadThread downloadThread;

	private final int downloadThreadSize;
	private final boolean downloadExisted;
	private final YahooDatafeedSettings settings;
	private final boolean downloadByPattern;
	private final String startPattern;
	private final String endPattern;
	private final int stockNameMinLength;
	private final int stockNameMaxLength;
	private final YahooStockNameListGenerator yahooStockNameListGenerator;

	private volatile boolean stopped = false;

	public YahooDownloadCoroutine(DownloaderLogger logger, boolean downloadExisted, YahooDatafeedSettings settings, boolean downloadByPattern, String startPattern,
			String endPattern, int stockNameMinLength, int stockNameMaxLength, int downloadThreadSize) {
		this.logger = logger;
		this.downloadExisted = downloadExisted;
		this.settings = settings;
		this.downloadByPattern = downloadByPattern;
		this.startPattern = startPattern;
		this.endPattern = endPattern;
		this.stockNameMinLength = stockNameMinLength;
		this.stockNameMaxLength = stockNameMaxLength;
		this.downloadThreadSize = downloadThreadSize;
		this.yahooStockNameListGenerator = new YahooStockNameListGenerator();

		downloadThread = new YahooStockDownloadThread(logger, settings, new YahooStockNames.Builder().build());
	}

	public void start() throws InterruptedException {
		logger.log().trace("starting");
		final YahooStockNames.Builder yahooStockNamesBuilder = new YahooStockNames.Builder();
		fillStockListFromBusinessIndexes(yahooStockNamesBuilder);
		if (downloadExisted) {
			yahooStockNameListGenerator.fillWithExistedFilesFromFolder(settings.getDataFolder(), yahooStockNamesBuilder);
		} else {
			if (downloadByPattern) {
				yahooStockNameListGenerator.fillWithBeginEndPatterns(startPattern, endPattern, yahooStockNamesBuilder);
			} else {
				yahooStockNameListGenerator.fillWithStockNameLength(stockNameMinLength, stockNameMaxLength, yahooStockNamesBuilder);
			}
		}
		if (stopped) {
			return;
		}
		final YahooStockNames yahooStockNames = yahooStockNamesBuilder.build();
		logger.log().trace("tasks size: {}", yahooStockNames.size());
		downloadThread = new YahooStockDownloadThread(logger, settings, yahooStockNames);

		final List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < downloadThreadSize; ++i) {
			final Thread newThread = new Thread(downloadThread);
			threads.add(newThread);
			newThread.start();
		}

		logger.log().info("calculating threads started ( {} )", downloadThreadSize);
		for (Thread thread : threads) {
			thread.join();
		}

		logger.log().trace("finishing");
	}

	private void fillStockListFromBusinessIndexes(final YahooStockNames.Builder builder) {
		yahooStockNameListGenerator.fillWithIndexesFromBase(new MetaIndicesRepositoryIncodeImpl(), builder);
	}

	public void stop() throws Exception {
		stopped = true;
		downloadThread.stop();
		logger.log().trace("stop command was processed");
	}

}
