package stsc.yahoo.liquiditator;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stsc.common.stocks.Stock;
import stsc.common.stocks.UnitedFormatStock;
import stsc.yahoo.YahooSettings;
import stsc.yahoo.YahooUtils;
import stsc.yahoo.liquiditator.StockFilter;

class FilterThread implements Runnable {

	private final YahooSettings settings;
	private final StockFilter stockFilter;
	private static Logger logger = LogManager.getLogger("FilterThread");

	FilterThread(final YahooSettings settings, Date d) {
		this.settings = settings;
		this.stockFilter = new StockFilter(d);
	}

	FilterThread(final YahooSettings settings) {
		this.settings = settings;
		stockFilter = new StockFilter();
	}

	@Override
	public void run() {
		String filesystemStockName = settings.getFilesystemStockName();
		while (filesystemStockName != null) {
			try {
				Optional<? extends Stock> s = settings.getStockFromFileSystem(filesystemStockName);
				if (s.isPresent() && stockFilter.isLiquid(s.get()) && stockFilter.isValid(s.get())) {
					YahooUtils.copyFilteredStockFile(settings.getDataFolder(), settings.getFilteredDataFolder(), filesystemStockName);
					logger.trace("stock " + filesystemStockName + " liquid");
				} else {
					deleteIfExisted(filesystemStockName);
				}
			} catch (IOException e) {
				logger.trace("binary file " + filesystemStockName + " processing throw IOException: " + e.toString());
			}
			filesystemStockName = settings.getFilesystemStockName();
		}
	}

	private void deleteIfExisted(String stockName) {
		final File file = new File(UnitedFormatStock.generatePath(settings.getFilteredDataFolder(), stockName));
		if (file.exists()) {
			logger.debug("deleting filtered file with stock " + stockName + " it doesn't pass new liquidity filter tests");
			file.delete();
		}
	}

}
