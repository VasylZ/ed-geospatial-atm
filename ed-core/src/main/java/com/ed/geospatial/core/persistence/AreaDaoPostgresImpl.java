package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.persistence.model.Area;
import com.ed.geospatial.core.persistence.model.PointMapper;
import com.ed.geospatial.core.postgres.PostgresConnectionPool;
import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Point;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AreaDaoPostgresImpl implements AreaDao {

    @Inject
    private PostgresConnectionPool connectionPool;

    @Override
    public Area find(Point point) {
        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setReadOnly(true);

            final String sql = "SELECT areas.id as id, areas.name1 as name " +
                    "FROM areas " +
                    "WHERE st_contains(areas.geom, st_geomfromgeojson(?)) " +
                    "LIMIT 1;";

            final String geoJson = PointMapper.pointToGeoJson(point);

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, geoJson);

                try (final ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? toArea(rs) : null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Area toArea(final ResultSet rs) throws SQLException {
        final Area area = new Area();
        area.setId(rs.getInt("id"));
        area.setName(rs.getString("name"));
        return area;
    }
}
