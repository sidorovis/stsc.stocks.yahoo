package stsc.simulator.multistarter.grid;

import stsc.algorithms.AlgorithmSettings;
import stsc.common.FromToPeriod;
import stsc.simulator.multistarter.MpIterator;
import stsc.simulator.multistarter.ParameterList;
import stsc.simulator.multistarter.ParameterType;
import stsc.simulator.multistarter.ResetableIterable;
import stsc.simulator.multistarter.ResetableIterator;

public class AlgorithmSettingsGridIterator implements ResetableIterable<AlgorithmSettings> {

	public class Element implements ResetableIterator<AlgorithmSettings>, Cloneable {

		private final ParameterList[] parameters;
		private boolean finished;

		public Element(ParameterList parameterList[], boolean finished) {
			this.parameters = parameterList;
			this.finished = finished;
		}

		public Element clone() {
			ParameterList[] pList = new ParameterList[this.parameters.length];
			for (int i = 0; i < this.parameters.length; ++i) {
				pList[i] = this.parameters[i].clone();
			}
			return new Element(pList, this.finished);
		}

		@Override
		public boolean hasNext() {
			if (finished)
				return false;
			for (int i = 0; i < ParameterType.size.getValue(); ++i) {
				ParameterList list = parameters[i];
				if (list.hasCurrent())
					return true;
			}
			return false;
		}

		@Override
		public AlgorithmSettings next() {
			final AlgorithmSettings result = generateSettings();
			generateNext();
			return result;
		}

		@Override
		public void remove() {
		}

		public AlgorithmSettings current() {
			return generateSettings();
		}

		protected void generateNext() {
			int parameterIndex = 0;
			while (parameterIndex < ParameterType.size.getValue()) {
				final ParameterList list = parameters[parameterIndex];
				if (list.empty()) {
					parameterIndex += 1;
					continue;
				}
				final MpIterator<?> iterator = list.getCurrentParam();
				iterator.increment();
				if (iterator.hasNext()) {
					list.reset();
					return;
				}
				iterator.reset();
				if (list.hasNext()) {
					list.increment();
				} else {
					list.reset();
					parameterIndex += 1;
				}
			}
			finished = true;
			return;
		}

		protected AlgorithmSettings generateSettings() {
			final AlgorithmSettings algoSettings = new AlgorithmSettings(period);

			for (int i = 0; i < ParameterType.typesSize; ++i) {
				final ParameterList list = parameters[i];
				for (MpIterator<?> p : list.getParams()) {
					final String name = p.currentParameter().getName();
					final String value = p.currentParameter().getStringName();
					algoSettings.set(name, value);
				}
			}
			final ParameterList list = parameters[ParameterType.subExecutionType.getValue()];
			for (MpIterator<?> p : list.getParams()) {
				final String subExecutionName = p.currentParameter().getStringName();
				algoSettings.addSubExecutionName(subExecutionName);
			}
			return algoSettings;
		}

		public void reset() {
			finished = false;
			for (ParameterList list : parameters) {
				list.reset();
			}
		}
	}

	private final FromToPeriod period;
	private boolean finished;

	private final ParameterList[] parameters;

	public AlgorithmSettingsGridIterator(final FromToPeriod period, final boolean finished, ParameterList[] parameters) {
		this.period = period;
		this.finished = finished;
		this.parameters = new ParameterList[parameters.length];
		for (int i = 0; i < parameters.length; ++i) {
			this.parameters[i] = parameters[i].clone();
		}
	}

	@Override
	public Element iterator() {
		return new Element(this.parameters, this.finished);
	}

	public Element getResetIterator() {
		return new Element(this.parameters, this.finished);
	}

	public boolean getFinished() {
		return this.finished;
	}

	@Override
	public String toString() {
		String result = period.toString();
		parameters.toString();
		for (ParameterList p : parameters) {
			result += "\n" + p.type.toString() + " " + p.toString();
		}
		return result;
	}
}
