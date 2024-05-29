package cz.cuni.mff.mbohin.productParser.adapters;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileHandler {

    public static String loadJsonFromPath(String relativePath) throws IOException {
        File file = new File(relativePath);
        if (file.exists()) {
            return Files.readString(Paths.get(relativePath));
        }
        throw new IOException("File not found: " + relativePath);
    }

    public static String loadJsonFromPath(String path, String extractPath) throws IOException {
        File extractDir = new File(extractPath);
        if (extractDir.exists()) {
            deleteDirectory(extractDir);
        }
        assert extractDir.mkdirs();

        unzip(path, extractPath);

        List<File> jsonFiles;
        try (Stream<Path> paths = Files.walk(Paths.get(extractPath))) {
            jsonFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(Path::toFile)
                    .toList();
        }

        if (jsonFiles.isEmpty()) {
            throw new IOException("No JSON file found in the extracted directory.");
        }

        String json = Files.readString(jsonFiles.getFirst().toPath());

        deleteDirectory(extractDir);  // Clean up extraction directory

        return json;
    }

    private static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        createDirectory(destDir);

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                processZipEntry(zipFile, entry, destDir);
            }
        }
    }

    private static void createDirectory(File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create the directory: " + directory);
        }
    }

    private static void processZipEntry(ZipFile zipFile, ZipEntry entry, File destDir) throws IOException {
        File entryDestination = new File(destDir, entry.getName());
        if (entry.isDirectory()) {
            createDirectory(entryDestination);
        } else {
            File parentDir = entryDestination.getParentFile();
            createDirectory(parentDir);
            writeFile(zipFile, entry, entryDestination);
        }
    }

    private static void writeFile(ZipFile zipFile, ZipEntry entry, File destination) throws IOException {
        try (InputStream in = zipFile.getInputStream(entry);
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) >= 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        assert directoryToBeDeleted.delete();
    }
}
