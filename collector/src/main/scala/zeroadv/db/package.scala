package zeroadv

import reactivemongo.bson.{BSONBinary, BSONDateTime, BSONHandler}
import org.joda.time.{DateTimeZone, DateTime}
import reactivemongo.bson.Subtype.GenericBinarySubtype

package object db extends Utils {
  implicit object DateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def write(t: DateTime) = BSONDateTime(t.getMillis)
    def read(bson: BSONDateTime) = new DateTime(bson.value).withZone(DateTimeZone.UTC)
  }

  implicit object ByteArrayHandler extends BSONHandler[BSONBinary, Array[Byte]] {
    def write(t: Array[Byte]) = BSONBinary(t, GenericBinarySubtype)
    def read(bson: BSONBinary) = bson.value.readArray(bson.value.size)
  }
}
