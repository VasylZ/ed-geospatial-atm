package com.ed.geospatial.core.versioning;

import java.util.concurrent.Callable;

public interface AtmVersioningService {

    AtmState get(String id);

    AtmState save(String id, long currentVersion);

    AtmState initUpdateIfMatchVersion(String id, long updateDocumentVersion, Callable<Boolean> updateFunc);

    AtmState commitUpdate(String id, long currentVersion);
}
