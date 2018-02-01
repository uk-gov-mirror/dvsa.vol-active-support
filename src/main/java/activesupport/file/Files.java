package activesupport.file;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Files {
    /**
     * Deletes Folder with all of its content
     *
     * @param folder path to folder which should be deleted
     */
    public static void deleteFolderAndItsContent(final Path folder) throws IOException {
        java.nio.file.Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(java.nio.file.Files.exists(file)){
                    java.nio.file.Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                if((new File(dir.toString()).exists())){
                    java.nio.file.Files.delete(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static File createDirectory(@NotNull String path) {
        return createFolder(path);
    }

    public static File createFolder(@NotNull String path) {
        File folder = new File(path);

        if (!folder.exists()) {
            folder.mkdir();
        }

        return folder;
    }

    public static void write(@NotNull Path path, @NotNull String contents) throws IOException {
        File file = path.toFile();

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        try(FileOutputStream outputStream = new FileOutputStream(file)){
            byte[] strToBytes = contents.getBytes();
            outputStream.write(strToBytes);
        }
    }
}
