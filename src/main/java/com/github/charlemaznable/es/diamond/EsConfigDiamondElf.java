package com.github.charlemaznable.es.diamond;

import com.google.common.base.Splitter;
import com.google.common.primitives.Primitives;
import lombok.val;
import org.n3r.diamond.client.Miner;
import org.n3r.eql.util.O;
import org.n3r.eql.util.O.ValueGettable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.n3r.diamond.client.impl.DiamondUtils.parseStoneToProperties;

public final class EsConfigDiamondElf {

    public static final String ES_CONFIG_GROUP_NAME = "EsConfig";

    public static String getEsConfigStone(String dataId) {
        return new Miner().getStone(ES_CONFIG_GROUP_NAME, dataId);
    }

    public static EsConfig parseStoneToEsConfig(String stone) {
        val esConfig = new EsConfig();

        val properties = parseStoneToProperties(stone);
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

    private EsConfigDiamondElf() {}
}
