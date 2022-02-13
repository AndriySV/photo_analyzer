import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

public class Analyzer {

//    static String pathStr = "C:\\Docker\\test.txt";
//    static String pathStr = "C:\\IdeaProjects\\Dev";
    static String pathStr = "D:\\Фото Відео";

    public static void main(String[] args) throws IOException {
        Map<String, List<Path>> filesByYear = new HashMap<>();

        try (Stream<Path> paths = Files.walk(Paths.get(pathStr))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String year = "2020";
                        try {
                            String photoTakenDate = getPhotoTakenDate(path);
                            if (photoTakenDate != null && !photoTakenDate.isEmpty()){
                                try {
                                    year = photoTakenDate.substring(0, 4);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (filesByYear.get(year) != null) {
                                List<Path> list = filesByYear.get(year);
                                list.add(path);
                            } else {
                                ArrayList<Path> list = new ArrayList<>();
                                list.add(path);
                                filesByYear.put(year, list);
                            }

                            System.out.println(year);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ImageProcessingException e) {
                            e.printStackTrace();
                        }
                    });
        }

        makeCopy(filesByYear);
        System.out.println();
    }

    private static void makeCopy(Map<String, List<Path>> filesByYear) {
String baseDir = "C:\\photo\\";

        for (Map.Entry<String, List<Path>> entry : filesByYear.entrySet()) {
            File dir = new File(baseDir + entry.getKey());
            dir.mkdir();

            List<Path> paths = entry.getValue();
            paths.forEach(path -> {
                try {
                    FileUtils.copyFileToDirectory(path.toFile(), dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }
    }

    private static String getPhotoTakenDate(Path path) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
        String year = "";
        ExifThumbnailDirectory firstDirectoryOfType = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
        if (firstDirectoryOfType != null) {
            for (Tag tag : firstDirectoryOfType.getTags()) {
                if ("Date/Time".equals(tag.getTagName())) {
                    year =  tag.getDescription();
                }
            }
        }
        if (!year.isEmpty()) {
            return year;
        }
        ExifSubIFDDirectory firstDirectoryOfType1 = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (firstDirectoryOfType1 != null) {
            for (Tag tag : firstDirectoryOfType1.getTags()) {
                if ("Date/Time Digitized".equals(tag.getTagName())) {
                    year =  tag.getDescription();
                }
            }
        }

        return year;
    }

}