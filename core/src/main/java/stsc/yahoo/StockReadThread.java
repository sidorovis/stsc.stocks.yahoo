package stsc.yahoo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import stsc.common.stocks.Stock;
import stsc.common.stocks.UnitedFormatHelper;

class StockReadThread implements Runnable {

	private final YahooDatafeedSettings settings;
	private final YahooStockNames yahooStockNames;
	private final List<LoadStockReceiver> receivers = Collections.synchronizedList(new ArrayList<LoadStockReceiver>());

	public StockReadThread(final YahooDatafeedSettings settings, final YahooStockNames yahooStockNames) {
		this.settings = settings;
		this.yahooStockNames = yahooStockNames;
	}

	public void addReceiver(final LoadStockReceiver receiver) {
		receivers.add(receiver);
	}

	public void addReceivers(List<LoadStockReceiver> receiversToAdd) {
		receivers.addAll(receiversToAdd);
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

	private void updateReceivers(Stock s) {
		for (LoadStockReceiver receiver : receivers) {
			receiver.newStock(s);
		}
	}

}