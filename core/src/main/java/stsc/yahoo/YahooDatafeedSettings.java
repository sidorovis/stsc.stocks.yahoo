package stsc.yahoo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import stsc.common.stocks.UnitedFormatFilename;
import stsc.common.stocks.UnitedFormatHelper;
import stsc.common.stocks.UnitedFormatStock;

/**
 * In fact pair of datafeed paths to the yahoo datafeed. <br/>
 * Common data path (with all possible stocks) and filtered data path (only
 * liquid stocks). <br/>
 * TODO please change {@link String} folder storage to {@link Path}.
 */
public final class YahooDatafeedSettings {

	private final String dataFolder;
	private final String filteredDataFolder;

	public YahooDatafeedSettings(String dataFolder, String filteredDataFolder) throws IOException {
		this.dataFolder = checkFolder(dataFolder, "Bad data folder");
		this.filteredDataFolder = checkFolder(filteredDataFolder, "Bad filtered data folder");
	}

	private static String checkFolder(final String dataFolder, final String message) throws IOException {
		final File dataFolderFile = new File(dataFolder);
		if (dataFolderFile.exists() && dataFolderFile.isDirectory()) {
			return dataFolderFile.getPath() + File.separatorChar;
		} else {
			throw new IOException(message + ": " + dataFolder);
		}
	}

	String generateUniteFormatPath(final UnitedFormatFilename unitedFormatFilename) {
		return UnitedFormatHelper.generatePath(dataFolder, unitedFormatFilename);
	}

	public Optional<UnitedFormatStock> getStockFromFileSystem(final UnitedFormatFilename unitedFormatFilename) {
		UnitedFormatStock s = null;
		try (FileInputStream is = new FileInputStream(generateUniteFormatPath(unitedFormatFilename))) {
			s = UnitedFormatStock.readFromUniteFormatFile(is);
		} catch (Exception e) {
		}
		return Optional.ofNullable(s);
	}

	public String getDataFolder() {
		return dataFolder;
	}

	public String getFilteredDataFolder() {
		return filteredDataFolder;
	}

}
