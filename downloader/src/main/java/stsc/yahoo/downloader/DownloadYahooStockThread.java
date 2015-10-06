package stsc.yahoo.downloader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import stsc.common.service.statistics.StatisticType;
import stsc.common.service.statistics.DownloaderLogger;
import stsc.common.stocks.UnitedFormatStock;
import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooUtils;
import stsc.yahoo.liquiditator.StockFilter;

class DownloadYahooStockThread implements Runnable {

	private static final Set<String> bannedNames = Sets.newHashSet("aux");

	private static final int printEach = 100;

	private final YahooDatafeedSettings settings;
	private final StockFilter stockFilter;
	private int solvedAmount = 0;
	private boolean deleteFilteredData = true;
	private DownloaderLogger logger;

	private volatile boolean stopped = false;

	private final YahooDownloadHelper yahooDownloadHelper = new YahooDownloadHelper();

	DownloadYahooStockThread(DownloaderLogger logger, YahooDatafeedSettings settings) {
		this.logger = logger;
		this.settings = settings;
		this.stockFilter = new StockFilter();
	}

	DownloadYahooStockThread(DownloaderLogger logger, YahooDatafeedSettings settings, boolean deleteFilteredData) {
		this.logger = logger;
		this.settings = settings;
		this.stockFilter = new StockFilter();
		this.deleteFilteredData = deleteFilteredData;
	}

	public void run() {
		String filesystemStockName = settings.getFilesystemStockName();
		while (filesystemStockName != null) {
			if (!bannedNames.contains(filesystemStockName)) {
				downloadMarketDatafeedStock(filesystemStockName);
				increaseDownloadStatistics(filesystemStockName);
				if (stopped) {
					break;
				}
			}
			filesystemStockName = settings.getFilesystemStockName();
		}
	}

	private void downloadMarketDatafeedStock(String filesystemStockName) {
		try {
			Optional<UnitedFormatStock> s = settings.getStockFromFileSystem(filesystemStockName);
			boolean downloaded = false;
			if (!s.isPresent()) {
				s = yahooDownloadHelper.download(filesystemStockName);
				if (s.isPresent()) {
					s.get().storeUniteFormatToFolder(settings.getDataFolder());
				}
				downloaded = true;
				logger.log(StatisticType.TRACE, "task fully downloaded: " + filesystemStockName);
			} else {
				downloaded = partiallDownload(filesystemStockName, s);
			}
			if (downloaded) {
				processDownloadedStock(filesystemStockName, s);
			} else {
				logger.log(StatisticType.INFO, "task is considered as downloaded: " + filesystemStockName);
			}
		} catch (Exception e) {
			logger.log(StatisticType.TRACE, "task " + filesystemStockName + " throwed an exception: " + e.toString());
			final File file = new File(getPath(settings.getDataFolder(), filesystemStockName));
			if (file.length() == 0)
				file.delete();
		}
	}

	private void increaseDownloadStatistics(String filesystemStockName) {
		synchronized (settings) {
			solvedAmount += 1;
			if (solvedAmount % printEach == 0)
				logger.log().info("solved {} tasks last stock name {}", solvedAmount, filesystemStockName);
		}
	}

	private boolean partiallDownload(String filesystemStockName, Optional<UnitedFormatStock> s) throws InterruptedException, IOException {
		boolean downloaded;
		downloaded = yahooDownloadHelper.partiallyDownload(s.get());
		if (downloaded) {
			s.get().storeUniteFormatToFolder(settings.getDataFolder());
		}
		logger.log(StatisticType.TRACE, "task partially downloaded: " + filesystemStockName);
		return downloaded;
	}

	private void processDownloadedStock(String filesystemStockName, Optional<UnitedFormatStock> s) throws IOException {
		final boolean filtered = stockFilter.isLiquid(s.get()) && stockFilter.isValid(s.get());
		if (filtered) {
			YahooUtils.copyFilteredStockFile(settings.getDataFolder(), settings.getFilteredDataFolder(), s.get().getInstrumentName());
			logger.log(StatisticType.INFO, "task is liquid and copied to filter stock directory: " + filesystemStockName);
		} else {
			final boolean deleted = yahooDownloadHelper.deleteFilteredFile(deleteFilteredData, settings.getFilteredDataFolder(), filesystemStockName);
			if (deleted) {
				logger.log(StatisticType.DEBUG, "deleting filtered file with stock " + filesystemStockName + " it doesn't pass new liquidity filter tests");
			}
		}
	}

	public void stop() {
		stopped = true;
	}

	private static String getPath(String folder, String taskName) {
		return YahooDownloadHelper.getPath(folder, taskName);
	}
}
