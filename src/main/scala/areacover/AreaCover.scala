package areacover

// simple helper file on shape geojson to s2 covers used mostly for indexing
// 2018 shawel negussie

import java.util
import scala.collection.JavaConversions._
import scala.io.Source
import org.locationtech.jts.geom.{ Coordinate, Geometry, Polygon, MultiPolygon }
import com.github.davidmoten.geo.GeoHash
import org.wololo.jts2geojson.GeoJSONReader
import org.wololo.geojson.{ Feature, FeatureCollection, GeoJSONFactory }
import com.google.common.geometry._

case class CustomException(message: String) extends Exception(message)

class AreaCover extends Serializable {

  def importGeoJson(filePath: String): Array[Feature] = {
    val geojsonSource = Source.fromFile(filePath) // handle shape string ??
    val shape = geojsonSource.mkString //geojsonSource.getLines mkString "\n"
    geojsonSource.close()
    // println(shape)
    this.getFeatures(shape)
  }

  def toS2Geometry(featureCollection: Array[Feature]): util.ArrayList[S2Region] = {
    val geoJsonReader = new GeoJSONReader()
    val regionList = new util.ArrayList[S2Region]()
    for (feature <- featureCollection) {
      val geometry = geoJsonReader.read(feature.getGeometry)
      val s2Geometry = this.toS2Geometry(geometry)
      regionList.add(s2Geometry)
    }
    regionList
  }

  def toS2Geometry(geometry: Geometry): S2Region = {
    geometry.getGeometryType match {
      case "Polygon" => this.toS2Polygon(geometry.asInstanceOf[Polygon])
      case "MultiPolygon" => this.toS2MultiPolygon(geometry.asInstanceOf[MultiPolygon])
      case _ => throw CustomException("UnSupported Geometry")
    }
  }

  // geojson file
  def getFeatures(shape: String): Array[Feature] = {
    val shapeFile = GeoJSONFactory.create(shape);
    if (shapeFile.getType != "FeatureCollection") {
      throw CustomException("Shape File not Feature Collection ")
    }

    shapeFile.asInstanceOf[FeatureCollection].getFeatures
  }

  def geometryToBBox(shape: Geometry) {
    shape.getEnvelopeInternal
  }

  def geoHashCoverBBox(feature: Feature): util.Set[String] = {
    val geoJsonReader = new GeoJSONReader()
    val envelope = geoJsonReader.read(feature.getGeometry).getEnvelopeInternal
    GeoHash.coverBoundingBox(envelope.getMaxY, envelope.getMinX, envelope.getMinY, envelope.getMaxX).getHashes

  }

  // only bounding box for now mostly for density comparisons
  def geoHashCover(region: S2Region): util.Set[String] = {
    val envelope = region.getRectBound
    val maxY = envelope.hi.latDegrees()
    val minY = envelope.lo.latDegrees()
    val maxX = envelope.hi.lngDegrees()
    val minX = envelope.lo().lngDegrees()
    GeoHash.coverBoundingBox(maxY, minX, minY, maxX).getHashes

  }

  // interior coverings are expensive if shapes mostly dont have rings opt for cover.
  def s2Cover(geometry: S2Region): util.Set[String] = this.s2Cover(geometry, true, 100)

  def s2Cover(geometry: S2Region, interior: Boolean): util.Set[String] = this.s2Cover(geometry, interior, 100)

  def s2Cover(geometry: S2Region, cellSize: Integer): util.Set[String] = this.s2Cover(geometry, true, cellSize)

  def s2Cover(geometry: S2Region, interior: Boolean, cellSize: Integer): util.Set[String] = {
    val coverer = new S2RegionCoverer()

    coverer.setMinLevel(1) // expose levels ??
    coverer.setMaxCells(Predef.Integer2int(cellSize))

    val coverage = if (interior) coverer.getInteriorCovering(geometry) else coverer.getCovering(geometry)
    val tokens = new util.HashSet[String]()

    coverage.cellIds().foreach((cellId: S2CellId) => tokens.add(cellId.toToken))
    tokens
  }

  def toS2Polygon(geometry: Polygon): S2Polygon = {
    val loops = new util.ArrayList[S2Loop]()
    val rings = geometry.getNumInteriorRing

    for (i <- 0 to rings - 1) {
      val s2Points = new util.ArrayList[S2Point]
      val ring = geometry.getInteriorRingN(i)
      val coordinates = ring.getCoordinates
      coordinates
        .take(n = coordinates.size - 1)
        .foreach((c: Coordinate) => s2Points.add(S2LatLng.fromDegrees(c.y, c.x).toPoint))
      val loop = new S2Loop(s2Points)

      loop.setDepth(1) ///  multi depth rings  not supported
      loop.normalize()
      loops.add(loop)
    }

    val s2ExteriorPoints = new util.ArrayList[S2Point]
    val coordinates = geometry.getExteriorRing.getCoordinates

    for (i <- 0 to coordinates.size - 2) { // remove duplicate/closing coordinate
      val coordinate = coordinates(i)
      val latLng = S2LatLng.fromDegrees(coordinate.y, coordinate.x)
      s2ExteriorPoints.add(latLng.toPoint)
    }

    val loop = new S2Loop(s2ExteriorPoints)
    loop.setDepth(0)
    loop.normalize()
    loops.add(loop)

    val polygon = new S2Polygon()
    polygon.init(loops)

    polygon

  }

  def toS2MultiPolygon(geometry: MultiPolygon): S2Polygon = {

    val s2Polygons = new util.ArrayList[S2Polygon]()

    for (i <- 0 to geometry.getNumGeometries - 1) {
      val partGeometry = geometry.getGeometryN(i)
      val s2Geometry = this.toS2Geometry(partGeometry)
      s2Polygons.add(s2Geometry.asInstanceOf[S2Polygon])
    }

    val s2Polygon = S2Polygon.destructiveUnion(s2Polygons)
    s2Polygon

  }
}
