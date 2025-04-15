package fctreddit.impl.server.grpc;

import fctreddit.impl.server.grpc.generated_java.ContentGrpc;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

public class GrpcContentServerStub implements ContentGrpc.AsyncService, BindableService {

    @Override
    public ServerServiceDefinition bindService() {
        return null;
    }
}
