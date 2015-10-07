package stsc.yahoo;

import org.junit.Assert;
import org.junit.Test;

public class YahooStockNamesTest {

	@Test
	public void testYahooStockNames() {
		final YahooStockNames.Builder yahooStockNamesBuilder = new YahooStockNames.Builder();
		Assert.assertEquals(yahooStockNamesBuilder.build().getNextStockName(), null);
		yahooStockNamesBuilder.add("a");
		final YahooStockNames yahooStockNames = yahooStockNamesBuilder.build();
		Assert.assertEquals(1, yahooStockNames.size());

		Assert.assertEquals(yahooStockNames.getNextStockName(), "a");
		Assert.assertEquals(yahooStockNames.getNextStockName(), null);
	}

}
