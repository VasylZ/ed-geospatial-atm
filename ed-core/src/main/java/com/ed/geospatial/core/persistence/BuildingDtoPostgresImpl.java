package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.persistence.model.Building;
import com.ed.geospatial.core.persistence.model.PointMapper;
import com.ed.geospatial.core.postgres.PostgresConnectionPool;
import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Point;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BuildingDtoPostgresImpl implements BuildingDto {

    @Inject
    private PostgresConnectionPool connectionPool;

    @Override
    public Building find(Point point) {
        try (final Connection conn = connectionPool.getDataSource()) {
            conn.setReadOnly(true);

            final String sql = "SELECT buildings.osm_id AS id, buildings.name, buildings.type " +
                    "FROM buildings " +
                    "WHERE st_contains(buildings.geom, st_geomfromgeojson(?)) " +
                    "LIMIT 1;";

            final String geoJson = PointMapper.pointToGeoJson(point);

            try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, geoJson);

                try (final ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? toBuilding(rs) : null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Building toBuilding(final ResultSet rs) throws SQLException {
        final Building area = new Building();
        area.setId(rs.getString("id"));
        area.setName(rs.getString("name"));
        area.setType(rs.getString("type"));
        return area;
    }
}
