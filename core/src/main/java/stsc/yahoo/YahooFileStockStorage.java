package stsc.yahoo;

import java.io.IOException;
import java.nio.file.FileSystems;
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

public class YahooFileStockStorage extends ThreadSafeStockStorage implements LoadStockReceiver {

	public static final String DATA_FOLDER = "./data/";
	public static final String FILTER_DATA_FOLDER = "./filtered_data/";

	static {
		System.setProperty(XMLConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "./config/log4j2.xml");
	}

	private static Logger logger = LogManager.getLogger("YahooFileStorage");

	private final YahooDatafeedSettings settings;
	private YahooStockNames yahooStockNames = new YahooStockNames.Builder().build();
	private final int readStockThreadSize = 4;
	private final List<Thread> threads = new ArrayList<Thread>();
	private final List<LoadStockReceiver> receivers = Collections.synchronizedList(new ArrayList<LoadStockReceiver>());

	public YahooFileStockStorage(YahooDatafeedSettings settings) throws ClassNotFoundException, IOException {
		super();
		this.settings = settings;
		loadStocksFromFileSystem(true);

	}

	public YahooFileStockStorage() throws ClassNotFoundException, IOException {
		this(DATA_FOLDER, FILTER_DATA_FOLDER);
	}

	public static YahooFileStockStorage forData(String dataFolder) throws ClassNotFoundException, IOException {
		return new YahooFileStockStorage(dataFolder, FILTER_DATA_FOLDER);
	}

	public static YahooFileStockStorage forFilteredData(String dataFilterFolder) throws ClassNotFoundException, IOException {
		return new YahooFileStockStorage(DATA_FOLDER, dataFilterFolder);
	}

	public YahooFileStockStorage(String dataFolder, String filteredDataFolder) throws ClassNotFoundException, IOException {
		this(dataFolder, filteredDataFolder, true);
	}

	public YahooFileStockStorage(String dataFolder, String filteredDataFolder, boolean autoStart) throws ClassNotFoundException, IOException {
		super();
		this.settings = new YahooDatafeedSettings(dataFolder, filteredDataFolder);
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
		new YahooStockNameListGenerator(new MetaIndicesRepositoryIncodeImpl()).fillWithExistedFilesFromFolder(FileSystems.getDefault().getPath(settings.getFilteredDataFolder()),
				yahooStockNamesBuilder);
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
