package com.github.charlemaznable.es.config;

import com.ctrip.framework.apollo.ConfigService;
import com.github.charlemaznable.core.es.EsConfig;
import com.google.common.base.Splitter;
import com.google.common.primitives.Primitives;
import lombok.NoArgsConstructor;
import lombok.val;
import org.n3r.diamond.client.Miner;
import org.n3r.eql.util.O;
import org.n3r.eql.util.O.ValueGettable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static org.n3r.diamond.client.impl.DiamondUtils.parseStoneToProperties;

@NoArgsConstructor(access = PRIVATE)
public final class EsConfigElf {

    public static final String ES_CONFIG_APOLLO_NAMESPACE = "EsConfig";
    public static final String ES_CONFIG_DIAMOND_GROUP_NAME = "EsConfig";

    public static String getApolloEsConfig(String propertyName) {
        return ConfigService.getConfig(ES_CONFIG_APOLLO_NAMESPACE)
                .getProperty(propertyName, "");
    }

    public static String getDiamondEsConfig(String dataId) {
        return new Miner().getStone(ES_CONFIG_DIAMOND_GROUP_NAME, dataId);
    }

    public static EsConfig parseConfigValueToEsConfig(String value) {
        val esConfig = new EsConfig();

        val properties = parseStoneToProperties(value);
        for (val prop : properties.entrySet()) {
            O.setValue(esConfig, Objects.toString(prop.getKey()), new ValueGettable() {
                @Override
                public Object getValue() {
                    return prop.getValue();
                }

                @SuppressWarnings("unchecked")
                @Override
                public Object getValue(Class<?> returnType) {
                    val value = Objects.toString(prop.getValue());
                    val rt = Primitives.unwrap(returnType);
                    if (rt == String.class) return value;
                    if (rt == Duration.class)
                        return Duration.ofSeconds(Long.parseLong(value));
                    if (rt == List.class)
                        return Splitter.on(",").omitEmptyStrings()
                                .trimResults().splitToList(value);
                    return null;
                }
            });
        }
        return esConfig;
    }
}
