package stsc.yahoo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import stsc.common.stocks.Stock;
import stsc.common.stocks.united.format.UnitedFormatHelper;

/**
 * {@link StockReadThread} is a part of {@link YahooFileStockStorage}.
 * Implements next algorithm: <br/>
 * 1. get {@link Stock} name from queue (this queue is common for all of the
 * threads) <br/>
 * 2. read {@link Stock} from the file system,
 * {@link LoadStockReceiver#newStock(Stock)} for all receivers.
 */
final class StockReadThread implements Runnable {

	private final YahooDatafeedSettings settings;
	private final YahooStockNames yahooStockNames;
	private final List<LoadStockReceiver> receivers = new CopyOnWriteArrayList<LoadStockReceiver>();

	public StockReadThread(final YahooDatafeedSettings settings, final YahooStockNames yahooStockNames) {
		this.settings = settings;
		this.yahooStockNames = yahooStockNames;
	}

	public void addReceiver(final LoadStockReceiver... receiversToAdd) {
		this.receivers.addAll(Lists.newArrayList(receiversToAdd));
	}

	public void addReceivers(final List<LoadStockReceiver> receiversToAdd) {
		this.receivers.addAll(receiversToAdd);
	}

	@Override
	public void run() {
		String task = yahooStockNames.getNextStockName();
		while (task != null) {
			final Optional<? extends Stock> s = settings.getStockFromFileSystem(UnitedFormatHelper.toFilesystem(task));
			if (s.isPresent()) {
				updateReceivers(s.get());
			}
			task = yahooStockNames.getNextStockName();
		}
	}

	private void updateReceivers(final Stock s) {
		for (LoadStockReceiver receiver : receivers) {
			receiver.newStock(s);
		}
	}

}
