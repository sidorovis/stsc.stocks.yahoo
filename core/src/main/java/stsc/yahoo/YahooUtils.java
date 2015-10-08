package stsc.yahoo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import stsc.common.stocks.UnitedFormatFilename;
import stsc.common.stocks.UnitedFormatHelper;

public final class YahooUtils {

	private YahooUtils() {
	}

	public static void copyFilteredStockFile(Path dataFolder, Path filteredDataFolder, String instrumentName) throws IOException {
		final UnitedFormatFilename filename = UnitedFormatHelper.toFilesystem(instrumentName);
		final File originalFile = dataFolder.resolve(filename.getFilename()).toFile();
		final File filteredFile = filteredDataFolder.resolve(filename.getFilename()).toFile();
		if (filteredFile.exists() && originalFile.exists() && filteredFile.length() == originalFile.length()) {
			// filteter file exists and have the same size, so do nothing
		} else
			Files.copy(originalFile.toPath(), filteredFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
