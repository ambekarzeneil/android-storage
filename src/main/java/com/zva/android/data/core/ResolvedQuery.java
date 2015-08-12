package com.zva.android.data.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.zva.android.commonLib.utils.CollectionUtils;
import com.zva.android.commonLib.utils.core.Transformer;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 06/08/15.
 */
public class ResolvedQuery {

    private final String       query;
    private final List<Object> params;

    public ResolvedQuery(String query, List<Object> params) {
        this.query = query;
        this.params = Collections.unmodifiableList(params);
    }

    public String getQuery() {
        return query;
    }

    public String[] getParams() {
        return CollectionUtils.map(params, new Transformer<Object, String>() {
            @Override
            public String transform(Object input) {
                return input.toString();
            }
        }).toArray(new String[params.size()]);
    }

    static class Builder {
        private StringBuilder query  = new StringBuilder();
        private List<Object>  params = new ArrayList<>();

        private Builder() {
        }

        @Contract(pure = true)
        @NotNull
        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder addResolvedQuery(ResolvedQuery resolvedQuery, boolean and) {
            query.append(resolvedQuery.query).append(and ? " AND " : " OR ");
            params.addAll(resolvedQuery.params);
            return this;
        }

        public ResolvedQuery build(boolean and) {
            return new ResolvedQuery(String.format("(%s)", query.substring(0, query.length() - (and ? 5 : 4))), params);
        }

    }

}
