package stsc.yahoo.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.google.common.io.Files;

import stsc.common.service.statistics.DownloaderLogger;
import stsc.common.stocks.Stock;
import stsc.common.stocks.UnitedFormatHelper;
import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooStockNames;
import stsc.yahoo.YahooUtils;

public class YahooStockDownloadThreadTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testDownloadThread() throws Exception {
		final Path testPath = FileSystems.getDefault().getPath(testFolder.getRoot().getAbsolutePath());
		final YahooDatafeedSettings settings = YahooUtils.createSettings(testPath.toString(), testPath.toString());
		final String fileName = UnitedFormatHelper.toFilesystem("aaoi").getFilename();
		Files.copy(new File(YahooStockDownloadThreadTest.class.getResource(fileName).toURI()), testPath.resolve(fileName).toFile());
		{
			YahooStockDownloadThread downloadThread = new YahooStockDownloadThread(Mockito.mock(DownloaderLogger.class), settings, //
					new YahooStockNames.Builder().add("a").build(), false);
			{
				Thread th = new Thread(downloadThread);
				th.start();
				th.join();
			}
		}
		int beforeDownload = Integer.MAX_VALUE;
		{
			final Optional<? extends Stock> s = settings.getStockFromFileSystem(UnitedFormatHelper.toFilesystem("aaoi"));
			beforeDownload = s.get().getDays().size();
			Assert.assertEquals(104, s.get().getDays().size());
		}
		{
			YahooStockDownloadThread downloadThread = new YahooStockDownloadThread(Mockito.mock(DownloaderLogger.class), settings, //
					new YahooStockNames.Builder().add("aaoi").build(), false);
			final Thread th = new Thread(downloadThread);
			th.start();
			th.join();
		}
		{
			YahooStockDownloadThread downloadThread = new YahooStockDownloadThread(Mockito.mock(DownloaderLogger.class), settings, //
					new YahooStockNames.Builder().add("aaoi").build(), false);
			final Thread th = new Thread(downloadThread);
			th.start();
			th.join();
		}
		{
			final Optional<? extends Stock> s = settings.getStockFromFileSystem(UnitedFormatHelper.toFilesystem("aaoi"));
			Assert.assertEquals(true, beforeDownload < s.get().getDays().size());
		}
	}

	@Test
	public void testDownloadThreadForDiffenetInstrumentNameFilesystemName() throws InterruptedException, IOException, ClassNotFoundException {
		final Path testPath = FileSystems.getDefault().getPath(testFolder.getRoot().getAbsolutePath());
		final YahooDatafeedSettings settings = YahooUtils.createSettings(testPath.toString(), testPath.toString());
		{
			final YahooStockDownloadThread downloadThread = new YahooStockDownloadThread(Mockito.mock(DownloaderLogger.class), settings, //
					new YahooStockNames.Builder().add("^ftse").build(), false);
			final Thread th = new Thread(downloadThread);
			th.start();
			th.join();
		}
		{
			final Optional<? extends Stock> s = settings.getStockFromFileSystem(UnitedFormatHelper.toFilesystem("^ftse"));
			Assert.assertTrue(s.isPresent());
			Assert.assertEquals("^ftse", s.get().getInstrumentName());
		}
		Assert.assertTrue(testPath.resolve(UnitedFormatHelper.toFilesystem("^ftse").getFilename()).toFile().exists());
	}
}
