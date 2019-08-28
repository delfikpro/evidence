package evidence;

public class Glint implements Filter {

	/**
	 * Эти данные (полученные через слёзы, кровь, пот и прочие жидкости)
	 * используются майнкрафтом для генерации отблеска зачарованных предметов (glint)
	 *
	 * За одну итерацию используется только 8 из 64 пикселей.
	 */
	public static final int[] DATA = {
			// Расширение для зацикливания
//			111, 87 , 95 , 103, 108, 148, 152, 75 ,

			// Полезные цвета
			104, 49 , 91 , 56 , 79 , 98 , 109, 89 ,
			86 , 113, 107, 132, 130, 160, 108, 106,
			116, 109, 163, 221, 184, 160, 149, 131,
			134, 134, 63 , 24 , 65 , 78 , 94 , 125,
			121, 121, 131, 93 , 126, 143, 144, 166,
			155, 189, 204, 255, 229, 218, 209, 197,
			154, 105, 104, 156, 139, 137, 87 , 80 ,
			111, 87 , 95 , 103, 108, 148, 152, 75 ,

			// Расширение для зацикливания
			104, 49 , 91 , 56 , 79 , 98 , 109, 89
	};

	/**
	 * Баклажановая штука для чаренных вещей
	 */
	public static final int COLOR = 0xff8040cc;

	/**
	 * Магические константы из майнкрафта.
	 * Узор отблеска берётся из DATA дважды и рисуется поверх в зависимости от системного времени.
	 *
	 * OZO - первый слой, цикл занимает 3000 мс и в конце оно поворачивается на -50 градусов
	 * ULU - второй слой, цикл занимает 4873 мс и в конце оно поворачивается на +10 градусов
	 */
	public static final int OZO = (int) (System.currentTimeMillis() % 3000L / 3000.0F * 64);
	public static final int ULU = (int) (System.currentTimeMillis() % 4873L / 4873.0F * 64);

	public static final double ozoSin = Math.sin(-50.0 / 180.0 * Math.PI);
	public static final double ozoCos = Math.cos(-50.0 / 180.0 * Math.PI);
	public static final double uluSin = Math.sin(10.0 / 180.0 * Math.PI);
	public static final double uluCos = Math.cos(10.0 / 180.0 * Math.PI);

	@Override
	public float filter(double x, double y, float color, int channel) {

		// Игнорим прозрачность
		if (channel == 3) return color;

		float ozo;
		float ulu;

		// Слой ozo
		{
			double rx = ozoCos * x - ozoSin * y;
			double ry = ozoSin * x + ozoCos * y;

			double r = rx * 8 - (int) (rx * 8);

			int left = (int) (rx * 8) + OZO;

			if (left < 0) left = 64 - left;
			float color1 = DATA[left] * Image.Q;
			float color2 = DATA[left + 1] * Image.Q;

			ozo = (float) ((color2 - color1) * r + color1);

		}
		// Слой ulu
		{
			double rx = uluCos * x - uluSin * y;
			double ry = uluSin * x + uluCos * y;

			double r = rx * 8 - (int) (rx * 8);

			int left = (int) (rx * 8) + ULU;

			if (left < 0) left = 64 - left;
			float color1 = DATA[left] * Image.Q;
			float color2 = DATA[left + 1] * Image.Q;

			ulu = (float) ((color2 - color1) * r + color1);

		}

		float baklazhan = (COLOR >> 8 * (2 - channel) & 0xFF) * Image.Q;

//		float ozoIter = (1 - ozo) * color + baklazhan * ozo;
//		float uluIter = (1 - ulu) * ozoIter + baklazhan * ulu;
		float v = color * color + baklazhan * ozo;
		float d = v * v + baklazhan * ulu;
		if (v > 1) v = 1;
		return v;


	}

}
