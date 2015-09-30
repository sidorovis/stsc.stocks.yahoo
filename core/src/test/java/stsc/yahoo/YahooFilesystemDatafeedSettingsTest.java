package stsc.yahoo;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class YahooFilesystemDatafeedSettingsTest {

	@Test
	public void testYahooFilesystemDatafeedSettings() throws IOException {
		YahooDatafeedSettings settings = new YahooDatafeedSettings("./test/", "./test/");
		Assert.assertEquals(settings.getFilesystemStockName(), null);
		settings.addTask("a");
		Assert.assertEquals(1, settings.taskQueueSize());
		Assert.assertEquals(settings.getFilesystemStockName(), "a");
		Assert.assertEquals(settings.getFilesystemStockName(), null);

		Assert.assertEquals(new File("./test/asd.uf"), new File(settings.generateUniteFormatPath("asd")));
	}

	@Test
	public void testGetStockFromFileSystem() throws IOException {
		final YahooDatafeedSettings settings = new YahooDatafeedSettings("./test_data/", "./test/");
		Assert.assertNotNull(settings.getStockFromFileSystem("aapl"));
		Assert.assertFalse(settings.getStockFromFileSystem("a").isPresent());
	}
}
