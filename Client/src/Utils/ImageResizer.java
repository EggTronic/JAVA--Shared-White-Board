package Utils;

import java.awt.Image;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ImageResizer {
	
	
	public static ImageIcon reSizeForLabel(ImageIcon icon, JLabel label) {
		label.setOpaque(false);
		Image img = icon.getImage();  
	    Image resizedImage = img.getScaledInstance(label.getWidth(), label.getHeight(),  java.awt.Image.SCALE_SMOOTH);  
	    return new ImageIcon(resizedImage);
	}
	
	public static ImageIcon reSizeForButton(ImageIcon icon, JButton btn) {
		btn.setOpaque(false);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setBorder(null);
		btn.setMargin(new Insets(0, 0, 0, 0));
		Image img = icon.getImage();  
	    Image resizedImage = img.getScaledInstance((int)(btn.getWidth() * 0.7), (int) (btn.getHeight() * 0.7),  java.awt.Image.SCALE_SMOOTH);  
	    return new ImageIcon(resizedImage);
	}
}
