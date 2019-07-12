package fake;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * Distorts images using transformations.
 * <p>
 * Created by Matthias Braun on 2018-09-05.
 */
public class HandRender {

	public static void main(String... args) throws IOException {
		URL imgUrl = new URL("https://cdn.sstatic.net/Sites/stackoverflow/company/img/logos/so/so-logo.png?v=9c558ec15d8a");
		BufferedImage img = ImageIO.read(imgUrl);
		BufferedImage distorted = distortImg(img);

		File newImgFile = new File("distorted.png");
		System.out.println("Saving to: " + newImgFile);
		ImageIO.write(distorted, "png", newImgFile);

		// Since we started a JavaFX thread in distortImg we have to shut it down. Otherwise the JVM won't exit
		Platform.exit();
	}

	/**
	 * Applies perspective transformations to a copy of this {@code image} and rotates it.
	 * <p>
	 * Since this method starts a JavaFX thread, it's important to call {@link Platform#exit()} at the end of
	 * your application. Otherwise the thread will prevent the JVM from shutting down.
	 *
	 * @param image the image we want to distort
	 * @return the distorted image
	 */
	public static BufferedImage distortImg(BufferedImage image) {
		// Necessary to initialize the JavaFX platform and to avoid "IllegalStateException: Toolkit not initialized"
		new JFXPanel();

		// This array allows us to get the distorted image out of the runLater closure below
		final BufferedImage[] imageContainer = new BufferedImage[1];

		// We use this latch to await the end of the JavaFX thread. Otherwise this method would finish before
		// the thread creates the distorted image
		final CountDownLatch latch = new CountDownLatch(1);

		// To avoid "IllegalStateException: Not on FX application thread" we start a JavaFX thread
		Platform.runLater(() -> {
			int width = image.getWidth();
			int height = image.getHeight();
			Canvas canvas = new Canvas(width, height);
			GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
			ImageView imageView = new ImageView(SwingFXUtils.toFXImage(image, null));

			PerspectiveTransform trans = new PerspectiveTransform();
			trans.setUlx(0);
			trans.setUly(height / 4);
			trans.setUrx(width);
			trans.setUry(0);
			trans.setLrx(width);
			trans.setLry(height);
			trans.setLlx(0);
			trans.setLly(height - height / 2);

			imageView.setEffect(trans);

			imageView.setRotate(2);

			SnapshotParameters params = new SnapshotParameters();
			params.setFill(Color.TRANSPARENT);

			Image newImage = imageView.snapshot(params, null);
			graphicsContext.drawImage(newImage, 0, 0);

			imageContainer[0] = SwingFXUtils.fromFXImage(newImage, image);
			// Work is done, we decrement the latch which we used for awaiting the end of this thread
			latch.countDown();
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return imageContainer[0];
	}
}
