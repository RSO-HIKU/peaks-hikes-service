-- Helper function: link a single peak to trails within 50m
CREATE OR REPLACE FUNCTION link_peak_to_nearby_trails(p_peak_id bigint)
RETURNS void AS $$
BEGIN
  INSERT INTO peaks_hikes_service.trails_peaks (trail_id, peak_id)
  SELECT t.id, p_peak_id
  FROM peaks_hikes_service.trails t
  JOIN peaks_hikes_service.peaks p ON p.id = p_peak_id
  WHERE ST_DWithin(t.geometry::geography, p.geom::geography, 50)
  ON CONFLICT DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- Trigger for peaks insert
CREATE OR REPLACE FUNCTION trg_peak_insert_link()
RETURNS trigger AS $$
BEGIN
  PERFORM link_peak_to_nearby_trails(NEW.id);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;



-- Trigger for trails insert (reuse the same helper, but invert)
CREATE OR REPLACE FUNCTION trg_trail_insert_link()
RETURNS trigger AS $$
BEGIN
  INSERT INTO peaks_hikes_service.trails_peaks (trail_id, peak_id)
  SELECT NEW.id, p.id
  FROM peaks_hikes_service.peaks p
  WHERE ST_DWithin(NEW.geometry::geography, p.geom::geography, 50)
  ON CONFLICT DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;



