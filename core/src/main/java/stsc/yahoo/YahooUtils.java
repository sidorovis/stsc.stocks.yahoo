package stsc.yahoo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import stsc.common.stocks.UnitedFormatFilename;
import stsc.common.stocks.UnitedFormatHelper;

public final class YahooUtils {

	private YahooUtils() {
	}

	public static void copyFilteredStockFile(String dataFolder, String filteredDataFolder, String instrumentName) throws IOException {
		final UnitedFormatFilename filename = UnitedFormatHelper.toFilesystem(instrumentName);
		final File originalFile = new File(UnitedFormatHelper.generatePath(dataFolder, filename));
		final File filteredFile = new File(UnitedFormatHelper.generatePath(filteredDataFolder, filename));
		if (filteredFile.exists() && originalFile.exists() && filteredFile.length() == originalFile.length()) {
			// filteter file exists and have the same size, so do nothing
		} else
			Files.copy(originalFile.toPath(), filteredFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	public static YahooDatafeedSettings createSettings() throws IOException {
		return new YahooDatafeedSettings("./data/", "./filtered_data/");
	}

	public static YahooDatafeedSettings createSettings(String dataFolder, String filteredDataFolder) throws IOException {
		return new YahooDatafeedSettings(dataFolder, filteredDataFolder);
	}
}
