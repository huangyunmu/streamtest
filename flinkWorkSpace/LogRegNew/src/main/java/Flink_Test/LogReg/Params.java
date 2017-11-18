package Flink_Test.LogReg;

import java.io.Serializable;

public class Params implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double theta0, theta1;

	public Params() {}

	public Params(double x0, double x1) {
		this.theta0 = x0;
		this.theta1 = x1;
	}

	@Override
	public String toString() {
		return theta0 + " " + theta1;
	}

	public double getTheta0() {
		return theta0;
	}

	public double getTheta1() {
		return theta1;
	}

	public void setTheta0(double theta0) {
		this.theta0 = theta0;
	}

	public void setTheta1(double theta1) {
		this.theta1 = theta1;
	}

	public Params div(Integer a) {
		this.theta0 = theta0 / a;
		this.theta1 = theta1 / a;
		return this;
	}

}
