package stsc.yahoo;

import static stsc.common.stocks.UnitedFormatStock.EXTENSION;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import stsc.common.stocks.UnitedFormatStock;
import stsc.stocks.indexes.MarketIndex;
import stsc.stocks.repo.MetaIndicesRepository;

/**
 * This class store several mechanisms how to generate yahoo stock names lists.
 * <br/>
 * 1. It could use {@link MetaIndicesRepository} to fill data from it (
 * {@link #fillWithIndexesFromBase(stsc.yahoo.YahooStockNames.Builder)} method
 * fill with all Country / Global / Region indices).<br/>
 * 2. It could use files into selected file folder (
 * {@link #fillWithExistedFilesFromFolder(Path, stsc.yahoo.YahooStockNames.Builder)}
 * method).<br/>
 * 3. It could use pattern filling type: for example 'a' ... 'zz', will include
 * 'a', 'b', 'c' ... 'z', 'aa', 'ab' ... 'zz'.
 * {@link #fillWithBeginEndPatterns(String, String, stsc.yahoo.YahooStockNames.Builder)}
 * method.<br/>
 * 4. It could use stock length size (1..4 will generate 'a', 'b', 'z' ...
 * 'aaaz' ... 'zzzz'
 * {@link #fillWithStockNameLength(int, int, stsc.yahoo.YahooStockNames.Builder)}
 * method).
 */
public final class YahooStockNameListGenerator {

	private final MetaIndicesRepository metaIndicesRepository;

	public YahooStockNameListGenerator(final MetaIndicesRepository metaIndicesRepository) {
		this.metaIndicesRepository = metaIndicesRepository;

	}

	/**
	 * Fill collection with names from repository
	 * 
	 * @param collection
	 *            to fill
	 */
	public YahooStockNames.Builder fillWithIndexesFromBase(final YahooStockNames.Builder stockNames) {
		addAll(metaIndicesRepository.getCountryMarketIndices(), stockNames);
		addAll(metaIndicesRepository.getGlobalMarketIndices(), stockNames);
		addAll(metaIndicesRepository.getRegionMarketIndices(), stockNames);
		return stockNames;
	}

	private <E extends MarketIndex<E>> void addAll(List<E> countryMarketIndices, final YahooStockNames.Builder stockNames) {
		for (E i : countryMarketIndices) {
			stockNames.add(i.getFilesystemName());
		}
	}

	/**
	 * Load file names of {@link UnitedFormatStock} from the selected folder.
	 * 
	 * @param folderData
	 *            - folder path where
	 * @param fileNames
	 *            - collection of strings (file names) those have
	 *            {@link #EXTENSION} and placed at the folderData
	 */
	public YahooStockNames.Builder fillWithExistedFilesFromFolder(Path folderPath, YahooStockNames.Builder t) {
		final File folder = folderPath.toFile();
		final File[] listOfFiles = folder.listFiles();
		Arrays.sort(listOfFiles, new FileComparator());
		for (File file : listOfFiles) {
			String filename = file.getName();
			if (file.isFile() && filename.endsWith(EXTENSION)) {
				t.add(filename.substring(0, filename.length() - EXTENSION.length()));
			}
		}
		return t;
	}

	private final static class FileComparator implements Comparator<File> {
		@Override
		public int compare(File left, File right) {
			return getStockName(left).compareTo(getStockName(right));
		}

		private String getStockName(File file) {
			String filename = file.getName();
			filename = filename.substring(0, filename.length() - EXTENSION.length());
			return filename;
		}
	}

	public YahooStockNames.Builder fillWithBeginEndPatterns(String pattern, String endPattern, YahooStockNames.Builder t) {
		while (StringUtils.comparePatterns(pattern, endPattern) <= 0) {
			t.add(pattern);
			pattern = StringUtils.nextPermutation(pattern);
		}
		return t;
	}

	public YahooStockNames.Builder fillWithStockNameLength(int minLength, int maxLength, YahooStockNames.Builder t) {
		for (int i = minLength; i <= maxLength; ++i)
			generateTasks(i, t);
		return t;
	}

	private void generateTasks(int taskLength, YahooStockNames.Builder t) {
		char[] generatedText = new char[taskLength];
		generateNextElement(generatedText, 0, taskLength, t);
		if (taskLength > 1) {
			generatedText[0] = '^';
			generateNextElement(generatedText, 1, taskLength, t);
			generatedText[0] = '.';
			generateNextElement(generatedText, 1, taskLength, t);
			generatedText[0] = '$';
			generateNextElement(generatedText, 1, taskLength, t);
		}
	}

	private void generateNextElement(char[] generatedText, int currentIndex, int size, YahooStockNames.Builder t) {
		for (char c = 'a'; c <= 'z'; ++c) {
			generatedText[currentIndex] = c;
			if (currentIndex == size - 1) {
				String newTask = new String(generatedText);
				t.add(newTask);
			} else {
				generateNextElement(generatedText, currentIndex + 1, size, t);
			}
		}
	}

}
