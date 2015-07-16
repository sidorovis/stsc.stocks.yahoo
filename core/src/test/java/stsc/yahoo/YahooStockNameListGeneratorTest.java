package stsc.yahoo;

import java.nio.file.FileSystems;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import stsc.stocks.repo.MetaIndicesRepository;
import stsc.stocks.repo.MetaIndicesRepositoryIncodeImpl;

public class YahooStockNameListGeneratorTest {

	@Test
	public void fillWithIndexesFromBase() {
		final ArrayList<String> list = new ArrayList<>();
		final MetaIndicesRepository repo = new MetaIndicesRepositoryIncodeImpl();
		final YahooStockNameListGenerator listGenerator = new YahooStockNameListGenerator(repo);
		listGenerator.fillWithIndexesFromBase(list);
		final int expectedSize = repo.getCountryMarketIndices().size() + repo.getGlobalMarketIndices().size() + repo.getRegionMarketIndices().size();
		Assert.assertEquals(expectedSize, list.size());
	}

	@Test
	public void fillWithExistedFilesFromFolder() {
		final ArrayList<String> list = new ArrayList<>();
		YahooStockNameListGenerator.fillWithExistedFilesFromFolder(FileSystems.getDefault().getPath("./test_data"), list);
		Assert.assertEquals(4, list.size());
		Assert.assertEquals("aaae", list.get(0));
	}

	@Test
	public void fillWithBeginEndPatterns() {
		final ArrayList<String> list = new ArrayList<>();
		final MetaIndicesRepository repo = new MetaIndicesRepositoryIncodeImpl();
		final YahooStockNameListGenerator listGenerator = new YahooStockNameListGenerator(repo);
		listGenerator.fillWithBeginEndPatterns("a", "zz", list);
		Assert.assertEquals(26 * 27, list.size());
		list.clear();
		listGenerator.fillWithBeginEndPatterns("aaa", "zzz", list);
		Assert.assertEquals(26 * 26 * 26, list.size());
	}

	@Test
	public void fillWithStockNameLength() {
		final ArrayList<String> list = new ArrayList<>();
		final MetaIndicesRepository repo = new MetaIndicesRepositoryIncodeImpl();
		final YahooStockNameListGenerator listGenerator = new YahooStockNameListGenerator(repo);
		listGenerator.fillWithStockNameLength(1, 2, list);
		Assert.assertEquals(26 + 3 * 26 + 26 * 26, list.size());
		list.clear();
		listGenerator.fillWithStockNameLength(2, 3, list);
		Assert.assertEquals(26 * (29 + 3 * 26 + 26 * 26), list.size());
	}
}
