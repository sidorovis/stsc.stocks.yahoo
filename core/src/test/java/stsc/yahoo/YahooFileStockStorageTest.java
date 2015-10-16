package stsc.yahoo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import stsc.common.storage.StockStorage;

public class YahooFileStockStorageTest {

	private static StockStorage stockStorage = null;

	final static private Path resourceToPath(final String resourcePath) throws URISyntaxException {
		return FileSystems.getDefault().getPath(new File(YahooFileStockStorageTest.class.getResource(resourcePath).toURI()).getAbsolutePath());
	}

	private static synchronized StockStorage getStockStorage() throws ClassNotFoundException, IOException, InterruptedException, URISyntaxException {
		if (stockStorage == null) {
			final YahooFileStockStorage ss = new YahooFileStockStorage(new YahooDatafeedSettings(resourceToPath("./"), resourceToPath("./")), true);
			stockStorage = ss.waitForBackgroundProcess();
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
