package Flink_Test.OnlineSVMExample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.ml.common.LabeledVector;
import org.apache.flink.ml.math.SparseVector;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;

public class CountLabelExampleSchema
		implements DeserializationSchema<CountLabelExample>, SerializationSchema<CountLabelExample> {
	protected int paramSize;

	private static double formalize(double label) {
		if (label == 1) {
			return 1;
		} else {
			return -1;
		}
	}
	public CountLabelExampleSchema() {

	}

	public CountLabelExampleSchema(int paramSize) {
		this.paramSize = paramSize;
	}

	@Override
	public TypeInformation<CountLabelExample> getProducedType() {
		// TODO Auto-generated method stub
		return BasicTypeInfo.of(CountLabelExample.class);
	}

	@Override
	public byte[] serialize(CountLabelExample element) {
		// TODO Auto-generated method stub
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		double label = element.getVector().label();
		SparseVector vector = (SparseVector) (element.getVector().vector());
		int count = element.getCount();

		try {

			out.writeChars(element.toString());
			// out.writeInt(element.getCount());
			// out.writeChars("|");
			// out.writeDouble(label);
			//
			// for (int i = 0; i < vector.size(); i++) {
			// out.writeChars(" ");
			// out.writeInt(vector.indices()[i]);
			// out.writeChars(":");
			// out.writeDouble(vector.data()[i]);
			// }
			out.close();
			return bytes.toByteArray();
		} catch (Exception e) {
			return new byte[0];
		}

	}

	private LabeledVector parseExample(String s) {
		// format of s: timestamp sample
		// format of sample: label idx1:val1 idx2:val2 idx3:val3 ...
		String[] splits = s.split("\\s");
		double label = formalize(Integer.valueOf(splits[0]));

		int[] indices = new int[splits.length - 1];
		double[] values = new double[splits.length - 1];
		for (int i = 1; i < splits.length; i++) {
			String[] iv = splits[i].split(":");
			indices[i - 1] = Integer.valueOf(iv[0]);
			values[i - 1] = Double.valueOf(iv[1]);
		}
		return new LabeledVector(label, new SparseVector(paramSize, indices, values));
	}

	@Override
	public CountLabelExample deserialize(byte[] message) throws IOException {
		// TODO Auto-generated method stub

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
		String rawData = in.readLine();
		String[] tempSplits = rawData.split("|");
		int count = Integer.parseInt(tempSplits[0]);
		LabeledVector vector = parseExample(tempSplits[1]);
		CountLabelExample countLabelExample = new CountLabelExample(vector, count);
		return countLabelExample;
	}

	@Override
	public boolean isEndOfStream(CountLabelExample nextElement) {
		// TODO Auto-generated method stub
		return false;
	}

}
