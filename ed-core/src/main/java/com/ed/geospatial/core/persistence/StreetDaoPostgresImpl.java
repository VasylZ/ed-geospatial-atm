package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.persistence.model.PointMapper;
import com.ed.geospatial.core.persistence.model.Street;
import com.ed.geospatial.core.postgres.PostgresConnectionPool;
import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Point;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StreetDaoPostgresImpl implements StreetDao {

    @Inject
    private PostgresConnectionPool connectionPool;

    @Override
    public Street find(Point point) {
        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setReadOnly(true);

            final String sql = "SELECT streets.osm_id AS id, streets.name " +
                    "FROM streets " +
                    "WHERE streets.name IS NOT NULL " +
                    "ORDER BY ST_Distance(streets.geom, st_geomfromgeojson(?)) ASC " +
                    "LIMIT 1;";

            final String geoJson = PointMapper.pointToGeoJson(point);

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, geoJson);

                try (final ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? toStreet(rs) : null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Street toStreet(final ResultSet rs) throws SQLException {
        final Street area = new Street();
        area.setId(rs.getString("id"));
        area.setName(rs.getString("name"));
        return area;
    }
}
