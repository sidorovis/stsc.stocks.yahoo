package stsc.yahoo;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.collect.Sets;

/**
 * {@link YahooStockNames} is a queue of stock names that provide possibility to
 * execute download process on different stocks. <br/>
 * This class provide possibility to divide download process on two phases:
 * <br/>
 * 1. filling queue with names; <br/>
 * 2. polling elements from queue. <br/>
 * Also store list of not processing stock names for now (windows filesystem
 * problem) {@link #bannedStockNames}.
 * 
 * @mark partly thread safe (add() method for {@link Builder} and
 *       getNextStockName() method for {@link YahooStockNames}).
 */
public final class YahooStockNames {

	/**
	 * This is a list of stock names that can't be used because of some //
	 * reason (like Windows file system is still a shit and can't process for
	 * example 'aux.uf' filenames correctly).
	 */
	private static final Set<String> bannedStockNames = Sets.newHashSet("aux", "con", "prn", "nul", "lpt1", "lpt2", "lpt3", "com1", "com2", "com3", "com4");

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
			if (!bannedStockNames.contains(stockName.toLowerCase())) {
				stockNamesQueue.add(stockName);
			}
			return this;
		}

		public YahooStockNames build() {
			return new YahooStockNames(this);
		}

	}

}
