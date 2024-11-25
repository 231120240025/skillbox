package searchengine.services;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;

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
            logger.warn("Попытка запуска индексации, которая уже выполняется.");
            return false;
        }

        indexingInProgress = true;
        try {
            logger.info("Запуск асинхронной индексации.");
            performIndexingAsync();
            return true;
        } catch (Exception e) {
            logger.error("Ошибка при запуске индексации.", e);
            indexingInProgress = false;
            return false;
        }
    }

    @Async
    public void performIndexingAsync() {
        try {
            logger.info("Индексация началась...");
            sitesList.getSites().forEach(siteConfig -> {
                logger.info("Индексация сайта: {} ({})", siteConfig.getName(), siteConfig.getUrl());

                deleteSiteData(siteConfig.getUrl());
                createNewSiteEntry(siteConfig.getName(), siteConfig.getUrl());

                try {
                    performSiteIndexing(siteConfig.getUrl());
                } catch (Exception e) {
                    logger.error("Ошибка при индексации сайта: {}", siteConfig.getUrl(), e);
                }
            });
            logger.info("Индексация завершена для всех сайтов.");
        } catch (Exception e) {
            logger.error("Ошибка во время индексации.", e);
        } finally {
            indexingInProgress = false;
        }
    }

    private void deleteSiteData(String siteUrl) {
        logger.info("Удаление данных для сайта: {}", siteUrl);

        siteRepository.findByUrl(siteUrl).ifPresentOrElse(site -> {
            pageRepository.deleteBySiteId(site.getId());
            siteRepository.delete(site);
            logger.info("Данные для сайта {} удалены.", siteUrl);
        }, () -> logger.warn("Сайт {} не найден в базе данных. Удаление пропущено.", siteUrl));
    }

    private void createNewSiteEntry(String siteName, String siteUrl) {
        logger.info("Создание новой записи для сайта: {}", siteUrl);
        Site newSite = new Site();
        newSite.setName(siteName);
        newSite.setUrl(siteUrl);
        newSite.setStatus(Status.INDEXING);
        newSite.setStatusTime(LocalDateTime.now());
        siteRepository.save(newSite);
        logger.info("Новая запись для сайта {} успешно создана.", siteUrl);
    }

    private void performSiteIndexing(String siteUrl) throws InterruptedException {
        logger.info("Индексация контента для сайта: {}", siteUrl);
        Thread.sleep(1000); // Заглушка
        logger.info("Индексация завершена для сайта: {}", siteUrl);
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
