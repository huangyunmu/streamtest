package Flink_Test.LogReg;

import java.io.Serializable;

public class Params implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Float theta0, theta1;

	public Params() {
		this.theta0 = 0.1f;
		this.theta1 = -0.1f;
	}

	public Params(Float x0, Float x1) {
		this.theta0 = x0;
		this.theta1 = x1;
	}

	@Override
	public String toString() {
		return theta0 + " " + theta1;
	}

	public Float getTheta0() {
		return theta0;
	}

	public Float getTheta1() {
		return theta1;
	}

	public void setTheta0(Float theta0) {
		this.theta0 = theta0;
	}

	public void setTheta1(Float theta1) {
		this.theta1 = theta1;
	}

	public Params div(Integer a) {
		this.theta0 = theta0 / a;
		this.theta1 = theta1 / a;
		return this;
	}

}
