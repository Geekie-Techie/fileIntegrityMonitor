import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.HashSet;
import java.util.Set;

public class FileIntegrityMonitor {

    private static final String DIRECTORY_TO_MONITOR = "/home/hibino/Desktop/Java/Monitor/";

    public static String sha256Checksum(String file) throws IOException, NoSuchAlgorithmException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(file))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytesBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }
            byte[] hashedBytes = digest.digest();
            return bytesToHex(hashedBytes);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static void initializeChecksums(File directory, Set<String> checksums) throws IOException, NoSuchAlgorithmException {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                initializeChecksums(file, checksums);
            } else {
                String checksum = sha256Checksum(file.getAbsolutePath());
                checksums.add("File Path : " + file.getAbsolutePath() + " and its checksum : " + checksum);
            }
        }
    }

    private static void monitorDirectory() throws IOException, NoSuchAlgorithmException {
        Set<String> currentChecksums = new HashSet<>();
        Set<String> changedFiles = new HashSet<>();

        initializeChecksums(new File(DIRECTORY_TO_MONITOR), currentChecksums);

        while (true) {
            Set<String> newChecksums = new HashSet<>();
            Set<String> newChangedFiles = new HashSet<>();

            initializeChecksums(new File(DIRECTORY_TO_MONITOR), newChecksums);

            for (String newChecksum : newChecksums) {
                if (!currentChecksums.contains(newChecksum)) {
                    newChangedFiles.add(newChecksum + " (added)");
                }
            }

            for (String currentChecksum : currentChecksums) {
                if (!newChecksums.contains(currentChecksum)) {
                    newChangedFiles.add(currentChecksum + " (deleted)");
                }
            }

            if (!newChangedFiles.isEmpty()) {
                System.out.println("Changes detected:");
                for (String change : newChangedFiles) {
                    System.out.println(change);
                }
            } else {
                System.out.println("No changes detected.");
            }

            currentChecksums = newChecksums;

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        System.out.println("Initializing...");
        System.out.println("Monitoring directory: " + DIRECTORY_TO_MONITOR);

        monitorDirectory();
    }
}
