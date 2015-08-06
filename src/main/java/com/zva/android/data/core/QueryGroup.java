package com.zva.android.data.core;

import java.util.Set;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 02/08/15.
 */
public class QueryGroup implements IQueryResolver {

    Set<Query<?>>   queries;

    Set<QueryGroup> queryGroups;

    boolean         and;

    private QueryGroup() {
    }

    public static QueryGroup withQueries(Set<Query<?>> queries, boolean and) {
        QueryGroup queryGroup = new QueryGroup();
        queryGroup.queries = queries;
        queryGroup.and = and;
        return queryGroup;
    }

    public static QueryGroup withQueryGroups(Set<QueryGroup> queryGroups, boolean and) {
        QueryGroup queryGroup = new QueryGroup();
        queryGroup.queryGroups = queryGroups;
        queryGroup.and = and;
        return queryGroup;
    }

    @Override
    public ResolvedQuery resolveQuery() {

        if (queryGroups == null && queries == null)
            return null;

        ResolvedQuery.Builder builder = ResolvedQuery.Builder.newBuilder();

        for (IQueryResolver queryResolver : queryGroups == null ? queries : queryGroups) {
            builder.addResolvedQuery(queryResolver.resolveQuery(), and);
        }

        return builder.build();
    }

    public Set<Query<?>> getQueries() {
        return queries;
    }

    public QueryGroup setQueries(Set<Query<?>> queries) {
        this.queries = queries;
        return this;
    }

    public Set<QueryGroup> getQueryGroups() {
        return queryGroups;
    }

    public QueryGroup setQueryGroups(Set<QueryGroup> queryGroups) {
        this.queryGroups = queryGroups;
        return this;
    }

    public boolean isAnd() {
        return and;
    }

    public QueryGroup setAnd(boolean and) {
        this.and = and;
        return this;
    }
}
