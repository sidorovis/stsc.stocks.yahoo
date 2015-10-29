package stsc.yahoo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import stsc.common.stocks.united.format.UnitedFormatFilename;
import stsc.common.stocks.united.format.UnitedFormatStock;

/**
 * In fact pair of datafeed paths to the yahoo datafeed. <br/>
 * Common data path (with all possible stocks) and filtered data path (only
 * liquid stocks).
 */
public final class YahooDatafeedSettings {

	public static final String DATA_FOLDER = "data";
	public static final String FILTER_DATA_FOLDER = "filtered_data";

	private final Path dataFolder;
	private final Path filteredDataFolder;

	/**
	 * by default it use {@link #DATA_FOLDER}, {@link #FILTER_DATA_FOLDER}
	 * 
	 * @throws IOException
	 */
	public YahooDatafeedSettings() throws IOException {
		this(Paths.get("./"));
	}

	public YahooDatafeedSettings(final Path path) throws IOException {
		this(path.resolve(DATA_FOLDER), path.resolve(FILTER_DATA_FOLDER));
	}

	public YahooDatafeedSettings(final Path dataFolder, final Path filteredDataFolder) throws IOException {
		this.dataFolder = dataFolder;
		this.filteredDataFolder = filteredDataFolder;
		checkFolder(dataFolder, "Bad data folder");
		checkFolder(filteredDataFolder, "Bad filtered data folder");
	}

	private static void checkFolder(final Path dataFolder, final String message) throws IOException {
		final File dataFolderFile = dataFolder.toFile();
		if (dataFolderFile.exists() && dataFolderFile.isDirectory()) {
		} else {
			throw new IOException(message + ": " + dataFolder);
		}
	}

	Path generateUniteFormatPath(final UnitedFormatFilename unitedFormatFilename) {
		return dataFolder.resolve(unitedFormatFilename.getFilename());
	}

	public Optional<UnitedFormatStock> getStockFromFileSystem(final UnitedFormatFilename unitedFormatFilename) {
		UnitedFormatStock s = null;
		try (FileInputStream is = new FileInputStream(generateUniteFormatPath(unitedFormatFilename).toFile())) {
			s = UnitedFormatStock.readFromUniteFormatFile(is);
		} catch (Exception e) {
		}
		return Optional.ofNullable(s);
	}

	public Path getDataFolder() {
		return dataFolder;
	}

	public Path getFilteredDataFolder() {
		return filteredDataFolder;
	}

}
