import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.time.Duration;
import java.util.Arrays;

public class MetricsExample {
    public static void main(String[] args) throws InterruptedException {
        OtlpGrpcMetricExporter exporter = OtlpGrpcMetricExporter.builder().setEndpoint("http://lingchutest.cn-hangzhou.log.aliyuncs.com:10010")
            .addHeader("x-sls-otel-project", "lingchutest")
            .addHeader("x-sls-otel-instance-id", "lingchu-metricstest")
            .addHeader("x-sls-otel-ak-id", "AccessKey")
            .addHeader("x-sls-otel-ak-secret", "SecretKey")
            .setTimeout(Duration.ofSeconds(3)).build();

        InstrumentSelector instrumentSelector = InstrumentSelector.builder()
            .setType(InstrumentType.HISTOGRAM)
            .build();

        final View view = View.builder()
            .setAggregation(
                Aggregation.explicitBucketHistogram(Arrays.asList(0.1d, 1d, 10d, 30d, 60d)))
            .setName("my-view-name")
            .setDescription("my-view-description")
            .build();

        PeriodicMetricReader reader = PeriodicMetricReader.builder(exporter).setInterval(Duration.ofSeconds(3)).build();
        final SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader)
            .registerView(instrumentSelector, view)
            .build();
        final OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().setMeterProvider(provider).build();
        final Meter meter = openTelemetry.getMeter("lingchu-test");

        final DoubleHistogram hh = meter.histogramBuilder("lingchuhistogram").build();
        hh.record(0.5);

        final LongCounter counter = meter.counterBuilder("lingchucounter").build();
        counter.add(1);
        Thread.sleep(100000000);
    }
}
