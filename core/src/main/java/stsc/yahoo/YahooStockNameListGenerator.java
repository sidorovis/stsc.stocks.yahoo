package stsc.yahoo;

import static stsc.common.stocks.UnitedFormatStock.EXTENSION;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import stsc.common.stocks.UnitedFormatStock;
import stsc.stocks.indexes.MarketIndex;
import stsc.stocks.repo.MetaIndicesRepository;

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
	public <T extends Collection<String>> T fillWithIndexesFromBase(T collection) {
		addAll(metaIndicesRepository.getCountryMarketIndices(), collection);
		addAll(metaIndicesRepository.getGlobalMarketIndices(), collection);
		addAll(metaIndicesRepository.getRegionMarketIndices(), collection);
		return collection;
	}

	private <E extends MarketIndex<E>, T extends Collection<String>> void addAll(List<E> countryMarketIndices, T filesystemStockNamesQueue) {
		for (E i : countryMarketIndices) {
			filesystemStockNamesQueue.add(i.getFilesystemName());
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
	public static <T extends Collection<String>> T fillWithExistedFilesFromFolder(Path folderPath, T t) {
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

	public <T extends Collection<String>> T fillWithBeginEndPatterns(String pattern, String endPattern, T t) {
		while (StringUtils.comparePatterns(pattern, endPattern) <= 0) {
			t.add(pattern);
			pattern = StringUtils.nextPermutation(pattern);
		}
		return t;
	}

	public <T extends Collection<String>> T fillWithStockNameLength(int minLength, int maxLength, T t) {
		for (int i = minLength; i <= maxLength; ++i)
			generateTasks(i, t);
		return t;
	}

	private <T extends Collection<String>> void generateTasks(int taskLength, T t) {
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

	private <T extends Collection<String>> void generateNextElement(char[] generatedText, int currentIndex, int size, T t) {
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
