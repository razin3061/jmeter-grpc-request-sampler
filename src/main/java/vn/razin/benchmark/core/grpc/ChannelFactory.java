package vn.razin.benchmark.core.grpc;

import com.google.common.net.HostAndPort;
import io.grpc.*;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import vn.razin.benchmark.core.cookie.CookieStoreInterceptor;

import java.net.SocketAddress;
import java.util.Map;

/**
 * Knows how to construct grpc channels.
 */
public class ChannelFactory {
    public static ChannelFactory create() {
        return new ChannelFactory();
    }

    private ChannelFactory() {
    }

    public ManagedChannel createChannel(HostAndPort endpoint, boolean tls, Map<String, String> metadataHash) {
        ManagedChannelBuilder managedChannelBuilder = createChannelBuilder(endpoint, tls, metadataHash);
        return managedChannelBuilder.build();
    }

    private ManagedChannelBuilder createChannelBuilder(HostAndPort endpoint, boolean tls, Map<String, String> metadataHash) {
        if (tls) {
            return NettyChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPortOrDefault(-1))
                    .negotiationType(NegotiationType.TLS)
                    .intercept(new CookieStoreInterceptor(),metadataInterceptor(metadataHash));
        }
        return NettyChannelBuilder.forAddress(endpoint.getHost(), endpoint.getPortOrDefault(-1))
                .negotiationType(NegotiationType.PLAINTEXT)
                .intercept(new CookieStoreInterceptor(),metadataInterceptor(metadataHash));
    }


    private ClientInterceptor metadataInterceptor(Map<String, String> metadataHash) {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    final io.grpc.MethodDescriptor<ReqT, RespT> method,
                    CallOptions callOptions,
                    final Channel next) {
                return new ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                    @Override
                    protected void checkedStart(Listener<RespT> responseListener, Metadata headers) {
                        for (Map.Entry<String, String> entry : metadataHash.entrySet()) {
                            Metadata.Key<String> key = Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER);
                            headers.put(key, entry.getValue());
                        }
                        delegate().start(responseListener, headers);
                    }

                };
            }
        };
    }

}
