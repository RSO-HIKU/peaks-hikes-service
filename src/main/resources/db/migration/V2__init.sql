CREATE TABLE peaks_hikes_service.trails (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    length_km FLOAT,
    geometry GEOMETRY(LineString, 4326) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    source_file TEXT
);

CREATE TABLE peaks_hikes_service.peaks (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    territory VARCHAR,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    elevation_m DOUBLE PRECISION,
    geom GEOMETRY(Point, 4326) GENERATED ALWAYS AS (
        ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
    ) STORED
);

CREATE INDEX idx_peaks_geom
ON peaks_hikes_service.peaks
USING GIST (geom);

CREATE INDEX idx_trails_geom
ON peaks_hikes_service.trails
USING GIST (geometry);

CREATE TABLE peaks_hikes_service.trails_peaks (
    trail_id INTEGER NOT NULL,
    peak_id INTEGER NOT NULL,
    PRIMARY KEY (trail_id, peak_id),
    CONSTRAINT fk_trail FOREIGN KEY (trail_id) REFERENCES peaks_hikes_service.trails(id) ON DELETE CASCADE,
    CONSTRAINT fk_peak FOREIGN KEY (peak_id) REFERENCES peaks_hikes_service.peaks(id) ON DELETE CASCADE
);