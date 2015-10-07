package stsc.yahoo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import stsc.common.storage.StockStorage;

public class YahooFileStockStorageTest {

	private static StockStorage stockStorage = null;

	final static private String resourceToPath(final String resourcePath) throws URISyntaxException {
		return new File(YahooFileStockStorageTest.class.getResource(resourcePath).toURI()).getAbsolutePath();
	}

	private static synchronized StockStorage getStockStorage() throws ClassNotFoundException, IOException, InterruptedException, URISyntaxException {
		if (stockStorage == null) {
			final YahooFileStockStorage ss = new YahooFileStockStorage(resourceToPath("./"), resourceToPath("./"));
			ss.waitForLoad();
			stockStorage = ss;
		}
		return stockStorage;
	}

	@Test
	public void testStockStorage() throws Exception {
		final StockStorage stockStorage = getStockStorage();
		Assert.assertNotNull(stockStorage);
		Assert.assertNotNull(stockStorage.getStock("aaae"));
		Assert.assertNotNull(stockStorage.getStock("aapl"));
		Assert.assertFalse(stockStorage.getStock("anse").isPresent());
		Assert.assertEquals(7430, stockStorage.getStock("aapl").get().getDays().size());
	}

	@Test
	public void testLiqudityStorageReader() throws Exception {
		final StockStorage stockStorage = getStockStorage();
		Assert.assertNotNull(stockStorage);
		Assert.assertNotNull(stockStorage.getStock("aaae"));
		Assert.assertNotNull(stockStorage.getStock("aapl"));
		Assert.assertFalse(stockStorage.getStock("noexistsstock").isPresent());
	}
}
