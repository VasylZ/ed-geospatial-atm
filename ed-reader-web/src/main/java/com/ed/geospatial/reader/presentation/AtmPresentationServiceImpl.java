package com.ed.geospatial.reader.presentation;

import com.ed.geospatial.core.persistence.AtmDto;
import com.ed.geospatial.core.persistence.AtmQuery;
import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.core.persistence.model.ResultLine;
import com.ed.geospatial.core.versioning.AtmState;
import com.ed.geospatial.core.versioning.AtmVersioningService;
import com.ed.geospatial.reader.shared.ResponseData;
import com.google.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

public class AtmPresentationServiceImpl implements AtmPresentationService {

    @Inject
    private AtmDto persistence;
    @Inject
    private AtmVersioningService versioningService;

    @Override
    public AtmVO get(String id) {
        Atm atm = persistence.get(id);
        return toVO(atm);
    }

    @Override
    public ResponseData<ResultLine<AtmVO>> find(AtmQuery query) {
        final List<ResultLine<AtmVO>> result = persistence.find(query)
                .stream().map(rl -> {
                    final ResultLine<AtmVO> voRl = new ResultLine<>();
                    voRl.setAtm(toVO(rl.getAtm()));
                    voRl.setDistance(rl.getDistance());
                    return voRl;
                }).collect(Collectors.toList());

        if (query.getLimit() == 0 && query.getOffset() == 0 && result.size() < query.getLimit()) {
            return new ResponseData<>(result.size(), result);
        }
        int total = (int) persistence.count(query);
        return new ResponseData<>(total, result);
    }

    private AtmVO toVO(final Atm atm) {
        if (atm == null) {
            return null;
        }
        AtmState state = versioningService.get(atm.getId());

        AtmVO vo = new AtmVO(atm);
        vo.setStale(!(state == null || state.getLatestCommittedVersion() == atm.getVersion() && state.getInSyncVersion() == 0));
        return vo;
    }
}
