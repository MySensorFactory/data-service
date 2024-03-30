package com.factory.persistence.elasticsearch.repository;

import com.factory.persistence.elasticsearch.model.ReportDataEsModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReportsEsRepository extends ElasticsearchRepository<ReportDataEsModel, String>, ReportsSearchEsRepository {

}
