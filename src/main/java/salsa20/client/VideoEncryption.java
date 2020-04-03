package salsa20.client;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.*;
import salsa20.salsa20.Salsa20;
import salsa20.salsa20.Utils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class VideoEncryption {

    private static VideoClient client = new VideoClient("Cipher Demonstration", 0, 0);

    private static final int[] k = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    private static final int[] n = {101, 102, 103, 104, 105, 106, 107, 108};


    private static final String inputImagePath = "src/test/resources/imageInput";
    private static final String outputImagePath = "src/test/resources/image";
    private static final File outputImage = new File(outputImagePath + ".bmp");
    private static final String outputEncryptedImageImagePath = "src/test/resources/encryptedImage";
    private static final File outputEncryptedImage = new File(outputImagePath + ".bmp");
    private static final String extension = "bmp";
    private static final int HEADER_LENGTH = 54;

    private static final Salsa20 salsa20 = new Salsa20();

    private static opencv_core.IplImage grabbedImage = null;
    private static Frame frame = null;
    private static final OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

    public static void main(String[] args) throws IOException {
        client.setVisible(true);
        imageEncryption(client);
    }

    private static void imageEncryption(VideoClient client) throws IOException {
        cryptImage(client, inputImagePath);
    }

    private static void cryptAndSaveImage(VideoClient client) throws IOException {
        if (grabbedImage != null) {
            ImageIO.write(Utils.IplImageToBufferedImage(grabbedImage), extension, outputImage);
            ImageIO.write(Utils.IplImageToBufferedImage(grabbedImage), extension, outputEncryptedImage);

            cryptImage(client, outputImagePath);
        }
    }

    private static void cryptImage(VideoClient cliente, String inputImagePath) throws IOException {
        int[] message = Utils.getImageArray(inputImagePath, extension);
        int[] crypt = salsa20.salsa20Encryption(k, n, message, 0);
        int[] decrypt = salsa20.salsa20Encryption(k, n, crypt, 0);

        for(int i = 0; i < HEADER_LENGTH; i++)
            crypt[i] = message[i];

        byte[] byteImage = Utils.intToByte(decrypt);
        Utils.arrayToImage(byteImage, outputImagePath, extension);
        byteImage = Utils.intToByte(crypt);
        Utils.arrayToImage(byteImage, outputEncryptedImageImagePath, extension);

        cliente.updateImages(outputImagePath + "." + extension, outputEncryptedImageImagePath + "." + extension);
    }

    private static void cameraEncryption(VideoClient cliente) throws IOException {
        FrameGrabber grabber = new VideoInputFrameGrabber(0);

        grabber.start();
        while (true) {
            frame = grabber.grab();
            grabbedImage = converter.convert(frame);

            cryptAndSaveImage(cliente);
        }
    }

    private static void videoEncryption(VideoClient cliente) throws IOException {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("src/test/resources/TEST_VIDEO.mp4");

        grabber.start();

        for (int i = 0; i < grabber.getLengthInTime(); i++) {
            frame = grabber.grab();
            grabbedImage = converter.convert(frame);

            cryptAndSaveImage(cliente);
        }

        grabber.stop();
    }
}
