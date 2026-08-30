package com.alphabetz.webalphabetz.config;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.render.SelectRenderContext;

@Configuration(proxyBeanMethods = false)
public class JdbcDialectConfig {

    @Bean
    public JdbcDialect jdbcDialect() {
        return new JdbcDialect() {
            @Override
            public LimitClause limit() {
                return AnsiDialect.INSTANCE.limit();
            }

            @Override
            public LockClause lock() {
                return AnsiDialect.INSTANCE.lock();
            }

            @Override
            public SelectRenderContext getSelectContext() {
                return AnsiDialect.INSTANCE.getSelectContext();
            }
        };
    }

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
                new UuidToStringConverter(),
                new StringToUuidConverter()
        ));
    }

    @WritingConverter
    static class UuidToStringConverter implements Converter<UUID, String> {
        @Override
        public String convert(UUID source) {
            return source == null ? null : source.toString();
        }
    }

    @ReadingConverter
    static class StringToUuidConverter implements Converter<String, UUID> {
        @Override
        public UUID convert(String source) {
            return source == null || source.isBlank() ? null : UUID.fromString(source);
        }
    }

}
