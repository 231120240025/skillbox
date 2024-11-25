package searchengine.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.IndexingService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class IndexingController {

    private static final Logger logger = LoggerFactory.getLogger(IndexingController.class);
    private final IndexingService indexingService;

    @Autowired
    public IndexingController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @GetMapping("/api/startIndexing")
    public Map<String, Object> startIndexing() {
        Map<String, Object> response = new HashMap<>();
        logger.info("Получен запрос на запуск индексации.");
        if (indexingService.isIndexingInProgress()) {
            logger.warn("Индексация уже запущена.");
            response.put("result", false);
            response.put("error", "Индексация уже запущена");
            return response;
        }
        boolean started = indexingService.startIndexing();
        if (started) {
            logger.info("Индексация успешно запущена.");
            response.put("result", true);
        } else {
            logger.error("Не удалось запустить индексацию.");
            response.put("result", false);
            response.put("error", "Не удалось запустить индексацию");
        }
        return response;
    }

    @GetMapping("/api/stopIndexing")
    public Map<String, Object> stopIndexing() {
        Map<String, Object> response = new HashMap<>();
        logger.info("Получен запрос на остановку индексации.");
        if (!indexingService.isIndexingInProgress()) {
            logger.warn("Попытка остановить индексацию, которая не запущена.");
            response.put("result", false);
            response.put("error", "Индексация не запущена");
            return response;
        }
        indexingService.stopIndexing();
        logger.info("Индексация успешно остановлена.");
        response.put("result", true);
        return response;
    }
}
