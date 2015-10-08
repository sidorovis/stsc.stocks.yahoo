package stsc.yahoo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.XMLConfigurationFactory;

import stsc.common.stocks.Stock;
import stsc.common.stocks.StockLock;
import stsc.stocks.repo.MetaIndicesRepositoryIncodeImpl;
import stsc.storage.ThreadSafeStockStorage;

public final class YahooFileStockStorage extends ThreadSafeStockStorage implements LoadStockReceiver {

	static {
		System.setProperty(XMLConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "./config/log4j2.xml");
	}

	private static Logger logger = LogManager.getLogger("YahooFileStorage");

	private final YahooDatafeedSettings settings;
	private YahooStockNames yahooStockNames = new YahooStockNames.Builder().build();
	private final int readStockThreadSize = 4;
	private final List<Thread> threads = new ArrayList<Thread>();
	private final List<LoadStockReceiver> receivers = Collections.synchronizedList(new ArrayList<LoadStockReceiver>());

	public YahooFileStockStorage(final YahooDatafeedSettings settings, boolean autoStart) throws ClassNotFoundException, IOException {
		super();
		this.settings = settings;
		loadStocksFromFileSystem(autoStart);

	}

	public void addReceiver(LoadStockReceiver receiver) {
		receivers.add(receiver);
	}

	private void loadStocksFromFileSystem(final boolean autoStart) throws ClassNotFoundException, IOException {
		logger.trace("created");
		this.yahooStockNames = loadFilteredDatafeed();
		logger.info("filtered datafeed header readed: {} stocks", yahooStockNames.size());
		if (autoStart) {
			loadStocks();
		}
	}

	private YahooStockNames loadFilteredDatafeed() {
		final YahooStockNames.Builder yahooStockNamesBuilder = new YahooStockNames.Builder();
		new YahooStockNameListGenerator(new MetaIndicesRepositoryIncodeImpl()).fillWithExistedFilesFromFolder(settings.getFilteredDataFolder(), yahooStockNamesBuilder);
		return yahooStockNamesBuilder.build();
	}

	private void loadStocks() throws ClassNotFoundException, IOException {
		logger.info("stocks load was initiated");
		final StockReadThread stockReadThread = new StockReadThread(settings, yahooStockNames);
		stockReadThread.addReceiver(this);
		stockReadThread.addReceivers(receivers);
		for (int i = 0; i < readStockThreadSize; ++i) {
			final Thread newThread = new Thread(stockReadThread);
			newThread.setName("YahooFileStockStorage Reading Thread - " + String.valueOf(i));
			threads.add(newThread);
			newThread.start();
		}
	}

	public void stopLoadStocks() {
		yahooStockNames.clear();
	}

	public YahooFileStockStorage waitForLoad() throws InterruptedException {
		for (Thread thread : threads) {
			thread.join();
		}
		return this;
	}

	@Override
	public void newStock(Stock newStock) {
		datafeed.put(newStock.getInstrumentName(), new StockLock(newStock));
	}
}
