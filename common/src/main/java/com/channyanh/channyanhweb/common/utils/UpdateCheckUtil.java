package com.channyanh.channyanhweb.common.utils;

import com.channyanh.channyanhweb.common.ChannyAnhWEBCommon;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateCheckUtil {

    private final int resourceId;

    public UpdateCheckUtil(int resourceId) {
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        ChannyAnhWEBCommon.getInstance().getScheduler().runAsync(() -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                ChannyAnhWEBCommon.getInstance().getLogger().error("Unable to check for updates: " + exception.getMessage());
            }
        });
    }

    public static void checkForUpdate(int resourceId, String currentVersion) {
        new UpdateCheckUtil(resourceId).getVersion(version -> {
            if (!currentVersion.equals(version)) {
                ChannyAnhWEBCommon.getInstance().getLogger().warning("A new version of ChannyAnhWEB is available. Please update to latest version " + version);
                ChannyAnhWEBCommon.getInstance().getLogger().warning("Download https://www.github.com/ChannyAnhWEB/plugin/releases/latest");
            } else {
                ChannyAnhWEBCommon.getInstance().getLogger().info("Yay! You are using the latest version of ChannyAnhWEB.");
            }
        });
    }
}
