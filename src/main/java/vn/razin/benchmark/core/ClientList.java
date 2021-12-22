package vn.razin.benchmark.core;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import vn.razin.benchmark.core.protobuf.ServiceResolver;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class ClientList {

    public static List<String> listServices(String PROTOCOMPIL) {
        List<String> methods = new LinkedList<>();

        final DescriptorProtos.FileDescriptorSet fileDescriptorSet;
        try {
            fileDescriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(Files.readAllBytes(Paths.get(PROTOCOMPIL)));
        } catch (Throwable t) {
            throw new RuntimeException("Unable to resolve service by invoking protoc", t);
        }

        ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
        for (ServiceDescriptor descriptor : serviceResolver.listServices()) {
            for (MethodDescriptor method : descriptor.getMethods()) {
                methods.add(descriptor.getFullName() + "/" + method.getName());
            }
        }

        return methods;
    }

}
