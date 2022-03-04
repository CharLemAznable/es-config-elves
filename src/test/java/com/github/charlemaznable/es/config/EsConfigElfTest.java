package com.github.charlemaznable.es.config;

import com.github.charlemaznable.apollo.MockApolloServer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.time.Duration;

import static com.github.charlemaznable.es.config.EsConfigElf.ES_CONFIG_DIAMOND_GROUP_NAME;
import static com.github.charlemaznable.es.config.EsConfigElf.getApolloEsConfig;
import static com.github.charlemaznable.es.config.EsConfigElf.getDiamondEsConfig;
import static com.github.charlemaznable.es.config.EsConfigElf.parseConfigValueToEsConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EsConfigElfTest {

    @Test
    public void testEsConfigElfInApollo() {
        MockApolloServer.setUpMockServer();

        val configProperty = getApolloEsConfig("default");
        assertConfigValue(configProperty);

        MockApolloServer.tearDownMockServer();
    }

    @Test
    public void testEsConfigElfInDiamond() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo(ES_CONFIG_DIAMOND_GROUP_NAME, "DEFAULT", "" +
                "uris=http://localhost:9200,http://localhost:9201\n" +
                "username=username\n" +
                "password=pa55wOrd\n" +
                "connectionTimeout=5\n" +
                "socketTimeout=60\n");

        val configStone = getDiamondEsConfig("DEFAULT");
        assertConfigValue(configStone);

        MockDiamondServer.tearDownMockServer();
    }

    private void assertConfigValue(String configValue) {
        assertNotNull(configValue);
        val esConfig = parseConfigValueToEsConfig(configValue);
        val uris = esConfig.getUris();
        assertEquals(2, uris.size());
        assertTrue(uris.contains("http://localhost:9200"));
        assertTrue(uris.contains("http://localhost:9201"));
        assertEquals("username", esConfig.getUsername());
        assertEquals("pa55wOrd", esConfig.getPassword());
        assertEquals(Duration.ofSeconds(5), esConfig.getConnectionTimeout());
        assertEquals(Duration.ofSeconds(60), esConfig.getSocketTimeout());
        assertNull(esConfig.getPathPrefix());
    }
}
