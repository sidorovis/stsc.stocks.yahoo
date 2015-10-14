package stsc.yahoo.liquiditator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import stsc.common.Day;
import stsc.common.stocks.Stock;

/**
 * {@link StockFilter} class that provide possibility to test stock data.
 * Liquidity Test implemented as:
 * <hr/>
 * <code> {@link #isLiquidTestWithError(Stock)} </code>method could be divided
 * on: <br/>
 * 1. {@link #testLastPeriods(Stock)} as next tests: <br/>
 * 1.1. daysWithDataForLastYear < {@link #minimalDaysWithDataPerLastYear} <br/>
 * 1.2. daysWithDataForLastMonth < {@link #minimalDaysWithDataPerLastMonth}
 * <br/>
 * 1.3. volumeAmount < {@link #minimalAverageYearVolume} <br/>
 * 2. {@link #testLastNYears(Stock)}; <br/>
 * 2.1. averagePercentDaysPerSeveralYear <
 * minimalDaysPercentPerLastSeveralYears;
 * <hr/>
 * <code> {@link #isValidWithError(Stock)} </code>method could be divided on:
 * <br/>
 * 1. {@link #testGapsOnAdjectiveClose(Stock)} as next tests: <br/>
 * 1.1. Double.compare(currentAdjective, 0.0) != 0 <br/>
 * 1.2. Math.abs(1.0 - previousAdjective / currentAdjective) <=
 * {@link #valuableGapInPercents}
 */
public final class StockFilter {

	private static final int minimalDaysWithDataPerLastYear = 216;
	private static final int minimalDaysWithDataPerLastMonth = 19;
	private static final int minimalAverageYearVolume = 60000000;
	private static final float minimalDaysPercentPerLastSeveralYears = (float) 0.9;
	private static final int lastYearsAmount = 18;
	private static final int daysPerYear = 256;
	private static final float valuableGapInPercents = 2.0f;

	private final Date today;

	private final static Logger logger = LogManager.getLogger(StockFilter.class.getName());

	public StockFilter() {
		this.today = new Date();
	}

	public StockFilter(final Date testToday) {
		this.today = testToday;
	}

	// Liquidity Test

	private String testLastPeriods(final Stock s) {
		String errors = "";
		final ArrayList<Day> days = s.getDays();
		final LocalDate todayDate = getTodayWithoutHolidays();

		final int yearAgoIndex = s.findDayIndex(todayDate.plusYears(-1).toDate());
		final int daysWithDataForLastYear = days.size() - yearAgoIndex;
		if (daysWithDataForLastYear < minimalDaysWithDataPerLastYear) {
			logger.debug("stock " + s.getInstrumentName() + " have only " + daysWithDataForLastYear + " days for last year");
			errors += "stock " + s.getInstrumentName() + " have only " + daysWithDataForLastYear + " days for last year\n";
		}
		final int monthAgoIndex = s.findDayIndex(todayDate.plusMonths(-1).toDate());
		final int daysWithDataForLastMonth = days.size() - monthAgoIndex;
		if (daysWithDataForLastMonth < minimalDaysWithDataPerLastMonth) {
			logger.debug("stock " + s.getInstrumentName() + " have only " + daysWithDataForLastMonth + " days for last month");
			errors += "stock " + s.getInstrumentName() + " have only " + daysWithDataForLastMonth + " days for last month\n";
			return errors;
		} else
			logger.info("stock " + s.getInstrumentName() + " have " + daysWithDataForLastMonth + " days for last month");

		final double averageYearVolume = getAverageYearVolume(daysWithDataForLastYear, days);
		if (averageYearVolume < minimalAverageYearVolume) {
			logger.debug("stock " + s.getInstrumentName() + " have only " + averageYearVolume + ", it is too small average volume amount for last year");
			errors += "stock " + s.getInstrumentName() + " have only " + averageYearVolume + ", it is too small average volume amount for last year\n";
		}
		return errors;
	}

	private LocalDate getTodayWithoutHolidays() {
		LocalDate todayDate = new LocalDate(today);
		if (todayDate.getDayOfWeek() == DateTimeConstants.SUNDAY)
			todayDate = todayDate.minusDays(2);
		else if (todayDate.getDayOfWeek() == DateTimeConstants.SATURDAY)
			todayDate = todayDate.minusDays(1);
		return todayDate;
	}

	private double getAverageYearVolume(final int daysWithDataForLastYear, final List<Day> days) {
		double volumeAmount = 0;
		for (int i = daysWithDataForLastYear; i < days.size(); ++i)
			volumeAmount += days.get(i).volume;
		volumeAmount = volumeAmount / daysWithDataForLastYear;
		return volumeAmount;
	}

	private String testLastNYears(final Stock s) {
		String errors = "";
		final LocalDate todayDate = new LocalDate(today);
		final ArrayList<Day> days = s.getDays();

		final int tenYearsAgoIndex = s.findDayIndex(todayDate.plusYears(-lastYearsAmount).toDate());
		final int realDaysForTenYears = days.size() - tenYearsAgoIndex;
		final int expectedDaysForLast10Year = daysPerYear * lastYearsAmount;

		final float averagePercentDaysPerSeveralYear = (float) realDaysForTenYears / expectedDaysForLast10Year;

		if (averagePercentDaysPerSeveralYear < minimalDaysPercentPerLastSeveralYears) {
			logger.debug("stock " + s.getInstrumentName() + " have only " + realDaysForTenYears + " days per last " + lastYearsAmount + " years, thats not enought");
			errors += "stock " + s.getInstrumentName() + " have only " + realDaysForTenYears + " days per last " + lastYearsAmount + " years, thats not enought\n";
		}
		return errors;
	}

	/**
	 * @return null if there is no errors in liquidity test
	 */
	public String isLiquidTestWithError(final Stock s) {
		if (s != null) {
			final String lastPeriodsErrors = testLastPeriods(s);
			final String lastNYearsErrors = testLastNYears(s);
			if (lastPeriodsErrors == "" && lastNYearsErrors == "") {
				return null;
			} else {
				return lastPeriodsErrors + lastNYearsErrors;
			}
		}
		return "Stock could not be null";
	}

	public boolean isLiquid(final Stock s) {
		return isLiquidTestWithError(s) == null;
	}

	// Validity Test

	private String testGapsOnAdjectiveClose(Stock s) {
		final ArrayList<Day> days = s.getDays();

		final int todayIndex = s.findDayIndex(today) - 1;
		for (int i = 1; i < todayIndex; ++i) {
			final double previousAdjective = days.get(i - 1).getAdjClose();
			final double currentAdjective = days.get(i).getAdjClose();
			if (Double.compare(previousAdjective, 0.0) == 0) {
				return "Adjective Close Price could not be Zero (" + s.getInstrumentName() + ":" + days.get(i - 1).getDate() + ")";
			}
			if (Double.compare(currentAdjective, 0.0) == 0) {
				return "Adjective Close Price could not be Zero (" + s.getInstrumentName() + ":" + days.get(i).getDate() + ")";
			}
			if (Math.abs(1.0 - previousAdjective / currentAdjective) > valuableGapInPercents) {
				return "Adjective Close Price Gap found (" + s.getInstrumentName() + ":" + days.get(i - 1).getDate() + ")";
			}
		}
		return "";
	}

	public String isValidWithError(final Stock s) {
		if (s != null) {
			final String gapsOnAdjectiveClose = testGapsOnAdjectiveClose(s);
			if (gapsOnAdjectiveClose == "") {
				return null;
			} else {
				return gapsOnAdjectiveClose;
			}
		}
		return "Stock could not be null";
	}

	public boolean isValid(Stock s) {
		return isValidWithError(s) == null;
	}

}
