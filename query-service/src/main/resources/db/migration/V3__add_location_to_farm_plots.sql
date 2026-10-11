-- V3__add_location_to_farm_plots.sql
-- Add location column to farm_plots for parcel-specific advisory context

ALTER TABLE farm_plots ADD COLUMN IF NOT EXISTS location VARCHAR(255);
