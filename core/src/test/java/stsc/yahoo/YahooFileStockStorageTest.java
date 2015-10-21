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

	private final static Path resourceToPath(final String resourcePath) throws URISyntaxException {
		return FileSystems.getDefault().getPath(new File(YahooFileStockStorageTest.class.getResource(resourcePath).toURI()).getAbsolutePath());
	}

	private final static synchronized StockStorage getStockStorage() throws IOException, InterruptedException, URISyntaxException {
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

	@Test
	public void testYahooFileStockStorageRemoveIf() throws IOException, URISyntaxException {
		final YahooFileStockStorage ss = new YahooFileStockStorage(new YahooDatafeedSettings(resourceToPath("./"), resourceToPath("./")), false);
		Assert.assertEquals(1, ss.removeIf(p -> {
			return p.startsWith("aaa");
		}));
		Assert.assertEquals(3, ss.amountToProcess());
	}
}
