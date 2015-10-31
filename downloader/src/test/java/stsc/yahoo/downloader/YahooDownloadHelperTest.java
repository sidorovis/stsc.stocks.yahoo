package stsc.yahoo.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import stsc.common.Day;
import stsc.common.stocks.Stock;
import stsc.common.stocks.united.format.UnitedFormatHelper;
import stsc.common.stocks.united.format.UnitedFormatStock;

public final class YahooDownloadHelperTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	final private String resourceToPath(final String resourcePath) throws URISyntaxException {
		return new File(YahooDownloadHelperTest.class.getResource(resourcePath).toURI()).getAbsolutePath();
	}

	@Test
	public void testDownloadWithWeekendTest() throws InterruptedException {
		final YahooDownloadHelper yahooDownloadHelper = new YahooDownloadHelper();
		final Optional<UnitedFormatStock> downloadedAapl = yahooDownloadHelper.download("aapl");
		Assert.assertTrue(downloadedAapl.isPresent());
		final Stock aapl = downloadedAapl.get();
		final int index = aapl.findDayIndex(Date.from(LocalDate.of(2013, 9, 8).atStartOfDay().toInstant(ZoneOffset.UTC)));
		final LocalDate localDate = LocalDateTime.ofInstant(aapl.getDays().get(index).getDate().toInstant(), ZoneOffset.UTC).toLocalDate();
		Assert.assertEquals(9, localDate.getDayOfMonth());
	}

	@Test
	public void testDownloadAdm() throws InterruptedException, IOException {
		final YahooDownloadHelper yahooDownloadHelper = new YahooDownloadHelper();
		final UnitedFormatStock downloadedStock = yahooDownloadHelper.download("adm").get();
		final ArrayList<Day> days = downloadedStock.getDays();
		downloadedStock.storeUniteFormatToFolder(testFolder.getRoot().toPath());
		try (InputStream is = new FileInputStream((testFolder.getRoot().toPath().resolve(UnitedFormatHelper.toFilesystem("adm").getFilename()).toFile()))) {
			final UnitedFormatStock stock = UnitedFormatStock.readFromUniteFormatFile(is);
			for (Day d : stock.getDays()) {
				final LocalDate date = d.getDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
				Assert.assertTrue(date.getYear() > 1800);
				Assert.assertTrue(date.getYear() < 2200);
			}
		}
		for (Day d : days) {
			final LocalDate date = d.getDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
			Assert.assertTrue(date.getYear() > 1800);
			Assert.assertTrue(date.getYear() < 2200);
		}
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
		final Path getYahooDeletePath = Paths.get(testFolder.getRoot().getAbsolutePath()).resolve("yahooDeleteTest");
		new File(getYahooDeletePath.toString()).createNewFile();
		yahooDownloadHelper.deleteFilteredFile(true, Paths.get(testFolder.getRoot().getAbsolutePath()),
				UnitedFormatHelper.filesystemToFilesystem("yahooDeleteTest"));
		Assert.assertFalse(new File(getYahooDeletePath.toString()).exists());
	}

}
