# Peaks-Hikes Service

The Peaks-Hikes Service is the authoritative source for geographic data related to peaks and hiking trails within the HIKU hiking application, utilizing PostGIS for advanced spatial queries and data management. It stores peak information (coordinates, elevation, territory) and trail geometry as LineString objects, enabling efficient geospatial calculations for proximity-based linking between peaks and trails. The service exposes both REST and gRPC endpoints for seamless integration with other microservices like Badge Service, and automatically establishes relationships between peaks and trails based on spatial proximity.

For full documentation see: [Docs](./documentation.md)
