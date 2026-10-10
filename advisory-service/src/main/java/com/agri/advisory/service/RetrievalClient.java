package com.agri.advisory.service;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.ContextItem;
import java.util.List;

public interface RetrievalClient {
    List<ContextItem> retrieveContext(AdvisoryRequest request);
}
