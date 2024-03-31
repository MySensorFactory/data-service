package com.factory.persistence.elasticsearch.repository;

import com.factory.config.dto.EsConfig;
import com.factory.domain.Filter;
import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.Objects;

@RequiredArgsConstructor
public class ReportsSearchEsRepositoryImpl implements ReportsSearchEsRepository {
    private final EsConfig esConfig;

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchPage<ReportDataEsModel> search(final Pageable pageable, final Filter filter) {
        BoolQueryBuilder finalQuery = buildFinalQuery(filter);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<ReportDataEsModel> searchHits = elasticsearchOperations.search(
                searchQuery, ReportDataEsModel.class, IndexCoordinates.of(esConfig.getIndexName()));

        return SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
    }

    private BoolQueryBuilder buildFinalQuery(final Filter filter) {
        BoolQueryBuilder keywordQuery = buildKeywordQuery(filter);

        var stringQueryBuilder = QueryBuilders.queryStringQuery(filter.getTextQuery())
                .lenient(Boolean.TRUE);
        filter.getTextFields().forEach(stringQueryBuilder::field);

        BoolQueryBuilder finalQuery = QueryBuilders.boolQuery()
                .must(keywordQuery)
                .must(stringQueryBuilder);

        addDateTimeRangeQueries(filter, finalQuery);

        return finalQuery;
    }

    private static void addDateTimeRangeQueries(final Filter filter, final BoolQueryBuilder finalQuery) {
        if (Objects.nonNull(filter.getFrom())) {
            RangeQueryBuilder fromTimeRangeQueryBuilder = QueryBuilders.rangeQuery("from")
                    .gte(filter.getFrom());
            finalQuery.must(fromTimeRangeQueryBuilder);
        }
        if (Objects.nonNull(filter.getTo())) {
            RangeQueryBuilder toTimeRangeQueryBuilder = QueryBuilders.rangeQuery("to")
                    .lte(filter.getTo());
            finalQuery.must(toTimeRangeQueryBuilder);
        }
    }

    private BoolQueryBuilder buildKeywordQuery(final Filter filter) {
        BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
        if (Objects.nonNull(filter.getKeywords())) {
            filter.getKeywords().forEach((key, value) -> keywordQuery.must(QueryBuilders.termQuery(key, value)));
        }
        return keywordQuery;
    }
}