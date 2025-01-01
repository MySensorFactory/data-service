package com.factory.persistence.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import com.factory.config.dto.EsConfig;
import com.factory.domain.Filter;
import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class ReportsSearchEsRepositoryImpl implements ReportsSearchEsRepository {
    private final EsConfig esConfig;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchPage<ReportDataEsModel> search(final Pageable pageable, final Filter filter) {
        BoolQuery finalQuery = buildFinalQuery(filter);

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(finalQuery))
                .withPageable(pageable)
                .build();

        SearchHits<ReportDataEsModel> searchHits = elasticsearchOperations.search(
                searchQuery,
                ReportDataEsModel.class,
                IndexCoordinates.of(esConfig.getIndexName())
        );

        return SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
    }

    private BoolQuery buildFinalQuery(final Filter filter) {
        List<Query> mustQueries = new ArrayList<>();

        // Add keyword queries
        BoolQuery keywordQuery = buildKeywordQuery(filter);
        mustQueries.add(Query.of(q -> q.bool(keywordQuery)));

        // Add text query
        QueryStringQuery stringQuery = QueryStringQuery.of(q -> q
                .query(filter.getTextQuery())
                .lenient(true)
                .fields(filter.getTextFields())
        );
        mustQueries.add(Query.of(q -> q.queryString(stringQuery)));

        // Add date range queries
        addDateTimeRangeQueries(filter, mustQueries);

        return BoolQuery.of(b -> b
                .must(mustQueries)
        );
    }

    private void addDateTimeRangeQueries(final Filter filter, List<Query> mustQueries) {
        if (Objects.nonNull(filter.getFrom())) {
            RangeQuery fromRangeQuery = RangeQuery.of(r -> r
//                    .field("from")
//                    .gte(JsonData.of(filter.getFrom()))
            );
            mustQueries.add(Query.of(q -> q.range(fromRangeQuery)));
        }

        if (Objects.nonNull(filter.getTo())) {
            RangeQuery toRangeQuery = RangeQuery.of(r -> r
//                    .field("to")
//                    .lte(JsonData.of(filter.getTo()))
            );
            mustQueries.add(Query.of(q -> q.range(toRangeQuery)));
        }
    }

    private BoolQuery buildKeywordQuery(final Filter filter) {
        List<Query> mustQueries = new ArrayList<>();

//        if (Objects.nonNull(filter.getKeywords())) {
//            filter.getKeywords().forEach((key, value) -> {
//                TermQuery termQuery = TermQuery.of(t -> t
//                        .field(key)
//                        .value(value)
//                );
//                mustQueries.add(Query.of(q -> q.term(termQuery)));
//            });
//        }

        return BoolQuery.of(b -> b
                .must(mustQueries)
        );
    }
}