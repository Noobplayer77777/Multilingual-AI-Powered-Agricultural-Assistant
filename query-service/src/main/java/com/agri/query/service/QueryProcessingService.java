package com.agri.query.service;

import com.agri.query.dto.QueryRequest;
import com.agri.query.dto.QueryResponse;

import java.util.List;

public interface QueryProcessingService {
    QueryResponse processQuery(QueryRequest request);
    List<QueryResponse> getHistory();
}
