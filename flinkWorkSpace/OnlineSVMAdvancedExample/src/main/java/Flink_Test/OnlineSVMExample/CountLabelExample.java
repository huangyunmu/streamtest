package Flink_Test.OnlineSVMExample;

import org.apache.flink.ml.common.LabeledVector;
import org.apache.flink.ml.math.SparseVector;
import org.apache.flink.ml.math.Vector;

public class CountLabelExample {

	private LabeledVector vector;
	private int count;

	public CountLabelExample(LabeledVector vector, int count) {
		this.vector = vector;
		this.count = count;
	}

	public CountLabelExample() {
	}

	public LabeledVector getVector() {
		return vector;
	}

	public void setVector(LabeledVector vector) {
		this.vector = vector;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String toString() {
		// count|(lib svm format)
		String result = "";
		result = result + count + "|";
		result = result + this.vector.label();
		SparseVector sparseVector = (SparseVector) vector.vector();
		int length = sparseVector.indices().length;
		for (int i = 0; i < length; i++) {
			result = result + " " + sparseVector.indices()[i] + ":" + sparseVector.data()[i];
		}
		// result = result + "\n";
		// result = result + sparseVector.toString();
		return result;
	}

	public void decreaseCount() {
		this.count--;
	}

}
