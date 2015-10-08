package stsc.yahoo.liquiditator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import stsc.common.Day;
import stsc.common.stocks.MemoryStock;
import stsc.common.stocks.Stock;
import stsc.common.stocks.UnitedFormatHelper;
import stsc.common.stocks.UnitedFormatStock;

public class StockFilterTest {

	final private Path resourceToPath(final String resourcePath) throws URISyntaxException {
		return FileSystems.getDefault().getPath(new File(StockFilterTest.class.getResource(resourcePath).toURI()).getAbsolutePath());
	}

	final private InputStream resourceToStream(final String resourcePath) throws URISyntaxException {
		return StockFilterTest.class.getResourceAsStream(UnitedFormatHelper.toFilesystem(resourcePath).getFilename());
	}

	@Test
	public void testStockFilter() throws IOException, ParseException, ClassNotFoundException, URISyntaxException {

		final StockFilter stockFilter = new StockFilter(new LocalDate(2013, 1, 13).toDate());
		Stock s1 = UnitedFormatStock.readFromCsvFile("ibm", resourceToPath("ibm.csv").toString());
		Assert.assertEquals(true, stockFilter.isLiquid(s1));
		Stock s2 = UnitedFormatStock.readFromCsvFile("anse", resourceToPath("anse.csv").toString());
		Assert.assertEquals(false, stockFilter.isLiquid(s2));

		final StockFilter stockFilter2 = new StockFilter(new LocalDate(2014, 2, 10).toDate());

		Stock s3 = UnitedFormatStock.readFromUniteFormatFile(resourceToStream("aapl"));
		Assert.assertEquals(true, stockFilter2.isLiquid(s3));

		Stock s4 = UnitedFormatStock.readFromUniteFormatFile(resourceToStream("spy"));
		Assert.assertEquals(true, stockFilter2.isLiquid(s4));

		Stock s5 = UnitedFormatStock.readFromUniteFormatFile(resourceToStream("aaae"));
		Assert.assertEquals(false, stockFilter2.isLiquid(s5));
	}

	@Test
	public void testLast10Year() throws IOException, URISyntaxException {
		final StockFilter stockFilter = new StockFilter(new LocalDate(2014, 2, 10).toDate());

		final Stock aapl = UnitedFormatStock.readFromUniteFormatFile(resourceToStream("aapl"));
		final ArrayList<Day> copyFromDays = aapl.getDays();

		final MemoryStock smallappl = new MemoryStock("smallaapl");
		final ArrayList<Day> days = smallappl.getDays();

		final int indexOfDeletingTo = aapl.findDayIndex(new LocalDate(2007, 1, 1).toDate());

		for (int i = 0; i < indexOfDeletingTo; i++) {
			days.add(copyFromDays.get(i));
		}
		int indexOfDeletingFrom = aapl.findDayIndex(new LocalDate(2009, 1, 1).toDate());
		int indexOfEndOfPeriod = aapl.findDayIndex(new LocalDate(2014, 2, 10).toDate());

		for (int i = indexOfDeletingFrom; i < indexOfEndOfPeriod; i++) {
			days.add(copyFromDays.get(i));
		}
		Assert.assertEquals(false, stockFilter.isLiquid(smallappl));

		for (int i = indexOfDeletingFrom; i < copyFromDays.size(); i++) {
			days.add(copyFromDays.get(i));
		}
		Assert.assertEquals(true, stockFilter.isLiquid(smallappl));
	}

	@Test
	public void testIsValid() throws IOException, URISyntaxException {
		final StockFilter stockFilter = new StockFilter(new LocalDate(2014, 5, 17).toDate());
		final Stock aapl = UnitedFormatStock.readFromUniteFormatFile(resourceToStream("aapl"));
		Assert.assertEquals(false, stockFilter.isValid(aapl));
		final StockFilter stockFilterForApril = new StockFilter(new LocalDate(2014, 3, 17).toDate());
		Assert.assertEquals(true, stockFilterForApril.isValid(aapl));
	}

}
