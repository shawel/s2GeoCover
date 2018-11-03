# Area Cover

*Converts GeoJson Geometry to S2 Cover both interior and exterior bounds. Polygon and MultiPolygon supported*


### Example

```  val areaCover = new AreaCover
       val features = areaCover.importGeoJson("/path/to/geojson")
       val s2Regions = areaCover.toS2Geometry(features)
       val s2CellTokens = s2Regions.map(areaCover.s2Cover(_))
```

## Test
``` sbt test ```



## Area Cover

*Converts GeoJson Geometry to S2 Cover both interior and exterior bounds. Polygon and MultiPolygon supported*


### Example

```  val areaCover = new AreaCover
       val features = areaCover.importGeoJson("/path/to/geojson")
       val s2Regions = areaCover.toS2Geometry(features)
       val s2CellTokens = s2Regions.map(areaCover.s2Cover(_))
```

### Test
`` sbt test ``

