package io.schultz.dustin;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;

import static java.lang.String.format;
import static java.net.URI.create;
import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create;
import static software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create;
import static software.amazon.awssdk.regions.Region.US_WEST_2;

@Testcontainers
@Slf4j
public class LocalstackKinesisTest {

    private static final DockerImageName LOCALSTACK = DockerImageName.parse("localstack/localstack");
    private static final int LOCALSTACK_PORT = 4566;
    private static final String STREAM_NAME = "test-stream";

    @Container
    private GenericContainer localstack = new GenericContainer(LOCALSTACK).withExposedPorts(LOCALSTACK_PORT);

    @Test
    public void localStackKinesisTest() {
        KinesisClient kinesisClient = KinesisClient
                .builder()
                .credentialsProvider(create(create("foo", "bar")))
                .endpointOverride(create(format("http://localhost:%d", localstack.getMappedPort(LOCALSTACK_PORT))))
                .region(US_WEST_2)
                .build();

        CreateStreamRequest streamReq = CreateStreamRequest.builder()
                .streamName(STREAM_NAME)
                .shardCount(1)
                .build();

        kinesisClient.createStream(streamReq);
        ListStreamsResponse listStreamsResponse = kinesisClient.listStreams();
        assertThat(listStreamsResponse.streamNames()).contains(STREAM_NAME);
    }

}
