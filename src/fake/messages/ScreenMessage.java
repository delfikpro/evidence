package fake.messages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenMessage implements Message {
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
	private final String time;

	public ScreenMessage(String time) {
		if (time == null) this.time = dateFormat.format(new Date());
		else this.time = time;
	}

	@Override
	public String getText() {
		return "Снимок экрана сохранён как §n" + time + ".png";
	}

}
