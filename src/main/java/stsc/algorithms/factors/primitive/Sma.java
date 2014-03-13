package stsc.algorithms.factors.primitive;

import java.util.LinkedList;
import stsc.algorithms.StockAlgorithm;
import stsc.algorithms.StockSignal;
import stsc.common.Day;
import stsc.storage.BadSignalException;

public class Sma extends StockAlgorithm {

	static public class Signal extends StockSignal {
		final public Double value;

		public Signal(final double value) {
			this.value = new Double(value);
		}
	}

	static final int n = 5;
	final LinkedList<Double> elements = new LinkedList<>();
	Double sum = new Double(0.0);

	@Override
	public Class<? extends StockSignal> registerSignalsClass() {
		return Signal.class;
	}

	@Override
	public void process(String stockName, Day day) throws BadSignalException {
		final double price = day.prices.getOpen();
		elements.push(price);
		sum += price;
		if (elements.size() == n) {
			addSignal(day.getDate(), new Signal(sum / n));
		} else if (elements.size() > n) {
			Double lastElement = elements.pollLast();
			sum -= lastElement;
			addSignal(day.getDate(), new Signal(sum / n));
		}
	}
}