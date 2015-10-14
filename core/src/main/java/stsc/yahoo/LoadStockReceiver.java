package stsc.yahoo;

import stsc.common.stocks.Stock;

/**
 * This interface used for signaling about just loaded {@link Stock} at
 * {@link YahooFileStockStorage}. <br/>
 * 
 * @mark {@link #newStock(Stock)} method could be called in different threads
 *       simultaneously.
 */
public interface LoadStockReceiver {

	void newStock(final Stock newStock);

}
