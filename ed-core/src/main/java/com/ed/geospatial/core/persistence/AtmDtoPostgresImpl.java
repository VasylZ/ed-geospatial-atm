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
import java.util.UUID;

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

    @Override
    public Atm create(final Atm atm) {
        if (isEmpty(atm.getId())) {
            atm.setId(UUID.randomUUID().toString());
        }

        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setAutoCommit(false);

            final String sql = "INSERT INTO atms (id, active, name, point, address, area_id, street_id, building_id, version) " +
                    "values (?, ?, ?, st_geomfromgeojson(?), ?, ?, ?, ?, ?) ";

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, atm.getId());
                statement.setBoolean(2, atm.isActive());
                statement.setString(3, atm.getName());
                statement.setString(4, PointMapper.pointToGeoJson(atm.getPoint()));
                statement.setString(5, atm.getAddress());
                statement.setInt(6, atm.getArea().getId());
                statement.setString(7, atm.getStreet().getId());
                statement.setString(8, atm.getBuilding() != null ? atm.getBuilding().getId() : null);
//                statement.setLong(8, atm.getVersion());
                statement.setLong(9, 0L); // force to set 0 as initial version value

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

            final String sql = "UPDATE atms SET " +
                    "active = ?, " +
                    "name = ?, " +
                    "point = st_geomfromgeojson(?), " +
                    "address = ?, " +
                    "area_id = ?, " +
                    "street_id = ?, " +
                    "building_id = ?, " +
                    "version = atms.version + 1 " +
                    "WHERE atms.id = ? "; // AND atms.version == ?

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setBoolean(1, atm.isActive());
                statement.setString(2, atm.getName());
                statement.setString(3, PointMapper.pointToGeoJson(atm.getPoint()));
                statement.setString(4, atm.getAddress());
                statement.setInt(5, atm.getArea().getId());
                statement.setString(6, atm.getStreet().getId());
                statement.setString(7, atm.getBuilding() != null ? atm.getBuilding().getId() : null);
                statement.setString(8, atm.getId());

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
