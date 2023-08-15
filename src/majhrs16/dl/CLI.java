package majhrs16.dl;

import java.io.IOException;
import java.util.Map;

import org.jline.keymap.BindingReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import majhrs16.lib.utils.Str;

public class CLI { // FRCASO.
    private boolean exit;
    private String input;

    public CLI() {
        input = "> ";
        exit = false;
    }

    public void start() {
        Thread showThread = new Thread(this::show);
        showThread.start();
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

            if (Main.vm.versions.isEmpty()) {
                sb.append("║ " + Str.ljust("Buscando versiones instaladas...", x - 3, " ") + "║\n");

            } else {
                sb.append("║ " + Str.ljust("Por favor, presione el número que corresponda a su versión:", x - 3, " ") + "║\n");
                int i = 1;
                for (Map.Entry<String, String[]> entry : Main.vm.versions.entrySet()) {
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

                if (key.equals("0") || Main.vm.versions.containsKey(key)) {
                    terminal.close();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return key;
    }
}