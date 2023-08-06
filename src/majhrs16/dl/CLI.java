package majhrs16.dl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jline.keymap.BindingReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import majhrs16.lib.utils.Str;

public class CLI {
    public Map<String, String[]> versions;
    private boolean exit;
    private String input;
    private Data D;

    public CLI(Data D) {
        versions = new HashMap<>();
        input = "> ";
        this.D = D;
    }

    public void start() {
        exit = false;

        Thread searchThread = new Thread(this::searchVersions);
        searchThread.setDaemon(true);
        searchThread.start();

//        Thread showThread = new Thread(this::show);
//        showThread.start();
    }

    public void searchVersions() {
        while (!exit) {
            versions = getVersions(".json");

            try { Thread.sleep(1000);
            } catch (InterruptedException e) { ; }
        }
    }    

    public Terminal createTerminal() {
        try {
            TerminalBuilder builder = TerminalBuilder.builder();

            // Forzar la detección del terminal adecuado en Windows
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder = builder.system(true);
            }

            return builder.build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void show() {
    	while (true) {
            if (exit) {
                break;
            }

            int x = 80 - 1; // Ancho del menú
            int y = 20; // Alto del menú

            StringBuilder sb = new StringBuilder();
            sb.append("\n╔" + Str.repeat("═", x - 2) + "╗\n");
            sb.append("║" + Str.center("DirectLauncher b1.5", x - 2) + "║\n");
            sb.append("╠" + Str.repeat("═", x - 2) + "╣\n");

            if (versions.isEmpty()) {
                sb.append("║ " + Str.ljust("Buscando versiones instaladas...", x - 3, " ") + "║\n");

            } else {
                sb.append("║ " + Str.ljust("Por favor, presione el número que corresponda a su versión:", x - 3, " ") + "║\n");
                int i = 1;
                for (Map.Entry<String, String[]> entry : versions.entrySet()) {
                    String version = entry.getValue()[0];
                    sb.append("║ " + Str.ljust(i + ") " + version, x - 3, " ") + "║\n");
                    i++;
                }
            }

            for (int i = sb.toString().split("\n").length + 2; i <= y; i++)
                sb.append("║" + Str.repeat(" ", x - 2) + "║\n");

            sb.append("║ " + Str.ljust(input, x - 3, " ") + "║\n");

            sb.append("╚" + Str.repeat("═", x - 2) + "╝");

            System.out.print(sb.toString());

            try {
                Thread.sleep(1000 / 5);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            clearConsole();
        }
    }

    public void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();

            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        exit = true;
    }

    public String getKey() {
        String key = "";
        try {
            Terminal terminal = createTerminal();
            if (terminal == null)
                terminal = TerminalBuilder.builder().dumb(true).build();
            BindingReader bindingReader = new BindingReader(terminal.reader());

            while (!exit) {
                key = "" + (char) bindingReader.readCharacter();

                input = "> " + key;

                if (key.equals("0") || versions.containsKey(key)) {
                    terminal.close();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return key;
    }

    private Map<String, String[]> getVersions(String... exts) {
    	Map<String, String[]> versions = new HashMap<>();
        int i = 1;
        for (String ext : exts) {
            File[] files = new File(D.MC, "versions").listFiles();
            if (files != null) {
                for (File file : files) {
                    File[] versionFiles = file.listFiles((dir, name) -> name.endsWith(ext));
                    if (versionFiles != null) {
                        for (File versionFile : versionFiles) {
                            String versionName = versionFile.getName();
                            String version = versionName.substring(0, versionName.lastIndexOf(ext));
                            versions.put(String.valueOf(i), new String[]{version, versionFile.getAbsolutePath()});
                            i++;
                        }
                    }
                }
            }
        }
        return versions;
    }

}