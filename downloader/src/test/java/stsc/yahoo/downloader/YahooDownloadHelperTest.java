package stsc.yahoo.downloader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import stsc.common.stocks.UnitedFormatHelper;
import stsc.common.stocks.UnitedFormatStock;

public class YahooDownloadHelperTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	final private String resourceToPath(final String resourcePath) throws URISyntaxException {
		return new File(YahooDownloadHelperTest.class.getResource(resourcePath).toURI()).getAbsolutePath();
	}

	@Test
	public void testDownload() throws InterruptedException {
		final YahooDownloadHelper yahooDownloadHelper = new YahooDownloadHelper();
		Assert.assertTrue(yahooDownloadHelper.download("aapl").isPresent());
	}

	@Test
	public void testGeneratePartiallyDownloadLine() throws IOException, ParseException, URISyntaxException {
		final YahooDownloadHelper yahooDownloadHelper = new YahooDownloadHelper();
		final UnitedFormatStock aahc = UnitedFormatStock.readFromCsvFile("aahc", resourceToPath("aahc.csv"));
		Assert.assertEquals("http://ichart.yahoo.com/table.csv?s=aahc&a=5&b=4&c=2013", yahooDownloadHelper.generatePartiallyDownloadLine(aahc));
		final UnitedFormatStock aaoi = UnitedFormatStock.readFromCsvFile("aaoi", resourceToPath("aaoi.csv"));
		Assert.assertEquals("http://ichart.yahoo.com/table.csv?s=aaoi&a=0&b=14&c=2014", yahooDownloadHelper.generatePartiallyDownloadLine(aaoi));
	}

	@Test
	public void testDeleteFilteredFile() throws IOException {
		final YahooDownloadHelper yahooDownloadHelper = new YahooDownloadHelper();
		new File(Paths.get(testFolder.getRoot().getAbsolutePath()).resolve("yahooDeleteTest").toString()).createNewFile();
		yahooDownloadHelper.deleteFilteredFile(true, Paths.get(testFolder.getRoot().getAbsolutePath()), UnitedFormatHelper.filesystemToFilesystem("yahooDeleteTest"));
		Assert.assertFalse(new File(Paths.get(testFolder.getRoot().getAbsolutePath()).resolve("yahooDeleteTest").toString()).exists());
	}

}
