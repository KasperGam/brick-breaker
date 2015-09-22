package base;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

public class Starter {

	private static Starter start = new Starter();
	private static BrickBreaker applet = new BrickBreaker();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AppletFrame frame = start.new AppletFrame();
		Dimension d = new Dimension(400, 700);
		frame.setSize(d);
		frame.setVisible(true);
		frame.add(applet);
		applet.init();
		frame.addKeyListener(applet.getKeyListeners()[0]);
		frame.addMouseListener(applet.getMouseListeners()[0]);
		frame.addComponentListener(applet.getComponentListeners()[0]);
		frame.setBackground(Color.blue);
		while(true){
			if (!frame.getBackground().equals(applet.getBackground())){
				frame.setBackground(applet.getBackground());
				frame.repaint();
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	class AppletFrame extends JFrame {
		private static final long serialVersionUID = 1L;
	}

}


