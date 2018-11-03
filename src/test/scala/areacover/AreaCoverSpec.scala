package areacover

import java.io.FileNotFoundException

import org.scalatest._

class AreaCoverSpec extends FlatSpec with Matchers {

  "Area Cover" should "Import from GeoJson Feature Collection from File" in {
    val areaCover = new AreaCover
    val features = areaCover.importGeoJson("./zips100.json")
    features.length should be >= (1)

  }

  "Area Cover" should "throw java.io.FileNotFoundException on invalid file path" in {
    val areaCover = new AreaCover
    a[FileNotFoundException] should be thrownBy {
      val features = areaCover.importGeoJson("/invalid/path/Downloads/map.json")

    }
  }

  "Area Cover" should "Support Polygon s2 Cover (w/wo holes)" in {
    val areaCover = new AreaCover
    val features = areaCover.importGeoJson("./zips100.json")
    val onlyPolygons = features.filter(_.getGeometry.getType == "Polygon")
    val s2Region = areaCover.toS2Geometry(onlyPolygons).get(0)

    areaCover.s2Cover(s2Region).size should ( be >= (1) and be  <= (100)) /// 100 is default
    areaCover.s2Cover(s2Region, 500).size should (be >= (1) and be >= (500))

  }


  "Area Cover" should "Support MultiPolygon s2 Cover" in {
    val areaCover = new AreaCover
    val features = areaCover.importGeoJson("./zips100.json")
    val onlyMultiPolygons = features.filter(_.getGeometry.getType == "MultiPolygon")
    val s2Region = areaCover.toS2Geometry(onlyMultiPolygons).get(0)

    areaCover.s2Cover(s2Region).size should  ( be >= (1) and be  <= (100)) /// 100 is default
    areaCover.s2Cover(s2Region, 500).size should (be >= (1) and be >= (500))

  }

  "Area Cover" should "exterior cover for polygon should be less than interior cover" in {
    val areaCover = new AreaCover
    val features = areaCover.importGeoJson("./zips100.json")
    val onlyMultiPolygons = features.filter(_.getGeometry.getType == "Polygon")
    val s2Region = areaCover.toS2Geometry(onlyMultiPolygons).get(0)

    val exterior = areaCover.s2Cover(s2Region, false).size
    val interior = areaCover.s2Cover(s2Region, true).size

    exterior should be < interior

  }

}
