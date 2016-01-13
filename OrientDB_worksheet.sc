import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import models.common.reference.Reference
import scala.collection.JavaConverters._

//Code for upoading the Country code to Orient db
val factory = new OrientGraphFactory("remote:localhost/chelford_preschool_01").setupPool(1, 10)
val graph = factory.getTx
try {
  val sc1 = graph.addVertex("School", null)
  sc1.setProperty("school_id", "SC1")
  sc1.setProperty("name", "Mongo Bongo School")
  sc1.setProperty("status", Reference.STATUS.ACTIVE)
  val add1 = graph.addVertex("Address", null)
  add1.setProperty("first_line", "1st Line of Mongo")
  add1.setProperty("second_line", "2nd Line of Bongo")
  add1.setProperty("county", "Cheshire")
  add1.setProperty("country_code", "GB")
  add1.setProperty("zip_code", "M14 8PP")
  val add2 = graph.addVertex("Address", null)
  add2.setProperty("first_line", "1st Line of Changu")
  add2.setProperty("second_line", "2nd Line of Mangu")
  add2.setProperty("county", "Cheshire")
  add2.setProperty("country_code", "GB")
  add2.setProperty("zip_code", "H89 S09")

  sc1.addEdge("has_address", add1, "has_address", null, Map("status" -> "H", "since" -> "1979-01-02", "until" -> "2005-12-31").asJava)
  sc1.addEdge("has_address", add2, "has_address", null, Map("status" -> "A", "since" -> "2006-01-01", "until" -> "3599-12-31").asJava)
  graph.commit()

} finally {
  graph.rollback()
  if (!graph.isClosed) graph.shutdown()
}