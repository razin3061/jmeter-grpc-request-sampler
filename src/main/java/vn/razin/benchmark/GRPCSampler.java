package vn.razin.benchmark;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.StatusRuntimeException;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.razin.benchmark.core.ClientCaller;

import java.util.concurrent.atomic.AtomicInteger;

public class GRPCSampler extends AbstractSampler implements Sampler{

    private static final Logger log = LoggerFactory.getLogger(GRPCSampler.class);
    private static final long serialVersionUID = 232L;

    public static final String METADATA = "GRPCSampler.metadata";
    public static final String HOST = "GRPCSampler.host";
    public static final String PORT = "GRPCSampler.port";
    public static final String FULL_METHOD = "GRPCSampler.fullMethod";
    public static final String REQUEST_JSON = "GRPCSampler.requestJson";
    public static final String DEADLINE = "GRPCSampler.deadline";
    public static final String TLS = "GRPCSampler.tls";
    public static final String PROTOCOMPIL = "";

    private transient ClientCaller clientCaller = null;

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public GRPCSampler() {
        classCount.incrementAndGet();
        trace("GRPCSampler()");
    }

    /**
     * @return a string for the sampleResult Title
     */
    private String getTitle() {
        return this.getName();
    }

    private void trace(String s) {
        if (log.isDebugEnabled()) {
            log.debug("{} ({}) {} {} {}", Thread.currentThread().getName(), classCount.get(),
                    getTitle(), s, this.toString());
        }
    }

    private void init() {
        clientCaller = new ClientCaller(
                getHostPort(),
                getFullMethod(),
                isTls(),
                getMetadata(),
                getProtoCompil());
    }

    @Override
    public SampleResult sample(Entry ignored) {
        log.info("{}\tSampleStarted", whoAmI());
        init();
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());

        String req = clientCaller.buildRequest(getRequestJson());
        res.setSamplerData(req);
        res.sampleStart();

        try {
            try {
                DynamicMessage resp = clientCaller.call(getDeadline());

                try {
                    res.sampleEnd();
                    res.setSuccessful(true);
                    res.setResponseData(JsonFormat.printer().print(resp).getBytes());
                    res.setResponseMessage("Success");
                    res.setDataType(SampleResult.TEXT);
                    res.setResponseCodeOK();
                } catch (InvalidProtocolBufferException e) {
                    errorResult(res, e);
                }
            } catch (RuntimeException e) {
                errorResult(res, e);
            }
        } catch (StatusRuntimeException e) {
            errorResult(res, e);
        }

        if (clientCaller != null) {
            clientCaller.shutdown();
        }
        return res;
    }

    @Override
    public void clear() {
        super.clear();
    }

    private String whoAmI() {
        return Thread.currentThread().getName() +
                "@" +
                Integer.toHexString(hashCode()) +
                "-" +
                getName();
    }

    private void errorResult(SampleResult res, Exception e) {
        if (!e.getCause().toString().contains("Need to sign messages")) {
                res.sampleEnd();
                res.setSuccessful(false);
                res.setResponseMessage("Exception: " + e.getCause());
                res.setResponseData(e.getMessage().getBytes());
                res.setResponseHeaders(getMetadata());
                res.setRequestHeaders(getMetadata());
                res.setDataType(SampleResult.TEXT);
                res.setResponseCode("500");
        }else {
            res.sampleEnd();
            res.setSuccessful(true);
            res.setRequestHeaders(getMetadata());
            res.setResponseData(e.getMessage().getBytes());
            //res.setResponseHeaders(e.getCause().toString());
            res.setResponseMessage(e.getCause().toString());
            res.setResponseHeaders(getMetadata());
            res.setDataType(SampleResult.TEXT);
            res.setResponseCodeOK();
        }
    }

    /**
     * GETTER AND SETTER
     */

    public String getProtoCompil() {
        return getPropertyAsString(PROTOCOMPIL);
    }

    public void setProtoCompil(String ProtoCompil) {
        setProperty(PROTOCOMPIL, ProtoCompil);
    }

    public String getMetadata() {
        return getPropertyAsString(METADATA);
    }

    public void setMetadata(String metadata) {
        setProperty(METADATA, metadata);
    }

    public String getFullMethod() {
        return getPropertyAsString(FULL_METHOD);
    }

    public void setFullMethod(String fullMethod) {
        setProperty(FULL_METHOD, fullMethod);
    }

    public String getRequestJson() {
        return getPropertyAsString(REQUEST_JSON);
    }

    public void setRequestJson(String requestJson) {
        setProperty(REQUEST_JSON, requestJson);
    }

    public String getDeadline() {
        return getPropertyAsString(DEADLINE);
    }

    public void setDeadline(String deadline) {
        setProperty(DEADLINE, deadline);
    }

    public boolean isTls() {
        return getPropertyAsBoolean(TLS);
    }

    public void setTls(boolean tls) {
        setProperty(TLS, tls);
    }

    public String getHost() {
        return getPropertyAsString(HOST);
    }

    public void setHost(String host) {
        setProperty(HOST, host);
    }

    public String getPort() {
        return getPropertyAsString(PORT);
    }

    public void setPort(String port) {
        setProperty(PORT, port);
    }

    private String getHostPort() {
        return getHost() + ":" + getPort();
    }




}
