package majhrs16.dl;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import majhrs16.lib.shell.commandline.Executor;
import majhrs16.lib.utils.InfOS;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;

import javax.swing.SwingUtilities;

public class Main {
	public static boolean exit            = false;
	public static Data D                  = new Data();
	public static final VersionManager vm = new VersionManager();

	public static final String name       = "DirectLauncher";
	public static final String version    = "b2.0";
	public static final GUI gui           = new GUI();

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("h", "help", false, "Muestra este mensaje de ayuda.");
		options.addOption("version", true, "Version de Minecraft Java.");
		options.addOption("debug", false, "Opcion para desarrolladores.");
		options.addOption("nick", true, "Nombre del jugador");

		boolean isDOS = false;

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("DirectLauncher", options);
			}

			if (cmd.hasOption("nick")) {
				D.Nick = cmd.getOptionValue("nick");
			}

			if (cmd.hasOption("version")) {
				D.Version = cmd.getOptionValue("version");
				isDOS = true;
			}

		} catch (ParseException e) {
			System.out.println("Error al analizar los argumentos: " + e.getMessage());
			return;
		}

		if (isDOS) {
			launch(D.Version);

		} else {
			vm.start();
			gui.updateListBox();

			SwingUtilities.invokeLater(() -> {
				gui.frame.setSize(800, 600);
				gui.frame.setVisible(true);
			});
		}
	}

	public static void exit() {
		System.exit(0);
	}

	public static void launch(String version) {
		Updater up = new Updater();
		D.Version = version;

		String filename = D.Version + (InfOS.getType().equals("linux") ? ".sh" : ".bat");
		File file = new File(D.MC, filename);

		if (!file.exists()) {
			if (up.updateData()) {
				try {
					String commandLine = D.format().toString();

					try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
						writer.write(commandLine);

					} catch (IOException e) {
						System.out.println("Error al escribir el acceso directo para la version: " + D.Version);
						e.printStackTrace();
						return;
					}

				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					return;
				}

			} else {
				System.out.println("Error al convertir la version: " + D.Version);
				return;
			}
		}

		Executor.execute((InfOS.getType().equals("linux") ? "bash " : "cmd /c ") + filename);
	}
}