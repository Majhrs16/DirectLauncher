package majhrs16.dl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import majhrs16.lib.utils.files.Downloader;
import majhrs16.lib.utils.files.FilePath;
import majhrs16.lib.shell.ProgressBar;
import majhrs16.lib.utils.SizeConverter;
import majhrs16.lib.utils.Str;

public class _Updater {
	private int width = 0;
    private JSONObject downloadsObj;
    private JSONObject artifactObj;
    private String url;
//  String sha1             = null;
    private int size                = 0;
    private String file_name;

	public String setWidth(String text) {
		if (text.length() > width)
			width = text.length(); 

		return text;
	}

	public void show(ProgressBar PB, String text) {
		System.out.print(Str.ljust(setWidth(PB.show() + " " + text), width, " "));
	}

	public boolean updateData() {
		Data D = Main.D;
        String filePath = Data.join(D.MC, "versions", D.Version, D.Version + ".json");
        String content = "";

        ProgressBar total = new ProgressBar(5);
        show(total, " [ .... ] Leyendo JSON");

        try {
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content += line.trim();
            }

            bufferedReader.close();

        } catch (IOException e) {
            System.out.println("FAIL.");
        	System.out.println("\t" + e.getMessage());
            return false;
        }

        total.set(1);
        show(total, "[  OK  ] Leyendo JSON");

        List<String> CP = new ArrayList<String>();
        JSONObject json;
        try{ 
        	json = new JSONObject(content);

            show(total, "[ .... ] Procesando JSON");

        	JSONObject assetIndex = json.getJSONObject("assetIndex");
            D.MainClass           = json.getString("mainClass");
        	D.AssetsIndex         = assetIndex.getString("id");

        	size      = assetIndex.getInt("size");
        	url       = assetIndex.getString("url");
        	file_name = FilePath.getFileNameFromURL(url);

        	try {
				download(D, Data.join(D.MC, "assets", "indexes"), size);

        	} catch (IOException e1) {
	            show(total, "[ FAIL ] Procesando JSON");
				return false;
			}

        	total.set(2);
            show(total, "[ .... ] Procesando JSON");

        	JSONObject client     = json.getJSONObject("downloads").getJSONObject("client");
            
           	size      = client.getInt("size");
        	url       = client.getString("url");
        	file_name = D.Version + ".jar";

        	try {
				download(D, Data.join(D.MC, "versions", D.Version), size);

        	} catch (IOException e1) {
	            show(total, "[ FAIL ] Procesando JSON");
				return false;
			}
        	
            total.set(3);
            show(total, "[ .... ] Procesando JSON");

            JSONArray librariesArray = json.getJSONArray("libraries");

            ProgressBar libs = new ProgressBar(librariesArray.length());
            for (int i = 0; i < librariesArray.length(); i++) {
                JSONObject libraryObj = librariesArray.getJSONObject(i);
                String name = libraryObj.getString("name");
                try {
	                downloadsObj  = libraryObj.getJSONObject("downloads");
	                artifactObj   = downloadsObj.getJSONObject("artifact");
	                url           = artifactObj.getString("url");
//	                sha1          = artifactObj.getString("sha1");
	                size          = artifactObj.getInt("size");
	                file_name     = FilePath.getFileNameFromURL(url);

                } catch (JSONException e) {
                	file_name = "*";
                }

                String[] _path = name.split(":");
                _path[0] = _path[0].replace("\\.", ":");
                String path = D._format(D, Data.join("{Data.Lib}", Data.join(_path)));
                String FN = Data.join(path, file_name);


                libs.set(i);
                show(libs, "[ .... ] Procesando librerias");

                try {
                	download(D, path, i);

                } catch (IOException e) {
                	continue;
                }
                
                CP.add(FN);
            }
            
            show(libs, "[  OK  ] Procesando librerias");

            total.set(5);
            show(total, "[  OK  ] Procesando JSON");

        } catch (JSONException e) {
        	System.out.println(Str.ljust(setWidth("[ FAIL ] Procesando JSON"), width, " "));
        	System.out.println("\t" + e.getMessage());
        	return false;
        }

        CP.add(Data.join("{Data.MC}", "versions", "{Data.Version}", "{Data.Version}.jar"));
        D.CLASSPATH = CP.toArray(new String[0]);
        return true;
    }
	
	public void download(Data D, String path, int i) throws IOException {
		FilePath.makedirs(path);

	    Path folderPath = Paths.get(Data.join(path, file_name));
        if (!Files.exists(folderPath) && file_name != "*") {
        	ProgressBar download = new ProgressBar(size);
        	show(download, "[ .... ] Descargando '" + file_name + "'");

        	Downloader downloader = new Downloader();
        	String _size = SizeConverter.getSize(size, null, false);
            downloader.addDownloadListener(bytesDownloaded -> {
            	download.set(bytesDownloaded);
                show(download, String.format("[ .... ] Descargando '%s', %s / %s", file_name, SizeConverter.getSize(bytesDownloaded, null, false), _size));
            });

            try {
                downloader.downloadFile(url, Data.join(path, file_name));
               	System.out.print(Str.ljust(setWidth(download.show() + " [  OK  ] Descargando '" + file_name + "'"), width, " "));

            } catch (IOException e) {
            	System.out.print(Str.ljust(setWidth(download.show() + " [ FAIL ] Descargando '" + file_name + "'"), width, " "));
            }
        }
	}
}