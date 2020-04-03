package salsa20.client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class VideoClient extends JFrame {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 480;
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 480;

    private JPanel contentPane;
    private JSplitPane splitPane;
    private JPanel paneImage;
    private JPanel paneEncryptedImage;
    private JLabel image;
    private JLabel encryptedImage;

    public VideoClient(String title, int posX, int posY) {
        config(title, posX, posY);
        init();
    }

    private void config(String title, int posX, int posY) {
        setTitle(title);
        setSize(WIDTH, HEIGHT);
        setLocation(posX, posY);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void init() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        paneImage = new JPanel();
        paneEncryptedImage = new JPanel();

        image = new JLabel();
        image.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        paneImage.add(image);

        encryptedImage = new JLabel();
        encryptedImage.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        paneEncryptedImage.add(encryptedImage);

        splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(WIDTH / 2);
        splitPane.setDividerSize(20);
        splitPane.setLeftComponent(paneImage);
        splitPane.setRightComponent(paneEncryptedImage);

        contentPane.add(splitPane);
    }

    public void updateImages(String pathImage, String pathEncryptedImage) throws IOException {
        image.setIcon(new ImageIcon(ImageIO.read(new File(pathImage))));
        encryptedImage.setIcon(new ImageIcon(ImageIO.read(new File(pathEncryptedImage))));
    }
}