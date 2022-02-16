package com.github.charlemaznable.es.diamond;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.time.Duration;

import static com.github.charlemaznable.es.diamond.EsConfigDiamondElf.ES_CONFIG_GROUP_NAME;
import static com.github.charlemaznable.es.diamond.EsConfigDiamondElf.getEsConfigStone;
import static com.github.charlemaznable.es.diamond.EsConfigDiamondElf.parseStoneToEsConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EsConfigDiamondElfTest {

    @Test
    public void testEsConfigDiamondElf() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo(ES_CONFIG_GROUP_NAME, "DEFAULT", "" +
                "uris=http://localhost:9200,http://localhost:9201\n" +
                "username=username\n" +
                "password=pa55wOrd\n" +
                "connectionTimeout=5\n" +
                "socketTimeout=60\n");

        val configStone = getEsConfigStone("DEFAULT");
        assertNotNull(configStone);

        val esConfig = parseStoneToEsConfig(configStone);
        val uris = esConfig.getUris();
        assertEquals(2, uris.size());
        assertTrue(uris.contains("http://localhost:9200"));
        assertTrue(uris.contains("http://localhost:9201"));
        assertEquals("username", esConfig.getUsername());
        assertEquals("pa55wOrd", esConfig.getPassword());
        assertEquals(Duration.ofSeconds(5), esConfig.getConnectionTimeout());
        assertEquals(Duration.ofSeconds(60), esConfig.getSocketTimeout());
        assertNull(esConfig.getPathPrefix());

        MockDiamondServer.tearDownMockServer();
    }
}
