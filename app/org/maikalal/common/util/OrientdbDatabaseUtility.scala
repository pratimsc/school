/*
 * Copyright [2016] Maikalal Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
  * package org.maikalal.common.util

  * import com.orientechnologies.orient.core.sql.OCommandSQL
  * import com.tinkerpop.blueprints.impls.orient.{OrientGraph, OrientGraphNoTx, OrientVertex}
  * import play.api.{Logger, Play}

  * /**
  * Created by pratimsc on 12/01/16.
  */
  * object OrientdbDatabaseUtility {

  * import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory

  * //Code for upoading the Country code to Orient db
  * lazy val graphFactory = new OrientGraphFactory(Play.current.configuration.getString("orientdb.db.url").get).setupPool(
  * Play.current.configuration.getInt("orientdb.db.pool.iMin").get,
  * Play.current.configuration.getInt("orientdb.db.pool.iMax").get)

  * /**
    * A helper function to execute database commands and also carry out house keeping
    * @param f
    */
  * def executeWithTransaction[A](graph: OrientGraph)(f: (OrientGraph) => A): Option[A] = try {
  * val res = f(graph)
  * graph.commit()
  * Some(res)
  * } catch {
  * case t: Throwable => graph.rollback()
  * Logger.error("Failed executing transaction controlled query on Graph", t)
  * None
  * } finally {
  * if (!graph.isClosed) graph.shutdown()
  * }

  * /**
    * A helper function to execute database commands without transactions
    */
  * def executeWithNoTransaction[A](graph: OrientGraphNoTx)(f: (OrientGraphNoTx) => A): Option[A] = try {
  * Some(f(graph))
  * } catch {
  * case t: Throwable => graph.rollback()
  * Logger.error("Failed executing transaction less query on Graph", t)
  * None
  * } finally {
  * graph.shutdown()
  * }

  * def getUniqueAddressId: Option[Long] = getUniqueId("address_id")

  * def getUniqueSchoolId: Option[Long] = getUniqueId("school_id")

  * private def getUniqueId(name: String): Option[Long] = executeWithTransaction[Long](graphFactory.getTx)(g => {
  * val sql = s"UPDATE IdCounter INCREMENT value = 1 return after $$current.value.asLong() WHERE name = '${name}'"
  * val result = g.command(new OCommandSQL(sql)).execute[java.lang.Iterable[OrientVertex]]()
  * val id: Long = result.iterator.next().getProperty("value").asInstanceOf[Long]
  * id
  * })

  * def caseClassToPropertyMap[T](o: T): Map[String, Any] = {
  * val m = o.getClass.getDeclaredFields.sortWith((a, b) => a.hashCode() < b.hashCode()).foldRight(Map[String, Any]())((field, map) => {
  * field.setAccessible(true)
  * field.get(o) match {
  * case Some(v) => map + (field.getName -> v)
  * case None => map
  * case a: Any => map + (field.getName -> a)
  * }
  * })
  * m
  * }
  * }

  */