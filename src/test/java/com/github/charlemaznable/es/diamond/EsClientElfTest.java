package com.github.charlemaznable.es.diamond;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.val;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.charlemaznable.es.diamond.EsClientElf.buildEsClient;
import static com.github.charlemaznable.es.diamond.EsClientElf.closeEsClient;
import static com.google.common.collect.Lists.newArrayList;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EsClientElfTest {

    private static final String ELASTICSEARCH_VERSION = "7.15.2";
    private static final DockerImageName ELASTICSEARCH_IMAGE = DockerImageName
            .parse("docker.elastic.co/elasticsearch/elasticsearch")
            .withTag(ELASTICSEARCH_VERSION);

    private static final String ELASTICSEARCH_USERNAME = "elastic";
    private static final String ELASTICSEARCH_PASSWORD = "changeme";

    @SneakyThrows
    @Test
    public void testEsClient() {
        try (val elasticsearch = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
                .withPassword(ELASTICSEARCH_PASSWORD)) {
            elasticsearch.start();

            val esConfig = new EsConfig();
            esConfig.setUris(newArrayList(elasticsearch.getHttpHostAddress()));
            esConfig.setUsername(ELASTICSEARCH_USERNAME);
            esConfig.setPassword(ELASTICSEARCH_PASSWORD);
            val esClient = buildEsClient(esConfig);

            val createIndexRequest = new CreateIndexRequest("twitter");
            val createIndexResponse = esClient.indices()
                    .create(createIndexRequest, DEFAULT);
            assertTrue(createIndexResponse.isAcknowledged());
            assertTrue(createIndexResponse.isShardsAcknowledged());

            val openIndexRequest = new OpenIndexRequest("twitter");
            val openIndexResponse = esClient.indices().open(openIndexRequest, DEFAULT);
            assertTrue(openIndexResponse.isAcknowledged());
            assertTrue(openIndexResponse.isShardsAcknowledged());

            val indexRequest = new IndexRequest("twitter").id("1");
            val sourceMap = Maps.<String, Object>newHashMap();
            sourceMap.put("user", "kimchy");
            sourceMap.put("postDate", new Date());
            sourceMap.put("message", "trying out Elasticsearch");
            indexRequest.source(sourceMap, XContentType.JSON);
            val indexResponse = esClient.index(indexRequest, DEFAULT);
            assertEquals("twitter", indexResponse.getIndex());
            assertEquals("1", indexResponse.getId());

            val getRequest = new GetRequest("twitter", "1");
            val getResponse = esClient.get(getRequest, DEFAULT);
            assertEquals("twitter", getResponse.getIndex());
            assertEquals("1", getResponse.getId());
            assertTrue(getResponse.isExists());
            val responseMap = getResponse.getSourceAsMap();
            assertEquals(sourceMap.get("user"), responseMap.get("user"));
            assertEquals(sourceMap.get("postDate"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                            .parse(responseMap.get("postDate").toString()));
            assertEquals(sourceMap.get("message"), responseMap.get("message"));

            closeEsClient(esClient);
        }
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> closeEsClient(null));
    }
}
