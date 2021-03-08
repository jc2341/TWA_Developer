/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.thermo.partition_function.rotation.internal.hindered.tdppi.as;

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.cam.ceb.como.math.constant.PhysicalConstants;
import uk.ac.cam.ceb.como.math.data.DataPoint;
import uk.ac.cam.ceb.como.math.fourier.series.FourierSeries;
import uk.ac.cam.ceb.como.math.function.Function;
import uk.ac.cam.ceb.como.math.function.FunctionCalculationException;
import uk.ac.cam.ceb.como.math.integrator.SimpsonIntegrator;
import uk.ac.cam.ceb.como.thermo.partition_function.PartitionFunction;
import uk.ac.cam.ceb.como.thermo.partition_function.PartitionValues;
import uk.ac.cam.ceb.como.thermo.partition_function.rotation.internal.hindered.c1dhr.C1DHRPartitionFunction;

/**
 *
 * @author pb556
 */
public class TDPPIASPartitionFunction extends PartitionFunction {

    protected double T = 0.0;
    protected FourierSeries approximation = null;
    protected double frequency = 0.0;
    protected double w = 0.0;
    protected double v = 0.0;
    protected SimpsonIntegrator integrator = new SimpsonIntegrator();
    private ITorsionBarrier1 iTorsionBarrier1 = new ITorsionBarrier1();
    private ITorsionBarrier2 iTorsionBarrier2 = new ITorsionBarrier2();
    private ITorsionBarrier3 iTorsionBarrier3 = new ITorsionBarrier3();

    public TDPPIASPartitionFunction() {
        super();
    }

    public TDPPIASPartitionFunction(FourierSeries approximation, double frequency) {
        super();
        this.approximation = approximation;
        this.frequency = frequency;
    }
    private int symmetry = -1;
    private double reducedMoment = 0.0;

    public void setRotorSymmetry(int symmetry) {
        this.symmetry = symmetry;
    }

    public void setReducedMoment(double reducedMoment) {
        this.reducedMoment = reducedMoment;
    }

    @Override
    public PartitionValues getPartitionValues(double T) {
        this.T = T;
        PartitionValues Q = new PartitionValues();
        //symmetry = 1;
        integrator.setLimit(0, 2.0 * Math.PI / (double) symmetry);

        try {
            
            v = getBarrierValue(approximation);
            
            // CALCULATE integrals
            integrator.setFunction(iTorsionBarrier1);
            double val_iTorsionBarrier1 = integrator.getIntegral();
            integrator.setFunction(iTorsionBarrier2);
            double val_iTorsionBarrier2 = integrator.getIntegral();
            integrator.setFunction(iTorsionBarrier3);
            double val_iTorsionBarrier3 = integrator.getIntegral();

            double K = Math.sqrt(2.0 * Math.PI * PhysicalConstants.k_B / (PhysicalConstants.h * PhysicalConstants.h));

            double m = 1.0 / (2.0 * PhysicalConstants.k_B);

            // CALCULATE q
            Q.q = K * Math.sqrt(T) * val_iTorsionBarrier1;

            // CALCULATE dqBydT
            double first_in = 0.5 / T;

            Q.dqBydT = Q.q * first_in + (K * m * val_iTorsionBarrier2 * Math.pow(T, -1.5));

            double term1 = (-1.0) * K * val_iTorsionBarrier1 / (4.0 * Math.pow(T, (3.0 / 2.0)));
            double term2 = (K * val_iTorsionBarrier2 * (1.0 / (T * T * 2.0 * PhysicalConstants.k_B))) / (Math.sqrt(T));
            double term3 = (-1.0) * K * Math.sqrt(T) * 2.0 * val_iTorsionBarrier2 / (Math.pow(T, 3.0) * 2.0 * PhysicalConstants.k_B);
            double term4 = K * Math.sqrt(T) * val_iTorsionBarrier3 / (Math.pow(T, 4.0) * 2.0 * 2.0 * PhysicalConstants.k_B * PhysicalConstants.k_B);

            Q.d2qBydT2 = term1 + term2 + term3 + term4;

        } catch (Exception ex) {
            Logger.getLogger(TDPPIASPartitionFunction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Q;
    }

    private double calculateC(double k) {
        double beta = 1.0 / (PhysicalConstants.k_B * T);
        double kappa;

        w = Math.sqrt(Math.abs(k) / reducedMoment);//

        if (k > 0) {
            double y = (beta * PhysicalConstants.hbar * w) / (2.0);
            kappa = (Math.sinh(y)) / (y);
        } else if (k < 0) {
            double alpha = 2.0 * Math.PI / (PhysicalConstants.hbar * Math.abs(k));
            double denominator = beta - alpha;
            double f = beta / denominator;
            if (alpha == beta) {
                kappa = beta * v;
            } else if (beta < alpha) {
                kappa = (Math.PI * beta / alpha) / (Math.sin(Math.PI * beta / alpha)) + f * Math.exp(v * (beta - alpha));
            } else {
                kappa = f * (Math.exp(v * (beta - alpha)) - 1.0);
            }
        } else {
            return 0.0;
        }
        return Math.sqrt((8.0 / (beta * Math.abs(k))) * Math.log(kappa));
    }

    private class ITorsionBarrier1 extends Function<Double, Double> {

        @Override
        public Double f(Double x, Object... additionalData) {
            try {
                double k = (Double) approximation.fDerivative(2, new Double[]{x}).getValue();
                double c = calculateC(k);
                double fx_A = (Double) ((DataPoint) approximation.f(new Double[]{x + c / 2})).getValue();
                double fx_B = (Double) ((DataPoint) approximation.f(new Double[]{x - c / 2})).getValue();
                double fx = fx_A + fx_B;
                return Math.sqrt((Double) reducedMoment) * Math.exp(-1 * (fx) / (2 * PhysicalConstants.k_B * T));
            } catch (Exception ex) {
                Logger.getLogger(C1DHRPartitionFunction.class.getName()).log(Level.SEVERE, null, ex);
            }
            return 0.0;
        }
    }

    private class ITorsionBarrier2 extends Function<Double, Double> {

        @Override
        public Double f(Double x, Object... additionalData) {
            try {
                double k = (Double) approximation.fDerivative(2, new Double[]{x}).getValue();
                double c = calculateC(k);
                double fx_A = (Double) ((DataPoint) approximation.f(new Double[]{x + c / 2})).getValue();
                double fx_B = (Double) ((DataPoint) approximation.f(new Double[]{x - c / 2})).getValue();
                double fx = fx_A + fx_B;
                return (fx) * Math.sqrt((Double) reducedMoment) * Math.exp(-(fx) / (2 * PhysicalConstants.k_B * T));
            } catch (Exception ex) {
                Logger.getLogger(C1DHRPartitionFunction.class.getName()).log(Level.SEVERE, null, ex);
            }
            return 0.0;
        }
    }

    private class ITorsionBarrier3 extends Function<Double, Double> {

        @Override
        public Double f(Double x, Object... additionalData) {
            try {
                double k = (Double) approximation.fDerivative(2, new Double[]{x}).getValue();
                double c = calculateC(k);
                double fx_A = (Double) ((DataPoint) approximation.f(new Double[]{x + c / 2})).getValue();
                double fx_B = (Double) ((DataPoint) approximation.f(new Double[]{x - c / 2})).getValue();
                double fx = fx_A + fx_B;
                return (fx) * (fx) * Math.sqrt(reducedMoment) * Math.exp(-(fx) / (2 * PhysicalConstants.k_B * T));
            } catch (Exception ex) {
                Logger.getLogger(C1DHRPartitionFunction.class.getName()).log(Level.SEVERE, null, ex);
            }
            return 0.0;
        }
    }
    
    private double getBarrierValue(FourierSeries s) throws FunctionCalculationException {
        double max = 0.0;
        for (double i = 0; i < 3600; i += 0.1) {
            DataPoint v = s.f(new Double[]{i});
            Double val = (Double) v.getValue();
            if (val > max) {
                max = val;
            }
        }
        return max;
    }
}
