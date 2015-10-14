package stsc.yahoo;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import stsc.common.stocks.UnitedFormatHelper;

public class YahooDatafeedSettingsTest {

	private YahooDatafeedSettings createSettings() throws IOException {
		final Path thisPath = FileSystems.getDefault().getPath("./");
		return new YahooDatafeedSettings(thisPath, thisPath);

	}

	@Test
	public void testYahooFilesystemDatafeedSettings() throws IOException {
		final YahooDatafeedSettings settings = createSettings();
		Assert.assertEquals(new File("./_asd.uf"), settings.generateUniteFormatPath(UnitedFormatHelper.toFilesystem("asd")).toFile());
	}

	@Test
	public void testGetStockFromFileSystem() throws IOException {
		final YahooDatafeedSettings settings = createSettings();
		Assert.assertNotNull(settings.getStockFromFileSystem(UnitedFormatHelper.toFilesystem("aapl")));
		Assert.assertFalse(settings.getStockFromFileSystem(UnitedFormatHelper.toFilesystem("a")).isPresent());
	}
}
