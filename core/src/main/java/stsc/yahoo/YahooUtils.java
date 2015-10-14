package stsc.yahoo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import stsc.common.stocks.UnitedFormatFilename;
import stsc.common.stocks.UnitedFormatHelper;

/**
 * This class store helper methods for controlling yahoo datafeed. <br/>
 * It very close to FileUtils like helpers, but store small stsc related
 * knowledge about how Yahoo filesystem datafeed stored.
 */
public final class YahooUtils {

	private YahooUtils() {
	}

	public static void copyFilteredStockFile(final Path dataFolder, final Path filteredDataFolder, final String instrumentName) throws IOException {
		final UnitedFormatFilename filename = UnitedFormatHelper.toFilesystem(instrumentName);
		final File originalFile = dataFolder.resolve(filename.getFilename()).toFile();
		final File filteredFile = filteredDataFolder.resolve(filename.getFilename()).toFile();
		if (filteredFile.exists() && originalFile.exists() && filteredFile.length() == originalFile.length()) {
			// filter file exists and have the same size, so do nothing
		} else
			Files.copy(originalFile.toPath(), filteredFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
