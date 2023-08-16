package majhrs16.dl;

import majhrs16.lib.utils.files.Downloader;
import majhrs16.lib.utils.files.FilePath;
import majhrs16.lib.utils.InfOS;
import majhrs16.lib.utils.SizeConverter;
import majhrs16.lib.shell.ProgressBar;
import majhrs16.lib.utils.Str;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.io.FileReader;
import java.util.List;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Updater {
	private String url;
	private String sha1;
	private String file_name;
	private JSONObject artifactObj;
	private JSONObject downloadsObj;

	private List<String> CP = new ArrayList<>();
	private int width       = 0;
	private int size        = 0;

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
		String filePath = FilePath.join(D.MC, "versions", D.Version, D.Version + ".json");

		ProgressBar total = new ProgressBar(5);

		JSONObject json;
		try {
			total.set(0); show(total, " [ .... ] Leyendo JSON");
			json = loadJsonFile(filePath);
			total.set(1); show(total, "[  OK  ] Leyendo JSON");
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			return false;
		}

		total.set(2); show(total, "[ .... ] Procesando JSON");
			try {
				D.MainClass = json.getString("mainClass");

				processAssetIndex(json.getJSONObject("assetIndex"));

			} catch (JSONException e) {
				total.set(2); show(total, "[ FAIL ] Procesando JSON\n");
				e.printStackTrace();
				return false;
			}

		total.set(3); show(total, "[ .... ] Procesando JSON");
			try {
				processClientDownload(json.getJSONObject("downloads").getJSONObject("client"));

			} catch (JSONException e) {
				total.set(2); show(total, "[ FAIL ] Procesando JSON");
			}

		total.set(4); show(total, "[ .... ] Procesando JSON");
			try {
				processLibraries(json.getJSONArray("libraries"), D);

			} catch (JSONException e) {
				total.set(2); show(total, "[ FAIL ] Procesando JSON\n");
				e.printStackTrace();
				return false;
			}

		CP.add(FilePath.join("{Data.MC}", "versions", "{Data.Version}", "{Data.Version}.jar"));
		D.CLASSPATH = CP.toArray(new String[0]);

		total.set(5); show(total, "[  OK  ] Procesando JSON");

		return true;
	}

	private JSONObject loadJsonFile(String filePath) throws IOException, JSONException {
		String content = "";
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content += line.trim();
			}
		}
		return new JSONObject(content);
	}

	private void processAssetIndex(JSONObject assetIndex) throws JSONException {
		Data D = Main.D;
		D.AssetsIndex = assetIndex.getString("id");

		size      = assetIndex.getInt("size");
		url       = assetIndex.getString("url");
		sha1      = assetIndex.getString("sha1");
		file_name = FilePath.getFileNameFromURL(url);

		try {
			downloadLibrary(D, FilePath.join(D.MC, "assets", "indexes"), url, sha1, size, file_name);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processClientDownload(JSONObject client) throws JSONException {
		Data D = Main.D;

		size      = client.getInt("size");
		sha1      = client.getString("sha1");
		url       = client.getString("url");
		file_name = D.Version + ".jar";

		try {
			downloadLibrary(D, FilePath.join(D.MC, "versions", D.Version), url, sha1, size, file_name);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void processLibraries(JSONArray librariesArray, Data D) throws JSONException {
		ProgressBar libs = new ProgressBar(librariesArray.length());
		for (int i = 0; i < librariesArray.length(); i++) {
			JSONObject libraryObj = librariesArray.getJSONObject(i);
			String name           = libraryObj.getString("name");
			String[] _path        = name.split(":");
			_path[0]              = _path[0].replace("\\.", ":");
			String path           = D._format(D, FilePath.join("{Data.Lib}", FilePath.join(_path)));

			try {
				downloadsObj = libraryObj.getJSONObject("downloads");
				artifactObj  = downloadsObj.getJSONObject("artifact");
				url          = artifactObj.getString("url");
				sha1         = artifactObj.getString("sha1");
				size         = artifactObj.getInt("size");
				file_name    = FilePath.getFileNameFromURL(url);

			} catch (JSONException e) {
				try {
					downloadsObj = libraryObj.getJSONObject("downloads");
					JSONObject classifiersObj = downloadsObj.getJSONObject("classifiers");
					JSONObject nativesObj = classifiersObj.getJSONObject(
						libraryObj.getJSONObject("natives").getString(InfOS.getType().replace("mac", "osx"))
					);
					url          = nativesObj.getString("url");
					sha1         = nativesObj.getString("sha1");
					size         = nativesObj.getInt("size");
					file_name    = FilePath.getFileNameFromURL(url);

					libs.set(i + 1);
					show(libs, "[ .... ] Procesando librerias nativas");

					downloadLibrary(D, path, url, sha1, size, file_name);

					JarExtractor.extract(FilePath.join(path, file_name), D._format(D, D.Natives));

				} catch (JSONException | IOException e2) {
					e2.printStackTrace();
				}

				continue;
			}

			libs.set(i + 1);
			show(libs, "[ .... ] Procesando librerias");

			try {
				downloadLibrary(D, path, url, sha1, size, file_name);

			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			CP.add(FilePath.join(path, file_name));
		}

		show(libs, "[  OK  ] Procesando librerias");
	}

	private void downloadLibrary(Data D, String path, String url, String sha1, int size, String fileName) throws IOException {
		if (fileName.equals("*")) {
			// Manejar caso especial
			return;
		}

		FilePath.makedirs(path);

		File file = new File(path, fileName);
		if (file.exists()) {
			try {
				if (HashCalculator.sha1(file.getAbsolutePath()).equals(sha1))
					return;

			} catch (NoSuchAlgorithmException | IOException e) {
				e.printStackTrace();
				return;
			}
		}

		ProgressBar download = new ProgressBar(size);
		show(download, "[ .... ] Descargando '" + fileName + "'");

		Downloader downloader = new Downloader();
		String _size = SizeConverter.getSize(size, null, false);
		downloader.addDownloadListener(bytesDownloaded -> {
			download.set(bytesDownloaded);
			show(download, String.format("[ .... ] Descargando '%s', %s / %s", fileName, SizeConverter.getSize(bytesDownloaded, null, false), _size));
		});

		try {
			downloader.downloadFile(url, FilePath.join(path, fileName));
			show(download, "[  OK  ] Descargando '" + fileName + "'");

		} catch (IOException e) {
			show(download, "[ FAIL ] Descargando '" + fileName + "'\n");
			e.printStackTrace();
		}
	}
}