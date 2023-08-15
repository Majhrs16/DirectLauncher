package majhrs16.dl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Data implements Cloneable {
    public Data() {}
    
    public static String join(String... paths) {
        Path result = Paths.get(paths[0]);

        for (int i = 1; i < paths.length; i++) {
            result = result.resolve(paths[i]);
        }

        return result.toString();
    }
    
    public String _format(Data D, String s) {
        Map<String, String> data = new HashMap<>();
        data.put("Debug", D.Debug.toString());
        data.put("JVM", D.JVM);
        data.put("MinRam", D.MinRam);
        data.put("MaxRam", D.MaxRam);
        data.put("MC", D.MC);
        data.put("Lib", D.Lib);
        data.put("CLASSPATH", String.join(D._sep, D.CLASSPATH));
        data.put("Natives", D.Natives);
        data.put("MainClass", D.MainClass);
        data.put("Nick", D.Nick);
        data.put("Token", D.Token);
        data.put("Version", D.Version);
        data.put("AssetsIndex", D.AssetsIndex);
        data.put("Flags.JVM", String.join(" ", D.Flags.JVM));
        data.put("Flags.MC", String.join(" ", D.Flags.MC));

        for (Map.Entry<String, String> entry : data.entrySet()) {
            s = s.replace("{Data." + entry.getKey() + "}", entry.getValue());
        }
       
        return s;
    }
    
    public Data format() throws CloneNotSupportedException {
    	Data D = (Data) clone();

        D.JVM         = _format(D, JVM);
        D.MC          = _format(D, MC);
        D.Lib         = _format(D, Lib);
        D.CLASSPATH   = _format(D, String.join(_sep, CLASSPATH)).split(_sep);
        D.Natives     = _format(D, Natives);
//      D.MainClass   = MainClass;
        if (!Nick.isEmpty())
            D.Nick    = "--username \"" + Nick + "\"";
//      D.Token       = Token;
//      D.Version     = Version;
//      D.AssetsIndex = AssetsIndex;
        D.Flags.JVM   = _format(D, String.join(" ", Flags.JVM)).split(" ");
        D.Flags.MC    = _format(D, String.join(" ", Flags.MC)).split(" ");
    	return D;
    }
	
    public String toString() {
    	return String.join(" ", JVM, String.join(" ", Flags.JVM), MainClass, String.join(" ", Flags.MC));
    }

    public String _sep = File.separator.replace("\\", "\\\\").equals("/") ? ":" : ";";

	public Boolean Debug = false;
    public String JVM = "java";
    public String MinRam = "1G";
    public String MaxRam = "1G";
    public String MC = System.getProperty("user.dir");
    public String Lib = join("{Data.MC}", "libraries");
    public String[] CLASSPATH = {
		join("{Data.Lib}", "log4j-core-2.17.0.jar"),
        join("{Data.Lib}", "log4j-api-2.17.0.jar"),
        join("{Data.MC}", "versions", "{Data.Version}", "{Data.Version}.jar"),
    };
    public String Natives = join("{Data.MC}", "versions", "{Data.Version}", "natives");
    public String MainClass = "net.minecraft.client.main.Main";
    public String Nick = "";
    public String Token = "null";
    public String Version = "CM";
    public String AssetsIndex = "1.8";
    public _Flags Flags = new _Flags();

    private class _Flags {
    	public String[] JVM = { 
            "-Xmx{Data.MinRam}",
            "-Xmx{Data.MaxRam}",
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch",
            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40",
            "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:InitiatingHeapOccupancyPercent=15",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",
            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1",
            "-Dusing.aikars.flags=https://mcflags.emc.gs",
            "-Daikars.new.flags=true",
            "-XX:+IgnoreUnrecognizedVMOptions",
            "-XX:MaxGCPauseMillis=50",
            "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump",
            "-Dsun.java2d.d3d=false",
            "-Dsun.java2d.opengl=true",
            "-Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true",
//          "-Xlog:gc*:logs/gc.log:time,uptime:filecount=5,filesize=1M", // Da problemas en <J12
            "-XX:+AggressiveOpts",
            "-XX:-UseCompressedOops",
            "-XX:ParallelGCThreads=4",
//          "-Dorg.lwjgl.opengl.Display.setFullscreen=true", // Innecesario
            "-Djava.library.path={Data.Natives}",
            "-cp {Data.CLASSPATH}",
            "-Dlog4j.configurationFile=" + join("{Data.Lib}", "client-1.7.xml")
    	};
    	
    	public String[] MC = {
            "--gameDir " + join("{Data.MC}", "{Data.Version}"),
            "--assetsDir " + join("{Data.MC}", "assets"),
            "{Data.Nick}",
            "--accessToken {Data.Token}",
            "--version \"{Data.Version}\"",
            "--assetIndex {Data.AssetsIndex}",
    	};
    }
}
