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

	@Test
	public void testYahooStockNamesRemoveIf() {
		final YahooStockNames yahooStockNames = new YahooStockNames.Builder(). //
				add("abc"). //
				add("bbc"). //
				add("dbc"). //
				add("cac"). //
				add("tbc"). //
				add("obc"). //
				build();
		final int deletedElementsSize = yahooStockNames.removeIf(p -> {
			return p.startsWith("a") || p.contains("bc");
		});
		Assert.assertEquals(5, deletedElementsSize);
		Assert.assertEquals(1, yahooStockNames.size());
	}
}
