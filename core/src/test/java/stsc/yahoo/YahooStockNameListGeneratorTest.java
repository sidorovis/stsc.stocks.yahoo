package stsc.yahoo;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import stsc.stocks.repo.MetaIndicesRepository;
import stsc.stocks.repo.MetaIndicesRepositoryIncodeImpl;

public class YahooStockNameListGeneratorTest {

	@Test
	public void fillWithIndexesFromBase() {
		final YahooStockNames.Builder builder = new YahooStockNames.Builder();
		final MetaIndicesRepository repo = new MetaIndicesRepositoryIncodeImpl();
		final YahooStockNameListGenerator listGenerator = new YahooStockNameListGenerator(repo);
		listGenerator.fillWithIndexesFromBase(builder);
		final int expectedSize = repo.getCountryMarketIndices().size() + repo.getGlobalMarketIndices().size() + repo.getRegionMarketIndices().size();
		final YahooStockNames list = builder.build();
		Assert.assertEquals(expectedSize, list.size());
	}

	@Test
	public void fillWithExistedFilesFromFolder() throws URISyntaxException {
		final YahooStockNames.Builder bForExisted = new YahooStockNames.Builder();
		final Path folderPath = FileSystems.getDefault().getPath(new File(YahooStockNameListGeneratorTest.class.getResource("./").toURI()).getAbsolutePath());
		new YahooStockNameListGenerator(new MetaIndicesRepositoryIncodeImpl()).fillWithExistedFilesFromFolder(folderPath, bForExisted);
		Assert.assertEquals(4, bForExisted.build().size());
		Assert.assertEquals("aaae", bForExisted.build().getNextStockName());
	}

	@Test
	public void fillWithBeginEndPatterns() {
		final YahooStockNames.Builder bFor2 = new YahooStockNames.Builder();
		final MetaIndicesRepository repo = new MetaIndicesRepositoryIncodeImpl();
		final YahooStockNameListGenerator listGenerator = new YahooStockNameListGenerator(repo);
		listGenerator.fillWithBeginEndPatterns("a", "zz", bFor2);
		Assert.assertEquals(26 * 27, bFor2.build().size());
		final YahooStockNames.Builder bFor3 = new YahooStockNames.Builder();
		listGenerator.fillWithBeginEndPatterns("aaa", "zzz", bFor3);
		Assert.assertEquals(26 * 26 * 26, bFor3.build().size());
	}

	@Test
	public void fillWithStockNameLength() {
		final YahooStockNames.Builder bFor1To2 = new YahooStockNames.Builder();
		final MetaIndicesRepository repo = new MetaIndicesRepositoryIncodeImpl();
		final YahooStockNameListGenerator listGenerator = new YahooStockNameListGenerator(repo);
		listGenerator.fillWithStockNameLength(1, 2, bFor1To2);
		Assert.assertEquals(26 + 3 * 26 + 26 * 26, bFor1To2.build().size());
		final YahooStockNames.Builder bFor2To3 = new YahooStockNames.Builder();
		listGenerator.fillWithStockNameLength(2, 3, bFor2To3);
		Assert.assertEquals(26 * (29 + 3 * 26 + 26 * 26), bFor2To3.build().size());
	}
}
