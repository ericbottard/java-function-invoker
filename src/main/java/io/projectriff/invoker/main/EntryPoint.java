package io.projectriff.invoker.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.projectriff.invoker.converters.NegotiatingByteArrayMessageConverter;
import io.projectriff.invoker.converters.NegotiatingMappingJackson2MessageConverter;
import io.projectriff.invoker.converters.NegotiatingStringMessageConverter;
import io.projectriff.invoker.rpc.StartFrame;
import io.projectriff.invoker.server.GrpcServerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.FunctionProperties;
import org.springframework.cloud.function.context.FunctionRegistry;
import org.springframework.cloud.function.context.catalog.BeanFactoryAwareFunctionRegistry;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.Nullable;
import org.springframework.messaging.converter.CompositeMessageConverter;

import java.io.IOException;
import java.util.Arrays;

/**
 * This is the main entry point for the java function invoker.
 * This sets up an application context with the whole Spring Cloud Function infrastructure (thanks to auto-configuration)
 * setup, pointing to the user function (via correctly set {@link FunctionProperties} ConfigurationProperties.
 * Then exposes a gRPC server adapting this function to the riff RPC protocol (muxing/de-muxing input and output values
 * over a single streaming channel). Marshalling and unmarshalling of byte encoded values is performed by Spring Cloud Function
 * itself, according to the incoming {@code Content-Type} header and the {@link StartFrame#getExpectedContentTypesList() expectedContentType} fields.
 *
 * @author Eric Bottard
 */
@SpringBootApplication
@EnableConfigurationProperties(FunctionProperties.class)
public class EntryPoint {

    @Value("#{systemEnvironment['GRPC_PORT'] ?: 8081}")
    private int grpcPort = 8081;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(EntryPoint.class, args);
        Object o = new Object();
        synchronized (o) {
            o.wait();
        }
    }

    @Bean
    public GrpcServerAdapter adapter(FunctionCatalog functionCatalog, FunctionProperties functionProperties) {
        return new GrpcServerAdapter(
                functionCatalog,
                functionProperties.getDefinition()
        );
    }

    @Bean
    public SmartLifecycle server(GrpcServerAdapter adapter) {
        Server server = ServerBuilder.forPort(grpcPort).addService(adapter).build();
        return new SmartLifecycle() {

            private volatile boolean running;

            @Override
            public void start() {
                try {
                    server.start();
                    running = true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void stop() {
                server.shutdown();
                running = false;
            }

            @Override
            public boolean isRunning() {
                return running;
            }
        };
    }

    /**
     * Install our own FunctionCatalog as we want stricter MessageConverters than the defaults and opting-out of them
     * is actually prettry complicated due to class-loading.
     */
    @Bean
    public FunctionRegistry functionCatalog(@Nullable ObjectMapper objectMapper) {
        ConversionService conversionService = new DefaultConversionService();

        CompositeMessageConverter messageConverter = new CompositeMessageConverter(Arrays.asList(
                new NegotiatingMappingJackson2MessageConverter(),
                new NegotiatingStringMessageConverter(),
                new NegotiatingByteArrayMessageConverter()
        ));
        return new BeanFactoryAwareFunctionRegistry(conversionService, messageConverter);
    }

}
