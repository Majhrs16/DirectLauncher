package majhrs16.dl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import majhrs16.lib.shell.commandline.Executor;

public class Main {
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("h", "help", false, "Muestra este mensaje de ayuda.");
		options.addOption("version", true, "Version de Minecraft Java.");
		options.addOption("debug", false, "Opcion para desarrolladores.");
		options.addOption("nick", true, "Nombre del jugador");

		Data D = new Data();
		
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
			}

		} catch (ParseException e) {
			System.out.println("Error al analizar los argumentos: " + e.getMessage());
			return;
		}

        /*// EN DESARROLLO!
        CLI cli = new CLI(D);
        cli.start();
        String key = cli.getKey();
    	cli.stop();
        if (key.equals("0"))
        	return;
        else
        	System.out.println(cli.versions.get(key)[0]);
       	*/

		Updater up = new Updater();
		if (up.updateData(D)) {
			try {
				String s = D.format().toString();
				System.out.println("\n" + s);
				Executor.execute(s);
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
