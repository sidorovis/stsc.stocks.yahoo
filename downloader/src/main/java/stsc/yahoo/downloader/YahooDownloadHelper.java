package stsc.yahoo.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.joda.time.LocalDate;

import stsc.common.Day;
import stsc.common.stocks.UnitedFormatStock;

import com.google.common.io.CharStreams;

public final class YahooDownloadHelper {

	private static final int waitTriesAmount = 5;
	private static final int waitTimeBetweenTries = 500;

	public static final Optional<UnitedFormatStock> download(String filesystemStockName) throws InterruptedException {
		int tries = 0;
		String error = "";
		UnitedFormatStock newStock = null;
		while (tries < waitTriesAmount) {
			try {
				final String instrumentName = UnitedFormatStock.fromFilesystem(filesystemStockName);
				final URL url = new URL("http://ichart.finance.yahoo.com/table.csv?s=" + instrumentName);
				final String stockContent = CharStreams.toString(new InputStreamReader(url.openStream()));
				newStock = UnitedFormatStock.newFromString(instrumentName, stockContent);
				if (newStock.getDays().isEmpty())
					return Optional.empty();
				return Optional.of(newStock);
			} catch (ParseException | IOException e) {
				error = e.toString();
			}
			tries += 1;
			Thread.sleep(waitTimeBetweenTries);
		}
		throw new InterruptedException(waitTriesAmount + " tries not enought to download data on " + filesystemStockName + " stock. " + error);
	}

	/**
	 * !!! Be careful. This method changes {@link UnitedFormatStock} content.
	 * 
	 * @param stock
	 *            to partially download
	 * @return true if it was downloaded partially
	 * @throws InterruptedException
	 */
	public static final boolean partiallyDownload(final UnitedFormatStock stock) throws InterruptedException {
		final String downloadLink = generatePartiallyDownloadLine(stock);
		if (downloadLink.isEmpty()) {
			return false;
		}
		String error = "";
		String stockNewContent = "";
		int tries = 0;

		while (tries < waitTriesAmount) {
			try {
				URL url = new URL(downloadLink);
				stockNewContent = CharStreams.toString(new InputStreamReader(url.openStream()));
				return stock.addDaysFromString(stockNewContent);
			} catch (ParseException e) {
				error = "exception " + e.toString() + " with: '" + stockNewContent + "'";
			} catch (IOException e) {
			}
			tries += 1;
			Thread.sleep(waitTimeBetweenTries);
		}
		throw new InterruptedException("" + waitTriesAmount + " tries not enought to partially download data on " + downloadLink + " stock " + error);
	}

	/**
	 * This method check current date (Calendar.getInstance()) and if last date
	 * from Stock is older then two days, returns http link to yahoo stock
	 * datafeed continuation.
	 * 
	 * @return http yahoo market datafeed link to download (new part of the
	 *         stock).
	 */
	static String generatePartiallyDownloadLine(final UnitedFormatStock stock) {
		final ArrayList<Day> days = stock.getDays();
		final Date lastDate = days.get(days.size() - 1).date;
		final Calendar cal = Calendar.getInstance();
		cal.setTime(lastDate);
		if (new LocalDate(lastDate).equals(new LocalDate(new Date()))) {
			return "";
		}
		if (new LocalDate(lastDate).plusDays(1).equals(new LocalDate(new Date()))) {
			return "";
		}
		cal.add(Calendar.DATE, 1);
		final int day = cal.get(Calendar.DAY_OF_MONTH);
		final int month = cal.get(Calendar.MONTH);
		final int year = cal.get(Calendar.YEAR);
		return "http://ichart.yahoo.com/table.csv?s=" + stock.getInstrumentName() + "&a=" + month + "&b=" + day + "&c=" + year;
	}

	public static boolean deleteFilteredFile(boolean deleteFilteredData, String filteredDataFolder, String stockName) {
		if (deleteFilteredData) {
			String filteredFilePath = getPath(filteredDataFolder, stockName);
			File filteredFile = new File(filteredFilePath);
			if (filteredFile.exists()) {
				filteredFile.delete();
				return true;
			}
		}
		return false;
	}

	public static String getPath(String folder, String taskName) {
		return UnitedFormatStock.generatePath(folder, taskName);
	}

}
