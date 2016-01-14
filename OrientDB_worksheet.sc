import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.impls.orient.{OrientGraphFactory, OrientVertex}

import scala.collection.JavaConversions._

//Code for upoading the Country code to Orient db
val factory = new OrientGraphFactory("remote:localhost/chelford_preschool_02").setupPool(1, 10)
val graph = factory.getTx
try {
  val school_id = 4
  val status = "A"
  //val data = graph.getVertices("School.school_id", 4).iterator().hasNext
  //graph.getVerticesOfClass("School").iterator().toList.map(_.getId).mkString(",")
  //graph.getVertices("School", Array("school_id"), Array(4)).iterator().toList
  graph.getVertices("School", Array("status"), Array("A")).iterator().toList

  val sql = s"""SELECT FROM School where school_id ="${school_id}""""
  val result = graph.command(new OCommandSQL(sql)).execute[java.lang.Iterable[OrientVertex]]().toList

  result


} finally {

  if (!graph.isClosed) graph.shutdown()
}

