package de.maikmerten.quaketexturetool;

/**
 *
 * @author maik
 */
public class DistanceCalculator {
	
	public static enum Method {
		RGB,
		YPRPB
	}
	
	private final Method method;
	
	public DistanceCalculator(Method method) {
		this.method = method;
	}
	
	
	public double getDistance(int color1, int color2) {
		if(method.equals(Method.RGB)) {
			return Color.getDistancePlain(color1, color2);
		} else {
			return Color.getDistanceYPrPb(color1, color2);
		}
	}
	
	
}
