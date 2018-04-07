package Flink_Test.OnlineSVMExample;

import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.ml.math.DenseVector;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;

import java.io.*;

public class DenseVectorSchema implements DeserializationSchema<DenseVector>, SerializationSchema<DenseVector> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DenseVector deserialize(byte[] message) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        double[] values = new double[in.readInt()];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readDouble();
        }
        return new DenseVector(values);
    }

    public byte[] serialize(DenseVector vec) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeInt(vec.size());
            for (int i = 0; i < vec.size(); i++) {
            	
                out.writeDouble(vec.data()[i]);
                if(i%1000==0){
                	out.flush();
                }
                out.flush();
            }
            out.close();
            return bytes.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public TypeInformation<DenseVector> getProducedType() {
        return BasicTypeInfo.of(DenseVector.class); // Don't know how to create the return value
    }

    public boolean isEndOfStream(DenseVector nextElement) {
        return false;
    }
}
