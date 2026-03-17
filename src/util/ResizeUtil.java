package util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

public class ResizeUtil {
	private static final int MAX_WIDTH = 1600;
	private static final int MAX_HEIGHT = 1600;

	public static void main(String args[]) throws IOException {

		final File folder = new File(args[0]);
		listFilesForFolder(folder);

	}

	public static void listFilesForFolder(final File folder) throws IOException {

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				// System.out.println(fileEntry.getAbsolutePath());
				// System.out.println(fileEntry.getCanonicalPath());
				// System.out.println(fileEntry.getPath());
				// System.out.println(fileEntry.getName());

				try {

					int orientation = 1;

					BufferedImage originalImage = ImageIO.read(new File(fileEntry.getPath()));

					if (originalImage == null) {
						System.out.println("Not a Image " + fileEntry.getPath());
						continue;
					}
					int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

					int height = originalImage.getHeight();
					int width = originalImage.getWidth();

					if (height > MAX_HEIGHT || width > MAX_WIDTH) {

						BufferedImage resizeImageHintJpg = resizeImageWithHint(originalImage, type, orientation);
						String ext = FilenameUtils.getExtension(fileEntry.getName());
						ImageIO.write(resizeImageHintJpg, ext, new File(fileEntry.getPath()));

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void ResizeFile(final FileItem item, final File fileEntry) throws IOException {

		try {

			BufferedInputStream bufferedIS = new BufferedInputStream(item.getInputStream());
			int orientation = getOrientation(bufferedIS);

			BufferedImage originalImage = ImageIO.read(new File(fileEntry.getPath()));
			if (originalImage == null) {
				System.out.println("Not a Image " + fileEntry.getPath());
				return;
			}
			int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

			int height = originalImage.getHeight();
			int width = originalImage.getWidth();

			if (height > MAX_HEIGHT || width > MAX_WIDTH) {

				originalImage = rotateImageForMobile(originalImage, orientation);

				BufferedImage resizeImageHintJpg = resizeImageWithHint(originalImage, type, orientation);

				String ext = FilenameUtils.getExtension(fileEntry.getName());

				ImageIO.write(resizeImageHintJpg, ext, new File(fileEntry.getPath()));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static BufferedImage rotateImageForMobile(BufferedImage bi, int orientation) throws IOException {
		if (orientation == 6) { // 정위치
			return rotateImage(bi, 90);
		} else if (orientation == 1) { // 왼쪽으로 눞였을때
			return bi;
		} else if (orientation == 3) {// 오른쪽으로 눞였을때
			return rotateImage(bi, 180);
		} else if (orientation == 8) {// 180도
			return rotateImage(bi, 270);
		} else {
			return bi;
		}
	}

	public static BufferedImage rotateImage(BufferedImage orgImage, int radians) {
		BufferedImage newImage;

		if (radians == 90 || radians == 270) {
			newImage = new BufferedImage(orgImage.getHeight(), orgImage.getWidth(), orgImage.getType());
		} else if (radians == 180) {
			newImage = new BufferedImage(orgImage.getWidth(), orgImage.getHeight(), orgImage.getType());
		} else {
			return orgImage;
		}

		Graphics2D graphics = (Graphics2D) newImage.getGraphics();
		graphics.rotate(Math.toRadians(radians), newImage.getWidth() / 2, newImage.getHeight() / 2);
		graphics.translate((newImage.getWidth() - orgImage.getWidth()) / 2, (newImage.getHeight() - orgImage.getHeight()) / 2);
		graphics.drawImage(orgImage, 0, 0, orgImage.getWidth(), orgImage.getHeight(), null);

		return newImage;
	}

	private static BufferedImage resizeImageWithHint(BufferedImage originalImage, int type, int orientation) {

		int height = originalImage.getHeight();
		int width = originalImage.getWidth();

		System.out.println("Resize : " + width + " x " + height + " to " + (width * MAX_HEIGHT) / height + " x " + MAX_HEIGHT + ",orientation=" + orientation);

		BufferedImage resizedImage = new BufferedImage((width * MAX_HEIGHT) / height, MAX_HEIGHT, type);

		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, (width * MAX_HEIGHT) / height, MAX_HEIGHT, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		return resizedImage;
	}

	public static int getOrientation(BufferedInputStream is) {
		int orientation = 1;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(is);
			ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			try {

				orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

			} catch (MetadataException me) {
				System.out.println("Could not get orientation");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orientation;
	}

}
