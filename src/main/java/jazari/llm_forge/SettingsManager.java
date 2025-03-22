package jazari.llm_forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SettingsManager {
    
    private static final String SETTINGS_DIR = System.getProperty("user.home") + File.separator + ".jazari_chat_forge";
    private static final String SETTINGS_FILE = "settings.properties";
    private static final String API_KEYS_FILE = "api_keys.enc";
    private static final String SALT_FILE = "salt.bin";
    
    private Properties settings;
    private Map<String, String> apiKeys;
    private byte[] salt;
    private final String masterPassword;
    
    public SettingsManager() {
        this("JazariChatForge"); // Default master password
    }
    
    public SettingsManager(String masterPassword) {
        this.masterPassword = masterPassword;
        this.settings = new Properties();
        this.apiKeys = new HashMap<>();
        initialize();
    }
    
    private void initialize() {
        try {
            // Create settings directory if it doesn't exist
            File settingsDir = new File(SETTINGS_DIR);
            if (!settingsDir.exists()) {
                settingsDir.mkdirs();
            }
            
            // Load settings file if it exists
            File settingsFile = new File(SETTINGS_DIR + File.separator + SETTINGS_FILE);
            if (settingsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(settingsFile)) {
                    settings.load(fis);
                }
            }
            
            // Check if salt exists, create if not
            File saltFile = new File(SETTINGS_DIR + File.separator + SALT_FILE);
            if (saltFile.exists()) {
                salt = Files.readAllBytes(saltFile.toPath());
            } else {
                salt = generateSalt();
                Files.write(saltFile.toPath(), salt);
            }
            
            // Load API keys if they exist
            File apiKeysFile = new File(SETTINGS_DIR + File.separator + API_KEYS_FILE);
            if (apiKeysFile.exists()) {
                loadEncryptedApiKeys();
            }
            
        } catch (Exception e) {
            System.err.println("Error initializing settings: " + e.getMessage());
            // Create empty settings and API keys in case of error
            settings = new Properties();
            apiKeys = new HashMap<>();
        }
    }
    
    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    
    public void setSetting(String key, String value) {
        settings.setProperty(key, value);
        saveSettings();
    }
    
    public String getSetting(String key, String defaultValue) {
        return settings.getProperty(key, defaultValue);
    }
    
    public void setApiKey(String provider, String apiKey) {
        apiKeys.put(provider, apiKey);
        saveEncryptedApiKeys();
    }
    
    public String getApiKey(String provider) {
        return apiKeys.getOrDefault(provider, "");
    }
    
    private void saveSettings() {
        try {
            File settingsFile = new File(SETTINGS_DIR + File.separator + SETTINGS_FILE);
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                settings.store(fos, "JazariChatForge Settings");
            }
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    private void saveEncryptedApiKeys() {
        try {
            // Create string representation of API keys
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : apiKeys.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            String apiKeysStr = sb.toString();
            
            // Encrypt API keys
            byte[] encryptedData = encrypt(apiKeysStr.getBytes(StandardCharsets.UTF_8));
            
            // Save to file
            Path path = Paths.get(SETTINGS_DIR + File.separator + API_KEYS_FILE);
            Files.write(path, encryptedData);
            
        } catch (Exception e) {
            System.err.println("Error saving API keys: " + e.getMessage());
        }
    }
    
    private void loadEncryptedApiKeys() {
        try {
            // Read encrypted data
            Path path = Paths.get(SETTINGS_DIR + File.separator + API_KEYS_FILE);
            byte[] encryptedData = Files.readAllBytes(path);
            
            // Decrypt data
            byte[] decryptedData = decrypt(encryptedData);
            String apiKeysStr = new String(decryptedData, StandardCharsets.UTF_8);
            
            // Parse API keys
            apiKeys = new HashMap<>();
            String[] lines = apiKeysStr.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                int separatorIndex = line.indexOf('=');
                if (separatorIndex > 0) {
                    String provider = line.substring(0, separatorIndex);
                    String apiKey = line.substring(separatorIndex + 1);
                    apiKeys.put(provider, apiKey);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading API keys: " + e.getMessage());
            apiKeys = new HashMap<>();
        }
    }
    
    private byte[] encrypt(byte[] data) throws Exception {
        // Derive key from password
        SecretKey key = deriveKey(masterPassword);
        
        // Initialize cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
        
        // Encrypt
        byte[] encryptedData = cipher.doFinal(data);
        
        // Prepend IV to encrypted data
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
        
        return combined;
    }
    
    private byte[] decrypt(byte[] encryptedData) throws Exception {
        // Derive key from password
        SecretKey key = deriveKey(masterPassword);
        
        // Extract IV from the beginning of the data
        byte[] iv = new byte[16];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);
        
        // Extract actual encrypted data
        byte[] actualData = new byte[encryptedData.length - iv.length];
        System.arraycopy(encryptedData, iv.length, actualData, 0, actualData.length);
        
        // Initialize cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
        
        // Decrypt
        return cipher.doFinal(actualData);
    }
    
    private SecretKey deriveKey(String password) throws Exception {
        int iterations = 10000;
        int keyLength = 256;
        
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    public void clearAllSettings() {
        settings = new Properties();
        apiKeys = new HashMap<>();
        saveSettings();
        saveEncryptedApiKeys();
    }
    
    public void clearApiKeys() {
        apiKeys = new HashMap<>();
        saveEncryptedApiKeys();
    }
    
    public Map<String, String> getSettings() {
        Map<String, String> result = new HashMap<>();
        for (String key : settings.stringPropertyNames()) {
            result.put(key, settings.getProperty(key));
        }
        return result;
    }
    
    public boolean hasSettingsFile() {
        File settingsFile = new File(SETTINGS_DIR + File.separator + SETTINGS_FILE);
        return settingsFile.exists();
    }
    
    public String hashApiKey(String apiKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            return apiKey.substring(0, 3) + "...";
        }
    }
}