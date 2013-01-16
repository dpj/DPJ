package DPJBenchmarks;

import java.util.Formatter;
import DPJRuntime.ArrayDouble;

/**
 * Vector operations
 */
public class Vector<region R> {

    static arrayclass Array<region R> {
	Vector<R> in R;
    }

    public final ArrayDouble<R> elts in R = 
	new ArrayDouble<R>(Constants.NDIM);

    public void CLRV() writes R {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = 0.0;
    }

    public void UNITV(int coord) writes R {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = (coord == i) ? 1.0 : 0.0;
    }

    public <region Ru>void SETV(Vector<Ru> u) reads Ru writes R {
	for (int i = 0; i < Constants.NDIM; i++) 					
	    elts[i] = u.elts[i]; 						
    }
    
    public <region Ru,Rw>void ADDV(Vector<Ru> u, Vector<Rw> w) 
	reads Ru, Rw writes R {
	for (int i = 0; i < Constants.NDIM; ++i)
	    elts[i] = u.elts[i] + w.elts[i];					
    }

    public <region Ru,Rw>void SUBV(Vector<Ru> u, Vector<Rw> w)
	reads R, Ru, Rw writes R {
	for (int i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] - w.elts[i];					
    }

    /**
     *  MULtiply Vector by Scalar 
     */
    public <region Ru>void MULVS(Vector<Ru> u, double s) 
	reads Ru writes R {
	int i;							
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] * s;					
    }

    public <region Ru>void DIVVS(Vector<Ru> u, double s)
	reads Ru writes R {
	int i;					       	
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = u.elts[i] / s;					
    }


    public <region Ru>double DOTVP(Vector<Ru> u)
	reads Ru writes R {
	int i;							
	double s = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    s += elts[i] * u.elts[i];					
	return s;
    }
    
    
    public void ABSV(double s) reads R {
	double tmp;                                                
	int i;							
	tmp = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    tmp += elts[i] * elts[i];					
	s = Math.sqrt(tmp);                                                   
    }
    
    public <region Ru>void DISTV(double s, Vector<Ru> u) 
        reads R, Ru {
	double tmp;                                                
	int i;							
	tmp = 0.0;								
	for (i = 0; i < Constants.NDIM; i++)					
	    tmp += (u.elts[i]-elts[i]) * (u.elts[i]-elts[i]);		        
	s = Math.sqrt(tmp);                                                   
    }

    public <region Ru,Rw>void CROSSVP(Vector<Ru> u, Vector<Rw> w) 
	reads Ru, Rw, Ru, Rw writes R {
	elts[0] = u.elts[1]*w.elts[2] - u.elts[2]*w.elts[1];				
	elts[1] = u.elts[2]*w.elts[0] - u.elts[0]*w.elts[2];				
	elts[2] = u.elts[0]*w.elts[1] - u.elts[1]*w.elts[0];				
    }

    public <region Ru>void INCADDV(Vector<Ru> u) 
	reads Ru writes R {
	    int i;
	    for (i = 0; i < Constants.NDIM; i++)
		elts[i] += u.elts[i];                                             
    }

    public <region Ru>void INCSUBV(Vector<Ru> u) 
	reads Ru writes R {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] -= u.elts[i];                                             
    }
    
    public void INCMULVS(double s) writes R {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] *= s;                                                 
    }

    public void INCDIVVS(double s) writes R {
	int i;                                                    
	for (i = 0; i < Constants.NDIM; i++)                                       
	    elts[i] /= s;                                                 
    }

    public void SETVS(double s) writes R {
	int i;							
	for (i = 0; i < Constants.NDIM; i++)					
	    elts[i] = s;							
    }

    public <region Ru>void ADDVS(Vector<Ru> u, double s) 
	reads Ru writes R {
	int i;
	for (i = 0; i < Constants.NDIM; i++)			
	    elts[i] = u.elts[i] + s;					
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	Formatter f = new Formatter(sb);
	sb.append("<");
	f.format("%f", elts[0]);
	for (int i = 1; i < elts.length; ++i) {
	    f.format(",%f", elts[i]);
	}
	sb.append(">");
	return sb.toString();
    }

    public <region Rv>boolean EQUAL(Vector<Rv> v) reads Rv, R {
	for (int i = 0; i < Constants.NDIM; ++i)
	    if (elts[i] != v.elts[i]) return false;
	return true;
    }
}
