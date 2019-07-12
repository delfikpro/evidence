package fake;

import java.awt.*;
import java.awt.event.KeyEvent;

public class k {

	public static void main(String[] args) throws AWTException, InterruptedException {
		Robot robot = new Robot();
		while (true) {
			robot.keyPress(KeyEvent.VK_V);
			Thread.sleep(10);
			robot.keyRelease(KeyEvent.VK_V);
			Thread.sleep(10);
		}
	}

}
