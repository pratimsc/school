import models.School
import models.common.Address
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

val json = Json.parse(
  """
    [{"status":"A","school_name":"Chinga Chnoplkooo","school_address":{"first_line":"Apartment 65","city":"MANCHESTER","county":"Cheshire","country":"United Kingdom","zip_code":"M14 4LB"},"_id":"schools/9677062879","_rev":"9677062879","_key":"9677062879"},{"status":"A","name":"Mongo Bongo School","address":{"country":"GB","first_line":"1st Avenue","second_line":"2nd Avenue","city":"Maya City","county":"Gondura County","zip_code":"UK11 9BB"},"_id":"schools/517096159","_rev":"574374623","_key":"517096159"}]
  """)

import models.SchoolHelper.schoolJsonReads
import models.common.AddressHelper.addressJsonReads

val scl = (json) (1).validate[School]