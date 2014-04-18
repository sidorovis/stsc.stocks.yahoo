package stsc.simulator.multistarter;

import java.util.Arrays;

import stsc.algorithms.BadAlgorithmException;
import stsc.algorithms.EodAlgorithm;
import stsc.algorithms.StockAlgorithm;
import stsc.common.FromToPeriod;
import stsc.simulator.MultiSimulatorSettings;
import stsc.simulator.SimulatorSettings;
import stsc.storage.AlgorithmsStorage;
import stsc.storage.ExecutionsStorage;
import stsc.storage.StockStorage;
import stsc.testhelper.StockStorageHelper;
import stsc.testhelper.TestHelper;
import stsc.trading.Broker;
import junit.framework.TestCase;

public class MultiSimulatorSettingsTest extends TestCase {

	private String algoStockName(String aname) throws BadAlgorithmException {
		return AlgorithmsStorage.getInstance().getStock(aname).getName();
	}

	private String algoEodName(String aname) throws BadAlgorithmException {
		return AlgorithmsStorage.getInstance().getEod(aname).getName();
	}

	public void testEmptyMultiSimulatorSettings() throws BadAlgorithmException, BadParameterException {
		final StockStorage stockStorage = new StockStorageHelper();
		final FromToPeriod period = TestHelper.getPeriod();

		final MultiSimulatorSettings settings = new MultiSimulatorSettings(stockStorage, period);
		int count = 0;
		for (SimulatorSettings simulatorSettings : settings) {
			count += 1;
			assertNotNull(simulatorSettings);
		}
		assertEquals(0, count);
	}

	public void testMultiSimulatorSettings() throws BadAlgorithmException, BadParameterException {
		final StockStorage stockStorage = new StockStorageHelper();
		final FromToPeriod period = TestHelper.getPeriod();

		final MultiSimulatorSettings settings = new MultiSimulatorSettings(stockStorage, period);
		final MultiAlgorithmSettings in = new MultiAlgorithmSettings(period);
		in.add(new MpString("e", Arrays.asList(new String[] { "open", "high", "low", "close", "value" })));
		settings.addStock("in", algoStockName("In"), in);

		final MultiAlgorithmSettings ema = new MultiAlgorithmSettings(period);
		ema.add(new MpDouble("P", 0.1, 0.6, 0.1));
		ema.add(new MpSubExecution("", Arrays.asList(new String[] { "e" })));
		settings.addStock("ema", algoStockName("Ema"), ema);

		final MultiAlgorithmSettings level = new MultiAlgorithmSettings(period);
		level.add(new MpDouble("f", 15.0, 20.0, 1.0));
		level.add(new MpSubExecution("", Arrays.asList(new String[] { "in", "ema" })));
		settings.addStock("level", algoStockName("Level"), level);

		final MultiAlgorithmSettings oneSide = new MultiAlgorithmSettings(period);
		oneSide.add(new MpString("side", Arrays.asList(new String[] { "long", "short" })));
		settings.addEod("os", algoEodName("OneSideOpenAlgorithm"), oneSide);

		final MultiAlgorithmSettings positionSide = new MultiAlgorithmSettings(period);
		positionSide.add(new MpSubExecution("", Arrays.asList(new String[] { "in", "level", "ema" })));
		positionSide.add(new MpSubExecution("", Arrays.asList(new String[] { "level", "ema" })));
		positionSide.add(new MpInteger("n", 1, 32, 10));
		positionSide.add(new MpInteger("m", 1, 32, 10));
		positionSide.add(new MpDouble("ps", 50000.0, 200001.0, 50000.0));
		settings.addEod("pnm", algoEodName("PositionNDayMStocks"), positionSide);

		int count = 0;
		for (SimulatorSettings simulatorSettings : settings) {
			count += 1;
			final ExecutionsStorage executionsStorage = simulatorSettings.getInit().getExecutionsStorage();
			executionsStorage.initialize(new Broker(stockStorage));
			final StockAlgorithm sain = executionsStorage.getStockAlgorithm("in", "aapl");
			final StockAlgorithm saema = executionsStorage.getStockAlgorithm("ema", "aapl");
			final StockAlgorithm salevel = executionsStorage.getStockAlgorithm("level", "aapl");
			final EodAlgorithm saone = executionsStorage.getEodAlgorithm("os");
			assertNotNull(sain);
			assertNotNull(saema);
			assertNotNull(salevel);
			assertNotNull(saone);
		}
		assertEquals(5 * 5 * 5 * 2 * 2 * 3 * 2 * 4 * 4 * 4, count);
	}
}
