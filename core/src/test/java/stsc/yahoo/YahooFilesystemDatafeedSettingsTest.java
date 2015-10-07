package stsc.yahoo;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import stsc.common.stocks.UnitedFormatHelper;

public class YahooFilesystemDatafeedSettingsTest {

	@Test
	public void testYahooFilesystemDatafeedSettings() throws IOException {
		final YahooDatafeedSettings settings = new YahooDatafeedSettings("./", "./");
		Assert.assertEquals(new File("./_asd.uf"), new File(settings.generateUniteFormatPath(UnitedFormatHelper.toFilesystem("asd"))));
	}

	@Test
	public void testGetStockFromFileSystem() throws IOException {
		final YahooDatafeedSettings settings = new YahooDatafeedSettings("./", "./");
		Assert.assertNotNull(settings.getStockFromFileSystem(UnitedFormatHelper.toFilesystem("aapl")));
		Assert.assertFalse(settings.getStockFromFileSystem(UnitedFormatHelper.toFilesystem("a")).isPresent());
	}
}
