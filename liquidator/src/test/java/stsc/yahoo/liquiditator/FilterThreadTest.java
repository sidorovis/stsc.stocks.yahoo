package stsc.yahoo.liquiditator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import stsc.common.stocks.UnitedFormatHelper;
import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooStockNames;

public class FilterThreadTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	final private Path resourceToPath(final String resourcePath) throws URISyntaxException {
		return FileSystems.getDefault().getPath(new File(FilterThreadTest.class.getResource(resourcePath).toURI()).getAbsolutePath());
	}

	@Test
	public void testFilterThread() throws IOException, InterruptedException, URISyntaxException {
		final Path testPath = FileSystems.getDefault().getPath(testFolder.getRoot().getAbsolutePath());
		final YahooDatafeedSettings settings = new YahooDatafeedSettings(resourceToPath("./"), testPath);
		final YahooStockNames yahooStockNames = new YahooStockNames.Builder().add("aaoi").add("aapl").add("ibm").add("spy").build();

		final FilterThread filterThread = new FilterThread(settings, yahooStockNames, new LocalDate(2014, 1, 14).toDate());
		{
			Thread th = new Thread(filterThread);
			th.start();
			th.join();
		}
		Assert.assertEquals(false, testPath.resolve(UnitedFormatHelper.toFilesystem("aaoi").getFilename()).toFile().exists());
		Assert.assertEquals(true, testPath.resolve(UnitedFormatHelper.toFilesystem("aapl").getFilename()).toFile().exists());
		Assert.assertEquals(false, testPath.resolve(UnitedFormatHelper.toFilesystem("ibm").getFilename()).toFile().exists());
		Assert.assertEquals(true, testPath.resolve(UnitedFormatHelper.toFilesystem("spy").getFilename()).toFile().exists());
	}
}
