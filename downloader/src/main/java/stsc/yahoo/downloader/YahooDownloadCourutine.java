package stsc.yahoo.downloader;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import stsc.common.service.statistics.DownloaderLogger;
import stsc.stocks.repo.MetaIndicesRepositoryIncodeImpl;
import stsc.yahoo.YahooSettings;
import stsc.yahoo.YahooStockNameListGenerator;

public final class YahooDownloadCourutine {

	private final DownloaderLogger logger;

	private final DownloadYahooStockThread downloadThread;

	private final int downloadThreadSize;
	private final boolean downloadExisted;
	private final YahooSettings settings;
	private final boolean downloadByPattern;
	private final String startPattern;
	private final String endPattern;
	private final int stockNameMinLength;
	private final int stockNameMaxLength;
	private final YahooStockNameListGenerator yahooStockNameListGenerator;

	private volatile boolean stopped = false;

	public YahooDownloadCourutine(DownloaderLogger logger, boolean downloadExisted, YahooSettings settings, boolean downloadByPattern, String startPattern,
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
		this.yahooStockNameListGenerator = new YahooStockNameListGenerator(new MetaIndicesRepositoryIncodeImpl());

		downloadThread = new DownloadYahooStockThread(logger, settings);
	}

	public void start() throws InterruptedException {
		logger.log().trace("starting");
		fillStockListFromBusinessIndexes(settings.getFilesystemStockNamesQueue());
		if (downloadExisted) {
			YahooStockNameListGenerator.fillWithExistedFilesFromFolder(FileSystems.getDefault().getPath(settings.getDataFolder()),
					settings.getFilesystemStockNamesQueue());
		} else {
			if (downloadByPattern) {
				yahooStockNameListGenerator.fillWithBeginEndPatterns(startPattern, endPattern, settings.getFilesystemStockNamesQueue());
			} else {
				yahooStockNameListGenerator.fillWithStockNameLength(stockNameMinLength, stockNameMaxLength, settings.getFilesystemStockNamesQueue());
			}
		}
		if (stopped) {
			return;
		}
		logger.log().trace("tasks size: {}", settings.taskQueueSize());
		final List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < downloadThreadSize; ++i) {
			Thread newThread = new Thread(downloadThread);
			threads.add(newThread);
			newThread.start();
		}

		logger.log().info("calculating threads started ( {} )", downloadThreadSize);
		for (Thread thread : threads) {
			thread.join();
		}

		logger.log().trace("finishing");
	}

	private void fillStockListFromBusinessIndexes(Queue<String> filesystemStockNamesQueue) {
		yahooStockNameListGenerator.fillWithIndexesFromBase(filesystemStockNamesQueue);
	}

	public void stop() throws Exception {
		stopped = true;
		downloadThread.stop();
		logger.log().trace("stop command was processed");
	}

}
