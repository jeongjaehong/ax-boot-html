package util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfUtil {

    public static void main(String[] args) {
        String inputImagePath = "path/to/your/image.jpg";
        String outputPdfPath = "path/to/your/output.pdf";
        String userPassword = "your_user_password";
        String ownerPassword = "your_owner_password";

        try {
            createEncryptedPdf(inputImagePath, outputPdfPath, userPassword, ownerPassword);
            System.out.println("Encrypted PDF created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createEncryptedPdf(String inputImagePath, String outputPdfPath, String userPassword, String ownerPassword) throws IOException {
        PDDocument document = new PDDocument();

        try {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            BufferedImage image = ImageIO.read(new File(inputImagePath));

            float scale = 1f; // Adjust the scale as needed
            contentStream.drawImage(LosslessFactory.createFromImage(document, image), 0, 0, image.getWidth() * scale, image.getHeight() * scale);
            contentStream.close();

            // Set document information
            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle("Encrypted PDF");
            document.setDocumentInformation(info);

            // Set access permissions
            AccessPermission accessPermission = new AccessPermission();
            accessPermission.setCanPrint(true);

            // Set encryption options
            StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(ownerPassword, userPassword, accessPermission);
            protectionPolicy.setEncryptionKeyLength(128);
            protectionPolicy.setPermissions(accessPermission);

            // Encrypt the PDF
            document.protect(protectionPolicy);

            // Save the document
            document.save(outputPdfPath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
