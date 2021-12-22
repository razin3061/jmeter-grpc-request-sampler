package vn.razin.benchmark.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import vn.razin.benchmark.core.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import vn.razin.benchmark.core.grpc.ChannelFactory;
import vn.razin.benchmark.core.grpc.DynamicGrpcClient;
import vn.razin.benchmark.core.io.MessageReader;
import vn.razin.benchmark.core.protobuf.ProtoMethodName;
import vn.razin.benchmark.core.protobuf.ServiceResolver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class ClientCaller {
    private Descriptors.MethodDescriptor methodDescriptor;
    private JsonFormat.TypeRegistry registry;
    private DynamicGrpcClient dynamicClient;
    private ImmutableList<DynamicMessage> requestMessages;
    private ManagedChannel channel;

    public ClientCaller(String HOST_PORT, String FULL_METHOD, boolean TLS, String METADATA, String PROTOCOMPIL) {
        this.init(HOST_PORT, FULL_METHOD, TLS, METADATA, PROTOCOMPIL);
    }

    public void init(String HOST_PORT, String FULL_METHOD, boolean tls, String metadata, String PROTOCOMPIL) {
        HostAndPort hostAndPort = HostAndPort.fromString(HOST_PORT);
        ProtoMethodName grpcMethodName =
                ProtoMethodName.parseFullGrpcMethodName(FULL_METHOD);

        ChannelFactory channelFactory = ChannelFactory.create();
        Map<String, String> metadataMap = buildHashMetadata(metadata);
        try {
            channel = channelFactory.createChannel(hostAndPort, tls, metadataMap);

        }catch (IllegalStateException e){
            throw new RuntimeException("Unable to create channel grpc by invoking tls", e);
        }

        // Fetch the appropriate file descriptors for the service.
        final DescriptorProtos.FileDescriptorSet fileDescriptorSet;

        try {
            fileDescriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(Files.readAllBytes(Paths.get(PROTOCOMPIL)));
        } catch (Throwable t) {
            throw new RuntimeException("Unable to resolve service by invoking protoc", t);
        }

        // Set up the dynamic client and make the call.
        ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
        methodDescriptor = serviceResolver.resolveServiceMethod(grpcMethodName);

        dynamicClient = DynamicGrpcClient.create(methodDescriptor, channel);

        // This collects all known types into a registry for resolution of potential "Any" types.
        registry = JsonFormat.TypeRegistry.newBuilder()
                .add(serviceResolver.listMessageTypes())
                .build();

    }

    private Map<String, String> buildHashMetadata(String metadata) {
        Map<String, String> metadataHash = new LinkedHashMap<>();

        if(Strings.isNullOrEmpty(metadata))
            return metadataHash;

        String[] keyValue;
        for (String part : metadata.split(";")){
            keyValue = part.split(":", 2);

            Preconditions.checkArgument(keyValue.length == 2,
                    "Metadata entry must be defined in key1:value1,key2:value2 format: " + metadata);

            metadataHash.put(keyValue[0], keyValue[1]);
        }

        return metadataHash;
    }

    public String buildRequest(String jsonData) {
        requestMessages =
                MessageReader.forJSON(methodDescriptor.getInputType(), registry, jsonData).read();

        try {
            return JsonFormat.printer().print(requestMessages.get(0));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Caught exception while parsing request for rpc", e);
        }
    }

    public DynamicMessage call(String deadlineMs) {
        long deadline;
        try {
            deadline = Long.parseLong(deadlineMs);
        }catch (Exception e){
            throw new RuntimeException("Caught exception while parsing deadline to long", e);
        }

        DynamicMessage resp;
        try {

            resp = dynamicClient.blockingUnaryCall(requestMessages, callOptions(deadline));
        } catch (Throwable t) {
            throw new RuntimeException("Caught exception while waiting for rpc", t);
        }
        return resp;
    }

    private static CallOptions callOptions(long deadlineMs) {
        CallOptions result = CallOptions.DEFAULT;
        if (deadlineMs > 0) {
            result = result.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
        }
        return result;
    }

    public void shutdown(){

        try {
            if (channel != null)
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Caught exception while shutting down channel", e);
        }
    }

}
