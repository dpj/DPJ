import java.util.List;

public class Inner<type T, region R> {
    region r;
    
    public class InnerInner {
	Inner<T, R:r> obj;
    }
}
