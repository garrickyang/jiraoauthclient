package httpclient;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertiesClient {
    public static final String CONSUMER_KEY = "consumer_key";
    public static final String PRIVATE_KEY = "private_key";
    public static final String REQUEST_TOKEN = "request_token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String SECRET = "secret";
    public static final String JIRA_HOME = "jira_home";


    private final static Map<String, String> DEFAULT_PROPERTY_VALUES = new HashMap<String, String>();
    static {
    	DEFAULT_PROPERTY_VALUES.put(JIRA_HOME, "https://jira-gcs-hz.atlassian.net");
    	DEFAULT_PROPERTY_VALUES.put(CONSUMER_KEY, "OauthKey");
    	DEFAULT_PROPERTY_VALUES.put(PRIVATE_KEY, "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKl2JcD3GoEe3uHackeeciaddvReMHC41kiUQLrJZxsAAsjFTncljZKSACtujkJhVEBc3aUYyTz0noP0Vpgy8DneAXSDc6EXMunFH9Dzr9ne61x+ZimagkHfXBLECzbb+Zu1SokDYDxjb6Oit6T08hCZrigq1DA1VHFxn25cdiGDAgMBAAECgYASszb3VE1Yck+mHLT/sjxmhnnZ/Yra5Yq/95wtAIygLiAgh6VhAIPe6L2cDVclfBgQAi9zSIjscRIM/amQog5gJk4c2wqFdLv6NhnxBpkdhf3s1ByS30JQHg8e1EeS9DoMg/L1DEWIlQec51AEszYHLcHSgxIcP2gXh+FgUZUwAQJBAN5qK7Bg/ymVAj+PnKP27XX175USkchFy2GVKsvY25sdFT13je+AAVMOYgwplIShgVHLzWBmi73YqmMZxftTDGMCQQDDDPpXV7ZYC0BeVi6g68BDvUE6jR2h6TpxWAHm+piR9WtaZDhF6I3yWvUqfKXYtrSG/8vkUVkV6fl2/E44utBhAkAKBK5DG5tivBuF0Wo02IKJtbI8/MEkTECE/LsYw4Pg0MaMJj52c0WcACHaemT+NGgmzw9JMFVLD99c52RLlcoRAkBOwMWvUFXqVJinvkpTZPybHSXiGyoUvpN/QhZ6iUHi5OF0fLSP3Wa6rOkCP5PC3Xoka9GKHSJIC9FSrmpy01LhAkBHKtkElHQA6G1viODWyQzKLhDGZp9YiSmZlW2DmIufgWqpTGHJxTRPl87N8hHnRoqeJ6g/wUGyWIbKSTqymA2Y");
    }

    private final String fileUrl;
    private final String propFileName = "config.properties";

    public PropertiesClient() throws Exception {
        fileUrl = "./" + propFileName;
    }

    public Map<String, String> getPropertiesOrDefaults() {
        try {
            Map<String, String> map = toMap(tryGetProperties());
            DEFAULT_PROPERTY_VALUES.forEach((key, value)->{
            	if(map.get(key)==null) {
            		map.put(key, value);
            	}
            });
            return map;
        } catch (FileNotFoundException e) {
            tryCreateDefaultFile();
            return new HashMap<>(DEFAULT_PROPERTY_VALUES);
        } catch (IOException e) {
            return new HashMap<>(DEFAULT_PROPERTY_VALUES);
        }
    }

    private Map<String, String> toMap(Properties properties) {
        return properties.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(o -> o.getKey().toString(), t -> t.getValue().toString()));
    }

    private Properties toProperties(Map<String, String> propertiesMap) {
        Properties properties = new Properties();
        propertiesMap.entrySet()
                .stream()
                .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
        return properties;
    }

    private Properties tryGetProperties() throws IOException {
        InputStream inputStream = new FileInputStream(new File(fileUrl));
        Properties prop = new Properties();
        prop.load(inputStream);
        return prop;
    }

    public void savePropertiesToFile(Map<String, String> properties) {
        OutputStream outputStream = null;
        File file = new File(fileUrl);

        try {
            outputStream = new FileOutputStream(file);
            Properties p = toProperties(properties);
            p.store(outputStream, null);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            closeQuietly(outputStream);
        }
    }

    public void tryCreateDefaultFile() {
        System.out.println("Creating default properties file: " + propFileName);
        tryCreateFile().ifPresent(file -> savePropertiesToFile(DEFAULT_PROPERTY_VALUES));
    }

    private Optional<File> tryCreateFile() {
        try {
            File file = new File(fileUrl);
            file.createNewFile();
            return Optional.of(file);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // ignored
        }
    }
}
