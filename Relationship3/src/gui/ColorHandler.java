package gui;

public class ColorHandler {
	
	public static java.awt.Color convertJavaFXColorToAWTColor(javafx.scene.paint.Color javaFXColor) {
		java.awt.Color awtColor = new java.awt.Color((float) javaFXColor.getRed(), (float) javaFXColor.getGreen(),
				(float) javaFXColor.getBlue(), (float) javaFXColor.getOpacity());
		return awtColor;
	}
	
	public static java.awt.Color stringToColor(final String value) {
	    String redValue = "";
	    String greenValue = "";
	    String blueValue = "";
	    int i = 17;
	    while (value.charAt(i) != ',') {
	    	redValue = redValue + value.charAt(i);
	    	i++;
	    }
	    i=i+3;
	    while (value.charAt(i) != ',') {
	    	greenValue = greenValue + value.charAt(i);
	    	i++;
	    }
	    i=i+3;
	    while (value.charAt(i) != ']') {
	    	blueValue = blueValue + value.charAt(i);
	    	i++;
	    }
	    java.awt.Color color = new java.awt.Color(Integer.parseInt(redValue), Integer.parseInt(greenValue), Integer.parseInt(blueValue));
	    return color;
	  }
}
