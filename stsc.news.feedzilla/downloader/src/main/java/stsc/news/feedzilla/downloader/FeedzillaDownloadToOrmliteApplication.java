package stsc.news.feedzilla.downloader;

import graef.feedzillajava.Article;
import graef.feedzillajava.Category;
import graef.feedzillajava.Subcategory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.XMLConfigurationFactory;

import stsc.news.feedzilla.FeedzillaOrmliteStorage;
import stsc.news.feedzilla.ormlite.schema.FeedzillaOrmliteArticle;
import stsc.news.feedzilla.ormlite.schema.FeedzillaOrmliteCategory;
import stsc.news.feedzilla.ormlite.schema.FeedzillaOrmliteSubcategory;

final class FeedzillaDownloadToOrmliteApplication implements LoadFeedReceiver {

	static {
		System.setProperty(XMLConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "./config/log4j2.xml");
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	private static Logger logger = LogManager.getLogger(FeedzillaDownloadToOrmliteApplication.class);

	private static String PRODUCTION_FILENAME = "feedzilla_production.properties";
	private static String DEVELOPER_FILENAME = "feedzilla_developer.properties";

	private static FeedzillaDownloadToOrmliteApplication downloadApplication;

	private final FeedzillaOrmliteStorage feedzillaStorage;
	private final FeedDataDownloader downloader;

	FeedzillaDownloadToOrmliteApplication() throws SQLException, IOException {
		this(DEVELOPER_FILENAME);
	}

	FeedzillaDownloadToOrmliteApplication(String propertyFile) throws SQLException, IOException {
		this.feedzillaStorage = new FeedzillaOrmliteStorage(propertyFile);
		this.downloader = new FeedDataDownloader(1, 100);
		downloader.addReceiver(this);
	}

	void startDownload() {
		downloader.download();
		for (int i = 3650; i > 1; --i) {
			if (downloader.isStopped()) {
				break;
			}
			downloader.setDaysToDownload(i);
			downloader.download();
		}
	}

	private FeedzillaOrmliteCategory createFeedzillaCategory(Category from) {
		final FeedzillaOrmliteCategory result = new FeedzillaOrmliteCategory(from.getDisplayName(), from.getEnglishName(),
				from.getUrlName());
		return feedzillaStorage.update(result);
	}

	private FeedzillaOrmliteSubcategory createFeedzillaSubcategory(FeedzillaOrmliteCategory categoryFrom, Subcategory from) {
		final FeedzillaOrmliteSubcategory result = new FeedzillaOrmliteSubcategory(categoryFrom, from.getDisplayName(),
				from.getEnglishName(), from.getUrlName());
		return feedzillaStorage.update(result);
	}

	private FeedzillaOrmliteArticle createFeedzillaArticle(FeedzillaOrmliteSubcategory subcategory, Article from) {
		final FeedzillaOrmliteArticle to = new FeedzillaOrmliteArticle(subcategory, from.getAuthor(), from.getPublishDate().toDate());
		to.setSource(from.getSource());
		to.setSourceUrl(from.getSourceUrl());
		to.setSummary(from.getSummary());
		to.setTitle(from.getTitle());
		to.setUrl(from.getUrl());

		return feedzillaStorage.update(to);
	}

	private void stop() throws InterruptedException {
		downloader.stopDownload();
	}

	@Override
	public void newArticle(Category newCategory, Subcategory newSubcategory, Article newArticle) {
		final FeedzillaOrmliteCategory category = createFeedzillaCategory(newCategory);
		final FeedzillaOrmliteSubcategory subcategory = createFeedzillaSubcategory(category, newSubcategory);
		createFeedzillaArticle(subcategory, newArticle);
	}

	public static void main(String[] args) {
		final CountDownLatch waitForStarting = new CountDownLatch(1);
		final CountDownLatch waitForEnding = new CountDownLatch(1);
		try {
			final Thread mainProcessingThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (args.length > 0 && args[0] == "production") {
							logger.info("Started production version");
							downloadApplication = new FeedzillaDownloadToOrmliteApplication(PRODUCTION_FILENAME);
						} else {
							logger.info("Started developer version");
							downloadApplication = new FeedzillaDownloadToOrmliteApplication(DEVELOPER_FILENAME);
						}
						waitForStarting.countDown();
					} catch (Exception e) {
						logger.error("Error on main execution thread", e);
					}
					downloadApplication.startDownload();
					waitForEnding.countDown();
				}
			});
			mainProcessingThread.start();
			waitForStarting.await();
			logger.info("Please enter 'e' and press Enter to stop application.");
			addExitHook(waitForEnding);
			waitForEnding.await();
			mainProcessingThread.join();
		} catch (Exception e) {
			logger.error("Error on main function. ", e);
		}
	}

	private static void addExitHook(final CountDownLatch waitForEnding) {
		try {
			try {
				final InputStreamReader fileInputStream = new InputStreamReader(System.in);
				final BufferedReader bufferedReader = new BufferedReader(fileInputStream);

				while (true) {
					if (bufferedReader.ready()) {
						final String s = bufferedReader.readLine();
						if (s.equals("e")) {
							downloadApplication.stop();
							break;
						}
					}
					if (waitForEnding.getCount() == 0) {
						downloadApplication.stop();
						break;
					}
				}
				bufferedReader.close();
			} catch (Exception e) {
				logger.error("Error on exit hook. ", e);
				downloadApplication.stop();
			}
		} catch (Exception e) {
			logger.error("Error on exit hook with non stop. ", e);
		}
	}
}