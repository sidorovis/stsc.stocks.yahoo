package stsc.yahoo;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@link YahooStockNames} is a queue of stock names that provide possibility to
 * execute download process on different stocks. <br/>
 * This class provide possibility to divide download process on two phases:
 * <br/>
 * 1. filling queue with names; <br/>
 * 
 * @mark partly thread safe (add() method for {@link Builder} and
 *       getNextStockName() method for {@link YahooStockNames}).
 */
public final class YahooStockNames {

	// filenames with no extension, for example: 'aapl', 'goog', 'ibm' etc.
	private final ConcurrentLinkedQueue<String> stockNamesQueue;

	private YahooStockNames(final Builder builder) {
		this.stockNamesQueue = new ConcurrentLinkedQueue<String>(builder.stockNamesQueue);
	}

	/**
	 * into file system format for example: "_094ftse"
	 * 
	 * @return next value stored in queue or null if queue is empty.
	 */
	public String getNextStockName() {
		return stockNamesQueue.poll();
	}

	/**
	 * Clear stock names collection (used for stopping parallel threads)
	 */
	public void clear() {
		stockNamesQueue.clear();
	}

	public int size() {
		return stockNamesQueue.size();
	}

	public static final class Builder {

		private final ConcurrentLinkedQueue<String> stockNamesQueue = new ConcurrentLinkedQueue<String>();

		public Builder add(final String stockName) {
			stockNamesQueue.add(stockName);
			return this;
		}

		public YahooStockNames build() {
			return new YahooStockNames(this);
		}

	}

}
