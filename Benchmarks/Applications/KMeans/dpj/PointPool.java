
import java.util.concurrent.ConcurrentHashMap;

public class PointPool {
	
	private ConcurrentHashMap<Point, Boolean> new_centers;
	Point sumFeature;
	
	PointPool(int nfeatures, int capacity, float loadfactor, int concurrencyLevel) {
		new_centers = new ConcurrentHashMap<Point, Boolean>(capacity, loadfactor, concurrencyLevel);
		sumFeature = new Point(nfeatures);
	}
	
	void putPoint(Point point) {
		new_centers.put(point, true);
	}
	
	void getObjectSum() {
		for (Point p : new_centers.keySet()) {
			sumFeature.addFeatures(p.getFeatures());
		}
	}
	
	/**
	 * @return the sumFeature
	 */
	public Point getSumFeature() {
		return sumFeature;
	}
	
	public int size() {
		return new_centers.size();
	}
	
	public void clear() {
		sumFeature.clear();
		new_centers.clear();
	}
}
