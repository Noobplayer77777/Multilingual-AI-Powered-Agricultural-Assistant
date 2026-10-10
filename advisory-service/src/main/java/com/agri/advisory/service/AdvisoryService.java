package com.agri.advisory.service;

import com.agri.advisory.dto.AdvisoryRequest;
import com.agri.advisory.dto.AdvisoryResponse;

public interface AdvisoryService {
    AdvisoryResponse generateAdvisory(AdvisoryRequest request);
}
