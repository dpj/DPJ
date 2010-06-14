
import EDU.oswego.cs.dl.util.concurrent.*;

public class ToResult implements java.io.Serializable {
    private String header;
    private double expectedReturnRate = Double.NaN;
    private double volatility = Double.NaN;
    private double volatility2 = Double.NaN;
    private double finalStockPrice = Double.NaN;
    private double[] pathValue;
    
    public ToResult(String header, double expectedReturnRate, double volatility, double volatility2, double finalStockPrice, double[] pathValue) {
        super();
        this.header = header;
        this.expectedReturnRate = expectedReturnRate;
        this.volatility = volatility;
        this.volatility2 = volatility2;
        this.finalStockPrice = finalStockPrice;
        this.pathValue = pathValue;
    }
    
    public String toString() {
        return (header);
    }
    
    public String get_header() {
        return (this.header);
    }
    
    public void set_header(String header) {
        this.header = header;
    }
    
    public double get_expectedReturnRate() {
        return (this.expectedReturnRate);
    }
    
    public void set_expectedReturnRate(double expectedReturnRate) {
        this.expectedReturnRate = expectedReturnRate;
    }
    
    public double get_volatility() {
        return (this.volatility);
    }
    
    public void set_volatility(double volatility) {
        this.volatility = volatility;
    }
    
    public double get_volatility2() {
        return (this.volatility2);
    }
    
    public void set_volatility2(double volatility2) {
        this.volatility2 = volatility2;
    }
    
    public double get_finalStockPrice() {
        return (this.finalStockPrice);
    }
    
    public void set_finalStockPrice(double finalStockPrice) {
        this.finalStockPrice = finalStockPrice;
    }
    
    public double[] get_pathValue() {
        return (this.pathValue);
    }
    
    public void set_pathValue(double[] pathValue) {
        this.pathValue = pathValue;
    }
}
