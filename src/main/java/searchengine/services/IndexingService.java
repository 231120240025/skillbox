package searchengine.services;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

@Service
@Getter
public class IndexingService {

    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);
    private volatile boolean indexingInProgress = false;
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    public IndexingService(SitesList sitesList, SiteRepository siteRepository, PageRepository pageRepository) {
        this.sitesList = sitesList;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
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

                // Удаление данных перед началом индексации
                deleteSiteData(site.getUrl());

                try {
                    // Реализация логики индексации (заглушка)
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

    private void deleteSiteData(String siteUrl) {
        logger.info("Удаление данных для сайта: {}", siteUrl);

        siteRepository.findByUrl(siteUrl).ifPresent(site -> {
            pageRepository.deleteBySiteId(site.getId());
            siteRepository.delete(site);
            logger.info("Данные для сайта {} удалены.", siteUrl);
        });
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
