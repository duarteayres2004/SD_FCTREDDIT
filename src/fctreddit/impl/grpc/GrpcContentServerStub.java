package fctreddit.impl.grpc;

import fctreddit.impl.grpc.generated_java.ContentGrpc;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

public class GrpcContentServerStub implements ContentGrpc.AsyncService, BindableService {

    @Override
    public ServerServiceDefinition bindService() {
        return null;
    }
}
