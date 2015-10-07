package stsc.yahoo.liquiditator;

import java.io.File;
import java.io.IOException;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import stsc.yahoo.YahooDatafeedSettings;
import stsc.yahoo.YahooStockNames;
import stsc.yahoo.YahooUtils;

public class FilterThreadTest {

	@Test
	public void testFilterThread() throws IOException, InterruptedException {
		final YahooDatafeedSettings settings = YahooUtils.createSettings("./test_data/", "./test/");
		final YahooStockNames yahooStockNames = new YahooStockNames.Builder().add("aaoi").add("aapl").add("ibm").add("spy").build();

		FilterThread filterThread = new FilterThread(settings, yahooStockNames, new LocalDate(2014, 1, 14).toDate());
		{
			Thread th = new Thread(filterThread);
			th.start();
			th.join();
		}
		Assert.assertEquals(false, new File("./test/aaoi.uf").exists());
		Assert.assertEquals(true, new File("./test/aapl.uf").exists());
		Assert.assertEquals(false, new File("./test/ibm.uf").exists());
		Assert.assertEquals(true, new File("./test/spy.uf").exists());
		new File("./test/aapl.uf").delete();
		new File("./test/spy.uf").delete();
	}
}
