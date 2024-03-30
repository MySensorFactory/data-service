package com.factory.persistence.elasticsearch.repository;

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

import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class ReportsSearchEsRepositoryImpl implements ReportsSearchEsRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    //TODO: refactor
    public SearchPage<ReportDataEsModel> search(Pageable pageable, Filter filter) {
        BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();

        // Add keyword filters if any.
        if (filter.getKeywords() != null) {
            for (Map.Entry<String, String> entry : filter.getKeywords().entrySet()) {
                keywordQuery.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
            }
        }

        // Create query for text fields.
        var stringQueryBuilder = QueryBuilders.queryStringQuery(filter.getTextQuery())
                .lenient(Boolean.TRUE);
        filter.getTextFields().forEach(stringQueryBuilder::field);

        // Combine keyword and text queries into a final bool query.
        var finalQuery = QueryBuilders.boolQuery()
                .must(keywordQuery)
                .must(stringQueryBuilder);

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

        // Define search query.
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        // Execute the query.
        SearchHits<ReportDataEsModel> searchHits = elasticsearchOperations.search(
                searchQuery, ReportDataEsModel.class, IndexCoordinates.of("report"));

        // Return the results as a page.
        return SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
    }
}