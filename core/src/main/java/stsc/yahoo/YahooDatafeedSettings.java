package stsc.yahoo;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import stsc.common.stocks.UnitedFormatFilename;
import stsc.common.stocks.UnitedFormatHelper;
import stsc.common.stocks.UnitedFormatStock;

public class YahooDatafeedSettings {

	private String dataFolder = "./data/";
	private String filteredDataFolder = "./filtered_data/";

	public YahooDatafeedSettings(String dataFolder, String filteredDataFolder) throws IOException {
		this.dataFolder = checkFolder(dataFolder, "Bad data folder");
		this.filteredDataFolder = checkFolder(filteredDataFolder, "Bad filtered data folder");
	}

	private String checkFolder(final String dataFolder, final String message) throws IOException {
		final File dataFolderFile = new File(dataFolder);
		if (dataFolderFile.exists() && dataFolderFile.isDirectory()) {
			return dataFolderFile.getPath() + File.separatorChar;
		} else {
			throw new IOException(message + ": " + dataFolder);
		}
	}

	public String generateUniteFormatPath(final UnitedFormatFilename unitedFormatFilename) {
		return UnitedFormatHelper.generatePath(dataFolder, unitedFormatFilename);
	}

	public Optional<UnitedFormatStock> getStockFromFileSystem(final UnitedFormatFilename unitedFormatFilename) {
		UnitedFormatStock s = null;
		try {
			s = UnitedFormatStock.readFromUniteFormatFile(generateUniteFormatPath(unitedFormatFilename));
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
