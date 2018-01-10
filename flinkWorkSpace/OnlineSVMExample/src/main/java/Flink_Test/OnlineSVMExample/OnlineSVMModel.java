package Flink_Test.OnlineSVMExample;

import org.apache.flink.ml.common.LabeledVector;
import org.apache.flink.ml.math.DenseVector;
import org.apache.flink.ml.math.SparseVector;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;

public class OnlineSVMModel extends OnlineLearningModel {
    public OnlineSVMModel(String[] args) {
        super(args);
    }

    @Override
    protected CoFlatMapFunction<LabeledVector, DenseVector, DenseVector> train() {
        return new CoFlatMapFunction<LabeledVector, DenseVector, DenseVector>() {
            private DenseVector oldParams = null;
            private DenseVector latestParams = null;
            private DenseVector accuGradient = null;
            private int updateCnt = 0;
            private boolean inited = false;

            private void init() {
                if (!inited) {
                    inited = true;
                    oldParams = newParams();
                    latestParams = newParams();
                    accuGradient = newParams();
                    updateCnt = 0;
                }
            }

            public void flatMap1(LabeledVector v, Collector<DenseVector> coll) throws Exception { // use each sample to train local model
                init();
                // http://www.mit.edu/~rakhlin/6.883/lectures/lecture04.pdf, Algorithm 2, Pegasos
                double fac = v.label() * latestParams.dot(v.vector());
                double dec = 1 - learningRate * regularization;
                for (int i = 0; i < paramSize; i++) {
                    latestParams.data()[i] *= dec;
                }
                if (fac < 1) {
                    SparseVector features = (SparseVector) v.vector();
                    double[] values = features.data();
                    int[] indices = features.indices();
                    for (int i = 0; i < indices.length; i++) {
                        latestParams.data()[indices[i]] += learningRate * v.label() * values[i];
                    }
                }
                updateCnt += 1;
                if (updateFreq == updateCnt) { // Can use count based here, not timer based, because we need to ensure coll is legal.
                    updateCnt = 0;
                    DenseVector grad = newParams();
                    for (int i = 0; i < paramSize; i++) {
                        grad.data()[i] = latestParams.data()[i] - oldParams.data()[i];
                    }
                    coll.collect(grad);
                    for (int i = 0; i < paramSize; i++) {
                        oldParams.data()[i] += accuGradient.data()[i];
                        accuGradient.data()[i] = 0;
                    }
                    latestParams = oldParams.copy();
                }
            }

            public void flatMap2(DenseVector grad, Collector<DenseVector> coll) throws Exception { // update local params with broadcasted gradient
                init();
                for (int i = 0; i < paramSize; i++) {
                    accuGradient.update(i, accuGradient.data()[i] + grad.data()[i]);
                }
            }
        };
    }
}
