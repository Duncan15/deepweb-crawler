package Algorithm_improved;
//developed by Doctor Wang

import java.util.*;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.linear.DiagonalMatrix;

class MyFunc implements ParametricUnivariateFunction {
    public double value(double r, double... parameters) {
        //return parameters[0] * Math.pow(t, parameters[1]) * Math.exp(-parameters[2] * t);
        //return parameters[0]/Math.pow((r+parameters[1]),parameters[2]);
    		return parameters[0] - parameters[2]*Math.log(r+parameters[1]);
    }

    // Jacobian matrix of the above. In this case, this is just an array of
    // partial derivatives of the above function, with one element for each parameter.
    public double[] gradient(double r, double... parameters) {
        final double a = parameters[0];
        final double b = parameters[1];
        final double c = parameters[2];

        return new double[] {
            1,
            -c/(b+r),
            -Math.log(b+r)
        		//1/Math.pow((r+parameters[1]),parameters[2]),
        		//-c*a*Math.pow((r+b),(-c-1)),
        		//-a*Math.log(r+b)*Math.pow((r+b),-c)
        		//Math.exp(-c*t) * Math.pow(t, b),
            //a * Math.exp(-c*t) * Math.pow(t, b) * Math.log(t),
            //a * (-Math.exp(-c*t)) * Math.pow(t, b+1)
        };
    }
}

public class MyFuncFitter extends AbstractCurveFitter {
    protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
        final int len = points.size();
        final double[] target  = new double[len];
        final double[] weights = new double[len];
        final double[] initialGuess = { 1.0, 1.0, 1.0 };

        int i = 0;
        for(WeightedObservedPoint point : points) {
            target[i]  = point.getY();
            weights[i] = point.getWeight();
            i += 1;
        }

        final AbstractCurveFitter.TheoreticalValuesFunction model = new
            AbstractCurveFitter.TheoreticalValuesFunction(new MyFunc(), points);

        return new LeastSquaresBuilder().
            maxEvaluations(Integer.MAX_VALUE).
            maxIterations(Integer.MAX_VALUE).
            start(initialGuess).
            target(target).
            weight(new DiagonalMatrix(weights)).
            model(model.getModelFunction(), model.getModelFunctionJacobian()).
            build();
    }

    public static void main(String[] args) {
        MyFuncFitter fitter = new MyFuncFitter();
        ArrayList<WeightedObservedPoint> points = new ArrayList<WeightedObservedPoint>();

        // Add points here; for instance,
        WeightedObservedPoint point = new WeightedObservedPoint(1.0,
            1.0,
            Math.log(34679));
        points.add(point);
        
        WeightedObservedPoint point1 = new WeightedObservedPoint(1.0,
                119,
                Math.log(43506));
        points.add(point1);
        
        WeightedObservedPoint point2 = new WeightedObservedPoint(1.0,
                237,
                Math.log(30064));
        points.add(point2);
        
        WeightedObservedPoint point3 = new WeightedObservedPoint(1.0,
                355,
                Math.log(20256));
        points.add(point3);
        
        WeightedObservedPoint point4 = new WeightedObservedPoint(1.0,
                473,
                Math.log(4745));
        points.add(point4);
            

        final double coeffs[] = fitter.fit(points);
        System.out.println(Arrays.toString(coeffs));
    }
}

