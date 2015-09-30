package stsc.yahoo.downloader;

import java.io.File;
import java.util.Optional;

import stsc.common.service.statistics.StatisticType;
import stsc.common.service.statistics.DownloaderLogger;
import stsc.common.stocks.UnitedFormatStock;
import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooUtils;
import stsc.yahoo.liquiditator.StockFilter;

class DownloadYahooStockThread implements Runnable {

	private static final int printEach = 100;

	private final YahooDatafeedSettings settings;
	private final StockFilter stockFilter;
	private int solvedAmount = 0;
	private boolean deleteFilteredData = true;
	private DownloaderLogger logger;

	private volatile boolean stopped = false;

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
			try {
				Optional<UnitedFormatStock> s = settings.getStockFromFileSystem(filesystemStockName);
				boolean downloaded = false;
				if (!s.isPresent()) {
					s = YahooDownloadHelper.download(filesystemStockName);
					if (s.isPresent()) {
						s.get().storeUniteFormatToFolder(settings.getDataFolder());
					}
					downloaded = true;
					logger.log(StatisticType.TRACE, "task fully downloaded: " + filesystemStockName);
				} else {
					downloaded = YahooDownloadHelper.partiallyDownload(s.get());
					if (downloaded) {
						s.get().storeUniteFormatToFolder(settings.getDataFolder());
					}
					logger.log(StatisticType.TRACE, "task partially downloaded: " + filesystemStockName);
				}
				if (downloaded) {
					final boolean filtered = stockFilter.isLiquid(s.get()) && stockFilter.isValid(s.get());
					if (filtered) {
						YahooUtils.copyFilteredStockFile(settings.getDataFolder(), settings.getFilteredDataFolder(), filesystemStockName);
						logger.log(StatisticType.INFO, "task is liquid and copied to filter stock directory: " + filesystemStockName);
					} else {
						final boolean deleted = YahooDownloadHelper.deleteFilteredFile(deleteFilteredData, settings.getFilteredDataFolder(),
								filesystemStockName);
						if (deleted) {
							logger.log(StatisticType.DEBUG, "deleting filtered file with stock " + filesystemStockName
									+ " it doesn't pass new liquidity filter tests");
						}
					}
				} else {
					logger.log(StatisticType.INFO, "task is considered as downloaded: " + filesystemStockName);
				}
			} catch (Exception e) {
				logger.log(StatisticType.TRACE, "task " + filesystemStockName + " throwed an exception: " + e.toString());
				File file = new File(getPath(settings.getDataFolder(), filesystemStockName));
				if (file.length() == 0)
					file.delete();
			}
			synchronized (settings) {
				solvedAmount += 1;
				if (solvedAmount % printEach == 0)
					logger.log().info("solved {} tasks last stock name {}", solvedAmount, filesystemStockName);
			}
			if (stopped) {
				break;
			}
			filesystemStockName = settings.getFilesystemStockName();
		}
	}

	public void stop() {
		stopped = true;
	}

	private static String getPath(String folder, String taskName) {
		return YahooDownloadHelper.getPath(folder, taskName);
	}
}
