package github.scarsz.discordsrv.objects.metrics;

import com.google.gson.*;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.LangUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @on 3/13/2017
 * @at 12:58 PM
 */
public class MetricsManager {

    private Map<String, AtomicInteger> metrics = new HashMap<>();
    private final File metricsFile;

    public MetricsManager(File metricsFile) {
        this.metricsFile = metricsFile;
        if (!metricsFile.exists()) return;

        try {
            String json = "";
            for (String s : FileUtils.readFileToString(metricsFile, Charset.forName("UTF-8")).split("\\[|, |]"))
                if (!s.trim().isEmpty()) json += Character.toChars(Integer.parseInt(s))[0];

            for (Map.Entry<String, JsonElement> entry : new Gson().fromJson(json, JsonObject.class).entrySet())
                metrics.put(entry.getKey(), new AtomicInteger(entry.getValue().getAsInt()));
        } catch (IOException e) {
            System.out.println("Failed loading Metrics: " + e.getMessage());
            metricsFile.delete();
        }
    }

    public void save() {
        if (metrics.size() == 0) {
            DiscordSRV.info(LangUtil.InternalMessage.METRICS_SAVE_SKIPPED);
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            JsonObject map = new JsonObject();
            metrics.forEach((key, atomicInteger) -> map.addProperty(key, atomicInteger.intValue()));
            FileUtils.writeStringToFile(metricsFile, Arrays.toString(map.toString().getBytes()), Charset.forName("UTF-8"));
        } catch (IOException e) {
            DiscordSRV.error(LangUtil.InternalMessage.METRICS_SAVE_FAILED + ": " + e.getMessage());
            return;
        }

        DiscordSRV.info(LangUtil.InternalMessage.METRICS_SAVED.toString()
                .replace("{ms}", String.valueOf(System.currentTimeMillis() - startTime))
        );
    }

    public void increment(String key) {
        if (metrics.containsKey(key.toLowerCase())) {
            metrics.get(key.toLowerCase()).getAndIncrement();
        } else {
            metrics.put(key.toLowerCase(), new AtomicInteger(1));
        }
    }
    public int get(String key) {
        return metrics.containsKey(key.toLowerCase()) ? metrics.get(key.toLowerCase()).intValue() : 0;
    }

}
