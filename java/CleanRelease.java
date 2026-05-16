import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CleanRelease {
    static final Path RELEASE = Paths.get("f:/trea-2dduzhan-xiangsu/PixelBattle-Release");

    public static void main(String[] args) throws Exception {
        Path old = RELEASE.resolve("2dxiangsu-duizhan");
        if (Files.exists(old)) {
            deleteDir(old);
            System.out.println("Deleted: 2dxiangsu-duizhan");
        }
        System.out.println("=== Final Release contents ===");
        Files.walk(RELEASE).filter(Files::isRegularFile).forEach(p -> {
            try {
                System.out.println("  " + RELEASE.relativize(p) + " (" + Files.size(p) / 1024 + " KB)");
            } catch (Exception e) {}
        });
    }

    static void deleteDir(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    try { Files.delete(f); } catch (Exception e) {}
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult postVisitDirectory(Path d, Exception e) {
                    try { Files.delete(d); } catch (Exception ex) {}
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }
}
