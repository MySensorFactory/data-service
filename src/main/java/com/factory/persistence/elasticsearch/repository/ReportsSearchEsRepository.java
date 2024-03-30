package com.factory.persistence.elasticsearch.repository;

import com.factory.domain.Filter;
import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;

public interface ReportsSearchEsRepository {
    SearchPage<ReportDataEsModel> search(Pageable pageable, Filter filter);
}
