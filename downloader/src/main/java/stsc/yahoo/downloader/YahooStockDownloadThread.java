package stsc.yahoo.downloader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import stsc.common.service.statistics.DownloaderLogger;
import stsc.common.service.statistics.StatisticType;
import stsc.common.stocks.Stock;
import stsc.common.stocks.united.format.UnitedFormatFilename;
import stsc.common.stocks.united.format.UnitedFormatHelper;
import stsc.common.stocks.united.format.UnitedFormatStock;
import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooStockNames;
import stsc.yahoo.YahooUtils;
import stsc.yahoo.liquiditator.StockFilter;

/**
 * {@link YahooStockDownloadThread} class implements download algorithm for
 * yahoo end-of-day stock datafeed. <br/>
 * 
 * Could be started as one instance in many threads. <br/>
 * 
 * Algorithm details: <br/>
 * 1. get next stock name from stock names queue (one for all threads); <br/>
 * <br/>
 * 2. {@link #downloadMarketDatafeedStock(String)} algorithm: ;<br/>
 * 2.1. get {@link Stock} from file system. <br/>
 * 2.2. if stock not exists on file system: <br>
 * 2.2.1. download stock using {@link YahooDownloadHelper#download(String)};
 * <br/>
 * 2.2.3. store stock to the file system if the download was successful; <br/>
 * 2.3. else {@link #partiallDownload(String, Optional)} and store downloaded
 * stock to the file system; <br/>
 * 2.4. {@link #processDownloadedStock(String, Optional)} <br/>
 * 2.4.1. check liquidity and validity for stock and copy {@link Stock} file to
 * filtered folder. <br/>
 * 2.4.2. delete not liquid or not valid {@link Stock} from filtered datafeed
 * folder. <br/>
 * 3. {@link #increaseDownloadStatistics(String)} (log each {@link #printEach}
 * download attempt;
 * 
 * @mark could be used into multi-thread environment (even as single object)
 */
final class YahooStockDownloadThread implements Runnable {

	private static final int printEach = 100;

	private final YahooDatafeedSettings settings;
	private final YahooStockNames yahooStockNames;
	private final StockFilter stockFilter;
	private int solvedAmount = 0;
	private boolean deleteFilteredData = true;
	private DownloaderLogger logger;

	private volatile boolean stopped = false;

	private final YahooDownloadHelper yahooDownloadHelper = new YahooDownloadHelper();

	YahooStockDownloadThread(DownloaderLogger logger, YahooDatafeedSettings settings, final YahooStockNames yahooStockNames) {
		this(logger, settings, yahooStockNames, true);
	}

	YahooStockDownloadThread(DownloaderLogger logger, YahooDatafeedSettings settings, final YahooStockNames yahooStockNames, boolean deleteFilteredData) {
		this.logger = logger;
		this.settings = settings;
		this.yahooStockNames = yahooStockNames;
		this.stockFilter = new StockFilter();
		this.deleteFilteredData = deleteFilteredData;
	}

	public void run() {
		String instrumentStockName = yahooStockNames.getNextStockName();
		while (instrumentStockName != null) {
			downloadMarketDatafeedStock(instrumentStockName);
			increaseDownloadStatistics(instrumentStockName);
			if (stopped) {
				break;
			}
			instrumentStockName = yahooStockNames.getNextStockName();
		}
	}

	private void downloadMarketDatafeedStock(final String instrumentStockName) {
		final UnitedFormatFilename filename = UnitedFormatHelper.toFilesystem(instrumentStockName);
		try {
			Optional<UnitedFormatStock> s = settings.getStockFromFileSystem(filename);
			boolean downloaded = false;
			if (!s.isPresent()) {
				s = yahooDownloadHelper.download(instrumentStockName);
				if (s.isPresent()) {
					s.get().storeUniteFormatToFolder(settings.getDataFolder());
				}
				downloaded = true;
				logger.log(StatisticType.TRACE, "task fully downloaded: " + instrumentStockName);
			} else {
				downloaded = partiallDownload(instrumentStockName, s);
			}
			if (downloaded) {
				processDownloadedStock(instrumentStockName, s);
			} else {
				logger.log(StatisticType.INFO, "task is considered as downloaded: " + instrumentStockName);
			}
		} catch (Exception e) {
			logger.log(StatisticType.TRACE, "task " + instrumentStockName + " throwed an exception: " + e.toString());
			final File file = settings.getDataFolder().resolve(filename.getFilename()).toFile();
			if (file.length() == 0)
				file.delete();
		}
	}

	private void increaseDownloadStatistics(String filesystemStockName) {
		// dirty hack, that helps to synchronize all threads
		synchronized (settings) {
			solvedAmount += 1;
			if (solvedAmount % printEach == 0)
				logger.log().info("solved {} tasks last stock name {}", solvedAmount, filesystemStockName);
		}
	}

	private boolean partiallDownload(String instrumentStockName, Optional<UnitedFormatStock> s) throws InterruptedException, IOException {
		boolean downloaded;
		downloaded = yahooDownloadHelper.partiallyDownload(s.get());
		if (downloaded) {
			s.get().storeUniteFormatToFolder(settings.getDataFolder());
		}
		logger.log(StatisticType.TRACE, "task partially downloaded: " + instrumentStockName);
		return downloaded;
	}

	private void processDownloadedStock(String instrumentStockName, Optional<UnitedFormatStock> s) throws IOException {
		final boolean filtered = stockFilter.isLiquid(s.get()) && stockFilter.isValid(s.get());
		if (filtered) {
			YahooUtils.copyFilteredStockFile(settings.getDataFolder(), settings.getFilteredDataFolder(), s.get().getInstrumentName());
			logger.log(StatisticType.INFO, "task is liquid and copied to filter stock directory: " + s.get().getFilesystemName());
		} else {
			final boolean deleted = yahooDownloadHelper.deleteFilteredFile(deleteFilteredData, settings.getFilteredDataFolder(), s.get().getFilesystemName());
			if (deleted) {
				logger.log(StatisticType.DEBUG, "deleting filtered file with stock " + s.get().getFilesystemName() + " it doesn't pass new liquidity filter tests");
			}
		}
	}

	public void stop() {
		stopped = true;
	}
}
