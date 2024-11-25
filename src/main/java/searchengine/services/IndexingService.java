package searchengine.services;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;

@Service
@Getter
public class IndexingService {

    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);
    private volatile boolean indexingInProgress = false;
    private final SitesList sitesList;

    public IndexingService(SitesList sitesList) {
        this.sitesList = sitesList;
    }

    public synchronized boolean startIndexing() {
        if (indexingInProgress) {
            return false;
        }

        indexingInProgress = true;
        try {
            performIndexingAsync();
            return true;
        } catch (Exception e) {
            logger.error("Ошибка при запуске индексации", e);
            indexingInProgress = false;
            return false;
        }
    }

    @Async
    public void performIndexingAsync() {
        try {
            logger.info("Индексация началась...");
            sitesList.getSites().forEach(site -> {
                logger.info("Индексация сайта: {} ({})", site.getName(), site.getUrl());
                // Здесь можно добавить реальную логику индексации
                try {
                    Thread.sleep(1000); // Имитация индексации сайта
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Индексация сайта прервана: {}", site.getUrl(), e);
                }
            });
            logger.info("Индексация завершена для всех сайтов.");
        } catch (Exception e) {
            logger.error("Ошибка во время индексации", e);
        } finally {
            indexingInProgress = false;
        }
    }

    public synchronized void stopIndexing() {
        if (indexingInProgress) {
            indexingInProgress = false;
            logger.info("Индексация была остановлена.");
        } else {
            logger.warn("Попытка остановить индексацию, которая не была запущена.");
        }
    }
}
