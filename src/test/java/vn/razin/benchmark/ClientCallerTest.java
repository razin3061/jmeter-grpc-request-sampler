package vn.razin.benchmark;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.razin.benchmark.core.ClientCaller;

import java.nio.file.Path;
import java.nio.file.Paths;

@Ignore
public class ClientCallerTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientCallerTest.class);

    private static final Path PROTO_FOLDER = Paths.get("D:\\atomyze\\proto_01032020\\atmz");

    /* Download at https://github.com/googleapis/googleapis */
    private static final Path LIB_FOLDER = Paths.get("D:\\atomyze\\googleapis-master");
    private static String HOST_PORT = "atomyze-develop.atm-dev.n-t.io:443";
    private static String HOST = "gateway.dev.atm-ru.n-t.io";
    private static Integer PORT = 443;
    private static String REQUEST_JSON = "{\n" +
            "    \"email\": \"stepanova.y@n-t.io\",\n" +
            "    \"password\": \"231339994c8573ac95019743582e8c25cc09f03d2ab5757d33ea3d42841f3659bddfa55572d4f1962159f1a2939eabebad1641e0774a774dca8de21a1bbb73cd\",\n" +
            "    \"verificationCode\": \"000000\"\n" +
            "  }";
    private static String FULL_METHOD = "atmz.web.auth.AuthService/AuthorizeB2B";
    private static String PROTOCOMPIL = "d:\\atomyze_RU\\protocInvoker\\compileProto12072021.pb.bin";
    private static boolean TLS = Boolean.TRUE;
    private static String METADATA = "device:f8fee325a5879a4729c5017d9698bab8;Platform:Desktop;" +
            "OS:linux;browser:Windows;x-forwarded-for:8.8.8.8;origin:\"https://atm-front.dev.atm-ru.n-t.io\"";

    private ClientCaller clientCaller;

    @Before
    public void setup() {
        logger.info("Setup test");

        //clientCaller = new ClientCaller(HOST_PORT, FULL_METHOD, TLS, METADATA, PROTOCOMPIL);
        clientCaller = new ClientCaller(HOST, FULL_METHOD, TLS, METADATA, PROTOCOMPIL);
    }

    @Test
    public void test() {
        logger.info("Main test");
        clientCaller.buildRequest(REQUEST_JSON);
        DynamicMessage resp = clientCaller.call("1000000");
        System.gc();
        clientCaller.shutdown();


        try {
            logger.info(JsonFormat.printer().print(resp));
            System.out.println(JsonFormat.printer().print(resp));
        } catch (InvalidProtocolBufferException e) {
            logger.error("Exception when parsing to JSON" , e);
        }
    }

}
