package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.core.persistence.model.PointMapper;
import com.ed.geospatial.core.persistence.model.ResultLine;
import com.ed.geospatial.core.postgres.PostgresConnectionPool;
import com.google.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class AtmDtoPostgresImpl implements AtmDto {

    @Inject
    private PostgresConnectionPool connectionPool;

    @Override
    public Atm get(String id) {
        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setReadOnly(true);

            final String sql = "SELECT id, active, name, st_asgeojson(point) as point, address, version " +
                    " FROM atms WHERE atms.id = ? ";

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, id);

                try (final ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? fromRS(rs) : null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ResultLine<Atm>> find(AtmQuery query) {
        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setReadOnly(true);

            final String sql = "SELECT id, active, name, st_asgeojson(point) as point, address, " +
                    "st_distance(atms.point::geography, st_geomfromgeojson(?)::geography) as distance, " +
                    "version " +
                    "FROM atms " +
                    "WHERE st_distance(atms.point::geography, st_geomfromgeojson(?)::geography) <= ? " +
                    (query.getLimit() != 0 ? "OFFSET ? LIMIT ? " : " ");

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, PointMapper.pointToGeoJson(query.getPoint()));
                statement.setString(2, PointMapper.pointToGeoJson(query.getPoint()));
                statement.setInt(3, query.getRadius());
                if (query.getLimit() != 0) {
                    statement.setInt(4, query.getOffset());
                    statement.setInt(5, query.getLimit());
                }

                try (final ResultSet rs = statement.executeQuery()) {
                    final List<ResultLine<Atm>> result = new ArrayList<>();
                    while (rs.next()) {
                        ResultLine<Atm> r = new ResultLine();
                        r.setAtm(fromRS(rs));
                        r.setDistance(rs.getInt("distance"));
                        result.add(r);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long count(AtmQuery query) {
        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setReadOnly(true);

            final String sql = "SELECT count(*) as count " +
                    "FROM atms " +
                    "WHERE st_distance(atms.point::geography, st_geomfromgeojson(?)::geography) <= ? ";

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, PointMapper.pointToGeoJson(query.getPoint()));
                statement.setInt(2, query.getRadius());

                try (final ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    return rs.getLong("count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String createKey(Atm atm) {
        final String areaId = atm.getArea() != null ? "area_id_" : "";
        final String streetId = atm.getStreet() != null ? "street_id_" : "";
        final String buildingId = atm.getBuilding() != null ? "building_id_" : "";
        return areaId + streetId + buildingId;
    }

    @Override
    public List<Atm> create(List<Atm> atms) {
        return null;
//        atms.forEach(atm -> atm.setId(UUID.randomUUID().toString()));
//
//        final Map<String, List<Atm>> map = atms.stream().collect(Collectors.groupingBy(this::createKey));
//
//        try (final Connection conn = connectionPool.getDataSource()) {
//            conn.setAutoCommit(false);
//
//            map.forEach((k, toSaveList) -> {
//                if (toSaveList.isEmpty()) return;
//
//                Atm atm = toSaveList.get(0);
//                final String areaId = atm.getArea() != null ? "area_id, " : "";
//                final String streetId = atm.getStreet() != null ? "street_id, " : "";
//                final String buildingId = atm.getBuilding() != null ? "building_id, " : "";
//
//                final String sql = "INSERT INTO atms (id, active, name, point, address, " +
//                        areaId +
//                        streetId +
//                        buildingId +
//                        "version) " +
//
//                toSaveList.
//
//                final String areaId = atm.getArea() != null ? "area_id, " : "";
//                final String streetId = atm.getStreet() != null ? "street_id, " : "";
//                final String buildingId = atm.getBuilding() != null ? "building_id, " : "";
//
//
//                        "values (?, ?, ?, st_geomfromgeojson(?), ?, " +
//                        (atm.getArea() != null ? "?, " : "") +
//                        (atm.getStreet() != null ? "?, " : "") +
//                        (atm.getBuilding() != null ? "?, " : "") +
//                        "?) ";
//
//                try (final PreparedStatement statement = conn.prepareStatement(sql)) {
//                    final AtomicInteger paramIndex = new AtomicInteger(0);
//                    statement.setString(paramIndex.incrementAndGet(), atm.getId());
//                    statement.setBoolean(paramIndex.incrementAndGet(), atm.isActive());
//                    statement.setString(paramIndex.incrementAndGet(), atm.getName());
//                    statement.setString(paramIndex.incrementAndGet(), PointMapper.pointToGeoJson(atm.getPoint()));
//                    statement.setString(paramIndex.incrementAndGet(), atm.getAddress());
//                    if (atm.getArea() != null) {
//                        statement.setInt(paramIndex.incrementAndGet(), atm.getArea().getId());
//                    }
//                    if (atm.getStreet() != null) {
//                        statement.setString(paramIndex.incrementAndGet(), atm.getStreet().getId());
//                    }
//                    if (atm.getBuilding() != null) {
//                        statement.setString(paramIndex.incrementAndGet(), atm.getBuilding() != null ? atm.getBuilding().getId() : null);
//                    }
//                    statement.setLong(paramIndex.incrementAndGet(), 0L); // force to set 0 as initial version value
//
//                    statement.executeUpdate();
//
//
//                    return get(atm.getId());
//                }
//
//            });
//            conn.commit();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public Atm create(final Atm atm) {
        if (isEmpty(atm.getId())) {
            atm.setId(UUID.randomUUID().toString());
        }

        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setAutoCommit(false);

            final String areaId = atm.getArea() != null ? "area_id, " : "";
            final String streetId = atm.getStreet() != null ? "street_id, " : "";
            final String buildingId = atm.getBuilding() != null ? "building_id, " : "";

            final String sql = "INSERT INTO atms (id, active, name, point, address, " +
                    areaId +
                    streetId +
                    buildingId +
                    "version) " +
                    "values (?, ?, ?, st_geomfromgeojson(?), ?, " +
                    (atm.getArea() != null ? "?, " : "") +
                    (atm.getStreet() != null ? "?, " : "") +
                    (atm.getBuilding() != null ? "?, " : "") +
                    "?) ";

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                final AtomicInteger paramIndex = new AtomicInteger(0);
                statement.setString(paramIndex.incrementAndGet(), atm.getId());
                statement.setBoolean(paramIndex.incrementAndGet(), atm.isActive());
                statement.setString(paramIndex.incrementAndGet(), atm.getName());
                statement.setString(paramIndex.incrementAndGet(), PointMapper.pointToGeoJson(atm.getPoint()));
                statement.setString(paramIndex.incrementAndGet(), atm.getAddress());
                if (atm.getArea() != null) {
                    statement.setInt(paramIndex.incrementAndGet(), atm.getArea().getId());
                }
                if (atm.getStreet() != null) {
                    statement.setString(paramIndex.incrementAndGet(), atm.getStreet().getId());
                }
                if (atm.getBuilding() != null) {
                    statement.setString(paramIndex.incrementAndGet(), atm.getBuilding() != null ? atm.getBuilding().getId() : null);
                }
                statement.setLong(paramIndex.incrementAndGet(), 0L); // force to set 0 as initial version value

                statement.executeUpdate();
                conn.commit();

                return get(atm.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Atm update(Atm atm) {
        if (isEmpty(atm.getId())) {
            throw new IllegalArgumentException("id is required to execute update");
        }

        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setAutoCommit(false);

            final String areaUpdate = atm.getArea() != null ? "area_id = ?, " : "";
            final String streetUpdate = atm.getStreet() != null ? "street_id = ?, " : "";
            final String buildingUpdate = atm.getStreet() != null ? "building_id = ?, " : "";

            final String sql = "UPDATE atms SET " +
                    "active = ?, " +
                    "name = ?, " +
                    "point = st_geomfromgeojson(?), " +
                    "address = ?, " +
                    areaUpdate +
                    streetUpdate +
                    buildingUpdate +
                    "version = atms.version + 1 " +
                    "WHERE atms.id = ? "; // AND atms.version == ?

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                final AtomicInteger paramIndex = new AtomicInteger(0);
                statement.setBoolean(paramIndex.incrementAndGet(), atm.isActive());
                statement.setString(paramIndex.incrementAndGet(), atm.getName());
                statement.setString(paramIndex.incrementAndGet(), PointMapper.pointToGeoJson(atm.getPoint()));
                statement.setString(paramIndex.incrementAndGet(), atm.getAddress());
                if (atm.getArea() != null) {
                    statement.setInt(paramIndex.incrementAndGet(), atm.getArea().getId());
                }
                if (atm.getStreet() != null) {
                    statement.setString(paramIndex.incrementAndGet(), atm.getStreet().getId());
                }
                if (atm.getBuilding() != null) {
                    statement.setString(paramIndex.incrementAndGet(), atm.getBuilding() != null ? atm.getBuilding().getId() : null);
                }
                statement.setString(paramIndex.incrementAndGet(), atm.getId());

                statement.executeUpdate();
                conn.commit();

                return get(atm.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(String id) {
        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setAutoCommit(false);

            final String sql = "DELETE FROM atms WHERE atms.id = ? ";

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, id);

                statement.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Atm fromRS(final ResultSet rs) throws SQLException {
        return Atm.builder()
                .id(rs.getString("id"))
                .active(rs.getBoolean("active"))
                .name(rs.getString("name"))
                .point(PointMapper.toPoint(rs.getString("point")))
                .address(rs.getString("address"))
                .version(rs.getLong("version"))
                .build();
    }
}
