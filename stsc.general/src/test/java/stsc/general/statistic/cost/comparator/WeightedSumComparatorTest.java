package stsc.general.statistic.cost.comparator;

import org.joda.time.LocalDate;

import stsc.general.statistic.Statistics;
import stsc.general.testhelper.TestHelper;
import junit.framework.TestCase;

public class WeightedSumComparatorTest extends TestCase {
	public void testWeightedSumComparator() {
		final Statistics stat = TestHelper.getStatistics();

		final WeightedSumComparator comparator = new WeightedSumComparator();
		comparator.addParameter("getKelly", 0.8);

		assertEquals(0, comparator.compare(stat, stat));

		final Statistics newStat = TestHelper.getStatistics(50, 150, new LocalDate(2013, 5, 1));
		assertEquals(0, comparator.compare(newStat, newStat));

		assertEquals(-1, comparator.compare(stat, newStat));
		assertEquals(1, comparator.compare(newStat, stat));
	}

	public void testWeightedSumComparatorOnSeveralStatistics() {
		final WeightedSumComparator comparator = new WeightedSumComparator();
		comparator.addParameter("getKelly", 0.8);
		comparator.addParameter("getWinProb", 0.4);
		comparator.addParameter("getMaxWin", 0.9);
		for (int i = 1; i < 6; ++i) {
			final Statistics leftStat = TestHelper.getStatistics(50, 150, new LocalDate(2013, 5, i));
			for (int u = i + 20; u < 25; ++u) {
				if (i != u) {
					final Statistics rightStat = TestHelper.getStatistics(50, 150, new LocalDate(2013, 5, u));
					final int r = comparator.compare(leftStat, rightStat) * comparator.compare(rightStat, leftStat);
					if (r != 0)
						assertEquals(-1, r);
				}
			}
		}
	}
}