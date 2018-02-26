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
		int dataLength = vector.data().length;

		try {

			// out.writeChars(element.toString());
			out.writeInt(element.getCount());
			out.writeInt(dataLength);
			out.writeDouble(label);
			for (int i = 0; i < vector.size(); i++) {
				out.writeInt(vector.indices()[i]);
				out.writeDouble(vector.data()[i]);
			}
			out.close();
			return bytes.toByteArray();
		} catch (Exception e) {
			return new byte[0];
		}

	}

	@Override
	public CountLabelExample deserialize(byte[] message) throws IOException {
		// TODO Auto-generated method stub

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
		// String rawData = in.readUTF();
		// String[] tempSplits = rawData.split("|");

		int count = in.readInt();
		int dataLength = in.readInt();
		double label = in.readDouble();
		int[] indices = new int[dataLength];
		double[] values = new double[dataLength];
		for (int i = 0; i < dataLength; i++) {
			indices[i] = in.readInt();
			values[i] = in.readDouble();
		}
		LabeledVector vector = new LabeledVector(label, new SparseVector(paramSize, indices, values));
		CountLabelExample countLabelExample = new CountLabelExample(vector, count);
		return countLabelExample;
	}

	@Override
	public boolean isEndOfStream(CountLabelExample nextElement) {
		// TODO Auto-generated method stub
		return false;
	}

}
