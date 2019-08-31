package evidence.render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

public class ScaledImage {

	/**
	 * Константа Q, приблизительно равная 1/255, но немного превышающая её.
	 * С ней можно конвертировать цвета из флоатов в 0xFF и т. п.
	 */
	public static final float Q = 0x1.010102p-8f;

	public final BufferedImage base;
	public final Graphics2D g;
	public final int scale;
	public final WritableRaster r;
	public final int[] data;
	public final int format;
	private final int height;
	private final int width;

	private float[] colors = {1, 1, 1, 1};
	private Filter filter;

	public ScaledImage(File file, int scale) throws IOException {
		this.scale = scale;
		BufferedImage img = ImageIO.read(file);
		this.base = img.getType() == TYPE_4BYTE_ABGR ? img : Util.convertColorspace(img, TYPE_4BYTE_ABGR);
		this.g = base.createGraphics();
		this.r = base.getRaster();
		this.height = r.getHeight();
		this.width = r.getWidth();
		format = r.getNumBands();
		this.data = new int[this.format * height * width];
		r.getPixels(0, 0, width, height, data);
	}

	public void setColor(float red, float green, float blue, float alpha) {
		colors[0] = red;
		colors[1] = green;
		colors[2] = blue;
		colors[3] = alpha;
	}
	public void permuteColor(float red, float green, float blue, float alpha) {
		colors[0] *= red;
		colors[1] *= green;
		colors[2] *= blue;
		colors[3] *= alpha;
	}
	public void setColor(Color color, float alpha) {
		setColor(color.r * Q, color.g * Q, color.b * Q, alpha);
	}

	public void filter(Filter filter) {
		this.filter = filter;
	}

	public int getHeight() {
		return height / scale;
	}

	public int getWidth() {
		return width / scale;
	}

	public void rect(float x1, float y1, float x2, float y2, int color) {


		int xmin = (int) (x1 * scale);
		int ymin = (int) (y1 * scale);
		int xmax = (int) (x2 * scale);
		int ymax = (int) (y2 * scale);

		int sizeX = xmax - xmin;
		int sizeY = ymax - ymin;

		int[] colorData = new int[4];
		for (int channel = 0; channel < 4; channel++) {
			colorData[channel] = color >> 8 * channel & 0xFF;
		}

		for (int x = xmin, a = 0; x < xmax; x++, a++) {
			for (int y = ymin, b = 0; y < ymax; y++, b++) {
				int offsetImg = (y * width + x) * format;

				double px = (double) a / sizeX;
				double py = (double) b / sizeY;

				float alphaAbove = 1;
				boolean alphaGot = false;
				for (int channel = 3; channel >= 0; channel--) {
					float c = colorData[channel] * Q * colors[channel];
					float colorBelow = data[offsetImg + channel] * Q;
					if (filter != null) c = filter.filter(px, py, c, channel, colorBelow);
					float colorAbove = c * alphaAbove;
					if (!alphaGot) {
						alphaAbove = c;
						alphaGot = true;
					}
					data[offsetImg + channel] = (int) ((colorAbove + colorBelow * (1 - alphaAbove)) * 255);

				}
			}
		}

	}

	public void pasteFull(BufferedImage texture, float x1, float y1, float x2, float y2) {
		draw(texture, 0, 0, 1, 1, x1, y1, x2, y2);
	}

	public void drawMCFormat(BufferedImage texture, int tx1, int ty1, int tx2, int ty2, float x1, float y1, float x2, float y2) {
		double p = 0x1P-8;
		draw(texture, tx1 * p, ty1 * p, tx2 * p, ty2 * p, x1, y1, x2, y2);
	}

	/**
	 * Рейтресинг текстурки на изображение
	 * @param img Текстутра (bufferedimage)
	 * @param u1 левая координата X в процентах от общей ширины
	 * @param v1 верхняя координата Y в процентах от общей высоты
	 * @param u2 правая координата X в процентах от общей ширины
	 * @param v2 нижняя координата Y в процентах от общей высоты
	 * @param x1 координата X на изображении, на которой будет левый верхний угол текстурки
	 * @param y1 координата Y на изображении, на которой будет левый верхний угол текстурки
	 * @param x2 координата X на изображении, на которой будет правый нижний угол текстурки
	 * @param y2 координата Y на изображении, на которой будет правый нижний угол текстурки
	 */
	public void draw(BufferedImage img, double u1, double v1, double u2, double v2, float x1, float y1, float x2, float y2) {

		Raster t = img.getRaster();
		int ff = t.getNumBands();

		int w = t.getWidth();
		int h = t.getHeight();

		double tx = (int) (u1 * w);
		double ty = (int) (v1 * h);
		double tw = (int) (u2 * w) - tx;
		double th = (int) (v2 * h) - ty;
		int[] texture = new int[(int) (tw * th * ff)];
		t.getPixels((int) tx, (int) ty, (int) tw, (int) th, texture);

		double xmin = x1 * scale;
		double ymin = y1 * scale;
		double xmax = x2 * scale;
		double ymax = y2 * scale;

		double sizeX = xmax - xmin;
		double sizeY = ymax - ymin;


		for (double x = xmin, a = 0; x < xmax; x++, a++) {
			pixelIterating:
			for (double y = ymin, b = 0; y < ymax; y++, b++) {
				int offsetImg = (int) ((y * width + x) * format);
				double px = a / sizeX;
				double py = b / sizeY;
				double rayX = (float) (px * tw);
				double rayY = (float) (py * th);
				int offsetTxt = (int) (((int) rayY * tw + (int) rayX) * ff);

				if (ff < 4) {
					for (int channel = 0; channel < ff || channel < format; channel++) {
						data[offsetImg + channel] = texture[offsetTxt + channel];
					}
					return;
				}
				float alphaAbove = 1;
				boolean alphaGot = false;
				// channel: 3 = alpha, 2 = blue, 1 = green, 0 = red
				for (int channel = 3; channel >= 0; channel--) {
					float color = texture[offsetTxt + channel] * Q * colors[channel];
					float colorBelow = data[offsetImg + channel] * Q;
					if (filter != null) color = filter.filter(px, py, color, channel, colorBelow);
					float colorAbove = color * alphaAbove;
					if (!alphaGot) {
						alphaAbove = color;
						if (alphaAbove == 0 && filter == null) continue pixelIterating;
						alphaGot = true;
					}
					data[offsetImg + channel] = (int) ((colorAbove + colorBelow * (1 - alphaAbove)) * 255);

				}
			}
		}


	}

	public void flush() {
		r.setPixels(0, 0, width, height, data);
	}

	public void save(File file) throws IOException {
		save(file, this.format);
	}
	public void save(File file, int format) throws IOException {
		flush();
		BufferedImage img;
		if (format != this.format) img = Util.convertColorspace(base, format);
		else img = base;
		ImageIO.write(img, "PNG", file);
	}


}
