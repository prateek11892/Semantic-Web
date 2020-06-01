import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Q3_parta {

	public static void main(String[] args) {

		String wordToSearch = "example";					//Word to be searched
		BufferedWriter bwr = null;
		BufferedReader br = null;

		try {

			URL url = new URL("http://api.conceptnet.io/c/en/" + wordToSearch);		// Providing the url
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();		// creating the http connection
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/ld+json");				// specifying the json ld format
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			FileWriter writer = new FileWriter("A1_MT19070_Q3_"+wordToSearch + ".json");
			bwr = new BufferedWriter(writer);

			String output;
			while ((output = br.readLine()) != null) {
				bwr.write(output);													// writing to the output file
			}

			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				bwr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}