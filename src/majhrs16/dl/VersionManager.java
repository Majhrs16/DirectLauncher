package majhrs16.dl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VersionManager {
    public Map<String, String[]> versions = new HashMap<>();

    public void start() {
        Thread searchThread = new Thread(this::searchVersions);
        searchThread.setDaemon(true);
        searchThread.start();
    }

    public void searchVersions() {
        while (!Main.exit) {
            versions = getVersions(".json");

            try {
            	Thread.sleep(5000);

            } catch (InterruptedException e) { ; }
        }
    }
	
    private Map<String, String[]> getVersions(String... exts) {
    	Map<String, String[]> versions = new HashMap<>();
        int i = 1;

        for (String ext : exts) {
            File[] files = new File(Main.D.MC, "versions").listFiles();
            if (files != null) {
                for (File file : files) {
                    File[] versionFiles = file.listFiles((dir, name) -> name.endsWith(ext));
                    if (versionFiles != null) {
                        for (File versionFile : versionFiles) {
                            String versionName = versionFile.getName();
                            String version = versionName.substring(0, versionName.lastIndexOf(ext));
                            versions.put(String.valueOf(i), new String[]{versionFile.getAbsolutePath(), version});
                            i++;
                        }
                    }
                }
            }
        }

        return versions;
    }
}
