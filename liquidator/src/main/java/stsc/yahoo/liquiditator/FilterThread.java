package stsc.yahoo.liquiditator;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stsc.common.stocks.Stock;
import stsc.common.stocks.united.format.UnitedFormatFilename;
import stsc.common.stocks.united.format.UnitedFormatHelper;
import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooStockNames;
import stsc.yahoo.YahooUtils;

/**
 * {@link FilterThread} class is a part of {@link DownloadedStockFilter}
 * application with next algorithm: <br/>
 * 1. get stock name from the queue of stock names to process (until queue is
 * not empty); <br/>
 * 2. generate {@link UnitedFormatFilename} and read {@link Stock} from it;
 * <br/>
 * 3. if stock <br/>
 * 3.1. is {@link StockFilter#isLiquid(Stock)} and
 * {@link StockFilter#isValid(Stock)} then copy stock file to ./filtered_data/
 * folder (if file already exists and differs by size - replace file); <br/>
 * 3.2. else if file with the same filename exists at './filtered_data' folder,
 * delete that file.
 */
final class FilterThread implements Runnable {

	private final YahooDatafeedSettings settings;
	private final YahooStockNames yahooStockNames;
	private final StockFilter stockFilter;
	private final static Logger logger = LogManager.getLogger("FilterThread");

	FilterThread(final YahooDatafeedSettings settings, final YahooStockNames yahooStockNames, final Date d) {
		this.settings = settings;
		this.yahooStockNames = yahooStockNames;
		this.stockFilter = new StockFilter(d);
	}

	FilterThread(final YahooDatafeedSettings settings, final YahooStockNames yahooStockNames) {
		this.settings = settings;
		this.yahooStockNames = yahooStockNames;
		this.stockFilter = new StockFilter();
	}

	@Override
	public void run() {
		String instrumentStockName = yahooStockNames.getNextStockName();
		while (instrumentStockName != null) {
			try {
				final UnitedFormatFilename filename = UnitedFormatHelper.toFilesystem(instrumentStockName);
				Optional<? extends Stock> s = settings.getStockFromFileSystem(filename);
				if (s.isPresent() && stockFilter.isLiquid(s.get()) && stockFilter.isValid(s.get())) {
					YahooUtils.copyFilteredStockFile(settings.getDataFolder(), settings.getFilteredDataFolder(), instrumentStockName);
					logger.trace("stock " + instrumentStockName + " liquid");
				} else {
					deleteIfExisted(filename);
				}
			} catch (IOException e) {
				logger.trace("binary file " + instrumentStockName + " processing throw IOException: " + e.toString());
			}
			instrumentStockName = yahooStockNames.getNextStockName();
		}
	}

	private void deleteIfExisted(final UnitedFormatFilename filename) {
		final File file = settings.getFilteredDataFolder().resolve(filename.getFilename()).toFile();
		if (file.exists()) {
			logger.debug("deleting filtered file with stock " + filename.getFilename() + " it doesn't pass new liquidity filter tests");
			file.delete();
		}
	}

}
