package stsc.yahoo.downloader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import stsc.common.service.statistics.DownloaderLogger;
import stsc.common.stocks.Stock;
import stsc.yahoo.YahooSettings;
import stsc.yahoo.YahooUtils;

import com.google.common.io.Files;

public class YahooDownloadThreadTest {

	@Test
	public void testDownloadThread() throws InterruptedException, IOException, ClassNotFoundException {
		final YahooSettings settings = YahooUtils.createSettings("./test/", "./test/");
		Files.copy(new File("./test_data/aaoi.uf"), new File("./test/aaoi.uf"));
		settings.addTask("a");
		DownloadYahooStockThread downloadThread = new DownloadYahooStockThread(Mockito.mock(DownloaderLogger.class), settings, false);
		{
			Thread th = new Thread(downloadThread);
			th.start();
			th.join();
		}
		int beforeDownload = Integer.MAX_VALUE;
		{
			final Optional<? extends Stock> s = settings.getStockFromFileSystem("aaoi");
			beforeDownload = s.get().getDays().size();
			Assert.assertEquals(104, s.get().getDays().size());
		}
		settings.addTask("aaoi");
		{
			Thread th = new Thread(downloadThread);
			th.start();
			th.join();
		}
		settings.addTask("aaoi");
		{
			Thread th = new Thread(downloadThread);
			th.start();
			th.join();
		}
		{
			final Optional<? extends Stock> s = settings.getStockFromFileSystem("aaoi");
			Assert.assertEquals(true, beforeDownload < s.get().getDays().size());
		}
		new File("./test/a.uf").delete();
		new File("./test/aaoi.uf").delete();
	}

	@Test
	public void testDownloadThreadForDiffenetInstrumentNameFilesystemName() throws InterruptedException, IOException, ClassNotFoundException {
		final YahooSettings settings = YahooUtils.createSettings("./test/", "./test/");
		settings.addTask("_094FTSE");
		final DownloadYahooStockThread downloadThread = new DownloadYahooStockThread(Mockito.mock(DownloaderLogger.class), settings, false);
		{
			Thread th = new Thread(downloadThread);
			th.start();
			th.join();
		}
		{
			final Optional<? extends Stock> s = settings.getStockFromFileSystem("_094FTSE");
			Assert.assertTrue(s.isPresent());
			Assert.assertEquals("^ftse", s.get().getInstrumentName());
		}
		new File("./test/_094FTSE.uf").delete();
	}
}
