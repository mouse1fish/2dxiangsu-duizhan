import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;

public class RebuildRelease {
    static final Path SRC = Paths.get("f:/trea-2dduzhan-xiangsu/java/src");
    static final Path DIST = Paths.get("f:/trea-2dduzhan-xiangsu/java/dist3/PixelBattle");
    static final Path RELEASE = Paths.get("f:/trea-2dduzhan-xiangsu/PixelBattle-Release");

    public static void main(String[] args) throws Exception {
        if (Files.exists(RELEASE)) {
            System.out.println("Deleting old release...");
            forceDeleteDir(RELEASE);
        }
        Files.createDirectories(RELEASE);

        Files.createDirectories(RELEASE.resolve("src"));
        copyDir(SRC, RELEASE.resolve("src"));

        Files.copy(DIST.resolve("PixelBattle.jar"), RELEASE.resolve("PixelBattle.jar"));
        Files.copy(DIST.resolve("PixelBattle.exe"), RELEASE.resolve("PixelBattle.exe"));
        Files.copy(DIST.resolve("PixelBattle.bat"), RELEASE.resolve("PixelBattle.bat"));

        System.out.println("=== Done! Release contents ===");
        Files.walk(RELEASE).filter(Files::isRegularFile).forEach(p -> {
            try {
                System.out.println("  " + RELEASE.relativize(p) + " (" + Files.size(p) / 1024 + " KB)");
            } catch (Exception e) {}
        });
    }

    static void copyDir(Path src, Path dst) throws Exception {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes a) {
                try { Files.createDirectories(dst.resolve(src.relativize(dir).toString())); } catch (Exception e) {}
                return FileVisitResult.CONTINUE;
            }
            public FileVisitResult visitFile(Path file, BasicFileAttributes a) {
                try { Files.copy(file, dst.resolve(src.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING); } catch (Exception e) {}
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static void forceDeleteDir(Path dir) throws Exception {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
                f.toFile().setWritable(true);
                Files.delete(f);
                return FileVisitResult.CONTINUE;
            }
            public FileVisitResult visitFileFailed(Path f, IOException e) throws IOException {
                f.toFile().setWritable(true);
                Files.delete(f);
                return FileVisitResult.CONTINUE;
            }
            public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
