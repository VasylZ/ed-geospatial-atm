-- CREATE EXTENSION IF NOT EXISTS postgis;
--
-- create table streets
-- (
--     osm_id   varchar not null
--         primary key,
--     geom     geometry(MultiLineString, 4326),
--     code     integer,
--     fclass   varchar(28),
--     name     varchar(100),
--     ref      varchar(20),
--     oneway   varchar(1),
--     maxspeed integer,
--     layer    double precision,
--     bridge   varchar(1),
--     tunnel   varchar(1)
-- );
--
-- alter table streets
--     owner to postgres;
--
-- create index sidx_streets_geom
--     on streets using gist (geom);
--
-- create table buildings
-- (
--     osm_id varchar not null
--         primary key,
--     geom   geometry(MultiPolygon, 4326),
--     code   integer,
--     fclass varchar(28),
--     name   varchar(100),
--     type   varchar(20)
-- );
--
-- alter table buildings
--     owner to postgres;
--
-- create index sidx_buildings_geom
--     on buildings using gist (geom);
--
-- create table areas
-- (
--     id       serial
--         primary key,
--     geom     geometry(Polygon, 4326),
--     objectid integer,
--     globalid varchar,
--     name1    varchar
-- );
--
-- alter table areas
--     owner to postgres;
--
create table atms
(
    id          uuid                                    not null
        primary key,
    active      boolean default false,
    name        varchar                                 not null,
    address     varchar default ''''::character varying not null,
    website     varchar default ''''::character varying not null,
    point       geometry(Point, 4326)                   not null,
    properties  json    default '{}'::json not null,
    area_id     integer
        constraint atms_areas_null_fk
            references areas,
    street_id   varchar
        constraint atms_streets_null_fk
            references streets,
    building_id varchar
        constraint atms_buildings_null_fk
            references buildings
);

alter table atms
    owner to postgres;

create index atms_point_gist_index
    on atms using gist (point);

create index atms_area_id_index
    on atms (area_id);

create index atms_name_index
    on atms (name);

create index atms_street_id_index
    on atms (street_id);

create index atms_building_id_index
    on atms (building_id);

-- create index sidx_areas_geom
--     on areas using gist (geom);

ALTER TABLE atms ALTER COLUMN id TYPE VARCHAR(36);
ALTER TABLE atms ADD COLUMN version integer DEFAULT 0;