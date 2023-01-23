package com.ed.geospatial.core.persistence;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.policy.CommitLevel;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.IndexType;
import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.core.persistence.model.PointMapper;
import com.ed.geospatial.core.persistence.model.ResultLine;
import com.ed.geospatial.core.shared.ConfUtils;
import com.ed.geospatial.core.versioning.aerospike.AbstractAerospikeClient;
import com.typesafe.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class AtmDtoAerospikeImpl extends AbstractAerospikeClient implements AtmDto {

    private static final String PERSISTENCE_CONFIG_FILE = "aerospike-atm-persistence.json";

    private static final Config config = ConfUtils.load(PERSISTENCE_CONFIG_FILE);

    private static final String ID = "g_id";
    private static final String ACTIVE = "g_ctive";
    private static final String NAME = "g_name";
    private static final String POINT = "g_point";
    private static final String AREA = "g_area";
    private static final String ADDRESS = "g_address";
    private static final String STREET = "g_street";
    private static final String BUILDING = "g_building";
    private static final String VERSION = "g_version";

    private static final String[] QUERY_BINS = new String[] {
            ID, ACTIVE, NAME, POINT, AREA, STREET, ADDRESS, BUILDING, VERSION
    };

    private final QueryPolicy queryPolicy;
    private final WritePolicy writePolicy;

    public AtmDtoAerospikeImpl() {
        super(config.getString("aNamespace"), config.getString("aSet"));

        createIndex(IndexType.GEO2DSPHERE, "point_index", POINT);

        this.queryPolicy = new QueryPolicy();
        this.queryPolicy.totalTimeout = 1_000; // is too much?
        this.queryPolicy.maxRetries = 1;

        this.writePolicy = new WritePolicy();
        this.writePolicy.totalTimeout = 1_000;
        this.writePolicy.maxRetries = 1;
        this.writePolicy.expiration = -1; // newer expire
        this.writePolicy.commitLevel = CommitLevel.COMMIT_ALL;  // ensure strong consistency
//        this.writePolicy.generationPolicy = GenerationPolicy.EXPECT_GEN_EQUAL;  // ensure strong consistency // Enterprise only
    }

    @Override
    public Atm get(String id) {
        return fromRecord(client.get(queryPolicy, toKey(id), QUERY_BINS));
    }

    @Override
    public List<ResultLine<Atm>> find(AtmQuery query) {
        final Filter filter = queryToFilter(query);
        List<Atm> atms = executeQueryAndGetResults(queryPolicy, filter, this::fromRecord, query.getOffset(), query.getLimit(), QUERY_BINS);

        return atms.stream().filter(Objects::nonNull).map(atm -> {
            ResultLine<Atm> r = new ResultLine<>();
            r.setAtm(atm);
            return r;
        }).collect(Collectors.toList());
    }

    private Filter queryToFilter(AtmQuery query) {
        final double radius = query.getRadius();
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius value can not be negative.");
        }
        if (radius > 1_000_000) {
            throw new IllegalArgumentException("Radius value can not be higher than 1m.");
        }

//        final String geoJson = PointMapper.pointToGeoJson(query.getPoint());
//        Filter filter = Filter.geoContains(POINT, geoJson);

        return Filter.geoWithinRadius(POINT, query.getPoint().getX(), query.getPoint().getY(), radius);
    }

    @Override
    public long count(AtmQuery query) {
        final Filter filter = queryToFilter(query);
        return executeQueryAndCount(queryPolicy, filter, ID);
    }

    @Override
    public List<Atm> create(List<Atm> atms) {
        // todo fix me
        atms.forEach(this::create);
        return atms;
    }

    @Override
    public Atm create(Atm atm) {
        atm.setId(UUID.randomUUID().toString());

        client.put(writePolicy, toKey(atm.getId()), toBins(atm));
        return get(atm.getId());
    }

    @Override
    public Atm update(Atm atm) {
        client.put(writePolicy, toKey(atm.getId()), toBins(atm, atm.getVersion() + 1));
        return get(atm.getId());
    }

    @Override
    public void remove(String id) {
        client.delete(writePolicy, toKey(id));
    }

    private static Bin[] toBins(final Atm atm) {
        return toBins(atm, atm.getVersion());
    }

    private static Bin[] toBins(final Atm atm, long version) {
        final List<Bin> bins = new ArrayList<>();
        bins.add(new Bin(ID, atm.getId()));
        bins.add(new Bin(NAME, atm.getName()));
        bins.add(new Bin(ADDRESS, atm.getAddress()));
        bins.add(new Bin(ACTIVE, atm.isActive()));
        bins.add(new Bin(POINT, Value.getAsGeoJSON(PointMapper.pointToGeoJson(atm.getPoint()))));
        if (atm.getArea() != null) {
            bins.add(new Bin(AREA, atm.getArea().getId()));
        }
        if (atm.getStreet() != null) {
            bins.add(new Bin(STREET, atm.getStreet().getId()));
        }
        if (atm.getBuilding() != null) {
            bins.add(new Bin(BUILDING, atm.getBuilding().getId()));
        }
        bins.add(new Bin(VERSION, version));
        return bins.toArray(new Bin[0]);
    }

    private Key toKey(final String id) {
        return new Key(namespace, set, id);
    }

    private Atm fromRecord(final Record rs) {
        if (rs == null) {
            return null;
        }
        return Atm.builder()
                .id(rs.getString(ID))
                .active(rs.getBoolean(ACTIVE))
                .name(rs.getString(NAME))
                .point(PointMapper.toPoint(rs.getGeoJSON(POINT)))
                .address(rs.getString(ADDRESS))
                .version(rs.getLong(VERSION))
                .build();
    }
}
