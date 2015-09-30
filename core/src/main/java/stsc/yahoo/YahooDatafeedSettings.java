package stsc.yahoo;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import stsc.common.stocks.UnitedFormatStock;

public class YahooDatafeedSettings {

	private final ConcurrentLinkedQueue<String> filesystemStockNamesForLoadQueue = new ConcurrentLinkedQueue<String>();
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

	public int taskQueueSize() {
		return filesystemStockNamesForLoadQueue.size();
	}

	public YahooDatafeedSettings addTask(String s) {
		filesystemStockNamesForLoadQueue.add(s);
		return this;
	}

	/**
	 * into file system format for example: "_094FTSE"
	 * 
	 * @return
	 */
	public String getFilesystemStockName() {
		return filesystemStockNamesForLoadQueue.poll();
	}

	public String generateUniteFormatPath(String filesystemName) {
		return UnitedFormatStock.generatePath(dataFolder, filesystemName);
	}

	public Optional<UnitedFormatStock> getStockFromFileSystem(String filesystemName) {
		UnitedFormatStock s = null;
		try {
			s = UnitedFormatStock.readFromUniteFormatFile(generateUniteFormatPath(filesystemName));
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

	public Queue<String> getFilesystemStockNamesQueue() {
		return filesystemStockNamesForLoadQueue;
	}

}
