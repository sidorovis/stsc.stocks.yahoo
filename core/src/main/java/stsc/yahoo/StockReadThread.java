package stsc.yahoo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import stsc.common.stocks.Stock;
import stsc.common.stocks.UnitedFormatStock;

class StockReadThread implements Runnable {

	private final YahooDatafeedSettings settings;
	private final List<LoadStockReceiver> receivers = Collections.synchronizedList(new ArrayList<LoadStockReceiver>());

	public StockReadThread(YahooDatafeedSettings settings) {
		this.settings = settings;
	}

	public void addReceiver(final LoadStockReceiver receiver) {
		receivers.add(receiver);
	}

	public void addReceivers(List<LoadStockReceiver> receiversToAdd) {
		receivers.addAll(receiversToAdd);
	}

	@Override
	public void run() {
		String task = settings.getFilesystemStockName();
		while (task != null) {
			final Optional<? extends Stock> s = settings.getStockFromFileSystem(task);
			task = UnitedFormatStock.fromFilesystem(task);
			if (s.isPresent()) {
				updateReceivers(s.get());
			}
			task = settings.getFilesystemStockName();
		}
	}

	private void updateReceivers(Stock s) {
		for (LoadStockReceiver receiver : receivers) {
			receiver.newStock(s);
		}
	}

}